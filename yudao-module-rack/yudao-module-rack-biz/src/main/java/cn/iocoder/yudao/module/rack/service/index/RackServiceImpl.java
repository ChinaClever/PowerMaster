package cn.iocoder.yudao.module.rack.service.index;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.rack.ele.RackEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.rack.ele.RackEqTotalDayDo;

import cn.iocoder.yudao.framework.common.entity.es.rack.pow.RackPowHourDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rack.enums.ErrorCodeConstants.*;

/**
 * 机架索引 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
@Slf4j
public class RackServiceImpl implements RackService {

    @Resource
    private RackMapper rackMapper;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private RedisTemplate redisTemplate;

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
    public Map getReportConsumeDataById(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase barRes = new LineResBase();
        BarSeries barSeries = new BarSeries();
        try {
            if(id != null){
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "rack_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "rack_eq_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> rackData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay){
                    for (String str : rackData) {
                        nowTimes++;
                        RackEleTotalRealtimeDo rackEleTotalRealtimeDo = JsonUtils.parseObject(str, RackEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = rackEleTotalRealtimeDo.getEleTotal();
                        }
                        if (nowTimes > 1){
                            barSeries.getData().add((float)(rackEleTotalRealtimeDo.getEleTotal() -lastEq));
                            barRes.getTime().add(rackEleTotalRealtimeDo.getCreateTime().toString("HH:mm:ss"));
                        }
                        lastEq = rackEleTotalRealtimeDo.getEleTotal();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "ele_total");
                    RackEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, RackEleTotalRealtimeDo.class);
                    if(eleMaxValue != null){
                        maxEle = eleMaxValue.getEleTotal();
                        maxEleTime = eleMaxValue.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("totalEle",totalEq);
                    result.put("maxEle",maxEle);
                    result.put("maxEleTime",maxEleTime);
                    result.put("firstEq",firstEq);
                    result.put("lastEq",lastEq);
                    result.put("barRes",barRes);
                }else {
                    for (String str : rackData) {
                        nowTimes++;
                        RackEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, RackEqTotalDayDo.class);
                        totalEq += totalDayDo.getEqValue();
                        barSeries.getData().add((float)totalDayDo.getEqValue());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "eq_value");
                    RackEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, RackEqTotalDayDo.class);
                    if(eqMaxValue != null){
                        maxEle = eqMaxValue.getEqValue();
                        maxEleTime = eqMaxValue.getStartTime().toString("yyyy-MM-dd HH:mm:ss");
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("totalEle",totalEq);
                    result.put("maxEle",maxEle);
                    result.put("maxEleTime",maxEleTime);
                    result.put("barRes",barRes);
                }
            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getReportPowDataById(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase totalLineRes = new LineResBase();
        
        result.put("totalLineRes",totalLineRes);
        
        result.put("apparentPowMaxValue",null);
        result.put("apparentPowMaxTime",null);
        result.put("apparentPowMinValue",null);
        result.put("apparentPowMinTime",null);
        result.put("activePowMaxValue", null);
        result.put("activePowMaxTime",  null);
        result.put("activePowMinValue", null);
        result.put("activePowMinTime",  null);

        try {
            if(id != null) {
                String index = null;

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "rack_hda_pow_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "rack_hda_pow_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                List<RackPowHourDo> roomPowHourDos = cabinetData.stream().map(str -> JsonUtils.parseObject(str, RackPowHourDo.class)).collect(Collectors.toList());


                LineSeries totalApparentPow = new LineSeries();
                totalApparentPow.setName("总平均视在功率");
                LineSeries totalActivePow = new LineSeries();
                totalActivePow.setName("总平均有功功率");
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);
                

                if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                    roomPowHourDos.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm:ss"));

                    });
                }else{
                    roomPowHourDos.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));
                        
                    });
                }

                String apparentTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_total_max_value");
                RackPowHourDo totalMaxApparent = JsonUtils.parseObject(apparentTotalMaxValue, RackPowHourDo.class);
                String apparentTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_total_min_value");
                RackPowHourDo totalMinApparent = JsonUtils.parseObject(apparentTotalMinValue, RackPowHourDo.class);

                String activeTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_total_max_value");
                RackPowHourDo totalMaxActive = JsonUtils.parseObject(activeTotalMaxValue, RackPowHourDo.class);
                String activeTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_total_min_value");
                RackPowHourDo totalMinActive = JsonUtils.parseObject(activeTotalMinValue, RackPowHourDo.class);

                

                result.put("totalLineRes",totalLineRes);

                result.put("apparentPowMaxValue",totalMaxApparent.getApparentTotalMaxValue());
                result.put("apparentPowMaxTime",totalMaxApparent.getApparentTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("apparentPowMinValue",totalMinApparent.getApparentTotalMinValue());
                result.put("apparentPowMinTime",totalMinApparent.getApparentTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMaxValue",totalMaxActive.getActiveTotalMaxValue());
                result.put("activePowMaxTime",totalMaxActive.getActiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMinValue",totalMinActive.getActiveTotalMinValue());
                result.put("activePowMinTime",totalMinActive.getActiveTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"));

            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public String getRackRedisById(Integer id) {
        if (id == null){
            return null;
        }else {
            ValueOperations ops = redisTemplate.opsForValue();
            JSONObject jsonObject = (JSONObject) ops.get("packet:rack:" + id);
            return jsonObject != null ? jsonObject.toJSONString() : null;
        }
    }

    @Override
    public Map getRoomPFLine(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase totalLineRes = new LineResBase();
        result.put("pfLineRes",totalLineRes);
        try {

            String index = null;

            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                index = "rack_hda_pow_hour";
                if (oldTime.equals(newTime)) {
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }

            } else {
                index = "rack_hda_pow_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }

            String startTime = localDateTimeToString(oldTime);
            String endTime = localDateTimeToString(newTime);
            List<String> data = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
            List<RackPowHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, RackPowHourDo.class)).collect(Collectors.toList());

            LineSeries totalPFLine = new LineSeries();
            totalPFLine.setName("总平均功率因素");

            totalLineRes.getSeries().add(totalPFLine);

            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());
                    DateTime dateTime = new DateTime(hourdo.getCreateTime());
                    totalLineRes.getTime().add(dateTime.toString("HH:mm"));

                });
            }else{
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());
                    DateTime dateTime = new DateTime(hourdo.getCreateTime());
                    totalLineRes.getTime().add(dateTime.toString("yyyy-MM-dd"));
                });
            }
            result.put("pfLineRes",totalLineRes);

        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    /**
     * 获取数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ids 机柜id列表
     * @param index 索引表
     */
    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("rack_id", ids))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(2000);

        List<String> list = new ArrayList<>();
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                list.add(str);
            }
        }
        return list;

    }

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index,String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("rack_id", ids))));
        builder.sort(order, SortOrder.DESC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                return str;
            }
        }
        return null;
    }

    private String getMinData(String startTime, String endTime, List<Integer> ids, String index,String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("rack_id", ids))));
        builder.sort(order, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                return str;
            }
        }
        return null;

    }

    private String localDateTimeToString(LocalDateTime time){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
    }

}