package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.dto.cabinet.*;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.*;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.*;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.vo.CabineIndexCfgVO;
import cn.iocoder.yudao.module.cabinet.enums.CabinetLoadEnums;
import cn.iocoder.yudao.module.cabinet.mapper.RackIndexMapper;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import cn.iocoder.yudao.module.cabinet.vo.*;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.REDIS_KEY_PDU;
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
            if (Objects.nonNull(vo.getCabinetIds()) && CollectionUtils.isEmpty(vo.getCabinetIds())) {
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
                        jsonObject.put("id",dto.getId());
                        jsonObject.put("roomId",dto.getRoomId());
                        jsonObject.put(COMPANY, Objects.nonNull(dto.getCompany()) ? dto.getCompany() : "");
                        result.add(jsonObject);
                    } else {
                        Map map = new HashMap();
                        map.put("id", dto.getId());
                        map.put("roomId", dto.getRoomId());
                        map.put("cabinet_name", dto.getName());
                        map.put("status", dto.getRunStatus());
                        map.put("cabinet_key", dto.getRoomId() + "-" + dto.getId());
                        map.put("pow_capacity", dto.getPowCapacity());
                        Map map1 = new HashMap();
                        map.put("cabinet_power", map1);

                        Map map2 = new HashMap();
                        map2.put("ele_active", 0);
                        map2.put("pow_active", 0);
                        map2.put("pow_apparent", 0);
                        map2.put("pow_reactive", 0);
                        map2.put("power_factor", 0);
                        map1.put("total_data", map2);
                        result.add(JSON.parseObject(JSON.toJSONString(map)));
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
            CabineIndexCfgVO index = cabinetIndexMapper.selectCabineIndexCfgById(id);
            if (Objects.nonNull(index)) {
                dto.setId(id);
                dto.setCabinetName(index.getCabinetName());
                dto.setAisleId(index.getAisleId());
                dto.setRoomId(index.getRoomId());
                dto.setPowCapacity(index.getPowerCapacity());
                dto.setRunStatus(index.getRunStatus());
                dto.setPduBox(index.getPduBox());
                dto.setEleAlarmDay(index.getEleAlarmDay());
                dto.setEleAlarmMonth(index.getEleAlarmMonth());
                dto.setEleLimitDay(index.getEleLimitDay());
                dto.setEleLimitMonth(index.getEleLimitMonth());
                dto.setCabinetHeight(index.getCabinetHeight());
                dto.setType(index.getCabinetType());
                dto.setXCoordinate(index.getXCoordinate());
                dto.setYCoordinate(index.getYCoordinate());
                dto.setCompany(index.getCompany());
                //机房信息
                RoomIndex roomIndex = roomIndexMapper.selectById(index.getRoomId());
                if (Objects.nonNull(roomIndex))
                    dto.setRoomName(roomIndex.getRoomName());

                //配置的数据来源信息
                ValueOperations ops = redisTemplate.opsForValue();

                if (index.getPduBox() == PduBoxFlagEnums.PDU.getValue()) {
                    //来源pdu
                    CabinetPdu pdu = cabinetPduMapper.selectOne(new LambdaQueryWrapper<CabinetPdu>()
                            .eq(CabinetPdu::getCabinetId, index.getId()));

                    if (Objects.nonNull(pdu)) {
                        String[] split = pdu.getPduKeyA().split("-");
                        dto.setPduIpA(split[0]);
                        dto.setCasIdA(Integer.parseInt(split[1]));

                        String[] split1 = pdu.getPduKeyB().split("-");
                        dto.setPduIpB(split1[0]);
                        dto.setCasIdB(Integer.parseInt(split1[1]));
                        //获取pdu数据
                        StringBuilder aKey = new StringBuilder();
                        aKey.append(REDIS_KEY_PDU);aKey.append(pdu.getPduKeyA());

                        Object aPdu = ops.get(aKey.toString());
                        if (Objects.nonNull(aPdu)) {
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
                        bKey.append(pdu.getPduKeyB());
//                        bKey.append(SPLIT_KEY);
//                        bKey.append(pdu.getCasIdB());

                        Object bPdu = ops.get(bKey.toString());
                        if (Objects.nonNull(bPdu)) {
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
                } else if (index.getPduBox() == PduBoxFlagEnums.BUS.getValue()) {
                    //配置的母线数据
                    CabinetBox cabinetBus = cabinetBusMapper.selectOne(new LambdaQueryWrapper<CabinetBox>()
                            .eq(CabinetBox::getCabinetId, index.getId()));
                    if (Objects.nonNull(cabinetBus)) {
                        if (StringUtils.isNotEmpty(cabinetBus.getBoxKeyA())) {
                            BoxIndex boxIndex = boxIndexMapper.selectOne(new LambdaQueryWrapper<BoxIndex>()
                                    .eq(BoxIndex::getBoxKey, cabinetBus.getBoxKeyA()));

                            if (Objects.nonNull(boxIndex)) {

                                dto.setBusIpA(boxIndex.getIpAddr());
//                                dto.setBusNameA(boxIndex.getBusName());
                                dto.setBoxNameA(boxIndex.getBoxName());
                                dto.setBarIdA(boxIndex.getBusId());
                                dto.setBoxIndexA(boxIndex.getBoxId());
                                dto.setBoxOutletIdA(cabinetBus.getOutletIdA());
                            } else {
                                String[] keys = cabinetBus.getBoxKeyA().split(SPLIT_KEY);
                                dto.setBusIpA(keys[0]);
                                dto.setBarIdA(Integer.valueOf(keys[1]));
                                dto.setBoxIndexA(Integer.valueOf(keys[2]));
                                dto.setBoxOutletIdA(cabinetBus.getOutletIdA());
                            }
                        }

                        if (StringUtils.isNotEmpty(cabinetBus.getBoxKeyB())) {
                            BoxIndex boxIndex = boxIndexMapper.selectOne(new LambdaQueryWrapper<BoxIndex>()
                                    .eq(BoxIndex::getBoxKey, cabinetBus.getBoxKeyB()));
                            if (Objects.nonNull(boxIndex)) {

                                dto.setBusIpB(boxIndex.getIpAddr());
//                                dto.setBusNameB(boxIndex.getBusName());
                                dto.setBoxNameB(boxIndex.getBoxName());
                                dto.setBarIdB(boxIndex.getBusId());
                                dto.setBoxIndexB(boxIndex.getBoxId());
                                dto.setBoxOutletIdB(cabinetBus.getOutletIdB());
                            } else {
                                String[] keys = cabinetBus.getBoxKeyB().split(SPLIT_KEY);
                                dto.setBusIpB(keys[0]);
                                dto.setBarIdB(Integer.valueOf(keys[1]));
                                dto.setBoxIndexB(Integer.valueOf(keys[2]));
                                dto.setBoxOutletIdB(cabinetBus.getOutletIdB());
                            }

                        }
//                        dto.setBoxIndexA(cabinetBus.getBoxIndexA());
//                        dto.setBoxIndexB(cabinetBus.getBoxIndexB());
                    }
                }


                List<CabinetEnvSensor> envSensorList = envSensorMapper.selectList(new LambdaQueryWrapper<CabinetEnvSensor>()
                        .eq(CabinetEnvSensor::getCabinetId, index.getId()));
                if (!CollectionUtils.isEmpty(envSensorList)) {

                    List<CabinetEnvSensorDTO> sensorDtos = new ArrayList<>();
                    envSensorList.forEach(env -> {
                        CabinetEnvSensorDTO sensorDTO = new CabinetEnvSensorDTO();
                        sensorDTO.setId(env.getId());
                        sensorDTO.setCabinetId(env.getCabinetId());
                        sensorDTO.setChannel(env.getChannel());
                        sensorDTO.setPosition(env.getPosition());
                        sensorDTO.setPathPdu(String.valueOf(env.getPathPdu()));
                        sensorDTO.setSensorId(env.getSensorId());
                        sensorDTO.setSensorType(env.getSensorType());
                        sensorDtos.add(sensorDTO);
                    });
                    dto.setSensorList(sensorDtos);

                }

                List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                        .eq(RackIndex::getCabinetId, index.getId())
                        .eq(RackIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));
                if (!CollectionUtils.isEmpty(rackIndexList)) {
                    dto.setRackIndexList(rackIndexList);
                    int usedSpace = rackIndexList.stream().map(RackIndex::getUHeight).reduce(0, Integer::sum);
                    int rackNum = rackIndexList.size();
                    int freeSpace = dto.getCabinetHeight() - usedSpace;
                    dto.setUsedSpace(usedSpace);
                    dto.setRackNum(rackNum);
                    dto.setFreeSpace(freeSpace);
                } else {
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
    public CommonResult saveCabinet(CabinetVo vo) throws Exception {
        try {
            //判断pdu是否已经关联其他机柜
            if (vo.getPduBox() == PduBoxFlagEnums.PDU.getValue()) {
                List<CabinetIndex> indexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
                        .eq(CabinetIndex::getIsDisabled, DisableEnums.ENABLE.getStatus())
                        .ne(CabinetIndex::getId, vo.getId()));
                if (!CollectionUtils.isEmpty(indexList)) {
                    List<Integer> ids = indexList.stream().map(CabinetIndex::getId).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(ids)) {
                        if (isExist(vo.getPduIpA(), vo.getCasIdA(), ids)) {
//                            log.info("---- " + vo.getPduIpA() + vo.getCasIdA());
                            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "pdu已关联其他机柜");
                        }

                        if (isExist(vo.getPduIpB(), vo.getCasIdB(), ids)) {
//                            log.info("---- " + vo.getPduIpB() + vo.getCasIdB());
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
            if (Objects.nonNull(vo.getIndex())) {
                AisleIndex aisleIndex = aisleIndexMapper.selectOne(new LambdaQueryWrapper<AisleIndex>()
                        .eq(AisleIndex::getId, vo.getAisleId()));

                if (Objects.nonNull(aisleIndex) && "x".equals(aisleIndex.getDirection())) {
                    //横向
                    vo.setXCoordinate(aisleIndex.getXCoordinate() + vo.getIndex() - 1);
                    vo.setYCoordinate(aisleIndex.getYCoordinate());
                }
                if (Objects.nonNull(aisleIndex) && "y".equals(aisleIndex.getDirection())) {
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
                cabinetIndexMapper.updateById(convertIndex(vo, index));
            } else {
                //新增
                //判断机柜名称是否重复（已删除的或者已禁用的恢复）
                index = cabinetIndexMapper.selectOne(new LambdaQueryWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getCabinetName, vo.getCabinetName())
                        .eq(CabinetIndex::getRoomId, vo.getRoomId()));
                if (Objects.nonNull(index)) {
                    if (index.getIsDeleted() == DelFlagEnums.DELETE.getStatus() || index.getIsDisabled() == DisableFlagEnums.DISABLE.getStatus()) {
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
            //机柜与PDU/插接箱的关联
            if (vo.getPduBox() == PduBoxFlagEnums.PDU.getValue()) {

                if (StringUtils.isEmpty(vo.getPduIpA()) && StringUtils.isEmpty(vo.getPduIpB())) {

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
                    if (StringUtils.isNotEmpty(vo.getPduIpA()) || StringUtils.isNotEmpty(vo.getPduIpB())) {

                        //新增
                        cabinetPduMapper.insert(convertPdu(vo, pdu));
                    }
                }
            } else if (vo.getPduBox() == PduBoxFlagEnums.BUS.getValue()) {

                if (Objects.isNull(vo.getAddrA()) && Objects.isNull(vo.getAddrB())) {
                    //删除
                    cabinetBusMapper.delete(new LambdaQueryWrapper<CabinetBox>()
                            .eq(CabinetBox::getCabinetId, vo.getId()));
                }

                //母线关联表
                CabinetBox bus = cabinetBusMapper.selectOne(new LambdaQueryWrapper<CabinetBox>()
                        .eq(CabinetBox::getCabinetId, vo.getId()));


                if (Objects.nonNull(bus)) {
                    //修改
                    cabinetBusMapper.updateById(convertBus(vo, bus));
                } else {
                    //新增
                    bus = new CabinetBox();
                    if (Objects.nonNull(vo.getBusIpA()) || Objects.nonNull(vo.getBusIpB())) {
                        //新增
                        cabinetBusMapper.insert(convertBus(vo, bus));
                    }
                }
            }

            //保存环境数据
            saveEnvSensor(vo.getId(), vo);
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
    public int delCabinet(int id) throws Exception {
        try {
            CabinetIndex index = cabinetIndexMapper.selectById(id);
            if (Objects.isNull(index)) {
                return -1;
            }

            List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                    .eq(RackIndex::getIsDelete, DelEnums.NO_DEL.getStatus())
                    .eq(RackIndex::getCabinetId, index.getId()));
            if (!CollectionUtils.isEmpty(rackIndexList)) {
                throw new RuntimeException("存在未删除机架，不可删除");
            }
            if (index.getIsDeleted() == DelFlagEnums.DELETE.getStatus()) {
                //已经删除则物理删除
                cabinetIndexMapper.deleteById(id);
                //删除pdu关联关系
                cabinetPduMapper.delete(new LambdaQueryWrapper<CabinetPdu>()
                        .eq(CabinetPdu::getCabinetId, id));
                cabinetBusMapper.delete(new LambdaQueryWrapper<CabinetBox>()
                        .eq(CabinetBox::getCabinetId, id));
                //删除配置信息
                cabinetCfgMapper.delete(new LambdaQueryWrapper<CabinetCfg>()
                        .eq(CabinetCfg::getCabinetId, id));
                //删除环境信息
                envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                        .eq(CabinetEnvSensor::getCabinetId, id));
            } else {
                //逻辑删除
                cabinetIndexMapper.update(new LambdaUpdateWrapper<CabinetIndex>()
                        .eq(CabinetIndex::getId, id)
                        .set(CabinetIndex::getIsDeleted, DelEnums.DELETE.getStatus()));
                //删除pdu关联关系
                cabinetPduMapper.delete(new LambdaQueryWrapper<CabinetPdu>()
                        .eq(CabinetPdu::getCabinetId, id));
                cabinetBusMapper.delete(new LambdaQueryWrapper<CabinetBox>()
                        .eq(CabinetBox::getCabinetId, id));
                //删除环境信息
                envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                        .eq(CabinetEnvSensor::getCabinetId, id));
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
        saveEnvSensor(vo.getId(), vo);
        return CommonResult.success(vo.getId());
    }

    @Override
    public PageResult<CabinetIndexDTO> getCapacityPage(CabinetIndexVo vo) {
        try {
            Page<CabinetIndexDTO> page = new Page<>(vo.getPageNo(), vo.getPageSize());
            //获取机柜列表
            if (Objects.nonNull(vo.getCabinetIds()) && CollectionUtils.isEmpty(vo.getCabinetIds())) {
                List<Integer> list = new ArrayList<>();
                list.add(-1);
                vo.setCabinetIds(list);
            }
            //获取机柜列表
            Page<CabinetIndexDTO> indexDTOPage = cabinetCfgMapper.selectCabList(page, vo);
//            List<CabinetIndexDTO> result = new ArrayList<>();
            List<CabinetIndexDTO> result = indexDTOPage.getRecords();
            //获取机房数据
            if (!CollectionUtils.isEmpty(result)) {
                List<Integer> roomIds = result.stream().map(CabinetIndexDTO::getRoomId).distinct().collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(roomIds)) {
                    List<RoomIndex> roomIndexList = roomIndexMapper.selectBatchIds(roomIds);
                    if (!CollectionUtils.isEmpty(roomIndexList)) {
                        Map<Integer, String> map = roomIndexList.stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getRoomName));

                        if (Objects.nonNull(map)) {
                            result.forEach(dto -> {
                                dto.setRoomName(map.get(dto.getRoomId()));
//                                result.add(dto);
                            });
                        }
                    }
                }
            }

            List<Integer> ids = result.stream().map(CabinetIndexDTO::getId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                return new PageResult<>(result, indexDTOPage.getTotal());
            }
            //获取机架列表
            List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                    .eq(RackIndex::getIsDelete, DelEnums.NO_DEL.getStatus())
                    .in(RackIndex::getCabinetId, ids));

            if (!CollectionUtils.isEmpty(rackIndexList)) {
                Map<Integer, List<RackIndex>> cabRacks = rackIndexList.stream().collect(Collectors.groupingBy(RackIndex::getCabinetId));

                result.forEach(dto -> {
                    List<RackIndex> racks = cabRacks.get(dto.getId());
                    if (!CollectionUtils.isEmpty(racks)) {
                        int usedSpace = racks.stream().map(RackIndex::getUHeight).reduce(0, Integer::sum);
                        int rackNum = racks.size();
                        int freeSpace = dto.getCabinetHeight() - usedSpace;
                        dto.setUsedSpace(usedSpace);
                        dto.setRackNum(rackNum);
                        dto.setFreeSpace(freeSpace);
                    }
                });

            }

            result.forEach(dto -> {
                if (dto.getUsedSpace() == 0) {
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
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < CabinetLoadEnums.values().length; i++) {
            map.put(CabinetLoadEnums.values()[i].getStatus(), 0);
        }
        LambdaQueryWrapper<CabinetIndex> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(CabinetIndex::getLoadStatus, CabinetIndex::getCount);
        queryWrapper.groupBy(CabinetIndex::getLoadStatus);
        List<CabinetIndex> list = cabinetIndexMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(list)) {
            Map<Integer, Integer> statusMap = list.stream().collect(Collectors.toMap(CabinetIndex::getLoadStatus, CabinetIndex::getCount));
            statusMap.keySet().forEach(key -> map.put(key, statusMap.get(key)));
        }
        return map;
    }

    /**
     * 获得已删除机柜分页
     *
     * @param pageReqVO
     * @return
     */
    @Override
    public PageResult<JSONObject> getDeletedCabinetPage(CabinetIndexVo pageReqVO) {
        List<JSONObject> result = new ArrayList<>();
        try {
            Page<CabinetIndexDTO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
            Page<CabinetIndexDTO> indexDTOPage = cabinetCfgMapper.selectCabdeleteList(page, pageReqVO);
            indexDTOPage.getRecords().forEach(dto -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", dto.getId());
                jsonObject.put("name", dto.getName());
                jsonObject.put("pduBox", dto.getPduBox());
                jsonObject.put("runStatus", dto.getRunStatus());
                jsonObject.put("updateTime", dto.getUpdateTime());
                result.add(jsonObject);
            });
            return new PageResult<>(result, indexDTOPage.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    //设备恢复
    @Override
    public int getrestorerCabinet(Integer id) {
        return cabinetCfgMapper.updaterestorerCabinet(id);
    }

    @Override
    public PageResult<CabinetIndexLoadResVO> getIndexLoadPage(CabinetIndexVo req) {
        Page page = new Page<>(req.getPageNo(), req.getPageSize());
        Page<CabineIndexCfgVO> page1 = cabinetIndexMapper.selectIndexLoadPage(page, req);
        if (page1.getRecords() != null && page1.getRecords().size() > 0) {
            List<CabineIndexCfgVO> records = page1.getRecords();
            List<CabinetIndexLoadResVO> bean = BeanUtils.toBean(records, CabinetIndexLoadResVO.class);
            Map<String, CabinetIndexLoadResVO> map = bean.stream().collect(Collectors.toMap(vo -> vo.getRoomId() + "-" + vo.getId(), i -> i));
            List<String> keys = bean.stream().map(i ->REDIS_KEY_CABINET+ i.getRoomId() + "-" + i.getId()).collect(Collectors.toList());
            List list = redisTemplate.opsForValue().multiGet(keys);
            for (Object obj : list) {
                if (Objects.isNull(obj)) {
                    continue;
                }
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));

                String cabinetKey = jsonObject.getString("cabinet_key");
                CabinetIndexLoadResVO vo = map.get(cabinetKey);

                vo.setRoomName(jsonObject.getString("room_name"));
                StringJoiner joiner = new StringJoiner("-");
                if (!StringUtils.isEmpty(vo.getRoomName())) {
                    joiner.add(vo.getRoomName());
                }
                joiner.add(vo.getCabinetName());
                vo.setLocation(joiner.toString());

                JSONObject apath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_a");
                JSONObject bpath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_b");
                JSONObject total = jsonObject.getJSONObject("cabinet_power").getJSONObject("total_data");
                if (Objects.nonNull(total)) {
                    vo.setActiveTotal(total.getBigDecimal("pow_active"));
                    vo.setApparentTotal(total.getBigDecimal("pow_apparent"));
                }
                vo.setLoadFactor(jsonObject.getBigDecimal("load_factor").setScale(2, RoundingMode.HALF_UP));
                if (Objects.nonNull(apath)){
                    vo.setPowActivea(apath.getBigDecimal("pow_active"));
                    vo.setPowApparenta(apath.getBigDecimal("pow_apparent"));
                }
                if (Objects.nonNull(bpath)){
                    vo.setPowActiveb(bpath.getBigDecimal("pow_active"));
                    vo.setPowApparentb(bpath.getBigDecimal("pow_apparent"));
                }
                vo.setDataUpdateTime(jsonObject.getString("date_time"));
            }
            return new PageResult<>(bean, page1.getTotal());

        }

        return null;
    }

    @Override
    public PageResult<CabinetEnergyStatisticsResVO> getEnergyStatisticsPage(CabinetIndexVo pageReqVO) throws IOException {
        Page page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        Page<CabineIndexCfgVO> voPage = cabinetIndexMapper.selectIndexLoadPage(page, pageReqVO);
        List<CabinetEnergyStatisticsResVO> list = BeanUtils.toBean(voPage.getRecords(), CabinetEnergyStatisticsResVO.class);
        List<Integer> ids = list.stream().map(CabinetEnergyStatisticsResVO::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(ids)) {
            return new PageResult<>(list, voPage.getTotal());
        }
        Map<String, CabinetEnergyStatisticsResVO> map = list.stream().collect(Collectors.toMap(vo -> vo.getRoomId() + "-" + vo.getId(), i -> i));
        List<String> keys = list.stream().map(i ->REDIS_KEY_CABINET+ i.getRoomId() + "-" + i.getId()).collect(Collectors.toList());
        List vlues = redisTemplate.opsForValue().multiGet(keys);
        //昨日
        String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant())));
        String endTime = DateUtil.formatDateTime(DateUtil.endOfDay(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant())));
        List<CabinetEqTotalDay> yesterdayList = getDataEs(startTime, endTime, ids, "cabinet_eq_total_day", CabinetEqTotalDay.class);
        Map<Integer, BigDecimal> yesterdayMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(yesterdayList)) {
            yesterdayMap = yesterdayList.stream().collect(Collectors.toMap(CabinetEqTotalDay::getCabinetId, CabinetEqTotalDay::getEqValue));
        }

        //上周
        startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(Date.from(LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant())));
        endTime = DateUtil.formatDateTime(DateUtil.endOfWeek(Date.from(LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant())));
        List<CabinetEqTotalDay> weekList = getDataEs(startTime, endTime, ids, "cabinet_eq_total_week", CabinetEqTotalDay.class);
        Map<Integer, BigDecimal> weekMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(weekList)) {
            weekMap = weekList.stream().collect(Collectors.toMap(CabinetEqTotalDay::getCabinetId, CabinetEqTotalDay::getEqValue));
        }

        //上月
        startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(Date.from(LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toInstant())));
        endTime = DateUtil.formatDateTime(DateUtil.endOfMonth(Date.from(LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toInstant())));
        List<CabinetEqTotalDay> monthList = getDataEs(startTime, endTime, ids, "cabinet_eq_total_month", CabinetEqTotalDay.class);
        Map<Integer, BigDecimal> monthMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(monthList)) {
            monthMap = monthList.stream().collect(Collectors.toMap(CabinetEqTotalDay::getCabinetId, CabinetEqTotalDay::getEqValue));
        }

        for (CabinetEnergyStatisticsResVO vo : list) {
            vo.setYesterdayEq(yesterdayMap.get(vo.getId()));
            vo.setLastWeekEq(weekMap.get(vo.getId()));
            vo.setLastMonthEq(monthMap.get(vo.getId()));
        }
//        if (pageReqVO.getTimeGranularity().equals("yesterday")) {
//            list.sort(Comparator.comparing(CabinetEnergyStatisticsResVO::getYesterdayEq).reversed());
//        } else if (pageReqVO.getTimeGranularity().equals("lastWeek")) {
//            list.sort(Comparator.comparing(CabinetEnergyStatisticsResVO::getLastWeekEq).reversed());
//        } else if (pageReqVO.getTimeGranularity().equals("lastMonth")) {
//            list.sort(Comparator.comparing(CabinetEnergyStatisticsResVO::getLastMonthEq).reversed());
//        }
        if (Objects.isNull(vlues))
        for (Object obj : vlues) {
            if (Objects.isNull(obj)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(obj));
            String cabinetKey = jsonObject.getString("cabinet_key");
            CabinetEnergyStatisticsResVO vo = map.get(REDIS_KEY_CABINET+cabinetKey);

            vo.setRoomName(jsonObject.getString("room_name"));
            StringJoiner joiner = new StringJoiner("-");
            if (!StringUtils.isEmpty(vo.getRoomName())) {
                joiner.add(vo.getRoomName());
            }
            joiner.add(vo.getCabinetName());
            vo.setLocation(joiner.toString());
        }
        return new PageResult<>(list, voPage.getTotal());
    }

    @Override
    public PageResult<CabinetIndexEnvResVO> getCabinetEnv(CabinetIndexVo pageReqVO) {
        Page page = new Page(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        Page<CabineIndexCfgVO> voPage = cabinetIndexMapper.selectIndexLoadPage(page, pageReqVO);
        if (!CollectionUtils.isEmpty(voPage.getRecords())) {
            List<CabineIndexCfgVO> records = voPage.getRecords();
            List<CabinetIndexEnvResVO> bean = BeanUtils.toBean(records, CabinetIndexEnvResVO.class);
            Map<String, CabinetIndexEnvResVO> map = bean.stream().collect(Collectors.toMap(vo ->REDIS_KEY_CABINET+ vo.getRoomId() + "-" + vo.getId(), i -> i));
            Set<String> ids = map.keySet();
            List list = redisTemplate.opsForValue().multiGet(ids);
            for (Object obj : list) {
                if (Objects.isNull(obj)){
                    continue;
                }
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(obj));
                String cabinetKey = jsonObject.getString("cabinet_key");
                CabinetIndexEnvResVO vo = map.get(REDIS_KEY_CABINET+cabinetKey);
                vo.setHumValue(jsonObject.getJSONObject("cabinet_env").getString("hum_value"));
                vo.setTemValue(jsonObject.getJSONObject("cabinet_env").getString("tem_value"));
                vo.setRoomName(jsonObject.getString("room_name"));
            }
            return new PageResult<>(bean, voPage.getTotal());
        }
        return null;
    }

    @Override
    public PageResult<CabinetIndexBalanceResVO> getCabinetIndexBalancePage(CabinetIndexVo pageReqVO) {
        Page<CabineIndexCfgVO> voPage = cabinetIndexMapper.selectIndexLoadPage(new Page(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
        List<CabinetIndexBalanceResVO> records = BeanUtils.toBean(voPage.getRecords(), CabinetIndexBalanceResVO.class);
        Map<String, CabinetIndexBalanceResVO> map = records.stream().collect(Collectors.toMap(vo -> REDIS_KEY_CABINET +  vo.getRoomId() + "-" + vo.getId(), i -> i));
        List list = redisTemplate.opsForValue().multiGet(map.keySet());
        for (Object obj : list) {
            if (Objects.isNull(obj)){
                continue;
            }
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(obj));
            CabinetIndexBalanceResVO cabinetKey = map.get(REDIS_KEY_CABINET+jsonObject.getString("cabinet_key"));
            cabinetKey.setRoomName(jsonObject.getString("room_name"));
            JSONObject aPath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_a");
            JSONObject bPath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_b");
            JSONObject total = jsonObject.getJSONObject("cabinet_power").getJSONObject("total_data");
            //总视在功率
            BigDecimal totalPow =new BigDecimal(0);
            if (Objects.nonNull(total)){
                totalPow =  total.getBigDecimal("pow_apparent");
            }
            if (Objects.nonNull(aPath)){
                //a的视在功率
                BigDecimal aPow = aPath.getBigDecimal("pow_apparent");
                cabinetKey.setAPow(BigDemicalUtil.safeMultiply(BigDemicalUtil.safeDivideNum(4, aPow, totalPow),100));
                cabinetKey.setACurValue(aPath.getList("cur_value",BigDecimal.class).get(0));
                cabinetKey.setAVolValue(aPath.getList("vol_value",BigDecimal.class).get(0));
                cabinetKey.setAPowValue(aPath.getList("pow_value",BigDecimal.class).get(0));
            }
            if (Objects.nonNull(bPath)) {
                //b的视在功率
                BigDecimal bPow = bPath.getBigDecimal("pow_apparent");
                cabinetKey.setBPow(BigDemicalUtil.safeMultiply(BigDemicalUtil.safeDivideNum(4, bPow, totalPow), 100));
                cabinetKey.setBCurValue(bPath.getList("cur_value", BigDecimal.class).get(0));
                cabinetKey.setBVolValue(bPath.getList("vol_value", BigDecimal.class).get(0));
                cabinetKey.setBPowValue(bPath.getList("pow_value", BigDecimal.class).get(0));
            }
        }
        return new PageResult<>(records, voPage.getTotal());
    }

    @Override
    public CabinetDistributionDetailsResVO getCabinetdistributionDetails(int id, int roomId, String type) throws IOException {
        CabinetDistributionDetailsResVO vo = new CabinetDistributionDetailsResVO();
        Object obj = redisTemplate.opsForValue().get(REDIS_KEY_CABINET + roomId + "-" + id);
        if (Objects.isNull(obj)){
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(obj));
        JSONObject apath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_a");
        JSONObject bpath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_b");
        JSONObject total = jsonObject.getJSONObject("cabinet_power").getJSONObject("total_data");

        vo.setLoadFactor(jsonObject.getBigDecimal("load_factor").setScale(0, RoundingMode.HALF_DOWN));
        vo.setCabinetName(jsonObject.getString("cabinet_name"));
        vo.setRoomName(jsonObject.getString("room_name"));
        vo.setDateTime(LocalDateTimeUtil.parse(jsonObject.getString("date_time"),"yyyy-MM-dd HH:mm:ss"));
        if (Objects.nonNull(total)){
            vo.setPowActiveTotal(total.getBigDecimal("pow_active").setScale(1, RoundingMode.HALF_DOWN));//有功功率
            vo.setPowApparentTotal(total.getBigDecimal("pow_apparent").setScale(3, RoundingMode.HALF_DOWN));//视在功率
            vo.setPowReactiveTotal(total.getBigDecimal("pow_reactive").setScale(3, RoundingMode.HALF_DOWN));//无功功率
            vo.setPowerFactor(total.getBigDecimal("power_factor").setScale(2, RoundingMode.HALF_DOWN));//功率因素
        }
        if (Objects.nonNull(apath)){
            vo.setCurA(apath.getList("cur_value",BigDecimal.class).stream().map(i ->i.setScale(2, RoundingMode.HALF_DOWN)).collect(Collectors.toList()));
            vo.setVolA(apath.getList("vol_value",BigDecimal.class).stream().map(i ->i.setScale(1, RoundingMode.HALF_DOWN)).collect(Collectors.toList()));
            vo.setPowActiveA(apath.getBigDecimal("pow_active").setScale(1, RoundingMode.HALF_DOWN));//有功功率
            vo.setPowApparentA(apath.getBigDecimal("pow_apparent").setScale(3, RoundingMode.HALF_DOWN));//视在功率
            vo.setPowReactiveA(apath.getBigDecimal("pow_reactive").setScale(3, RoundingMode.HALF_DOWN));//无功功率
            vo.setAPow(BigDemicalUtil.safeMultiply(BigDemicalUtil.safeDivideNum(4, vo.getPowApparentA(), vo.getPowApparentTotal()),100));
        }
        if (Objects.nonNull(bpath)){
            vo.setCurB(bpath.getList("cur_value",BigDecimal.class).stream().map(i ->i.setScale(2, RoundingMode.HALF_DOWN)).collect(Collectors.toList()));
            vo.setVolB(bpath.getList("vol_value",BigDecimal.class).stream().map(i ->i.setScale(1, RoundingMode.HALF_DOWN)).collect(Collectors.toList()));
            vo.setPowActiveB(bpath.getBigDecimal("pow_active").setScale(1, RoundingMode.HALF_DOWN));//有功功率
            vo.setPowApparentB(bpath.getBigDecimal("pow_apparent").setScale(3, RoundingMode.HALF_DOWN));//视在功率
            vo.setPowReactiveB(bpath.getBigDecimal("pow_reactive").setScale(3, RoundingMode.HALF_DOWN));//无功功率
            vo.setBPow(BigDemicalUtil.safeMultiply(BigDemicalUtil.safeDivideNum(4, vo.getPowApparentB(), vo.getPowApparentTotal()),100));
        }


        return vo;
    }

    @Override
    public Map getCabinetDistributionFactor(int id, int roomId, String type) throws IOException {
        String startTime = null;
        String endTime = null;
        String index = null;
        switch (type){
            case "day":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusDays(1),"yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(),"yyyy-MM-dd HH:mm:ss");
                index = "cabinet_hda_pow_hour";
                break;
            case "hour":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusHours(1),"yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(),"yyyy-MM-dd HH:mm:ss");
                index = "cabinet_hda_pow_realtime";
                break;
            case "today":
                startTime = LocalDateTimeUtil.format(LocalDateTime.MIN,"yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(),"yyyy-MM-dd HH:mm:ss");
                index = "cabinet_hda_pow_hour";
                break;
            case "threeDay":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusDays(3),"yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(),"yyyy-MM-dd HH:mm:ss");
                index = "cabinet_hda_pow_hour";
                break;
            default:
        }
        //day,today,threeDay
        List<Map<String, Object>> data = getDataEs(startTime, endTime, Collections.singletonList(id),
                index ,Map.class);
        List<BigDecimal> factorA = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("factor_a").toString()),100)).collect(Collectors.toList());
        List<BigDecimal> factorB = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("factor_b").toString()),100)).collect(Collectors.toList());
        List<BigDecimal> factorTotal = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("factor_total").toString()),100)).collect(Collectors.toList());
        List<String> createTime = data.stream().map(i -> String.valueOf(i.get("create_time"))).collect(Collectors.toList());

        Map map = new HashMap();
        map.put("factorA",factorA);
        map.put("factorB",factorB);
        map.put("factorTotal",factorTotal);
        map.put("day",createTime);
        return map;
    }

    private List getDataEs(String startTime, String endTime, List<Integer> ids, String index, Class objClass) throws IOException {
        try {
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

            List list = new ArrayList<>();
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse != null) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    list.add(JsonUtils.parseObject(hit.getSourceAsString(), objClass));
                }
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 机柜配电状态统计
     *
     * @return
     */
    @Override
    public PageResult<JSONObject> getCabinetRunStatus() {

        //查询全部的机柜配电状态
        Map<String, Integer> statusCounts = cabinetCfgMapper.selectRunStatus();

        JSONObject jsonObject = new JSONObject();
        List<JSONObject> result = new ArrayList<>();
        if (Objects.nonNull(statusCounts)) {
            jsonObject.put("sumNoload", statusCounts.get("sumNoload"));
            jsonObject.put("sumNormal", statusCounts.get("sumNormal"));
            jsonObject.put("sumEarly", statusCounts.get("sumEarly"));
            jsonObject.put("sumInform", statusCounts.get("sumInform"));
        }
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
        cabinetIndex.setCabinetName(vo.getCabinetName());
        cabinetIndex.setPduBox(vo.getPduBox());
        //未删除
        cabinetIndex.setIsDeleted(DelFlagEnums.NO_DEL.getStatus());
        //未禁用
        cabinetIndex.setIsDisabled(DisableFlagEnums.ENABLE.getStatus());
        cabinetIndex.setPowerCapacity(vo.getPowCapacity());
        cabinetIndex.setCabinetHeight(vo.getCabinetHeight());
        cabinetIndex.setRoomId(vo.getRoomId());
        cabinetIndex.setId(index.getId());
        cabinetIndex.setCabinetType(vo.getType());

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
        cabinetPdu.setPduKeyA(vo.getPduIpA()+"-"+vo.getCasIdB());
        cabinetPdu.setPduKeyB(vo.getPduIpB()+"-"+vo.getCasIdA());
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
    private CabinetBox convertBus(CabinetVo vo, CabinetBox bus) {
        CabinetBox cabinetBus = new CabinetBox();
        cabinetBus.setCabinetId(vo.getId());
        if (StringUtils.isNotEmpty(vo.getBusIpA())
                && Objects.nonNull(vo.getBarIdA())
                && Objects.nonNull(vo.getBoxIndexA())) {
            StringJoiner boxKeyA = new StringJoiner(SPLIT_KEY);
            boxKeyA.add(vo.getBusIpA()).add(String.valueOf(vo.getBarIdA())).add(vo.getBoxIndexA());
            cabinetBus.setBoxKeyA(boxKeyA.toString());
        } else {
            cabinetBus.setBoxKeyA("");
        }

        cabinetBus.setOutletIdA(vo.getBoxOutletIdA());
        if (StringUtils.isNotEmpty(vo.getBusIpB())
                && Objects.nonNull(vo.getBarIdB())
                && Objects.nonNull(vo.getBoxIndexB())) {
            StringJoiner boxKeyB = new StringJoiner(SPLIT_KEY);
            boxKeyB.add(vo.getBusIpB()).add(String.valueOf(vo.getBarIdB())).add(vo.getBoxIndexB());
            cabinetBus.setBoxKeyB(boxKeyB.toString());
        } else {
            cabinetBus.setBoxKeyB("");
        }
        cabinetBus.setOutletIdB(vo.getBoxOutletIdB());
        cabinetBus.setId(bus.getId());
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
        //TODO  后期更改
        cabinetCfg.setEleAlarmDay(vo.getEleAlarmDay());
        cabinetCfg.setEleAlarmMonth(vo.getEleAlarmMonth());
        cabinetCfg.setEleLimitDay(vo.getEleLimitDay());
        cabinetCfg.setEleLimitMonth(vo.getEleLimitMonth());
        cabinetCfg.setCompany(vo.getCompany());

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
                .and((wq -> wq.and(qr -> qr.eq(CabinetPdu::getPduKeyA, ip + "-" + cas))//.eq(CabinetPdu::getCasIdA, cas)
                        .or(qr -> qr.eq(CabinetPdu::getPduKeyB, ip + "-" + cas)))//.eq(CabinetPdu::getCasIdB, cas)
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
    private void saveEnvSensor(int cabinetId, CabinetVo vo) throws Exception {
        //环境数据为空，删除数据后返回
        if (CollectionUtils.isEmpty(vo.getSensorList())) {
            envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, cabinetId));
            return;
        }
        List<CabinetEnvSensor> envSensors = envSensorMapper.selectList(new LambdaQueryWrapper<CabinetEnvSensor>()
                .eq(CabinetEnvSensor::getCabinetId, cabinetId));
        if (!CollectionUtils.isEmpty(envSensors)) {
            //先删除再新增
            List<Integer> ids = envSensors.stream().map(CabinetEnvSensor::getId).collect(Collectors.toList());
            envSensorMapper.deleteBatchIds(ids);
            //新增
            vo.getSensorList().forEach(cabinetEnvSensor -> {
                cabinetEnvSensor.setCabinetId(cabinetId);
                envSensorMapper.insert(cabinetEnvSensor);
            });

        } else {
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
    private void saveRack(int cabinetId, CabinetVo vo) throws Exception {
        //数据为空，清空数据
        if (CollectionUtils.isEmpty(vo.getRackIndexList())) {
            //取消绑定
            List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                    .eq(RackIndex::getCabinetId, cabinetId)
                    .eq(RackIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));
            if (!CollectionUtils.isEmpty(rackIndexList)) {
                rackIndexMapper.update(new LambdaUpdateWrapper<RackIndex>()
                        .in(RackIndex::getId, rackIndexList.stream().map(RackIndex::getId).collect(Collectors.toList()))
                        .set(RackIndex::getCabinetId, 0));
            }

        }

        if (!CollectionUtils.isEmpty(vo.getRackIndexList())) {
            //修改
            vo.getRackIndexList().forEach(rackIndex -> rackIndexMapper.updateById(rackIndex));


        }
    }


    /**
     * 获取数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ids       机柜id列表
     * @param index     索引表
     */
    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        try {
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

        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

}
