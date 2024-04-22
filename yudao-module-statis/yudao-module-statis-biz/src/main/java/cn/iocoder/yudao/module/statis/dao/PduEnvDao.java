package cn.iocoder.yudao.module.statis.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineHourDo;
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
 * @Description: 环境数据统计
 */
@Slf4j
@Component
public class PduEnvDao {

    @Autowired
    RestHighLevelClient client;


    /**
     * 环境数据统计(按小时)
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Integer, Map<Integer, PduEnvBaseDo>> statisHour(String startTime, String endTime) {
        Map<Integer, Map<Integer, PduEnvBaseDo>> result = new HashMap<>();
        try {
            String index = EsIndexEnum.PDU_ENV_REALTIME.getIndex();
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));

            //创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);

            // 设置Avg指标聚合，按sensor_id分组
            TermsAggregationBuilder envAggregationBuilder = AggregationBuilders.terms(BY_SENSOR).field(SENSOR_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder.subAggregation(envAggregationBuilder
                    //统计平均温度
                    .subAggregation(AggregationBuilders.avg(TEM_AVG_VALUE).field(TEM))
                    //统计平均湿度
                    .subAggregation(AggregationBuilders.avg(HUM_AVG_VALUE).field(HUM))));

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
                Map<Integer, PduEnvBaseDo> dataMap = new HashMap<>();
                Terms byEnvAggregation = bucket.getAggregations().get(BY_SENSOR);
                //获取按sensor_id分组
                for (Terms.Bucket baseBucket : byEnvAggregation.getBuckets()) {
                    PduEnvBaseDo baseDo = new PduEnvBaseDo();
                    baseDo.setSensorId(Integer.parseInt(String.valueOf(baseBucket.getKey())));
                    baseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                    EsStatisFieldEnum.fields().forEach(field -> {
                        if (field.equals(TEM_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setTemAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(HUM_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setHumAvgValue(Integer.parseInt(String.valueOf(avg.getValue()) ));
                        }

                    });
                    baseDo.setCreateTime(DateTime.now());
                    dataMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())), baseDo);
                }
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), dataMap);
            }

            //获取时间
            Map<Integer,Map<Integer,String>> temMaxValueMap = getData(startTime,endTime,SortOrder.DESC,TEM,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>> temMinValueMap = getData(startTime,endTime,SortOrder.ASC,TEM,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>> humMaxValueMap = getData(startTime,endTime,SortOrder.DESC,HUM,index,CREATE_TIME);

            Map<Integer,Map<Integer,String>> humMinValueMap = getData(startTime,endTime,SortOrder.ASC,HUM,index,CREATE_TIME);

            result.keySet().forEach(pduId -> {
                Map<Integer, PduEnvBaseDo> fieldMap = result.get(pduId);

                fieldMap.keySet().forEach(id->{
                    PduEnvBaseDo baseDo = fieldMap.get(id);

                    PduEnvRealtimeDo temMaxMap = JsonUtils.parseObject(temMaxValueMap.get(pduId).get(id),PduEnvRealtimeDo.class) ;
                    baseDo.setTemMaxValue(temMaxMap.getTem());
                    baseDo.setTemMaxTime(temMaxMap.getCreateTime());

                    PduEnvRealtimeDo temlMinMap = JsonUtils.parseObject(temMinValueMap.get(pduId).get(id),PduEnvRealtimeDo.class) ;
                    baseDo.setTemMinValue(temlMinMap.getTem());
                    baseDo.setTemMinTime(temlMinMap.getCreateTime());

                    PduEnvRealtimeDo humMaxMap = JsonUtils.parseObject(humMaxValueMap.get(pduId).get(id),PduEnvRealtimeDo.class) ;
                    baseDo.setHumMaxValue(humMaxMap.getHum());
                    baseDo.setHumMaxTime(humMaxMap.getCreateTime());

                    PduEnvRealtimeDo humMinMap = JsonUtils.parseObject(humMinValueMap.get(pduId).get(id),PduEnvRealtimeDo.class) ;
                    baseDo.setHumMinValue(humMinMap.getHum());
                    baseDo.setHumMinTime(humMinMap.getCreateTime());

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
     * 环境数据统计(按天)
     *
     * @param startTime 统计开始时间
     * @param endTime   统计结束时间
     */
    public Map<Integer, Map<Integer, PduEnvBaseDo>> statisDay(String startTime, String endTime) {
        Map<Integer, Map<Integer, PduEnvBaseDo>> result = new HashMap<>();
        try {
            String index = EsIndexEnum.PDU_ENV_HOUR.getIndex();
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));


            //创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);

            // 设置Avg指标聚合，按sensor_id分组
            TermsAggregationBuilder envAggregationBuilder = AggregationBuilders.terms(BY_SENSOR).field(SENSOR_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder.subAggregation(envAggregationBuilder
                    //统计平均温度
                    .subAggregation(AggregationBuilders.avg(TEM_AVG_VALUE).field(TEM_AVG_VALUE))
                    //统计平均湿度
                    .subAggregation(AggregationBuilders.avg(HUM_AVG_VALUE).field(HUM_AVG_VALUE))));

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
                Map<Integer, PduEnvBaseDo> dataMap = new HashMap<>();
                Terms byEnvAggregation = bucket.getAggregations().get(BY_SENSOR);
                //获取按sensor_id分组
                for (Terms.Bucket baseBucket : byEnvAggregation.getBuckets()) {
                    PduEnvBaseDo baseDo = new PduEnvBaseDo();
                    baseDo.setSensorId(Integer.parseInt(String.valueOf(baseBucket.getKey())));
                    baseDo.setPduId(Integer.parseInt(String.valueOf(bucket.getKey())));
                    EsStatisFieldEnum.fields().forEach(field -> {
                        if (field.equals(TEM_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setTemAvgValue(((Double) avg.getValue()).floatValue());
                        }
                        if (field.equals(HUM_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setHumAvgValue(Integer.parseInt(String.valueOf(avg.getValue()) ));
                        }

                    });
                    baseDo.setCreateTime(DateTime.now());
                    dataMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())), baseDo);
                }
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), dataMap);
            }

            //获取时间
            Map<Integer,Map<Integer,String>> temMaxValueMap = getData(startTime,endTime,SortOrder.DESC,TEM_MAX_VALUE,index,TEM_MAX_TIME);

            Map<Integer,Map<Integer,String>> temMinValueMap = getData(startTime,endTime,SortOrder.ASC,TEM_MIN_VALUE,index,TEM_MIN_TIME);

            Map<Integer,Map<Integer,String>> humMaxValueMap = getData(startTime,endTime,SortOrder.DESC,HUM_MAX_VALUE,index,HUM_MAX_TIME);

            Map<Integer,Map<Integer,String>> humMinValueMap = getData(startTime,endTime,SortOrder.ASC,HUM_MIN_VALUE,index,HUM_MIN_TIME);

            result.keySet().forEach(pduId -> {
                Map<Integer, PduEnvBaseDo> fieldMap = result.get(pduId);

                fieldMap.keySet().forEach(id->{
                    PduEnvBaseDo baseDo = fieldMap.get(id);

                    PduEnvHourDo temMaxMap = JsonUtils.parseObject(temMaxValueMap.get(pduId).get(id),PduEnvHourDo.class) ;
                    baseDo.setTemMaxValue(temMaxMap.getTemMaxValue());
                    baseDo.setTemMaxTime(temMaxMap.getTemMaxTime());

                    PduEnvHourDo temlMinMap = JsonUtils.parseObject(temMinValueMap.get(pduId).get(id),PduEnvHourDo.class) ;
                    baseDo.setTemMinValue(temlMinMap.getTemMinValue());
                    baseDo.setTemMinTime(temlMinMap.getTemMinTime());

                    PduEnvHourDo humMaxMap = JsonUtils.parseObject(humMaxValueMap.get(pduId).get(id),PduEnvHourDo.class) ;
                    baseDo.setHumMaxValue(humMaxMap.getHumMaxValue());
                    baseDo.setHumMaxTime(humMaxMap.getHumMaxTime());

                    PduEnvHourDo humMinMap = JsonUtils.parseObject(humMinValueMap.get(pduId).get(id),PduEnvHourDo.class) ;
                    baseDo.setHumMinValue(humMinMap.getHumMinValue());
                    baseDo.setHumMinTime(humMinMap.getHumMinTime());

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
        TermsAggregationBuilder sensorAggregationBuilder = AggregationBuilders.terms(BY_SENSOR).field(SENSOR_ID);
        // 设置聚合查询
        String top = "top";
        AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                .size(1).sort(field, sortOrder).sort(sortTime + KEYWORD,SortOrder.ASC);

        builder.aggregation(pduAggregationBuilder.subAggregation(sensorAggregationBuilder.subAggregation(topAgg)));

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

            Terms bySensorAggregation = bucket.getAggregations().get(BY_SENSOR);
            Map<Integer,String> doMap = new HashMap<>();
            //获取按SENSOR_ID分组
            for (Terms.Bucket baseBucket : bySensorAggregation.getBuckets()) {
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
