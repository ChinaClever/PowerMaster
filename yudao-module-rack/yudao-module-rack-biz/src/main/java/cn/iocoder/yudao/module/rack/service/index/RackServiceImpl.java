package cn.iocoder.yudao.module.rack.service.index;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.rack.ele.RackEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.rack.ele.RackEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.rack.pow.RackPowDayDo;
import cn.iocoder.yudao.framework.common.entity.es.rack.pow.RackPowHourDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.rack.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.rack.dal.dataobject.index.RackDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.rack.dal.mysql.index.RackMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rack.enums.ErrorCodeConstants.*;

/**
 * 机架索引 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class RackServiceImpl implements RackService {

    @Resource
    private RackMapper rackMapper;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Integer createIndex(IndexSaveReqVO createReqVO) {
        // 插入
        RackDO index = BeanUtils.toBean(createReqVO, RackDO.class);
        rackMapper.insert(index);
        // 返回
        return index.getId();
    }

    @Override
    public void updateIndex(IndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        RackDO updateObj = BeanUtils.toBean(updateReqVO, RackDO.class);
        rackMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Integer id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        rackMapper.deleteById(id);
    }

    private void validateIndexExists(Integer id) {
        if (rackMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public RackDO getIndex(Integer id) {
        return rackMapper.selectById(id);
    }

    @Override
    public PageResult<RackDO> getIndexPage(IndexPageReqVO pageReqVO) {
        return rackMapper.selectPage(pageReqVO);
    }

    @Override
    public Map getReportConsumeDataById(String Id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        if(Id != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                if(oldTime.equals(newTime)){
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest rackRealTimeDisRequest = new SearchRequest("rack_ele_total_realtime");
                SearchSourceBuilder rackRealTimeDisSourceBuilder = new SearchSourceBuilder();

                rackRealTimeDisSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("rack_id", Id)));

                rackRealTimeDisSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                rackRealTimeDisSourceBuilder.size(24); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                rackRealTimeDisRequest.source(rackRealTimeDisSourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(rackRealTimeDisRequest);
                double todayEle = 0;
                List<Double> eq = new ArrayList<>();
                List<String> time = new ArrayList<>();
                double maxEle = -1;
                DateTime maxEleTime = new DateTime();
                double lastEq = 0;
                double firstEq = 0;
                int index = 0;
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse cabinetEleTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(cabinetEleTotalRealDisResponse != null){
                        for (SearchHit hit : cabinetEleTotalRealDisResponse.getHits()) {
                            index++;
                            RackEleTotalRealtimeDo rackEleTotalRealtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), RackEleTotalRealtimeDo.class);
                            if(index == 1){
                                firstEq = rackEleTotalRealtimeDo.getEleTotal();
                            }
                            double eleValue  = rackEleTotalRealtimeDo.getEleTotal() - lastEq;
                            DateTime createTime = new DateTime(rackEleTotalRealtimeDo.getCreateTime());
                            if(eleValue > maxEle){
                                maxEle = eleValue;
                                maxEleTime = createTime;
                            }
                            lastEq = rackEleTotalRealtimeDo.getEleTotal();
                            if(index > 1){
                                eq.add(eleValue);
                                time.add(createTime.toString("yyyy-MM-dd HH:mm"));
                            }
                        }
                    }
                    result.put("eq",eq);
                    result.put("time",time);
                    result.put("totalEle",todayEle);
                    result.put("maxEle",maxEle != -1 ? maxEle : null);
                    result.put("maxEleTime",maxEle != -1 ? maxEleTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("firstEq",firstEq);
                    result.put("lastEq",lastEq);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建聚合搜索请求
                SearchRequest rackEqTotalDayRequest = new SearchRequest("rack_eq_total_day");
                SearchSourceBuilder rackEqTotalDaySourceBuilder = new SearchSourceBuilder();
                // 设置时间范围查询条件

                rackEqTotalDaySourceBuilder.query(QueryBuilders.boolQuery()
                        //今天的数据 cabinet_ele_total_realtime的时间范围查询必须使用字符串
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("rack_id", Id))); // 添加cabinet_id条件
                // 设置聚合条件
                rackEqTotalDaySourceBuilder.aggregation(AggregationBuilders.sum("total_eq")
                        .field("eq_value"));
                // 将搜索条件添加到请求中
                rackEqTotalDayRequest.source(rackEqTotalDaySourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(rackEqTotalDayRequest);

                // 创建时间分布搜索请求
                SearchRequest rackTotalDayDisRequest = new SearchRequest("rack_eq_total_day");
                SearchSourceBuilder rackTotalDayDisSourceBuilder = new SearchSourceBuilder();

                rackTotalDayDisSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("rack_id", Id)));

                rackTotalDayDisSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                rackTotalDayDisSourceBuilder.size(31); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                rackTotalDayDisRequest.source(rackTotalDayDisSourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(rackTotalDayDisRequest);
                double totalEq = 0;
                List<Double> eq = new ArrayList<>();
                List<String> time = new ArrayList<>();
                double maxEq = -1;
                DateTime maxEleTime = new DateTime();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析第一个搜索请求的聚合结果
                    SearchResponse rackEleTotalRealResponse = multiSearchResponse.getResponses()[0].getResponse();
                    Sum sumAggregation1 = rackEleTotalRealResponse.getAggregations().get("total_eq");
                    totalEq = sumAggregation1.getValue();

                    // 解析第二个搜索请求
                    SearchResponse rackEqTotalDayDisResponse = multiSearchResponse.getResponses()[1].getResponse();
                    if(rackEqTotalDayDisResponse != null){
                        for (SearchHit hit : rackEqTotalDayDisResponse.getHits()) {
                            RackEqTotalDayDo rackEqTotalDayDo = JsonUtils.parseObject(hit.getSourceAsString(), RackEqTotalDayDo.class);
                            double eqValue  = rackEqTotalDayDo.getEqValue();
                            DateTime startTime = rackEqTotalDayDo.getStartTime();
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
        } else{
            return result;
        }
    }

    @Override
    public Map getReportPowDataById(String Id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        if(Id != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                if(oldTime.equals(newTime)){
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest rackPowTotalRealRequest = new SearchRequest("rack_hda_pow_hour");
                SearchSourceBuilder rackPowTotalRealSourceBuilder = new SearchSourceBuilder();

                rackPowTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("rack_id", Id)));

                rackPowTotalRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                rackPowTotalRealSourceBuilder.size(24); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                rackPowTotalRealRequest.source(rackPowTotalRealSourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(rackPowTotalRealRequest);

                List<Double> activePowAvgValue = new ArrayList<>();
                List<Double> apparentPowAvgValue = new ArrayList<>();
                

                List<String> time = new ArrayList<>();

                double apparentPowMaxValue = -1;
                DateTime apparentPowMaxTime = new DateTime();
                double apparentPowMinValue = Double.MAX_VALUE;
                DateTime apparentPowMinTime = new DateTime();
                double activePowMaxValue = -1;
                DateTime activePowMaxTime = new DateTime();
                double activePowMinValue = Double.MAX_VALUE;
                DateTime activePowMinTime = new DateTime();

                
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse rackPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(rackPowTotalRealDisResponse != null){
                        for (SearchHit hit : rackPowTotalRealDisResponse.getHits()) {
                            RackPowHourDo rackHdaTotalHourDo = JsonUtils.parseObject(hit.getSourceAsString(), RackPowHourDo.class);
                            double activePowAvg  = rackHdaTotalHourDo.getActiveTotalAvgValue();
                            double apparentPowAvg = rackHdaTotalHourDo.getApparentTotalAvgValue();
                            DateTime createTime = new DateTime(rackHdaTotalHourDo.getCreateTime());

                            if(rackHdaTotalHourDo.getApparentTotalMaxValue() > apparentPowMaxValue){
                                apparentPowMaxValue = rackHdaTotalHourDo.getApparentTotalMaxValue();
                                apparentPowMaxTime = new DateTime(rackHdaTotalHourDo.getApparentTotalMaxTime());
                            }
                            if(rackHdaTotalHourDo.getApparentTotalMinValue() < apparentPowMinValue ){
                                apparentPowMinValue = rackHdaTotalHourDo.getApparentTotalMinValue();
                                apparentPowMinTime = new DateTime(rackHdaTotalHourDo.getApparentTotalMinTime());
                            }
                            if(rackHdaTotalHourDo.getActiveTotalMaxValue() > activePowMaxValue){
                                activePowMaxValue = rackHdaTotalHourDo.getActiveTotalMaxValue();
                                activePowMaxTime = new DateTime(rackHdaTotalHourDo.getActiveTotalMaxTime());
                            }
                            if(rackHdaTotalHourDo.getActiveTotalMinValue() < activePowMinValue ){
                                activePowMinValue = rackHdaTotalHourDo.getActiveTotalMinValue();
                                activePowMinTime = new DateTime(rackHdaTotalHourDo.getActiveTotalMinTime());
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
                    result.put("apparentPowMinValue",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinValue : null);
                    result.put("apparentPowMinTime",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMaxValue",activePowMaxValue != -1 ? activePowMaxValue : null);
                    result.put("activePowMaxTime",activePowMaxValue != -1 ? activePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMinValue",activePowMinValue != Double.MAX_VALUE ? activePowMinValue : null);
                    result.put("activePowMinTime",activePowMinValue != Double.MAX_VALUE ? activePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest rackPowTotalDayRequest = new SearchRequest("cabinet_hda_pow_day");
                SearchSourceBuilder rackPowTotalDaySourceBuilder = new SearchSourceBuilder();

                rackPowTotalDaySourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("rack_id", Id)));

                rackPowTotalDaySourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                rackPowTotalDaySourceBuilder.size(31); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                rackPowTotalDayRequest.source(rackPowTotalDaySourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(rackPowTotalDayRequest);

                List<Double> activePowAvgValue = new ArrayList<>();
                List<Double> apparentPowAvgValue = new ArrayList<>();

                List<String> time = new ArrayList<>();
                double apparentPowMaxValue = -1;
                DateTime apparentPowMaxTime = new DateTime();
                double apparentPowMinValue = Double.MAX_VALUE;
                DateTime apparentPowMinTime = new DateTime();
                double activePowMaxValue = -1;
                DateTime activePowMaxTime = new DateTime();
                double activePowMinValue = Double.MAX_VALUE;
                DateTime activePowMinTime = new DateTime();

                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse rackPowTotalDayDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(rackPowTotalDayDisResponse != null){
                        for (SearchHit hit : rackPowTotalDayDisResponse.getHits()) {
                            RackPowDayDo rackHdaTotalDayDo = JsonUtils.parseObject(hit.getSourceAsString(), RackPowDayDo.class);
                            double activePowAvg  = rackHdaTotalDayDo.getActiveTotalAvgValue();
                            double apparentPowAvg = rackHdaTotalDayDo.getApparentTotalAvgValue();

                            DateTime createTime = new DateTime(rackHdaTotalDayDo.getCreateTime());
                            createTime = new DateTime(createTime.toLocalDateTime().minusDays(1));
                            if(rackHdaTotalDayDo.getApparentTotalMaxValue() > apparentPowMaxValue){
                                apparentPowMaxValue = rackHdaTotalDayDo.getApparentTotalMaxValue();
                                apparentPowMaxTime = new DateTime(rackHdaTotalDayDo.getApparentTotalMaxTime());
                            }
                            if(rackHdaTotalDayDo.getApparentTotalMinValue() < apparentPowMinValue ){
                                apparentPowMinValue = rackHdaTotalDayDo.getApparentTotalMinValue();
                                apparentPowMinTime = new DateTime(rackHdaTotalDayDo.getApparentTotalMinTime());
                            }
                            if(rackHdaTotalDayDo.getActiveTotalMaxValue() > activePowMaxValue){
                                activePowMaxValue = rackHdaTotalDayDo.getActiveTotalMaxValue();
                                activePowMaxTime = new DateTime(rackHdaTotalDayDo.getActiveTotalMaxTime());
                            }
                            if(rackHdaTotalDayDo.getActiveTotalMinValue() < activePowMinValue ){
                                activePowMinValue = rackHdaTotalDayDo.getActiveTotalMinValue();
                                activePowMinTime = new DateTime(rackHdaTotalDayDo.getActiveTotalMinTime());
                            }

                            activePowAvgValue.add(activePowAvg);
                            apparentPowAvgValue.add(apparentPowAvg);

                            time.add(createTime.toString("yyyy-MM-dd"));
                        }
                    }
                    result.put("activePowAvgValue",activePowAvgValue);
                    result.put("apparentPowAvgValue",apparentPowAvgValue);

                    result.put("time",time);

                    result.put("apparentPowMaxValue",apparentPowMaxValue != -1 ? apparentPowMaxValue : null);
                    result.put("apparentPowMaxTime",apparentPowMaxValue != -1 ? apparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("apparentPowMinValue",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinValue : null);
                    result.put("apparentPowMinTime",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMaxValue",activePowMaxValue != -1 ? activePowMaxValue : null);
                    result.put("activePowMaxTime",activePowMaxValue != -1 ? activePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMinValue",activePowMinValue != Double.MAX_VALUE ? activePowMinValue : null);
                    result.put("activePowMinTime",activePowMinValue != Double.MAX_VALUE ? activePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }else {
            return result;
        }
    }

}