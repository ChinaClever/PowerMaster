package cn.iocoder.yudao.module.statis.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.PduEqBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.enums.EsIndexEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.statis.vo.EqBillConfigVo;
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
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.module.statis.constant.Constants.*;

/**
 * @Author: chenwany
 * @Date: 2024/4/9 09:52
 * @Description:  总电量计算
 */
@Slf4j
@Component
public class PduEleTotalDao {

    @Autowired
    RestHighLevelClient client;

    /**
     *  电费计算 (按天统计)
     * @param billConfigVoList 电费计算配置
     */
    public  List<PduEqBaseDo> statisEleDay(List<EqBillConfigVo> billConfigVoList){
        List<PduEqBaseDo> list = new ArrayList<>();

        try {
            billConfigVoList.forEach(billConfigVo -> {
                String[] times = billConfigVo.getBillPeriod().split(SPLIT);
                String startTime = DateUtil.formatDate(DateTime.now()) +" " + times[0];
                String endTime = DateUtil.formatDate(DateTime.now()) +" " + times[1];

                billConfigVo.setStartTime(startTime);
                billConfigVo.setEndTime(endTime);

                //获取时间段内第一条和最后一条数据
                Map<Integer, PduEleTotalRealtimeDo> endMap = getEleData(billConfigVo,
                        SortOrder.DESC,
                        EsIndexEnum.PDU_ELE_TOTAL_REALTIME.getIndex());
                Map<Integer, PduEleTotalRealtimeDo> startMap = getEleData(billConfigVo,
                        SortOrder.ASC,
                        EsIndexEnum.PDU_ELE_TOTAL_REALTIME.getIndex());
                endMap.keySet().forEach(pduId ->{
                    PduEqBaseDo dayDo = new PduEqBaseDo();
                    //统计时间段
                    dayDo.setStartTime(DateUtil.parseDateTime(billConfigVo.getStartTime()));
                    dayDo.setEndTime(DateUtil.parseDateTime(billConfigVo.getEndTime()));
                    dayDo.setPduId(pduId);

                    PduEleTotalRealtimeDo endRealtimeDo = endMap.get(pduId);
                    PduEleTotalRealtimeDo startRealtimeDo = startMap.get(pduId);
                    //结束时间电量
                    double endEle = endRealtimeDo.getEle();
                    dayDo.setEndEle(endEle);
                    //开始时间电量
                    double startEle = startRealtimeDo.getEle();
                    dayDo.setStartEle(startEle);
                    //判断使用电量  开始电量大于结束电量，电量有清零操作，以结束电量为准
                    double eq ;
                    if (startEle>endEle){
                        eq = endEle;
                    }else {
                        eq = endEle - startEle;
                    }
                    dayDo.setEq(eq);
                    //电费计算 电量*该时间段计费
                    double bill = billConfigVo.getBill() * eq;
                    dayDo.setBill(bill);
                    dayDo.setCreateTime(DateTime.now());
                    dayDo.setBillPeriod(billConfigVo.getBillPeriod());
                    dayDo.setBillMode(billConfigVo.getBillMode());

                    list.add(dayDo);
                });

            });

            return list;
        }catch (Exception e){
            log.error("计算异常：",e);
        }
        return list;
    }
    

    /**
     * @description:  获取ES中数据
      * @param configVo 时间段配置
     * @param sortOrder 排序
     * @param index  索引名称
     * @return Map<Integer,PduEleTotalRealtimeDo>
     * @author luowei
     * @date: 2024/4/10 10:46
     */
    private Map<Integer, PduEleTotalRealtimeDo> getEleData(EqBillConfigVo configVo,SortOrder sortOrder,String index){
        Map<Integer, PduEleTotalRealtimeDo> dataMap = new HashMap<>();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD)
                    .gte(configVo.getStartTime())
                    .lt(configVo.getEndTime()));

            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);
            // 嵌套聚合
            // 设置聚合查询
            String top = "top";
            AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                    .size(1).sort(CREATE_TIME +KEYWORD, sortOrder);

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
                SearchHits tophitsHits = tophits.getHits();
                SearchHit hit = tophitsHits.getHits()[0];
                PduEleTotalRealtimeDo realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEleTotalRealtimeDo.class);
                dataMap.put(Integer.parseInt(String.valueOf(bucket.getKey())),realtimeDo);
            }
            return dataMap;
        }catch (Exception e){
            log.error("获取数据异常：",e);
        }
       return dataMap;
    }



    /**
     *  电费计算 (按周统计)
     * @param billConfigVoList 电费计算配置
     */
    public  List<PduEqBaseDo> statisEleWeek(List<EqBillConfigVo> billConfigVoList){
        List<PduEqBaseDo> list = new ArrayList<>();

        try {
            billConfigVoList.forEach(billConfigVo -> {
                String[] times = billConfigVo.getBillPeriod().split(SPLIT);
                String startTime = DateUtil.formatDate(DateUtil.beginOfWeek(DateTime.now())) +" " + times[0];
                String endTime = DateUtil.formatDate(DateTime.now()) +" " + times[1];

                billConfigVo.setStartTime(startTime);
                billConfigVo.setEndTime(endTime);

                //获取时间段内第一条和最后一条数据
                Map<Integer, PduEleTotalRealtimeDo> endMap = getEleData(billConfigVo,
                        SortOrder.DESC,
                        EsIndexEnum.PDU_ELE_TOTAL_REALTIME.getIndex());
                Map<Integer, PduEleTotalRealtimeDo> startMap = getEleData(billConfigVo,
                        SortOrder.ASC,
                        EsIndexEnum.PDU_ELE_TOTAL_REALTIME.getIndex());

                startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
                endTime = DateUtil.formatDateTime(DateTime.now());

                billConfigVo.setStartTime(startTime);
                billConfigVo.setEndTime(endTime);
                //计算总量
                Map<Integer, Map<String,Double>> dataMap = getEleDataByDay(billConfigVo,
                        EsIndexEnum.PDU_EQ_TOTAL_DAY.getIndex());

                endMap.keySet().forEach(pduId ->{
                    PduEqBaseDo baseDo = new PduEqBaseDo();
                    //统计时间段
                    baseDo.setStartTime(DateUtil.parseDateTime(billConfigVo.getStartTime()));
                    baseDo.setEndTime(DateUtil.parseDateTime(billConfigVo.getEndTime()));
                    baseDo.setPduId(pduId);

                    PduEleTotalRealtimeDo endRealtimeDo = endMap.get(pduId);
                    PduEleTotalRealtimeDo startRealtimeDo = startMap.get(pduId);
                    //结束时间电量
                    double endEle = endRealtimeDo.getEle();
                    baseDo.setEndEle(endEle);
                    //开始时间电量
                    double startEle = startRealtimeDo.getEle();
                    baseDo.setStartEle(startEle);
                    //电量集合
                    baseDo.setEq(dataMap.get(pduId).get(EQ_VALUE));
                    //电费集合
                    baseDo.setBill(dataMap.get(pduId).get(BILL_VALUE));
                    baseDo.setCreateTime(DateTime.now());
                    baseDo.setBillPeriod(billConfigVo.getBillPeriod());
                    baseDo.setBillMode(billConfigVo.getBillMode());
                    list.add(baseDo);
                });

            });

            return list;
        }catch (Exception e){
            log.error("计算异常：",e);
        }
        return list;
    }


    /**
     * @description:  获取ES中数据
     * @param configVo 时间段配置
     * @param index  索引名称
     * @return Map<Integer,PduEleTotalRealtimeDo>
     * @author luowei
     * @date: 2024/4/10 10:46
     */
    private Map<Integer, Map<String,Double>> getEleDataByDay(EqBillConfigVo configVo, String index){
        Map<Integer, Map<String,Double>> dataMap = new HashMap<>();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD)
                    .gte(configVo.getStartTime())
                    .lt(configVo.getEndTime()))
                    .must(QueryBuilders.termQuery(BILL_MODE ,configVo.getBillMode()))
                    .must(QueryBuilders.termQuery(BILL_PERIOD + KEYWORD,configVo.getBillPeriod())));

            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms(BY_PDU)
                    .field(PDU_ID);
            // 嵌套聚合
            // 设置聚合查询
            builder.aggregation(pduAggregationBuilder
                    .subAggregation(AggregationBuilders.sum(BILL_VALUE).field(BILL_VALUE))
                    .subAggregation(AggregationBuilders.sum(EQ_VALUE).field(EQ_VALUE)));

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

            for (Terms.Bucket bucket : byPduAggregation.getBuckets()){
                Sum  bills  = bucket.getAggregations().get(BILL_VALUE);
                Integer pudId = Integer.parseInt(String.valueOf(bucket.getKey()));
                Sum  eqs = bucket.getAggregations().get(EQ_VALUE);
                Map<String,Double> map = new HashMap<>();
                map.put(BILL_VALUE,bills.getValue());
                map.put(EQ_VALUE,eqs.getValue());
                dataMap.put(pudId,map);
            }
            return dataMap;
        }catch (Exception e){
            log.error("获取数据异常：",e);
        }
        return dataMap;
    }


    /**
     *  电费计算 (按月统计)
     * @param billConfigVoList 电费计算配置
     */
    public  List<PduEqBaseDo> statisEleMonth(List<EqBillConfigVo> billConfigVoList){
        List<PduEqBaseDo> list = new ArrayList<>();

        try {
            billConfigVoList.forEach(billConfigVo -> {
                String[] times = billConfigVo.getBillPeriod().split(SPLIT);
                String startTime = DateUtil.formatDate(DateUtil.beginOfMonth(DateTime.now())) +" " + times[0];
                String endTime = DateUtil.formatDate(DateTime.now()) +" " + times[1];

                billConfigVo.setStartTime(startTime);
                billConfigVo.setEndTime(endTime);

                //获取时间段内第一条和最后一条数据
                Map<Integer, PduEleTotalRealtimeDo> endMap = getEleData(billConfigVo,
                        SortOrder.DESC,
                        EsIndexEnum.PDU_ELE_TOTAL_REALTIME.getIndex());
                Map<Integer, PduEleTotalRealtimeDo> startMap = getEleData(billConfigVo,
                        SortOrder.ASC,
                        EsIndexEnum.PDU_ELE_TOTAL_REALTIME.getIndex());

                startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
                endTime = DateUtil.formatDateTime(DateTime.now());

                billConfigVo.setStartTime(startTime);
                billConfigVo.setEndTime(endTime);
                //计算总量
                Map<Integer, Map<String,Double>> dataMap = getEleDataByDay(billConfigVo,
                        EsIndexEnum.PDU_EQ_TOTAL_DAY.getIndex());

                endMap.keySet().forEach(pduId ->{
                    PduEqBaseDo baseDo = new PduEqBaseDo();
                    //统计时间段
                    baseDo.setStartTime(DateUtil.parseDateTime(billConfigVo.getStartTime()));
                    baseDo.setEndTime(DateUtil.parseDateTime(billConfigVo.getEndTime()));
                    baseDo.setPduId(pduId);

                    PduEleTotalRealtimeDo endRealtimeDo = endMap.get(pduId);
                    PduEleTotalRealtimeDo startRealtimeDo = startMap.get(pduId);
                    //结束时间电量
                    double endEle = endRealtimeDo.getEle();
                    baseDo.setEndEle(endEle);
                    //开始时间电量
                    double startEle = startRealtimeDo.getEle();
                    baseDo.setStartEle(startEle);
                    //电量集合
                    baseDo.setEq(dataMap.get(pduId).get(EQ_VALUE));
                    //电费集合
                    baseDo.setBill(dataMap.get(pduId).get(BILL_VALUE));
                    baseDo.setCreateTime(DateTime.now());
                    baseDo.setBillPeriod(billConfigVo.getBillPeriod());
                    baseDo.setBillMode(billConfigVo.getBillMode());
                    list.add(baseDo);
                });

            });

            return list;
        }catch (Exception e){
            log.error("计算异常：",e);
        }
        return list;
    }


}
