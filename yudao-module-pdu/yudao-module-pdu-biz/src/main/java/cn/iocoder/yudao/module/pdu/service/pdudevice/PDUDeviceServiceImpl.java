package cn.iocoder.yudao.module.pdu.service.pdudevice;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalRealtimeDo;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;

import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndex;


import com.alibaba.druid.util.StringUtils;
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
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.framework.common.pojo.PageResult;


import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndexMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * PDU设备 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PDUDeviceServiceImpl implements PDUDeviceService {

    @Resource
    private PduIndexMapper pDUDeviceMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient client;


    @Override
    public PageResult<PDUDeviceDO> getPDUDevicePage(PDUDevicePageReqVO pageReqVO) {

        PageResult<PduIndex> pduIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PduIndex>()
                .likeIfPresent(PduIndex::getDevKey,pageReqVO.getDevKey()));


        List<PduIndex> pduIndices = pduIndexPageResult.getList();
        ValueOperations ops = redisTemplate.opsForValue();
        List<PDUDeviceDO> result = new ArrayList<>();
        long i = 0;
        for (PduIndex pduIndex : pduIndices) {
            i++;
            JSONObject jsonObject = (JSONObject) ops.get("packet:pdu:" + pduIndex.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject pduTgData = jsonObject.getJSONObject("pdu_data").getJSONObject("pdu_tg_data");
            Integer status1 = jsonObject.getInteger("status");
            if(pageReqVO.getStatus() != null){
                if(!pageReqVO.getStatus().contains(status1)){
                    continue;
                }
            }
            PDUDeviceDO pduDeviceDO = new PDUDeviceDO();
            if(status1 != null){
                pduDeviceDO.setStatus(status1);
            }
            pduDeviceDO.setId(pduIndex.getId());
            pduDeviceDO.setPf(pduTgData.getDoubleValue("pf"));
            pduDeviceDO.setDevKey(pduIndex.getDevKey());
            pduDeviceDO.setEle(pduTgData.getDoubleValue("ele"));
            pduDeviceDO.setPow(pduTgData.getDoubleValue("pow"));
            pduDeviceDO.setApparentPow(pduTgData.getDoubleValue("pow_apparent"));
            pduDeviceDO.setReactivePow(pduTgData.getDoubleValue("pow_reactive"));
            pduDeviceDO.setDataUpdateTime(jsonObject.getString("sys_time"));
            pduDeviceDO.setPduAlarm(jsonObject.getString("pdu_alarm"));
            result.add(pduDeviceDO);
        }
        return new PageResult<PDUDeviceDO>(result,pduIndexPageResult.getTotal());
    }

    @Override
    public String getDisplayDataByDevKey(String devKey) {
        if (StringUtils.isEmpty(devKey)){
            return null;
        }else {
            ValueOperations ops = redisTemplate.opsForValue();
            JSONObject jsonObject = (JSONObject) ops.get("packet:pdu:" + devKey);
            return jsonObject.toJSONString();
        }
    }

    @Override
    public Map getHistoryDataByPduId(Long id,String type) {
        HashMap result = new HashMap<>();

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
    }

    @Override
    public Map getChartNewDataByPduDevKey(String devKey,LocalDateTime oldTime,String type) {
        HashMap result = new HashMap<>();

        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
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
    }

    @Override
    public Map getReportConsumeDataByDevKey(String devKey,Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        Long pduId = pduIndex.getId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if(timeType.equals(0) || oldTime.equals(newTime)){
            if(oldTime.equals(newTime)){
                newTime = newTime.withHour(23).withMinute(59).withSecond(59);
            }
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            // 创建时间分布搜索请求
            SearchRequest pduRealTimeDisRequest = new SearchRequest("pdu_ele_total_realtime");
            SearchSourceBuilder pduRealTimeDisSourceBuilder = new SearchSourceBuilder();

            pduRealTimeDisSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                    .must(QueryBuilders.termQuery("pdu_id", pduId)));

            pduRealTimeDisSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            pduRealTimeDisSourceBuilder.size(24); // 设置返回的最大结果数
            // 将搜索条件添加到请求中
            pduRealTimeDisRequest.source(pduRealTimeDisSourceBuilder);
            // 将第二个搜索请求添加到多索引搜索请求中
            multiSearchRequest.add(pduRealTimeDisRequest);
            double todayEle = 0;
            List<Double> eq = new ArrayList<>();
            List<String> time = new ArrayList<>();
            double maxEle = -1;
            DateTime maxEleTime = new DateTime();
            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                // 解析搜索请求
                SearchResponse pduEleTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                if(pduEleTotalRealDisResponse != null){
                    for (SearchHit hit : pduEleTotalRealDisResponse.getHits()) {
                        PduEleTotalRealtimeDo pduEleTotalRealtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEleTotalRealtimeDo.class);
                        double eleValue  = pduEleTotalRealtimeDo.getEle();
                        DateTime createTime = pduEleTotalRealtimeDo.getCreateTime();
                        if(eleValue > maxEle){
                            maxEle = eleValue;
                            maxEleTime = createTime;
                        }
                        eq.add(eleValue);
                        time.add(createTime.toString("yyyy-MM-dd HH:mm"));
                    }
                }
                result.put("eq",eq);
                result.put("time",time);
                result.put("totalEle",todayEle);
                result.put("maxEle",maxEle != -1 ? maxEle : null);
                result.put("maxEleTime",maxEle != -1 ? maxEleTime.toString("yyyy-MM-dd HH:mm:ss") : null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            // 创建聚合搜索请求
            SearchRequest pduEqTotalDayRequest = new SearchRequest("pdu_eq_total_day");
            SearchSourceBuilder pduEqTotalDaySourceBuilder = new SearchSourceBuilder();
            // 设置时间范围查询条件

            pduEqTotalDaySourceBuilder.query(QueryBuilders.boolQuery()
                    //今天的数据 pdu_ele_total_realtime的时间范围查询必须使用字符串
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1).withHour(7))))
                    .must(QueryBuilders.termQuery("pdu_id", pduId))); // 添加pdu_id条件
            // 设置聚合条件
            pduEqTotalDaySourceBuilder.aggregation(AggregationBuilders.sum("total_eq")
                    .field("eq_value"));
            // 将搜索条件添加到请求中
            pduEqTotalDayRequest.source(pduEqTotalDaySourceBuilder);
            // 将第二个搜索请求添加到多索引搜索请求中
            multiSearchRequest.add(pduEqTotalDayRequest);

            // 创建时间分布搜索请求
            SearchRequest pduRealTimeDisRequest = new SearchRequest("pdu_eq_total_day");
            SearchSourceBuilder pduRealTimeDisSourceBuilder = new SearchSourceBuilder();

            pduRealTimeDisSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1).withHour(7))))
                    .must(QueryBuilders.termQuery("pdu_id", pduId)));

            pduRealTimeDisSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            pduRealTimeDisSourceBuilder.size(31); // 设置返回的最大结果数
            // 将搜索条件添加到请求中
            pduRealTimeDisRequest.source(pduRealTimeDisSourceBuilder);
            // 将第二个搜索请求添加到多索引搜索请求中
            multiSearchRequest.add(pduRealTimeDisRequest);
            double totalEq = 0;
            List<Double> eq = new ArrayList<>();
            List<String> time = new ArrayList<>();
            double maxEq = -1;
            DateTime maxEleTime = new DateTime();
            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                // 解析第一个搜索请求的聚合结果
                SearchResponse pduEleTotalRealResponse = multiSearchResponse.getResponses()[0].getResponse();
                Sum sumAggregation1 = pduEleTotalRealResponse.getAggregations().get("total_eq");
                totalEq = sumAggregation1.getValue();

                // 解析第二个搜索请求
                SearchResponse pduEqTotalDayDisResponse = multiSearchResponse.getResponses()[1].getResponse();
                if(pduEqTotalDayDisResponse != null){
                    for (SearchHit hit : pduEqTotalDayDisResponse.getHits()) {
                        PduEqTotalDayDo pduEqTotalDayDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEqTotalDayDo.class);
                        double eqValue  = pduEqTotalDayDo.getEq();
                        DateTime startTime = pduEqTotalDayDo.getStartTime();
                        if(eqValue > maxEq){
                            maxEq = eqValue;
                            maxEleTime = startTime;
                        }
                        eq.add(eqValue);
                        time.add(startTime.toString("yyyy-MM-dd "));
                    }
                }
                result.put("eq",eq);
                result.put("time",time);
                result.put("totalEle",totalEq);
                result.put("maxEle",maxEq != -1 ? maxEq : null);
                result.put("maxEleTime",maxEq != -1 ? maxEleTime.toString("yyyy-MM-dd HH:mm:ss") : null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        Long pduId = pduIndex.getId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if(timeType.equals(0) || oldTime.equals(newTime)){
            if(oldTime.equals(newTime)){
                newTime = newTime.withHour(23).withMinute(59).withSecond(59);
            }
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            // 创建时间分布搜索请求
            SearchRequest pduPowTotalRealRequest = new SearchRequest("pdu_hda_total_hour");
            SearchSourceBuilder pduPowTotalRealSourceBuilder = new SearchSourceBuilder();

            pduPowTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                    .must(QueryBuilders.termQuery("pdu_id", pduId)));

            pduPowTotalRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            pduPowTotalRealSourceBuilder.size(24); // 设置返回的最大结果数
            // 将搜索条件添加到请求中
            pduPowTotalRealRequest.source(pduPowTotalRealSourceBuilder);
            // 将第二个搜索请求添加到多索引搜索请求中
            multiSearchRequest.add(pduPowTotalRealRequest);

            List<Double> activePowAvgValue = new ArrayList<>();
            List<Double> apparentPowAvgValue = new ArrayList<>();
            List<String> time = new ArrayList<>();
            double apparentPowMaxValue = -1;
            DateTime apparentPowMaxTime = new DateTime();
            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                // 解析搜索请求
                SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                if(pduPowTotalRealDisResponse != null){
                    for (SearchHit hit : pduPowTotalRealDisResponse.getHits()) {
                        PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduHdaTotalHourDo.class);
                        double activePowAvg  = pduHdaTotalHourDo.getActivePowAvgValue();
                        double apparentPowAvg = pduHdaTotalHourDo.getApparentPowAvgValue();
                        DateTime createTime = pduHdaTotalHourDo.getCreateTime();
                        if(pduHdaTotalHourDo.getApparentPowMaxValue() > apparentPowMaxValue){
                            apparentPowMaxValue = pduHdaTotalHourDo.getApparentPowMaxValue();
                            apparentPowMaxTime = createTime;
                        }
                        activePowAvgValue.add(activePowAvg);
                        apparentPowAvgValue.add(apparentPowAvg);
                        time.add(createTime.toString("yyyy-MM-dd HH:mm"));
                    }
                }
                result.put("activePowAvgValue",activePowAvgValue);
                result.put("apparentPowAvgValue",apparentPowAvgValue);
                result.put("time",time);
                result.put("apparentPowMaxValue",apparentPowMaxValue != -1 ? apparentPowMaxValue : null);
                result.put("apparentPowMaxTime",apparentPowMaxValue != -1 ? apparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            // 创建时间分布搜索请求
            SearchRequest pduPowTotalRealRequest = new SearchRequest("pdu_hda_total_day");
            SearchSourceBuilder pduPowTotalRealSourceBuilder = new SearchSourceBuilder();

            pduPowTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1).withHour(7))))
                    .must(QueryBuilders.termQuery("pdu_id", pduId)));

            pduPowTotalRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            pduPowTotalRealSourceBuilder.size(31); // 设置返回的最大结果数
            // 将搜索条件添加到请求中
            pduPowTotalRealRequest.source(pduPowTotalRealSourceBuilder);
            // 将第搜索请求添加到多索引搜索请求中
            multiSearchRequest.add(pduPowTotalRealRequest);

            List<Double> activePowAvgValue = new ArrayList<>();
            List<Double> apparentPowAvgValue = new ArrayList<>();
            List<String> time = new ArrayList<>();
            double apparentPowMaxValue = -1;
            DateTime apparentPowMaxTime = new DateTime();
            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                // 解析搜索请求
                SearchResponse pduPowTotalDayDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                if(pduPowTotalDayDisResponse != null){
                    for (SearchHit hit : pduPowTotalDayDisResponse.getHits()) {
                        PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduHdaTotalHourDo.class);
                        double activePowAvg  = pduHdaTotalHourDo.getActivePowAvgValue();
                        double apparentPowAvg = pduHdaTotalHourDo.getApparentPowAvgValue();
                        DateTime createTime = pduHdaTotalHourDo.getCreateTime();
                        if(pduHdaTotalHourDo.getApparentPowMaxValue() > apparentPowMaxValue){
                            apparentPowMaxValue = pduHdaTotalHourDo.getApparentPowMaxValue();
                            apparentPowMaxTime = createTime;
                        }
                        activePowAvgValue.add(activePowAvg);
                        apparentPowAvgValue.add(apparentPowAvg);
                        time.add(createTime.toString("yyyy-MM-dd HH:mm"));
                    }
                }
                result.put("activePowAvgValue",activePowAvgValue);
                result.put("apparentPowAvgValue",apparentPowAvgValue);
                result.put("time",time);
                result.put("apparentPowMaxValue",apparentPowMaxValue != -1 ? apparentPowMaxValue : null);
                result.put("apparentPowMaxTime",apparentPowMaxValue != -1 ? apparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public Map getReportOutLetDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        Long pduId = pduIndex.getId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if(timeType.equals(0) || oldTime.equals(newTime)){
            if(oldTime.equals(newTime)){
                newTime = newTime.withHour(23).withMinute(59).withSecond(59);
            }
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            // 创建时间分布搜索请求
            SearchRequest pduOutLetTotalRealRequest = new SearchRequest("pdu_ele_outlet");
            SearchSourceBuilder pduOutLetTotalRealSourceBuilder = new SearchSourceBuilder();

            pduOutLetTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                    .must(QueryBuilders.termQuery("pdu_id", pduId)));
            //映射sum的sum_ele_active为sumEle
            Map<String, String> map = new HashMap<>();
            map.put("sumEle", "sum_ele_active");
            pduOutLetTotalRealSourceBuilder.aggregation(AggregationBuilders
                    .terms("by_outlet_id").field("outlet_id").order(BucketOrder.aggregation("sum_ele_active",true)).size(24)
                    .subAggregation(AggregationBuilders.sum("sum_ele_active").field("ele_active"))
                    //筛选sumEle > 0的
                    .subAggregation(PipelineAggregatorBuilders.bucketSelector("positive_sum_ele",map, new Script("params.sumEle > 0"))));


            pduOutLetTotalRealSourceBuilder.size(0); // 设置返回的最大结果数
            // 将搜索条件添加到请求中
            pduOutLetTotalRealRequest.source(pduOutLetTotalRealSourceBuilder);
            // 将第搜索请求添加到多索引搜索请求中
            multiSearchRequest.add(pduOutLetTotalRealRequest);

            List<Double> eleValue = new ArrayList<>();
            List<String> outLetId = new ArrayList<>();
            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                // 解析搜索请求
                SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                if(pduPowTotalRealDisResponse != null){
                    for ( Terms.Bucket bucket : ((ParsedLongTerms) pduPowTotalRealDisResponse.getAggregations().get("by_outlet_id")).getBuckets()) {
                        String outlet = bucket.getKeyAsString();
                        Sum sum = bucket.getAggregations().get("sum_ele_active");
                        double sumEleActive = sum.getValue();
                        eleValue.add(sumEleActive);
                        outLetId.add(outlet);
                    }
                }
                result.put("eleValue",eleValue);
                result.put("outLetId",outLetId);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            // 创建时间分布搜索请求
            SearchRequest pduOutLetTotalDayRequest = new SearchRequest("pdu_eq_outlet_day");
            SearchSourceBuilder pduOutLetTotalDaySourceBuilder = new SearchSourceBuilder();

            pduOutLetTotalDaySourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1).withHour(7))))
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

            List<Double> eqValue = new ArrayList<>();
            List<String> outLetId = new ArrayList<>();
            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                // 解析搜索请求
                SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                if(pduPowTotalRealDisResponse != null){
                    for ( Terms.Bucket bucket : ((ParsedLongTerms) pduPowTotalRealDisResponse.getAggregations().get("by_outlet_id")).getBuckets()) {
                        String outlet = bucket.getKeyAsString();
                        Sum sum = bucket.getAggregations().get("sum_eq");
                        double sumEleActive = sum.getValue();
                        eqValue.add(sumEleActive);
                        outLetId.add(outlet);
                    }
                }
                result.put("eleValue",eqValue);
                result.put("outLetId",outLetId);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        Long pduId = pduIndex.getId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if(timeType.equals(0) || oldTime.equals(newTime)){
            if(oldTime.equals(newTime)){
                newTime = newTime.withHour(23).withMinute(59).withSecond(59);
            }
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            // 创建时间分布搜索请求
            SearchRequest pduTemRealRequest = new SearchRequest("pdu_env_hour");
            SearchSourceBuilder pduTemRealSourceBuilder = new SearchSourceBuilder();

            pduTemRealSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                    .must(QueryBuilders.termQuery("pdu_id", pduId)));


            pduTemRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            pduTemRealSourceBuilder.size(24); // 设置返回的最大结果数
            // 将搜索条件添加到请求中
            pduTemRealRequest.source(pduTemRealSourceBuilder);
            // 将第搜索请求添加到多索引搜索请求中
            multiSearchRequest.add(pduTemRealRequest);

            List<Double> temAvgValue = new ArrayList<>();
            List<String> time = new ArrayList<>();
            double temMaxValue = -255;
            DateTime temMaxTime = null;
            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                // 解析搜索请求
                SearchResponse pduTemRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                if(pduTemRealDisResponse != null) {
                    for (SearchHit hit : pduTemRealDisResponse.getHits()) {
                        PduEnvHourDo pduEnvHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEnvHourDo.class);
                        double temAvg = pduEnvHourDo.getTemAvgValue();
                        DateTime createTime = pduEnvHourDo.getCreateTime();
                        if (pduEnvHourDo.getTemMaxValue() > temMaxValue) {
                            temMaxValue = pduEnvHourDo.getTemMaxValue();
                            temMaxTime = createTime;
                        }
                        temAvgValue.add(temAvg);
                        time.add(createTime.toString("yyyy-MM-dd HH:mm"));
                    }
                }
                result.put("temAvgValue",temAvgValue);
                result.put("time",time);
                result.put("temMaxValue",temMaxValue != -255 ? temMaxValue : null);
                result.put("temMaxTime",temMaxValue != -255 ? temMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

            // 创建时间分布搜索请求
            SearchRequest pduTemDayRequest = new SearchRequest("pdu_env_day");
            SearchSourceBuilder pduTemDaySourceBuilder = new SearchSourceBuilder();

            pduTemDaySourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                    .must(QueryBuilders.termQuery("pdu_id", pduId)));


            pduTemDaySourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            pduTemDaySourceBuilder.size(24); // 设置返回的最大结果数
            // 将搜索条件添加到请求中
            pduTemDayRequest.source(pduTemDaySourceBuilder);
            // 将第搜索请求添加到多索引搜索请求中
            multiSearchRequest.add(pduTemDayRequest);

            List<Double> temAvgValue = new ArrayList<>();
            List<String> time = new ArrayList<>();
            double temMaxValue = -255;
            DateTime temMaxTime = null;
            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                // 解析搜索请求
                SearchResponse pduTemRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                if(pduTemRealDisResponse != null) {
                    for (SearchHit hit : pduTemRealDisResponse.getHits()) {
                        PduEnvHourDo pduEnvHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEnvHourDo.class);
                        double temAvg = pduEnvHourDo.getTemAvgValue();
                        DateTime createTime = pduEnvHourDo.getCreateTime();
                        if (pduEnvHourDo.getTemMaxValue() > temMaxValue) {
                            temMaxValue = pduEnvHourDo.getTemMaxValue();
                            temMaxTime = createTime;
                        }
                        temAvgValue.add(temAvg);
                        time.add(createTime.toString("yyyy-MM-dd HH:mm"));
                    }
                }
                result.put("temAvgValue",temAvgValue);
                result.put("time",time);
                result.put("temMaxValue",temMaxValue != -255 ? temMaxValue : null);
                result.put("temMaxTime",temMaxValue != -255 ? temMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }

}