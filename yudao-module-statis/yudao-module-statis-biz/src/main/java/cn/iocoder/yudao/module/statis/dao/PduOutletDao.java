package cn.iocoder.yudao.module.statis.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.outlet.PduEleOutletDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.enums.EsIndexEnum;
import cn.iocoder.yudao.framework.common.enums.EsStatisFieldEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.module.statis.constant.Constants.*;

/**
 * @Author: chenwany
 * @Date: 2024/4/3 09:35
 * @Description: 输出位历史数据统计
 */
@Slf4j
@Component
public class PduOutletDao {

    @Autowired
    RestHighLevelClient client;


    /**
     * 输出位历史数据统计(按小时)
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Integer, Map<Integer, PduHdaOutletBaseDo>> statisOutletHour(String startTime, String endTime) {
        Map<Integer, Map<Integer, PduHdaOutletBaseDo>> result = new HashMap<>();
        try {
            String index = EsIndexEnum.PDU_HDA_OUTLET_REALTIME.getIndex();
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);

            // 设置Avg指标聚合，按outlet_id分组
            TermsAggregationBuilder outletAggregationBuilder = AggregationBuilders.terms(BY_OUTLET).field(OUTLET_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder.subAggregation(outletAggregationBuilder
                    //统计平均电流
                    .subAggregation(AggregationBuilders.avg(CUR_AVG_VALUE).field(CUR))
                    //平均有功功率
                    .subAggregation(AggregationBuilders.avg(ACTIVE_POW_AVG_VALUE).field(ACTIVE_POW))
                    //平均视在功率
                    .subAggregation(AggregationBuilders.avg(APPARENT_POW_AVG_VALUE).field(APPARENT_POW))));

            // 设置搜索条件
            searchRequest.source(builder);
            // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();
            // 根据by_pdu名字查询terms聚合结果
            Terms byPduAggregation = aggregations.get(BY_PDU);


            // 遍历terms聚合结果
            for (Terms.Bucket bucket : byPduAggregation.getBuckets()) {
                // 获取按pduId分组
                Map<Integer, PduHdaOutletBaseDo> dataMap = new HashMap<>();
                Terms byOutletAggregation = bucket.getAggregations().get(BY_OUTLET);
                //获取按outlet_Id分组
                for (Terms.Bucket baseBucket : byOutletAggregation.getBuckets()) {
                    PduHdaOutletBaseDo baseDo = new PduHdaOutletBaseDo();
                    baseDo.setOutletId(Integer.parseInt(String.valueOf(baseBucket.getKey())));
                    baseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                    EsStatisFieldEnum.fields().forEach(field -> {

                        if (field.equals(CUR_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setCurAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(ACTIVE_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setActivePowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(APPARENT_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setApparentPowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                    });
                    baseDo.setCreateTime(DateTime.now());
                    dataMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())), baseDo);
                }
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), dataMap);
            }

            //获取时间
            Map<Integer,Map<Integer,String>> curMaxValueMap = getData(startTime,endTime,SortOrder.DESC,CUR,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>> curMinValueMap = getData(startTime,endTime,SortOrder.ASC,CUR,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>> activePowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,ACTIVE_POW,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>>  activePowMinValueMap = getData(startTime,endTime,SortOrder.ASC,ACTIVE_POW,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>>  apparentPowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,APPARENT_POW,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>>  apparentPowMinValueMap = getData(startTime,endTime,SortOrder.ASC,APPARENT_POW,index,CREATE_TIME);

            result.keySet().forEach(pduId -> {
                Map<Integer, PduHdaOutletBaseDo> fieldMap = result.get(pduId);

                fieldMap.keySet().forEach(outletId->{
                    PduHdaOutletBaseDo baseDo = fieldMap.get(outletId);

                    PduHdaOutletRealtimeDo curMaxMap = JsonUtils.parseObject(curMaxValueMap.get(pduId).get(outletId),PduHdaOutletRealtimeDo.class) ;
                    baseDo.setCurMaxTime(curMaxMap.getCreateTime());
                    baseDo.setCurMaxValue(curMaxMap.getCur());

                    PduHdaOutletRealtimeDo curMinMap = JsonUtils.parseObject(curMinValueMap.get(pduId).get(outletId),PduHdaOutletRealtimeDo.class) ;
                    baseDo.setCurMinTime(curMinMap.getCreateTime());
                    baseDo.setCurMinValue(curMinMap.getCur());

                    PduHdaOutletRealtimeDo activePowMaxMap = JsonUtils.parseObject(activePowMaxValueMap.get(pduId).get(outletId),PduHdaOutletRealtimeDo.class) ;
                    baseDo.setActivePowMaxTime(activePowMaxMap.getCreateTime());
                    baseDo.setActivePowMaxValue(activePowMaxMap.getActivePow());

                    PduHdaOutletRealtimeDo activePowMinMap = JsonUtils.parseObject(activePowMinValueMap.get(pduId).get(outletId),PduHdaOutletRealtimeDo.class) ;
                    baseDo.setActivePowMinTime(activePowMinMap.getCreateTime());
                    baseDo.setActivePowMinValue(activePowMinMap.getActivePow());

                    PduHdaOutletRealtimeDo apparentPowMaxMap = JsonUtils.parseObject(apparentPowMaxValueMap.get(pduId).get(outletId),PduHdaOutletRealtimeDo.class) ;
                    baseDo.setApparentPowMaxValue(apparentPowMaxMap.getApparentPow());
                    baseDo.setApparentPowMaxTime(apparentPowMaxMap.getCreateTime());

                    PduHdaOutletRealtimeDo apparentPowMinMap = JsonUtils.parseObject(apparentPowMinValueMap.get(pduId).get(outletId),PduHdaOutletRealtimeDo.class) ;
                    baseDo.setApparentPowMinTime(apparentPowMinMap.getCreateTime());
                    baseDo.setApparentPowMinValue(apparentPowMinMap.getApparentPow());
                    fieldMap.put(outletId,baseDo);
                });

                result.put(pduId, fieldMap);
            });

            log.info("--------------------" + result);

            return result;

        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return result;
    }

    /**
     * 输出位历史数据统计(按天)
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Integer, Map<Integer, PduHdaOutletBaseDo>> statisOutletDay(String startTime, String endTime) {
        Map<Integer, Map<Integer, PduHdaOutletBaseDo>> result = new HashMap<>();
        try {
            String index = EsIndexEnum.PDU_HDA_OUTLET_HOUR.getIndex();
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);

            // 设置Avg指标聚合，按outlet_id分组
            TermsAggregationBuilder outletAggregationBuilder = AggregationBuilders.terms(BY_OUTLET).field(OUTLET_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder.subAggregation(outletAggregationBuilder
                    //统计平均电流
                    .subAggregation(AggregationBuilders.avg(CUR_AVG_VALUE).field(CUR_AVG_VALUE))
                    //平均有功功率
                    .subAggregation(AggregationBuilders.avg(ACTIVE_POW_AVG_VALUE).field(ACTIVE_POW_AVG_VALUE))
                    //平均视在功率
                    .subAggregation(AggregationBuilders.avg(APPARENT_POW_AVG_VALUE).field(APPARENT_POW_AVG_VALUE))));

            // 设置搜索条件
            searchRequest.source(builder);
            // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();
            // 根据by_pdu名字查询terms聚合结果
            Terms byPduAggregation = aggregations.get(BY_PDU);


            // 遍历terms聚合结果
            for (Terms.Bucket bucket : byPduAggregation.getBuckets()) {
                // 获取按pduId分组
                Map<Integer, PduHdaOutletBaseDo> dataMap = new HashMap<>();
                Terms byOutletAggregation = bucket.getAggregations().get(BY_OUTLET);
                //获取按outlet_Id分组
                for (Terms.Bucket baseBucket : byOutletAggregation.getBuckets()) {
                    PduHdaOutletBaseDo baseDo = new PduHdaOutletBaseDo();
                    baseDo.setOutletId(Integer.parseInt(String.valueOf(baseBucket.getKey())));
                    baseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                    EsStatisFieldEnum.fields().forEach(field -> {

                        if (field.equals(CUR_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setCurAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(ACTIVE_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setActivePowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(APPARENT_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setApparentPowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                    });
                    baseDo.setCreateTime(DateTime.now());
                    dataMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())), baseDo);
                }
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), dataMap);
            }

            //获取时间
            Map<Integer,Map<Integer,String>> curMaxValueMap = getData(startTime,endTime,SortOrder.DESC,CUR_MAX_VALUE,index,CUR_MAX_TIME);

            Map<Integer,Map<Integer,String>> curMinValueMap = getData(startTime,endTime,SortOrder.ASC,CUR_MIN_VALUE,index,CUR_MIN_TIME);

            Map<Integer,Map<Integer,String>> activePowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,ACTIVE_POW_MAX_VALUE,index,ACTIVE_POW_MAX_TIME);

            Map<Integer,Map<Integer,String>>  activePowMinValueMap = getData(startTime,endTime,SortOrder.ASC,ACTIVE_POW_MIN_VALUE,index,ACTIVE_POW_MIN_TIME);

            Map<Integer,Map<Integer,String>>  apparentPowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,APPARENT_POW_MAX_VALUE,index,APPARENT_POW_MAX_TIME);

            Map<Integer,Map<Integer,String>>  apparentPowMinValueMap = getData(startTime,endTime,SortOrder.ASC,APPARENT_POW_MIN_VALUE,index,APPARENT_POW_MIN_TIME);

            result.keySet().forEach(pduId -> {
                Map<Integer, PduHdaOutletBaseDo> fieldMap = result.get(pduId);

                fieldMap.keySet().forEach(outletId->{
                    PduHdaOutletBaseDo baseDo = fieldMap.get(outletId);

                    PduHdaOutletHourDo curMaxMap = JsonUtils.parseObject(curMaxValueMap.get(pduId).get(outletId),PduHdaOutletHourDo.class) ;
                    baseDo.setCurMaxTime(curMaxMap.getCurMaxTime());
                    baseDo.setCurMaxValue(curMaxMap.getCurMaxValue());

                    PduHdaOutletHourDo curMinMap = JsonUtils.parseObject(curMinValueMap.get(pduId).get(outletId),PduHdaOutletHourDo.class) ;
                    baseDo.setCurMinTime(curMinMap.getCurMinTime());
                    baseDo.setCurMinValue(curMinMap.getCurMinValue());

                    PduHdaOutletHourDo activePowMaxMap = JsonUtils.parseObject(activePowMaxValueMap.get(pduId).get(outletId),PduHdaOutletHourDo.class) ;
                    baseDo.setActivePowMaxTime(activePowMaxMap.getActivePowMaxTime());
                    baseDo.setActivePowMaxValue(activePowMaxMap.getActivePowMaxValue());

                    PduHdaOutletHourDo activePowMinMap = JsonUtils.parseObject(activePowMinValueMap.get(pduId).get(outletId),PduHdaOutletHourDo.class) ;
                    baseDo.setActivePowMinTime(activePowMinMap.getActivePowMinTime());
                    baseDo.setActivePowMinValue(activePowMinMap.getActivePowMinValue());

                    PduHdaOutletHourDo apparentPowMaxMap = JsonUtils.parseObject(apparentPowMaxValueMap.get(pduId).get(outletId),PduHdaOutletHourDo.class) ;
                    baseDo.setApparentPowMaxValue(apparentPowMaxMap.getApparentPowMaxValue());
                    baseDo.setApparentPowMaxTime(apparentPowMaxMap.getApparentPowMaxTime());

                    PduHdaOutletHourDo apparentPowMinMap = JsonUtils.parseObject(apparentPowMinValueMap.get(pduId).get(outletId),PduHdaOutletHourDo.class) ;
                    baseDo.setApparentPowMinTime(apparentPowMinMap.getApparentPowMinTime());
                    baseDo.setApparentPowMinValue(apparentPowMinMap.getApparentPowMinValue());
                    fieldMap.put(outletId,baseDo);
                });

                result.put(pduId, fieldMap);
            });
            log.info("--------------------" + result);

            return result;

        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return result;
    }

    /**
     * 获取最大/最小数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param sortOrder 升序或降序
     * @param field 统计字段
     */
    private  Map<Integer,Map<Integer,String>>  getData(String startTime, String endTime, SortOrder sortOrder, String field, String index,String sortTime) throws IOException {
        Map<Integer,Map<Integer,String>>  dataMap = new HashMap<>();
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

        // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
        TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                .field(PDU_ID);

        // 设置Avg指标聚合，按outlet_id分组
        TermsAggregationBuilder outletAggregationBuilder = AggregationBuilders.terms(BY_OUTLET).field(OUTLET_ID);
        // 设置聚合查询
        String top = "top";
        AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                .size(1).sort(field, sortOrder).sort(sortTime + KEYWORD,SortOrder.ASC);

        builder.aggregation(pduAggregationBuilder.subAggregation(outletAggregationBuilder.subAggregation(topAgg)));

        // 设置搜索条件
        searchRequest.source(builder);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 处理聚合查询结果
        Aggregations aggregations = searchResponse.getAggregations();
        // 根据by_pdu名字查询terms聚合结果
        Terms byPduAggregation = aggregations.get(BY_PDU);

        for (Terms.Bucket bucket : byPduAggregation.getBuckets()){
            Integer pduId = Integer.parseInt(String.valueOf(bucket.getKey()));

            Terms byOutletAggregation = bucket.getAggregations().get(BY_OUTLET);
            Map<Integer,String> outletDoMap = new HashMap<>();
            //获取按outlet_Id分组
            for (Terms.Bucket baseBucket : byOutletAggregation.getBuckets()) {
                TopHits tophits = baseBucket.getAggregations().get(top);
                SearchHits sophistsHits = tophits.getHits();
                SearchHit hit = sophistsHits.getHits()[0];

                outletDoMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())),hit.getSourceAsString());
            }
            dataMap.put(pduId,outletDoMap);
        }
        return dataMap;

    }
}
