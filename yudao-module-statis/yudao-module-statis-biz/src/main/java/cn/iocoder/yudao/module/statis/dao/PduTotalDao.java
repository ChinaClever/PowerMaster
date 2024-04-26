package cn.iocoder.yudao.module.statis.dao;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduTotalBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalRealtimeDo;
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
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.module.statis.constant.Constants.BY_PDU;
import static cn.iocoder.yudao.module.statis.constant.Constants.KEYWORD;

/**
 * @Author: chenwany
 * @Date: 2024/4/3 09:35
 * @Description: 总历史数据统计
 */
@Slf4j
@Component
public class PduTotalDao {

    @Autowired
    RestHighLevelClient client;


    /**
     * 总历史数据统计（按小时）
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Integer, PduTotalBaseDo> statisTotalHour(String startTime, String endTime) {
        Map<Integer, PduTotalBaseDo> result = new HashMap<>();
        try {
            String index = EsIndexEnum.PDU_HDA_TOTAL_REALTIME.getIndex();
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder
                    //平均有功功率
                    .subAggregation(AggregationBuilders.avg(ACTIVE_POW_AVG_VALUE).field(ACTIVE_POW))
                    //最大有功功率
                    .subAggregation(AggregationBuilders.max(ACTIVE_POW_MAX_VALUE).field(ACTIVE_POW))
                    // 最小有功功率
                    .subAggregation(AggregationBuilders.min(ACTIVE_POW_MIN_VALUE).field(ACTIVE_POW))
                    //平均视在功率
                    .subAggregation(AggregationBuilders.avg(APPARENT_POW_AVG_VALUE).field(APPARENT_POW))
                    //最大视在功率
                    .subAggregation(AggregationBuilders.max(APPARENT_POW_MAX_VALUE).field(APPARENT_POW))
                    //最小视在功率
                    .subAggregation(AggregationBuilders.min(APPARENT_POW_MIN_VALUE).field(APPARENT_POW)));

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
                PduTotalBaseDo baseDo = new PduTotalBaseDo();
                baseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                EsStatisFieldEnum.fields().forEach(field -> {

                    if (field.equals(ACTIVE_POW_AVG_VALUE)) {
                        Avg avg = bucket.getAggregations().get(field);
                        baseDo.setActivePowAvgValue(((Double) avg.getValue()).floatValue());
                    }
                    if (field.equals(ACTIVE_POW_MAX_VALUE)) {
                        Max max = bucket.getAggregations().get(field);
                        baseDo.setActivePowMaxValue(((Double) max.getValue()).floatValue());
                    }

                    if (field.equals(ACTIVE_POW_MIN_VALUE)) {
                        Min min = bucket.getAggregations().get(field);
                        baseDo.setActivePowMinValue(((Double) min.getValue()).floatValue());
                    }
                    if (field.equals(APPARENT_POW_AVG_VALUE)) {
                        Avg avg = bucket.getAggregations().get(field);
                        baseDo.setApparentPowAvgValue(((Double) avg.getValue()).floatValue());
                    }
                    if (field.equals(APPARENT_POW_MAX_VALUE)) {
                        Max max = bucket.getAggregations().get(field);
                        baseDo.setApparentPowMaxValue(((Double) max.getValue()).floatValue());
                    }

                    if (field.equals(APPARENT_POW_MIN_VALUE)) {
                        Min min = bucket.getAggregations().get(field);
                        baseDo.setApparentPowMinValue(((Double) min.getValue()).floatValue());
                    }

                });
                baseDo.setCreateTime(DateTime.now());
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), baseDo);
            }


            //获取时间
            Map<Integer,String> activePowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,ACTIVE_POW,index,CREATE_TIME);

            Map<Integer,String> activePowMinValueMap = getData(startTime,endTime,SortOrder.ASC,ACTIVE_POW,index,CREATE_TIME);

            Map<Integer,String> apparentPowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,APPARENT_POW,index,CREATE_TIME);

            Map<Integer,String> apparentPowMinValueMap = getData(startTime,endTime,SortOrder.ASC,APPARENT_POW,index,CREATE_TIME);

            result.keySet().forEach(pduId -> {
                PduTotalBaseDo fieldMap = result.get(pduId);

                PduHdaTotalRealtimeDo activePowMaxMap = JsonUtils.parseObject(activePowMaxValueMap.get(pduId),PduHdaTotalRealtimeDo.class) ;
                fieldMap.setActivePowMaxTime(activePowMaxMap.getCreateTime());
                fieldMap.setActivePowMaxValue(activePowMaxMap.getActivePow());

                PduHdaTotalRealtimeDo activePowMinMap = JsonUtils.parseObject(activePowMinValueMap.get(pduId),PduHdaTotalRealtimeDo.class) ;
                fieldMap.setActivePowMinTime(activePowMinMap.getCreateTime());
                fieldMap.setActivePowMinValue(activePowMinMap.getActivePow());

                PduHdaTotalRealtimeDo apparentPowMaxMap = JsonUtils.parseObject(apparentPowMaxValueMap.get(pduId),PduHdaTotalRealtimeDo.class) ;
                fieldMap.setApparentPowMaxValue(apparentPowMaxMap.getApparentPow());
                fieldMap.setApparentPowMaxTime(apparentPowMaxMap.getCreateTime());

                PduHdaTotalRealtimeDo apparentPowMinMap = JsonUtils.parseObject(apparentPowMinValueMap.get(pduId),PduHdaTotalRealtimeDo.class) ;
                fieldMap.setApparentPowMinTime(apparentPowMinMap.getCreateTime());
                fieldMap.setApparentPowMinValue(apparentPowMinMap.getApparentPow());

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
     * 总历史数据统计（按天）
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Integer, PduTotalBaseDo> statisTotalDay(String startTime, String endTime) {
        Map<Integer, PduTotalBaseDo> result = new HashMap<>();
        try {
            String index = EsIndexEnum.PDU_HDA_TOTAL_HOUR.getIndex();
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder
                    //平均有功功率
                    .subAggregation(AggregationBuilders.avg(ACTIVE_POW_AVG_VALUE).field(ACTIVE_POW_AVG_VALUE))
                    //平均视在功率
                    .subAggregation(AggregationBuilders.avg(APPARENT_POW_AVG_VALUE).field(APPARENT_POW_AVG_VALUE)));

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
                PduTotalBaseDo baseDo = new PduTotalBaseDo();
                baseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                EsStatisFieldEnum.fields().forEach(field -> {

                    if (field.equals(ACTIVE_POW_AVG_VALUE)) {
                        Avg avg = bucket.getAggregations().get(field);
                        baseDo.setActivePowAvgValue(((Double) avg.getValue()).floatValue());
                    }
                    if (field.equals(APPARENT_POW_AVG_VALUE)) {
                        Avg avg = bucket.getAggregations().get(field);
                        baseDo.setApparentPowAvgValue(((Double) avg.getValue()).floatValue());
                    }

                });
                baseDo.setCreateTime(DateTime.now());
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), baseDo);
            }

            //获取时间
            Map<Integer,String> activePowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,ACTIVE_POW_MAX_VALUE,index,ACTIVE_POW_MAX_TIME);

            Map<Integer,String> activePowMinValueMap = getData(startTime,endTime,SortOrder.ASC,ACTIVE_POW_MIN_VALUE,index,ACTIVE_POW_MIN_TIME);

            Map<Integer,String> apparentPowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,APPARENT_POW_MAX_VALUE,index,APPARENT_POW_MAX_TIME);

            Map<Integer,String> apparentPowMinValueMap = getData(startTime,endTime,SortOrder.ASC,APPARENT_POW_MIN_VALUE,index,APPARENT_POW_MIN_TIME);

            result.keySet().forEach(pduId -> {
                PduTotalBaseDo fieldMap = result.get(pduId);

                PduHdaTotalHourDo activePowMaxMap = JsonUtils.parseObject(activePowMaxValueMap.get(pduId),PduHdaTotalHourDo.class) ;
                fieldMap.setActivePowMaxTime(activePowMaxMap.getActivePowMaxTime());
                fieldMap.setActivePowMaxValue(activePowMaxMap.getActivePowMaxValue());

                PduHdaTotalHourDo activePowMinMap = JsonUtils.parseObject(activePowMinValueMap.get(pduId),PduHdaTotalHourDo.class) ;
                fieldMap.setActivePowMinTime(activePowMinMap.getActivePowMinTime());
                fieldMap.setActivePowMinValue(activePowMinMap.getActivePowMinValue());

                PduHdaTotalHourDo apparentPowMaxMap = JsonUtils.parseObject(apparentPowMaxValueMap.get(pduId),PduHdaTotalHourDo.class) ;
                fieldMap.setApparentPowMaxValue(apparentPowMaxMap.getApparentPowMaxValue());
                fieldMap.setApparentPowMaxTime(apparentPowMaxMap.getApparentPowMaxTime());

                PduHdaTotalHourDo apparentPowMinMap = JsonUtils.parseObject(apparentPowMinValueMap.get(pduId),PduHdaTotalHourDo.class) ;
                fieldMap.setApparentPowMinTime(apparentPowMinMap.getApparentPowMinTime());
                fieldMap.setApparentPowMinValue(apparentPowMinMap.getApparentPowMinValue());

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
    private Map<Integer,String> getData(String startTime, String endTime, SortOrder sortOrder,String field,String index,String sortTime) throws IOException {
        Map<Integer,String>  dataMap = new HashMap<>();
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

        // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
        TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                .field(PDU_ID);

        // 设置聚合查询
        String top = "top";
        AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                .size(1).sort(field, sortOrder).sort(sortTime + KEYWORD,SortOrder.ASC);

        builder.aggregation(pduAggregationBuilder.subAggregation(topAgg));

        // 设置搜索条件
        searchRequest.source(builder);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 处理聚合查询结果
        Aggregations aggregations = searchResponse.getAggregations();
        // 根据by_pdu名字查询terms聚合结果
        Terms byPduAggregation = aggregations.get(BY_PDU);

        for (Terms.Bucket bucket : byPduAggregation.getBuckets()){
            TopHits tophits = bucket.getAggregations().get(top);
            SearchHits sophistsHits = tophits.getHits();
            SearchHit hit = sophistsHits.getHits()[0];
//            log.info("hit.getSourceAsString(): " + hit.getSourceAsString());
            dataMap.put(Integer.parseInt(String.valueOf(bucket.getKey())),hit.getSourceAsString());
        }
        return dataMap;

    }
}
