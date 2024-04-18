package cn.iocoder.yudao.module.statis.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduBaseDo;
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
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    public Map<Integer, PduBaseDo> statisTotalHour(String startTime, String endTime) {
        Map<Integer, PduBaseDo> result = new HashMap<>();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(EsIndexEnum.PDU_HDA_TOTAL_REALTIME.getIndex());
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

//            builder.query(QueryBuilders.matchAllQuery());
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
//            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //查询结果
            SearchHits hits = searchResponse.getHits();
            LinkedList<PduHdaTotalRealtimeDo> resList = new LinkedList<>();

            for (SearchHit hit : hits.getHits()) {
                String str = hit.getSourceAsString();
                PduHdaTotalRealtimeDo realtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                resList.add(realtimeDo);
            }


            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();
            // 根据by_pdu名字查询terms聚合结果
            Terms byPduAggregation = aggregations.get(BY_PDU);


            // 遍历terms聚合结果
            for (Terms.Bucket bucket : byPduAggregation.getBuckets()) {
                // 获取按pduId分组
                PduBaseDo baseDo = new PduBaseDo();
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
            Map<Integer, List<PduHdaTotalRealtimeDo>> realtimeDoMap = resList.stream().collect(Collectors.groupingBy(PduHdaTotalRealtimeDo::getPduId));
            realtimeDoMap.keySet().forEach(pduId -> {
                List<PduHdaTotalRealtimeDo> realtimeDos = realtimeDoMap.get(pduId);
                PduBaseDo fieldMap = result.get(pduId);

                Map<Float, DateTime> activePowMap = realtimeDos.stream().collect(Collectors
                        .toMap(PduHdaTotalRealtimeDo::getActivePow,PduHdaTotalRealtimeDo::getCreateTime,(v1,v2) -> {
                            if (DateUtil.compare(v1 ,v2) < 0) {
                                return v1;
                            }
                            return v2;
                        }));

                Map<Float, DateTime> apparentPowMap = realtimeDos.stream().collect(Collectors
                        .toMap(PduHdaTotalRealtimeDo::getApparentPow,PduHdaTotalRealtimeDo::getCreateTime,(v1,v2) -> {
                            if (DateUtil.compare(v1 ,v2) < 0) {
                                return v1;
                            }
                            return v2;
                        }));
                fieldMap.setActivePowMaxTime(activePowMap.get(fieldMap.getActivePowMaxValue()));
                fieldMap.setActivePowMinTime(activePowMap.get(fieldMap.getActivePowMinValue()));
                fieldMap.setApparentPowMaxTime(apparentPowMap.get(fieldMap.getApparentPowMaxValue()));
                fieldMap.setApparentPowMinTime(apparentPowMap.get(fieldMap.getActivePowMinValue()));
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
    public Map<Integer, PduBaseDo> statisTotalDay(String startTime, String endTime) {
        Map<Integer, PduBaseDo> result = new HashMap<>();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(EsIndexEnum.PDU_HDA_TOTAL_HOUR.getIndex());
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

//            builder.query(QueryBuilders.matchAllQuery());
            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder
                    //平均有功功率
                    .subAggregation(AggregationBuilders.avg(ACTIVE_POW_AVG_VALUE).field(ACTIVE_POW_AVG_VALUE))
                    //最大有功功率
                    .subAggregation(AggregationBuilders.max(ACTIVE_POW_MAX_VALUE).field(ACTIVE_POW_MAX_VALUE))
                    // 最小有功功率
                    .subAggregation(AggregationBuilders.min(ACTIVE_POW_MIN_VALUE).field(ACTIVE_POW_MIN_VALUE))
                    //平均视在功率
                    .subAggregation(AggregationBuilders.avg(APPARENT_POW_AVG_VALUE).field(APPARENT_POW_AVG_VALUE))
                    //最大视在功率
                    .subAggregation(AggregationBuilders.max(APPARENT_POW_MAX_VALUE).field(APPARENT_POW_MAX_VALUE))
                    //最小视在功率
                    .subAggregation(AggregationBuilders.min(APPARENT_POW_MIN_VALUE).field(APPARENT_POW_MIN_VALUE)));

            // 设置搜索条件
            searchRequest.source(builder);
            // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
//            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //查询结果
            SearchHits hits = searchResponse.getHits();
            LinkedList<PduHdaTotalHourDo> resList = new LinkedList<>();

            for (SearchHit hit : hits.getHits()) {
                String str = hit.getSourceAsString();
                PduHdaTotalHourDo realtimeDo = JsonUtils.parseObject(str, PduHdaTotalHourDo.class);
                resList.add(realtimeDo);
            }


            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();
            // 根据by_pdu名字查询terms聚合结果
            Terms byPduAggregation = aggregations.get(BY_PDU);


            // 遍历terms聚合结果
            for (Terms.Bucket bucket : byPduAggregation.getBuckets()) {
                // 获取按pduId分组
                PduBaseDo baseDo = new PduBaseDo();
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
            Map<Integer, List<PduHdaTotalHourDo>> realtimeDoMap = resList.stream().collect(Collectors.groupingBy(PduHdaTotalHourDo::getPduId));
            realtimeDoMap.keySet().forEach(pduId -> {
                List<PduHdaTotalHourDo> realtimeDos = realtimeDoMap.get(pduId);

                PduBaseDo fieldMap = result.get(pduId);

                Map<Float, DateTime> activePowMaxMap = realtimeDos.stream().collect(Collectors
                        .toMap(PduHdaTotalHourDo::getActivePowMaxValue,PduHdaTotalHourDo::getActivePowMaxTime,(v1,v2) -> {
                            if (DateUtil.compare(v1 ,v2) < 0) {
                                return v1;
                            }
                            return v2;
                        }));
                Map<Float, DateTime> activePowMinMap = realtimeDos.stream().collect(Collectors
                        .toMap(PduHdaTotalHourDo::getActivePowMinValue,PduHdaTotalHourDo::getActivePowMinTime,(v1,v2) -> {
                            if (DateUtil.compare(v1 ,v2) < 0) {
                                return v1;
                            }
                            return v2;
                        }));

                Map<Float, DateTime> apparentPowMaxMap = realtimeDos.stream().collect(Collectors
                        .toMap(PduHdaTotalHourDo::getApparentPowMaxValue,PduHdaTotalHourDo::getApparentPowMaxTime,(v1,v2) -> {
                            if (DateUtil.compare(v1 ,v2) < 0) {
                                return v1;
                            }
                            return v2;
                        }));
                Map<Float, DateTime> apparentPowMinMap = realtimeDos.stream().collect(Collectors
                        .toMap(PduHdaTotalHourDo::getApparentPowMinValue,PduHdaTotalHourDo::getApparentPowMinTime,(v1,v2) -> {
                            if (DateUtil.compare(v1 ,v2) < 0) {
                                return v1;
                            }
                            return v2;
                        }));
                fieldMap.setActivePowMaxTime(activePowMaxMap.get(fieldMap.getActivePowMaxValue()));
                fieldMap.setActivePowMinTime(activePowMinMap.get(fieldMap.getActivePowMinValue()));
                fieldMap.setApparentPowMaxTime(apparentPowMaxMap.get(fieldMap.getApparentPowMaxValue()));
                fieldMap.setApparentPowMinTime(apparentPowMinMap.get(fieldMap.getActivePowMinValue()));

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
     * 判断两个值是否相等
     *
     * @param value
     * @param esValue
     */
    private boolean equalsValue(float value, float esValue) {
        BigDecimal doubleValue = BigDecimal.valueOf(value);
        BigDecimal floatValue = BigDecimal.valueOf(esValue);

        if (doubleValue.equals(floatValue)) {
            return true;
        }

        return false;

    }
}
