package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.dto.cabinet.*;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.*;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduIndexDo;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.*;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.ThreadPoolConfig;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.common.vo.CabineIndexCfgVO;
import cn.iocoder.yudao.framework.common.vo.CabinetCapacityStatisticsResVO;
import cn.iocoder.yudao.framework.common.vo.CabinetRunStatusResVO;
import cn.iocoder.yudao.framework.common.vo.RackIndexResVO;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.CabinetEnvAndHumRes;
import cn.iocoder.yudao.module.cabinet.mapper.RackIndexMapper;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import cn.iocoder.yudao.module.cabinet.service.ICabinetEnvSensorService;
import cn.iocoder.yudao.module.cabinet.service.temcolor.TemColorService;
import cn.iocoder.yudao.module.cabinet.vo.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Max;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

    @Autowired
    private TemColorService temColorService;

    @Autowired
    private PduIndexDoMapper pduIndexDoMapper;

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
                List<CabinetIndexDTO> records = indexDTOPage.getRecords();
                List<Integer> collect = records.stream().map(CabinetIndexDTO::getId).collect(Collectors.toList());

                List<CabinetBox> cabinetBoxes = cabinetBusMapper.selectList(new LambdaQueryWrapper<CabinetBox>().in(CabinetBox::getCabinetId, collect));
                Map<Integer, List<CabinetBox>> boxMap = cabinetBoxes.stream().collect(Collectors.groupingBy(CabinetBox::getCabinetId));
                List<CabinetPdu> cabinetPdus = cabinetPduMapper.selectList(new LambdaQueryWrapper<CabinetPdu>().in(CabinetPdu::getCabinetId, collect));
                Map<Integer, List<CabinetPdu>> pduMap = cabinetPdus.stream().collect(Collectors.groupingBy(CabinetPdu::getCabinetId));
                List<RackIndex> rackIndices = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>().in(RackIndex::getCabinetId, collect));
                Map<Integer, List<RackIndex>> rackMap = rackIndices.stream().collect(Collectors.groupingBy(RackIndex::getCabinetId));

                List<String> keys = records.stream().map(dto -> REDIS_KEY_CABINET + dto.getRoomId() + SPLIT_KEY + dto.getId()).distinct().collect(Collectors.toList());
                List list = redisTemplate.opsForValue().multiGet(keys);
                Map<String, Object> redisMap = (Map<String, Object>) list.stream().filter(itr -> Objects.nonNull(itr)).collect(Collectors.toMap(i -> JSON.parseObject(JSON.toJSONString(i)).getString("cabinet_key"), Function.identity()));
                records.forEach(dto -> {
                    Object obj = redisMap.get(dto.getRoomId() + "-" + dto.getId());
//                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
                    if (Objects.nonNull(obj)) {
                        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));
                        jsonObject.put("id", dto.getId());
                        jsonObject.put("roomId", dto.getRoomId());
                        List<CabinetBox> cabinetBoxes1 = boxMap.get(dto.getId());
                        jsonObject.put("cabinetBoxes", cabinetBoxes1);
                        List<CabinetPdu> cabinetPdus1 = pduMap.get(dto.getId());
                        jsonObject.put("cabinetPdus", cabinetPdus1);
                        List<RackIndex> rackIndices1 = rackMap.get(dto.getId());
                        jsonObject.put("rackIndices", rackIndices1);

                        jsonObject.put(COMPANY, Objects.nonNull(dto.getCompany()) ? dto.getCompany() : "");
                        result.add(jsonObject);
                    } else {
                        Map map = new HashMap();
                        map.put("id", dto.getId());
                        map.put("roomId", dto.getRoomId());
                        map.put("room_name", dto.getRoomName());
                        List<CabinetBox> cabinetBoxes1 = boxMap.get(dto.getId());
                        map.put("cabinetBoxes", cabinetBoxes1);

                        List<CabinetPdu> cabinetPdus1 = pduMap.get(dto.getId());
                        map.put("cabinetPdus", cabinetPdus1);

                        List<RackIndex> rackIndices1 = rackMap.get(dto.getId());
                        map.put("rackIndices", rackIndices1);

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
    public Map getCabinetDetail(int id) {
        HashMap result = new HashMap();
        try {
            String name = new String();
            CabinetIndex index = cabinetIndexMapper.selectById(id);
            AisleIndex aisleIndex = aisleIndexMapper.selectById(index.getAisleId());
            RoomIndex roomIndex = roomIndexMapper.selectById(index.getRoomId());
            if (Objects.nonNull(roomIndex)) {
                name = name + roomIndex.getRoomName() + "-";
            }
            if (Objects.nonNull(aisleIndex)) {
                name = aisleIndex.getAisleName() + "-";
            }
            name = name + index.getCabinetName();
            //获取redis数据
            String key = REDIS_KEY_CABINET + index.getRoomId() + SPLIT_KEY + id;
            result.put("redisData", JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key))));
            result.put("name", name);

            List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id));
            if (!CollectionUtils.isEmpty(cabinetPduList)) {
                CabinetPdu cabinetPdu = cabinetPduList.get(0);
                Object obj = redisTemplate.opsForValue().get(REDIS_KEY_PDU + cabinetPdu.getPduKeyA());
                if (Objects.nonNull(obj)) {
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));
                    JSONObject pduTgData = jsonObject.getJSONObject("pdu_data").getJSONObject("pdu_total_data");
                    Double curUnbalance = pduTgData.getDoubleValue("cur_unbalance");
                    result.put("curUnbalancea", new BigDecimal(curUnbalance).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }

                Object objb = redisTemplate.opsForValue().get(REDIS_KEY_PDU + cabinetPdu.getPduKeyB());
                if (Objects.nonNull(objb)) {
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(objb));
                    JSONObject pduTgData = jsonObject.getJSONObject("pdu_data").getJSONObject("pdu_total_data");
                    Double curUnbalance = pduTgData.getDoubleValue("cur_unbalance");
                    result.put("curUnbalanceb", new BigDecimal(curUnbalance).setScale(2, RoundingMode.HALF_UP).doubleValue());
                }

            }
            return result;
        } catch (Exception e) {
            log.error("获取基础数据失败: ", e);
        }
        return result;
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
                        aKey.append(REDIS_KEY_PDU);
                        aKey.append(pdu.getPduKeyA());

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
                    }
                }

                List<CabinetEnvSensor> envs = envSensorMapper.selectList(new LambdaQueryWrapper<CabinetEnvSensor>()
                        .eq(CabinetEnvSensor::getCabinetId, index.getId()));
                if (!CollectionUtils.isEmpty(envs)) {
                    List<CabinetEnvSensorDTO> left = BeanUtils.toBean(envs, CabinetEnvSensorDTO.class);
                    dto.setSensorList(left);
                }

                List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                        .eq(RackIndex::getCabinetId, index.getId())
                        .eq(RackIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));
                if (!CollectionUtils.isEmpty(rackIndexList)) {
                    List<RackIndexResVO> bean = BeanUtils.toBean(rackIndexList, RackIndexResVO.class);

//                    List<String> rackIds = bean.stream().map(i -> "packet:rack:" + i.getId()).distinct().collect(Collectors.toList());
//                    List list = redisTemplate.opsForValue().multiGet(rackIds);
//                    Map<Integer, Object> rackKey = new HashMap<>();
//                    if (Objects.nonNull(list.get(0))) {
//                        rackKey = (Map<Integer, Object>) list.stream().collect(Collectors.toMap(i -> JSON.parseObject(JSON.toJSONString(i)).getInteger("rack_key"), Function.identity()));
//                    }
//                    for (RackIndexResVO t : bean){
//                        Object obj = rackKey.get(t.getId());
//
//                        JSONObject rackPower = JSON.parseObject(JSON.toJSONString(obj)).getJSONObject("rack_power");
//                        if (Objects.nonNull(rackPower)) {
//                            Double cura = rackPower.getJSONObject("total_data").getDouble("cur_a");
//                            Double curb = rackPower.getJSONObject("total_data").getDouble("cur_b");
//                            Double powApparent = rackPower.getJSONObject("total_data").getDouble("pow_apparent");
//                            t.setPowActive(BigDecimal.valueOf(powApparent).setScale(3, BigDecimal.ROUND_HALF_DOWN));
//                            t.setCurValueA(BigDecimal.valueOf(cura).setScale(2, BigDecimal.ROUND_HALF_DOWN));
//                            t.setCurValueB(BigDecimal.valueOf(curb).setScale(2, BigDecimal.ROUND_HALF_DOWN));
//                        }
//                    }
                    dto.setRackIndexList(bean);
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
                            return CommonResult.error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "pdu已关联其他机柜");
                        }

                        if (isExist(vo.getPduIpB(), vo.getCasIdB(), ids)) {
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

                if (Objects.isNull(vo.getBoxOutletIdA()) && Objects.isNull(vo.getBoxOutletIdB())) {
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
            cabinetEnvSensorService.saveEnvSensor(vo.getId(), vo);

            //saveEnvSensor(vo.getId(), vo);
            //保存U位数据
            saveRack(vo.getId(), vo);

            return CommonResult.success(vo.getId());
        } finally {
            //刷新机柜计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
//            HttpUtil.get(adder);
            //ThreadPoolConfig.getTHreadPool().execute(() -> {
            //HttpUtil.get(adder);
            //});
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delCabinet(int id, Integer type) {
        try {
            int delete = 0;
            //：1-解绑pdu  2-解绑bus(母线) 3-解绑机架  4-删除机柜
            switch (type) {
                case 1:
                    //删除pdu关联关系
                    delete = cabinetPduMapper.delete(new LambdaQueryWrapper<CabinetPdu>()
                            .eq(CabinetPdu::getCabinetId, id));
                    break;
                case 2:
                    delete = cabinetBusMapper.delete(new LambdaQueryWrapper<CabinetBox>()
                            .eq(CabinetBox::getCabinetId, id));
                    break;
                case 3:
                    delete = rackIndexMapper.updateByCabinetId(id);
                    break;
                case 4:
                    CabinetIndex index = cabinetIndexMapper.selectById(id);
                    if (Objects.isNull(index)) {
                        return -1;
                    }
//                    List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
//                            .eq(RackIndex::getIsDelete, DelEnums.NO_DEL.getStatus())
//                            .eq(RackIndex::getCabinetId, index.getId()));
//                    if (!CollectionUtils.isEmpty(rackIndexList)) {
//                        throw new RuntimeException("存在未删除机架，不可删除");
//                    }
                    if (index.getIsDeleted() == DelFlagEnums.DELETE.getStatus()) {
                        //已经删除则物理删除
                        delete = cabinetIndexMapper.deleteById(id);
                        //删除配置信息
                        cabinetCfgMapper.delete(new LambdaQueryWrapper<CabinetCfg>()
                                .eq(CabinetCfg::getCabinetId, id));
                        //删除环境信息
                        envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                                .eq(CabinetEnvSensor::getCabinetId, id));
                    } else {
                        //逻辑删除
                        delete = cabinetIndexMapper.update(new LambdaUpdateWrapper<CabinetIndex>()
                                .eq(CabinetIndex::getId, id)
                                .set(CabinetIndex::getIsDeleted, DelEnums.DELETE.getStatus())
                                .set(CabinetIndex::getCabinetUseHeight, 0));
                        //删除环境信息
                        envSensorMapper.delete(new LambdaQueryWrapper<CabinetEnvSensor>()
                                .eq(CabinetEnvSensor::getCabinetId, id));
                    }

                    //删除key
                    String key = REDIS_KEY_CABINET + index.getRoomId() + SPLIT_KEY + index.getId();

                    boolean flag = redisTemplate.delete(key);
                    log.info("key: " + key + " flag : " + flag);
                    break;
                default:
                    break;
            }
            return delete;
        } catch (Exception e) {
            log.error("删除机柜异常", e);
            return 0;
        } finally {
            log.info("刷新计算服务缓存 --- " + adder);
            //刷新机柜计算服务缓存
            ThreadPoolConfig.getTHreadPool().execute(() -> {
                HttpUtil.get(adder);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult saveEnvCabinet(CabinetVo vo) throws Exception {
        //保存环境数据
        cabinetEnvSensorService.saveEnvSensor(vo.getId(), vo);
//        saveEnvSensor(vo.getId(), vo);
        return CommonResult.success(vo.getId());
    }

    @Override
    public PageResult<CabinetIndexDTO> getCapacityPage(CabinetIndexVo vo) {
        try {
            Page<CabinetIndexDTO> page = new Page<>(vo.getPageNo(), vo.getPageSize());

            //获取机柜列表
            Page<CabinetIndexDTO> indexDTOPage = cabinetCfgMapper.selectCabList(page, vo);

            List<CabinetIndexDTO> result = indexDTOPage.getRecords();

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

            return new PageResult<>(result, indexDTOPage.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);

    }

    @Override
    public Map<String, Integer> loadStatusCount() {
        Map<String, Integer> map = cabinetIndexMapper.selectLoadStatusCount();
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
        if (page1.getRecords() != null && !page1.getRecords().isEmpty()) {
            List<CabineIndexCfgVO> records = page1.getRecords();
            List<CabinetIndexLoadResVO> bean = BeanUtils.toBean(records, CabinetIndexLoadResVO.class);
            Map<String, CabinetIndexLoadResVO> map = bean.stream().collect(Collectors.toMap(vo -> vo.getRoomId() + "-" + vo.getId(), i -> i));
            List<String> keys = bean.stream().map(i -> REDIS_KEY_CABINET + i.getRoomId() + "-" + i.getId()).collect(Collectors.toList());
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
                    vo.setPowReactiveTotal(total.getBigDecimal("pow_reactive"));
                }
                vo.setLoadFactor(jsonObject.getBigDecimal("load_factor").setScale(2, RoundingMode.HALF_UP));
                if (Objects.nonNull(apath)) {
                    vo.setPowActivea(apath.getBigDecimal("pow_active"));
                    vo.setPowApparenta(apath.getBigDecimal("pow_apparent"));
                    vo.setPowReactivea(apath.getBigDecimal("pow_reactive"));
                }
                if (Objects.nonNull(bpath)) {
                    vo.setPowActiveb(bpath.getBigDecimal("pow_active"));
                    vo.setPowApparentb(bpath.getBigDecimal("pow_apparent"));
                    vo.setPowReactiveb(bpath.getBigDecimal("pow_reactive"));
                }
                vo.setDataUpdateTime(jsonObject.getString("date_time"));
            }
            return new PageResult<>(bean, page1.getTotal());

        }

        return null;
    }

    @Override
    public PageResult<CabinetEnergyStatisticsResVO> getEnergyStatisticsPage(CabinetIndexVo pageReqVO) {
        Page page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        Page<CabineIndexCfgVO> voPage = cabinetIndexMapper.selectIndexLoadPage(page, pageReqVO);
        List<CabinetEnergyStatisticsResVO> list = BeanUtils.toBean(voPage.getRecords(), CabinetEnergyStatisticsResVO.class);
        List<Integer> ids = list.stream().map(CabinetEnergyStatisticsResVO::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(ids)) {
            return new PageResult<>(list, voPage.getTotal());
        }

        List<String> keys = list.stream().map(i -> REDIS_KEY_CABINET + i.getRoomId() + "-" + i.getId()).collect(Collectors.toList());

        //昨日

//        String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant())));
        String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
        String endTime = DateUtil.formatDateTime(DateTime.now());
//        String endTime = DateUtil.formatDateTime(DateUtil.endOfDay(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant())));
        List<CabinetEqTotalDay> yesterdayList = getDataEs(startTime, endTime, ids, "cabinet_eq_total_day", CabinetEqTotalDay.class, new String[]{"cabinet_id", "eq_value"});
        Map<Integer, BigDecimal> yesterdayMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(yesterdayList)) {
            yesterdayMap = yesterdayList.stream().collect(Collectors.toMap(CabinetEqTotalDay::getCabinetId, CabinetEqTotalDay::getEqValue));
        }

        //上周
//        startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(Date.from(LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant())));
        startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
//        endTime = DateUtil.formatDateTime(DateUtil.endOfWeek(Date.from(LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant())));
        List<CabinetEqTotalDay> weekList = getDataEs(startTime, endTime, ids, "cabinet_eq_total_week", CabinetEqTotalDay.class, new String[]{"cabinet_id", "eq_value"});
        Map<Integer, BigDecimal> weekMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(weekList)) {
            weekMap = weekList.stream().collect(Collectors.toMap(CabinetEqTotalDay::getCabinetId, CabinetEqTotalDay::getEqValue));
        }

        //上月
//        startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(Date.from(LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toInstant())));
        startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
//        endTime = DateUtil.formatDateTime(DateUtil.endOfMonth(Date.from(LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toInstant())));
        List<CabinetEqTotalDay> monthList = getDataEs(startTime, endTime, ids, "cabinet_eq_total_month", CabinetEqTotalDay.class, new String[]{"cabinet_id", "eq_value"});
        Map<Integer, BigDecimal> monthMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(monthList)) {
            monthMap = monthList.stream().collect(Collectors.toMap(CabinetEqTotalDay::getCabinetId, CabinetEqTotalDay::getEqValue));
        }

        List<Object> vlues = redisTemplate.opsForValue().multiGet(keys);
        Map<String, Object> keyMap = vlues.stream().filter(i -> Objects.nonNull(i))
                .collect(Collectors.toMap(i -> JSON.parseObject(JSONObject.toJSONString(i)).getString("cabinet_key"), Function.identity()));
        for (CabinetEnergyStatisticsResVO vo : list) {
            vo.setYesterdayEq(yesterdayMap.get(vo.getId()));
            vo.setLastWeekEq(weekMap.get(vo.getId()));
            vo.setLastMonthEq(monthMap.get(vo.getId()));
            if (vo.getYesterdayEq() == null) {
                vo.setYesterdayEq(new BigDecimal("0"));
            }
            if (vo.getLastWeekEq() == null) {
                vo.setLastWeekEq(new BigDecimal("0"));
            }
            if (vo.getLastMonthEq() == null) {
                vo.setLastMonthEq(new BigDecimal("0"));
            }
            Object obj = keyMap.get(vo.getRoomId() + "-" + vo.getId());
            if (Objects.isNull(obj)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(obj));
            vo.setRoomName(jsonObject.getString("room_name"));
            StringJoiner joiner = new StringJoiner("-");
            if (!StringUtils.isEmpty(vo.getRoomName())) {
                joiner.add(vo.getRoomName());
            }
            joiner.add(vo.getCabinetName());
            vo.setLocation(joiner.toString());
        }
        if (Objects.nonNull(pageReqVO.getTimeGranularity())) {
            if (pageReqVO.getTimeGranularity().equals("yesterday")) {
                list.sort(Comparator.comparing(CabinetEnergyStatisticsResVO::getYesterdayEq).reversed());
            } else if (pageReqVO.getTimeGranularity().equals("lastWeek")) {
                list.sort(Comparator.comparing(CabinetEnergyStatisticsResVO::getLastWeekEq).reversed());
            } else if (pageReqVO.getTimeGranularity().equals("lastMonth")) {
                list.sort(Comparator.comparing(CabinetEnergyStatisticsResVO::getLastMonthEq).reversed());
            }
        }
        return new PageResult<>(list, voPage.getTotal());
    }

    @Override
    public PageResult<CabinetEnergyStatisticsResVO> getEqPage1(CabinetIndexVo pageReqVO) {
        String indices = null;
        String startTime = null;
        String endTime = DateUtil.formatDateTime(DateTime.now());
        Integer total = 0;
        List<Integer> cabinetIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(pageReqVO.getCabinetIds())) {
            cabinetIds.addAll(pageReqVO.getCabinetIds());
        }
        switch (pageReqVO.getTimeGranularity()) {
            case "yesterday":
                startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
                indices = "cabinet_eq_total_day";
                break;
            case "lastWeek":
                startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
                indices = "cabinet_eq_total_week";
                break;
            case "lastMonth":
                startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
                indices = "cabinet_eq_total_month";
                break;
            default:
        }
        try {
            // 搜索源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            int pageNo = pageReqVO.getPageNo();
            int pageSize = pageReqVO.getPageSize();
            int index = (pageNo - 1) * pageSize;
            searchSourceBuilder.from(index);
            // 最后一页请求超过一万，pageSize设置成请求刚好一万条
            if (index + pageSize > 10000) {
                searchSourceBuilder.size(10000 - index);
            } else {
                searchSourceBuilder.size(pageSize);
            }
            searchSourceBuilder.trackTotalHits(true);
            searchSourceBuilder.fetchSource(new String[]{"cabinet_id"}, null);
            searchSourceBuilder.sort("eq_value", SortOrder.DESC);
            //获取需要处理的数据
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime));
            if (!CollectionUtils.isEmpty(cabinetIds)) {
                boolQueryBuilder.must(QueryBuilders.termsQuery(CABINET_ID, cabinetIds));
            }
            searchSourceBuilder.query(QueryBuilders.constantScoreQuery(boolQueryBuilder));

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            searchRequest.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<CabinetEqTotalDay> list = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits()) {
                CabinetEqTotalDay dayDo = JsonUtils.parseObject(hit.getSourceAsString(), CabinetEqTotalDay.class);
                list.add(dayDo);
            }

            if (!CollectionUtils.isEmpty(list)) {
                List<Integer> ids = list.stream().map(CabinetEqTotalDay::getCabinetId).collect(Collectors.toList());
                pageReqVO.setCabinetIds(ids);
                List<CabineIndexCfgVO> voList = cabinetIndexMapper.selectIndexLoadPage(pageReqVO);
                Map<Integer, CabineIndexCfgVO> map = voList.stream().collect(Collectors.toMap(CabineIndexCfgVO::getId, x -> x));

                //昨日
                startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
                List<CabinetEqTotalDay> yesterdayList = getDataEs(startTime, endTime, ids, "cabinet_eq_total_day", CabinetEqTotalDay.class,
                        new String[]{"cabinet_id", "eq_value"});
                Map<Integer, BigDecimal> yesterdayMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(yesterdayList)) {
                    yesterdayMap = yesterdayList.stream().collect(Collectors.toMap(CabinetEqTotalDay::getCabinetId, CabinetEqTotalDay::getEqValue));
                }

                //上周
                startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
                List<CabinetEqTotalDay> weekList = getDataEs(startTime, endTime, ids, "cabinet_eq_total_week", CabinetEqTotalDay.class, new String[]{"cabinet_id", "eq_value"});
                Map<Integer, BigDecimal> weekMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(weekList)) {
                    weekMap = weekList.stream().collect(Collectors.toMap(CabinetEqTotalDay::getCabinetId, CabinetEqTotalDay::getEqValue));
                }

                //上月
                startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
                List<CabinetEqTotalDay> monthList = getDataEs(startTime, endTime, ids, "cabinet_eq_total_month", CabinetEqTotalDay.class, new String[]{"cabinet_id", "eq_value"});
                Map<Integer, BigDecimal> monthMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(monthList)) {
                    monthMap = monthList.stream().collect(Collectors.toMap(CabinetEqTotalDay::getCabinetId, CabinetEqTotalDay::getEqValue));
                }

                List<CabinetEnergyStatisticsResVO> result = new ArrayList<>();
                for (CabinetEqTotalDay vo : list) {
                    Integer id = vo.getCabinetId();
                    CabineIndexCfgVO cabineIndexCfgVO = map.get(id);
                    CabinetEnergyStatisticsResVO vos = new CabinetEnergyStatisticsResVO().setId(cabineIndexCfgVO.getId()).setRunStatus(cabineIndexCfgVO.getRunStatus());
                    if (ObjectUtils.isNotEmpty(cabineIndexCfgVO.getRoomName())) {
                        vos.setLocation(cabineIndexCfgVO.getRoomName() + "-" + cabineIndexCfgVO.getCabinetName());
                    } else {
                        vos.setLocation(cabineIndexCfgVO.getCabinetName());
                    }
                    vos.setCabinetName(cabineIndexCfgVO.getCabinetName());
                    vos.setRoomName(cabineIndexCfgVO.getRoomName());
                    vos.setYesterdayEq(yesterdayMap.get(id));
                    vos.setLastWeekEq(weekMap.get(id));
                    vos.setLastMonthEq(monthMap.get(id));
                    if (vos.getYesterdayEq() == null) {
                        vos.setYesterdayEq(new BigDecimal("0.0"));
                    }
                    if (vos.getLastWeekEq() == null) {
                        vos.setLastWeekEq(new BigDecimal("0.0"));
                    }
                    if (vos.getLastMonthEq() == null) {
                        vos.setLastMonthEq(new BigDecimal("0.0"));
                    }
                    result.add(vos);
                }
//                getPosition(result);
                return new PageResult<>(result, searchResponse.getHits().getTotalHits().value);
            }
            return null;
        } catch (Exception e) {
            log.error("获取数据失败：", e);
            return null;
        }
    }

    @Override
    public void editHeight(int cabinetId, int sum) {
        CabinetIndex cabinetIndex = new CabinetIndex();
        cabinetIndex.setId(cabinetId).setCabinetUseHeight(sum);
        cabinetIndexMapper.updateById(cabinetIndex);
    }

    @Override
    public CabinetCapacityStatisticsResVO getCapacitystatistics() {
        return cabinetIndexMapper.getCapacitystatistics();
    }

    @Override
    public PageResult<CabinetEnvAndHumRes> getCabinetEnvPage(CabinetIndexVo pageReqVO) {
        PageResult<CabinetEnvAndHumRes> pageResult = new PageResult<CabinetEnvAndHumRes>();
        Page page = new Page(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        Page<CabineIndexCfgVO> envPage = cabinetIndexMapper.selectIndexLoadPage(page, pageReqVO);

//        List<TemColorDO> temColorAll = temColorService.getTemColorAll();

        List<CabineIndexCfgVO> records = envPage.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<CabinetEnvAndHumRes> res = BeanUtils.toBean(records, CabinetEnvAndHumRes.class);
            List<String> collect = res.stream().map(i -> REDIS_KEY_CABINET + i.getRoomId() + "-" + i.getId()).collect(Collectors.toList());
            List<Object> list = redisTemplate.opsForValue().multiGet(collect);
            Map<String, JSONObject> keyMap = list.stream().filter(i -> Objects.nonNull(i))
                    .collect(Collectors.toMap(i -> JSON.parseObject(JSONObject.toJSONString(i)).getString("cabinet_key"), x -> JSON.parseObject(JSONObject.toJSONString(x))));
            for (CabinetEnvAndHumRes env : res) {
                StringJoiner joiner = new StringJoiner("-");
                if (!StringUtils.isEmpty(env.getRoomName())) {
                    joiner.add(env.getRoomName());
                }
                joiner.add(env.getCabinetName());
                env.setLocation(joiner.toString());
                JSONObject jsonObject = keyMap.get(env.getRoomId() + "-" + env.getId());
                if (jsonObject == null) {
                    continue;
                }
                JSONObject cabinetEnv = jsonObject.getJSONObject("cabinet_env");
                if (cabinetEnv != null) {
                    JSONObject humValue = cabinetEnv.getJSONArray("hum_value").getJSONObject(0);
                    if (Objects.nonNull(humValue)) {
                        List<BigDecimal> front = humValue.getList("front", BigDecimal.class);
                        env.setIceTopHum(front.get(0));
                        env.setIceMidHum(front.get(1));
                        env.setIceBomHum(front.get(2));
                        List<BigDecimal> black = humValue.getList("black", BigDecimal.class);
                        env.setHotTopHum(black.get(0));
                        env.setHotMidHum(black.get(1));
                        env.setHotBomHum(black.get(2));
                    }
                    JSONObject temValue = cabinetEnv.getJSONArray("tem_value").getJSONObject(0);
                    if (Objects.nonNull(temValue)) {
                        List<BigDecimal> front = temValue.getList("front", BigDecimal.class);
                        env.setIceTopTem(front.get(0));
                        env.setIceMidTem(front.get(1));
                        env.setIceBomTem(front.get(2));
                        List<BigDecimal> black = temValue.getList("black", BigDecimal.class);
                        env.setHotTopTem(black.get(0));
                        env.setHotMidTem(black.get(1));
                        env.setHotBomTem(black.get(2));
                    }
                    JSONArray humAverage = cabinetEnv.getJSONArray("hum_average");
                    if (!CollectionUtils.isEmpty(humAverage)) {
                        env.setIceAverageHum(humAverage.getBigDecimal(0));
                        env.setHotAverageHum(humAverage.getBigDecimal(1));
                    }
                    JSONArray temAverage = cabinetEnv.getJSONArray("tem_average");
                    if (!CollectionUtils.isEmpty(temAverage)) {
                        env.setIceAverageTem(temAverage.getBigDecimal(0));
                        env.setHotAverageTem(temAverage.getBigDecimal(1));
                    }

                    String iceTopTemColor = temColorService.findColor(env.getIceTopTem());
                    String iceMidTemColor = temColorService.findColor(env.getIceMidTem());
                    String iceBomTemColor = temColorService.findColor(env.getIceBomTem());
                    String iceAverageTemColor = temColorService.findColor(env.getIceAverageTem());

                    String hotTopTemColor = temColorService.findColor(BigDemicalUtil.safeSubtract(env.getHotTopTem(), new BigDecimal("15")));
                    String hotMidTemColor = temColorService.findColor(BigDemicalUtil.safeSubtract(env.getHotTopTem(), new BigDecimal("15")));
                    String hotBomTemColor = temColorService.findColor(BigDemicalUtil.safeSubtract(env.getHotTopTem(), new BigDecimal("15")));
                    String hotAverageTemColor = temColorService.findColor(BigDemicalUtil.safeSubtract(env.getHotTopTem(), new BigDecimal("15")));

                    env.setIceAverageTemColor(iceAverageTemColor);
                    env.setHotAverageTemColor(hotAverageTemColor);
                    env.setIceTopTemColor(iceTopTemColor);
                    env.setIceMidTemColor(iceMidTemColor);
                    env.setIceBomTemColor(iceBomTemColor);
                    env.setHotTopTemColor(hotTopTemColor);
                    env.setHotMidTemColor(hotMidTemColor);
                    env.setHotBomTemColor(hotBomTemColor);

                }
            }
            pageResult.setList(res).setTotal(envPage.getTotal());
        }
        return pageResult;
    }

    @Override
    public CabinetDTO getCabinetCapacityDetail(int id) {
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
                List<RackIndex> rackIndexList = rackIndexMapper.selectList(new LambdaQueryWrapper<RackIndex>()
                        .eq(RackIndex::getCabinetId, index.getId())
                        .eq(RackIndex::getIsDelete, DelEnums.NO_DEL.getStatus()));
                if (!CollectionUtils.isEmpty(rackIndexList)) {
                    List<RackIndexResVO> bean = BeanUtils.toBean(rackIndexList, RackIndexResVO.class);

                    List<String> rackIds = bean.stream().map(i -> "packet:rack:" + i.getId()).distinct().collect(Collectors.toList());
                    List list = redisTemplate.opsForValue().multiGet(rackIds);
                    Map<Integer, Object> rackKey = new HashMap<>();
                    if (Objects.nonNull(list.get(0))) {
                        rackKey = (Map<Integer, Object>) list.stream().collect(Collectors.toMap(i -> JSON.parseObject(JSON.toJSONString(i)).getInteger("rack_key"), Function.identity()));
                    }
                    for (RackIndexResVO t : bean) {
                        Object obj = rackKey.get(t.getId());

                        JSONObject rackPower = JSON.parseObject(JSON.toJSONString(obj)).getJSONObject("rack_power");
                        if (Objects.nonNull(rackPower)) {
                            JSONObject totalData = rackPower.getJSONObject("total_data");
                            if (Objects.nonNull(totalData)) {
                                Double cura = totalData.getDouble("cur_a");
                                Double curb = totalData.getDouble("cur_b");
                                Double powApparent = totalData.getDouble("pow_apparent");
                                t.setPowActiveTotal(BigDecimal.valueOf(powApparent).setScale(3, BigDecimal.ROUND_HALF_DOWN));
                                t.setCurValueA(BigDecimal.valueOf(cura).setScale(2, BigDecimal.ROUND_HALF_DOWN));
                                t.setCurValueB(BigDecimal.valueOf(curb).setScale(2, BigDecimal.ROUND_HALF_DOWN));
                            }
                            JSONObject pathA = rackPower.getJSONObject("path_a");
                            if (Objects.nonNull(pathA)) {
                                Double powApparenta = pathA.getList("pow_apparent", Double.class).get(0);
                                t.setPowActiveA(BigDecimal.valueOf(powApparenta).setScale(3, BigDecimal.ROUND_HALF_DOWN));
                            }
                            JSONObject pathB = rackPower.getJSONObject("path_b");
                            if (Objects.nonNull(pathB)) {
                                Double powApparentb = pathB.getList("pow_apparent", Double.class).get(0);
                                t.setPowActiveB(BigDecimal.valueOf(powApparentb).setScale(3, BigDecimal.ROUND_HALF_DOWN));
                            }
                        }
                    }
                    dto.setRackIndexList(bean);
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

    @Override
    public Map getCabinetPFDetail(CabinetIndexVo pageReqVO) {
        Map map = new HashMap();
        LocalDateTime localDateTime = pageReqVO.getStartTime().withHour(23).withMinute(59).withSecond(59);
        String startTime = LocalDateTimeUtil.format(pageReqVO.getStartTime(), "yyyy-MM-dd HH:mm:ss");
        String endTime = LocalDateTimeUtil.format(localDateTime, "yyyy-MM-dd HH:mm:ss");
        List<String> lineHour = getData(startTime, endTime, pageReqVO.getCabinetIds(), "cabinet_hda_pow_hour");
        List<CabinetPFDetailVO> strList = lineHour.stream()
                .map(str -> JsonUtils.parseObject(str, CabinetPFDetailVO.class))
                .collect(Collectors.toList());

        CabinetPFDetailResVO vo = new CabinetPFDetailResVO();
        List<Float> factora = strList.stream().map(CabinetPFDetailVO::getFactorAAvgValue).collect(Collectors.toList());
        List<Float> factorb = strList.stream().map(CabinetPFDetailVO::getFactorBAvgValue).collect(Collectors.toList());
        List<Float> factorTotal = strList.stream().map(CabinetPFDetailVO::getFactorTotalAvgValue).collect(Collectors.toList());
        List<String> time = strList.stream().map(CabinetPFDetailVO::getCreateTime).collect(Collectors.toList());
        vo.setTime(time);
        vo.setPowerFactorAvgValueA(factora);
        vo.setPowerFactorAvgValueB(factorb);
        vo.setPowerFactorAvgValueTotal(factorTotal);
        map.put("chart", vo);
        map.put("table", strList);
        return map;
    }

    @Override
public PageResult<CabinetIndexEnvResVO> getCabinetEnv(CabinetIndexVo pageReqVO) {
    Page page = new Page(pageReqVO.getPageNo(), pageReqVO.getPageSize());
    Page<CabineIndexCfgVO> voPage = cabinetIndexMapper.selectIndexLoadPage(page, pageReqVO);
    if (!CollectionUtils.isEmpty(voPage.getRecords())) {
        List<CabineIndexCfgVO> records = voPage.getRecords();
        List<CabinetIndexEnvResVO> bean = BeanUtils.toBean(records, CabinetIndexEnvResVO.class);
        Map<String, CabinetIndexEnvResVO> map = bean.stream().collect(Collectors.toMap(vo -> REDIS_KEY_CABINET + vo.getRoomId() + "-" + vo.getId(), i -> i));
        Set<String> ids = map.keySet();
        List list = redisTemplate.opsForValue().multiGet(ids);
        for (Object obj : list) {
            if (Objects.isNull(obj)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(obj));
            String cabinetKey = jsonObject.getString("cabinet_key");
            CabinetIndexEnvResVO vo = map.get(REDIS_KEY_CABINET + cabinetKey);
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
        Map<String, CabinetIndexBalanceResVO> map = records.stream().collect(Collectors.toMap(vo -> REDIS_KEY_CABINET + vo.getRoomId() + "-" + vo.getId(), i -> i));
        List list = redisTemplate.opsForValue().multiGet(map.keySet());
        for (Object obj : list) {
            if (Objects.isNull(obj)) {
                continue;
            }
            JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(obj));
            CabinetIndexBalanceResVO cabinetKey = map.get(REDIS_KEY_CABINET + jsonObject.getString("cabinet_key"));
            cabinetKey.setRoomName(jsonObject.getString("room_name"));
            JSONObject aPath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_a");
            JSONObject bPath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_b");
            JSONObject total = jsonObject.getJSONObject("cabinet_power").getJSONObject("total_data");
            //总视在功率
            BigDecimal totalPow = total.getBigDecimal("pow_apparent");
            cabinetKey.setPowApparentTotal(totalPow);
            if (Objects.nonNull(aPath)) {
                //a的视在功率
                BigDecimal aPow = aPath.getBigDecimal("pow_apparent");
                cabinetKey.setPowApparentA(aPow);
                cabinetKey.setAPow(BigDemicalUtil.safeMultiply(BigDemicalUtil.safeDivideNum(4, aPow, totalPow), 100));

            List<BigDecimal> curValue = aPath.getList("cur_value", BigDecimal.class);
            if (!CollectionUtils.isEmpty(curValue)) {
                if (curValue.size()==3) {
                    cabinetKey.setAcurValueOne(BigDemicalUtil.setScale(curValue.get(0), 2));
                    cabinetKey.setAcurValueTwe(BigDemicalUtil.setScale(curValue.get(1), 2));
                    cabinetKey.setAcurValueThree(BigDemicalUtil.setScale(curValue.get(2), 2));
                }else {
                    cabinetKey.setAcurValueOne(BigDemicalUtil.setScale(curValue.get(0), 2));
                }
            }

            List<BigDecimal> volValue = aPath.getList("vol_value", BigDecimal.class);
            if (!CollectionUtils.isEmpty(volValue)) {
                if (volValue.size()==3) {
                    cabinetKey.setAvolValueOne(BigDemicalUtil.setScale(volValue.get(0), 1));
                    cabinetKey.setAvolValueTwe(BigDemicalUtil.setScale(volValue.get(1), 1));
                    cabinetKey.setAvolValueThree(BigDemicalUtil.setScale(volValue.get(2), 1));
                }else {
                    cabinetKey.setAvolValueOne(BigDemicalUtil.setScale(volValue.get(0), 1));
                }
            }

            List<BigDecimal> powValue = aPath.getList("pow_value", BigDecimal.class);
            if (!CollectionUtils.isEmpty(powValue)) {
                if (powValue.size()==3) {
                    cabinetKey.setApowValueOne(BigDemicalUtil.setScale(powValue.get(0), 3));
                    cabinetKey.setApowValueTwe(BigDemicalUtil.setScale(powValue.get(1), 3));
                    cabinetKey.setApowValueThree(BigDemicalUtil.setScale(powValue.get(2), 3));
                }else {
                    cabinetKey.setApowValueOne(BigDemicalUtil.setScale(powValue.get(0), 3));
                }
            }
        }
        if (Objects.nonNull(bPath)) {
            //b的视在功率
            BigDecimal bPow = bPath.getBigDecimal("pow_apparent");
            cabinetKey.setPowApparentB(bPow);
            cabinetKey.setBPow(BigDemicalUtil.safeMultiply(BigDemicalUtil.safeDivideNum(4, bPow, totalPow), 100));
            List<BigDecimal> curValue = bPath.getList("cur_value", BigDecimal.class);
            if (!CollectionUtils.isEmpty(curValue)) {
                if (curValue.size()==3) {
                    cabinetKey.setBcurValueOne(BigDemicalUtil.setScale(curValue.get(0), 2));
                    cabinetKey.setBcurValueTwe(BigDemicalUtil.setScale(curValue.get(1), 2));
                    cabinetKey.setBcurValueThree(BigDemicalUtil.setScale(curValue.get(2), 2));
                }else {
                    cabinetKey.setBcurValueOne(BigDemicalUtil.setScale(curValue.get(0), 2));
                }
            }
            List<BigDecimal> volValue = bPath.getList("vol_value", BigDecimal.class);
            if (!CollectionUtils.isEmpty(volValue)) {
                if (volValue.size()==3) {
                    cabinetKey.setBvolValueOne(BigDemicalUtil.setScale(volValue.get(0), 1));
                    cabinetKey.setBvolValueOne(BigDemicalUtil.setScale(volValue.get(1), 1));
                    cabinetKey.setBvolValueOne(BigDemicalUtil.setScale(volValue.get(2), 1));
                }else {
                    cabinetKey.setBvolValueOne(BigDemicalUtil.setScale(volValue.get(0), 1));
                }
            }
            List<BigDecimal> powValue = bPath.getList("pow_value", BigDecimal.class);
            if (!CollectionUtils.isEmpty(powValue)) {
                if (powValue.size()==3) {
                    cabinetKey.setBpowValueOne(BigDemicalUtil.setScale(powValue.get(0), 3));
                    cabinetKey.setBpowValueTwe(BigDemicalUtil.setScale(powValue.get(1), 3));
                    cabinetKey.setBpowValueThree(BigDemicalUtil.setScale(powValue.get(2), 3));
                }else {
                    cabinetKey.setBpowValueOne(BigDemicalUtil.setScale(powValue.get(0), 3));
                }
            }
        }
    }
    return new PageResult<>(records, voPage.getTotal());
}

@Override
public CabinetDistributionDetailsResVO getCabinetdistributionDetails(int id, int roomId, String type) throws IOException {

    CabinetDistributionDetailsResVO vo = new CabinetDistributionDetailsResVO();
    CabinetIndex cabinetIndex = cabinetIndexMapper.selectById(id);
    vo.setPduBox(cabinetIndex.getPduBox());
    if (cabinetIndex.getPduBox()) {
        CabinetBox cabinetBox = cabinetBusMapper.selectOne(new LambdaQueryWrapper<CabinetBox>().eq(CabinetBox::getCabinetId, id));
        if (Objects.nonNull(cabinetBox)) {
            vo.setKeyA(cabinetBox.getBoxKeyA());
            vo.setKeyB(cabinetBox.getBoxKeyB());
        }
    } else {
        CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapper<CabinetPdu>().eq(CabinetPdu::getCabinetId, id));
        if (Objects.nonNull(cabinetPdu)) {
            vo.setKeyA(cabinetPdu.getPduKeyA());
            vo.setKeyB(cabinetPdu.getPduKeyB());
        }
    }
    Object obj = redisTemplate.opsForValue().get(REDIS_KEY_CABINET + roomId + "-" + id);
    if (Objects.isNull(obj)) {
        return vo;
    }
    JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(obj));
    JSONObject apath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_a");
    JSONObject bpath = jsonObject.getJSONObject("cabinet_power").getJSONObject("path_b");
    JSONObject total = jsonObject.getJSONObject("cabinet_power").getJSONObject("total_data");

    vo.setLoadFactor(jsonObject.getBigDecimal("load_factor").setScale(0, RoundingMode.HALF_DOWN));
    vo.setCabinetName(jsonObject.getString("cabinet_name"));
    vo.setRoomName(jsonObject.getString("room_name"));
    vo.setDateTime(LocalDateTimeUtil.parse(jsonObject.getString("date_time"), "yyyy-MM-dd HH:mm:ss"));
    if (Objects.nonNull(total)) {
        vo.setPowActiveTotal(total.getBigDecimal("pow_active").setScale(1, RoundingMode.HALF_DOWN));//有功功率
        vo.setPowApparentTotal(total.getBigDecimal("pow_apparent").setScale(3, RoundingMode.HALF_DOWN));//视在功率
        vo.setPowReactiveTotal(total.getBigDecimal("pow_reactive").setScale(3, RoundingMode.HALF_DOWN));//无功功率
        vo.setPowerFactor(total.getBigDecimal("power_factor").setScale(2, RoundingMode.HALF_DOWN));//功率因素
    }
    if (Objects.nonNull(apath)) {
        vo.setCurA(apath.getList("cur_value", BigDecimal.class).stream().map(i -> i.setScale(2, RoundingMode.HALF_DOWN)).collect(Collectors.toList()));
        vo.setVolA(apath.getList("vol_value", BigDecimal.class).stream().map(i -> i.setScale(1, RoundingMode.HALF_DOWN)).collect(Collectors.toList()));
        vo.setPowActiveA(apath.getBigDecimal("pow_active").setScale(1, RoundingMode.HALF_DOWN));//有功功率
        vo.setPowApparentA(apath.getBigDecimal("pow_apparent").setScale(3, RoundingMode.HALF_DOWN));//视在功率
        vo.setPowReactiveA(apath.getBigDecimal("pow_reactive").setScale(3, RoundingMode.HALF_DOWN));//无功功率
        vo.setPowerFactorA(apath.getBigDecimal("power_factor").setScale(2, RoundingMode.HALF_DOWN));//功率因素
        vo.setAPow(BigDemicalUtil.safeMultiply(BigDemicalUtil.safeDivideNum(4, vo.getPowApparentA(), vo.getPowApparentTotal()), 100));
    }
    if (Objects.nonNull(bpath)) {
        vo.setCurB(bpath.getList("cur_value", BigDecimal.class).stream().map(i -> i.setScale(2, RoundingMode.HALF_DOWN)).collect(Collectors.toList()));
        vo.setVolB(bpath.getList("vol_value", BigDecimal.class).stream().map(i -> i.setScale(1, RoundingMode.HALF_DOWN)).collect(Collectors.toList()));
        vo.setPowActiveB(bpath.getBigDecimal("pow_active").setScale(1, RoundingMode.HALF_DOWN));//有功功率
        vo.setPowApparentB(bpath.getBigDecimal("pow_apparent").setScale(3, RoundingMode.HALF_DOWN));//视在功率
        vo.setPowReactiveB(bpath.getBigDecimal("pow_reactive").setScale(3, RoundingMode.HALF_DOWN));//无功功率
        vo.setPowerFactorB(bpath.getBigDecimal("power_factor").setScale(2, RoundingMode.HALF_DOWN));//功率因素
        vo.setBPow(BigDemicalUtil.safeMultiply(BigDemicalUtil.safeDivideNum(4, vo.getPowApparentB(), vo.getPowApparentTotal()), 100));

        Map map = getCabinetDistributionFactor(id, roomId, type);
        vo.setDay((List<String>) map.get("day"));
        vo.setFactorTotal((List<BigDecimal>) map.get("factorTotal"));
        if (Objects.nonNull(map.get("load_rate"))) {
            vo.setLoadFactorBig(BigDemicalUtil.setScale(new BigDecimal(String.valueOf(map.get("load_rate"))), 2));
            vo.setLoadFactorTime((String) map.get("create_time"));
        }
//            vo.setFactorTotal((List<BigDecimal>) map.get("factorTotal"));
//            vo.setFactorA((List<BigDecimal>) map.get("factorA"));
//            vo.setFactorB((List<BigDecimal>) map.get("factorB"));
        }


        return vo;
    }

    @Override
    public Map getCabinetDistributionFactor(int id, int roomId, String type) throws IOException {
        String startTime = null;
        String endTime = null;
        String index = null;
        String key = null;
        String[] heads = new String[0];
        switch (type) {
            case "day":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusDays(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
                index = "cabinet_hda_pow_hour";
                key = "load_rate_total_avg_value";
                heads = new String[]{"cabinet_id", "load_rate_total_avg_value", "create_time"};
                break;
            case "hour":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusHours(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
                index = "cabinet_hda_pow_realtime";
                key = "load_rate";
                heads = new String[]{"cabinet_id", "load_rate", "create_time"};
                break;
            case "today":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd 00:00:00");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
                index = "cabinet_hda_pow_hour";
                key = "load_rate_total_avg_value";
                heads = new String[]{"cabinet_id", "load_rate_total_avg_value", "create_time"};
                break;
            case "threeDay":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusDays(3), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
                index = "cabinet_hda_pow_hour";
                key = "load_rate_total_avg_value";
                heads = new String[]{"cabinet_id", "load_rate_total_avg_value", "create_time"};
                break;
            default:
        }


        Map map = new HashMap();
        //day,today,threeDay
        List<Map<String, Object>> data = getDataEs(startTime, endTime, Collections.singletonList(id),
                index, Map.class, heads);

        List<BigDecimal> factorA = new ArrayList<>();
        List<BigDecimal> factorB = new ArrayList<>();
        List<BigDecimal> factorTotal = new ArrayList<>();
        List<BigDecimal> loadRate = new ArrayList<>();
        List<String> createTime = new ArrayList<>();
        if (CollectionUtils.isEmpty(data)) {
            return map;
        }
        data = data.stream().sorted(Comparator.comparing(i -> i.get("create_time").toString())).collect(Collectors.toList());
        if (Objects.nonNull(data)) {
            switch (index) {
                case "cabinet_hda_pow_realtime":
//                    factorA = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("factor_a").toString()), 100)).collect(Collectors.toList());
//                    factorB = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("factor_b").toString()), 100)).collect(Collectors.toList());
//                    factorTotal = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("factor_total").toString()), 100)).collect(Collectors.toList());
                    loadRate = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("load_rate").toString()), 100)).collect(Collectors.toList());
                    createTime = data.stream().map(i -> String.valueOf(i.get("create_time"))).collect(Collectors.toList());
                    break;
                case "cabinet_hda_pow_hour":
//                    factorA = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("factor_a_avg_value").toString()), 100)).collect(Collectors.toList());
//                    factorB = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("factor_b_avg_value").toString()), 100)).collect(Collectors.toList());
//                    factorTotal = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("factor_total_avg_value").toString()), 100)).collect(Collectors.toList());
                    loadRate = data.stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("load_rate_total_avg_value").toString()), 100)).collect(Collectors.toList());
                    createTime = data.stream().map(i -> String.valueOf(i.get("create_time"))).collect(Collectors.toList());
                    break;
                default:
            }
        }
//        map.put("factorA", factorA);
//        map.put("factorB", factorB);
//        map.put("factorTotal", factorTotal);
        map.put("factorTotal", loadRate);
        map.put("day", createTime);
        Map<String, Object> loadRateEs = getDataLoadRateEs(startTime, endTime, id, index, key);
        if (Objects.nonNull(loadRateEs)) {
            map.put("load_rate", loadRateEs.get("load_rate"));
            map.put("create_time", loadRateEs.get("create_time"));
        }
        return map;
    }

    @Override
    public CabinetPowerLoadDetailRespVO getDetailData(CabinetPowerLoadDetailReqVO reqVO) {
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get(REDIS_KEY_CABINET + reqVO.getRoomId() + "-" + reqVO.getId());
        if (jsonObject == null) {
            return null;
        }
        BigDecimal powCapacity = jsonObject.getBigDecimal("pow_capacity");
        BigDecimal powApparent = jsonObject.getJSONObject("cabinet_power").getJSONObject("total_data").getBigDecimal("pow_apparent");//视在功率=运行负荷
        BigDecimal margin = BigDemicalUtil.safeSubtract(2, powCapacity, powApparent);//余量

        BigDecimal powActive = jsonObject.getJSONObject("cabinet_power").getJSONObject("total_data").getBigDecimal("pow_value");
        BigDecimal powReactive = jsonObject.getJSONObject("cabinet_power").getJSONObject("total_data").getBigDecimal("pow_reactive");

        // 等待异步操作完成并获取结果
        Double peakDemand = findEsByPowApparentMax(reqVO);

        // 返回数据
        CabinetPowerLoadDetailRespVO respVO = new CabinetPowerLoadDetailRespVO();
        respVO.setRunLoad(powApparent);
        respVO.setRatedCapacity(powCapacity);
        respVO.setReserveMargin(margin);
        respVO.setPowActive(powActive);
        respVO.setPowReactive(powReactive);
        if (Objects.nonNull(peakDemand))
            respVO.setPeakDemand(BigDecimal.valueOf(peakDemand));
        return respVO;
    }

    private Double findEsByPowApparentMax(CabinetPowerLoadDetailReqVO reqVO) {
        // 执行 Elasticsearch 查询bus_hda_total_hour近24小时总视在功率最大值
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices("cabinet_hda_pow_hour");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


            String startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusDays(1), "yyyy-MM-dd HH:mm:ss");
            String endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
            searchSourceBuilder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                    .must(QueryBuilders.termQuery("cabinet_id", reqVO.getId()))));
            searchSourceBuilder.aggregation(
                    AggregationBuilders.max("pow_apparent_max").field("apparent_total_max_value")
            );
            searchSourceBuilder.size(1);
            searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
            searchRequest.source(searchSourceBuilder);

            // 执行搜索
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            // 从聚合结果中获取最大值
            Max maxAggregation = searchResponse.getAggregations().get("pow_apparent_max");
            return maxAggregation.getValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Map<String, List<CabinetLoadPageChartResVO>> getLineChartDetailData(CabinetPowerLoadDetailReqVO reqVO) {
        Map<String, List<CabinetLoadPageChartResVO>> resultMap = new HashMap<>();
        String str = StrUtils.redisKeyByLoginId(null, "/cabinet/loadPage/chart-detail", reqVO);
        Object obj = redisTemplate.opsForValue().get(str);
        if (ObjectUtil.isNotEmpty(obj)) {
            JSONObject jsonObject = JSONObject.parseObject(obj.toString());
            resultMap = JSON.toJavaObject(jsonObject, Map.class);
            return resultMap;
        }
        Integer cabinet = reqVO.getId();
        if (cabinet == null) {
            return null;
        }
        List<CabinetLoadPageChartResVO> aPath = new ArrayList<>();
        List<CabinetLoadPageChartResVO> bPath = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String start = null;
        String end = LocalDateTime.now().format(formatter);
        String index;
        String[] heads;
        String idKey;
        if (Objects.equals(reqVO.getType(), 0)) {
            idKey = "cabinet_id";
            switch (reqVO.getGranularity()) {
                case "realtime":
                    index = "cabinet_hda_pow_realtime";
                    heads = new String[]{"cabinet_id", "apparent_total", "apparent_a", "apparent_b", "active_total", "active_a",
                            "active_b", "reactive_a", "reactive_b", "reactive_total", "factor_a", "factor_b", "factor_total", "load_rate", "create_time"};
                    start = LocalDateTime.now().minusHours(1).format(formatter);
                    break;
                case "hour":
                    index = "cabinet_hda_pow_hour";
                    heads = new String[]{"cabinet_id", "apparent_a_avg_value", "apparent_b_avg_value", "active_a_avg_value", "active_b_avg_value",
                            "apparent_total_avg_value", "active_total_avg_value", "reactive_a_avg_value", "reactive_b_avg_value", "reactive_total_avg_value",
                            "factor_a_avg_value", "factor_b_avg_value", "factor_total_avg_value", "load_rate_total_avg_value", "create_time"};
                    start = LocalDateTime.now().minusDays(1).format(formatter);
                    break;
                case "SeventyHours":
                    index = "cabinet_hda_pow_hour";
                    heads = new String[]{"cabinet_id", "apparent_a_avg_value", "apparent_b_avg_value", "active_a_avg_value", "active_b_avg_value",
                            "apparent_total_avg_value", "active_total_avg_value", "reactive_a_avg_value", "reactive_b_avg_value", "reactive_total_avg_value",
                            "factor_a_avg_value", "factor_b_avg_value", "factor_total_avg_value", "load_rate_total_avg_value", "create_time"};
                    start = LocalDateTime.now().minusDays(3).format(formatter);
                    break;
                default:
                    index = "cabinet_hda_pow_day";
                    heads = new String[]{"cabinet_id", "apparent_a_avg_value", "apparent_b_avg_value", "active_a_avg_value", "active_b_avg_value",
                            "apparent_total_avg_value", "active_total_avg_value", "reactive_a_avg_value", "reactive_b_avg_value", "reactive_total_avg_value",
                            "factor_a_avg_value", "factor_b_avg_value", "factor_total_avg_value", "load_rate_total_avg_value", "create_time"};
                    start = LocalDateTime.now().minusMonths(1).format(formatter);
            }
            List<Map<String, Object>> mapList = getDataEsChart(start, end, idKey, cabinet, index, heads);
            mapList.forEach(map -> {
                // 获取文档内容，假设它以 Map 的形式存储
                CabinetLoadPageChartResVO vo = new CabinetLoadPageChartResVO();
                if (Objects.equals(reqVO.getGranularity(), "realtime")) {
                    vo.setCabinetId((Integer) map.get("cabinet_id"));
                    vo.setCreateTime(String.valueOf(map.get("create_time")));
                    vo.setPowActiveA((Double) map.get("active_a"));
                    vo.setPowApparentA((Double) map.get("apparent_a"));
                    vo.setPowActiveTotal((Double) map.get("active_total"));
                    vo.setPowActiveB((Double) map.get("active_b"));
                    vo.setPowApparentB((Double) map.get("apparent_b"));
                    vo.setPowApparentTotal((Double) map.get("apparent_total"));
                    vo.setLoadRateTotal((Double) map.get("load_rate"));
                    vo.setPowReactiveA((Double) map.get("reactive_a"));
                    vo.setPowReactiveB((Double) map.get("reactive_b"));
                    vo.setPowReactiveTotal((Double) map.get("reactive_total"));
                    vo.setPowerFactorA((Double) map.get("factor_a"));
                    vo.setPowerFactorB((Double) map.get("factor_b"));
                    vo.setPowerFactorTotal((Double) map.get("factor_total"));
                    aPath.add(vo);
                } else {
                    vo.setCabinetId((Integer) map.get("cabinet_id"));
                    vo.setCreateTime(String.valueOf(map.get("create_time")));
                    vo.setPowActiveA((Double) map.get("active_a_avg_value"));
                    vo.setPowApparentA((Double) map.get("apparent_a_avg_value"));
                    vo.setPowApparentTotal((Double) map.get("apparent_total_avg_value"));
                    vo.setPowActiveTotal((Double) map.get("active_total_avg_value"));
                    vo.setPowActiveB((Double) map.get("active_b_avg_value"));
                    vo.setPowApparentB((Double) map.get("apparent_b_avg_value"));
                    vo.setLoadRateTotal((Double) map.get("load_rate_total_avg_value"));
                    vo.setPowReactiveA((Double) map.get("reactive_a_avg_value"));
                    vo.setPowReactiveB((Double) map.get("reactive_b_avg_value"));
                    vo.setPowReactiveTotal((Double) map.get("reactive_total_avg_value"));
                    vo.setPowerFactorA((Double) map.get("factor_a_avg_value"));
                    vo.setPowerFactorB((Double) map.get("factor_b_avg_value"));
                    vo.setPowerFactorTotal((Double) map.get("factor_total_avg_value"));
                    aPath.add(vo);
                }
            });
            resultMap.put("aPath", aPath);
        } else {
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapper<CabinetPdu>().eq(CabinetPdu::getCabinetId, cabinet).last("limit 1"));
            if (Objects.isNull(cabinetPdu)) {
                return null;
            }
            cabinetPdu.getPduKeyA();

            PduIndexDo a = pduIndexDoMapper.selectOne(new LambdaQueryWrapper<PduIndexDo>().eq(PduIndexDo::getPduKey, cabinetPdu.getPduKeyA()));
            PduIndexDo b = pduIndexDoMapper.selectOne(new LambdaQueryWrapper<PduIndexDo>().eq(PduIndexDo::getPduKey, cabinetPdu.getPduKeyB()));

            idKey = "pdu_id";
            switch (reqVO.getGranularity()) {
                case "realtime":
                    index = "pdu_hda_line_realtime";
                    heads = new String[]{"pdu_id", "line_id", "vol_value", "cur_value", "create_time"};
                    start = LocalDateTime.now().minusHours(1).format(formatter);
                    break;
                case "hour":
                    index = "pdu_hda_line_hour";
                    heads = new String[]{"cabinet_id", "line_id", "cur_avg_value", "vol_avg_value", "create_time"};
                    start = LocalDateTime.now().minusDays(1).format(formatter);
                    break;
                case "SeventyHours":
                    index = "pdu_hda_line_hour";
                    heads = new String[]{"pdu_id", "line_id", "cur_avg_value", "vol_avg_value", "create_time"};
                    start = LocalDateTime.now().minusDays(3).format(formatter);
                    break;
                default:
                    index = "pdu_hda_line_day";
                    heads = new String[]{"pdu_id", "line_id", "cur_avg_value", "vol_avg_value", "create_time"};
                    start = LocalDateTime.now().minusMonths(1).format(formatter);
            }

            List<CabinetLoadPageChartResVO> aPathVc = new ArrayList<>();
            List<CabinetLoadPageChartResVO> bPathVc = new ArrayList<>();
            if (Objects.nonNull(a)) {
                List<Map<String, Object>> aList = getDataEsChart(start, end, idKey, a.getId(), index, heads);
                Map<String, List<Map<String, Object>>> aMap = aList.stream().collect(Collectors.groupingBy(i -> (String) i.get("create_time")));
                for (String key : aMap.keySet()) {
                    CabinetLoadPageChartResVO avo = new CabinetLoadPageChartResVO();
                    List<Map<String, Object>> list = aMap.get(key);
                    Double vol;
                    Double cur;
                    Map<Integer, List<Map<String, Object>>> lineId = list.stream().collect(Collectors.groupingBy(i -> (Integer) i.get("line_id")));
                    for (Integer linekey : lineId.keySet()) {
                        for (Map<String, Object> map : lineId.get(linekey)) {
                            switch (linekey) {
                                case 1:
                                    if (Objects.equals("realtime", reqVO.getGranularity())) {
                                        vol = (Double) map.get("vol_value");
                                        cur = (Double) map.get("cur_value");
                                    } else {
                                        vol = (Double) map.get("vol_avg_value");
                                        cur = (Double) map.get("cur_avg_value");
                                    }
                                    avo.setVolValue(BigDemicalUtil.setScale(vol, 1).doubleValue());
                                    avo.setCurValue(BigDemicalUtil.setScale(cur, 2).doubleValue());
                                    break;
                                case 2:
                                    if (Objects.equals("realtime", reqVO.getGranularity())) {
                                        vol = (Double) map.get("vol_value");
                                        cur = (Double) map.get("cur_value");
                                    } else {
                                        vol = (Double) map.get("vol_avg_value");
                                        cur = (Double) map.get("cur_avg_value");
                                    }
                                    avo.setVolValuell(BigDemicalUtil.setScale(vol, 1).doubleValue());
                                    avo.setCurValuell(BigDemicalUtil.setScale(cur, 2).doubleValue());
                                    break;
                                case 3:
                                    if (Objects.equals("realtime", reqVO.getGranularity())) {
                                        vol = (Double) map.get("vol_value");
                                        cur = (Double) map.get("cur_value");
                                    } else {
                                        vol = (Double) map.get("vol_avg_value");
                                        cur = (Double) map.get("cur_avg_value");
                                    }
                                    avo.setVolValuelll(BigDemicalUtil.setScale(vol, 1).doubleValue());
                                    avo.setCurValuelll(BigDemicalUtil.setScale(cur, 2).doubleValue());
                                    break;
                                default:
                            }
                        }
                    }
                    avo.setCreateTime(key);
                    aPathVc.add(avo);
                }
            }
            if (Objects.nonNull(b)) {
                List<Map<String, Object>> bList = getDataEsChart(start, end, idKey, b.getId(), index, heads);
                Map<String, List<Map<String, Object>>> bMap = bList.stream().collect(Collectors.groupingBy(i -> (String) i.get("create_time")));
                for (String key : bMap.keySet()) {
                    CabinetLoadPageChartResVO bvo = new CabinetLoadPageChartResVO();
                    List<Map<String, Object>> list = bMap.get(key);
                    Double vol = null;
                    Double cur = null;
                    Map<Integer, List<Map<String, Object>>> lineId = list.stream().collect(Collectors.groupingBy(i -> (Integer) i.get("line_id")));
                    for (Integer linekey : lineId.keySet()) {
                        for (Map<String, Object> map : lineId.get(linekey)) {
                            switch (linekey) {
                                case 1:
                                    if (Objects.equals("realtime", reqVO.getGranularity())) {
                                        vol = (Double) map.get("vol_value");
                                        cur = (Double) map.get("cur_value");
                                    } else {
                                        vol = (Double) map.get("vol_avg_value");
                                        cur = (Double) map.get("cur_avg_value");
                                    }
                                    bvo.setVolValue(BigDemicalUtil.setScale(vol, 1).doubleValue());
                                    bvo.setCurValue(BigDemicalUtil.setScale(cur, 2).doubleValue());
                                    break;
                                case 2:
                                    if (Objects.equals("realtime", reqVO.getGranularity())) {
                                        vol = (Double) map.get("vol_value");
                                        cur = (Double) map.get("cur_value");
                                    } else {
                                        vol = (Double) map.get("vol_avg_value");
                                        cur = (Double) map.get("cur_avg_value");
                                    }
                                    bvo.setVolValuell(BigDemicalUtil.setScale(vol, 1).doubleValue());
                                    bvo.setCurValuell(BigDemicalUtil.setScale(cur, 2).doubleValue());
                                    break;
                                case 3:
                                    if (Objects.equals("realtime", reqVO.getGranularity())) {
                                        vol = (Double) map.get("vol_value");
                                        cur = (Double) map.get("cur_value");
                                    } else {
                                        vol = (Double) map.get("vol_avg_value");
                                        cur = (Double) map.get("cur_avg_value");
                                    }
                                    bvo.setVolValuelll(BigDemicalUtil.setScale(vol, 1).doubleValue());
                                    bvo.setCurValuelll(BigDemicalUtil.setScale(cur, 2).doubleValue());
                                    break;
                                default:
                            }
                        }
                    }
                    bvo.setCreateTime(key);
                    bPathVc.add(bvo);
                }
            }


            resultMap.put("aPathVc", aPathVc);
            resultMap.put("bPathVc", bPathVc);
        }
        redisTemplate.opsForValue().set(str, JSONObject.toJSONString(resultMap), 5, TimeUnit.MINUTES);
        return resultMap;
    }

    @Override
    public List<CabinetEnergyMaxResVO> getEnergyMax() {
        List<CabinetEnergyMaxResVO> result = new ArrayList<>();
        String endTime = DateUtil.formatDateTime(DateTime.now());
        String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
        //借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
        extractedMaxEq("cabinet_eq_total_day", startTime, endTime, result, 0);

        //上周
        startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
        extractedMaxEq("cabinet_eq_total_week", startTime, endTime, result, 1);
        //上月
        startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
        extractedMaxEq("cabinet_eq_total_week", startTime, endTime, result, 2);

        return result;
    }

    private void extractedMaxEq(String indexEs, String startTime, String endTime, List<CabinetEnergyMaxResVO> result, Integer type) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(indexEs);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                    .gte(startTime).lte(endTime))));
            builder.sort("eq_value", SortOrder.DESC);
            // 设置搜索条件
            searchRequest.source(builder);
            builder.size(1);
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (hits.length > 0) {
                // 获取最大值和时间字段
                Map<String, Object> sourceAsMap = hits[0].getSourceAsMap();
                CabinetEnergyMaxResVO vo = new CabinetEnergyMaxResVO();
                vo.setEqValue((Double) sourceAsMap.get("eq_value"));
                vo.setId((Integer) sourceAsMap.get("cabinet_id"));
                CabinetIndex cabinetIndex = cabinetIndexMapper.selectById(vo.getId());
                if (Objects.nonNull(cabinetIndex)) {
                    RoomIndex roomIndex = roomIndexMapper.selectById(cabinetIndex.getRoomId());
                    StringJoiner joiner = new StringJoiner("-");
                    vo.setRoomName(roomIndex.getRoomName());
                    vo.setCabinetName(cabinetIndex.getCabinetName());
                    joiner.add(roomIndex.getRoomName()).add(cabinetIndex.getCabinetName());
                    vo.setLocation(joiner.toString());
                }
                vo.setType(type);//借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
                result.add(vo);
            }
        } catch (Exception e) {
            log.error("插接箱用能最大查询异常：" + e);
            e.printStackTrace();
        }
    }

    private List<Map<String, Object>> getDataEsChart(String startTime, String endTime, String idKey, Integer id, String index, String[] heads) {
        try {
            // 搜索源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            searchSourceBuilder.size(10000);
            searchSourceBuilder.trackTotalHits(true);
            // 搜索请求对象
            SearchRequest searchRequest = new SearchRequest();
            searchSourceBuilder.query(QueryBuilders.termQuery(idKey, id));

            searchRequest.indices(index);
            searchSourceBuilder.fetchSource(heads, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(startTime)
                    .to(endTime));
            searchRequest.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<Map<String, Object>> list = new ArrayList<>();
            // 搜索结果
            if (searchResponse != null) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    Map<String, Object> map = hit.getSourceAsMap();
                    list.add(map);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("机柜负荷详情错误" + e);
        }
        return null;
    }

    private List getDataEs(String startTime, String endTime, List<Integer> ids, String index, Class objClass, String[] heads) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.fetchSource(heads, null);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                    .must(QueryBuilders.termsQuery(CABINET_ID, ids))));
//            builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
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

    private Map<String, Object> getDataLoadRateEs(String startTime, String endTime, Integer id, String index, String key) {
        try {
            // 搜索源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.size(1);
            searchSourceBuilder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                    .must(QueryBuilders.termsQuery(CABINET_ID, id.toString()))));

            searchSourceBuilder.sort(key, SortOrder.DESC);
            SearchRequest searchRequest1 = new SearchRequest();
            searchRequest1.indices(index);
            //query条件--正常查询条件
            searchRequest1.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse1.getHits();
            Map<String, Object> map = new HashMap();
            if (hits.getTotalHits().value > 0) {
                SearchHit hit = hits.getAt(0);
                map.put("create_time", hit.getSourceAsMap().get("create_time").toString());
                map.put("load_rate", hit.getSourceAsMap().get(key));
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 机柜配电状态统计
     *
     * @return
     */
    @Override
    public CabinetRunStatusResVO getCabinetRunStatus() {
        //查询全部的机柜配电状态
        CabinetRunStatusResVO statusCounts = cabinetCfgMapper.selectRunStatus();
        return statusCounts;
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
        cabinetPdu.setPduKeyA(vo.getPduIpA() + "-" + vo.getCasIdA());
        cabinetPdu.setPduKeyB(vo.getPduIpB() + "-" + vo.getCasIdB());
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
        return !CollectionUtils.isEmpty(pduFlag);
    }

    @Autowired
    private ICabinetEnvSensorService cabinetEnvSensorService;

    /**
     * 保存机柜环境数据
     */
    private void saveEnvSensor(int cabinetId, CabinetVo vo) {
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
    private void saveRack(int cabinetId, CabinetVo vo) {
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
    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index) {
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

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

}
