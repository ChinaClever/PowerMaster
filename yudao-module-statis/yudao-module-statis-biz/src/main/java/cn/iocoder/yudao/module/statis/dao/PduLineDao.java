package cn.iocoder.yudao.module.statis.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.loop.PduHdaLoopBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.loop.PduHdaLoopHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.loop.PduHdaLoopRealtimeDo;
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
 * @Description: 相历史数据统计
 */
@Slf4j
@Component
public class PduLineDao {

    @Autowired
    RestHighLevelClient client;


    /**
     * 相历史数据统计(按小时)
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Integer, Map<Integer, PduHdaLineBaseDo>> statisLineHour(String startTime, String endTime) {
        Map<Integer, Map<Integer, PduHdaLineBaseDo>> result = new HashMap<>();
        try {
            String index = EsIndexEnum.PDU_HDA_LINE_REALTIME.getIndex();
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));


            //创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);

            // 设置Avg指标聚合，按line_id分组
            TermsAggregationBuilder lineAggregationBuilder = AggregationBuilders.terms(BY_LINE).field(LINE_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder.subAggregation(lineAggregationBuilder
                    //统计平均电压
                    .subAggregation(AggregationBuilders.avg(VOL_AVG_VALUE).field(VOL))
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
                Map<Integer, PduHdaLineBaseDo> dataMap = new HashMap<>();
                Terms byLineAggregation = bucket.getAggregations().get(BY_LINE);
                //获取按line_id分组
                for (Terms.Bucket baseBucket : byLineAggregation.getBuckets()) {
                    PduHdaLineBaseDo lineBaseDo = new PduHdaLineBaseDo();
                    lineBaseDo.setLineId(Integer.parseInt(String.valueOf(baseBucket.getKey())));
                    lineBaseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                    EsStatisFieldEnum.fields().forEach(field -> {
                        if (field.equals(VOL_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            lineBaseDo.setVolAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(CUR_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            lineBaseDo.setCurAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(ACTIVE_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            lineBaseDo.setActivePowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(APPARENT_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            lineBaseDo.setApparentPowAvgValue(((Double) avg.getValue()).floatValue());
                        }

                    });
                    lineBaseDo.setCreateTime(DateTime.now());
                    dataMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())), lineBaseDo);
                }
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), dataMap);
            }

            //获取时间
            Map<Integer,Map<Integer,String>> volMaxValueMap = getData(startTime,endTime,SortOrder.DESC,VOL,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>> volMinValueMap = getData(startTime,endTime,SortOrder.ASC,VOL,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>> curMaxValueMap = getData(startTime,endTime,SortOrder.DESC,CUR,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>> curMinValueMap = getData(startTime,endTime,SortOrder.ASC,CUR,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>> activePowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,ACTIVE_POW,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>>  activePowMinValueMap = getData(startTime,endTime,SortOrder.ASC,ACTIVE_POW,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>>  apparentPowMaxValueMap = getData(startTime,endTime,SortOrder.DESC,APPARENT_POW,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>>  apparentPowMinValueMap = getData(startTime,endTime,SortOrder.ASC,APPARENT_POW,index,CREATE_TIME);

            result.keySet().forEach(pduId -> {
                Map<Integer, PduHdaLineBaseDo> fieldMap = result.get(pduId);

                fieldMap.keySet().forEach(id->{
                    PduHdaLineBaseDo baseDo = fieldMap.get(id);

                    PduHdaLineRealtimeDo volMaxMap = JsonUtils.parseObject(volMaxValueMap.get(pduId).get(id),PduHdaLineRealtimeDo.class) ;
                    baseDo.setVolMaxTime(volMaxMap.getCreateTime());
                    baseDo.setVolMaxValue(volMaxMap.getVol());

                    PduHdaLineRealtimeDo volMinMap = JsonUtils.parseObject(volMinValueMap.get(pduId).get(id),PduHdaLineRealtimeDo.class) ;
                    baseDo.setVolMinTime(volMinMap.getCreateTime());
                    baseDo.setVolMinValue(volMinMap.getVol());

                    PduHdaLineRealtimeDo curMaxMap = JsonUtils.parseObject(curMaxValueMap.get(pduId).get(id),PduHdaLineRealtimeDo.class) ;
                    baseDo.setCurMaxTime(curMaxMap.getCreateTime());
                    baseDo.setCurMaxValue(curMaxMap.getCur());

                    PduHdaLineRealtimeDo curMinMap = JsonUtils.parseObject(curMinValueMap.get(pduId).get(id),PduHdaLineRealtimeDo.class) ;
                    baseDo.setCurMinTime(curMinMap.getCreateTime());
                    baseDo.setCurMinValue(curMinMap.getCur());

                    PduHdaLineRealtimeDo activePowMaxMap = JsonUtils.parseObject(activePowMaxValueMap.get(pduId).get(id),PduHdaLineRealtimeDo.class) ;
                    baseDo.setActivePowMaxTime(activePowMaxMap.getCreateTime());
                    baseDo.setActivePowMaxValue(activePowMaxMap.getActivePow());

                    PduHdaLineRealtimeDo activePowMinMap = JsonUtils.parseObject(activePowMinValueMap.get(pduId).get(id),PduHdaLineRealtimeDo.class) ;
                    baseDo.setActivePowMinTime(activePowMinMap.getCreateTime());
                    baseDo.setActivePowMinValue(activePowMinMap.getActivePow());

                    PduHdaLineRealtimeDo apparentPowMaxMap = JsonUtils.parseObject(apparentPowMaxValueMap.get(pduId).get(id),PduHdaLineRealtimeDo.class) ;
                    baseDo.setApparentPowMaxValue(apparentPowMaxMap.getApparentPow());
                    baseDo.setApparentPowMaxTime(apparentPowMaxMap.getCreateTime());

                    PduHdaLineRealtimeDo apparentPowMinMap = JsonUtils.parseObject(apparentPowMinValueMap.get(pduId).get(id),PduHdaLineRealtimeDo.class) ;
                    baseDo.setApparentPowMinTime(apparentPowMinMap.getCreateTime());
                    baseDo.setApparentPowMinValue(apparentPowMinMap.getApparentPow());
                    fieldMap.put(id,baseDo);
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
     * 相历史数据统计(按天)
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Integer, Map<Integer, PduHdaLineBaseDo>> statisLineDay(String startTime, String endTime) {
        Map<Integer, Map<Integer, PduHdaLineBaseDo>> result = new HashMap<>();
        try {
            String index = EsIndexEnum.PDU_HDA_LINE_HOUR.getIndex();
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

            //创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);

            // 设置Avg指标聚合，按line_id分组
            TermsAggregationBuilder lineAggregationBuilder = AggregationBuilders.terms(BY_LINE).field(LINE_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder.subAggregation(lineAggregationBuilder
                    //统计平均电压
                    .subAggregation(AggregationBuilders.avg(VOL_AVG_VALUE).field(VOL_AVG_VALUE))
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
                Map<Integer, PduHdaLineBaseDo> dataMap = new HashMap<>();
                Terms byLineAggregation = bucket.getAggregations().get(BY_LINE);
                //获取按line_id分组
                for (Terms.Bucket baseBucket : byLineAggregation.getBuckets()) {
                    PduHdaLineBaseDo lineBaseDo = new PduHdaLineBaseDo();
                    lineBaseDo.setLineId(Integer.parseInt(String.valueOf(baseBucket.getKey())));
                    lineBaseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                    EsStatisFieldEnum.fields().forEach(field -> {
                        if (field.equals(VOL_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            lineBaseDo.setVolAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(CUR_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            lineBaseDo.setCurAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(ACTIVE_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            lineBaseDo.setActivePowAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(APPARENT_POW_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            lineBaseDo.setApparentPowAvgValue(((Double) avg.getValue()).floatValue());
                        }

                    });
                    lineBaseDo.setCreateTime(DateTime.now());
                    dataMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())), lineBaseDo);
                }
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), dataMap);
            }

            //获取时间
            Map<Integer, Map<Integer, String>> volMaxValueMap = getData(startTime, endTime, SortOrder.DESC, VOL_MAX_VALUE, index,VOL_MAX_TIME);

            Map<Integer, Map<Integer, String>> volMinValueMap = getData(startTime, endTime, SortOrder.ASC, VOL_MIN_VALUE, index,VOL_MIN_TIME);

            Map<Integer, Map<Integer, String>> curMaxValueMap = getData(startTime, endTime, SortOrder.DESC, CUR_MAX_VALUE, index,CUR_MAX_TIME);

            Map<Integer, Map<Integer, String>> curMinValueMap = getData(startTime, endTime, SortOrder.ASC, CUR_MIN_VALUE, index,CUR_MIN_TIME);

            Map<Integer, Map<Integer, String>> activePowMaxValueMap = getData(startTime, endTime, SortOrder.DESC, ACTIVE_POW_MAX_VALUE, index,ACTIVE_POW_MAX_TIME);

            Map<Integer, Map<Integer, String>> activePowMinValueMap = getData(startTime, endTime, SortOrder.ASC, ACTIVE_POW_MIN_VALUE, index,ACTIVE_POW_MIN_TIME);

            Map<Integer, Map<Integer, String>> apparentPowMaxValueMap = getData(startTime, endTime, SortOrder.DESC, APPARENT_POW_MAX_VALUE, index,APPARENT_POW_MAX_TIME);

            Map<Integer, Map<Integer, String>> apparentPowMinValueMap = getData(startTime, endTime, SortOrder.ASC, APPARENT_POW_MIN_VALUE, index,APPARENT_POW_MIN_TIME);

            result.keySet().forEach(pduId -> {
                Map<Integer, PduHdaLineBaseDo> fieldMap = result.get(pduId);

                fieldMap.keySet().forEach(id -> {
                    PduHdaLineBaseDo baseDo = fieldMap.get(id);

                    PduHdaLineHourDo volMaxMap = JsonUtils.parseObject(volMaxValueMap.get(pduId).get(id), PduHdaLineHourDo.class);
                    baseDo.setVolMaxTime(volMaxMap.getVolMaxTime());
                    baseDo.setVolMaxValue(volMaxMap.getVolMaxValue());

                    PduHdaLineHourDo volMinMap = JsonUtils.parseObject(volMinValueMap.get(pduId).get(id), PduHdaLineHourDo.class);
                    baseDo.setVolMinTime(volMinMap.getVolMinTime());
                    baseDo.setVolMinValue(volMinMap.getVolMinValue());

                    PduHdaLineHourDo curMaxMap = JsonUtils.parseObject(curMaxValueMap.get(pduId).get(id), PduHdaLineHourDo.class);
                    baseDo.setCurMaxTime(curMaxMap.getCurMaxTime());
                    baseDo.setCurMaxValue(curMaxMap.getCurMaxValue());

                    PduHdaLineHourDo curMinMap = JsonUtils.parseObject(curMinValueMap.get(pduId).get(id), PduHdaLineHourDo.class);
                    baseDo.setCurMinTime(curMinMap.getCurMinTime());
                    baseDo.setCurMinValue(curMinMap.getCurMinValue());

                    PduHdaLineHourDo activePowMaxMap = JsonUtils.parseObject(activePowMaxValueMap.get(pduId).get(id), PduHdaLineHourDo.class);
                    baseDo.setActivePowMaxTime(activePowMaxMap.getActivePowMaxTime());
                    baseDo.setActivePowMaxValue(activePowMaxMap.getActivePowMaxValue());

                    PduHdaLineHourDo activePowMinMap = JsonUtils.parseObject(activePowMinValueMap.get(pduId).get(id), PduHdaLineHourDo.class);
                    baseDo.setActivePowMinTime(activePowMinMap.getActivePowMinTime());
                    baseDo.setActivePowMinValue(activePowMinMap.getActivePowMinValue());

                    PduHdaLineHourDo apparentPowMaxMap = JsonUtils.parseObject(apparentPowMaxValueMap.get(pduId).get(id), PduHdaLineHourDo.class);
                    baseDo.setApparentPowMaxValue(apparentPowMaxMap.getApparentPowMaxValue());
                    baseDo.setApparentPowMaxTime(apparentPowMaxMap.getApparentPowMaxTime());

                    PduHdaLineHourDo apparentPowMinMap = JsonUtils.parseObject(apparentPowMinValueMap.get(pduId).get(id), PduHdaLineHourDo.class);
                    baseDo.setApparentPowMinTime(apparentPowMinMap.getApparentPowMinTime());
                    baseDo.setApparentPowMinValue(apparentPowMinMap.getApparentPowMinValue());
                    fieldMap.put(id, baseDo);
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

        // 设置Avg指标聚合
        TermsAggregationBuilder lineAggregationBuilder = AggregationBuilders.terms(BY_LINE).field(LINE_ID);
        // 设置聚合查询
        String top = "top";
        AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                .size(1).sort(field, sortOrder).sort(sortTime + KEYWORD,SortOrder.ASC);

        builder.aggregation(pduAggregationBuilder.subAggregation(lineAggregationBuilder.subAggregation(topAgg)));

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

            Terms byLinetAggregation = bucket.getAggregations().get(BY_LINE);
            Map<Integer,String> doMap = new HashMap<>();
            //获取按line_Id分组
            for (Terms.Bucket baseBucket : byLinetAggregation.getBuckets()) {
                TopHits tophits = baseBucket.getAggregations().get(top);
                SearchHits sophistsHits = tophits.getHits();
                SearchHit hit = sophistsHits.getHits()[0];

                doMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())),hit.getSourceAsString());
            }
            dataMap.put(pduId,doMap);
        }
        return dataMap;

    }
}
