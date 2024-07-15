package cn.iocoder.yudao.module.room.service.roomindex;
import cn.hutool.core.date.DateTime;

import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.room.pow.RoomPowHourDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.alibaba.fastjson2.JSON;
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

import cn.iocoder.yudao.module.room.controller.admin.roomindex.vo.*;
import cn.iocoder.yudao.module.room.dal.dataobject.roomindex.RoomIndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.room.dal.mysql.roomindex.RoomIndexCopyMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.room.enums.ErrorCodeConstants.*;

/**
 * 机房索引 Service 实现类
 *
 * @author clever
 */
@Service
@Slf4j
@Validated
public class RoomIndexServiceImpl implements RoomIndexService {

    @Resource
    private RoomIndexCopyMapper roomIndexCopyMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public Integer createIndex(RoomIndexSaveReqVO createReqVO) {
        // 插入
        RoomIndexDO index = BeanUtils.toBean(createReqVO, RoomIndexDO.class);
        roomIndexCopyMapper.insert(index);
        // 返回
        return index.getId();
    }

    @Override
    public void updateIndex(RoomIndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        RoomIndexDO updateObj = BeanUtils.toBean(updateReqVO, RoomIndexDO.class);
        roomIndexCopyMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Integer id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        roomIndexCopyMapper.deleteById(id);
    }

    private void validateIndexExists(Integer id) {
        if (roomIndexCopyMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public RoomIndexDO getIndex(Integer id) {
        return roomIndexCopyMapper.selectById(id);
    }

    @Override
    public PageResult<RoomIndexDO> getIndexPage(RoomIndexPageReqVO pageReqVO) {
        return roomIndexCopyMapper.selectPage(pageReqVO);
    }

    @Override
    public Map getReportConsumeDataById(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase barRes = new LineResBase();
        BarSeries barSeries = new BarSeries();
        try {
            if(id != null){
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "room_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "room_eq_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay){
                    for (String str : cabinetData) {
                        nowTimes++;
                        RoomEleTotalRealtimeDo roomEleTotalRealtimeDo = JsonUtils.parseObject(str, RoomEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = roomEleTotalRealtimeDo.getEleTotal();
                        }
                        if (nowTimes > 1){
                            barSeries.getData().add((float)(roomEleTotalRealtimeDo.getEleTotal() -lastEq));
                            barRes.getTime().add(roomEleTotalRealtimeDo.getCreateTime().toString("HH:mm:ss"));
                        }
                        lastEq = roomEleTotalRealtimeDo.getEleTotal();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "ele_total");
                    RoomEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, RoomEleTotalRealtimeDo.class);
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
                    for (String str : cabinetData) {
                        nowTimes++;
                        RoomEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, RoomEqTotalDayDo.class);
                        totalEq += totalDayDo.getEqValue();
                        barSeries.getData().add((float)totalDayDo.getEqValue());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "eq_value");
                    RoomEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, RoomEqTotalDayDo.class);
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
    public Map getReportPowDataById(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase totalLineRes = new LineResBase();
        LineResBase aLineRes = new LineResBase();
        LineResBase bLineRes = new LineResBase();

        result.put("totalLineRes",totalLineRes);
        result.put("aLineRes",aLineRes);
        result.put("bLineRes",bLineRes);

        result.put("apparentPowMaxValue",null);
        result.put("apparentPowMaxTime",null);
        result.put("apparentPowMinValue",null);
        result.put("apparentPowMinTime",null);
        result.put("activePowMaxValue", null);
        result.put("activePowMaxTime",  null);
        result.put("activePowMinValue", null);
        result.put("activePowMinTime",  null);

        result.put("AapparentPowMaxValue",  null);
        result.put("AapparentPowMaxTime",   null);
        result.put("AapparentPowMinValue",  null);
        result.put("AapparentPowMinTime",   null);
        result.put("AactivePowMaxValue",    null);
        result.put("AactivePowMaxTime",     null);
        result.put("AactivePowMinValue",    null);
        result.put("AactivePowMinTime",     null);

        result.put("BapparentPowMaxValue",  null);
        result.put("BapparentPowMaxTime",   null);
        result.put("BapparentPowMinValue",  null);
        result.put("BapparentPowMinTime",   null);
        result.put("BactivePowMaxValue",    null);
        result.put("BactivePowMaxTime",     null);
        result.put("BactivePowMinValue",    null);
        result.put("BactivePowMinTime",     null);
        try {
            if(id != null) {
                String index = null;

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "room_hda_pow_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "room_hda_pow_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                List<RoomPowHourDo> roomPowHourDos = cabinetData.stream().map(str -> JsonUtils.parseObject(str, RoomPowHourDo.class)).collect(Collectors.toList());


                LineSeries totalApparentPow = new LineSeries();
                totalApparentPow.setName("总平均视在功率");
                LineSeries totalActivePow = new LineSeries();
                totalActivePow.setName("总平均有功功率");
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);


                LineSeries apparentPowA = new LineSeries();
                apparentPowA.setName("A路平均视在功率");
                LineSeries activePowA = new LineSeries();
                activePowA.setName("A路平均有功功率");
                aLineRes.getSeries().add(apparentPowA);
                aLineRes.getSeries().add(activePowA);


                LineSeries apparentPowB = new LineSeries();
                apparentPowB.setName("B路平均视在功率");
                LineSeries activePowB = new LineSeries();
                activePowB.setName("B路平均有功功率");
                bLineRes.getSeries().add(apparentPowB);
                bLineRes.getSeries().add(activePowB);

                if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                    roomPowHourDos.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm:ss"));

                        apparentPowA.getData().add(hourdo.getApparentAAvgValue());
                        activePowA.getData().add(hourdo.getActiveAAvgValue());
                        aLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm:ss"));

                        apparentPowB.getData().add(hourdo.getApparentBAvgValue());
                        activePowB.getData().add(hourdo.getActiveBAvgValue());
                        bLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm:ss"));
                    });
                }else{
                    roomPowHourDos.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));

                        apparentPowA.getData().add(hourdo.getApparentAAvgValue());
                        activePowA.getData().add(hourdo.getActiveAAvgValue());
                        aLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));

                        apparentPowB.getData().add(hourdo.getApparentBAvgValue());
                        activePowB.getData().add(hourdo.getActiveBAvgValue());
                        bLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));
                    });
                }

                String apparentTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_total_max_value");
                RoomPowHourDo totalMaxApparent = JsonUtils.parseObject(apparentTotalMaxValue, RoomPowHourDo.class);
                String apparentTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_total_min_value");
                RoomPowHourDo totalMinApparent = JsonUtils.parseObject(apparentTotalMinValue, RoomPowHourDo.class);

                String activeTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_total_max_value");
                RoomPowHourDo totalMaxActive = JsonUtils.parseObject(activeTotalMaxValue, RoomPowHourDo.class);
                String activeTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_total_min_value");
                RoomPowHourDo totalMinActive = JsonUtils.parseObject(activeTotalMinValue, RoomPowHourDo.class);


                String apparentAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_a_max_value");
                RoomPowHourDo maxApparentA = JsonUtils.parseObject(apparentAMaxValue, RoomPowHourDo.class);
                String apparentAMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_a_min_value");
                RoomPowHourDo minApparentA = JsonUtils.parseObject(apparentAMinValue, RoomPowHourDo.class);

                String activeAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_a_max_value");
                RoomPowHourDo maxActiveA = JsonUtils.parseObject(activeAMaxValue, RoomPowHourDo.class);
                String activeAMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_a_min_value");
                RoomPowHourDo minActiveA = JsonUtils.parseObject(activeAMinValue, RoomPowHourDo.class);

                String apparentBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_b_max_value");
                RoomPowHourDo maxApparentB = JsonUtils.parseObject(apparentBMaxValue, RoomPowHourDo.class);
                String apparentBMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_b_min_value");
                RoomPowHourDo minApparentB = JsonUtils.parseObject(apparentBMinValue, RoomPowHourDo.class);

                String activeBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_b_max_value");
                RoomPowHourDo maxActiveB = JsonUtils.parseObject(activeBMaxValue, RoomPowHourDo.class);
                String activeBMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_b_min_value");
                RoomPowHourDo minActiveB = JsonUtils.parseObject(activeBMinValue, RoomPowHourDo.class);

                result.put("totalLineRes",totalLineRes);
                result.put("aLineRes",aLineRes);
                result.put("bLineRes",bLineRes);

                result.put("apparentPowMaxValue",totalMaxApparent.getApparentTotalMaxValue());
                result.put("apparentPowMaxTime",totalMaxApparent.getApparentTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("apparentPowMinValue",totalMinApparent.getApparentTotalMinValue());
                result.put("apparentPowMinTime",totalMinApparent.getApparentTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMaxValue",totalMaxActive.getActiveTotalMaxValue());
                result.put("activePowMaxTime",totalMaxActive.getActiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMinValue",totalMinActive.getActiveTotalMinValue());
                result.put("activePowMinTime",totalMinActive.getActiveTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"));

                result.put("AapparentPowMaxValue",maxApparentA.getApparentAMaxValue());
                result.put("AapparentPowMaxTime",maxApparentA.getApparentAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("AapparentPowMinValue",minApparentA.getApparentAMinValue());
                result.put("AapparentPowMinTime",minApparentA.getApparentAMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("AactivePowMaxValue",maxActiveA.getActiveAMaxValue());
                result.put("AactivePowMaxTime",maxActiveA.getActiveAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("AactivePowMinValue",minActiveA.getActiveAMinValue());
                result.put("AactivePowMinTime",minActiveA.getActiveAMinTime().toString("yyyy-MM-dd HH:mm:ss"));

                result.put("BapparentPowMaxValue",  maxApparentB.getApparentBMaxValue());
                result.put("BapparentPowMaxTime",   maxApparentB.getApparentBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("BapparentPowMinValue",  minApparentB.getApparentBMinValue());
                result.put("BapparentPowMinTime",   minApparentB.getApparentBMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("BactivePowMaxValue",    maxActiveB.getActiveBMaxValue());
                result.put("BactivePowMaxTime",     maxActiveB.getActiveBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("BactivePowMinValue",    minActiveB.getActiveBMinValue());
                result.put("BactivePowMinTime",     minActiveB.getActiveBMinTime().toString("yyyy-MM-dd HH:mm:ss"));

            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getRoomPFLine(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase totalLineRes = new LineResBase();
        result.put("pfLineRes",totalLineRes);
        try {

            String index = null;

            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                index = "room_hda_pow_hour";
                if (oldTime.equals(newTime)) {
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }

            } else {
                index = "room_hda_pow_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }

            String startTime = localDateTimeToString(oldTime);
            String endTime = localDateTimeToString(newTime);
            List<String> data = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
            List<RoomPowHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, RoomPowHourDo.class)).collect(Collectors.toList());

            LineSeries totalPFLine = new LineSeries();
            totalPFLine.setName("总平均功率因素");
            LineSeries PFLineA = new LineSeries();
            PFLineA.setName("A路功率因素");
            LineSeries PFLineB = new LineSeries();
            PFLineB.setName("B路功率因素");

            totalLineRes.getSeries().add(totalPFLine);
            totalLineRes.getSeries().add(PFLineA);
            totalLineRes.getSeries().add(PFLineB);

            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getFactorTotalAvgValue());
                    PFLineA.getData().add(hourdo.getFactorAAvgValue());
                    PFLineB.getData().add(hourdo.getFactorBAvgValue());
                    DateTime dateTime = new DateTime(hourdo.getCreateTime());
                    totalLineRes.getTime().add(dateTime.toString("HH:mm"));

                });
            }else{
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getFactorTotalAvgValue());
                    PFLineA.getData().add(hourdo.getFactorAAvgValue());
                    PFLineB.getData().add(hourdo.getFactorBAvgValue());
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

    @Override
    public PageResult<RoomBalanceRes> getRoomBalancePage(RoomIndexPageReqVO pageReqVO) {
        PageResult<RoomIndexDO> roomIndexDOPageResult = roomIndexCopyMapper.selectPage(pageReqVO);
        List<RoomBalanceRes> result = new ArrayList<>();
        List<RoomIndexDO> roomIndexDOList = roomIndexDOPageResult.getList();
        List mutiRedis = getMutiRedis(roomIndexDOList);
        roomIndexDOList.forEach(aisleIndexDO -> {
            RoomBalanceRes aisleBalanceRes = new RoomBalanceRes();
            aisleBalanceRes.setId(aisleIndexDO.getId());
            aisleBalanceRes.setName(aisleIndexDO.getName());
            aisleBalanceRes.setLocation(aisleIndexDO.getName());
            result.add(aisleBalanceRes);
        });
        Map<Integer, RoomBalanceRes> resMap = result.stream().collect(Collectors.toMap(RoomBalanceRes::getId, Function.identity()));
        for (Object o : mutiRedis) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            Integer aisleKey = jsonObject.getInteger("room_key") ;
            RoomBalanceRes aisleBalanceRes = resMap.get(aisleKey);
            JSONObject roomPower = jsonObject.getJSONObject("room_power");
            JSONObject totalData = roomPower.getJSONObject("total_data");
            if (Objects.nonNull(totalData)){
                aisleBalanceRes.setPowApparentTotal(totalData.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveTotal(totalData.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveTotal(totalData.getDouble("pow_reactive"));
            }

            JSONObject pathA = roomPower.getJSONObject("path_a");
            if (Objects.nonNull(pathA)){
                aisleBalanceRes.setPowApparentA(pathA.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveA(pathA.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveA(pathA.getDouble("pow_reactive"));
            }

            JSONObject pathB = roomPower.getJSONObject("path_b");
            if (Objects.nonNull(pathB)){
                aisleBalanceRes.setPowApparentB(pathB.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveB(pathB.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveB(pathB.getDouble("pow_reactive"));
            }

            if(aisleBalanceRes.getPowApparentA() != null && aisleBalanceRes.getPowApparentA() != 0 && aisleBalanceRes.getPowApparentTotal() != null && aisleBalanceRes.getPowApparentTotal() != 0){
                aisleBalanceRes.setRateA((aisleBalanceRes.getPowApparentA() / aisleBalanceRes.getPowApparentTotal())*100);
            }
        }
        return new PageResult<>(result,roomIndexDOPageResult.getTotal());
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
                .must(QueryBuilders.termsQuery("room_id", ids))));
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
                .must(QueryBuilders.termsQuery("room_id", ids))));
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
                .must(QueryBuilders.termsQuery("room_id", ids))));
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

    private List getMutiRedis(List<RoomIndexDO> list){
        List<String> devKeys = list.stream().map(busIndexDo -> REDIS_KEY_ROOM + busIndexDo.getId()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }
    
    private String localDateTimeToString(LocalDateTime time){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
    }

}