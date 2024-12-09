package cn.iocoder.yudao.module.cabinet.service.index;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalDayDo;

import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowHourDo;

import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetEnvSensor;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetEnvSensorMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetPduMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.PduIndex;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.temcolor.TemColorDO;
import cn.iocoder.yudao.module.cabinet.dal.mysql.temcolor.TemColorMapper;
import cn.iocoder.yudao.module.cabinet.mapper.*;
import cn.iocoder.yudao.module.cabinet.service.temcolor.TemColorService;
import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.cabinet.dal.mysql.index.CabIndexMapper;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.cabinet.enums.ErrorCodeConstants.*;

/**
 * 机柜索引 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IndexServiceImpl implements IndexService {

    @Resource
    private CabIndexMapper cabIndexMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private AisleIndexMapper aisleIndexMapper;

    @Autowired
    private CabinetPduMapper cabinetPduMapper;

    @Autowired
    private NewPDUIndexMapper pduIndexMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CabinetEnvSensorMapper cabinetEnvSensorMapper;

    @Resource
    private TemColorMapper temColorMapper;

    @Autowired
    private TemColorService temColorService;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Integer createIndex(IndexSaveReqVO createReqVO) {
        // 插入
        IndexDO index = BeanUtils.toBean(createReqVO, IndexDO.class);
        cabIndexMapper.insert(index);
        // 返回
        return index.getId();
    }

    @Override
    public void updateIndex(IndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        IndexDO updateObj = BeanUtils.toBean(updateReqVO, IndexDO.class);
        cabIndexMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Integer id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        cabIndexMapper.deleteById(id);
    }

    private void validateIndexExists(Integer id) {
        if (cabIndexMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public IndexDO getIndex(Integer id) {
        return cabIndexMapper.selectById(id);
    }

    @Override
    public PageResult<IndexDO> getIndexPage(IndexPageReqVO pageReqVO) {
        return cabIndexMapper.selectPage(pageReqVO);
    }


    @Override
    public Map getReportConsumeDataById(String Id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase barRes = new CabinetChartResBase();
        BarSeries barSeries = new BarSeries();
        try {
            if(Id != null){
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "cabinet_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "cabinet_eq_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getCabinetData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index);
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay){
                    for (String str : cabinetData) {
                        nowTimes++;
                        CabinetEleTotalRealtimeDo cabinetEleTotalRealtimeDo = JsonUtils.parseObject(str, CabinetEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = cabinetEleTotalRealtimeDo.getEleTotal();
                        }
                        if (nowTimes > 1){
                            barSeries.getData().add((float)(cabinetEleTotalRealtimeDo.getEleTotal() -lastEq));
                            barRes.getTime().add(cabinetEleTotalRealtimeDo.getCreateTime().split(" ")[1]);
                        }
                        lastEq = cabinetEleTotalRealtimeDo.getEleTotal();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "ele_total");
                    CabinetEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, CabinetEleTotalRealtimeDo.class);
                    if(eleMaxValue != null){
                        maxEle = eleMaxValue.getEleTotal();
                        maxEleTime = eleMaxValue.getCreateTime();
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("totalEle",totalEq);
                    result.put("maxEle",maxEle);
                    result.put("maxEleTime",maxEleTime);
                    result.put("firstEq",firstEq);
                    result.put("lastEq",lastEq);
                    result.put("barRes",barRes);
                }else {
                    for (String str : cabinetData) {
                        nowTimes++;
                        CabinetEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, CabinetEqTotalDayDo.class);
                        totalEq += totalDayDo.getEqValue();
                        barSeries.getData().add((float)totalDayDo.getEqValue());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "eq_value");
                    CabinetEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, CabinetEqTotalDayDo.class);
                    if(eqMaxValue != null){
                        maxEle = eqMaxValue.getEqValue();
                        maxEleTime = eqMaxValue.getStartTime().toString("yyyy-MM-dd HH:mm:ss");
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("totalEle",totalEq);
                    result.put("maxEle",maxEle);
                    result.put("maxEleTime",maxEleTime);
                    result.put("barRes",barRes);
                }
            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getReportPowDataById(String Id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        CabinetChartResBase aLineRes = new CabinetChartResBase();
        CabinetChartResBase bLineRes = new CabinetChartResBase();

        result.put("totalLineRes",totalLineRes);
        result.put("aLineRes",aLineRes);
        result.put("bLineRes",bLineRes);

        result.put("apparentPowMaxValue",null);
        result.put("apparentPowMaxTime",null);
        result.put("apparentPowMinValue",null);
        result.put("apparentPowMinTime",null);
        result.put("activePowMaxValue", null);
        result.put("activePowMaxTime",  null);
        result.put("activePowMinValue", null);
        result.put("activePowMinTime",  null);

        result.put("AapparentPowMaxValue",  null);
        result.put("AapparentPowMaxTime",   null);
        result.put("AapparentPowMinValue",  null);
        result.put("AapparentPowMinTime",   null);
        result.put("AactivePowMaxValue",    null);
        result.put("AactivePowMaxTime",     null);
        result.put("AactivePowMinValue",    null);
        result.put("AactivePowMinTime",     null);

        result.put("BapparentPowMaxValue",  null);
        result.put("BapparentPowMaxTime",   null);
        result.put("BapparentPowMinValue",  null);
        result.put("BapparentPowMinTime",   null);
        result.put("BactivePowMaxValue",    null);
        result.put("BactivePowMaxTime",     null);
        result.put("BactivePowMinValue",    null);
        result.put("BactivePowMinTime",     null);
        try {
            if(Id != null) {
                String index = null;

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "cabinet_hda_pow_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "cabinet_hda_pow_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getCabinetData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index);
                List<CabinetPowHourDo> cabinetPowHourDoList = cabinetData.stream().map(str -> JsonUtils.parseObject(str, CabinetPowHourDo.class)).collect(Collectors.toList());


                LineSeries totalApparentPow = new LineSeries();
                totalApparentPow.setName("总平均视在功率");
                LineSeries totalActivePow = new LineSeries();
                totalActivePow.setName("总平均有功功率");
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);


                LineSeries apparentPowA = new LineSeries();
                apparentPowA.setName("A平均视在功率");
                LineSeries activePowA = new LineSeries();
                activePowA.setName("A平均有功功率");
                aLineRes.getSeries().add(apparentPowA);
                aLineRes.getSeries().add(activePowA);


                LineSeries apparentPowB = new LineSeries();
                apparentPowB.setName("B平均视在功率");
                LineSeries activePowB = new LineSeries();
                activePowB.setName("B平均有功功率");
                bLineRes.getSeries().add(apparentPowB);
                bLineRes.getSeries().add(activePowB);

                if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                    cabinetPowHourDoList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().split(" ")[1]);

                        apparentPowA.getData().add(hourdo.getApparentAAvgValue());
                        activePowA.getData().add(hourdo.getActiveAAvgValue());
                        aLineRes.getTime().add(hourdo.getCreateTime().split(" ")[1]);

                        apparentPowB.getData().add(hourdo.getApparentBAvgValue());
                        activePowB.getData().add(hourdo.getActiveBAvgValue());
                        bLineRes.getTime().add(hourdo.getCreateTime().split(" ")[1]);
                    });
                }else{
                    cabinetPowHourDoList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().split(" ")[0]);

                        apparentPowA.getData().add(hourdo.getApparentAAvgValue());
                        activePowA.getData().add(hourdo.getActiveAAvgValue());
                        aLineRes.getTime().add(hourdo.getCreateTime().split(" ")[0]);

                        apparentPowB.getData().add(hourdo.getApparentBAvgValue());
                        activePowB.getData().add(hourdo.getActiveBAvgValue());
                        bLineRes.getTime().add(hourdo.getCreateTime().split(" ")[0]);
                    });
                }

                String apparentTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_total_max_value");
                CabinetPowHourDo totalMaxApparent = JsonUtils.parseObject(apparentTotalMaxValue, CabinetPowHourDo.class);
                String apparentTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_total_min_value");
                CabinetPowHourDo totalMinApparent = JsonUtils.parseObject(apparentTotalMinValue, CabinetPowHourDo.class);

                String activeTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_total_max_value");
                CabinetPowHourDo totalMaxActive = JsonUtils.parseObject(activeTotalMaxValue, CabinetPowHourDo.class);
                String activeTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_total_min_value");
                CabinetPowHourDo totalMinActive = JsonUtils.parseObject(activeTotalMinValue, CabinetPowHourDo.class);


                String apparentAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_a_max_value");
                CabinetPowHourDo maxApparentA = JsonUtils.parseObject(apparentAMaxValue, CabinetPowHourDo.class);
                String apparentAMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_a_min_value");
                CabinetPowHourDo minApparentA = JsonUtils.parseObject(apparentAMinValue, CabinetPowHourDo.class);

                String activeAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_a_max_value");
                CabinetPowHourDo maxActiveA = JsonUtils.parseObject(activeAMaxValue, CabinetPowHourDo.class);
                String activeAMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_a_min_value");
                CabinetPowHourDo minActiveA = JsonUtils.parseObject(activeAMinValue, CabinetPowHourDo.class);

                String apparentBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_b_max_value");
                CabinetPowHourDo maxApparentB = JsonUtils.parseObject(apparentBMaxValue, CabinetPowHourDo.class);
                String apparentBMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_b_min_value");
                CabinetPowHourDo minApparentB = JsonUtils.parseObject(apparentBMinValue, CabinetPowHourDo.class);

                String activeBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_b_max_value");
                CabinetPowHourDo maxActiveB = JsonUtils.parseObject(activeBMaxValue, CabinetPowHourDo.class);
                String activeBMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_b_min_value");
                CabinetPowHourDo minActiveB = JsonUtils.parseObject(activeBMinValue, CabinetPowHourDo.class);

                result.put("totalLineRes",totalLineRes);
                result.put("aLineRes",aLineRes);
                result.put("bLineRes",bLineRes);

                result.put("apparentPowMaxValue",totalMaxApparent.getApparentTotalMaxValue());
                result.put("apparentPowMaxTime",totalMaxApparent.getApparentTotalMaxTime());
                result.put("apparentPowMinValue",totalMinApparent.getApparentTotalMinValue());
                result.put("apparentPowMinTime",totalMinApparent.getApparentTotalMinTime());
                result.put("activePowMaxValue",totalMaxActive.getActiveTotalMaxValue());
                result.put("activePowMaxTime",totalMaxActive.getActiveTotalMaxTime());
                result.put("activePowMinValue",totalMinActive.getActiveTotalMinValue());
                result.put("activePowMinTime",totalMinActive.getActiveTotalMinTime());

                result.put("AapparentPowMaxValue",maxApparentA.getApparentAMaxValue());
                result.put("AapparentPowMaxTime",maxApparentA.getApparentAMaxTime());
                result.put("AapparentPowMinValue",minApparentA.getApparentAMinValue());
                result.put("AapparentPowMinTime",minApparentA.getApparentAMinTime());
                result.put("AactivePowMaxValue",maxActiveA.getActiveAMaxValue());
                result.put("AactivePowMaxTime",maxActiveA.getActiveAMaxTime());
                result.put("AactivePowMinValue",minActiveA.getActiveAMinValue());
                result.put("AactivePowMinTime",minActiveA.getActiveAMinTime());

                result.put("BapparentPowMaxValue",  maxApparentB.getApparentBMaxValue());
                result.put("BapparentPowMaxTime",   maxApparentB.getApparentBMaxTime());
                result.put("BapparentPowMinValue",  minApparentB.getApparentBMinValue());
                result.put("BapparentPowMinTime",   minApparentB.getApparentBMinTime());
                result.put("BactivePowMaxValue",    maxActiveB.getActiveBMaxValue());
                result.put("BactivePowMaxTime",     maxActiveB.getActiveBMaxTime());
                result.put("BactivePowMinValue",    minActiveB.getActiveBMinValue());
                result.put("BactivePowMinTime",     minActiveB.getActiveBMinTime());

            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public PageResult<CabinetEnvAndHumRes> getCabinetEnvPage(IndexPageReqVO pageReqVO) {

        PageResult<IndexDO> indexDOPageResult = cabIndexMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<IndexDO>()
                .inIfPresent(IndexDO::getId, pageReqVO.getCabinetIds()).eq(IndexDO::getPduBox,0));
        List<IndexDO> list = indexDOPageResult.getList();
        System.out.println(list);
        List<CabinetEnvAndHumRes> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(list)){
            return new PageResult<>(result, 0L);
        }
        List<TemColorDO> temColorList = temColorService.getTemColorAll();

        List<Integer> ids = list.stream().map(IndexDO::getId).collect(Collectors.toList());

        List<RoomIndex> roomIndices = roomIndexMapper.selectBatchIds(list.stream().map(IndexDO::getRoomId).collect(Collectors.toList()));
        Map<Integer, String> roomMap = roomIndices.stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getRoomName));
        Map<Integer, String>  aisleMap = aisleIndexMapper.selectBatchIds(list.stream()
//                .filter(dto -> dto.getAisleId() != 0)
                    .map(IndexDO::getAisleId).collect(Collectors.toList())).stream().collect(Collectors.toMap(AisleIndex::getId, AisleIndex::getAisleName));

        List<CabinetPdu> cabinetPdus = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>().in(CabinetPdu::getCabinetId, ids));

        List<String> pduA = cabinetPdus.stream()
                .filter(pdu -> !StringUtils.isEmpty(pdu.getPduIpA()))
                .map(pdu -> pdu.getPduIpA() + '-' + pdu.getCasIdA())
                .collect(Collectors.toList());

        List<String> pduB = cabinetPdus.stream()
                .filter(pdu -> !StringUtils.isEmpty(pdu.getPduIpB()))
                .map(pdu -> pdu.getPduIpB() + '-' + pdu.getCasIdB())
                .collect(Collectors.toList());

        Map<String, Integer> pduMap = null;
        if (!CollectionUtil.isEmpty(pduA) || !CollectionUtil.isEmpty(pduB) ){
            pduMap = pduIndexMapper.selectList(new LambdaQueryWrapperX<PduIndex>()
                    .inIfPresent(PduIndex::getDevKey, pduA)
                    .or(!CollectionUtil.isEmpty(pduA) && !CollectionUtil.isEmpty(pduB))
                    .in(!CollectionUtil.isEmpty(pduB),PduIndex::getDevKey, pduB)).stream().collect(Collectors.toMap(PduIndex::getDevKey, PduIndex::getId));
        }

        Map<Integer, CabinetPdu> cabinetPduMap = cabinetPdus.stream().collect(Collectors.toMap(CabinetPdu::getCabinetId, Function.identity()));
        List<CabinetEnvSensor> cabinetEnvSensors = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                .in(CabinetEnvSensor::getCabinetId, ids)
                .eq(CabinetEnvSensor::getType, 1));
        Map<Integer, List<CabinetEnvSensor>> cabinetEnvMap = cabinetEnvSensors.stream().collect(Collectors.groupingBy(cabinetEnvSensor -> cabinetEnvSensor.getCabinetId()));

        for (IndexDO indexDO : list) {
            CabinetEnvAndHumRes res = new CabinetEnvAndHumRes();
            result.add(res);
            String localtion = null;
            if(indexDO.getAisleId() != 0){
                localtion = roomMap.get(indexDO.getRoomId()) + "-" + aisleMap.get(indexDO.getAisleId()) + "-" + indexDO.getCabinetName();
            }else {
                localtion = roomMap.get(indexDO.getRoomId()) + "-"  + indexDO.getCabinetName() ;
            }
            res.setLocation(localtion);
            res.setId(indexDO.getId());
            if(pduMap == null){
                break;
            }
            CabinetPdu cabinetPdu = cabinetPduMap.get(indexDO.getId());
            if (cabinetPdu == null){
                continue;
            }
            List<CabinetEnvSensor> envList = cabinetEnvMap.get(indexDO.getId());
            if(envList == null || envList.size() == 0){
                continue;
            }
            for (CabinetEnvSensor cabinetEnvSensor : envList) {
                String devKey = null;
                if(cabinetEnvSensor.getPathPdu() == 'A'){
                    devKey = cabinetPdu.getPduIpA() + '-' + cabinetPdu.getCasIdA();
                }else{
                    devKey = cabinetPdu.getPduIpB() + '-' + cabinetPdu.getCasIdB();
                }

                Integer pduId = pduMap.get(devKey);
                if (pduId == null){
                    continue;
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                SearchRequest pduEnvRealtimeRequest = new SearchRequest("pdu_env_realtime");
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("pdu_id", pduId))
                        .must(QueryBuilders.termQuery("sensor_id", cabinetEnvSensor.getSensorId())));
                searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
                searchSourceBuilder.size(1); // 设置返回的最大结果数
                pduEnvRealtimeRequest.source(searchSourceBuilder);
                multiSearchRequest.add(pduEnvRealtimeRequest);

                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    SearchResponse response = multiSearchResponse.getResponses()[0].getResponse();
                    if (response != null) {
                        SearchHits hits = response.getHits();
                        if (hits.getTotalHits().value > 0) {
                            SearchHit hit = hits.getAt(0);
                            PduEnvRealtimeDo pduEnvRealtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEnvRealtimeDo.class);
                            if (cabinetEnvSensor.getChannel() == 1 && cabinetEnvSensor.getPosition() == 1) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setIceTopTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setIceTopTem(tem);
                                res.setIceTopHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 1 && cabinetEnvSensor.getPosition() == 2) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setIceMidTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setIceMidTem(tem);
                                res.setIceMidHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 1 && cabinetEnvSensor.getPosition() == 3) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setIceBomTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setIceBomTem(tem);
                                res.setIceBomHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 2 && cabinetEnvSensor.getPosition() == 1) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setHotTopTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setHotTopTem(tem);
                                res.setHotTopHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 2 && cabinetEnvSensor.getPosition() == 2) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setHotMidTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setHotMidTem(tem);
                                res.setHotMidHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 2 && cabinetEnvSensor.getPosition() == 3) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setHotBomTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setHotBomTem(tem);
                                res.setHotBomHum(hum);
                            }
                        }
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        return new PageResult<CabinetEnvAndHumRes>(result,indexDOPageResult.getTotal());
    }

    @Override
    public Map getCabinetEnvIceTemAndHumData(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase temResult = new CabinetChartResBase();
        CabinetChartResBase humResult = new CabinetChartResBase();
        result.put("temResult",temResult);
        result.put("humResult",humResult);
        result.put("temMaxValue",null);
        result.put("temMaxTime",null);
        result.put("temMaxSensorId",null);
        result.put("temMinValue",null);
        result.put("temMinTime",null);
        result.put("temMinSensorId",null);
        try {
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id),false);
            if (cabinetPdu == null){
                return result;
            }
            List<String> devKeyList = new ArrayList<>();
            Map<String,String> pduMap = new HashMap<>();
            pduMap.put("A",cabinetPdu.getPduIpA() + '-' + cabinetPdu.getCasIdA());
            pduMap.put("B",cabinetPdu.getPduIpB() + '-' + cabinetPdu.getCasIdB());
            if (!StringUtils.isEmpty(cabinetPdu.getPduIpA())){
                devKeyList.add(cabinetPdu.getPduIpA() + '-' + cabinetPdu.getCasIdA());
            }
            if (!StringUtils.isEmpty(cabinetPdu.getPduIpB())){
                devKeyList.add(cabinetPdu.getPduIpB() + '-' + cabinetPdu.getCasIdB());
            }
            if(CollectionUtil.isEmpty(devKeyList)){
                return result;
            }
            List<PduIndex> pduIndices = pduIndexMapper.selectList(new LambdaQueryWrapperX<PduIndex>().in(PduIndex::getDevKey, devKeyList));
            Map<String, Integer> pduIdMap = pduIndices.stream().collect(Collectors.toMap(PduIndex::getDevKey, PduIndex::getId));
            String whichIndex = "pdu_env_hour";
            LocalDateTime now = LocalDateTime.now();
            if (timeType == 2){
                whichIndex = "pdu_env_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }else{
                oldTime = now.minusHours(25);
                newTime = now;
            }
            List<CabinetEnvSensor> cabinetEnvSensors = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 1)
                    .eq(CabinetEnvSensor::getType, 1)
                    .orderByAsc(CabinetEnvSensor::getPosition));
            List<Integer> searchIds = cabinetEnvSensors.stream().filter(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu()))) != null ).map(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu())))).collect(Collectors.toList());
            List<Integer> sensorIds = cabinetEnvSensors.stream().map(CabinetEnvSensor::getSensorId).collect(Collectors.toList());
            List<String> data = getData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex);
            if (CollectionUtil.isEmpty(data)){
                return result;
            }
            Map<Integer, Map<Integer, List<PduEnvHourDo>>> pduEnvHourDoMap = data.stream()
                    .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                    .collect(Collectors.groupingBy( PduEnvHourDo::getPduId, Collectors.groupingBy(PduEnvHourDo::getSensorId) ));
            List<String> time = null;
            boolean isFisrt = false;
            for (CabinetEnvSensor cabinetEnvSensor : cabinetEnvSensors) {
                int position = cabinetEnvSensor.getPosition();
                if (pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))) == null){
                    continue;
                }
                List<PduEnvHourDo> pduEnvHourDo = pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))).get(cabinetEnvSensor.getSensorId());
                List<Float> temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                List<Float> humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumAvgValue).map(Float::valueOf).collect(Collectors.toList());
                LineSeries temLineSeries = new LineSeries();
                LineSeries humLineSeries = new LineSeries();
                String temName = null;
                String humName = null;
                if (position == 1){
                    temName = "上层温度传感器";
                    humName = "上层湿度传感器";
                } else if(position == 2){
                    temName = "中层温度传感器";
                    humName = "中层湿度传感器";
                } else if (position == 3) {
                    temName = "下层温度传感器";
                    humName = "下层湿度传感器";
                }
                temLineSeries.setName(temName);
                temLineSeries.setData(temList);
                humLineSeries.setName(humName);
                humLineSeries.setData(humList);
                if(!isFisrt){
                    if(timeType == 2){

                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                    }else{
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                    }
                    temResult.setTime(time);
                    humResult.setTime(time);
                    isFisrt = true;
                }
                temResult.getSeries().add(temLineSeries);
                humResult.getSeries().add(humLineSeries);
            }
            String temMaxValue = getPDUMaxData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "tem_max_value");
            PduEnvHourDo temMax = JsonUtils.parseObject(temMaxValue, PduEnvHourDo.class);
            String temMinValue = getPDUMinData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "tem_min_value");
            PduEnvHourDo temMin = JsonUtils.parseObject(temMinValue, PduEnvHourDo.class);
            if(temMax != null){
                result.put("temMaxValue",temMax.getTemMaxValue());
                result.put("temMaxTime",temMax.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("temMaxSensorId",temMax.getSensorId());
            }
            if(temMin != null){
                result.put("temMinValue", temMin.getTemMinValue());
                result.put("temMinTime",temMin.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("temMinSensorId",temMin.getSensorId());
            }
            return result;
        } catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getCabinetEnvHotTemAndHumData(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase temResult = new CabinetChartResBase();
        CabinetChartResBase humResult = new CabinetChartResBase();
        result.put("temResult",temResult);
        result.put("humResult",humResult);
        result.put("temResult",temResult);
        result.put("humResult",humResult);
        result.put("temMaxValue",null);
        result.put("temMaxTime",null);
        result.put("temMaxSensorId",null);
        result.put("temMinValue",null);
        result.put("temMinTime",null);
        result.put("temMinSensorId",null);
        try {
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id),false);
            if (cabinetPdu == null){
                return result;
            }
            List<String> devKeyList = new ArrayList<>();
            Map<String,String> pduMap = new HashMap<>();
            pduMap.put("A",cabinetPdu.getPduIpA() + '-' + cabinetPdu.getCasIdA());
            pduMap.put("B",cabinetPdu.getPduIpB() + '-' + cabinetPdu.getCasIdB());
            if (!StringUtils.isEmpty(cabinetPdu.getPduIpA())){
                devKeyList.add(cabinetPdu.getPduIpA() + '-' + cabinetPdu.getCasIdA());
            }
            if (!StringUtils.isEmpty(cabinetPdu.getPduIpB())){
                devKeyList.add(cabinetPdu.getPduIpB() + '-' + cabinetPdu.getCasIdB());
            }
            if(CollectionUtil.isEmpty(devKeyList)){
                return result;
            }
            List<PduIndex> pduIndices = pduIndexMapper.selectList(new LambdaQueryWrapperX<PduIndex>().in(PduIndex::getDevKey, devKeyList));
            Map<String, Integer> pduIdMap = pduIndices.stream().collect(Collectors.toMap(PduIndex::getDevKey, PduIndex::getId));
            String whichIndex = "pdu_env_hour";
            LocalDateTime now = LocalDateTime.now();
            if (timeType == 2){
                whichIndex = "pdu_env_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }else{
                oldTime = now.minusHours(25);
                newTime = now;
            }
            List<CabinetEnvSensor> cabinetEnvSensors = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 2)
                    .eq(CabinetEnvSensor::getType, 1)
                    .orderByAsc(CabinetEnvSensor::getPosition));
            List<Integer> searchIds = cabinetEnvSensors.stream().filter(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu()))) != null ).map(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu())))).collect(Collectors.toList());
            List<Integer> sensorIds = cabinetEnvSensors.stream().map(CabinetEnvSensor::getSensorId).collect(Collectors.toList());
            List<String> data = getData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex);
            if (CollectionUtil.isEmpty(data)){
                return result;
            }
            Map<Integer, Map<Integer, List<PduEnvHourDo>>> pduEnvHourDoMap = data.stream()
                    .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                    .collect(Collectors.groupingBy( PduEnvHourDo::getPduId, Collectors.groupingBy(PduEnvHourDo::getSensorId) ));
            List<String> time = null;
            boolean isFisrt = false;
            for (CabinetEnvSensor cabinetEnvSensor : cabinetEnvSensors) {
                int position = cabinetEnvSensor.getPosition();
                Integer i = pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())));
                if (pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))) == null){
                    continue;
                }
                List<PduEnvHourDo> pduEnvHourDo = pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))).get(cabinetEnvSensor.getSensorId());
                List<Float> temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                List<Float> humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumAvgValue).map(Float::valueOf).collect(Collectors.toList());
                LineSeries temLineSeries = new LineSeries();
                LineSeries humLineSeries = new LineSeries();
                String temName = null;
                String humName = null;
                if (position == 1){
                    temName = "上层温度传感器";
                    humName = "上层湿度传感器";
                } else if(position == 2){
                    temName = "中层温度传感器";
                    humName = "中层湿度传感器";
                } else if (position == 3) {
                    temName = "下层温度传感器";
                    humName = "下层湿度传感器";
                }
                temLineSeries.setName(temName);
                temLineSeries.setData(temList);
                humLineSeries.setName(humName);
                humLineSeries.setData(humList);
                if(!isFisrt){
                    if(timeType == 2){
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                    }else{
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                    }
                    temResult.setTime(time);
                    humResult.setTime(time);
                    isFisrt = true;
                }
                temResult.getSeries().add(temLineSeries);
                humResult.getSeries().add(humLineSeries);
            }
            String temMaxValue = getPDUMaxData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "tem_max_value");
            PduEnvHourDo temMax = JsonUtils.parseObject(temMaxValue, PduEnvHourDo.class);
            String temMinValue = getPDUMinData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "tem_min_value");
            PduEnvHourDo temMin = JsonUtils.parseObject(temMinValue, PduEnvHourDo.class);
            if(temMax != null){
                result.put("temMaxValue",temMax.getTemMaxValue());
                result.put("temMaxTime",temMax.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("temMaxSensorId",temMax.getSensorId());
            }
            if(temMin != null){
                result.put("temMinValue", temMin.getTemMinValue());
                result.put("temMinTime",temMin.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("temMinSensorId",temMin.getSensorId());
            }

            return result;
        } catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getCabinetPFLine(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        result.put("pfLineRes",totalLineRes);
        try {

            String index = null;

            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                index = "cabinet_hda_pow_hour";
                if (oldTime.equals(newTime)) {
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }

            } else {
                index = "cabinet_hda_pow_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }

            String startTime = localDateTimeToString(oldTime);
            String endTime = localDateTimeToString(newTime);
            List<String> data = getCabinetData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
            List<CabinetPowHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, CabinetPowHourDo.class)).collect(Collectors.toList());

            LineSeries totalPFLine = new LineSeries();
            totalPFLine.setName("总平均功率因素");
            LineSeries PFLineA = new LineSeries();
            PFLineA.setName("A路功率因素");
            LineSeries PFLineB = new LineSeries();
            PFLineB.setName("B路功率因素");

            totalLineRes.getSeries().add(totalPFLine);
            totalLineRes.getSeries().add(PFLineA);
            totalLineRes.getSeries().add(PFLineB);

            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getFactorTotalAvgValue());
                    PFLineA.getData().add(hourdo.getFactorAAvgValue());
                    PFLineB.getData().add(hourdo.getFactorBAvgValue());
                    DateTime dateTime = new DateTime(hourdo.getCreateTime());
                    totalLineRes.getTime().add(dateTime.toString("HH:mm"));

                });
            }else{
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getFactorTotalAvgValue());
                    PFLineA.getData().add(hourdo.getFactorAAvgValue());
                    PFLineB.getData().add(hourdo.getFactorBAvgValue());
                    DateTime dateTime = new DateTime(hourdo.getCreateTime());
                    totalLineRes.getTime().add(dateTime.toString("yyyy-MM-dd"));
                });
            }
            result.put("pfLineRes",totalLineRes);

        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public List<Integer> idList() {
        return cabIndexMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(IndexDO::getId).collect(Collectors.toList());
    }

    /**
     * 获取数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ids 机柜id列表
     * @param index 索引表
     */
    private List<String> getData(String startTime, String endTime, List<Integer> ids, List<Integer> sensorIds, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))
                .must(QueryBuilders.termsQuery("sensor_id", sensorIds))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(2000);

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

    private List<String> getCabinetData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("cabinet_id", ids))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(2000);

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

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index,String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("cabinet_id", ids))));
        builder.sort(order, SortOrder.DESC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                return str;
            }
        }
        return null;
    }

    private String getPDUMaxData(String startTime, String endTime, List<Integer> ids, List<Integer> sensorIds,String index,String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))
                .must(QueryBuilders.termsQuery("sensor_id", sensorIds))));
        builder.sort(order, SortOrder.DESC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                return str;
            }
        }
        return null;
    }

    private String getMinData(String startTime, String endTime, List<Integer> ids, String index,String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("cabinet_id", ids))));
        builder.sort(order, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                return str;
            }
        }
        return null;

    }

    private String getPDUMinData(String startTime, String endTime, List<Integer> ids, List<Integer> sensorIds,String index,String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))
                .must(QueryBuilders.termsQuery("sensor_id", sensorIds))));
        builder.sort(order, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                return str;
            }
        }
        return null;
    }
    private String localDateTimeToString(LocalDateTime time){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
    }


}