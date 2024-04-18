package cn.iocoder.yudao.module.statis.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvRealtimeDo;
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
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(EsIndexEnum.PDU_ENV_REALTIME.getIndex());
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
                    //最大温度
                    .subAggregation(AggregationBuilders.max(TEM_MAX_VALUE).field(TEM))
                    //最小温度
                    .subAggregation(AggregationBuilders.min(TEM_MIN_VALUE).field(TEM))
                    //统计平均湿度
                    .subAggregation(AggregationBuilders.avg(HUM_AVG_VALUE).field(HUM))
                    //最大湿度
                    .subAggregation(AggregationBuilders.max(HUM_MAX_VALUE).field(HUM))
                    //最小湿度
                    .subAggregation(AggregationBuilders.min(HUM_MIN_VALUE).field(HUM))));

            // 设置搜索条件
            searchRequest.source(builder);
            // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
//            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //查询结果
            SearchHits hits = searchResponse.getHits();
            LinkedList<PduEnvRealtimeDo> resList = new LinkedList<>();

            for (SearchHit hit : hits.getHits()) {
                String str = hit.getSourceAsString();
                PduEnvRealtimeDo realtimeDo = JsonUtils.parseObject(str, PduEnvRealtimeDo.class);
                resList.add(realtimeDo);
            }


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
                        if (field.equals(TEM_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setTemMaxValue(((Double) max.getValue()).floatValue());
                        }

                        if (field.equals(TEM_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setTemMinValue(((Double) min.getValue()).floatValue());
                        }
                        if (field.equals(HUM_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setHumAvgValue(Integer.parseInt(String.valueOf(avg.getValue()) ));
                        }
                        if (field.equals(HUM_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setHumMaxValue(Integer.parseInt(String.valueOf(max.getValue())));
                        }

                        if (field.equals(HUM_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setHumMinValue(Integer.parseInt(String.valueOf(min.getValue())));
                        }

                    });
                    baseDo.setCreateTime(DateTime.now());
                    dataMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())), baseDo);
                }
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), dataMap);
            }

            //获取时间
            Map<Integer, List<PduEnvRealtimeDo>> realtimeDoMap = resList.stream().collect(Collectors.groupingBy(PduEnvRealtimeDo::getPduId));
            realtimeDoMap.keySet().forEach(pduId -> {
                List<PduEnvRealtimeDo> realtimeDos = realtimeDoMap.get(pduId);
                Map<Integer, List<PduEnvRealtimeDo>> map = realtimeDos.stream().collect(Collectors.groupingBy(PduEnvRealtimeDo::getSensorId));
                map.keySet().forEach(id -> {
                    List<PduEnvRealtimeDo> list = map.get(id);
                    Map<Integer, DateTime> humValueMap = list.stream().collect(Collectors
                            .toMap(PduEnvRealtimeDo::getHum,PduEnvRealtimeDo::getCreateTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));

                    Map<Float, DateTime> temValueMap = list.stream().collect(Collectors
                            .toMap(PduEnvRealtimeDo::getTem,PduEnvRealtimeDo::getCreateTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));

                    PduEnvBaseDo fieldMap = result.get(pduId).get(id);
                    fieldMap.setHumMaxTime(humValueMap.get(fieldMap.getHumMaxValue()));
                    fieldMap.setHumMinTime(humValueMap.get(fieldMap.getHumMinValue()));
                    fieldMap.setTemMaxTime(temValueMap.get(fieldMap.getTemMaxValue()));
                    fieldMap.setTemMinTime(temValueMap.get(fieldMap.getTemMinValue()));

                    result.get(pduId).put(id, fieldMap);
                });
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
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(EsIndexEnum.PDU_ENV_HOUR.getIndex());
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
                    //最大温度
                    .subAggregation(AggregationBuilders.max(TEM_MAX_VALUE).field(TEM_MAX_VALUE))
                    //最小温度
                    .subAggregation(AggregationBuilders.min(TEM_MIN_VALUE).field(TEM_MIN_VALUE))
                    //统计平均湿度
                    .subAggregation(AggregationBuilders.avg(HUM_AVG_VALUE).field(HUM_AVG_VALUE))
                    //最大湿度
                    .subAggregation(AggregationBuilders.max(HUM_MAX_VALUE).field(HUM_MAX_VALUE))
                    //最小湿度
                    .subAggregation(AggregationBuilders.min(HUM_MIN_VALUE).field(HUM_MIN_VALUE))));

            // 设置搜索条件
            searchRequest.source(builder);
            // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
//            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            //查询结果
            SearchHits hits = searchResponse.getHits();
            LinkedList<PduEnvHourDo> resList = new LinkedList<>();

            for (SearchHit hit : hits.getHits()) {
                String str = hit.getSourceAsString();
                PduEnvHourDo realtimeDo = JsonUtils.parseObject(str, PduEnvHourDo.class);
                resList.add(realtimeDo);
            }


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
                        if (field.equals(TEM_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setTemMaxValue(((Double) max.getValue()).floatValue());
                        }

                        if (field.equals(TEM_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setTemMinValue(((Double) min.getValue()).floatValue());
                        }
                        if (field.equals(HUM_AVG_VALUE)) {
                            Avg avg = baseBucket.getAggregations().get(field);
                            baseDo.setHumAvgValue(Integer.parseInt(String.valueOf(avg.getValue()) ));
                        }
                        if (field.equals(HUM_MAX_VALUE)) {
                            Max max = baseBucket.getAggregations().get(field);
                            baseDo.setHumMaxValue(Integer.parseInt(String.valueOf(max.getValue())));
                        }

                        if (field.equals(HUM_MIN_VALUE)) {
                            Min min = baseBucket.getAggregations().get(field);
                            baseDo.setHumMinValue(Integer.parseInt(String.valueOf(min.getValue())));
                        }

                    });
                    baseDo.setCreateTime(DateTime.now());
                    dataMap.put(Integer.parseInt(String.valueOf(baseBucket.getKey())), baseDo);
                }
                result.put(Integer.parseInt(String.valueOf(bucket.getKey())), dataMap);
            }

            //获取时间
            Map<Integer, List<PduEnvHourDo>> realtimeDoMap = resList.stream().collect(Collectors.groupingBy(PduEnvHourDo::getPduId));
            realtimeDoMap.keySet().forEach(pduId -> {
                List<PduEnvHourDo> realtimeDos = realtimeDoMap.get(pduId);
                Map<Integer, List<PduEnvHourDo>> map = realtimeDos.stream().collect(Collectors.groupingBy(PduEnvHourDo::getSensorId));
                map.keySet().forEach(id -> {
                    List<PduEnvHourDo> list = map.get(id);
                    Map<Integer, DateTime> humMaxValueMap = list.stream().collect(Collectors
                            .toMap(PduEnvHourDo::getHumMaxValue,PduEnvHourDo::getHumMaxTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));
                    Map<Integer, DateTime> humMinValueMap = list.stream().collect(Collectors
                            .toMap(PduEnvHourDo::getHumMinValue,PduEnvHourDo::getHumMinTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));

                    Map<Float, DateTime> temMaxValueMap = list.stream().collect(Collectors
                            .toMap(PduEnvHourDo::getTemMaxValue,PduEnvHourDo::getTemMaxTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));

                    Map<Float, DateTime> temMinValueMap = list.stream().collect(Collectors
                            .toMap(PduEnvHourDo::getTemMinValue,PduEnvHourDo::getTemMinTime,(v1,v2) -> {
                                if (DateUtil.compare(v1 ,v2) < 0) {
                                    return v1;
                                }
                                return v2;
                            }));
                    PduEnvBaseDo fieldMap = result.get(pduId).get(id);
                    fieldMap.setHumMaxTime(humMaxValueMap.get(fieldMap.getHumMaxValue()));
                    fieldMap.setHumMinTime(humMinValueMap.get(fieldMap.getHumMinValue()));
                    fieldMap.setTemMaxTime(temMaxValueMap.get(fieldMap.getTemMaxValue()));
                    fieldMap.setTemMinTime(temMinValueMap.get(fieldMap.getTemMinValue()));

                    result.get(pduId).put(id, fieldMap);

                });
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
     * @param value 值1
     * @param esValue 值2
     */
    private boolean equalsValue(float value, float esValue) {
        BigDecimal doubleValue = BigDecimal.valueOf(value);
        BigDecimal floatValue = BigDecimal.valueOf(esValue);

        return doubleValue.equals(floatValue);

    }
}
