package cn.iocoder.yudao.module.cabinet.service.index;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalDayDo;

import cn.iocoder.yudao.framework.common.entity.es.cabinet.env.CabinetEnvDayDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.env.CabinetEnvHourDo;

import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowDayDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowHourDo;

import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetEnvSensor;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetPduMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.PduIndex;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.temcolor.TemColorDO;
import cn.iocoder.yudao.module.cabinet.dal.mysql.temcolor.TemColorMapper;
import cn.iocoder.yudao.module.cabinet.mapper.*;
import cn.iocoder.yudao.module.cabinet.service.temcolor.TemColorService;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.cabinet.dal.mysql.index.CabIndexMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.cabinet.enums.ErrorCodeConstants.*;

/**
 * 机柜索引 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IndexServiceImpl implements IndexService {

    @Resource
    private CabIndexMapper cabIndexMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private AisleIndexMapper aisleIndexMapper;

    @Autowired
    private CabinetPduMapper cabinetPduMapper;

    @Autowired
    private NewPDUIndexMapper pduIndexMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CabinetEnvSensorMapper cabinetEnvSensorMapper;

    @Resource
    private TemColorMapper temColorMapper;

    @Autowired
    private TemColorService temColorService;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Integer createIndex(IndexSaveReqVO createReqVO) {
        // 插入
        IndexDO index = BeanUtils.toBean(createReqVO, IndexDO.class);
        cabIndexMapper.insert(index);
        // 返回
        return index.getId();
    }

    @Override
    public void updateIndex(IndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        IndexDO updateObj = BeanUtils.toBean(updateReqVO, IndexDO.class);
        cabIndexMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Integer id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        cabIndexMapper.deleteById(id);
    }

    private void validateIndexExists(Integer id) {
        if (cabIndexMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public IndexDO getIndex(Integer id) {
        return cabIndexMapper.selectById(id);
    }

    @Override
    public PageResult<IndexDO> getIndexPage(IndexPageReqVO pageReqVO) {
        return cabIndexMapper.selectPage(pageReqVO);
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
                SearchRequest cabinetRealTimeDisRequest = new SearchRequest("cabinet_ele_total_realtime");
                SearchSourceBuilder cabinetRealTimeDisSourceBuilder = new SearchSourceBuilder();

                cabinetRealTimeDisSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("cabinet_id", Id)));

                cabinetRealTimeDisSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                cabinetRealTimeDisSourceBuilder.size(24); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                cabinetRealTimeDisRequest.source(cabinetRealTimeDisSourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(cabinetRealTimeDisRequest);
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
                            CabinetEleTotalRealtimeDo cabinetEleTotalRealtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), CabinetEleTotalRealtimeDo.class);
                            if(index == 1){
                                firstEq = cabinetEleTotalRealtimeDo.getEleTotal();
                            }
                            double eleValue  = cabinetEleTotalRealtimeDo.getEleTotal() - lastEq;
                            DateTime createTime = new DateTime(cabinetEleTotalRealtimeDo.getCreateTime());
                            if(eleValue > maxEle){
                                maxEle = eleValue;
                                maxEleTime = createTime;
                            }
                            lastEq = cabinetEleTotalRealtimeDo.getEleTotal();
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
                SearchRequest cabinetEqTotalDayRequest = new SearchRequest("cabinet_eq_total_day");
                SearchSourceBuilder cabinetEqTotalDaySourceBuilder = new SearchSourceBuilder();
                // 设置时间范围查询条件

                cabinetEqTotalDaySourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("cabinet_id", Id))); // 添加cabinet_id条件
                // 设置聚合条件
                cabinetEqTotalDaySourceBuilder.aggregation(AggregationBuilders.sum("total_eq")
                        .field("eq_value"));
                // 将搜索条件添加到请求中
                cabinetEqTotalDayRequest.source(cabinetEqTotalDaySourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(cabinetEqTotalDayRequest);

                // 创建时间分布搜索请求
                SearchRequest cabinetRealTimeDisRequest = new SearchRequest("cabinet_eq_total_day");
                SearchSourceBuilder cabinetRealTimeDisSourceBuilder = new SearchSourceBuilder();

                cabinetRealTimeDisSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("cabinet_id", Id)));

                cabinetRealTimeDisSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                cabinetRealTimeDisSourceBuilder.size(31); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                cabinetRealTimeDisRequest.source(cabinetRealTimeDisSourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(cabinetRealTimeDisRequest);
                double totalEq = 0;
                List<Double> eq = new ArrayList<>();
                List<String> time = new ArrayList<>();
                double maxEq = -1;
                DateTime maxEleTime = new DateTime();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析第一个搜索请求的聚合结果
                    SearchResponse cabinetEleTotalRealResponse = multiSearchResponse.getResponses()[0].getResponse();
                    Sum sumAggregation1 = cabinetEleTotalRealResponse.getAggregations().get("total_eq");
                    totalEq = sumAggregation1.getValue();

                    // 解析第二个搜索请求
                    SearchResponse cabinetEqTotalDayDisResponse = multiSearchResponse.getResponses()[1].getResponse();
                    if(cabinetEqTotalDayDisResponse != null){
                        for (SearchHit hit : cabinetEqTotalDayDisResponse.getHits()) {
                            CabinetEqTotalDayDo cabinetEqTotalDayDo = JsonUtils.parseObject(hit.getSourceAsString(), CabinetEqTotalDayDo.class);
                            double eqValue  = cabinetEqTotalDayDo.getEqValue();
                            DateTime startTime = cabinetEqTotalDayDo.getStartTime();
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
                SearchRequest cabinetPowTotalRealRequest = new SearchRequest("cabinet_hda_pow_hour");
                SearchSourceBuilder cabinetPowTotalRealSourceBuilder = new SearchSourceBuilder();

                cabinetPowTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("cabinet_id", Id)));

                cabinetPowTotalRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                cabinetPowTotalRealSourceBuilder.size(24); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                cabinetPowTotalRealRequest.source(cabinetPowTotalRealSourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(cabinetPowTotalRealRequest);

                List<Double> activePowAvgValue = new ArrayList<>();
                List<Double> apparentPowAvgValue = new ArrayList<>();

                List<Double> AactivePowAvgValue = new ArrayList<>();
                List<Double> AapparentPowAvgValue = new ArrayList<>();

                List<Double> BactivePowAvgValue = new ArrayList<>();
                List<Double> BapparentPowAvgValue = new ArrayList<>();

                List<String> time = new ArrayList<>();

                double apparentPowMaxValue = -1;
                DateTime apparentPowMaxTime = new DateTime();
                double apparentPowMinValue = Double.MAX_VALUE;
                DateTime apparentPowMinTime = new DateTime();
                double activePowMaxValue = -1;
                DateTime activePowMaxTime = new DateTime();
                double activePowMinValue = Double.MAX_VALUE;
                DateTime activePowMinTime = new DateTime();

                double AapparentPowMaxValue = -1;
                DateTime AapparentPowMaxTime = new DateTime();
                double AapparentPowMinValue = Double.MAX_VALUE;
                DateTime AapparentPowMinTime = new DateTime();
                double AactivePowMaxValue = -1;
                DateTime AactivePowMaxTime = new DateTime();
                double AactivePowMinValue = Double.MAX_VALUE;
                DateTime AactivePowMinTime = new DateTime();

                double BapparentPowMaxValue = -1;
                DateTime BapparentPowMaxTime = new DateTime();
                double BapparentPowMinValue = Double.MAX_VALUE;
                DateTime BapparentPowMinTime = new DateTime();
                double BactivePowMaxValue = -1;
                DateTime BactivePowMaxTime = new DateTime();
                double BactivePowMinValue = Double.MAX_VALUE;
                DateTime BactivePowMinTime = new DateTime();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse cabinetPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(cabinetPowTotalRealDisResponse != null){
                        for (SearchHit hit : cabinetPowTotalRealDisResponse.getHits()) {
                            CabinetPowHourDo cabinetHdaTotalHourDo = JsonUtils.parseObject(hit.getSourceAsString(), CabinetPowHourDo.class);
                            double activePowAvg  = cabinetHdaTotalHourDo.getActiveTotalAvgValue();
                            double apparentPowAvg = cabinetHdaTotalHourDo.getApparentTotalAvgValue();
                            double activeAAvgValue = cabinetHdaTotalHourDo.getActiveAAvgValue();
                            double apparentAAvgValue = cabinetHdaTotalHourDo.getApparentAAvgValue();
                            double activeBAvgValue = cabinetHdaTotalHourDo.getActiveBAvgValue();
                            double apparentBAvgValue = cabinetHdaTotalHourDo.getApparentBAvgValue();
                            DateTime createTime = new DateTime(cabinetHdaTotalHourDo.getCreateTime());

                            if(cabinetHdaTotalHourDo.getApparentTotalMaxValue() > apparentPowMaxValue){
                                apparentPowMaxValue = cabinetHdaTotalHourDo.getApparentTotalMaxValue();
                                apparentPowMaxTime = new DateTime(cabinetHdaTotalHourDo.getApparentTotalMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getApparentTotalMinValue() < apparentPowMinValue ){
                                apparentPowMinValue = cabinetHdaTotalHourDo.getApparentTotalMinValue();
                                apparentPowMinTime = new DateTime(cabinetHdaTotalHourDo.getApparentTotalMinTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveTotalMaxValue() > activePowMaxValue){
                                activePowMaxValue = cabinetHdaTotalHourDo.getActiveTotalMaxValue();
                                activePowMaxTime = new DateTime(cabinetHdaTotalHourDo.getActiveTotalMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveTotalMinValue() < activePowMinValue ){
                                activePowMinValue = cabinetHdaTotalHourDo.getActiveTotalMinValue();
                                activePowMinTime = new DateTime(cabinetHdaTotalHourDo.getActiveTotalMinTime());
                            }

                            if(cabinetHdaTotalHourDo.getApparentAMaxValue() > AapparentPowMaxValue){
                                AapparentPowMaxValue = cabinetHdaTotalHourDo.getApparentAMaxValue();
                                AapparentPowMaxTime = new DateTime(cabinetHdaTotalHourDo.getApparentAMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getApparentAMinValue() < AapparentPowMinValue ){
                                AapparentPowMinValue = cabinetHdaTotalHourDo.getApparentAMinValue();
                                AapparentPowMinTime = new DateTime(cabinetHdaTotalHourDo.getApparentAMinTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveAMaxValue() > AactivePowMaxValue){
                                AactivePowMaxValue = cabinetHdaTotalHourDo.getActiveAMaxValue();
                                AactivePowMaxTime = new DateTime(cabinetHdaTotalHourDo.getActiveAMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveAMinValue() < AactivePowMinValue ){
                                AactivePowMinValue = cabinetHdaTotalHourDo.getActiveAMinValue();
                                AactivePowMinTime = new DateTime(cabinetHdaTotalHourDo.getActiveAMinTime());
                            }

                            if(cabinetHdaTotalHourDo.getApparentBMaxValue() > BapparentPowMaxValue){
                                BapparentPowMaxValue = cabinetHdaTotalHourDo.getApparentBMaxValue();
                                BapparentPowMaxTime = new DateTime(cabinetHdaTotalHourDo.getApparentBMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getApparentBMinValue() < BapparentPowMinValue ){
                                BapparentPowMinValue = cabinetHdaTotalHourDo.getApparentBMinValue();
                                BapparentPowMinTime = new DateTime(cabinetHdaTotalHourDo.getApparentBMinTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveBMaxValue() > BactivePowMaxValue){
                                BactivePowMaxValue = cabinetHdaTotalHourDo.getActiveBMaxValue();
                                BactivePowMaxTime = new DateTime(cabinetHdaTotalHourDo.getActiveBMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveBMinValue() < BactivePowMinValue ){
                                BactivePowMinValue = cabinetHdaTotalHourDo.getActiveBMinValue();
                                BactivePowMinTime = new DateTime(cabinetHdaTotalHourDo.getActiveBMinTime());
                            }

                            activePowAvgValue.add(activePowAvg);
                            apparentPowAvgValue.add(apparentPowAvg);

                            AactivePowAvgValue.add(activeAAvgValue);
                            AapparentPowAvgValue.add(apparentAAvgValue);

                            BactivePowAvgValue.add(activeBAvgValue);
                            BapparentPowAvgValue.add(apparentBAvgValue);

                            time.add(createTime.toString("yyyy-MM-dd HH:mm"));
                        }
                    }
                    result.put("activePowAvgValue",activePowAvgValue);
                    result.put("apparentPowAvgValue",apparentPowAvgValue);

                    result.put("AactivePowAvgValue",AactivePowAvgValue);
                    result.put("AapparentPowAvgValue",AapparentPowAvgValue);

                    result.put("BactivePowAvgValue",BactivePowAvgValue);
                    result.put("BapparentPowAvgValue",BapparentPowAvgValue);
                    result.put("time",time);

                    result.put("apparentPowMaxValue",apparentPowMaxValue != -1 ? apparentPowMaxValue : null);
                    result.put("apparentPowMaxTime",apparentPowMaxValue != -1 ? apparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("apparentPowMinValue",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinValue : null);
                    result.put("apparentPowMinTime",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMaxValue",activePowMaxValue != -1 ? activePowMaxValue : null);
                    result.put("activePowMaxTime",activePowMaxValue != -1 ? activePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMinValue",activePowMinValue != Double.MAX_VALUE ? activePowMinValue : null);
                    result.put("activePowMinTime",activePowMinValue != Double.MAX_VALUE ? activePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);

                    result.put("AapparentPowMaxValue",AapparentPowMaxValue != -1 ? AapparentPowMaxValue : null);
                    result.put("AapparentPowMaxTime",AapparentPowMaxValue != -1 ? AapparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("AapparentPowMinValue",AapparentPowMinValue != Double.MAX_VALUE ? AapparentPowMinValue : null);
                    result.put("AapparentPowMinTime",AapparentPowMinValue != Double.MAX_VALUE ? AapparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("AactivePowMaxValue",AactivePowMaxValue != -1 ? AactivePowMaxValue : null);
                    result.put("AactivePowMaxTime",AactivePowMaxValue != -1 ? AactivePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("AactivePowMinValue",AactivePowMinValue != Double.MAX_VALUE ? AactivePowMinValue : null);
                    result.put("AactivePowMinTime",AactivePowMinValue != Double.MAX_VALUE ? AactivePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);

                    result.put("BapparentPowMaxValue",BapparentPowMaxValue != -1 ? BapparentPowMaxValue : null);
                    result.put("BapparentPowMaxTime",BapparentPowMaxValue != -1 ? BapparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("BapparentPowMinValue",BapparentPowMinValue != Double.MAX_VALUE ? BapparentPowMinValue : null);
                    result.put("BapparentPowMinTime",BapparentPowMinValue != Double.MAX_VALUE ? BapparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("BactivePowMaxValue",BactivePowMaxValue != -1 ? BactivePowMaxValue : null);
                    result.put("BactivePowMaxTime",BactivePowMaxValue != -1 ? BactivePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("BactivePowMinValue",BactivePowMinValue != Double.MAX_VALUE ? BactivePowMinValue : null);
                    result.put("BactivePowMinTime",BactivePowMinValue != Double.MAX_VALUE ? BactivePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest cabinetPowTotalRealRequest = new SearchRequest("cabinet_hda_pow_day");
                SearchSourceBuilder cabinetPowTotalRealSourceBuilder = new SearchSourceBuilder();

                cabinetPowTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("cabinet_id", Id)));

                cabinetPowTotalRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                cabinetPowTotalRealSourceBuilder.size(31); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                cabinetPowTotalRealRequest.source(cabinetPowTotalRealSourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(cabinetPowTotalRealRequest);

                List<Double> activePowAvgValue = new ArrayList<>();
                List<Double> apparentPowAvgValue = new ArrayList<>();

                List<Double> AactivePowAvgValue = new ArrayList<>();
                List<Double> AapparentPowAvgValue = new ArrayList<>();

                List<Double> BactivePowAvgValue = new ArrayList<>();
                List<Double> BapparentPowAvgValue = new ArrayList<>();

                List<String> time = new ArrayList<>();
                double apparentPowMaxValue = -1;
                DateTime apparentPowMaxTime = new DateTime();
                double apparentPowMinValue = Double.MAX_VALUE;
                DateTime apparentPowMinTime = new DateTime();
                double activePowMaxValue = -1;
                DateTime activePowMaxTime = new DateTime();
                double activePowMinValue = Double.MAX_VALUE;
                DateTime activePowMinTime = new DateTime();

                double AapparentPowMaxValue = -1;
                DateTime AapparentPowMaxTime = new DateTime();
                double AapparentPowMinValue = Double.MAX_VALUE;
                DateTime AapparentPowMinTime = new DateTime();
                double AactivePowMaxValue = -1;
                DateTime AactivePowMaxTime = new DateTime();
                double AactivePowMinValue = Double.MAX_VALUE;
                DateTime AactivePowMinTime = new DateTime();

                double BapparentPowMaxValue = -1;
                DateTime BapparentPowMaxTime = new DateTime();
                double BapparentPowMinValue = Double.MAX_VALUE;
                DateTime BapparentPowMinTime = new DateTime();
                double BactivePowMaxValue = -1;
                DateTime BactivePowMaxTime = new DateTime();
                double BactivePowMinValue = Double.MAX_VALUE;
                DateTime BactivePowMinTime = new DateTime();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse cabinetPowTotalDayDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(cabinetPowTotalDayDisResponse != null){
                        for (SearchHit hit : cabinetPowTotalDayDisResponse.getHits()) {
                            CabinetPowDayDo cabinetHdaTotalHourDo = JsonUtils.parseObject(hit.getSourceAsString(), CabinetPowDayDo.class);
                            double activePowAvg  = cabinetHdaTotalHourDo.getActiveTotalAvgValue();
                            double apparentPowAvg = cabinetHdaTotalHourDo.getApparentTotalAvgValue();
                            double activeAAvgValue = cabinetHdaTotalHourDo.getActiveAAvgValue();
                            double apparentAAvgValue = cabinetHdaTotalHourDo.getApparentAAvgValue();
                            double activeBAvgValue = cabinetHdaTotalHourDo.getActiveBAvgValue();
                            double apparentBAvgValue = cabinetHdaTotalHourDo.getApparentBAvgValue();
                            DateTime createTime = new DateTime(cabinetHdaTotalHourDo.getCreateTime());
                            createTime = new DateTime(createTime.toLocalDateTime().minusDays(1));
                            if(cabinetHdaTotalHourDo.getApparentTotalMaxValue() > apparentPowMaxValue){
                                apparentPowMaxValue = cabinetHdaTotalHourDo.getApparentTotalMaxValue();
                                apparentPowMaxTime = new DateTime(cabinetHdaTotalHourDo.getApparentTotalMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getApparentTotalMinValue() < apparentPowMinValue ){
                                apparentPowMinValue = cabinetHdaTotalHourDo.getApparentTotalMinValue();
                                apparentPowMinTime = new DateTime(cabinetHdaTotalHourDo.getApparentTotalMinTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveTotalMaxValue() > activePowMaxValue){
                                activePowMaxValue = cabinetHdaTotalHourDo.getActiveTotalMaxValue();
                                activePowMaxTime = new DateTime(cabinetHdaTotalHourDo.getActiveTotalMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveTotalMinValue() < activePowMinValue ){
                                activePowMinValue = cabinetHdaTotalHourDo.getActiveTotalMinValue();
                                activePowMinTime = new DateTime(cabinetHdaTotalHourDo.getActiveTotalMinTime());
                            }

                            if(cabinetHdaTotalHourDo.getApparentAMaxValue() > AapparentPowMaxValue){
                                AapparentPowMaxValue = cabinetHdaTotalHourDo.getApparentAMaxValue();
                                AapparentPowMaxTime = new DateTime(cabinetHdaTotalHourDo.getApparentAMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getApparentAMinValue() < AapparentPowMinValue ){
                                AapparentPowMinValue = cabinetHdaTotalHourDo.getApparentAMinValue();
                                AapparentPowMinTime = new DateTime(cabinetHdaTotalHourDo.getApparentAMinTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveAMaxValue() > AactivePowMaxValue){
                                AactivePowMaxValue = cabinetHdaTotalHourDo.getActiveAMaxValue();
                                AactivePowMaxTime = new DateTime(cabinetHdaTotalHourDo.getActiveAMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveAMinValue() < AactivePowMinValue ){
                                AactivePowMinValue = cabinetHdaTotalHourDo.getActiveAMinValue();
                                AactivePowMinTime = new DateTime(cabinetHdaTotalHourDo.getActiveAMinTime());
                            }

                            if(cabinetHdaTotalHourDo.getApparentBMaxValue() > BapparentPowMaxValue){
                                BapparentPowMaxValue = cabinetHdaTotalHourDo.getApparentBMaxValue();
                                BapparentPowMaxTime = new DateTime(cabinetHdaTotalHourDo.getApparentBMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getApparentBMinValue() < BapparentPowMinValue ){
                                BapparentPowMinValue = cabinetHdaTotalHourDo.getApparentBMinValue();
                                BapparentPowMinTime = new DateTime(cabinetHdaTotalHourDo.getApparentBMinTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveBMaxValue() > BactivePowMaxValue){
                                BactivePowMaxValue = cabinetHdaTotalHourDo.getActiveBMaxValue();
                                BactivePowMaxTime = new DateTime(cabinetHdaTotalHourDo.getActiveBMaxTime());
                            }
                            if(cabinetHdaTotalHourDo.getActiveBMinValue() < BactivePowMinValue ){
                                BactivePowMinValue = cabinetHdaTotalHourDo.getActiveBMinValue();
                                BactivePowMinTime = new DateTime(cabinetHdaTotalHourDo.getActiveBMinTime());
                            }

                            activePowAvgValue.add(activePowAvg);
                            apparentPowAvgValue.add(apparentPowAvg);

                            AactivePowAvgValue.add(activeAAvgValue);
                            AapparentPowAvgValue.add(apparentAAvgValue);

                            BactivePowAvgValue.add(activeBAvgValue);
                            BapparentPowAvgValue.add(apparentBAvgValue);

                            time.add(createTime.toString("yyyy-MM-dd"));
                        }
                    }
                    result.put("activePowAvgValue",activePowAvgValue);
                    result.put("apparentPowAvgValue",apparentPowAvgValue);

                    result.put("AactivePowAvgValue",AactivePowAvgValue);
                    result.put("AapparentPowAvgValue",AapparentPowAvgValue);

                    result.put("BactivePowAvgValue",BactivePowAvgValue);
                    result.put("BapparentPowAvgValue",BapparentPowAvgValue);
                    result.put("time",time);

                    result.put("apparentPowMaxValue",apparentPowMaxValue != -1 ? apparentPowMaxValue : null);
                    result.put("apparentPowMaxTime",apparentPowMaxValue != -1 ? apparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("apparentPowMinValue",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinValue : null);
                    result.put("apparentPowMinTime",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMaxValue",activePowMaxValue != -1 ? activePowMaxValue : null);
                    result.put("activePowMaxTime",activePowMaxValue != -1 ? activePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMinValue",activePowMinValue != Double.MAX_VALUE ? activePowMinValue : null);
                    result.put("activePowMinTime",activePowMinValue != Double.MAX_VALUE ? activePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);

                    result.put("AapparentPowMaxValue",AapparentPowMaxValue != -1 ? AapparentPowMaxValue : null);
                    result.put("AapparentPowMaxTime",AapparentPowMaxValue != -1 ? AapparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("AapparentPowMinValue",AapparentPowMinValue != Double.MAX_VALUE ? AapparentPowMinValue : null);
                    result.put("AapparentPowMinTime",AapparentPowMinValue != Double.MAX_VALUE ? AapparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("AactivePowMaxValue",AactivePowMaxValue != -1 ? AactivePowMaxValue : null);
                    result.put("AactivePowMaxTime",AactivePowMaxValue != -1 ? AactivePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("AactivePowMinValue",AactivePowMinValue != Double.MAX_VALUE ? AactivePowMinValue : null);
                    result.put("AactivePowMinTime",AactivePowMinValue != Double.MAX_VALUE ? AactivePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);

                    result.put("BapparentPowMaxValue",BapparentPowMaxValue != -1 ? BapparentPowMaxValue : null);
                    result.put("BapparentPowMaxTime",BapparentPowMaxValue != -1 ? BapparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("BapparentPowMinValue",BapparentPowMinValue != Double.MAX_VALUE ? BapparentPowMinValue : null);
                    result.put("BapparentPowMinTime",BapparentPowMinValue != Double.MAX_VALUE ? BapparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("BactivePowMaxValue",BactivePowMaxValue != -1 ? BactivePowMaxValue : null);
                    result.put("BactivePowMaxTime",BactivePowMaxValue != -1 ? BactivePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("BactivePowMinValue",BactivePowMinValue != Double.MAX_VALUE ? BactivePowMinValue : null);
                    result.put("BactivePowMinTime",BactivePowMinValue != Double.MAX_VALUE ? BactivePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }else {
            return result;
        }
    }

//    @Override
//    public Map getReportOutLetDataById(String Id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
//        Map result = new HashMap<>();
//        if(Id != null){
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
//                if(oldTime.equals(newTime)){
//                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
//                }
//                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
//
//                // 创建时间分布搜索请求
//                SearchRequest cabinetOutLetTotalRealRequest = new SearchRequest("cabinet_ele_outlet");
//                SearchSourceBuilder cabinetOutLetTotalRealSourceBuilder = new SearchSourceBuilder();
//
//                cabinetOutLetTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
//                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
//                        .must(QueryBuilders.termQuery("cabinet_id", Id)));
//                //映射sum的sum_ele_active为sumEle
//                Map<String, String> map = new HashMap<>();
//                map.put("sumEle", "sum_ele_active");
//                cabinetOutLetTotalRealSourceBuilder.aggregation(AggregationBuilders
//                        .terms("by_outlet_id").field("outlet_id").order(BucketOrder.aggregation("sum_ele_active",true)).size(24)
//                        .subAggregation(AggregationBuilders.sum("sum_ele_active").field("ele_active"))
//                        //筛选sumEle > 0的
//                        .subAggregation(PipelineAggregatorBuilders.bucketSelector("positive_sum_ele",map, new Script("params.sumEle > 0"))));
//
//
//                cabinetOutLetTotalRealSourceBuilder.size(0); // 设置返回的最大结果数
//                // 将搜索条件添加到请求中
//                cabinetOutLetTotalRealRequest.source(cabinetOutLetTotalRealSourceBuilder);
//                // 将第搜索请求添加到多索引搜索请求中
//                multiSearchRequest.add(cabinetOutLetTotalRealRequest);
//
//                List<Double> eleValue = new ArrayList<>();
//                List<String> outLetId = new ArrayList<>();
//                try {
//                    // 执行多索引搜索请求
//                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);
//
//                    // 解析搜索请求
//                    SearchResponse cabinetPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
//                    if(cabinetPowTotalRealDisResponse != null){
//                        for ( Terms.Bucket bucket : ((ParsedLongTerms) cabinetPowTotalRealDisResponse.getAggregations().get("by_outlet_id")).getBuckets()) {
//                            String outlet = bucket.getKeyAsString();
//                            Sum sum = bucket.getAggregations().get("sum_ele_active");
//                            double sumEleActive = sum.getValue();
//                            eleValue.add(sumEleActive);
//                            outLetId.add(outlet);
//                        }
//                    }
//                    result.put("eleValue",eleValue);
//                    result.put("outLetId",outLetId);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }else {
//                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
//
//                // 创建时间分布搜索请求
//                SearchRequest cabinetOutLetTotalDayRequest = new SearchRequest("cabinet_eq_outlet_day");
//                SearchSourceBuilder cabinetOutLetTotalDaySourceBuilder = new SearchSourceBuilder();
//
//                cabinetOutLetTotalDaySourceBuilder.query(QueryBuilders.boolQuery()
//                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1).withHour(7))))
//                        .must(QueryBuilders.termQuery("cabinet_id", Id)));
//
//                //映射sum的sum_ele_active为sumEle
//                Map<String, String> map = new HashMap<>();
//                map.put("sumEq", "sum_eq");
//                cabinetOutLetTotalDaySourceBuilder.aggregation(AggregationBuilders
//                        .terms("by_outlet_id").field("outlet_id").order(BucketOrder.aggregation("sum_eq",true)).size(24)
//                        .subAggregation(AggregationBuilders.sum("sum_eq").field("eq_value"))
//                        //筛选sumEle > 0的
//                        .subAggregation(PipelineAggregatorBuilders.bucketSelector("positive_sum_eq",map, new Script("params.sumEq > 0"))));
//
//                cabinetOutLetTotalDaySourceBuilder.size(0); // 设置返回的最大结果数
//                // 将搜索条件添加到请求中
//                cabinetOutLetTotalDayRequest.source(cabinetOutLetTotalDaySourceBuilder);
//                // 将第搜索请求添加到多索引搜索请求中
//                multiSearchRequest.add(cabinetOutLetTotalDayRequest);
//
//                List<Double> eqValue = new ArrayList<>();
//                List<String> outLetId = new ArrayList<>();
//                try {
//                    // 执行多索引搜索请求
//                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);
//
//                    // 解析搜索请求
//                    SearchResponse cabinetPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
//                    if(cabinetPowTotalRealDisResponse != null){
//                        for ( Terms.Bucket bucket : ((ParsedLongTerms) cabinetPowTotalRealDisResponse.getAggregations().get("by_outlet_id")).getBuckets()) {
//                            String outlet = bucket.getKeyAsString();
//                            Sum sum = bucket.getAggregations().get("sum_eq");
//                            double sumEleActive = sum.getValue();
//                            eqValue.add(sumEleActive);
//                            outLetId.add(outlet);
//                        }
//                    }
//                    result.put("eleValue",eqValue);
//                    result.put("outLetId",outLetId);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            return result;
//        } else {
//            return result;
//        }
//
//    }

    @Override
    public Map getReportTemDataById(String Id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        if(Id != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                if(oldTime.equals(newTime)){
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest cabinetTemRealRequest = new SearchRequest("cabinet_env_hour");
                SearchSourceBuilder cabinetTemRealSourceBuilder = new SearchSourceBuilder();

                cabinetTemRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("cabinet_id", Id)));


                cabinetTemRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                cabinetTemRealSourceBuilder.size(24 * 6); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                cabinetTemRealRequest.source(cabinetTemRealSourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(cabinetTemRealRequest);

                List<List<Double>> temAvgValue = new ArrayList<>();
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                List<List<String>> time = new ArrayList<>();
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                double temMaxValue = -255;
                DateTime temMaxTime = null;
                int temMaxSensorId = -1;
                double temMinValue = Double.MAX_VALUE;
                DateTime temMinTime = null;
                int temMinSensorId = -1;
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse cabinetTemRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(cabinetTemRealDisResponse != null) {
                        for (SearchHit hit : cabinetTemRealDisResponse.getHits()) {
                            CabinetEnvHourDo cabinetEnvHourDo = JsonUtils.parseObject(hit.getSourceAsString(), CabinetEnvHourDo.class);
                            double temAvg = cabinetEnvHourDo.getTemAvgValue();
                            DateTime createTime = new DateTime(cabinetEnvHourDo.getCreateTime());
                            if (cabinetEnvHourDo.getTemMaxValue() > temMaxValue) {
                                temMaxValue = cabinetEnvHourDo.getTemMaxValue();
                                temMaxTime = new DateTime(cabinetEnvHourDo.getTemMaxTime());
                                temMaxSensorId = cabinetEnvHourDo.getSensorId();
                            }
                            if (cabinetEnvHourDo.getTemMinValue() > 0 && cabinetEnvHourDo.getTemMinValue() < temMinValue) {
                                temMinValue = cabinetEnvHourDo.getTemMinValue();
                                temMinTime = new DateTime(cabinetEnvHourDo.getTemMinTime());
                                temMinSensorId = cabinetEnvHourDo.getSensorId();
                            }
                            temAvgValue.get(cabinetEnvHourDo.getSensorId()).add(temAvg);
                            time.get(cabinetEnvHourDo.getSensorId()).add(createTime.toString("yyyy-MM-dd HH:mm"));
                        }
                    }
                    result.put("temAvgValue",temAvgValue);
                    result.put("time",time);
                    result.put("temMaxValue",temMaxValue != -255 ? temMaxValue : null);
                    result.put("temMaxTime",temMaxValue != -255 ? temMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("temMaxSensorId",temMaxValue != -255 ? temMaxSensorId : null);
                    result.put("temMinValue",temMinValue != Double.MAX_VALUE ? temMinValue : null);
                    result.put("temMinTime",temMinValue != Double.MAX_VALUE ? temMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("temMinSensorId",temMinValue != Double.MAX_VALUE ? temMinSensorId : null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest cabinetTemDayRequest = new SearchRequest("cabinet_env_day");
                SearchSourceBuilder cabinetTemDaySourceBuilder = new SearchSourceBuilder();

                cabinetTemDaySourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("cabinet_id", Id)));


                cabinetTemDaySourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                cabinetTemDaySourceBuilder.size(31 * 6); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                cabinetTemDayRequest.source(cabinetTemDaySourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(cabinetTemDayRequest);

                List<List<Double>> temAvgValue = new ArrayList<>();
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                List<List<String>> time = new ArrayList<>();
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                double temMaxValue = -255;
                DateTime temMaxTime = null;
                int temMaxSensorId = -1;
                double temMinValue = Double.MAX_VALUE;
                DateTime temMinTime = null;
                int temMinSensorId = -1;
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse cabinetTemRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(cabinetTemRealDisResponse != null) {
                        for (SearchHit hit : cabinetTemRealDisResponse.getHits()) {
                            CabinetEnvDayDo cabinetEnvHourDo = JsonUtils.parseObject(hit.getSourceAsString(), CabinetEnvDayDo.class);
                            double temAvg = cabinetEnvHourDo.getTemAvgValue();
                            DateTime createTime = new DateTime(cabinetEnvHourDo.getCreateTime());
                            createTime = new DateTime(createTime.toLocalDateTime().minusDays(1));
                            if (cabinetEnvHourDo.getTemMaxValue() > temMaxValue) {
                                temMaxValue = cabinetEnvHourDo.getTemMaxValue();
                                temMaxTime = new DateTime(cabinetEnvHourDo.getTemMaxTime());
                                temMaxSensorId = cabinetEnvHourDo.getSensorId();
                            }
                            if (cabinetEnvHourDo.getTemMinValue() > 0 && cabinetEnvHourDo.getTemMinValue() < temMinValue) {
                                temMinValue = cabinetEnvHourDo.getTemMinValue();
                                temMinTime = new DateTime(cabinetEnvHourDo.getTemMinTime());
                                temMinSensorId = cabinetEnvHourDo.getSensorId();
                            }
                            temAvgValue.get(cabinetEnvHourDo.getSensorId()).add(temAvg);
                            time.get(cabinetEnvHourDo.getSensorId()).add(createTime.toString("yyyy-MM-dd"));
                        }
                    }
                    result.put("temAvgValue",temAvgValue);
                    result.put("time",time);
                    result.put("temMaxValue",temMaxValue != -255 ? temMaxValue : null);
                    result.put("temMaxTime",temMaxValue != -255 ? temMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("temMaxSensorId",temMaxValue != -255 ? temMaxSensorId : null);
                    result.put("temMinValue",temMinValue != Double.MAX_VALUE ? temMinValue : null);
                    result.put("temMinTime",temMinValue != Double.MAX_VALUE ? temMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("temMinSensorId",temMinValue != Double.MAX_VALUE ? temMinSensorId : null);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }else{
            return result;
        }
    }

    @Override
    public PageResult<CabinetEnvAndHumRes> getCabinetEnvPage(IndexPageReqVO pageReqVO) {

        PageResult<IndexDO> indexDOPageResult = cabIndexMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<IndexDO>()
                .inIfPresent(IndexDO::getId, pageReqVO.getCabinetIds()).ne(IndexDO::getPduBox,1));
        List<CabinetEnvAndHumRes> result = new ArrayList<>();
        List<TemColorDO> temColorList = temColorService.getTemColorAll();
        for (IndexDO indexDO : indexDOPageResult.getList()) {
            CabinetEnvAndHumRes res = new CabinetEnvAndHumRes();
            result.add(res);
            String localtion = null;
            RoomIndex roomIndex = roomIndexMapper.selectById(indexDO.getRoomId());
            String roomName = roomIndex.getName();
            if(indexDO.getAisleId() != 0){
                String aisleName = aisleIndexMapper.selectById(indexDO.getAisleId()).getName();
                localtion = roomName + "-" + aisleName + "-" + indexDO.getName();
            }else {
                localtion = roomName + "-"  + indexDO.getName() ;
            }
            res.setLocation(localtion);
            res.setId(indexDO.getId());
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, indexDO.getId()));
            if (cabinetPdu == null){
                continue;
            }
            List<CabinetEnvSensor> envList = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, indexDO.getId())
                    .eq(CabinetEnvSensor::getType,1));
            if(envList == null || envList.size() == 0){
                continue;
            }
            for (CabinetEnvSensor cabinetEnvSensor : envList) {
                String devKey = null;
                if(cabinetEnvSensor.getPathPdu() == 'A'){
                    devKey = cabinetPdu.getPduIpA() + '-' + cabinetPdu.getCasIdA();
                }else{
                    devKey = cabinetPdu.getPduIpB() + '-' + cabinetPdu.getCasIdB();
                }
                List<PduIndex> pduList = pduIndexMapper.selectList(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
                if (pduList == null || pduList.size() == 0){
                    continue;
                }
                PduIndex pdu = pduList.get(0);
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                SearchRequest pduEnvRealtimeRequest = new SearchRequest("pdu_env_realtime");
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

                searchSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("pdu_id", pdu.getId()))
                        .must(QueryBuilders.termQuery("sensor_id", cabinetEnvSensor.getSensorId())));
                searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
                searchSourceBuilder.size(1); // 设置返回的最大结果数
                pduEnvRealtimeRequest.source(searchSourceBuilder);
                multiSearchRequest.add(pduEnvRealtimeRequest);

                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    SearchResponse response = multiSearchResponse.getResponses()[0].getResponse();
                    if (response != null) {
                        SearchHits hits = response.getHits();
                        if (hits.getTotalHits().value > 0) {
                            SearchHit hit = hits.getAt(0);
                            PduEnvRealtimeDo pduEnvRealtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEnvRealtimeDo.class);
                            if (cabinetEnvSensor.getChannel() == 1 && cabinetEnvSensor.getPosition() == 1) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setIceTopTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setIceTopTem(tem);
                                res.setIceTopHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 1 && cabinetEnvSensor.getPosition() == 2) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setIceMidTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setIceMidTem(tem);
                                res.setIceMidHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 1 && cabinetEnvSensor.getPosition() == 3) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setIceBomTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setIceBomTem(tem);
                                res.setIceBomHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 2 && cabinetEnvSensor.getPosition() == 1) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setHotTopTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setHotTopTem(tem);
                                res.setHotTopHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 2 && cabinetEnvSensor.getPosition() == 2) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setHotMidTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setHotMidTem(tem);
                                res.setHotMidHum(hum);
                            } else if (cabinetEnvSensor.getChannel() == 2 && cabinetEnvSensor.getPosition() == 3) {
                                Double tem = new Double(pduEnvRealtimeDo.getTem());
                                Double hum = new Double(pduEnvRealtimeDo.getHum());
                                for (TemColorDO temColorDO : temColorList) {
                                    if(tem >= temColorDO.getMin() && tem <= temColorDO.getMax()){
                                        res.setHotBomTemColor(temColorDO.getColor());
                                    }
                                }
                                res.setHotBomTem(tem);
                                res.setHotBomHum(hum);
                            }
                        }
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        return new PageResult<CabinetEnvAndHumRes>(result,indexDOPageResult.getTotal());
    }

    @Override
    public Map getCabinetEnvIceTemAndHumData(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id),false);
        if (cabinetPdu == null){
            return null;
        }
        List<List> tem = new ArrayList<>();
        tem.add(new ArrayList());
        tem.add(new ArrayList());
        tem.add(new ArrayList());
        tem.add(new ArrayList());
        List<List> hum = new ArrayList<>();
        hum.add(new ArrayList());
        hum.add(new ArrayList());
        hum.add(new ArrayList());
        hum.add(new ArrayList());
        List<String> time = null;
        boolean firstTime = false;
        for (int position = 1; position <= 3; position++) {
            List<String> createTimeList = new ArrayList<>();
            CabinetEnvSensor cabinetEnvSensor = cabinetEnvSensorMapper.selectOne(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 1)
                    .eq(CabinetEnvSensor::getPosition, position)
                    .eq(CabinetEnvSensor::getSensorId, 1), false);
            if (cabinetEnvSensor == null){
                continue;
            }
            String devKey = null;
            if(cabinetEnvSensor.getPathPdu() == 'A'){
                devKey = cabinetPdu.getPduIpA() + '-' + cabinetPdu.getCasIdA();
            }else{
                devKey = cabinetPdu.getPduIpB() + '-' + cabinetPdu.getCasIdB();
            }
            PduIndex pduIndex = pduIndexMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey), false);
            if (pduIndex == null){
                continue;
            }
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
            String whichIndex = "pdu_env_hour";
            if (timeType == 2){
                whichIndex = "pdu_env_day";
            }

            SearchRequest pduEnvRealtimeRequest = new SearchRequest(whichIndex);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            if(timeType == 1){
                LocalDateTime now = LocalDateTime.now();
                oldTime = now.minusHours(25);
                newTime = now;
            } else if (timeType == 2) {
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            searchSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("pdu_id", pduIndex.getId()))
                    .must(QueryBuilders.termQuery("sensor_id", cabinetEnvSensor.getSensorId()))
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime))));

            searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            pduEnvRealtimeRequest.source(searchSourceBuilder);
            multiSearchRequest.add(pduEnvRealtimeRequest);

            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                SearchResponse response = multiSearchResponse.getResponses()[0].getResponse();
                if (response != null) {
                    for (SearchHit hit : response.getHits()) {
                        PduEnvHourDo pduEnvHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEnvHourDo.class);
                        tem.get(position).add(pduEnvHourDo.getTemAvgValue());
                        hum.get(position).add(pduEnvHourDo.getHumAvgValue());
                        createTimeList.add(pduEnvHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm"));
                    }
                }
                if (!firstTime){
                    time = createTimeList;
                    firstTime = true;
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        result.put("temAvgValue",tem);
        result.put("humAvgValue",hum);
        result.put("time",time);
        return result;
    }

    @Override
    public Map getCabinetEnvHotTemAndHumData(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id),false);
        if (cabinetPdu == null){
            return null;
        }
        List<List> tem = new ArrayList<>();
        tem.add(new ArrayList());
        tem.add(new ArrayList());
        tem.add(new ArrayList());
        tem.add(new ArrayList());
        List<List> hum = new ArrayList<>();
        hum.add(new ArrayList());
        hum.add(new ArrayList());
        hum.add(new ArrayList());
        hum.add(new ArrayList());
        List<String> time = null;
        boolean firstTime = false;
        for (int position = 1; position <= 3; position++) {
            List<String> createTimeList = new ArrayList<>();
            CabinetEnvSensor cabinetEnvSensor = cabinetEnvSensorMapper.selectOne(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 2)
                    .eq(CabinetEnvSensor::getPosition, position)
                    .eq(CabinetEnvSensor::getSensorId, 1), false);
            if (cabinetEnvSensor == null){
                continue;
            }
            String devKey = null;
            if(cabinetEnvSensor.getPathPdu() == 'A'){
                devKey = cabinetPdu.getPduIpA() + '-' + cabinetPdu.getCasIdA();
            }else{
                devKey = cabinetPdu.getPduIpB() + '-' + cabinetPdu.getCasIdB();
            }
            PduIndex pduIndex = pduIndexMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey), false);
            if (pduIndex == null){
                continue;
            }
            MultiSearchRequest multiSearchRequest = new MultiSearchRequest();
            String whichIndex = "pdu_env_hour";
            if (timeType == 2){
                whichIndex = "pdu_env_day";
            }

            SearchRequest pduEnvRealtimeRequest = new SearchRequest(whichIndex);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            if(timeType == 1){
                LocalDateTime now = LocalDateTime.now();
                oldTime = now.minusHours(25);
                newTime = now;
            } else if (timeType == 2) {
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            searchSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("pdu_id", pduIndex.getId()))
                    .must(QueryBuilders.termQuery("sensor_id", cabinetEnvSensor.getSensorId()))
                    .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime))));


            searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);

            pduEnvRealtimeRequest.source(searchSourceBuilder);
            multiSearchRequest.add(pduEnvRealtimeRequest);

            try {
                // 执行多索引搜索请求
                MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                SearchResponse response = multiSearchResponse.getResponses()[0].getResponse();
                if (response != null) {
                    for (SearchHit hit : response.getHits()) {
                        PduEnvHourDo pduEnvHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEnvHourDo.class);
                        tem.get(position).add(pduEnvHourDo.getTemAvgValue());
                        hum.get(position).add(pduEnvHourDo.getHumAvgValue());
                        createTimeList.add(pduEnvHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm"));
                    }
                }
                if (!firstTime){
                    time = createTimeList;
                    firstTime = true;
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        result.put("temAvgValue",tem);
        result.put("humAvgValue",hum);
        result.put("time",time);
        return result;
    }

}