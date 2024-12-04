package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.dto.cabinet.*;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.*;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.enums.DisableEnums;
import cn.iocoder.yudao.framework.common.enums.PduBoxEnums;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cabinet.enums.CabinetLoadEnums;
import cn.iocoder.yudao.module.cabinet.mapper.RackIndexMapper;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.*;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜页面
 * @date 2024/4/28 14:25
 */
@Slf4j
@Service
public class CabinetServiceImpl implements CabinetService {

    @Autowired
    CabinetCfgMapper cabinetCfgMapper;
    @Autowired
    CabinetIndexMapper cabinetIndexMapper;
    @Autowired
    CabinetPduMapper cabinetPduMapper;

    @Autowired
    CabinetEnvSensorMapper envSensorMapper;
    @Autowired
    RoomIndexMapper roomIndexMapper;
    @Autowired
    RackIndexMapper rackIndexMapper;
    @Autowired
    CabinetBusMapper cabinetBusMapper;
    @Autowired
    BoxIndexMapper boxIndexMapper;

    @Autowired
    AisleIndexMapper aisleIndexMapper;

    @Autowired
    RedisTemplate redisTemplate;


    @Value("${cabinet-refresh-url}")
    public String adder;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult<JSONObject> getPageCabinet(CabinetIndexVo vo) {
        try {
            Page<CabinetIndexDTO> page = new Page<>(vo.getPageNo(), vo.getPageSize());
            //获取机柜列表
            if (Objects.nonNull(vo.getCabinetIds()) && CollectionUtils.isEmpty(vo.getCabinetIds())){
                List<Integer> list = new ArrayList<>();
                list.add(-1);
                vo.setCabinetIds(list);
            }

            Page<CabinetIndexDTO> indexDTOPage = cabinetCfgMapper.selectCabList(page, vo);
            List<JSONObject> result = new ArrayList<>();
            //获取redis 实时数据
            if (Objects.nonNull(indexDTOPage) && !CollectionUtils.isEmpty(indexDTOPage.getRecords())) {
                indexDTOPage.getRecords().forEach(dto -> {
                    String key = REDIS_KEY_CABINET + dto.getRoomId() + SPLIT_KEY + dto.getId();
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
                    if (Objects.nonNull(jsonObject)) {
                        jsonObject.put(COMPANY, Objects.nonNull(dto.getCompany()) ? dto.getCompany() : "");
                        result.add(jsonObject);
                    }
                });
            }
            return new PageResult<>(result, indexDTOPage.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);

    }

    @Override
    public JSONObject getCabinetDetail(int id) {

        try {

            CabinetIndex index = cabinetIndexMapper.selectById(id);
            //获取redis数据
            String key = REDIS_KEY_CABINET + index.getRoomId() + SPLIT_KEY + id;
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
            return jsonObject;
        } catch (Exception e) {
            log.error("获取基础数据失败: ", e);
        }
        return new JSONObject();
    }

    @Override
    public CabinetDTO getCabinetDetailV2(int id) {
        CabinetDTO dto = new CabinetDTO();

        try {
            //获取数据库保存数据
            CabinetIndex index = cabinetIndexMapper.selectById(id);
            if (Objects.nonNull(index)) {

                dto.setId(id);
                dto.setCabinetName(index.getName());
                dto.setAisleId(index.getAisleId());
                dto.setRoomId(index.getRoomId());
                dto.setPowCapacity(index.getPowCapacity());
                dto.setRunStatus(index.getRunStatus());
                dto.setPduBox(index.getPduBox());
                dto.setEleAlarmDay(index.getEleAlarmDay());
                dto.setEleAlarmMonth(index.getEleAlarmMonth());
                dto.setEleLimitDay(index.getEleLimitDay());
                dto.setEleLimitMonth(index.getEleLimitMonth());

                //机房信息
                RoomIndex roomIndex = roomIndexMapper.selectById(index.getRoomId());
                dto.setRoomName(roomIndex.getName());

                //基本配置信息
                CabinetCfg cfg = cabinetCfgMapper.selectOne(new LambdaQueryWrapper<CabinetCfg>()
                        .eq(CabinetCfg::getCabinetId, index.getId()));
                if (Objects.nonNull(cfg)) {
                    dto.setCabinetHeight(cfg.getCabinetHeight());
                    dto.setType(cfg.getType());
                    dto.setXCoordinate(cfg.getXCoordinate());
                    dto.setYCoordinate(cfg.getYCoordinate());
                    dto.setCompany(cfg.getCompany());
                }
                //配置的数据来源信息
                ValueOperations ops = redisTemplate.opsForValue();

                if (index.getPduBox() == PduBoxEnums.PDU.getValue()){
                    //来源pdu
                    CabinetPdu pdu = cabinetPduMapper.selectOne(new LambdaQueryWrapper<CabinetPdu>()
                            .eq(CabinetPdu::getCabinetId, index.getId()));

                    if (Objects.nonNull(pdu)) {
                        dto.setPduIpA(pdu.getPduIpA());
                        dto.setPduIpB(pdu.getPduIpB());
                        dto.setCasIdA(pdu.getCasIdA());
                        dto.setCasIdB(pdu.getCasIdB());

                        //获取pdu数据
                        StringBuilder aKey = new StringBuilder();
                        aKey.append(REDIS_KEY_PDU);
                        aKey.append(pdu.getPduIpA());
                        aKey.append(SPLIT_KEY);
                        aKey.append(pdu.getCasIdA());

                        Object aPdu = ops.get(aKey.toString());
                        if (Objects.nonNull(aPdu)){
                            JSONObject aPduJson = JSON.parseObject(JSON.toJSONString(aPdu));
                            if (aPduJson.containsKey(PDU_DATA)) {
                                JSONObject pduData = aPduJson.getJSONObject(PDU_DATA);
                                if (pduData.containsKey(OUTPUT_ITEM_LIST)) {
                                    JSONObject outData = pduData.getJSONObject(OUTPUT_ITEM_LIST);
                                    //电流
                                    double[] curs = outData.getObject(CUR_VALUE, double[].class);
                                    if (Objects.nonNull(curs)) {
                                        dto.setOutletA(curs.length);
                                    }
                                }
                            }

                        }
                        StringBuilder bKey = new StringBuilder();
                        bKey.append(REDIS_KEY_PDU);
                        bKey.append(pdu.getPduIpB());
                        bKey.append(SPLIT_KEY);
                        bKey.append(pdu.getCasIdB());

                        Object bPdu = ops.get(bKey.toString());
                        if (Objects.nonNull(bPdu)){
                            JSONObject bPduJson = JSON.parseObject(JSON.toJSONString(bPdu));
                            if (bPduJson.containsKey(PDU_DATA)) {
                                JSONObject pduData = bPduJson.getJSONObject(PDU_DATA);
                                if (pduData.containsKey(OUTPUT_ITEM_LIST)) {
                                    JSONObject outData = pduData.getJSONObject(OUTPUT_ITEM_LIST);
                                    //电流
                                    double[] curs = outData.getObject(CUR_VALUE, double[].class);
                                    if (Objects.nonNull(curs)) {
                                        dto.setOutletB(curs.length);
                                    }
                                }
                            }

                        }
                    }
                }else if (index.getPduBox() == PduBoxEnums.BUS.getValue()){
                    //配置的母线数据
                    CabinetBus cabinetBus = cabinetBusMapper.selectOne(new LambdaQueryWrapper<CabinetBus>()
                            .eq(CabinetBus::getCabinetId, index.getId()));
                    if (Objects.nonNull(cabinetBus)){
                        if (StringUtils.isNotEmpty(cabinetBus.getDevKeyA())){
                            BoxIndex  boxIndex = boxIndexMapper.selectOne(new LambdaQueryWrapper<BoxIndex>()
                                    .eq(BoxIndex::getBoxKey,cabinetBus.getDevKeyA()));

                            if (Objects.nonNull(boxIndex)){

                                dto.setBusIpA(boxIndex.getIpAddr());
//                                dto.setBusNameA(boxIndex.getBusName());
                                dto.setBoxNameA(boxIndex.getBoxName());
                                dto.setBarIdA(boxIndex.getBoxId());
//                                dto.setAddrA(boxIndex.getCasAddr());
                                dto.setBoxOutletIdA(cabinetBus.getOutletIdA());
                            }else {
                                String[] keys = cabinetBus.getDevKeyA().split(SPLIT_KEY);
                                dto.setBusIpA(keys[0]);
                                dto.setBarIdA(Integer.valueOf(keys[1]));
                                dto.setAddrA(Integer.valueOf(keys[2]));
                                dto.setBoxOutletIdA(cabinetBus.getOutletIdA());
                            }

                        }

                        if (StringUtils.isNotEmpty(cabinetBus.getDevKeyB())){
                            BoxIndex  boxIndex = boxIndexMapper.selectOne(new LambdaQueryWrapper<BoxIndex>()
                                    .eq(BoxIndex::getBoxKey,cabinetBus.getDevKeyA()));
                            if (Objects.nonNull(boxIndex)){

                                dto.setBusIpB(boxIndex.getIpAddr());
//                                dto.setBusNameB(boxIndex.getBusName());
                                dto.setBoxNameB(boxIndex.getBoxName());
                                dto.setBarIdB(boxIndex.getBoxId());
//                                dto.setAddrB(boxIndex.getCasAddr());
                                dto.setBoxOutletIdB(cabinetBus.getOutletIdB());
                            }else {
                                String[] keys = cabinetBus.getDevKeyB().split(SPLIT_KEY);
                                dto.setBusIpB(keys[0]);
                                dto.setBarIdB(Integer.valueOf(keys[1]));
                                dto.setAddrB(Integer.valueOf(keys[2]));
                                dto.setBoxOutletIdB(cabinetBus.getOutletIdB());
                            }

                        }
                        dto.setBoxIndexA(cabinetBus.getBoxIndexA());
                        dto.setBoxIndexB(cabinetBus.getBoxIndexB());
                    }
                }


                List<CabinetEnvSensor> envSensorList = envSensorMapper.selectList(new LambdaQueryWrapper<CabinetEnvSensor>()
                        .eq(CabinetEnvSensor::getCabinetId,index.getId()));
                if (!CollectionUtils.isEmpty(envSensorList)){

                    List<CabinetEnvSensorDTO> sensorDtos = new ArrayList<>();
                    envSensorList.forEach(env -> {
                        CabinetEnvSensorDTO sensorDTO = new CabinetEnvSensorDTO();
                        sensorDTO.setId(env.getId());
                        sensorDTO.setCabinetId(env.getCabinetId());
                        sensorDTO.setChannel(env.getChannel());
                        sensorDTO.setPosition(env.getPosition());
                        sensorDTO.setPathPdu(String.valueOf(env.getPathPdu()));
                        sensorDTO.setSensorId(env.getSensorId());
                        sensorDTO.setSensorType(env.getType());
                        sensorDtos.add(sensorDTO);
                    });
                   dto.setSensorList(sensorDtos);

                }

                List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                        .eq(RackIndex::getCabinetId,index.getId())
                        .eq(RackIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
                if (!CollectionUtils.isEmpty(rackIndexList)){
                    dto.setRackIndexList(rackIndexList);
                    int usedSpace = rackIndexList.stream().map(RackIndex::getUHeight).reduce(0,Integer::sum);
                    int rackNum = rackIndexList.size();
                    int freeSpace = dto.getCabinetHeight() - usedSpace;
                    dto.setUsedSpace(usedSpace);
                    dto.setRackNum(rackNum);
                    dto.setFreeSpace(freeSpace);
                }else {
                    dto.setFreeSpace(dto.getCabinetHeight());
                }

            }
            return dto;
        } catch (Exception e) {
            log.error("获取机柜信息失败：", e);
        }
        return dto;
    }



    //保存数据失败全部回滚
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult saveCabinet(CabinetVo vo) throws Exception{
        try {

            //判断pdu是否已经关联其他机柜
            if (vo.getPduBox() == PduBoxEnums.PDU.getValue()){
                List<CabinetIndex> indexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                        .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus())
                        .ne(CabinetIndex::getId, vo.getId()));
                if (!CollectionUtils.isEmpty(indexList)) {
                    List<Integer> ids = indexList.stream().map(CabinetIndex::getId).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(ids)) {
                        if (isExist(vo.getPduIpA(), vo.getCasIdA(), ids)) {
                            log.info("---- " + vo.getPduIpA() + vo.getCasIdA());
                            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "pdu已关联其他机柜");
                        }

                        if (isExist(vo.getPduIpB(), vo.getCasIdB(), ids)) {
                            log.info("---- " + vo.getPduIpB() + vo.getCasIdB());
                            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "pdu已关联其他机柜");
                        }
                    }
                }

                //判断AB路pdu是否一样
                if (StringUtils.isNotEmpty(vo.getPduIpA()) && StringUtils.isNotEmpty(vo.getPduIpB())) {
                    if (vo.getPduIpA().equals(vo.getPduIpB()) && vo.getCasIdA() == vo.getCasIdB()) {
                        return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "AB路pdu一致，请重新输入");
                    }
                }
            }
            //计算机柜位置
            if (Objects.nonNull(vo.getIndex())){
                AisleIndex aisleIndex = aisleIndexMapper.selectOne(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getId,vo.getAisleId()));

                if (Objects.nonNull(aisleIndex) && "x".equals(aisleIndex.getDirection())){
                    //横向
                    vo.setXCoordinate(aisleIndex.getXCoordinate() + vo.getIndex() - 1);
                    vo.setYCoordinate(aisleIndex.getYCoordinate());
                }
                if (Objects.nonNull(aisleIndex) && "y".equals(aisleIndex.getDirection())){
                    //纵向
                    vo.setYCoordinate(aisleIndex.getYCoordinate() + vo.getIndex() - 1);
                    vo.setXCoordinate(aisleIndex.getXCoordinate());
                }
            }

            CabinetIndex index;
            //编辑
            if (vo.getId() > 0) {
                //index 索引表
                index = cabinetIndexMapper.selectById(vo.getId());

                //修改
                cabinetIndexMapper.updateById( convertIndex(vo, index));
            } else {
                //新增
                //判断机柜名称是否重复（已删除的或者已禁用的恢复）
                index = cabinetIndexMapper.selectOne(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getName, vo.getCabinetName())
                        .eq(CabinetIndex::getRoomId, vo.getRoomId()));
                if (Objects.nonNull(index)) {
                    if (index.getIsDeleted() == DelEnums.DELETE.getStatus() || index.getIsDisabled() == DisableEnums.DISABLE.getStatus()) {
                        //index 索引表
                        //修改
                        cabinetIndexMapper.updateById(convertIndex(vo, index));
                    } else {
                        return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "机柜名称重复");
                    }

                } else {
                    index = new CabinetIndex();
                    //index 索引表
                    //新增
                    CabinetIndex cabinetIndex = convertIndex(vo, index);
//                    cabinetIndexMapper.addIndex(cabinetIndex);
                    cabinetIndexMapper.insert(cabinetIndex);
                    vo.setId(cabinetIndex.getId());
                }
            }

            log.info("vo : " + vo);

            //配置表
            CabinetCfg cfg = cabinetCfgMapper.selectOne(new LambdaQueryWrapper<CabinetCfg>()
                    .eq(CabinetCfg::getCabinetId, vo.getId()));
            if (Objects.nonNull(cfg)) {
                //修改
                cabinetCfgMapper.updateById(convertCfg(vo, cfg));
            } else {
                cfg = new CabinetCfg();

                //新增
                cabinetCfgMapper.insert(convertCfg(vo, cfg));
            }

            if (vo.getPduBox() == PduBoxEnums.PDU.getValue()){

                if (StringUtils.isEmpty(vo.getPduIpA()) && StringUtils.isEmpty(vo.getPduIpB())){

                    //删除
                    cabinetPduMapper.delete(new LambdaQueryWrapper<CabinetPdu>()
                            .eq(CabinetPdu::getCabinetId, vo.getId()));
                }

                //pdu关联表
                CabinetPdu pdu = cabinetPduMapper.selectOne(new LambdaQueryWrapper<CabinetPdu>()
                        .eq(CabinetPdu::getCabinetId, vo.getId()));


                if (Objects.nonNull(pdu)) {
                    //修改
                    cabinetPduMapper.updateById(convertPdu(vo, pdu));
                } else {
                    pdu = new CabinetPdu();
                    if (StringUtils.isNotEmpty(vo.getPduIpA()) || StringUtils.isNotEmpty(vo.getPduIpB())){

                        //新增
                        cabinetPduMapper.insert(convertPdu(vo, pdu));
                    }
                }
            }else if (vo.getPduBox() == PduBoxEnums.BUS.getValue()){

                if (Objects.isNull(vo.getAddrA()) && Objects.isNull(vo.getAddrB())){
                    //删除
                    cabinetBusMapper.delete(new LambdaQueryWrapper<CabinetBus>()
                            .eq(CabinetBus::getCabinetId, vo.getId()));
                }

                //母线关联表
                CabinetBus bus = cabinetBusMapper.selectOne(new LambdaQueryWrapper<CabinetBus>()
                        .eq(CabinetBus::getCabinetId, vo.getId()));



                if (Objects.nonNull(bus)){
                    //修改
                    cabinetBusMapper.updateById(convertBus(vo,bus));
                }else {
                    //新增
                    bus = new CabinetBus();
                    if (Objects.nonNull(vo.getAddrA()) || Objects.nonNull(vo.getAddrB())){
                        //新增
                        cabinetBusMapper.insert(convertBus(vo, bus));
                    }
                }
            }

            //保存环境数据
            saveEnvSensor(vo.getId(),vo);
            //保存U位数据
            saveRack(vo.getId(), vo);

            return CommonResult.success(vo.getId());
        } finally {
            //刷新机柜计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delCabinet(int id) throws Exception{
        try {
            CabinetIndex index = cabinetIndexMapper.selectById(id);
            if (Objects.isNull(index)) {
                return -1;
            }

            List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                    .eq(RackIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                    .eq(RackIndex::getCabinetId,index.getId()));
            if (!CollectionUtils.isEmpty(rackIndexList)){
                throw new RuntimeException("存在未删除机架，不可删除");
            }
            if (index.getIsDeleted() == DelEnums.DELETE.getStatus()) {
                //已经删除则物理删除
                cabinetIndexMapper.deleteById(id);
                //删除pdu关联关系
                cabinetPduMapper.delete(new LambdaQueryWrapper<CabinetPdu>()
                        .eq(CabinetPdu::getCabinetId, id));
                cabinetBusMapper.delete(new LambdaQueryWrapper<CabinetBus>()
                        .eq(CabinetBus::getCabinetId, id));
                //删除配置信息
                cabinetCfgMapper.delete(new LambdaQueryWrapper<CabinetCfg>()
                        .eq(CabinetCfg::getCabinetId, id));
                //删除环境信息
                envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                        .eq(CabinetEnvSensor::getCabinetId,id));
            } else {
                //逻辑删除
                cabinetIndexMapper.update(new LambdaUpdateWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getId, id)
                        .set(CabinetIndex::getIsDeleted, DelEnums.DELETE.getStatus()));
                //删除pdu关联关系
                cabinetPduMapper.delete(new LambdaQueryWrapper<CabinetPdu>()
                        .eq(CabinetPdu::getCabinetId, id));
                cabinetBusMapper.delete(new LambdaQueryWrapper<CabinetBus>()
                        .eq(CabinetBus::getCabinetId, id));
                //删除环境信息
                envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                        .eq(CabinetEnvSensor::getCabinetId,id));
            }

            //删除key
            String key = REDIS_KEY_CABINET + index.getRoomId() + SPLIT_KEY + index.getId();

            boolean flag = redisTemplate.delete(key);
            log.info("key: " + key + " flag : " + flag);

            return id;
        } finally {
            log.info("刷新计算服务缓存 --- " + adder);
            //刷新机柜计算服务缓存
            HttpUtil.get(adder);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult saveEnvCabinet(CabinetVo vo) throws Exception {
        saveEnvSensor(vo.getId(),vo);
        return CommonResult.success(vo.getId());
    }

    @Override
    public PageResult<CabinetIndexDTO> getEqPage(CabinetIndexVo vo) {
        try {
            Page<CabinetIndexDTO> page = new Page<>(vo.getPageNo(), vo.getPageSize());
            //获取机柜列表
            if (Objects.nonNull(vo.getCabinetIds()) && CollectionUtils.isEmpty(vo.getCabinetIds())){
                List<Integer> list = new ArrayList<>();
                list.add(-1);
                vo.setCabinetIds(list);
            }
            //获取机柜列表
            Page<CabinetIndexDTO> indexDTOPage = cabinetCfgMapper.selectCabList(page, vo);
            List<CabinetIndexDTO> result = new ArrayList<>();
            //获取机房数据
            if (!CollectionUtils.isEmpty(indexDTOPage.getRecords())){
                List<Integer> roomIds = indexDTOPage.getRecords().stream().map(CabinetIndexDTO::getRoomId).distinct().collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(roomIds)){
                    List<RoomIndex> roomIndexList = roomIndexMapper.selectBatchIds(roomIds);
                    if (!CollectionUtils.isEmpty(roomIndexList)){
                        Map<Integer,String>  map = roomIndexList.stream().collect(Collectors.toMap(RoomIndex::getId,RoomIndex::getName));

                        if (Objects.nonNull(map)){
                            indexDTOPage.getRecords().forEach(dto -> {
                               dto.setRoomName(map.get(dto.getRoomId()));
                               result.add(dto);
                            });
                        }
                    }
                }
            }

            List<Integer> ids = result.stream().map(CabinetIndexDTO::getId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)){
                return new PageResult<>(result, indexDTOPage.getTotal());
            }
            //昨日
            String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            String endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  yesterdayList = getData(startTime,endTime, ids,CABINET_EQ_TOTAL_DAY);
            Map<Integer,Double> yesterdayMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(yesterdayList)){
                yesterdayList.forEach(str -> {
                    CabinetEqTotalDayDo dayDo = JsonUtils.parseObject(str, CabinetEqTotalDayDo.class);
                    yesterdayMap.put(dayDo.getCabinetId(),dayDo.getEqValue());
                });
            }

            //上周
            startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
            endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  weekList = getData(startTime,endTime, ids,CABINET_EQ_TOTAL_WEEK);
            Map<Integer,Double> weekMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(weekList)){
                weekList.forEach(str -> {
                    CabinetEqTotalWeekDo weekDo = JsonUtils.parseObject(str, CabinetEqTotalWeekDo.class);
                    weekMap.put(weekDo.getCabinetId(),weekDo.getEqValue());
                });
            }

            //上月
            startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
            endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  monthList = getData(startTime,endTime, ids,CABINET_EQ_TOTAL_MONTH);
            Map<Integer,Double> monthMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(monthList)){
                monthList.forEach(str -> {
                    CabinetEqTotalMonthDo monthDo = JsonUtils.parseObject(str, CabinetEqTotalMonthDo.class);
                    monthMap.put(monthDo.getCabinetId(),monthDo.getEqValue());
                });
            }

            result.forEach(dto -> {
                dto.setYesterdayEq(yesterdayMap.get(dto.getId()));
                dto.setLastWeekEq(weekMap.get(dto.getId()));
                dto.setLastMonthEq(monthMap.get(dto.getId()));
            });

            return new PageResult<>(result, indexDTOPage.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);

    }

    @Override
    public PageResult<CabinetIndexDTO> getCapacityPage(CabinetIndexVo vo) {
        try {
            Page<CabinetIndexDTO> page = new Page<>(vo.getPageNo(), vo.getPageSize());
            //获取机柜列表
            if (Objects.nonNull(vo.getCabinetIds()) && CollectionUtils.isEmpty(vo.getCabinetIds())){
                List<Integer> list = new ArrayList<>();
                list.add(-1);
                vo.setCabinetIds(list);
            }
            //获取机柜列表
            Page<CabinetIndexDTO> indexDTOPage = cabinetCfgMapper.selectCabList(page, vo);
            List<CabinetIndexDTO> result = new ArrayList<>();
            //获取机房数据
            if (!CollectionUtils.isEmpty(indexDTOPage.getRecords())){
                List<Integer> roomIds = indexDTOPage.getRecords().stream().map(CabinetIndexDTO::getRoomId).distinct().collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(roomIds)){
                    List<RoomIndex> roomIndexList = roomIndexMapper.selectBatchIds(roomIds);
                    if (!CollectionUtils.isEmpty(roomIndexList)){
                        Map<Integer,String>  map = roomIndexList.stream().collect(Collectors.toMap(RoomIndex::getId,RoomIndex::getName));

                        if (Objects.nonNull(map)){
                            indexDTOPage.getRecords().forEach(dto -> {
                                dto.setRoomName(map.get(dto.getRoomId()));
                                result.add(dto);
                            });
                        }
                    }
                }
            }

            List<Integer> ids = result.stream().map(CabinetIndexDTO::getId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)){
                return new PageResult<>(result, indexDTOPage.getTotal());
            }
            //获取机架列表
            List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                    .eq(RackIndex::getIsDelete,DelEnums.NO_DEL.getStatus())
                    .in(RackIndex::getCabinetId,ids));

            if (!CollectionUtils.isEmpty(rackIndexList)){
                Map<Integer,List<RackIndex>> cabRacks = rackIndexList.stream().collect(Collectors.groupingBy(RackIndex::getCabinetId));

                result.forEach(dto -> {
                    List<RackIndex> racks = cabRacks.get(dto.getId());
                    if (!CollectionUtils.isEmpty(racks)){
                        int usedSpace = racks.stream().map(RackIndex::getUHeight).reduce(0,Integer::sum);
                        int rackNum = racks.size();
                        int freeSpace = dto.getCabinetHeight() - usedSpace;
                        dto.setUsedSpace(usedSpace);
                        dto.setRackNum(rackNum);
                        dto.setFreeSpace(freeSpace);
                    }
                });

            }

            result.forEach(dto -> {
               if (dto.getUsedSpace() == 0){
                   dto.setFreeSpace(dto.getCabinetHeight());
               }
            });
            return new PageResult<>(result, indexDTOPage.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);

    }

    @Override
    public Map<Integer, Integer> loadStatusCount() {
        Map<Integer,Integer> map = new HashMap<>();
        for (int i = 0; i < CabinetLoadEnums.values().length; i++) {
            map.put(CabinetLoadEnums.values()[i].getStatus(),0);
        }
        LambdaQueryWrapper<CabinetIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CabinetIndex::getLoadStatus,CabinetIndex::getCount);
        queryWrapper.groupBy(CabinetIndex::getLoadStatus);
        List<CabinetIndex> list = cabinetIndexMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(list)){
            Map<Integer,Integer>  statusMap = list.stream().collect(Collectors.toMap(CabinetIndex::getLoadStatus,CabinetIndex::getCount));
            statusMap.keySet().forEach(key -> map.put(key,statusMap.get(key)));
        }
        return map;
    }

    /**
     * 获得已删除机柜分页
     * @param pageReqVO
     * @return
     */
    @Override
    public PageResult<CabinetIndexVo> getDeletedCabinetPage(CabinetIndexVo pageReqVO) {

        LambdaQueryWrapper<CabinetIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CabinetIndex::getIsDeleted,0);
        List<CabinetIndex> cabinetIndices = cabinetIndexMapper.selectList(queryWrapper);
        System.out.println(cabinetIndices);
        System.out.println(cabinetIndices.toString());
        return null;
    }

    /**
     * 机柜配电状态统计
     * @return
     */
    @Override
    public PageResult<JSONObject> getCabinetRunStatus() {
        Map<String, Integer> statusCounts = new HashMap<>();
        //查询全部的机柜配电状态
        List<CabinetIndexDTO> cabinetCfgs = cabinetCfgMapper.selectRunStatus();
        cabinetCfgs.forEach(dto -> {
            String key = REDIS_KEY_CABINET + dto.getRoomId() + SPLIT_KEY + dto.getId();
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
            if (Objects.nonNull(jsonObject)) {
                String status = jsonObject.getString("status");
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }
        });
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> result = new ArrayList<>();
        jsonObject.put("sumNoload", 0);
        jsonObject.put("sumNormal", 0);
        jsonObject.put("sumEarly", 0);
        jsonObject.put("sumInform", 0);
        statusCounts.forEach((status, count) -> {
            switch (status) {
                case "0":
                    jsonObject.put("sumNoload", count);
                    break;
                case "1":
                    jsonObject.put("sumNormal", count);
                    break;
                case "2":
                    jsonObject.put("sumEarly", count);
                    break;
                case "3":
                    jsonObject.put("sumInform", count);
                    break;
            }
        });
        result.add(jsonObject);
        return new PageResult<>(result);
    }

    /**
     * 实体转换
     *
     * @param vo
     * @param index
     * @return
     */
    private CabinetIndex convertIndex(CabinetVo vo, CabinetIndex index) {
        CabinetIndex cabinetIndex = new CabinetIndex();
        cabinetIndex.setAisleId(vo.getAisleId());
        cabinetIndex.setName(vo.getCabinetName());
        cabinetIndex.setPduBox(vo.getPduBox());
        //未删除
        cabinetIndex.setIsDeleted(DelEnums.NO_DEL.getStatus());
        //未禁用
        cabinetIndex.setIsDisabled(DisableEnums.ENABLE.getStatus());
        cabinetIndex.setPowCapacity(vo.getPowCapacity());
        cabinetIndex.setRoomId(vo.getRoomId());
        cabinetIndex.setId(index.getId());
        cabinetIndex.setEleAlarmDay(vo.getEleAlarmDay());
        cabinetIndex.setEleAlarmMonth(vo.getEleAlarmMonth());
        cabinetIndex.setEleLimitDay(vo.getEleLimitDay());
        cabinetIndex.setEleLimitMonth(vo.getEleLimitMonth());
        return cabinetIndex;
    }

    /**
     * 实体转换
     *
     * @param vo
     * @param pdu
     * @return
     */
    private CabinetPdu convertPdu(CabinetVo vo, CabinetPdu pdu) {
        CabinetPdu cabinetPdu = new CabinetPdu();
        cabinetPdu.setCabinetId(vo.getId());
        cabinetPdu.setPduIpA(vo.getPduIpA());
        cabinetPdu.setPduIpB(vo.getPduIpB());
        cabinetPdu.setCasIdB(vo.getCasIdB());
        cabinetPdu.setCasIdA(vo.getCasIdA());
        cabinetPdu.setId(pdu.getId());
        return cabinetPdu;
    }
    /**
     * 实体转换
     *
     * @param vo
     * @param bus
     * @return
     */
    private CabinetBus convertBus(CabinetVo vo, CabinetBus bus) {
        CabinetBus cabinetBus = new CabinetBus();
        cabinetBus.setCabinetId(vo.getId());
        if (StringUtils.isNotEmpty(vo.getBusIpA())
                && Objects.nonNull(vo.getBarIdA())
                && Objects.nonNull(vo.getAddrA())){
            cabinetBus.setDevKeyA(vo.getBusIpA()
                    .concat(SPLIT_KEY)
                    .concat(String.valueOf(vo.getBarIdA()))
                    .concat(SPLIT_KEY)
                    .concat(String.valueOf(vo.getAddrA())));
        }else {
            cabinetBus.setDevKeyA("");
        }

        cabinetBus.setOutletIdA(vo.getBoxOutletIdA());
        if (StringUtils.isNotEmpty(vo.getBusIpB())
                && Objects.nonNull(vo.getBarIdB())
                && Objects.nonNull(vo.getAddrB())){
            cabinetBus.setDevKeyB(vo.getBusIpB()
                    .concat(SPLIT_KEY)
                    .concat(String.valueOf(vo.getBarIdB()))
                    .concat(SPLIT_KEY)
                    .concat(String.valueOf(vo.getAddrB())));
        }else {
            cabinetBus.setDevKeyB("");
        }
        cabinetBus.setOutletIdB(vo.getBoxOutletIdB());
        cabinetBus.setId(bus.getId());
        cabinetBus.setBoxIndexA(vo.getBoxIndexA());
        cabinetBus.setBoxIndexB(vo.getBoxIndexB());
        return cabinetBus;
    }


    /**
     * 实体转换
     *
     * @param vo
     * @param cfg
     * @return
     */
    private CabinetCfg convertCfg(CabinetVo vo, CabinetCfg cfg) {
        CabinetCfg cabinetCfg = new CabinetCfg();
        cabinetCfg.setCabinetId(vo.getId());
        cabinetCfg.setCabinetHeight(vo.getCabinetHeight());
        cabinetCfg.setCabinetName(vo.getCabinetName());
        cabinetCfg.setCompany(vo.getCompany());
        cabinetCfg.setType(vo.getType());
        cabinetCfg.setXCoordinate(vo.getXCoordinate());
        cabinetCfg.setYCoordinate(vo.getYCoordinate());
        cabinetCfg.setId(cfg.getId());
        return cabinetCfg;
    }

    /**
     * 判断pdu是否已经绑定
     *
     * @param ip
     * @param cas
     * @param ids
     * @return
     */
    private boolean isExist(String ip, Integer cas, List<Integer> ids) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }
        List<CabinetPdu> pduFlag = cabinetPduMapper.selectList(new LambdaQueryWrapper<CabinetPdu>()
                .in(CabinetPdu::getCabinetId, ids)
                .and((wq -> wq.and(qr -> qr.eq(CabinetPdu::getPduIpA, ip)
                                .eq(CabinetPdu::getCasIdA, cas))
                        .or(qr -> qr.eq(CabinetPdu::getPduIpB, ip)
                                .eq(CabinetPdu::getCasIdB, cas)))
                )
        );
        if (!CollectionUtils.isEmpty(pduFlag)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 保存机柜环境数据
     */
    private void saveEnvSensor(int cabinetId,CabinetVo vo) throws Exception{
        //环境数据为空，删除数据后返回
        if (CollectionUtils.isEmpty(vo.getSensorList())){
            envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId,cabinetId));
            return;
        }
        List<CabinetEnvSensor> envSensors = envSensorMapper.selectList(new LambdaQueryWrapper<CabinetEnvSensor>()
                .eq(CabinetEnvSensor::getCabinetId,cabinetId));
        if (!CollectionUtils.isEmpty(envSensors)){
            //先删除再新增
            List<Integer> ids = envSensors.stream().map(CabinetEnvSensor::getId).collect(Collectors.toList());
            envSensorMapper.deleteBatchIds(ids);
            //新增
            vo.getSensorList().forEach(cabinetEnvSensor -> {
                cabinetEnvSensor.setCabinetId(cabinetId);
                envSensorMapper.insert(cabinetEnvSensor);
            });

        }else {
            //新增
            vo.getSensorList().forEach(cabinetEnvSensor -> {
                cabinetEnvSensor.setCabinetId(cabinetId);
                envSensorMapper.insert(cabinetEnvSensor);
            });
        }
    }

    /**
     * 保存机柜机架数据
     */
    private void saveRack(int cabinetId,CabinetVo vo) throws Exception{
        //数据为空，清空数据
        if (CollectionUtils.isEmpty(vo.getRackIndexList())){
           //取消绑定
            List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                    .eq(RackIndex::getCabinetId,cabinetId)
                    .eq(RackIndex::getIsDelete,DelEnums.NO_DEL.getStatus()));
            if (!CollectionUtils.isEmpty(rackIndexList)){
                rackIndexMapper.update(new LambdaUpdateWrapper<RackIndex>()
                        .in(RackIndex::getId,rackIndexList.stream().map(RackIndex::getId).collect(Collectors.toList()))
                        .set(RackIndex::getCabinetId,0));
            }

        }

        if (!CollectionUtils.isEmpty(vo.getRackIndexList())){
            //修改
            vo.getRackIndexList().forEach(rackIndex -> rackIndexMapper.updateById(rackIndex));


        }
    }


    /**
     * 获取数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ids 机柜id列表
     * @param index 索引表
     */
    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery(CABINET_ID, ids))));
        builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(10000);

        List<String> list = new ArrayList<>();
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                list.add(str);
            }
        }
        return list;

    }

}
