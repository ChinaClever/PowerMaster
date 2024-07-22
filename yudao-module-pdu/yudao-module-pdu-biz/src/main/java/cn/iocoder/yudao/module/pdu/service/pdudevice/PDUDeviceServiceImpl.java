package cn.iocoder.yudao.module.pdu.service.pdudevice;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;


import cn.iocoder.yudao.framework.common.entity.es.cabinet.env.CabinetEnvHourDo;

import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEqTotalDayDo;

import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineHourDo;

import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.BarSeries;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.CabinetChartResBase;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.LineSeries;
import cn.iocoder.yudao.framework.common.mapper.CabinetPduMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.MaxValueAndCreateTime;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDULineRes;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.PDUCurbalanceColorDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.curbalancecolor.PDUCurbalanceColorMapper;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.search.aggregations.metrics.*;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;

import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndex;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;

import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.framework.common.pojo.PageResult;


import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndexMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;


/**
 * PDU设备 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class PDUDeviceServiceImpl implements PDUDeviceService {

    @Resource
    private PduIndexMapper pDUDeviceMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private CabinetPduMapper cabinetPduMapper;

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;

    @Autowired
    private AisleIndexMapper aisleIndexMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Resource
    private PDUCurbalanceColorMapper PDUCurbalanceColorMapper;

    @Override
    public PageResult<PDUDeviceDO> getPDUDevicePage(PDUDevicePageReqVO pageReqVO) {

        PageResult<PduIndex> pduIndexPageResult = null;
        List<PDUDeviceDO> result = new ArrayList<>();
        PDUCurbalanceColorDO PDUCurbalanceColorDO = PDUCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        if(pageReqVO.getCabinetIds() != null && !pageReqVO.getCabinetIds().isEmpty()) {
            List<String> devKeyList = new ArrayList<>();

            List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>().inIfPresent(CabinetPdu::getCabinetId, pageReqVO.getCabinetIds()));
            if(cabinetPduList != null && cabinetPduList.size() > 0){
                for (CabinetPdu cabinetPdu : cabinetPduList) {
                    if (!StringUtils.isEmpty(cabinetPdu.getPduIpA()) && cabinetPdu.getCasIdA() >= 0){
                        devKeyList.add(cabinetPdu.getPduIpA() + '-' +cabinetPdu.getCasIdA());
                    }
                    if (!StringUtils.isEmpty(cabinetPdu.getPduIpB()) && cabinetPdu.getCasIdB() >= 0){
                        devKeyList.add(cabinetPdu.getPduIpB() + '-' +cabinetPdu.getCasIdB());
                    }
                }
            }else{
                return new PageResult<PDUDeviceDO>(result,0L);
            }
            pduIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PduIndex>()
                    .likeIfPresent(PduIndex::getDevKey,pageReqVO.getDevKey()).inIfPresent(PduIndex::getDevKey,devKeyList).inIfPresent(PduIndex::getRunStatus,pageReqVO.getStatus()));
        }else{
            pduIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PduIndex>()
                    .likeIfPresent(PduIndex::getDevKey,pageReqVO.getDevKey()).inIfPresent(PduIndex::getRunStatus,pageReqVO.getStatus()));
        }

        List<PduIndex> pduIndices = pduIndexPageResult.getList();
        List redisList = getMutiRedis(pduIndices);

        for (PduIndex pduIndex : pduIndices) {
            PDUDeviceDO pduDeviceDO = new PDUDeviceDO();
            pduDeviceDO.setStatus(pduIndex.getRunStatus());
            pduDeviceDO.setId(pduIndex.getId());
            pduDeviceDO.setDevKey(pduIndex.getDevKey());
            result.add(pduDeviceDO);
        }
        Map<String, PDUDeviceDO> resMap = result.stream().collect(Collectors.toMap(PDUDeviceDO::getDevKey, Function.identity()));
        setLocation(pduIndices,result);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));

            String devKey = jsonObject.getString("dev_ip") + "-" + jsonObject.getString("addr");
            PDUDeviceDO pduDeviceDO = resMap.get(devKey);

            if (jsonObject == null || jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list") == null || jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").size() <= 0){
                continue;
            }
            JSONObject pduTgData = jsonObject.getJSONObject("pdu_data").getJSONObject("pdu_total_data");
            JSONArray curArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("cur_value");
            JSONArray volArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("vol_value");
            JSONArray curAlarmArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("cur_alarm_max");
            curAlarmArr.sort(Collections.reverseOrder());
            double maxVal = curAlarmArr.getDouble(0);
            List<Double> temp = curArr.toList(Double.class);
            Double curUnbalance = null;
            Double bcur = null;
            Double ccur = null;
            temp.sort(Collections.reverseOrder());
            int color = 0;
            if(temp.size() >= 2){
                double a = temp.get(0) - temp.get(2);
                curUnbalance = pduTgData.getDoubleValue("cur_unbalance");
                bcur = curArr.getDoubleValue(1);
                ccur = curArr.getDoubleValue(2);
                if (PDUCurbalanceColorDO == null) {
                    if (a >= maxVal * 0.2) {
                        if (curUnbalance < 15) {
                            color = 2;
                        } else if (curUnbalance < 30) {
                            color = 3;
                        } else {
                            color = 4;
                        }
                    } else {
                        color = 1;
                    }
                } else {
                    if (a >= maxVal * 0.2) {
                        if (curUnbalance < PDUCurbalanceColorDO.getRangeOne()) {
                            color = 2;
                        } else if (curUnbalance < PDUCurbalanceColorDO.getRangeFour()) {
                            color = 3;
                        } else {
                            color = 4;
                        }
                    } else {
                        color = 1;
                    }
                }
            }
            if(pageReqVO.getColor() != null){
                if(!pageReqVO.getColor().contains(color)){
                    continue;
                }
            }
            pduDeviceDO.setPf(pduTgData.getDoubleValue("power_factor"));

            pduDeviceDO.setEle(pduTgData.getDoubleValue("ele_active"));
            pduDeviceDO.setPow(pduTgData.getDoubleValue("pow_active"));
            pduDeviceDO.setApparentPow(pduTgData.getDoubleValue("pow_apparent"));
            pduDeviceDO.setReactivePow(pduTgData.getDoubleValue("pow_reactive"));
            pduDeviceDO.setDataUpdateTime(jsonObject.getString("sys_time"));
            pduDeviceDO.setPduAlarm(jsonObject.getString("pdu_alarm"));
            pduDeviceDO.setAcur(curArr.getDoubleValue(0));
            pduDeviceDO.setBcur(bcur);
            pduDeviceDO.setCcur(ccur);
            pduDeviceDO.setCurUnbalance(curUnbalance);
            pduDeviceDO.setAvol(volArr.getDoubleValue(0));
            pduDeviceDO.setBvol(volArr.size() > 1 ? volArr.getDoubleValue(1) : null);
            pduDeviceDO.setCvol(volArr.size() > 2 ? volArr.getDoubleValue(2) : null);
            pduDeviceDO.setVolUnbalance(pduTgData.getDouble("vol_unbalance") != null ? pduTgData.getDouble("vol_unbalance") : null);
            pduDeviceDO.setColor(color);

        }
        return new PageResult<PDUDeviceDO>(result,pduIndexPageResult.getTotal());
    }

    @Override
    public PageResult<PDULineRes> getPDULineDevicePage(PDUDevicePageReqVO pageReqVO) {
        try {
            List<PduIndex> pduIndices = null;
            List<PDULineRes> result = new ArrayList<>();
            if(pageReqVO.getCabinetIds() != null && !pageReqVO.getCabinetIds().isEmpty()) {
                List<String> devKeyList = new ArrayList<>();
                List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>().inIfPresent(CabinetPdu::getCabinetId, pageReqVO.getCabinetIds()));
                if(cabinetPduList != null && !cabinetPduList.isEmpty()){
                    for (CabinetPdu cabinetPdu : cabinetPduList) {
                        if (!StringUtils.isEmpty(cabinetPdu.getPduIpA())){
                            devKeyList.add(cabinetPdu.getPduIpA() + "-" + cabinetPdu.getCasIdA());
                        }
                        if (!StringUtils.isEmpty(cabinetPdu.getPduIpB())){
                            devKeyList.add(cabinetPdu.getPduIpB() + "-" + cabinetPdu.getCasIdB());
                        }
                    }
                }else{
                    return new PageResult<>(result, 0L);
                }
                pduIndices = pDUDeviceMapper.selectList(new LambdaQueryWrapperX<PduIndex>()
                        .likeIfPresent(PduIndex::getDevKey,pageReqVO.getDevKey()).inIfPresent(PduIndex::getDevKey,devKeyList));
            }
            if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            }
            Map<Integer,Map<Integer, MaxValueAndCreateTime>> curMap ;
            Map<Integer,Map<Integer, MaxValueAndCreateTime>> powMap ;
            String index = null;
            if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "pdu_hda_line_hour";
            } else {
                index = "pdu_hda_line_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            Map esTotalPduId = null;
            if(pduIndices != null){
                esTotalPduId = getESTotalPduId(index, startTime, endTime,pageReqVO.getPageSize(), pageReqVO.getPageNo() - 1,pduIndices.stream().map(PduIndex::getId).collect(Collectors.toList()));
            } else {
                esTotalPduId = getESTotalPduId(index, startTime, endTime,pageReqVO.getPageSize(), pageReqVO.getPageNo() - 1);
            }
            Long total = (Long)esTotalPduId.get("total");
            if(total == 0){
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            List<Integer> pduIds = (List<Integer>) esTotalPduId.get("ids");

            List<PduIndex> pdus = pDUDeviceMapper.selectBatchIds(pduIds);
            curMap = getPDULineCurMaxData(startTime,endTime,pduIds,index);
            powMap = getPDULinePowMaxData(startTime,endTime,pduIds,index);

            for (PduIndex pduIndex : pdus) {
                Integer id = pduIndex.getId().intValue();
                if (curMap.get(id) == null){
                    continue;
                }
                PDULineRes pduLineRes = new PDULineRes();
                pduLineRes.setStatus(pduIndex.getRunStatus());
                pduLineRes.setPduId(pduIndex.getId());
                pduLineRes.setDevKey(pduIndex.getDevKey());

                MaxValueAndCreateTime curl1 = curMap.get(id).get(1);
                pduLineRes.setL1MaxCur(curl1.getMaxValue().floatValue());
                pduLineRes.setL1MaxCurTime(curl1.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                MaxValueAndCreateTime curl2 = curMap.get(id).get(2);
                if(curl2 != null){
                    pduLineRes.setL2MaxCur(curl2.getMaxValue().floatValue());
                    pduLineRes.setL2MaxCurTime(curl2.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                MaxValueAndCreateTime curl3 = curMap.get(id).get(3);
                if(curl3 != null){
                    pduLineRes.setL3MaxCur(curl3.getMaxValue().floatValue());
                    pduLineRes.setL3MaxCurTime(curl3.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                MaxValueAndCreateTime powl1 = powMap.get(id).get(1);
                pduLineRes.setL1MaxPow(powl1.getMaxValue().floatValue());
                pduLineRes.setL1MaxPowTime(powl1.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                MaxValueAndCreateTime powl2 = powMap.get(id).get(2);
                if(powl2 != null) {
                    pduLineRes.setL2MaxPow(powl2.getMaxValue().floatValue());
                    pduLineRes.setL2MaxPowTime(powl2.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                MaxValueAndCreateTime powl3 = powMap.get(id).get(3);
                if(powl3 != null) {
                    pduLineRes.setL3MaxPow(powl3.getMaxValue().floatValue());
                    pduLineRes.setL3MaxPowTime(powl3.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                result.add(pduLineRes);
            }

            setLocation(pdus,result);
            return new PageResult<PDULineRes>(result,total);

        }catch (Exception e){
            log.error("获取数据失败",e);
        }


        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public List<String> getDevKeyList() {
        List<String> result = pDUDeviceMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(PduIndex::getDevKey).collect(Collectors.toList());
        return result;
    }

    @Override
    public List<String> getIpList() {
        List<String> result = pDUDeviceMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(PduIndex::getIpAddr).collect(Collectors.toList());
        return result;
    }



    @Override
    public String getDisplayDataByDevKey(String devKey) {
        if (StringUtils.isEmpty(devKey)){
            return null;
        }else {
            ValueOperations ops = redisTemplate.opsForValue();
            JSONObject jsonObject = (JSONObject) ops.get("packet:pdu:" + devKey);
            return jsonObject != null ? jsonObject.toJSONString() : null;
        }
    }

    @Override
    public Map getHistoryDataByDevKey(String devKey,String type) {
        HashMap result = new HashMap<>();

        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        if(pduIndex != null){
            Long id = pduIndex.getId();
            // 构建查询请求
            SearchRequest searchRequest = null;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime pastTime = null;
            if("oneHour".equals(type)){
                pastTime = now.minusHours(1);
                pastTime = pastTime.minusMinutes(1);
                searchRequest = new SearchRequest("pdu_hda_total_realtime");
            } else if("twentyfourHour".equals(type)){
                pastTime = now.minusHours(25);
                searchRequest = new SearchRequest("pdu_hda_total_hour");
            }

            // 构建查询请求
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", id));
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(formatter.format(pastTime))
                    .to(formatter.format(now)));
            searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            searchSourceBuilder.size(1000); // 设置返回的最大结果数

            searchRequest.source(searchSourceBuilder);
            List<Double> apparentList = new ArrayList<>();
            List<Double> activeList = new ArrayList<>();
            List<String> dateTimes = new ArrayList<>();
            // 执行查询请求
            try {
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                if(searchResponse != null){
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit hit : hits) {
                        String str = hit.getSourceAsString();
                        if("oneHour".equals(type)){
                            PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                            apparentList.add(Double.valueOf(pduHdaTotalRealtimeDo.getApparentPow()));
                            activeList.add(Double.valueOf(pduHdaTotalRealtimeDo.getActivePow()));
                            dateTimes.add(pduHdaTotalRealtimeDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
                        } else if("twentyfourHour".equals(type)){
                            PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaTotalHourDo.class);
                            apparentList.add(Double.valueOf(pduHdaTotalHourDo.getApparentPowAvgValue()));
                            activeList.add(Double.valueOf(pduHdaTotalHourDo.getActivePowAvgValue()));
                            dateTimes.add(pduHdaTotalHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            result.put("apparentList",apparentList);
            result.put("activeList",activeList);
            result.put("dateTimes",dateTimes);

            return result;
        } else{
            return result;
        }
    }

    @Override
    public Map getChartNewDataByPduDevKey(String devKey,LocalDateTime oldTime,String type) {
        HashMap result = new HashMap<>();

        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        if(pduIndex != null){
            Long id = pduIndex.getId();
            // 构建查询请求
            SearchRequest searchRequest = null;
            LocalDateTime newTime = null;
            if("oneHour".equals(type)){
                newTime = oldTime.plusMinutes(1);
                newTime = newTime.plusSeconds(20);
                // 构建查询请求
                searchRequest = new SearchRequest("pdu_hda_total_realtime");
            } else if("twentyfourHour".equals(type)){
                newTime = oldTime.plusHours(1);
                newTime = newTime.plusMinutes(20);
                // 构建查询请求
                searchRequest = new SearchRequest("pdu_hda_total_hour");
            }

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", id));
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(formatter.format(oldTime))
                    .to(formatter.format(newTime)));
            searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
            searchSourceBuilder.size(1); // 设置返回的最大结果数
            searchRequest.source(searchSourceBuilder);

            double apparent = 0;
            double active = 0;
            String dateTime = "";
            try {
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                if(searchResponse != null){
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit hit : hits) {
                        String str = hit.getSourceAsString();
                        if("oneHour".equals(type)){
                            PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                            apparent = pduHdaTotalRealtimeDo.getApparentPow();
                            active = pduHdaTotalRealtimeDo.getActivePow();
                            dateTime = pduHdaTotalRealtimeDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        } else if("twentyfourHour".equals(type)){
                            PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaTotalHourDo.class);
                            apparent = pduHdaTotalHourDo.getApparentPowAvgValue();
                            active = pduHdaTotalHourDo.getActivePowAvgValue();
                            dateTime = pduHdaTotalHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            result.put("apparent",apparent);
            result.put("active",active);
            result.put("dateTime",dateTime);

            return result;
        } else {
            return result;
        }

    }

    @Override
    public Map getReportConsumeDataByDevKey(String devKey,Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase barRes = new CabinetChartResBase();
        BarSeries barSeries = new BarSeries();
        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
            if(pduIndex != null) {
                String index = null;
                boolean isSameDay = false;
                Long Id = pduIndex.getId();
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "pdu_eq_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Id.intValue()), index);
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay){
                    for (String str : cabinetData) {
                        nowTimes++;
                        PduEleTotalRealtimeDo eleDO = JsonUtils.parseObject(str, PduEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = eleDO.getEle();
                        }
                        if (nowTimes > 1){
                            barSeries.getData().add((float)(eleDO.getEle() -lastEq));
                            barRes.getTime().add(eleDO.getCreateTime().toString("HH:mm"));
                        }
                        lastEq = eleDO.getEle();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "ele_active");
                    PduEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, PduEleTotalRealtimeDo.class);
                    if(eleMaxValue != null){
                        maxEle = eleMaxValue.getEle();
                        maxEleTime = eleMaxValue.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
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
                        PduEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, PduEqTotalDayDo.class);
                        totalEq += totalDayDo.getEq();
                        barSeries.getData().add((float)totalDayDo.getEq());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "eq_value");
                    PduEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, PduEqTotalDayDo.class);
                    if(eqMaxValue != null){
                        maxEle = eqMaxValue.getEq();
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
    public Map getPDUPFLine(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        result.put("pfLineRes",totalLineRes);
        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));

            if(pduIndex != null) {
                String index = null;
                Long Id = pduIndex.getId();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_hda_total_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "pdu_hda_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }

                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index);
                List<PduHdaTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, PduHdaTotalHourDo.class)).collect(Collectors.toList());

                LineSeries totalPFLine = new LineSeries();
                totalPFLine.setName("总平均功率因素");

                totalLineRes.getSeries().add(totalPFLine);

                if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                    powList.forEach(hourdo -> {
                        totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());

                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm"));

                    });
                }else{
                    powList.forEach(hourdo -> {
                        totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));
                    });
                }
                result.put("pfLineRes",totalLineRes);
            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        result.put("totalLineRes",totalLineRes);

        result.put("apparentPowMaxValue",null);
        result.put("apparentPowMaxTime",null);
        result.put("apparentPowMinValue",null);
        result.put("apparentPowMinTime",null);
        result.put("activePowMaxValue", null);
        result.put("activePowMaxTime",  null);
        result.put("activePowMinValue", null);
        result.put("activePowMinTime",  null);
        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));

            if(pduIndex != null) {
                String index = null;
                Long Id = pduIndex.getId();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_hda_total_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "pdu_hda_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }

                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index);
                List<PduHdaTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, PduHdaTotalHourDo.class)).collect(Collectors.toList());

                LineSeries totalApparentPow = new LineSeries();
                totalApparentPow.setName("总平均视在功率");
                LineSeries totalActivePow = new LineSeries();
                totalActivePow.setName("总平均有功功率");
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);


                if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentPowAvgValue());
                        totalActivePow.getData().add(hourdo.getActivePowAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm"));

                    });
                }else{
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentPowAvgValue());
                        totalActivePow.getData().add(hourdo.getActivePowAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));

                    });
                }

                String apparentTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "pow_apparent_max_value");
                PduHdaTotalHourDo totalMaxApparent = JsonUtils.parseObject(apparentTotalMaxValue, PduHdaTotalHourDo.class);
                String apparentTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "pow_apparent_min_value");
                PduHdaTotalHourDo totalMinApparent = JsonUtils.parseObject(apparentTotalMinValue, PduHdaTotalHourDo.class);

                String activeTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "pow_active_max_value");
                PduHdaTotalHourDo totalMaxActive = JsonUtils.parseObject(activeTotalMaxValue, PduHdaTotalHourDo.class);
                String activeTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "pow_active_min_value");
                PduHdaTotalHourDo totalMinActive = JsonUtils.parseObject(activeTotalMinValue, PduHdaTotalHourDo.class);

                result.put("totalLineRes",totalLineRes);

                result.put("apparentPowMaxValue",totalMaxApparent.getApparentPowMaxValue());
                result.put("apparentPowMaxTime",totalMaxApparent.getApparentPowMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("apparentPowMinValue",totalMinApparent.getApparentPowMinValue());
                result.put("apparentPowMinTime",totalMinApparent.getApparentPowMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMaxValue",totalMaxActive.getActivePowMaxValue());
                result.put("activePowMaxTime",totalMaxActive.getActivePowMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMinValue",totalMinActive.getActivePowMinValue());
                result.put("activePowMinTime",totalMinActive.getActivePowMinTime().toString("yyyy-MM-dd HH:mm:ss"));

            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getReportOutLetDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase cabinetChartResBase = new CabinetChartResBase();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        if(pduIndex != null){
            Long pduId = pduIndex.getId();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                if(oldTime.equals(newTime)){
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                HashMap<String, String> scriptMap = new HashMap<>();
                scriptMap.put("maxEleActive","max_ele_active.value");
                scriptMap.put("minEleActive","min_ele_active.value");

                HashMap<String, String> selectMap = new HashMap<>();
                selectMap.put("key","eleValue.value");

                // 创建时间分布搜索请求
                SearchRequest pduOutLetTotalRealRequest = new SearchRequest("pdu_ele_outlet");
                SearchSourceBuilder pduOutLetTotalRealSourceBuilder = new SearchSourceBuilder();

                pduOutLetTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));
                //映射sum的sum_ele_active为sumEle

                pduOutLetTotalRealSourceBuilder.aggregation(
                        AggregationBuilders.terms("group_by_outlet").field("outlet_id").size(1000)
                                .subAggregation(AggregationBuilders.min("min_ele_active").field("ele_active"))
                                .subAggregation(AggregationBuilders.max("max_ele_active").field("ele_active"))
                                .subAggregation(PipelineAggregatorBuilders.bucketScript("eleValue",scriptMap,new Script("params.maxEleActive - params.minEleActive")))
                                .subAggregation(PipelineAggregatorBuilders.bucketSelector("eleValue_range",selectMap,new Script("params.key > 0"))));

                pduOutLetTotalRealSourceBuilder.size(0); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduOutLetTotalRealRequest.source(pduOutLetTotalRealSourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduOutLetTotalRealRequest);

                List<Float> eleValue = new ArrayList<>();
                List<String> outLetId = new ArrayList<>();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(pduPowTotalRealDisResponse != null){
                        for ( Terms.Bucket bucket : ((ParsedLongTerms) pduPowTotalRealDisResponse.getAggregations().get("group_by_outlet")).getBuckets()) {
                            String outlet = "输出位" + bucket.getKeyAsString();
                            ParsedSimpleValue eleValue1 = bucket.getAggregations().get("eleValue");

                            eleValue.add((float)eleValue1.value());
                            outLetId.add(outlet);
                        }
                    }

                    // 创建一个自定义的Comparator，用于eleValue的降序排序
                    Comparator<Integer> comparator = Comparator.comparingDouble(eleValue::get);

                    // 创建一个索引列表，用于保存eleValue的原始索引位置
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < eleValue.size(); i++) {
                        indexes.add(i);
                    }

                    // 使用Collections.sort()方法对indexes进行排序，并根据indexes的排序结果更新eleValue和outLetId列表
                    Collections.sort(indexes, comparator);
                    List<Float> sortedEleValue = new ArrayList<>();
                    List<String> sortedOutLetId = new ArrayList<>();
                    for (int i = 0; i < indexes.size(); i++) {
                        int index = indexes.get(i);
                        sortedEleValue.add(eleValue.get(index));
                        sortedOutLetId.add(outLetId.get(index));
                    }
                    BarSeries barSeries = new BarSeries();
                    barSeries.setLabel("{ show: true, position: 'right' }");
                    barSeries.setData(sortedEleValue);
                    cabinetChartResBase.setTime(sortedOutLetId);
                    cabinetChartResBase.getSeries().add(barSeries);
                    result.put("eleValue",sortedEleValue);
                    result.put("outLetId",sortedOutLetId);
                    result.put("barRes",cabinetChartResBase);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest pduOutLetTotalDayRequest = new SearchRequest("pdu_eq_outlet_day");
                SearchSourceBuilder pduOutLetTotalDaySourceBuilder = new SearchSourceBuilder();

                pduOutLetTotalDaySourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));

                //映射sum的sum_ele_active为sumEle
                Map<String, String> map = new HashMap<>();
                map.put("sumEq", "sum_eq");
                pduOutLetTotalDaySourceBuilder.aggregation(AggregationBuilders
                        .terms("by_outlet_id").field("outlet_id").order(BucketOrder.aggregation("sum_eq",true)).size(24)
                        .subAggregation(AggregationBuilders.sum("sum_eq").field("eq_value"))
                        //筛选sumEle > 0的
                        .subAggregation(PipelineAggregatorBuilders.bucketSelector("positive_sum_eq",map, new Script("params.sumEq > 0"))));

                pduOutLetTotalDaySourceBuilder.size(0); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduOutLetTotalDayRequest.source(pduOutLetTotalDaySourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduOutLetTotalDayRequest);

                List<Float> eqValue = new ArrayList<>();
                List<String> outLetId = new ArrayList<>();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(pduPowTotalRealDisResponse != null){
                        for ( Terms.Bucket bucket : ((ParsedLongTerms) pduPowTotalRealDisResponse.getAggregations().get("by_outlet_id")).getBuckets()) {
                            String outlet =  "输出位" + bucket.getKeyAsString();
                            Sum sum = bucket.getAggregations().get("sum_eq");
                            double sumEleActive = sum.getValue();
                            eqValue.add((float)sumEleActive);
                            outLetId.add(outlet);
                        }
                    }

                    BarSeries barSeries = new BarSeries();
                    barSeries.setLabel("{ show: true, position: 'right' }");
                    barSeries.setData(eqValue);
                    cabinetChartResBase.setTime(outLetId);
                    cabinetChartResBase.getSeries().add(barSeries);
                    result.put("eleValue",eqValue);
                    result.put("outLetId",outLetId);
                    result.put("barRes",cabinetChartResBase);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        } else {
            return result;
        }

    }

    @Override
    public Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase lineRes = new CabinetChartResBase();
        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
            if(pduIndex != null) {
                Long Id = pduIndex.getId();
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_env_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "pdu_env_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index);
                Map<Integer, List<PduEnvHourDo>> envMap = cabinetData.stream()
                        .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                        .collect(Collectors.groupingBy(PduEnvHourDo::getSensorId));
                boolean isFisrt = false;
                List<String> time = null;
                for (int i = 1; i < 6; i++) {
                    if(CollectionUtil.isEmpty(envMap.get(i))){
                        continue;
                    }
                    LineSeries lineSeries = new LineSeries();
                    lineSeries.setName("温度传感器" + i + "号");
                    List<PduEnvHourDo> hourDoList = envMap.get(i);
                    List<Float> temAvg = hourDoList.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                    lineSeries.setData(temAvg);
                    if(!isFisrt){
                        if(!isSameDay){
                            time = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                        }else{
                            time = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                        }
                        lineRes.setTime(time);
                        isFisrt = true;
                    }
                    lineRes.getSeries().add(lineSeries);
                }
                String temMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "tem_max_value");
                CabinetEnvHourDo temMax = JsonUtils.parseObject(temMaxValue, CabinetEnvHourDo.class);
                String temMinValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "tem_min_value");
                CabinetEnvHourDo temMin = JsonUtils.parseObject(temMinValue, CabinetEnvHourDo.class);
                result.put("lineRes",lineRes);
                if(temMax != null){
                    result.put("temMaxValue",temMax.getTemMaxValue());
                    result.put("temMaxTime",temMax.getTemMaxTime());
                    result.put("temMaxSensorId",temMax.getSensorId());
                }
                if(temMin != null) {
                    result.put("temMinValue", temMin.getTemMinValue());
                    result.put("temMinTime",temMin.getTemMinTime());
                    result.put("temMinSensorId",temMin.getSensorId());
                }
                return result;
            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }


    private List<String> getPDULineData(String startTime, String endTime, List<Integer> ids, String index,String sort) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))));
        builder.sort(sort, SortOrder.DESC);
        // 设置搜索条件
        searchRequest.source(builder);


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

    private   Map<Integer, Map<Integer, MaxValueAndCreateTime>> getPDULineCurMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))));

        builder.aggregation(
                AggregationBuilders.terms("group_by_pdu_id")
                        .field("pdu_id")
                        .size(10000)
                        .subAggregation(AggregationBuilders.terms("by_line_id")
                                .field("line_id")
                                .size(1000)
                                .order(BucketOrder.aggregation("max_cur", false))
                                .subAggregation(AggregationBuilders.max("max_cur").field("cur_max_value"))
                                .subAggregation(AggregationBuilders.topHits("top_docs")
                                        .size(1)
                                        .fetchSource(new String[]{"cur_max_time"}, null)
                                        .sort(SortBuilders.fieldSort("cur_max_value").order(SortOrder.DESC))))
        );



        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(0);

        List<String> list = new ArrayList<>();
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 初始化结果Map
        Map<Integer, Map<Integer, MaxValueAndCreateTime>> resultMap = new HashMap<>();
        // 获取group_by_box_id聚合结果
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_pdu_id");
        for (Terms.Bucket boxIdBucket : groupByBoxId.getBuckets()) {
            Integer boxId = boxIdBucket.getKeyAsNumber().intValue();
            Map<Integer, MaxValueAndCreateTime> lineIdMap = new HashMap<>();

            // 获取by_line_id聚合结果
            Terms byLineId = boxIdBucket.getAggregations().get("by_line_id");
            for (Terms.Bucket lineIdBucket : byLineId.getBuckets()) {
                Integer lineId = lineIdBucket.getKeyAsNumber().intValue();
                MaxValueAndCreateTime maxValueAndCreateTime = new MaxValueAndCreateTime();
                // 获取max_cur聚合结果
                ParsedMax maxCur = (ParsedMax) lineIdBucket.getAggregations().get("max_cur");
                maxValueAndCreateTime.setMaxValue(maxCur.getValue());

                // 获取top_hits聚合结果
                ParsedTopHits topHits = (ParsedTopHits) lineIdBucket.getAggregations().get("top_docs");
                if (topHits.getHits().getHits().length != 0) {
                    SearchHit topHit = topHits.getHits().getHits()[0]; // 取第一个top hit
                    Map<String, Object> sourceAsMap = topHit.getSourceAsMap();
                    maxValueAndCreateTime.setMaxTime(new DateTime(sourceAsMap.get("cur_max_time").toString(),"yyyy-MM-dd HH:mm:ss"));
                }

                // 将valueMap添加到lineIdMap中
                lineIdMap.put(lineId, maxValueAndCreateTime);
            }

            // 将lineIdMap添加到resultMap中
            resultMap.put(boxId, lineIdMap);
        }
        return resultMap;
    }

    private  Map<Integer, Map<Integer, MaxValueAndCreateTime>> getPDULinePowMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))));

        builder.aggregation(
                AggregationBuilders.terms("group_by_pdu_id")
                        .field("pdu_id")
                        .size(10000)
                        .subAggregation(AggregationBuilders.terms("by_line_id")
                                .field("line_id")
                                .size(1000)
                                .order(BucketOrder.aggregation("max_pow", false))
                                .subAggregation(AggregationBuilders.max("max_pow").field("pow_active_max_value"))
                                .subAggregation(AggregationBuilders.topHits("top_docs")
                                        .size(1)
                                        .fetchSource(new String[]{"pow_active_max_time"}, null)
                                        .sort(SortBuilders.fieldSort("pow_active_max_value").order(SortOrder.DESC))))
        );



        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(0);

        List<String> list = new ArrayList<>();
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);


        // 初始化结果Map
        Map<Integer, Map<Integer, MaxValueAndCreateTime>> resultMap = new HashMap<>();
        // 获取group_by_box_id聚合结果
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_pdu_id");
        for (Terms.Bucket boxIdBucket : groupByBoxId.getBuckets()) {
            Integer boxId = boxIdBucket.getKeyAsNumber().intValue();
            Map<Integer, MaxValueAndCreateTime> lineIdMap = new HashMap<>();

            // 获取by_line_id聚合结果
            Terms byLineId = boxIdBucket.getAggregations().get("by_line_id");
            for (Terms.Bucket lineIdBucket : byLineId.getBuckets()) {
                Integer lineId = lineIdBucket.getKeyAsNumber().intValue();
                MaxValueAndCreateTime maxValueAndCreateTime = new MaxValueAndCreateTime();
                // 获取max_cur聚合结果
                ParsedMax maxPow = (ParsedMax) lineIdBucket.getAggregations().get("max_pow");
                maxValueAndCreateTime.setMaxValue(maxPow.getValue());

                // 获取top_hits聚合结果
                ParsedTopHits topHits = (ParsedTopHits) lineIdBucket.getAggregations().get("top_docs");
                if (topHits.getHits().getHits().length != 0) {
                    SearchHit topHit = topHits.getHits().getHits()[0]; // 取第一个top hit
                    Map<String, Object> sourceAsMap = topHit.getSourceAsMap();
                    maxValueAndCreateTime.setMaxTime(new DateTime(sourceAsMap.get("pow_active_max_time").toString(),"yyyy-MM-dd HH:mm:ss"));
                }

                // 将valueMap添加到lineIdMap中
                lineIdMap.put(lineId, maxValueAndCreateTime);
            }

            // 将lineIdMap添加到resultMap中
            resultMap.put(boxId, lineIdMap);
        }
        return resultMap;
    }

    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))));
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
                .must(QueryBuilders.termsQuery("pdu_id", ids))));
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
                .must(QueryBuilders.termsQuery("pdu_id", ids))));
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

    private void setLocation(List<PduIndex> pduIndices,List<? extends PDUDeviceDO> result){
        if (CollectionUtils.isEmpty(result)){
            return;
        }
        Set<String> ipAddrSet = new HashSet<>();
        Set<Integer> cascadeAddrSet = new HashSet<>();
        for (PduIndex pduIndex : pduIndices) {
            ipAddrSet.add(pduIndex.getIpAddr());
            cascadeAddrSet.add(pduIndex.getCascadeAddr());
        }

        // 批量查询 CabinetPdu 表
        List<CabinetPdu> cabinetPdus = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>()
                .in(CabinetPdu::getPduIpA, ipAddrSet).in(CabinetPdu::getCasIdA, cascadeAddrSet)
                .or().in(CabinetPdu::getPduIpB,ipAddrSet).in(CabinetPdu::getCasIdB,cascadeAddrSet));

        // 将查询结果按 ipAddr 和 cascadeAddr 分组
        Map<String, List<CabinetPdu>> cabinetPduAMap = cabinetPdus.stream()
                .filter(cabinetPdu -> ipAddrSet.contains(cabinetPdu.getPduIpA()) && cascadeAddrSet.contains(cabinetPdu.getCasIdA()))
                .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduIpA() + "-" + cabinetPdu.getCasIdA()));


        Map<String, List<CabinetPdu>> cabinetPduBMap = cabinetPdus.stream()
                .filter(cabinetPdu -> ipAddrSet.contains(cabinetPdu.getPduIpB()) && cascadeAddrSet.contains(cabinetPdu.getCasIdB()))
                .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduIpB() + "-" + cabinetPdu.getCasIdB()));


        List<CabinetIndex> cabinetIndices = cabinetIndexMapper.selectBatchIds(cabinetPdus.stream().map(CabinetPdu::getCabinetId).collect(Collectors.toList()));
        Map<Integer, CabinetIndex> cabinetMap = cabinetIndices.stream().collect(Collectors.toMap(CabinetIndex::getId, Function.identity()));
        List<RoomIndex> roomIndices = roomIndexMapper.selectBatchIds(cabinetIndices.stream().map(CabinetIndex::getRoomId).collect(Collectors.toList()));
        Map<Integer, String> roomMap = roomIndices.stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getName));
        Map<Integer, String> aisleMap = aisleIndexMapper.selectBatchIds(cabinetIndices.stream().filter(dto -> dto.getAisleId() != 0).map(CabinetIndex::getAisleId).collect(Collectors.toList())).stream().collect(Collectors.toMap(AisleIndex::getId, AisleIndex::getName));

        result.forEach( pduIndex ->{
            String localtion = null;
            List<CabinetPdu> cabinetPduAList = cabinetPduAMap.get(pduIndex.getDevKey());
            List<CabinetPdu> cabinetPduBList = cabinetPduBMap.get(pduIndex.getDevKey());

            if (cabinetPduAList != null && !cabinetPduAList.isEmpty()) {
                CabinetPdu cabinetPduA = cabinetPduAList.get(0); // 假设结果唯一
                CabinetIndex cabinetIndex = cabinetMap.get(cabinetPduA.getCabinetId());
                if (cabinetIndex.getAisleId() != 0){
                    localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + aisleMap.get(cabinetIndex.getAisleId()) + "-" + cabinetIndex.getName() + "-" + "A路";
                }else {
                    localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + cabinetIndex.getName() + "-" + "A路";
                }
            }

            if (cabinetPduBList != null && !cabinetPduBList.isEmpty()) {
                CabinetPdu cabinetPduB = cabinetPduBList.get(0); // 假设结果唯一
                CabinetIndex cabinetIndex = cabinetMap.get(cabinetPduB.getCabinetId());
                if (cabinetIndex.getAisleId() != 0){
                    localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + aisleMap.get(cabinetIndex.getAisleId()) + "-" + cabinetIndex.getName() + "-" + "B路";
                }else {
                    localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + cabinetIndex.getName() + "-" + "B路";
                }
            }
            pduIndex.setLocation(localtion);
        });
    }

    private List getMutiRedis(List<PduIndex> list){
        List<String> devKeys = list.stream().map(pduIndex -> REDIS_KEY_PDU + pduIndex.getDevKey()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    private Map getESTotalPduId(String index,String startTime,String endTime,Integer pageSize,Integer pageNo) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder
                .from(pageNo * pageSize)
                .size(pageSize)
                .query(QueryBuilders.rangeQuery("create_time.keyword").gte(startTime).lte(endTime))
                .sort("pdu_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("pdu_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("pdu_id").precisionThreshold(10000));
        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                PduHdaLineHourDo hourDo = JsonUtils.parseObject(str, PduHdaLineHourDo.class);
                sortValues.add(hourDo.getPduId());
            }
        }
        Long totalRes = 0L;
        Cardinality totalSizeAggregation = searchResponse.getAggregations().get("total_size");
        if (totalSizeAggregation != null){
            totalRes = totalSizeAggregation.getValue();
        }

        result.put("total",totalRes);
        result.put("ids",sortValues);
        return result;
    }

    private Map getESTotalPduId(String index,String startTime,String endTime,Integer pageSize,Integer pageNo,List<Long> ids) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder
                .from(pageNo * pageSize)
                .size(pageSize)
                .query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                                .must(QueryBuilders.termsQuery("pdu_id", ids))))
                .sort("pdu_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("pdu_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("pdu_id").precisionThreshold(10000));
        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                PduHdaLineHourDo hourDo = JsonUtils.parseObject(str, PduHdaLineHourDo.class);
                sortValues.add(hourDo.getPduId());
            }
        }
        Long totalRes = 0L;
        Cardinality totalSizeAggregation = searchResponse.getAggregations().get("total_size");
        if (totalSizeAggregation != null){
            totalRes = totalSizeAggregation.getValue();
        }

        result.put("total",totalRes);
        result.put("ids",sortValues);
        return result;
    }
}