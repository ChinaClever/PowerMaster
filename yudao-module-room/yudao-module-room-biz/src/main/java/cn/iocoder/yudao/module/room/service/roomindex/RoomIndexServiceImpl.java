package cn.iocoder.yudao.module.room.service.roomindex;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.room.ele.RoomEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.room.pow.RoomPowHourDo;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.TimeUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.room.controller.admin.roomindex.DTO.RoomEleTotalRealtimeReqDTO;
import cn.iocoder.yudao.module.room.controller.admin.roomindex.vo.*;
import cn.iocoder.yudao.module.room.dal.dataobject.roomindex.RoomIndexDO;
import cn.iocoder.yudao.module.room.dal.mysql.roomindex.RoomIndexCopyMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.room.constant.RoomConstants.KEYWORD;
import static cn.iocoder.yudao.module.room.constant.RoomConstants.*;
import static cn.iocoder.yudao.module.room.enums.ErrorCodeConstants.INDEX_NOT_EXISTS;

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

    public static final String DAY_FORMAT = "dd";

    @Autowired
    private RoomIndexMapper roomIndexMapper;

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
            if (id != null) {
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
                List<String> roomData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay) {
                    for (String str : roomData) {
                        nowTimes++;
                        RoomEleTotalRealtimeDo roomEleTotalRealtimeDo = JsonUtils.parseObject(str, RoomEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = roomEleTotalRealtimeDo.getEleTotal();
                        }
                        if (nowTimes > 1) {
                            barSeries.getData().add((float) (roomEleTotalRealtimeDo.getEleTotal() - lastEq));
                            barRes.getTime().add(roomEleTotalRealtimeDo.getCreateTime().toString("HH:mm:ss"));
                        }
                        lastEq = roomEleTotalRealtimeDo.getEleTotal();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "ele_total");
                    RoomEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, RoomEleTotalRealtimeDo.class);
                    if (eleMaxValue != null) {
                        maxEle = eleMaxValue.getEleTotal();
                        maxEleTime = eleMaxValue.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("totalEle", totalEq);
                    result.put("maxEle", maxEle);
                    result.put("maxEleTime", maxEleTime);
                    result.put("firstEq", firstEq);
                    result.put("lastEq", lastEq);
                    result.put("barRes", barRes);
                } else {
                    for (String str : roomData) {
                        nowTimes++;
                        RoomEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, RoomEqTotalDayDo.class);
                        totalEq += totalDayDo.getEqValue();
                        barSeries.getData().add((float) totalDayDo.getEqValue());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "eq_value");
                    RoomEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, RoomEqTotalDayDo.class);
                    if (eqMaxValue != null) {
                        maxEle = eqMaxValue.getEqValue();
                        maxEleTime = eqMaxValue.getStartTime().toString("yyyy-MM-dd HH:mm:ss");
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("totalEle", totalEq);
                    result.put("maxEle", maxEle);
                    result.put("maxEleTime", maxEleTime);
                    result.put("barRes", barRes);
                }
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public Map getReportPowDataById(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase totalLineRes = new LineResBase();
        LineResBase aLineRes = new LineResBase();
        LineResBase bLineRes = new LineResBase();

        result.put("totalLineRes", totalLineRes);
        result.put("aLineRes", aLineRes);
        result.put("bLineRes", bLineRes);

        result.put("apparentPowMaxValue", null);
        result.put("apparentPowMaxTime", null);
        result.put("apparentPowMinValue", null);
        result.put("apparentPowMinTime", null);
        result.put("activePowMaxValue", null);
        result.put("activePowMaxTime", null);
        result.put("activePowMinValue", null);
        result.put("activePowMinTime", null);

        result.put("AapparentPowMaxValue", null);
        result.put("AapparentPowMaxTime", null);
        result.put("AapparentPowMinValue", null);
        result.put("AapparentPowMinTime", null);
        result.put("AactivePowMaxValue", null);
        result.put("AactivePowMaxTime", null);
        result.put("AactivePowMinValue", null);
        result.put("AactivePowMinTime", null);

        result.put("BapparentPowMaxValue", null);
        result.put("BapparentPowMaxTime", null);
        result.put("BapparentPowMinValue", null);
        result.put("BapparentPowMinTime", null);
        result.put("BactivePowMaxValue", null);
        result.put("BactivePowMaxTime", null);
        result.put("BactivePowMinValue", null);
        result.put("BactivePowMinTime", null);
        try {
            if (id != null) {
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
                List<String> roomData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                List<RoomPowHourDo> roomPowHourDos = roomData.stream().map(str -> JsonUtils.parseObject(str, RoomPowHourDo.class)).collect(Collectors.toList());


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

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
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
                } else {
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

                result.put("totalLineRes", totalLineRes);
                result.put("aLineRes", aLineRes);
                result.put("bLineRes", bLineRes);

                result.put("apparentPowMaxValue", totalMaxApparent.getApparentTotalMaxValue());
                result.put("apparentPowMaxTime", totalMaxApparent.getApparentTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("apparentPowMinValue", totalMinApparent.getApparentTotalMinValue());
                result.put("apparentPowMinTime", totalMinApparent.getApparentTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMaxValue", totalMaxActive.getActiveTotalMaxValue());
                result.put("activePowMaxTime", totalMaxActive.getActiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMinValue", totalMinActive.getActiveTotalMinValue());
                result.put("activePowMinTime", totalMinActive.getActiveTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"));

                result.put("AapparentPowMaxValue", maxApparentA.getApparentAMaxValue());
                result.put("AapparentPowMaxTime", maxApparentA.getApparentAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("AapparentPowMinValue", minApparentA.getApparentAMinValue());
                result.put("AapparentPowMinTime", minApparentA.getApparentAMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("AactivePowMaxValue", maxActiveA.getActiveAMaxValue());
                result.put("AactivePowMaxTime", maxActiveA.getActiveAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("AactivePowMinValue", minActiveA.getActiveAMinValue());
                result.put("AactivePowMinTime", minActiveA.getActiveAMinTime().toString("yyyy-MM-dd HH:mm:ss"));

                result.put("BapparentPowMaxValue", maxApparentB.getApparentBMaxValue());
                result.put("BapparentPowMaxTime", maxApparentB.getApparentBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("BapparentPowMinValue", minApparentB.getApparentBMinValue());
                result.put("BapparentPowMinTime", minApparentB.getApparentBMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("BactivePowMaxValue", maxActiveB.getActiveBMaxValue());
                result.put("BactivePowMaxTime", maxActiveB.getActiveBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("BactivePowMinValue", minActiveB.getActiveBMinValue());
                result.put("BactivePowMinTime", minActiveB.getActiveBMinTime().toString("yyyy-MM-dd HH:mm:ss"));

            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public Map getRoomPFLine(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase totalLineRes = new LineResBase();
        result.put("pfLineRes", totalLineRes);
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

            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getFactorTotalAvgValue());
                    PFLineA.getData().add(hourdo.getFactorAAvgValue());
                    PFLineB.getData().add(hourdo.getFactorBAvgValue());
                    DateTime dateTime = new DateTime(hourdo.getCreateTime());
                    totalLineRes.getTime().add(dateTime.toString("HH:mm"));

                });
            } else {
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getFactorTotalAvgValue());
                    PFLineA.getData().add(hourdo.getFactorAAvgValue());
                    PFLineB.getData().add(hourdo.getFactorBAvgValue());
                    DateTime dateTime = new DateTime(hourdo.getCreateTime());
                    totalLineRes.getTime().add(dateTime.toString("yyyy-MM-dd"));
                });
            }
            result.put("pfLineRes", totalLineRes);

        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public PageResult<RoomBalanceRes> getRoomBalancePage(RoomIndexPageReqVO pageReqVO) {
        PageResult<RoomIndexDO> roomIndexDOPageResult = roomIndexCopyMapper.selectPage(pageReqVO);
        List<RoomBalanceRes> result = new ArrayList<>();
        List<RoomIndexDO> roomIndexDOList = roomIndexDOPageResult.getList();
        List mutiRedis = getMutiRedis(roomIndexDOList);
        roomIndexDOList.forEach(roomIndexDO -> {
            RoomBalanceRes roomBalanceRes = new RoomBalanceRes();
            roomBalanceRes.setId(roomIndexDO.getId());
            roomBalanceRes.setName(roomIndexDO.getRoomName());
            roomBalanceRes.setLocation(roomIndexDO.getRoomName());
            result.add(roomBalanceRes);
        });
        Map<Integer, RoomBalanceRes> resMap = result.stream().collect(Collectors.toMap(RoomBalanceRes::getId, Function.identity()));
        for (Object o : mutiRedis) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            Integer roomKey = jsonObject.getInteger("room_key");
            RoomBalanceRes roomBalanceRes = resMap.get(roomKey);
            JSONObject roomPower = jsonObject.getJSONObject("room_power");
            JSONObject totalData = roomPower.getJSONObject("total_data");
            if (Objects.nonNull(totalData)) {
                roomBalanceRes.setPowApparentTotal(totalData.getDouble("pow_apparent"));
                roomBalanceRes.setPowActiveTotal(totalData.getDouble("pow_active"));
                roomBalanceRes.setPowReactiveTotal(totalData.getDouble("pow_reactive"));
            }

            JSONObject pathA = roomPower.getJSONObject("path_a");
            if (Objects.nonNull(pathA)) {
                roomBalanceRes.setPowApparentA(pathA.getDouble("pow_apparent"));
                roomBalanceRes.setPowActiveA(pathA.getDouble("pow_active"));
                roomBalanceRes.setPowReactiveA(pathA.getDouble("pow_reactive"));
            }

            JSONObject pathB = roomPower.getJSONObject("path_b");
            if (Objects.nonNull(pathB)) {
                roomBalanceRes.setPowApparentB(pathB.getDouble("pow_apparent"));
                roomBalanceRes.setPowActiveB(pathB.getDouble("pow_active"));
                roomBalanceRes.setPowReactiveB(pathB.getDouble("pow_reactive"));
            }

            if (roomBalanceRes.getPowApparentA() != null && roomBalanceRes.getPowApparentA() != 0 && roomBalanceRes.getPowApparentTotal() != null && roomBalanceRes.getPowApparentTotal() != 0) {
                roomBalanceRes.setRateA((roomBalanceRes.getPowApparentA() / roomBalanceRes.getPowApparentTotal()) * 100);
            }
        }
        return new PageResult<>(result, roomIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<RoomEQRes> getEqPage(RoomIndexPageReqVO pageReqVO) {
        try {
            pageReqVO.setIsDelete(0);
            PageResult<RoomIndexDO> roomIndexDOPageResult = roomIndexCopyMapper.selectPage(pageReqVO);
            List<RoomIndexDO> roomIndexDOList = roomIndexDOPageResult.getList();
            List<RoomEQRes> result = new ArrayList<>();
            List<Integer> ids = roomIndexDOList.stream().map(RoomIndexDO::getId).collect(Collectors.toList());
            if (org.springframework.util.CollectionUtils.isEmpty(ids)) {
                return new PageResult<>(result, roomIndexDOPageResult.getTotal());
            }
            //昨日
            roomIndexDOList.forEach(roomIndexDO -> {
                RoomEQRes res = new RoomEQRes();
                res.setId(roomIndexDO.getId());
                res.setName(roomIndexDO.getRoomName());
                res.setLocation(roomIndexDO.getRoomName());
                result.add(res);
            });
            String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            String endTime = DateUtil.formatDateTime(DateTime.now());
            List<String> yesterdayList = getData(startTime, endTime, ids, "room_eq_total_day");
            Map<Integer, Double> yesterdayMap = new HashMap<>();
            if (!org.springframework.util.CollectionUtils.isEmpty(yesterdayList)) {
                yesterdayList.forEach(str -> {
                    RoomEqTotalDayDo dayDo = JsonUtils.parseObject(str, RoomEqTotalDayDo.class);
                    yesterdayMap.put(dayDo.getRoomId(), dayDo.getEqValue());
                });
            }

            //上周
            startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());
            List<String> weekList = getData(startTime, endTime, ids, "room_eq_total_week");
            Map<Integer, Double> weekMap = new HashMap<>();
            if (!org.springframework.util.CollectionUtils.isEmpty(weekList)) {
                weekList.forEach(str -> {
                    RoomEqTotalWeekDo weekDo = JsonUtils.parseObject(str, RoomEqTotalWeekDo.class);
                    weekMap.put(weekDo.getRoomId(), weekDo.getEqValue());
                });
            }

            //上月
            startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());
            List<String> monthList = getData(startTime, endTime, ids, "room_eq_total_month");
            Map<Integer, Double> monthMap = new HashMap<>();
            if (!org.springframework.util.CollectionUtils.isEmpty(monthList)) {
                monthList.forEach(str -> {
                    RoomEqTotalMonthDo monthDo = JsonUtils.parseObject(str, RoomEqTotalMonthDo.class);
                    monthMap.put(monthDo.getRoomId(), monthDo.getEqValue());
                });
            }

            result.forEach(dto -> {
                dto.setYesterdayEq(yesterdayMap.get(dto.getId()));
                dto.setLastWeekEq(weekMap.get(dto.getId()));
                dto.setLastMonthEq(monthMap.get(dto.getId()));
            });
            return new PageResult<>(result, roomIndexDOPageResult.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    public static final String HOUR_FORMAT = "yyyy-MM-dd";

    public static final String TIME_STR = ":00:00";

    @Override
    public RoomActivePowDTO getActivePow(RoomPowVo vo) {
        RoomActivePowDTO powDTO = new RoomActivePowDTO();
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            String startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));
            String endTime = DateUtil.formatDateTime(TimeUtil.getEndOfDay(calendar.getTime()));
            List<RoomActivePowTrendDTO> yesterdayList = new ArrayList<>();
            List<RoomActivePowTrendDTO> todayList = new ArrayList<>();
            LocalDate old = LocalDate.now().minusDays(1);
            LocalDate now = LocalDate.now();
            for (int i = 0; i < 24; i++) {
                String oldDay = LocalDateTimeUtil.format(LocalDateTime.of(old, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                RoomActivePowTrendDTO dto = new RoomActivePowTrendDTO();
                dto.setDateTime(oldDay);
                dto.setActivePow("0");
                yesterdayList.add(dto);
                RoomActivePowTrendDTO dto1 = new RoomActivePowTrendDTO();
                String nowDay = LocalDateTimeUtil.format(LocalDateTime.of(now, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                dto1.setDateTime(nowDay);
                dto1.setActivePow("");
                todayList.add(dto1);
            }
            //获取昨日数据
            List<String> yesterdayData = getData(startTime, endTime, vo, "room_hda_pow_hour");
            Map<String, RoomActivePowTrendDTO> yesterdayMap = yesterdayList.stream().collect(Collectors.toMap(RoomActivePowTrendDTO::getDateTime, Function.identity()));
            yesterdayData.forEach(str -> {
                RoomPowHourDo hourDo = JsonUtils.parseObject(str, RoomPowHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH:mm");
                RoomActivePowTrendDTO dto = yesterdayMap.get(dateTime);
                if (Objects.nonNull(dto)) {
                    dto.setActivePow(String.valueOf(hourDo.getActiveTotalAvgValue()));
                    yesterdayList.add(dto);
                }
            });
            startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());
            //获取今日数据
            List<String> todayData = getData(startTime, endTime, vo, "room_hda_pow_hour");
            Map<String, RoomActivePowTrendDTO> todayListMap = todayList.stream().collect(Collectors.toMap(RoomActivePowTrendDTO::getDateTime, Function.identity()));
            todayData.forEach(str -> {
                RoomPowHourDo hourDo = JsonUtils.parseObject(str, RoomPowHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH:mm");
                RoomActivePowTrendDTO dto = todayListMap.get(dateTime);
                if (Objects.nonNull(dto)) {
                    dto.setActivePow(String.valueOf(hourDo.getActiveTotalAvgValue()));
                    todayList.add(dto);
                }
            });
            powDTO.setYesterdayList(yesterdayList);
            powDTO.setTodayList(todayList);
            //获取峰值
            RoomActivePowTrendDTO yesterdayMax = yesterdayList.stream().max(Comparator.comparing(RoomActivePowTrendDTO::getActivePow)).orElse(new RoomActivePowTrendDTO());
            RoomActivePowTrendDTO todayMax = todayList.stream().max(Comparator.comparing(RoomActivePowTrendDTO::getActivePow)).orElse(new RoomActivePowTrendDTO());
            powDTO.setTodayMax(Float.valueOf(todayMax.getActivePow()));
            powDTO.setTodayMaxTime(todayMax.getDateTime());
            powDTO.setYesterdayMaxTime(yesterdayMax.getDateTime());
            powDTO.setYesterdayMax(Float.valueOf(yesterdayMax.getActivePow()));
            return powDTO;
        } catch (Exception e) {
            log.error("获取数据失败： ", e);
        }
        return powDTO;
    }

    @Override
    public List<RoomEqTrendDTO> eqTrend(int id, String type) {
        List<RoomEqTrendDTO> list = new ArrayList<>();
        try {
            //当日
            if (type.equals(DAY)) {
                list.addAll(dayTrend(id));
            }

            //当周
            if (type.equals(WEEK)) {
                list.addAll(weekTrend(id));
            }
            //当月
            if (type.equals(MONTH)) {
                list.addAll(monthTrend(id));
            }

        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }

        return list;
    }

    @Override
    public RoomEleChainDTO getEleChain(int id) {
        RoomEleChainDTO chainDTO = new RoomEleChainDTO();
        try {


            getDayChain(id, chainDTO);


            getWeekChain(id, chainDTO);


            getMonthChain(id, chainDTO);


        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return chainDTO;
    }

    @Override
    public List<Integer> idList() {
        return roomIndexCopyMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(RoomIndexDO::getId).collect(Collectors.toList());
    }

    @Override
    public PageResult<RoomEleTotalRealtimeResVO> getRoomEleTotalRealtime(RoomEleTotalRealtimeReqDTO reqDTO, boolean flag) throws IOException {
        PageResult<RoomEleTotalRealtimeResVO> pageResult = new PageResult<>();
        List<RoomEleTotalRealtimeResVO> list = new ArrayList<>();
        List<RoomIndexDO> records = null;
        Long total = 0L;
        LambdaQueryWrapper<RoomIndexDO> queryWrapper = new LambdaQueryWrapper<RoomIndexDO>().eq(RoomIndexDO::getIsDelete, 0)
                .orderByDesc(RoomIndexDO::getCreateTime);
        if (reqDTO.getRoomIds() != null && reqDTO.getRoomIds().length != 0) {
            queryWrapper.in(RoomIndexDO::getId, reqDTO.getRoomIds());
        }
        if (flag) {
            IPage<RoomIndexDO> iPage = roomIndexCopyMapper.selectPage(new Page<>(reqDTO.getPageNo(), reqDTO.getPageSize()), queryWrapper);
            records = iPage.getRecords();
            total = iPage.getTotal();
        } else {
            records = roomIndexCopyMapper.selectList(queryWrapper);
        }
        for (RoomIndexDO record : records) {
            RoomEleTotalRealtimeResVO resVO = new RoomEleTotalRealtimeResVO();
            resVO.setRoomId(record.getId()).setName(record.getRoomName());
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.rangeQuery("create_time.keyword")
                    .gte(reqDTO.getTimeRange()[0])
                    .lte(reqDTO.getTimeRange()[1]));
            boolQuery.must(QueryBuilders.termsQuery("room_id", String.valueOf(record.getId())));
            // 搜索源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQuery);
            searchSourceBuilder.size(1);
            searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
            SearchRequest searchRequest1 = new SearchRequest();
            searchRequest1.indices("room_ele_total_realtime");
            //query条件--正常查询条件
            searchRequest1.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse1.getHits();
            for (SearchHit hit : hits) {
                resVO.setCreateTimeMax((String) hit.getSourceAsMap().get("create_time"));
                if (Objects.nonNull(resVO.getCreateTimeMax())) {
                    resVO.setEleActiveEnd((Double) Optional.ofNullable(hit.getSourceAsMap().get("ele_total")).orElseGet(() -> 0.0));
                }
            }
            SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();
            searchSourceBuilder2.query(boolQuery);
            searchSourceBuilder2.size(1);
            searchSourceBuilder2.sort("create_time.keyword", SortOrder.ASC);
            SearchRequest searchRequest2 = new SearchRequest();
            searchRequest2.indices("room_ele_total_realtime");
            //query条件--正常查询条件
            searchRequest2.source(searchSourceBuilder2);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse2 = client.search(searchRequest2, RequestOptions.DEFAULT);
            SearchHits hits2 = searchResponse2.getHits();

            for (SearchHit hit : hits2) {
                resVO.setCreateTimeMin((String) hit.getSourceAsMap().get("create_time"));
                if (Objects.nonNull(resVO.getCreateTimeMin())) {
                    resVO.setEleActiveStart((Double) Optional.ofNullable(hit.getSourceAsMap().get("ele_total")).orElseGet(() -> 0.0));
                    double sub = BigDemicalUtil.sub(resVO.getEleActiveEnd(), resVO.getEleActiveStart(), 1);
                    resVO.setEleActive(sub);
                    if (sub < 0) {
                        resVO.setEleActive(resVO.getEleActiveEnd());
                    }
                }
            }
            list.add(resVO);
        }
        pageResult.setTotal(total).setList(list);
        return pageResult;
    }

    @Override
    public PageResult<RoomEQRes> getEqPage1(RoomIndexPageReqVO pageReqVO) {
        String indices = "";
        String startTime = null;
        String endTime = null;
        LocalDate now = LocalDate.now();
        //todo bug
        LocalDate yesterday = LocalDate.now();
        switch (pageReqVO.getTimeGranularity()) {
            case "yesterday":
                indices = "room_eq_total_day";
                startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                break;
            case "lastWeek":
                indices = "room_eq_total_week";
                startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                break;
            case "lastMonth":
                indices = "room_eq_total_month";
                startTime = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                break;
            default:
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from((pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize());
        if (pageReqVO.getPageSize() * pageReqVO.getPageNo() > 10000) {
            searchSourceBuilder.size(10000 - (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize());
        } else {
            searchSourceBuilder.size(pageReqVO.getPageSize());
        }
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.sort("eq_value", SortOrder.DESC);
        searchSourceBuilder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))));
        searchSourceBuilder.fetchSource(new String[]{"room_id"}, null);
        SearchRequest sourceRequest = new SearchRequest().indices(indices).source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(sourceRequest, RequestOptions.DEFAULT);
            ArrayList<RoomEqTotalDayDo> list = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits()) {
                list.add(JsonUtils.parseObject(hit.getSourceAsString(), RoomEqTotalDayDo.class));
            }
//            System.out.println(list);
            if (!org.springframework.util.CollectionUtils.isEmpty(list)) {
                List<Integer> ids = list.stream().map(RoomEqTotalDayDo::getRoomId).collect(Collectors.toList());
                List<RoomIndexDO> roomIndexDOS = roomIndexCopyMapper.selectList(new LambdaUpdateWrapper<RoomIndexDO>().in(RoomIndexDO::getId, ids));
                Map<Integer, RoomIndexDO> collect = roomIndexDOS.stream().collect(Collectors.toMap(RoomIndexDO::getId, x -> x));
                startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                List<String> yesterdayList = getData(startTime, endTime, ids, "room_eq_total_day");
                Map<Integer, Double> yesterdayMap = new HashMap<>();
                if (!org.springframework.util.CollectionUtils.isEmpty(yesterdayList)) {
                    yesterdayList.forEach(str -> {
                        RoomEqTotalDayDo dayDo = JsonUtils.parseObject(str, RoomEqTotalDayDo.class);
                        yesterdayMap.put(dayDo.getRoomId(), dayDo.getEqValue());
                    });
                }

                //上周
                startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                List<String> weekList = getData(startTime, endTime, ids, "room_eq_total_week");
                Map<Integer, Double> weekMap = new HashMap<>();
                if (!org.springframework.util.CollectionUtils.isEmpty(weekList)) {
                    weekList.forEach(str -> {
                        RoomEqTotalWeekDo weekDo = JsonUtils.parseObject(str, RoomEqTotalWeekDo.class);
                        weekMap.put(weekDo.getRoomId(), weekDo.getEqValue());
                    });
                }

                //上月
                startTime = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                List<String> monthList = getData(startTime, endTime, ids, "aisle_eq_total_month");
                Map<Integer, Double> monthMap = new HashMap<>();
                if (!org.springframework.util.CollectionUtils.isEmpty(monthList)) {
                    monthList.forEach(str -> {
                        RoomEqTotalMonthDo monthDo = JsonUtils.parseObject(str, RoomEqTotalMonthDo.class);
                        monthMap.put(monthDo.getRoomId(), monthDo.getEqValue());
                    });
                }

                List<RoomEQRes> res = new ArrayList<>();
                List<Integer> roomIds = roomIndexDOS.stream().map(RoomIndexDO::getId).distinct().collect(Collectors.toList());
                Map<Integer, String> voMap = getPositionByIds(roomIds);
                list.forEach(item -> {
                    RoomEQRes roomEQRes = new RoomEQRes();
                    RoomIndexDO roomIndexDO = collect.get(item.getRoomId());
                    if (Objects.nonNull(roomIndexDO)) {
                        roomEQRes.setId(roomIndexDO.getId());
                        roomEQRes.setName(roomIndexDO.getRoomName());
                    }
                    if (Objects.nonNull(yesterdayMap.get(roomEQRes.getId()))) {
                        roomEQRes.setYesterdayEq(yesterdayMap.get(roomEQRes.getId()));
                    }
                    if (Objects.nonNull(weekMap.get(roomEQRes.getId()))) {
                        roomEQRes.setLastWeekEq(weekMap.get(roomEQRes.getId()));
                    }
                    if (Objects.nonNull(monthMap.get(roomEQRes.getId()))) {
                        roomEQRes.setLastMonthEq(monthMap.get(roomEQRes.getId()));
                    }
                    String roomName = voMap.get(roomEQRes.getId());
                    if (StringUtils.isNotEmpty(roomName)) {
                        roomEQRes.setName(roomName);
                        roomEQRes.setLocation(roomEQRes.getName());
                    } else {
                        roomEQRes.setLocation(roomEQRes.getName());
                    }
                    res.add(roomEQRes);
                });
                return new PageResult<>(res, searchResponse.getHits().getTotalHits().value);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<RoomMaxEqResVO> getMaxEq() {
        List<RoomMaxEqResVO> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now();

        // 昨天的起始时间（00:00:00）
        String start =LocalDateTimeUtil.format( yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        String end = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");

        extractedMaxEq("room_eq_total_day", start, end, result, 0);
        // 获取上周的开始时间（周一）
        start = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        end = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");

        extractedMaxEq("room_eq_total_week", start, end, result, 1);

        start = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
        end = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
        extractedMaxEq("room_eq_total_month", start,
                end, result, 2);
        return result;
    }

    private void extractedMaxEq(String indexEs, String startTime, String endTime, List<RoomMaxEqResVO> result, Integer type) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(indexEs);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                    .gte(startTime).lte(endTime))
                    .must(QueryBuilders.rangeQuery("eq_value").gt(0))));
            builder.sort("eq_value", SortOrder.DESC);
            // 设置搜索条件
            searchRequest.source(builder);
            builder.size(1);
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (hits.length > 0) {
                // 获取最大值和时间字段
                Map<String, Object> sourceAsMap = hits[0].getSourceAsMap();
                RoomMaxEqResVO vo = new RoomMaxEqResVO();
                vo.setMaxEq((Double) sourceAsMap.get("eq_value"));
                vo.setId((Integer) sourceAsMap.get("room_id"));
                RoomIndex roomIndex = roomIndexMapper.selectById(vo.getId());
                vo.setRoomName(roomIndex.getRoomName());
                vo.setType(type);//借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
                result.add(vo);
            }
        } catch (Exception e) {
            log.error("插接箱用能最大查询异常：" + e);
        }
    }

    private Map<Integer, String> getPositionByIds(List<Integer> roomIds) {
        Map<Integer, String> roomMap = new HashMap<>();
        if (cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.isAnyEmpty(roomIds)) {
            return roomMap;
        }
        roomMap = roomIndexMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getRoomName));
        return roomMap;
    }


    /**
     * 日趋势
     *
     * @param id
     * @return
     */
    private List<RoomEqTrendDTO> dayTrend(int id) throws IOException {
        List<RoomEqTrendDTO> trendDTOList = new ArrayList<>();

        //今日
        String startTime = "";
        String endTime = DateUtil.formatDateTime(DateTime.now());

        //昨日
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));

        List<String> list = getData(startTime, endTime, id, ROOM_ELE_TOTAL_REALTIME);

        List<RoomEleTotalRealtimeDo> yesterdayList = new ArrayList<>();
        List<RoomEleTotalRealtimeDo> todayList = new ArrayList<>();
        String finalStartTime = startTime;
        list.forEach(str -> {
            RoomEleTotalRealtimeDo realtimeDo = JsonUtils.parseObject(str, RoomEleTotalRealtimeDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            //昨天
            if (finalStartTime.contains(dateTime)) {
                yesterdayList.add(realtimeDo);
            }
            //今日
            if (endTime.contains(dateTime)) {
                todayList.add(realtimeDo);
            }

        });
        Map<String, RoomEqTrendDTO> map = new HashMap<>();
        //昨日数据处理
        for (int i = 0; i < yesterdayList.size() - 1; i++) {

            //前一个时间点
            RoomEleTotalRealtimeDo reDo = yesterdayList.get(i);
            //当前时间点
            RoomEleTotalRealtimeDo thisDo = yesterdayList.get(i + 1);

            String dateTime = thisDo.getCreateTime().toString("HH") + ":00";
            log.info("reDo : " + reDo.getEleTotal() + "thisDo : " + thisDo.getEleTotal());
            //避免负数
            double eq = (thisDo.getEleTotal() - reDo.getEleTotal()) < 0 ? 0 : thisDo.getEleTotal() - reDo.getEleTotal();

            RoomEqTrendDTO roomEqTrendDTO = new RoomEqTrendDTO();
            roomEqTrendDTO.setLastEq(eq);
            roomEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, roomEqTrendDTO);

        }
        //今日数据处理
        for (int i = 0; i < todayList.size() - 1; i++) {

            //前一个时间点
            RoomEleTotalRealtimeDo reDo = todayList.get(i);
            //当前时间点
            RoomEleTotalRealtimeDo thisDo = todayList.get(i + 1);

            String dateTime = thisDo.getCreateTime().toString("HH") + ":00";
            log.info("reDo : " + reDo.getEleTotal() + "thisDo : " + thisDo.getEleTotal());

            //避免负数
            double eq = (thisDo.getEleTotal() - reDo.getEleTotal()) < 0 ? 0 : thisDo.getEleTotal() - reDo.getEleTotal();

            RoomEqTrendDTO roomEqTrendDTO = map.get(dateTime);
            if (Objects.isNull(roomEqTrendDTO)) {
                roomEqTrendDTO = new RoomEqTrendDTO();
            }
            roomEqTrendDTO.setEq(eq);
            roomEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, roomEqTrendDTO);
        }

        map.keySet().forEach(key -> trendDTOList.add(map.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(RoomEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }

    /**
     * 周趋势
     *
     * @param id
     * @return
     */
    private List<RoomEqTrendDTO> weekTrend(int id) throws IOException {
        List<RoomEqTrendDTO> trendDTOList = new ArrayList<>();

        //本周
        String startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));

        Map<Integer, String> thisWeek = getWeek(startTime);


        //上周
        LocalDate today = LocalDate.now();
        //上周第一天
        DayOfWeek todayOfWeek = today.getDayOfWeek();
        int sub = todayOfWeek.getValue() + 6;
        LocalDate lastWeekFirst = today.minusDays(sub);
        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(Date.from(lastWeekFirst.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        String endTime = DateUtil.formatDateTime(DateTime.now());

        List<String> list = getData(startTime, endTime, id, ROOM_EQ_TOTAL_DAY);

        Map<String, RoomEqTotalDayDo> weekMap = new HashMap<>();

        list.forEach(str -> {
            RoomEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, RoomEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            weekMap.put(dateTime, realtimeDo);

        });
        Map<Integer, RoomEqTrendDTO> data = new HashMap<>();
        //本周数据
        thisWeek.keySet().forEach(key -> {

            String date = thisWeek.get(key);

            RoomEqTotalDayDo realtimeDo = weekMap.get(date);

            RoomEqTrendDTO trendDTO = new RoomEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<Integer, String> lastWeek = getWeek(startTime);
        //上周数据
        lastWeek.keySet().forEach(key -> {

            String date = lastWeek.get(key);

            RoomEqTotalDayDo realtimeDo = weekMap.get(date);

            RoomEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new RoomEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(RoomEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }


    /**
     * 月趋势
     *
     * @param id
     * @return
     */
    private List<RoomEqTrendDTO> monthTrend(int id) throws IOException {
        List<RoomEqTrendDTO> trendDTOList = new ArrayList<>();

        //本月
        String startTime = "";


        //上月第一天
        Calendar lastMonthFirstDateCal = Calendar.getInstance();
        lastMonthFirstDateCal.add(Calendar.MONTH, -1);
        lastMonthFirstDateCal.set(Calendar.DAY_OF_MONTH, 1);

        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(lastMonthFirstDateCal.getTime()));

        String endTime = DateUtil.formatDateTime(DateTime.now());

        List<String> list = getData(startTime, endTime, id, ROOM_EQ_TOTAL_DAY);

        Map<String, RoomEqTotalDayDo> monthMap = new HashMap<>();

        list.forEach(str -> {
            RoomEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, RoomEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            monthMap.put(dateTime, realtimeDo);

        });
        Map<String, RoomEqTrendDTO> data = new HashMap<>();

        Map<String, String> thisMonth = getThisMonth();
        //本月数据
        thisMonth.keySet().forEach(key -> {

            String date = thisMonth.get(key);

            RoomEqTotalDayDo realtimeDo = monthMap.get(date);

            RoomEqTrendDTO trendDTO = new RoomEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<String, String> lastMonth = getLastMonth();
        //上月数据
        lastMonth.keySet().forEach(key -> {

            String date = lastMonth.get(key);

            RoomEqTotalDayDo realtimeDo = monthMap.get(date);

            RoomEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new RoomEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(RoomEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }

    /**
     * 获取周
     *
     * @return
     */
    private Map<Integer, String> getWeek(String startTime) {
        Map<Integer, String> week = new HashMap<>();
        Date start = DateUtil.parse(startTime, HOUR_FORMAT);
        Calendar calendar = Calendar.getInstance();
        for (int i = 1; i <= 7; i++) {
            calendar.setTime(start);
            calendar.add(Calendar.DAY_OF_MONTH, i);
            String date = DateUtil.format(calendar.getTime(), HOUR_FORMAT);
            week.put(i, date);

        }
        return week;
    }

    /**
     * 获取当前月
     *
     * @return
     */
    private Map<String, String> getThisMonth() {
        Map<String, String> month = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat(HOUR_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < max; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            String day = sdf.format(calendar.getTime());
            String date = DateUtil.format(calendar.getTime(), DAY_FORMAT);
            month.put(date, day);
        }
        return month;
    }

    /**
     * 获取上月
     *
     * @return
     */
    private Map<String, String> getLastMonth() {
        Map<String, String> month = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat(HOUR_FORMAT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 0; i < max; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i + 1);
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            String day = sdf.format(calendar.getTime());
            String date = DateUtil.format(calendar.getTime(), DAY_FORMAT);
            month.put(date, day);
        }
        return month;
    }

    /**
     * 获取日环比
     *
     * @param id
     * @param chainDTO
     * @return
     */
    private void getDayChain(int id, RoomEleChainDTO chainDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //日环比
        //今日
        startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double todayEq = getDayEq(startTime, endTime, id);
        chainDTO.setTodayEq(todayEq);
        //昨日
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        List<String> list = getData(startTime, endTime, id, ROOM_EQ_TOTAL_DAY);
        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            RoomEqTotalDayDo dayDo = JsonUtils.parseObject(str, RoomEqTotalDayDo.class);
            String dateTime = dayDo.getCreateTime().toString(HOUR_FORMAT);
            eqMap.put(dateTime, dayDo.getEqValue());

        });
        //前日
        String lastTime = DateUtil.formatDate(DateUtil.parse(startTime, HOUR_FORMAT));
        double eveEq = eqMap.containsKey(lastTime) ? eqMap.get(lastTime) : 0;

        //昨天
        String thisTime = DateUtil.formatDate(DateUtil.beginOfDay(DateTime.now()));
        double lastEq = eqMap.containsKey(thisTime) ? eqMap.get(thisTime) : 0;

        chainDTO.setYesterdayEq(lastEq);

        NumberFormat percentInstance = NumberFormat.getPercentInstance();
        // 设置保留几位小数，这里设置的是保留两位小数
        percentInstance.setMinimumFractionDigits(2);
        //环比
        String dayRate = eveEq == 0 ? percentInstance.format(0) : percentInstance.format(lastEq / eveEq);
        chainDTO.setDayRate(dayRate);

    }

    /**
     * 获取周环比
     *
     * @param id
     * @param chainDTO
     */
    private void getWeekChain(int id, RoomEleChainDTO chainDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //周环比
        //本周
        startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double thisWeekEq = getDayEq(startTime, endTime, id);
        chainDTO.setThisWeekEq(thisWeekEq);
        //上周
        LocalDate today = LocalDate.now();
        //上周第一天
        DayOfWeek todayOfWeek = today.getDayOfWeek();
        int sub = todayOfWeek.getValue() + 6;
        LocalDate lastWeekFirst = today.minusDays(sub);


        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(Date.from(lastWeekFirst.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        List<String> list = getData(startTime, endTime, id, ROOM_EQ_TOTAL_WEEK);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            RoomEqTotalWeekDo weekDo = JsonUtils.parseObject(str, RoomEqTotalWeekDo.class);
            String dateTime = weekDo.getCreateTime().toString(HOUR_FORMAT);
            eqMap.put(dateTime, weekDo.getEqValue());

        });
        //上上周
        String lastTime = DateUtil.formatDate(DateUtil.parse(startTime, HOUR_FORMAT));
        double eveEq = eqMap.containsKey(lastTime) ? eqMap.get(lastTime) : 0;

        //上周
        String thisTime = DateUtil.formatDate(DateUtil.beginOfWeek(DateTime.now()));
        double lastEq = eqMap.containsKey(thisTime) ? eqMap.get(thisTime) : 0;

        chainDTO.setLastWeekEq(lastEq);

        NumberFormat percentInstance = NumberFormat.getPercentInstance();
        // 设置保留几位小数，这里设置的是保留两位小数
        percentInstance.setMinimumFractionDigits(2);
        //环比
        String weekRate = eveEq == 0 ? percentInstance.format(0) : percentInstance.format(lastEq / eveEq);
        chainDTO.setWeekRate(weekRate);
    }

    /**
     * 获取月环比
     *
     * @param id
     * @param chainDTO
     */
    private void getMonthChain(int id, RoomEleChainDTO chainDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //月环比
        //本月
        startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double thisMonthEq = getDayEq(startTime, endTime, id);
        chainDTO.setThisMonthEq(thisMonthEq);
        //上月
        //上月第一天
        Calendar lastMonthFirstDateCal = Calendar.getInstance();
        lastMonthFirstDateCal.add(Calendar.MONTH, -1);
        lastMonthFirstDateCal.set(Calendar.DAY_OF_MONTH, 1);

        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(lastMonthFirstDateCal.getTime()));


        List<String> list = getData(startTime, endTime, id, ROOM_EQ_TOTAL_MONTH);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            RoomEqTotalMonthDo monthDo = JsonUtils.parseObject(str, RoomEqTotalMonthDo.class);
            String dateTime = monthDo.getCreateTime().toString(HOUR_FORMAT);
            eqMap.put(dateTime, monthDo.getEqValue());

        });
        //上上月
        String lastMonthTime = DateUtil.formatDate(DateUtil.parse(startTime, HOUR_FORMAT));
        double eveEq = eqMap.containsKey(lastMonthTime) ? eqMap.get(lastMonthTime) : 0;
        //上月
        String thisMonthTime = DateUtil.formatDate(DateUtil.beginOfMonth(DateTime.now()));
        double lastMonthEq = eqMap.containsKey(thisMonthTime) ? eqMap.get(thisMonthTime) : 0;
        chainDTO.setLastMonthEq(lastMonthEq);

        NumberFormat percentInstance = NumberFormat.getPercentInstance();
        // 设置保留几位小数，这里设置的是保留两位小数
        percentInstance.setMinimumFractionDigits(2);
        //环比
        String monthRate = eveEq == 0 ? percentInstance.format(0) : percentInstance.format(lastMonthEq / eveEq);
        chainDTO.setMonthRate(monthRate);
    }

    /**
     * 获取日用电量
     *
     * @param startTime
     * @param endTime
     * @param id
     * @return
     */
    private double getDayEq(String startTime, String endTime, int id) {
        log.info("startTime : " + startTime + "endTime：" + endTime);
        //获取时间段内第一条和最后一条数据
        RoomEleTotalRealtimeDo endRealtimeDo = getEleData(startTime, endTime,
                SortOrder.DESC,
                ROOM_ELE_TOTAL_REALTIME, id);
        RoomEleTotalRealtimeDo startRealtimeDo = getEleData(startTime, endTime,
                SortOrder.ASC,
                ROOM_ELE_TOTAL_REALTIME, id);
        //结束时间电量
        double endEle = endRealtimeDo.getEleTotal();

        //开始时间电量
        double startEle = startRealtimeDo.getEleTotal();

        //判断使用电量  开始电量大于结束电量，记为0
        double eq;
        if (startEle > endEle) {
            eq = 0;
        } else {
            eq = endEle - startEle;
        }
        return eq;
    }

    /**
     * 获取数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ids       机柜id列表
     * @param index     索引表
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

    /**
     * 获取es数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param powVo     请求参数
     * @param index     索引表
     * @return
     * @throws IOException
     */
    private List<String> getData(String startTime, String endTime, RoomPowVo powVo, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termQuery("room_id", powVo.getId())));
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

    /**
     * 获取es数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param index     索引表
     * @return
     * @throws IOException
     */
    private List<String> getData(String startTime, String endTime, int id, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termQuery(ROOM_ID, id))));
        builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
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

    /**
     * @param startTime
     * @param endTime
     * @param sortOrder
     * @param index
     * @param id
     * @return
     */
    private RoomEleTotalRealtimeDo getEleData(String startTime, String endTime, SortOrder sortOrder, String index, int id) {
        RoomEleTotalRealtimeDo realtimeDo = new RoomEleTotalRealtimeDo();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD)
                            .gte(startTime)
                            .lt(endTime))
                    .must(QueryBuilders.termQuery(ROOM_ID, id))));

            // 嵌套聚合
            // 设置聚合查询
            String top = "top";
            AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                    .size(1).sort(CREATE_TIME + KEYWORD, sortOrder);

            builder.aggregation(topAgg);

            // 设置搜索条件
            searchRequest.source(builder);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();

            TopHits tophits = aggregations.get(top);
            SearchHits sophistsHits = tophits.getHits();
            if (null != sophistsHits.getHits() && sophistsHits.getHits().length > 0) {
                SearchHit hit = sophistsHits.getHits()[0];
                realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), RoomEleTotalRealtimeDo.class);
            }
            return realtimeDo;
        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return realtimeDo;
    }

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index, String order) throws IOException {
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

    private String getMinData(String startTime, String endTime, List<Integer> ids, String index, String order) throws IOException {
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

    private List getMutiRedis(List<RoomIndexDO> list) {
        List<String> devKeys = list.stream().map(roomIndexDO -> REDIS_KEY_ROOM + roomIndexDO.getId()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    private String localDateTimeToString(LocalDateTime time) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
    }

}