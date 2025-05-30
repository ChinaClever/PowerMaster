package cn.iocoder.yudao.module.aisle.service.aisleindex;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.pow.AisleHdaLineHour;
import cn.iocoder.yudao.framework.common.entity.es.aisle.pow.AislePowHourDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DataTypeEnums;
import cn.iocoder.yudao.framework.common.exception.BusinessAssert;
import cn.iocoder.yudao.framework.common.mapper.AisleBarMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.TimeUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo.*;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
import cn.iocoder.yudao.module.aisle.dal.mysql.aisleindex.AisleIndexCopyMapper;
import cn.iocoder.yudao.module.aisle.dto.AislePowerLoadDetailReqDTO;
import cn.iocoder.yudao.module.aisle.utils.DataProcessingUtils;
import cn.iocoder.yudao.module.cabinet.service.index.IndexServiceImpl;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.function.Pow;
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
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.AISLE_ID;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.AISLE_ELE_TOTAL_REALTIME;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.AISLE_EQ_TOTAL_DAY;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.AISLE_EQ_TOTAL_MONTH;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.AISLE_EQ_TOTAL_WEEK;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.KEYWORD;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.REDIS_KEY_BUS;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.SPLIT_KEY;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.*;
import static cn.iocoder.yudao.module.aisle.enums.ErrorCodeConstants.INDEX_NOT_EXISTS;

/**
 * 通道列 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
@Slf4j
public class AisleIndexServiceImpl implements AisleIndexService {

    @Autowired
    private AisleIndexCopyMapper aisleIndexCopyMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private AisleBarMapper aisleBarMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;

    public static final String DAY_FORMAT = "dd";

    public static final String FACTOR_FORMAT = "功率因素";

    @Override
    public Integer createIndex(AisleIndexSaveReqVO createReqVO) {
        // 插入
        AisleIndexDO index = BeanUtils.toBean(createReqVO, AisleIndexDO.class);
        aisleIndexCopyMapper.insert(index);
        // 返回
        return index.getId();
    }

    @Override
    public void updateIndex(AisleIndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        AisleIndexDO updateObj = BeanUtils.toBean(updateReqVO, AisleIndexDO.class);
        aisleIndexCopyMapper.updateById(updateObj);
    }

    @Override
    public Boolean deleteIndex(Integer id) {
        // 校验存在
        validateIndexExists(id);
        AisleIndexDO indexDO = new AisleIndexDO();
        indexDO.setId(id);
        indexDO.setIsDelete(1);
        // 删除
        return aisleIndexCopyMapper.updateById(indexDO) > 0;
//        aisleIndexCopyMapper.deleteById(id);
    }

    private void validateIndexExists(Integer id) {
        if (aisleIndexCopyMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public AisleIndexDO getIndex(Integer id) {
        return aisleIndexCopyMapper.selectById(id);
    }

    @Override
    public PageResult<AisleIndexRes> getIndexPage(AisleIndexPageReqVO pageReqVO) {
        if (pageReqVO.getAisleIds() != null && pageReqVO.getAisleIds().get(0) == -1) {
            return new PageResult<>(new ArrayList<>(), 0L);
        }
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
        List<AisleIndexDO> list = aisleIndexDOPageResult.getList();
        List<Integer> aisleIds = list.stream().map(AisleIndexDO::getId).collect(Collectors.toList());
        List<AisleBar> aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapperX<AisleBar>().inIfPresent(AisleBar::getAisleId, aisleIds));
        List<String> devKey = aisleBars.stream().map(AisleBar::getBusKey).collect(Collectors.toList());
        Map<String, Map<Integer, String>> aisleBarMap = aisleBars.stream()
                .collect(Collectors.groupingBy(
                        AisleBar::getBusKey,
                        Collectors.toMap(AisleBar::getAisleId, AisleBar::getPath)
                ));

        List<AisleIndexRes> res = new ArrayList<>();
        List redisList = getMutiBusRedis(devKey);
        for (AisleIndexDO aisleIndexDO : list) {
            AisleIndexRes aisleIndexRes = new AisleIndexRes();
            aisleIndexRes.setId(aisleIndexDO.getId());
            aisleIndexRes.setName(aisleIndexDO.getAisleName());
            aisleIndexRes.setRoomId(aisleIndexDO.getRoomId());
            res.add(aisleIndexRes);
        }
        Map<Integer, AisleIndexRes> resMap = res.stream().collect(Collectors.toMap(AisleIndexRes::getId, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String busDevKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id");
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray loadRate = lineItemList.getJSONArray("load_rate");
            List<Double> rateList = loadRate.toList(Double.class);
            for (Integer i : aisleBarMap.get(busDevKey).keySet()) {
                AisleIndexRes aisleIndexRes = resMap.get(i);
                String path = aisleBarMap.get(busDevKey).get(i);
                LoadRate rate = new LoadRate();
                if (rateList.size() > 1) {
                    rate.setALoadRate(loadRate.getDouble(0));
                    rate.setBLoadRate(loadRate.getDouble(1));
                    rate.setCLoadRate(loadRate.getDouble(2));
                } else {
                    rate.setALoadRate(loadRate.getDouble(0));
                }
                rateList.sort(Collections.reverseOrder());
                Double biggest = rateList.get(0) * 100;
                Integer color = 0;
                if (biggest == 0) {
                    color = 0;
                } else if (biggest < 30) {
                    color = 1;
                } else if (biggest < 60) {
                    color = 2;
                } else if (biggest < 90) {
                    color = 3;
                } else if (biggest >= 90) {
                    color = 4;
                }
                if (path.equals("A")) {
                    if (rateList.size() > 1) {
                        aisleIndexRes.setALoadRateA(loadRate.getDouble(0));
                        aisleIndexRes.setBLoadRateA(loadRate.getDouble(1));
                        aisleIndexRes.setCLoadRateA(loadRate.getDouble(2));
                    } else {
                        aisleIndexRes.setALoadRateA(loadRate.getDouble(0));
                    }
                    aisleIndexRes.setColorA(color);
                    aisleIndexRes.setDevKeyA(busDevKey);
                } else {
                    if (rateList.size() > 1) {
                        aisleIndexRes.setALoadRateB(loadRate.getDouble(0));
                        aisleIndexRes.setBLoadRateB(loadRate.getDouble(1));
                        aisleIndexRes.setCLoadRateB(loadRate.getDouble(2));
                    } else {
                        aisleIndexRes.setALoadRateB(loadRate.getDouble(0));
                    }
                    aisleIndexRes.setColorB(color);
                    aisleIndexRes.setDevKeyB(busDevKey);
                }
            }
        }

        return new PageResult<>(res, aisleIndexDOPageResult.getTotal());
    }

    @Override
    public List<Integer> getDevKeyList() {
        List<Integer> result = aisleIndexCopyMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(AisleIndexDO::getId).collect(Collectors.toList());
        return result;
    }

    @Override
    public PageResult<AislePowerRes> getPowerPage(AisleIndexPageReqVO pageReqVO) {
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
        if (Objects.equals(aisleIndexDOPageResult.getTotal(), 0L)) {
            return null;
        }
        List<AisleIndexDO> list = aisleIndexDOPageResult.getList();
        List<AislePowerRes> result = new ArrayList<>();

        List<Integer> aisleIds = list.stream().map(AisleIndexDO::getId).collect(Collectors.toList());
        List<CabinetIndex> cabinetIndexList = cabinetIndexMapper.selectList(new LambdaQueryWrapper<CabinetIndex>().in(CabinetIndex::getAisleId, aisleIds)
                .eq(CabinetIndex::getIsDeleted, false).eq(CabinetIndex::getIsDisabled, false));
        Map<Integer, List<CabinetIndex>> cabinetMap = cabinetIndexList.stream().collect(Collectors.groupingBy(CabinetIndex::getAisleId));


        Map<Integer, Map<String, String>> devKey = getDevKey(aisleIds);
        List<Integer> roomIds = list.stream().map(AisleIndexDO::getRoomId).collect(Collectors.toList());
        Map<Integer, String> voMap = getPositionByIds(roomIds);

        List redisList = getMutiRedis(list);
        Map<Integer, Object> redisMap = (Map<Integer, Object>) redisList.stream().filter(i -> Objects.nonNull(i)).collect(Collectors.toMap(i -> JSON.parseObject(JSON.toJSONString(i)).getInteger("aisle_key"), Function.identity()));

        for (AisleIndexDO aisleIndexDO : list) {
            AislePowerRes res = BeanUtils.toBean(aisleIndexDO, AislePowerRes.class);
            result.add(res);

            Map<String, String> aisleBarMap = devKey.get(aisleIndexDO.getId());
            if (Objects.nonNull(aisleBarMap)) {
                res.setDevKeyA(aisleBarMap.get("A"));
                res.setDevKeyB(aisleBarMap.get("B"));
            }
            res.setId(aisleIndexDO.getId());
            res.setFlagType(true);
            if (!CollectionUtils.isAnyEmpty(cabinetMap.get(aisleIndexDO.getId()))) {
                res.setFlagType(false);
            }
            res.setName(aisleIndexDO.getAisleName());
            res.setRoomId(aisleIndexDO.getRoomId());
            res.setPduBar(aisleIndexDO.getPduBar());
            String roomName = voMap.get(aisleIndexDO.getRoomId());
            if (StringUtils.isNotEmpty(roomName)) {
                res.setRoomName(roomName);
                res.setLocation(roomName + SPLIT_KEY + aisleIndexDO.getAisleName());
            } else {
                res.setLocation(aisleIndexDO.getAisleName());
            }

            Object obj = redisMap.get(aisleIndexDO.getId());
            if (Objects.isNull(obj)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));

            res.setStatus(jsonObject.getInteger("status"));
            JSONObject totalData = jsonObject.getJSONObject("aisle_power").getJSONObject("total_data");
            JSONObject pathA = jsonObject.getJSONObject("aisle_power").getJSONObject("path_a");
            JSONObject pathB = jsonObject.getJSONObject("aisle_power").getJSONObject("path_b");
            if (totalData != null) {
                res.setEleActiveTotal(totalData.getDouble("ele_active"));
                res.setPowApparentTotal(totalData.getDouble("pow_apparent"));
                res.setPowActiveTotal(totalData.getDouble("pow_active"));
                res.setPowReactiveTotal(totalData.getDouble("pow_reactive"));
                res.setPowerFactor(totalData.getDouble("power_factor"));
            }
            if (pathA != null) {
                res.setCurAList(pathA.getList("cur_value", Double.class));
                res.setVolAList(pathA.getList("vol_value", Double.class));
                res.setPowValueAList(pathA.getList("pow_value", Double.class));
                res.setEleActiveA(pathA.getDouble("ele_active"));
                res.setPowApparentA(pathA.getDouble("pow_apparent"));
                res.setPowActiveA(pathA.getDouble("pow_active"));
                res.setPowReactiveA(pathA.getDouble("pow_reactive"));
                res.setPowerFactorA(pathA.getDouble("power_factor"));
            }
            if (pathB != null) {
                res.setCurBList(pathB.getList("cur_value", Double.class));
                res.setVolBList(pathB.getList("vol_value", Double.class));
                res.setPowValueBList(pathB.getList("pow_value", Double.class));
                res.setEleActiveB(pathB.getDouble("ele_active"));
                res.setPowApparentB(pathB.getDouble("pow_apparent"));
                res.setPowActiveB(pathB.getDouble("pow_active"));
                res.setPowReactiveB(pathB.getDouble("pow_reactive"));
                res.setPowerFactorB(pathB.getDouble("power_factor"));
            }
        }
        return new PageResult<>(result, aisleIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<AisleEQRes> getEqPage(AisleIndexPageReqVO pageReqVO) {
        pageReqVO.setIsDelete(0);
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
        List<AisleIndexDO> aisleIndexDOList = aisleIndexDOPageResult.getList();
        List<AisleEQRes> result = new ArrayList<>();
        List<Integer> ids = aisleIndexDOList.stream().map(AisleIndexDO::getId).collect(Collectors.toList());
        if (org.springframework.util.CollectionUtils.isEmpty(ids)) {
            return new PageResult<>(result, aisleIndexDOPageResult.getTotal());
        }
        //昨日
        aisleIndexDOList.forEach(aisleIndexDO -> {
            AisleEQRes res = new AisleEQRes();
            res.setId(aisleIndexDO.getId());
            res.setName(aisleIndexDO.getAisleName());
            res.setRoomId(aisleIndexDO.getRoomId());
            result.add(res);
        });

        LocalDate now = LocalDate.now();
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now();

        // 昨天的起始时间（00:00:00）
        String startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        String endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");

        List<String> yesterdayList = getData(startTime, endTime, ids, "aisle_eq_total_day");
        Map<Integer, Double> yesterdayMap = new HashMap<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(yesterdayList)) {
            yesterdayList.forEach(str -> {
                AisleEqTotalDayDo dayDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
                yesterdayMap.put(dayDo.getAisleId(), dayDo.getEqValue());
            });
        }

        //上周
        startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        endTime = LocalDateTimeUtil.format(now.plusWeeks(1).plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        List<String> weekList = getData(startTime, endTime, ids, "aisle_eq_total_week");
        Map<Integer, Double> weekMap = new HashMap<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(weekList)) {
            weekList.forEach(str -> {
                AisleEqTotalWeekDo weekDo = JsonUtils.parseObject(str, AisleEqTotalWeekDo.class);
                weekMap.put(weekDo.getAisleId(), weekDo.getEqValue());
            });
        }

        //上月
        startTime = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
        endTime = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
//        endTime = LocalDateTimeUtil.format(now.minusMonths(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
        List<String> monthList = getData(startTime, endTime, ids, "aisle_eq_total_month");
        Map<Integer, Double> monthMap = new HashMap<>();
        if (!org.springframework.util.CollectionUtils.isEmpty(monthList)) {
            monthList.forEach(str -> {
                AisleEqTotalMonthDo monthDo = JsonUtils.parseObject(str, AisleEqTotalMonthDo.class);
                monthMap.put(monthDo.getAisleId(), monthDo.getEqValue());
            });
        }
        List<Integer> roomIds = result.stream().map(AisleIndexRespVO::getRoomId).collect(Collectors.toList());
        Map<Integer, String> voMap = getPositionByIds(roomIds);
        result.forEach(dto -> {
            if (Objects.nonNull(yesterdayMap.get(dto.getId()))) {
                dto.setYesterdayEq(yesterdayMap.get(dto.getId()));
            }
            if (Objects.nonNull(weekMap.get(dto.getId()))) {
                dto.setLastWeekEq(weekMap.get(dto.getId()));
            }
            if (Objects.nonNull(monthMap.get(dto.getId()))) {
                dto.setLastMonthEq(monthMap.get(dto.getId()));
            }
            String roomName = voMap.get(dto.getRoomId());
            if (StringUtils.isNotEmpty(roomName)) {
                dto.setRoomName(roomName);
                dto.setLocation(roomName + SPLIT_KEY + dto.getName());
            } else {
                dto.setLocation(dto.getName());
            }
        });
        if (StringUtils.isNotEmpty(pageReqVO.getTimeGranularity())) {
            if (pageReqVO.getTimeGranularity().equals("yesterday")) {
                result.sort(Comparator.comparing(AisleEQRes::getYesterdayEq).reversed());
            } else if (pageReqVO.getTimeGranularity().equals("lastWeek")) {
                result.sort(Comparator.comparing(AisleEQRes::getLastWeekEq).reversed());
            } else if (pageReqVO.getTimeGranularity().equals("lastMonth")) {
                result.sort(Comparator.comparing(AisleEQRes::getLastMonthEq).reversed());
            }
        }
        return new PageResult<>(result, aisleIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<AisleEQRes> getEqPage1(AisleIndexPageReqVO pageReqVO) {
        String indices = null;
        LocalDate now = LocalDate.now();
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now();

        String startTime = null;
        String endTime = null;
        Integer total = 0;
        switch (pageReqVO.getTimeGranularity()) {
            case "yesterday":
                indices = "aisle_eq_total_day";
                startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                break;
            case "lastWeek":
                indices = "aisle_eq_total_week";
                startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                break;
            case "lastMonth":
                indices = "aisle_eq_total_month";
                startTime = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                break;
            default:
        }
        try {
            // 搜索源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            int pageNo = pageReqVO.getPageNo();
            int pageSize = pageReqVO.getPageSize();
            int index = (pageNo - 1) * pageSize;
            searchSourceBuilder.from(index);
            // 最后一页请求超过一万，pageSize设置成请求刚好一万条
            if (index + pageSize > 10000) {
                searchSourceBuilder.size(10000 - index);
            } else {
                searchSourceBuilder.size(pageSize);
            }
            searchSourceBuilder.trackTotalHits(true);
            searchSourceBuilder.sort("eq_value", SortOrder.DESC);
            //获取需要处理的数据
            searchSourceBuilder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
            ));
            searchSourceBuilder.fetchSource(new String[]{"aisle_id"}, null);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            searchRequest.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<AisleEqTotalDayDo> list = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits()) {
                AisleEqTotalDayDo dayDo = JsonUtils.parseObject(hit.getSourceAsString(), AisleEqTotalDayDo.class);
                list.add(dayDo);
            }

            if (!org.springframework.util.CollectionUtils.isEmpty(list)) {
                List<Integer> ids = list.stream().map(AisleEqTotalDayDo::getAisleId).collect(Collectors.toList());
                List<AisleIndexDO> boxIndices = aisleIndexCopyMapper.selectList(new LambdaUpdateWrapper<AisleIndexDO>().in(AisleIndexDO::getId, ids));
                Map<Integer, AisleIndexDO> boxIndexMap = boxIndices.stream().collect(Collectors.toMap(AisleIndexDO::getId, x -> x));

                startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                List<String> yesterdayList = getData(startTime, endTime, ids, "aisle_eq_total_day");
                Map<Integer, Double> yesterdayMap = new HashMap<>();
                if (!org.springframework.util.CollectionUtils.isEmpty(yesterdayList)) {
                    yesterdayList.forEach(str -> {
                        AisleEqTotalDayDo dayDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
                        yesterdayMap.put(dayDo.getAisleId(), dayDo.getEqValue());
                    });
                }

                //上周
                startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                List<String> weekList = getData(startTime, endTime, ids, "aisle_eq_total_week");
                Map<Integer, Double> weekMap = new HashMap<>();
                if (!org.springframework.util.CollectionUtils.isEmpty(weekList)) {
                    weekList.forEach(str -> {
                        AisleEqTotalWeekDo weekDo = JsonUtils.parseObject(str, AisleEqTotalWeekDo.class);
                        weekMap.put(weekDo.getAisleId(), weekDo.getEqValue());
                    });
                }

                //上月
                startTime = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                List<String> monthList = getData(startTime, endTime, ids, "aisle_eq_total_month");
                Map<Integer, Double> monthMap = new HashMap<>();
                if (!org.springframework.util.CollectionUtils.isEmpty(monthList)) {
                    monthList.forEach(str -> {
                        AisleEqTotalMonthDo monthDo = JsonUtils.parseObject(str, AisleEqTotalMonthDo.class);
                        monthMap.put(monthDo.getAisleId(), monthDo.getEqValue());
                    });
                }
                List<AisleEQRes> result = new ArrayList<>();
                List<Integer> roomIds = boxIndices.stream().map(AisleIndexDO::getRoomId).distinct().collect(Collectors.toList());
                Map<Integer, String> voMap = getPositionByIds(roomIds);
                list.forEach(iter -> {
                    AisleEQRes dto = new AisleEQRes();
                    AisleIndexDO aisleIndexDO = boxIndexMap.get(iter.getAisleId());
                    if (Objects.nonNull(aisleIndexDO)) {
                        dto.setName(aisleIndexDO.getAisleName());
                        dto.setId(aisleIndexDO.getId());
                        dto.setRoomId(aisleIndexDO.getRoomId());
                    }

                    if (Objects.nonNull(yesterdayMap.get(dto.getId()))) {
                        dto.setYesterdayEq(yesterdayMap.get(dto.getId()));
                    }
                    if (Objects.nonNull(weekMap.get(dto.getId()))) {
                        dto.setLastWeekEq(weekMap.get(dto.getId()));
                    }
                    if (Objects.nonNull(monthMap.get(dto.getId()))) {
                        dto.setLastMonthEq(monthMap.get(dto.getId()));
                    }
                    String roomName = voMap.get(dto.getRoomId());
                    if (StringUtils.isNotEmpty(roomName)) {
                        dto.setRoomName(roomName);
                        dto.setLocation(roomName + SPLIT_KEY + dto.getName());
                    } else {
                        dto.setLocation(dto.getName());
                    }
                    result.add(dto);
                });
//                getPosition(result);
                return new PageResult<>(result, searchResponse.getHits().getTotalHits().value);
            }
            return null;
        } catch (Exception e) {
            log.error("获取数据失败：", e);
            return null;
        }
    }

    @Override
    public AisleBalanceChartResVO getAisleBalanceChart(Integer id) {
        Object o = redisTemplate.opsForValue().get(REDIS_KEY_AISLE + id);
        AisleBalanceChartResVO vo = new AisleBalanceChartResVO();
        if (Objects.nonNull(o)) {
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            JSONObject aislePower = jsonObject.getJSONObject("aisle_power");

            Double powApparentA = 0.0;
            Double powApparentB = 0.0;
            Double powApparentTotal = 0.0;

            Double powActiveA = 0.0;
            Double powActiveB = 0.0;
            Double powActiveTotal = 0.0;
            JSONObject totalData = aislePower.getJSONObject("total_data");
            if (Objects.nonNull(totalData)) {
                powApparentTotal = totalData.getDouble("pow_apparent");
                powActiveTotal = totalData.getDouble("pow_active");
            }

            JSONObject pathA = aislePower.getJSONObject("path_a");
            if (Objects.nonNull(pathA)) {
                powApparentA = pathA.getDouble("pow_apparent");
                powActiveA = pathA.getDouble("pow_active");
                vo.setVolLista(pathA.getList("vol_value", Double.class));
                List<Double> curList = pathA.getList("cur_value", Double.class);
                if (!CollectionUtils.isAnyEmpty(curList)) {
                    vo.setCurLista(curList);
                    Double curAvg = curList.stream().mapToDouble(i -> i).average().getAsDouble();
                    Double curUnbalance = curAvg == 0 ? 0 : (Collections.max(curList) - curAvg) / curAvg * 100;
                    vo.setCurUnbalancea(BigDecimal.valueOf(curUnbalance).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }

            JSONObject pathB = aislePower.getJSONObject("path_b");
            if (Objects.nonNull(pathB)) {
                powApparentB = pathB.getDouble("pow_apparent");
                powActiveB = pathB.getDouble("pow_active");
                vo.setVolListb(pathB.getList("vol_value", Double.class));
                List<Double> curList = pathB.getList("cur_value", Double.class);
                if (!CollectionUtils.isAnyEmpty(curList)) {
                    vo.setCurListb(curList);
                    Double curAvg = curList.stream().mapToDouble(i -> i).average().getAsDouble();
                    Double curUnbalance = curAvg == 0 ? 0 : (Collections.max(curList) - curAvg) / curAvg * 100;
                    vo.setCurUnbalanceb(BigDecimal.valueOf(curUnbalance).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }

            vo.setRateA(BigDemicalUtil.safeDivideNum(3, powApparentA, powApparentTotal).multiply(new BigDecimal(100)).doubleValue());
            vo.setPowActiveARate(BigDemicalUtil.safeDivideNum(3, powActiveA, powActiveTotal).multiply(new BigDecimal(100)).doubleValue());

            vo.setRateB(BigDemicalUtil.safeDivideNum(3, powApparentB, powApparentTotal).multiply(new BigDecimal(100)).doubleValue());
            vo.setPowActiveBRate(BigDemicalUtil.safeDivideNum(3, powActiveB, powActiveTotal).multiply(new BigDecimal(100)).doubleValue());

            List<AisleHdaLineHour> aisleHdaLineHour = getData(LocalDateTimeUtil.format(LocalDateTime.now().minusDays(1), "yyyy-MM-dd HH:mm:ss"),
                    LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"),
                    id, "aisle_hda_line_hour", AisleHdaLineHour.class);
            if (!CollectionUtils.isAnyEmpty(aisleHdaLineHour)) {
                List<AisleBalanceChartVolCurVO> bean = BeanUtils.toBean(aisleHdaLineHour, AisleBalanceChartVolCurVO.class);
                vo.setList(bean);
            }
        }
        return vo;
    }

    @Override
    public Map getLineChartDetailData(AislePowerLoadDetailReqDTO reqVO) {
        if (reqVO.getId() == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        String index = null;
        String[] heads = new String[]{};
        String startTime = null;
        String endTime = null;
        switch (reqVO.getGranularity()) {
            case "realtime":
                LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                if (Objects.equals(reqVO.getFlag(), 2)) {
                    heads = new String[]{"aisle_id", "apparent_total", "apparent_a", "apparent_b",
                            "active_total", "active_a", "active_b", "reactive_total", "reactive_a", "reactive_b", "factor_total", "factor_a", "factor_b", "create_time"};
                    index = "aisle_hda_pow_realtime";
                }
                if (Objects.equals(reqVO.getFlag(), 1)) {
                    index = "aisle_ele_total_realtime";
                    heads = new String[]{"aisle_id", "ele_total", "create_time"};
                }
                if (Objects.equals(reqVO.getFlag(), 0)) {
                    index = "aisle_hda_line_realtime";
                    heads = new String[]{"aisle_id", "line_id", "vol_a", "vol_b", "cur_a", "cur_b", "pow_a", "pow_b", "create_time"};
                }
                startTime = oneHourAgo.format(formatter);
                endTime = now.format(formatter);
                break;
            case "hour":
                LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
                if (Objects.equals(reqVO.getFlag(), 2)) {
                    index = "aisle_hda_pow_hour";
                    heads = new String[]{"apparent_total_avg_value", "apparent_a_avg_value", "apparent_b_avg_value", "active_total_avg_value", "active_a_avg_value", "active_b_avg_value",
                            "reactive_total_avg_value", "reactive_a_avg_value", "reactive_b_avg_value", "factor_total_avg_value", "factor_a_avg_value", "factor_b_avg_value",
                            "create_time", "aisle_id"};
                }
                if (Objects.equals(reqVO.getFlag(), 1)) {
                    index = "aisle_ele_total_realtime";
                    heads = new String[]{"aisle_id", "ele_total", "create_time"};
                }
                if (Objects.equals(reqVO.getFlag(), 0)) {
                    index = "aisle_hda_line_hour";
                    heads = new String[]{"aisle_id", "line_id", "vol_a_avg_value", "vol_b_avg_value", "cur_a_avg_value", "cur_b_avg_value", "create_time"};
                }
                startTime = oneDayAgo.format(formatter);
                endTime = now.format(formatter);
                break;
            case "SeventyHours":
                LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
                if (Objects.equals(reqVO.getFlag(), 2)) {
                    index = "aisle_hda_pow_hour";
                    heads = new String[]{"apparent_total_avg_value", "apparent_a_avg_value", "apparent_b_avg_value", "active_total_avg_value", "active_a_avg_value", "active_b_avg_value",
                            "reactive_total_avg_value", "reactive_a_avg_value", "reactive_b_avg_value", "factor_total_avg_value", "factor_a_avg_value", "factor_b_avg_value",
                            "create_time", "aisle_id"};
                }
                if (Objects.equals(reqVO.getFlag(), 1)) {
                    index = "aisle_ele_total_realtime";
                    heads = new String[]{"aisle_id", "ele_total", "create_time"};
                }
                if (Objects.equals(reqVO.getFlag(), 0)) {
                    index = "aisle_hda_line_hour";
                    heads = new String[]{"aisle_id", "line_id", "vol_a_avg_value", "vol_b_avg_value", "cur_a_avg_value", "cur_b_avg_value", "create_time"};
                }
                startTime = threeDaysAgo.format(formatter);
                endTime = now.format(formatter);
                break;
            default:
                LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
                if (Objects.equals(reqVO.getFlag(), 2)) {
                    index = "aisle_hda_pow_day";
                    heads = new String[]{"apparent_total_avg_value", "apparent_a_avg_value", "apparent_b_avg_value", "active_total_avg_value", "active_a_avg_value", "active_b_avg_value",
                            "reactive_total_avg_value", "reactive_a_avg_value", "reactive_b_avg_value", "factor_total_avg_value", "factor_a_avg_value", "factor_b_avg_value",
                            "create_time", "aisle_id"};
                }
                if (Objects.equals(reqVO.getFlag(), 1)) {
                    index = "aisle_eq_total_day";
                    heads = new String[]{"aisle_id", "eq_value", "create_time"};
                }
                if (Objects.equals(reqVO.getFlag(), 0)) {
                    index = "aisle_hda_line_day";
                    heads = new String[]{"aisle_id", "line_id", "vol_a_avg_value", "vol_b_avg_value", "cur_a_avg_value", "cur_b_avg_value", "create_time"};
                }
                startTime = oneMonthAgo.format(formatter);
                endTime = now.format(formatter);
                break;
        }
        List<String> data = getData(startTime, endTime, reqVO.getId(), index, heads);

        List<Map> list = new ArrayList<>();

        List<Object> resultLine1 = new ArrayList<>();
        List<Object> resultLine2 = new ArrayList<>();
        List<Object> resultLine3 = new ArrayList<>();

        String finalIndex = index;
        data.forEach(str -> {
            Map map = JsonUtils.parseObject(str, Map.class);
            if (Objects.equals(reqVO.getFlag(), 2)) {
                list.add(map);
            }
            if (Objects.equals(reqVO.getFlag(), 1)) {
                if (Objects.equals("aisle_ele_total_realtime", finalIndex)) {
                    Double newEle = (Double) map.get("ele_total");
                    int sub = data.indexOf(str);
                    if (sub > 0) {
                        String oldStr = data.get(sub - 1);
                        Map oldMap = JsonUtils.parseObject(oldStr, Map.class);
                        Double oldEle = (Double) oldMap.get("ele_total");
                        double ele = BigDemicalUtil.sub(newEle, oldEle, 1);
                        HashMap eleMap = new HashMap();
                        eleMap.put("ele", ele);
                        eleMap.put("aisleId", map.get("aisle_id"));
                        eleMap.put("create_time", map.get("create_time"));
                        list.add(eleMap);
                    }
                } else {
                    Double newEle = (Double) map.get("eq_value");
                    HashMap eleMap = new HashMap();
                    eleMap.put("ele", newEle);
                    eleMap.put("aisleId", map.get("aisle_id"));
                    eleMap.put("create_time", map.get("create_time"));
                    list.add(eleMap);

                }

            }
            if (Objects.equals(reqVO.getFlag(), 0)) {
                Integer lindId = (Integer) map.get("line_id");
                switch (lindId) {
                    case 0:
                        resultLine1.add(map);
                        break;
                    case 1:
                        resultLine2.add(map);
                        break;
                    case 2:
                        resultLine3.add(map);
                        break;
                    default:
                }
            }
        });
        Map map = new HashMap();

        if (Objects.equals(reqVO.getFlag(), 0)) {
            map.put("L1", resultLine1);
            map.put("L2", resultLine2);
            map.put("L3", resultLine3);
        } else {
            map.put("data", list);
        }
        return map;
    }

    @Override
    public List<AisleMaxEqResVO> getMaxEq() {
        List<AisleMaxEqResVO> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now();

        // 昨天的起始时间（00:00:00）
        String start = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        String end = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");

        extractedMaxEq("aisle_eq_total_day", start, end, result, 0);
        // 获取上周的开始时间（周一）
        start = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        end = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");

        extractedMaxEq("aisle_eq_total_week", start, end, result, 1);

        start = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
        end = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
        extractedMaxEq("aisle_eq_total_month", start,
                end, result, 2);
        return result;
    }

    @Override
    public Map findAisleFactor() throws IOException {
        HashMap map = new HashMap();

        SearchRequest searchRequest = new SearchRequest("aisle_hda_pow_realtime");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.sort("factor_total", SortOrder.DESC);
        builder.size(1);
        searchRequest.source(builder);
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        AislePowerFactorMaxResVO resVO = JsonUtils.parseObject(hits.getAt(0).getSourceAsString(), AislePowerFactorMaxResVO.class);
        if (Objects.nonNull(resVO)) {
            AisleIndexDO aisleIndexDO = aisleIndexCopyMapper.selectById(resVO.getAisleId());
            if (Objects.nonNull(aisleIndexDO)) {
                RoomIndex roomIndex = roomIndexMapper.selectById(aisleIndexDO.getRoomId());
                resVO.setAisleName(aisleIndexDO.getAisleName());
                resVO.setRoomName(roomIndex.getRoomName());
            }
        }
        map.put("max", resVO);


        SearchRequest searchRequest1 = new SearchRequest("aisle_hda_pow_realtime");
        SearchSourceBuilder builder1 = new SearchSourceBuilder();
        builder1.query(QueryBuilders.rangeQuery("factor_total").gt(0));
        builder1.sort("factor_total", SortOrder.ASC);
        builder1.size(1);
        searchRequest1.source(builder1);
        // 执行ES请求
        SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
        SearchHits hits1 = searchResponse1.getHits();
        String sourceAsString = hits1.getAt(0).getSourceAsString();
        AislePowerFactorMaxResVO resVO1 = JsonUtils.parseObject(sourceAsString, AislePowerFactorMaxResVO.class);
        if (Objects.nonNull(resVO1)) {
            AisleIndexDO aisleIndexDO = aisleIndexCopyMapper.selectById(resVO1.getAisleId());
            if (Objects.nonNull(aisleIndexDO)) {
                RoomIndex roomIndex = roomIndexMapper.selectById(aisleIndexDO.getRoomId());
                resVO1.setAisleName(aisleIndexDO.getAisleName());
                resVO1.setRoomName(roomIndex.getRoomName());
            }
        }
        map.put("min", resVO1);

        return map;
    }

    @Override
    public Map<String, AislePowerFactorMaxResVO> getAisleLineMax(AisleIndexPageReqVO pageReqVO) throws IOException {
        Map<String, AislePowerFactorMaxResVO> map = new HashMap<>();
        String index;
        if (pageReqVO.getTimeType() == 0) {
            index = "aisle_hda_pow_hour";
        } else {
            index = "aisle_hda_pow_day";
        }
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.fetchSource(new String[]{"aisle_id", "apparent_a_max_value", "apparent_a_max_time"}, null);
        builder.size(1);
        builder.sort("apparent_a_max_value", SortOrder.DESC);
        searchRequest.source(builder);
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        Map<String, Object> sourceAsMap = hits.getAt(0).getSourceAsMap();
        if (Objects.nonNull(sourceAsMap)) {
            Integer aisleId = (Integer) sourceAsMap.get("aisle_id");
            String time = (String) sourceAsMap.get("apparent_a_max_time");
            Double apparentMax = (Double) sourceAsMap.get("apparent_a_max_value");
            AislePowerFactorMaxResVO resVO = new AislePowerFactorMaxResVO();
            resVO.setApparentMax(apparentMax);
            resVO.setCreateTime(time);
            resVO.setAisleId(aisleId);
            if (Objects.nonNull(resVO)) {
                AisleIndexDO aisleIndexDO = aisleIndexCopyMapper.selectById(aisleId);
                resVO.setAisleName(aisleIndexDO.getAisleName());
                if (Objects.nonNull(aisleIndexDO)) {
                    RoomIndex roomIndex = roomIndexMapper.selectById(aisleIndexDO.getRoomId());
                    resVO.setRoomId(roomIndex.getId());
                    resVO.setRoomName(roomIndex.getRoomName());
                }
            }
            map.put("A", resVO);
        }

        SearchRequest searchRequest1 = new SearchRequest(index);
        SearchSourceBuilder builder1 = new SearchSourceBuilder();
        builder1.fetchSource(new String[]{"aisle_id", "apparent_b_max_value", "apparent_b_max_time"}, null);
        builder1.size(1);
        builder1.sort("apparent_b_max_value", SortOrder.DESC);
        searchRequest.source(builder1);
        // 执行ES请求
        SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
        Map<String, Object> sourceAsMap1 = searchResponse1.getHits().getAt(0).getSourceAsMap();

        if (Objects.nonNull(sourceAsMap1)) {
            Integer aisleId = (Integer) sourceAsMap1.get("aisle_id");
            String time = (String) sourceAsMap.get("apparent_a_max_time");
            Double apparentMax = (Double) sourceAsMap.get("apparent_a_max_value");
            AislePowerFactorMaxResVO resVO = new AislePowerFactorMaxResVO();
            resVO.setApparentMax(apparentMax);
            resVO.setCreateTime(time);
            resVO.setAisleId(aisleId);
            if (Objects.nonNull(resVO)) {
                AisleIndexDO aisleIndexDO = aisleIndexCopyMapper.selectById(aisleId);
                resVO.setAisleName(aisleIndexDO.getAisleName());
                if (Objects.nonNull(aisleIndexDO)) {
                    RoomIndex roomIndex = roomIndexMapper.selectById(aisleIndexDO.getRoomId());
                    resVO.setRoomId(roomIndex.getId());
                    resVO.setRoomName(roomIndex.getRoomName());
                }
            }
            map.put("B", resVO);
        }
        return map;
    }

    @Override
    public PageResult<AisleIndexDelResVO> getDelPage(AisleIndexPageReqVO pageReqVO) {
        Page page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
//        LambdaQueryWrapper<AisleIndexDO> queryWrapper = new LambdaQueryWrapper<AisleIndexDO>().eq(AisleIndexDO::getIsDelete,1)
//                .likeLeft(StringUtils.isNotEmpty(pageReqVO.getName()),AisleIndexDO::getAisleName,pageReqVO.getName());
        Page<AisleIndexDelResVO> page1 = aisleIndexCopyMapper.selectDelPageQuery(page, pageReqVO);
        if (page1.getTotal() > 0) {
            return new PageResult<>(page1.getRecords(), page1.getTotal());
        }
        return null;
    }

    @Override
    public Boolean restore(Integer id) {
        AisleIndexDO aisleIndexDO = aisleIndexCopyMapper.selectById(id);

        Integer count = aisleIndexCopyMapper.selectxOrY(aisleIndexDO);
        if (count > 0) {
            BusinessAssert.error(20014, "此柜列所在位置已存在柜列");
        }
        AisleIndexDO indexDO = new AisleIndexDO();
        indexDO.setId(id);
        indexDO.setIsDelete(0);
        return aisleIndexCopyMapper.updateById(indexDO) > 0;
    }

    private void extractedMaxEq(String indexEs, String startTime, String endTime, List<AisleMaxEqResVO> result, Integer type) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(indexEs);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                    .gte(startTime).lte(endTime))));
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
                AisleMaxEqResVO aisleMaxEqResVO = new AisleMaxEqResVO();
                aisleMaxEqResVO.setMaxEq((Double) sourceAsMap.get("eq_value"));
                aisleMaxEqResVO.setId((Integer) sourceAsMap.get("aisle_id"));
                AisleIndexDO aisleIndexDO = aisleIndexCopyMapper.selectById(aisleMaxEqResVO.getId());
                RoomIndex roomIndex = roomIndexMapper.selectById(aisleIndexDO.getRoomId());
                aisleMaxEqResVO.setRoomId(aisleIndexDO.getRoomId());
                aisleMaxEqResVO.setAisleName(aisleIndexDO.getAisleName());
                aisleMaxEqResVO.setRoomName(roomIndex.getRoomName());
                aisleMaxEqResVO.setType(type);//借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
                result.add(aisleMaxEqResVO);
            }
        } catch (Exception e) {
            log.error("插接箱用能最大查询异常：" + e);
        }
    }

    @Override
    public PageResult<AislePfRes> getAislePFPage(AisleIndexPageReqVO pageReqVO) {
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
        List<AisleIndexDO> list = aisleIndexDOPageResult.getList();
        List<AislePfRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (AisleIndexDO aisleIndexDO : list) {
            AislePfRes aislePfRes = new AislePfRes();
            aislePfRes.setId(aisleIndexDO.getId());
            aislePfRes.setName(aisleIndexDO.getAisleName());
            aislePfRes.setRoomId(aisleIndexDO.getRoomId());
            res.add(aislePfRes);
        }
        Map<Integer, AislePfRes> resMap = res.stream().collect(Collectors.toMap(AislePfRes::getId, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            Integer aisleKey = jsonObject.getInteger("aisle_key");
            AislePfRes aislePfRes = resMap.get(aisleKey);
            aislePfRes.setStatus(jsonObject.getInteger("status"));
            JSONObject totalData = jsonObject.getJSONObject("aisle_power").getJSONObject("total_data");
            JSONObject pathA = jsonObject.getJSONObject("aisle_power").getJSONObject("path_a");
            JSONObject pathB = jsonObject.getJSONObject("aisle_power").getJSONObject("path_b");
            if (pathA != null && !pathA.isEmpty()) {
                aislePfRes.setPfA(pathA.getDouble("power_factor"));
            }
            if (pathB != null && !pathB.isEmpty()) {
                aislePfRes.setPfB(pathB.getDouble("power_factor"));
            }
            if (totalData != null && !totalData.isEmpty()) {
                aislePfRes.setPfTotal(totalData.getDouble("power_factor"));
            }

        }

        return new PageResult<>(res, aisleIndexDOPageResult.getTotal());
    }

    @Override
    public Map getAislePFDetail(AisleIndexPageReqVO pageReqVO) {
        try {
            pageReqVO.setNewTime(pageReqVO.getOldTime().withHour(23).withMinute(59).withSecond(59));
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(pageReqVO.getId());
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<String> aisleHdaPowHour = getData(startTime, endTime, ids, "aisle_hda_pow_hour");
            List<AislePowHourDo> strList = aisleHdaPowHour.stream()
                    .map(str -> JsonUtils.parseObject(str, AislePowHourDo.class))
                    .collect(Collectors.toList());

            HashMap<String, Object> resultMap = new HashMap<>();

            List<AislePFTableRes> tableList = new ArrayList<>();
            AislePFDetailRes result = new AislePFDetailRes();
            result.setTime(new ArrayList<>());
            result.setFactorTotalAvgValue(new ArrayList<>());
            result.setFactorAAvgValue(new ArrayList<>());
            result.setFactorBAvgValue(new ArrayList<>());

            resultMap.put("chart", result);
            resultMap.put("table", tableList);

            if (strList == null || strList.size() == 0) {
                return resultMap;
            }
            strList.forEach(hourdo -> {
                AislePFTableRes pfTableRes = new AislePFTableRes();
                float factorTotalAvgValue = hourdo.getFactorTotalAvgValue();
                float factorAAvgValue = hourdo.getFactorAAvgValue();
                float factorBAvgValue = hourdo.getFactorBAvgValue();
                String pfTime = hourdo.getCreateTime().toString("HH:mm");
                pfTableRes.setFactorTotalAvgValue(factorTotalAvgValue);
                pfTableRes.setFactorAAvgValue(factorAAvgValue);
                pfTableRes.setFactorBAvgValue(factorBAvgValue);
                pfTableRes.setTime(pfTime);
                tableList.add(pfTableRes);

                result.getFactorTotalAvgValue().add(factorTotalAvgValue);
                result.getFactorAAvgValue().add(factorAAvgValue);
                result.getFactorBAvgValue().add(factorBAvgValue);
                result.getTime().add(pfTime);
            });


            return resultMap;
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return null;
    }

    @Override
    public List<Integer> idList() {
        return aisleIndexCopyMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(AisleIndexDO::getId).collect(Collectors.toList());
    }


    @Override
    public PageResult<AisleLineMaxRes> getAisleLineMaxPage(AisleIndexPageReqVO pageReqVO) {
        try {
            List<AisleIndexDO> searchList = aisleIndexCopyMapper.selectList(new LambdaQueryWrapperX<AisleIndexDO>()
                    .inIfPresent(AisleIndexDO::getId, pageReqVO.getAisleIds())
                    .likeIfPresent(AisleIndexDO::getAisleName, pageReqVO.getName()));
            if (org.springframework.util.CollectionUtils.isEmpty(searchList)) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            }


            String index = null;
            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "aisle_hda_pow_hour";
            } else {
                index = "aisle_hda_pow_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());

            Map esTotalAndIds = getESTotalAndIds(index, startTime, endTime, pageReqVO.getPageSize(), pageReqVO.getPageNo() - 1, searchList.stream().map(AisleIndexDO::getId).collect(Collectors.toList()));
            Long total = (Long) esTotalAndIds.get("total");
            if (total == 0) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            List<Integer> ids = (List<Integer>) esTotalAndIds.get("ids");


            //视在功率
            Map<Integer, MaxValueAndCreateTime> apparentAMap = getAisleLinePowMaxData(startTime, endTime, ids, index, "apparent_a_max_value", "apparent_a_max_time");
            Map<Integer, MaxValueAndCreateTime> apparentBMap = getAisleLinePowMaxData(startTime, endTime, ids, index, "apparent_b_max_value", "apparent_b_max_time");
            Map<Integer, MaxValueAndCreateTime> apparentTotalMap = getAisleLinePowMaxData(startTime, endTime, ids, index, "apparent_total_max_value", "apparent_total_max_time");
            //有功功率
            Map<Integer, MaxValueAndCreateTime> powAMap = getAisleLinePowMaxData(startTime, endTime, ids, index, "active_a_max_value", "active_a_max_time");
            Map<Integer, MaxValueAndCreateTime> powBMap = getAisleLinePowMaxData(startTime, endTime, ids, index, "active_b_max_value", "active_b_max_time");
            Map<Integer, MaxValueAndCreateTime> powTotalMap = getAisleLinePowMaxData(startTime, endTime, ids, index, "active_total_max_value", "active_total_max_time");
            List<AisleLineMaxRes> result = new ArrayList<>();
            List<AisleIndexDO> aisleIndexDOList = aisleIndexCopyMapper.selectList(new LambdaQueryWrapperX<AisleIndexDO>()
                    .inIfPresent(AisleIndexDO::getId, pageReqVO.getAisleIds())
                    .inIfPresent(AisleIndexDO::getId, ids));
            for (AisleIndexDO aisleIndexDO : aisleIndexDOList) {
                Integer id = aisleIndexDO.getId().intValue();

                AisleLineMaxRes aisleLineMaxRes = new AisleLineMaxRes();

                aisleLineMaxRes.setId(aisleIndexDO.getId());
                aisleLineMaxRes.setName(aisleIndexDO.getAisleName());
                aisleLineMaxRes.setRoomId(aisleIndexDO.getRoomId());

                MaxValueAndCreateTime powA = powAMap.get(id);
                if (powA != null) {
                    aisleLineMaxRes.setMaxPowA(powA.getMaxValue().floatValue());
                    aisleLineMaxRes.setMaxPowATime(powA.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                MaxValueAndCreateTime powB = powBMap.get(id);
                if (powB != null) {
                    aisleLineMaxRes.setMaxPowB(powB.getMaxValue().floatValue());
                    aisleLineMaxRes.setMaxPowBTime(powB.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                MaxValueAndCreateTime powTotal = powTotalMap.get(id);
                if (powTotal != null) {
                    aisleLineMaxRes.setMaxPowTotal(powTotal.getMaxValue().floatValue());
                    aisleLineMaxRes.setMaxPowTotalTime(powTotal.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                MaxValueAndCreateTime apparentA = apparentAMap.get(id);
                if (apparentA != null) {
                    aisleLineMaxRes.setMaxApparentA(apparentA.getMaxValue().floatValue());
                    aisleLineMaxRes.setMaxApparentATime(apparentA.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                MaxValueAndCreateTime apparentB = apparentBMap.get(id);
                if (apparentB != null) {
                    aisleLineMaxRes.setMaxApparentB(apparentB.getMaxValue().floatValue());
                    aisleLineMaxRes.setMaxApparentBTime(apparentB.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                MaxValueAndCreateTime apparentTotal = apparentTotalMap.get(id);
                if (apparentTotal != null) {
                    aisleLineMaxRes.setMaxApparentTotal(apparentTotal.getMaxValue().floatValue());
                    aisleLineMaxRes.setMaxApparentTotalTime(apparentTotal.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                result.add(aisleLineMaxRes);
            }
            if (!org.springframework.util.CollectionUtils.isEmpty(result)) {
                getPosition(result);
            }

            return new PageResult<AisleLineMaxRes>(result, total);
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }


        return new PageResult<>(new ArrayList<>(), 0L);
    }

    public static final String HOUR_FORMAT = "yyyy-MM-dd";

    public static final String TIME_STR = ":00:00";

    @Override
    public AisleActivePowDTO getActivePow(AislePowVo vo) {
        AisleActivePowDTO powDTO = new AisleActivePowDTO();
        try {

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            String startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));
            String endTime = DateUtil.formatDateTime(TimeUtil.getEndOfDay(calendar.getTime()));

            LocalDate old = LocalDate.now().minusDays(1);
            LocalDate now = LocalDate.now();
            List<AisleActivePowTrendDTO> yesterdayList = new ArrayList<>();
            List<AisleActivePowTrendDTO> todayList = new ArrayList<>();

            for (int i = 0; i < 24; i++) {
                String oldDay = LocalDateTimeUtil.format(LocalDateTime.of(old, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                AisleActivePowTrendDTO dto = new AisleActivePowTrendDTO();
                dto.setDateTime(oldDay);
                dto.setActivePow("0");
                yesterdayList.add(dto);
                AisleActivePowTrendDTO dto1 = new AisleActivePowTrendDTO();
                String nowDay = LocalDateTimeUtil.format(LocalDateTime.of(now, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                dto1.setDateTime(nowDay);
                dto1.setActivePow("");
                todayList.add(dto1);
            }

            Map<String, AisleActivePowTrendDTO> yesterdayMap = yesterdayList.stream().collect(Collectors.toMap(AisleActivePowTrendDTO::getDateTime, Function.identity()));
            //获取昨日数据
            List<String> yesterdayData = getData(startTime, endTime, vo, "aisle_hda_pow_hour");
            yesterdayData.forEach(str -> {
                AislePowHourDo hourDo = JsonUtils.parseObject(str, AislePowHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH:mm");
                AisleActivePowTrendDTO powTrendDTO = yesterdayMap.get(dateTime);
                powTrendDTO.setActivePow(String.valueOf(BigDemicalUtil.setScale(hourDo.getActiveTotalAvgValue(), 3)));
            });


            startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());

            //获取今日数据
            List<String> todayData = getData(startTime, endTime, vo, "aisle_hda_pow_hour");
            Map<String, AisleActivePowTrendDTO> todayMap = todayList.stream().collect(Collectors.toMap(AisleActivePowTrendDTO::getDateTime, Function.identity()));
            todayData.forEach(str -> {
                AislePowHourDo hourDo = JsonUtils.parseObject(str, AislePowHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH:mm");
                AisleActivePowTrendDTO powTrendDTO = todayMap.get(dateTime);
                powTrendDTO.setActivePow(String.valueOf(BigDemicalUtil.setScale(hourDo.getActiveTotalAvgValue(), 3)));
                powTrendDTO.setDateTime(dateTime);
            });

            powDTO.setYesterdayList(yesterdayList);
            powDTO.setTodayList(todayList);
            //获取峰值
            AisleActivePowTrendDTO yesterdayMax = yesterdayList.stream().max(Comparator.comparing(AisleActivePowTrendDTO::getActivePow)).orElse(new AisleActivePowTrendDTO());
            AisleActivePowTrendDTO todayMax = todayList.stream().max(Comparator.comparing(AisleActivePowTrendDTO::getActivePow)).orElse(new AisleActivePowTrendDTO());
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
    public List<AisleEqTrendDTO> eqTrend(int id, String type) {
        List<AisleEqTrendDTO> list = new ArrayList<>();
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
    public AisleEleChainDTO getEleChain(int id) {
        AisleEleChainDTO chainDTO = new AisleEleChainDTO();
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
    public AisleLineResBase getAisleLineCurLine(AisleIndexPageReqVO pageReqVO) {
        AisleLineResBase result = new AisleLineResBase();
        try {
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            String index = null;
            if (pageReqVO.getTimeType() == 0) {
                index = AISLE_HDA_POW_HOUR;
                startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
                endTime = localDateTimeToString(LocalDateTime.now());
            } else {
                index = AISLE_HDA_POW_DAY;
            }
            List<Integer> ids = Arrays.asList(pageReqVO.getId());
            String[] heads = new String[]{"create_time", "aisle_id", "active_total_max_value", "active_total_max_time",
                    "active_a_max_value", "active_a_max_time", "active_b_max_value", "active_b_max_time", "apparent_a_max_value", "apparent_a_max_time",
                    "apparent_b_max_value", "apparent_b_max_time", "apparent_total_max_value", "apparent_total_max_time",
                    "reactive_total_max_value", "reactive_total_max_time"};
            List<String> data = getData(startTime, endTime, ids, index, heads);

            result.getSeries().add(new RequirementLineSeries().setName("总最大有功功率"));
            result.getSeries().add(new RequirementLineSeries().setName("总最大视在功率"));
            result.getSeries().add(new RequirementLineSeries().setName("总最大无功功率"));
            result.getSeries().add(new RequirementLineSeries().setName("A路最大有功功率"));
            result.getSeries().add(new RequirementLineSeries().setName("B路最大有功功率"));

            result.getSeries().add(new RequirementLineSeries().setName("A路最大视在功率"));
            result.getSeries().add(new RequirementLineSeries().setName("B路最大视在功率"));

            List<AislePowHourDo> aislePowHourList = data.stream().map(str -> JsonUtils.parseObject(str, AislePowHourDo.class)).collect(Collectors.toList());

            aislePowHourList.forEach(hour -> {
                result.getTime().add(hour.getCreateTime().toString("yyyy-MM-dd HH"));

                result.getSeries().get(0).getData().add(hour.getActiveTotalMaxValue());
                ((RequirementLineSeries) result.getSeries().get(0)).getMaxTime().add(hour.getActiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));

                result.getSeries().get(1).getData().add(hour.getApparentTotalMaxValue());
                ((RequirementLineSeries) result.getSeries().get(1)).getMaxTime().add(hour.getApparentTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));

                result.getSeries().get(2).getData().add(hour.getReactiveTotalMaxValue());
                ((RequirementLineSeries) result.getSeries().get(2)).getMaxTime().add(hour.getReactiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));

                result.getSeries().get(3).getData().add(hour.getActiveAMaxValue());
                ((RequirementLineSeries) result.getSeries().get(3)).getMaxTime().add(hour.getActiveAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));

                result.getSeries().get(4).getData().add(hour.getActiveBMaxValue());
                ((RequirementLineSeries) result.getSeries().get(4)).getMaxTime().add(hour.getActiveBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));


                result.getSeries().get(5).getData().add(hour.getApparentAMaxValue());
                ((RequirementLineSeries) result.getSeries().get(5)).getMaxTime().add(hour.getApparentAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));

                result.getSeries().get(6).getData().add(hour.getApparentBMaxValue());
                ((RequirementLineSeries) result.getSeries().get(6)).getMaxTime().add(hour.getApparentBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
            });

            return result;
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public PageResult<AisleBalanceRes> getAisleBalancePage(AisleIndexPageReqVO pageReqVO) {
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
        List<AisleBalanceRes> result = new ArrayList<>();
        List<AisleIndexDO> aisleIndexDOList = aisleIndexDOPageResult.getList();
        List mutiRedis = getMutiRedis(aisleIndexDOList);
        aisleIndexDOList.forEach(aisleIndexDO -> {
            AisleBalanceRes aisleBalanceRes = new AisleBalanceRes();
            aisleBalanceRes.setId(aisleIndexDO.getId());
            aisleBalanceRes.setRoomId(aisleIndexDO.getRoomId());
            aisleBalanceRes.setName(aisleIndexDO.getAisleName());
            result.add(aisleBalanceRes);
        });
        getPosition(result);
        Map<Integer, AisleBalanceRes> resMap = result.stream().collect(Collectors.toMap(AisleBalanceRes::getId, Function.identity()));
        for (Object o : mutiRedis) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            Integer aisleKey = jsonObject.getInteger("aisle_key");
            AisleBalanceRes aisleBalanceRes = resMap.get(aisleKey);
            JSONObject aislePower = jsonObject.getJSONObject("aisle_power");
            JSONObject totalData = aislePower.getJSONObject("total_data");
            if (Objects.nonNull(totalData)) {
                aisleBalanceRes.setPowApparentTotal(totalData.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveTotal(totalData.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveTotal(totalData.getDouble("pow_reactive"));
            }

            JSONObject pathA = aislePower.getJSONObject("path_a");
            if (Objects.nonNull(pathA)) {
                aisleBalanceRes.setPowApparentA(pathA.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveA(pathA.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveA(pathA.getDouble("pow_reactive"));
            }


            JSONObject pathB = aislePower.getJSONObject("path_b");
            if (Objects.nonNull(pathB)) {
                aisleBalanceRes.setPowApparentB(pathB.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveB(pathB.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveB(pathB.getDouble("pow_reactive"));
            }

            if (aisleBalanceRes.getPowApparentA() != null && aisleBalanceRes.getPowApparentA() != 0 && aisleBalanceRes.getPowApparentTotal() != null && aisleBalanceRes.getPowApparentTotal() != 0) {
                aisleBalanceRes.setRateA((aisleBalanceRes.getPowApparentA() / aisleBalanceRes.getPowApparentTotal()) * 100);
            } else {
                aisleBalanceRes.setRateA(0.0);
            }
            if (aisleBalanceRes.getPowApparentB() != null && aisleBalanceRes.getPowApparentB() != 0 && aisleBalanceRes.getPowApparentTotal() != null && aisleBalanceRes.getPowApparentTotal() != 0) {
                aisleBalanceRes.setRateB((aisleBalanceRes.getPowApparentB() / aisleBalanceRes.getPowApparentTotal()) * 100);
            } else {
                aisleBalanceRes.setRateB(0.0);
            }
        }
        return new PageResult<>(result, aisleIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<AisleBalanceRes> getAisBasicInformation(AisleIndexPageReqVO pageReqVO) {

        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
        List<AisleBalanceRes> result = new ArrayList<>();
        List<AisleIndexDO> aisleIndexDOList = aisleIndexDOPageResult.getList();
        List mutiRedis = getMutiRedis(aisleIndexDOList);
        aisleIndexDOList.forEach(aisleIndexDO -> {
            AisleBalanceRes aisleBalanceRes = new AisleBalanceRes();
            aisleBalanceRes.setId(aisleIndexDO.getId());
            aisleBalanceRes.setRoomId(aisleIndexDO.getRoomId());
            aisleBalanceRes.setName(aisleIndexDO.getAisleName());
            result.add(aisleBalanceRes);
        });
        getPosition(result);
        Map<Integer, AisleBalanceRes> resMap = result.stream().collect(Collectors.toMap(AisleBalanceRes::getId, Function.identity()));
        for (Object o : mutiRedis) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            Integer aisleKey = jsonObject.getInteger("aisle_key");
            AisleBalanceRes aisleBalanceRes = resMap.get(aisleKey);
            aisleBalanceRes.setLoadRate(jsonObject.getDouble("load_factor"));
            Integer aisleId = jsonObject.getInteger("aisle_key");
            AisleIndexDO aisleIndexDO = aisleIndexCopyMapper.selectById(aisleId);
            aisleBalanceRes.setEleCapacity(aisleIndexDO.getPowerCapacity());
            JSONObject aislePower = jsonObject.getJSONObject("aisle_power");
            JSONObject totalData = aislePower.getJSONObject("total_data");
            if (Objects.nonNull(totalData)) {
                aisleBalanceRes.setPowApparentTotal(totalData.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveTotal(totalData.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveTotal(totalData.getDouble("pow_reactive"));
                aisleBalanceRes.setPowFactorTotal(totalData.getDouble("power_factor"));
                aisleBalanceRes.setEleActive(totalData.getDouble("ele_active"));
            }

            JSONObject pathA = aislePower.getJSONObject("path_a");
            if (Objects.nonNull(pathA)) {
                aisleBalanceRes.setPowApparentA(pathA.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveA(pathA.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveA(pathA.getDouble("pow_reactive"));
            }


            JSONObject pathB = aislePower.getJSONObject("path_b");
            if (Objects.nonNull(pathB)) {
                aisleBalanceRes.setPowApparentB(pathB.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveB(pathB.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveB(pathB.getDouble("pow_reactive"));
            }

            if (aisleBalanceRes.getPowApparentA() != null && aisleBalanceRes.getPowApparentA() != 0 && aisleBalanceRes.getPowApparentTotal() != null && aisleBalanceRes.getPowApparentTotal() != 0) {
                aisleBalanceRes.setRateA((aisleBalanceRes.getPowApparentA() / aisleBalanceRes.getPowApparentTotal()) * 100);
            } else {
                aisleBalanceRes.setRateA(0.0);
            }
            if (aisleBalanceRes.getPowApparentB() != null && aisleBalanceRes.getPowApparentB() != 0 && aisleBalanceRes.getPowApparentTotal() != null && aisleBalanceRes.getPowApparentTotal() != 0) {
                aisleBalanceRes.setRateB((aisleBalanceRes.getPowApparentB() / aisleBalanceRes.getPowApparentTotal()) * 100);
            } else {
                aisleBalanceRes.setRateB(0.0);
            }
            if (aisleBalanceRes.getPowActiveA() != null && aisleBalanceRes.getPowActiveA() != 0 &&
                    aisleBalanceRes.getPowActiveB() != null && aisleBalanceRes.getPowActiveB() != 0) {
                Double powActiveA = aisleBalanceRes.getPowActiveA();
                Double powActiveB = aisleBalanceRes.getPowActiveB();

                // 计算差的绝对值
                Double absDifference = Math.abs(powActiveA - powActiveB);

                // 找出 powActiveA 和 powActiveB 中的最大值
                Double maxPower = Math.max(powActiveA, powActiveB);

                // 计算绝对值与最大值的商
                double ratio = absDifference / maxPower;
                aisleBalanceRes.setDeviation(ratio);
            }
        }
        return new PageResult<>(result, aisleIndexDOPageResult.getTotal());
    }

    @Override
    public Map getReportConsumeEleDataById(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        AisleLineResBase barRes = new AisleLineResBase();
        BarSeries barSeries = new BarSeries();
        try {
            if (id != null) {
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "aisle_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "aisle_eq_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                if (cabinetData.size() == 0){
                    return result;
                }
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay) {
                    List<AisleEleTotalRealtimeDo> busList = new ArrayList<>();
                    for (String str : cabinetData) {
                        nowTimes++;
                        AisleEleTotalRealtimeDo aisleEleTotalRealtimeDo = JsonUtils.parseObject(str, AisleEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = aisleEleTotalRealtimeDo.getEleTotal();
                        }
                        if (nowTimes > 1) {
                            barSeries.getData().add((float) (aisleEleTotalRealtimeDo.getEleTotal() - lastEq));
                            barRes.getTime().add(aisleEleTotalRealtimeDo.getCreateTime().toString("HH:mm:ss"));
                        }
                        lastEq = aisleEleTotalRealtimeDo.getEleTotal();
                        busList.add(aisleEleTotalRealtimeDo);
                    }

                    //计算实时用电量
                    List<AisleEleTotalRealtimeDo> dayEqList = new ArrayList<>();
                    for (int i = 0; i < cabinetData.size() - 1; i++) {
                        AisleEleTotalRealtimeDo dayEleDo = new AisleEleTotalRealtimeDo();
                        totalEq += (float) busList.get(i + 1).getEleTotal() - (float) busList.get(i).getEleTotal();
                        dayEleDo.setEleTotal(busList.get(i + 1).getEleTotal() - busList.get(i).getEleTotal());
                        dayEleDo.setCreateTime(busList.get(i+1).getCreateTime());
                        dayEqList.add(dayEleDo);
                    }
                    dayEqList.sort(Comparator.comparing(AisleEleTotalRealtimeDo::getEleTotal));
                    maxEle = dayEqList.get(dayEqList.size() - 1).getEleTotal();
                    maxEleTime = dayEqList.get(dayEqList.size() - 1).getCreateTime().toString("yyyy-MM-dd HH:mm:ss");

                    barRes.getSeries().add(barSeries);
                    result.put("totalEle", totalEq);
                    result.put("maxEle", maxEle);
                    result.put("maxEleTime", maxEleTime);
                    result.put("firstEq", firstEq);
                    result.put("lastEq", lastEq);
                    result.put("barRes", barRes);
                } else {
                    int dataIndex = 0;
                    for (String str : cabinetData) {
                        nowTimes++;
                        AisleEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
                        totalEq += totalDayDo.getEqValue();
                        barSeries.getData().add((float) totalDayDo.getEqValue());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));   if (dataIndex == 0) {
                            firstEq = totalDayDo.getStartEle();
                        }
                        if (dataIndex == cabinetData.size() - 1) {
                            lastEq = totalDayDo.getEndEle();
                        }
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "eq_value");
                    AisleEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, AisleEqTotalDayDo.class);
                    if (eqMaxValue != null) {
                        maxEle = eqMaxValue.getEqValue();
                        maxEleTime = eqMaxValue.getStartTime().toString("yyyy-MM-dd HH:mm:ss");
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("firstEq", firstEq);
                    result.put("lastEq", lastEq);
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
    public List<AisleBalanceRes> getAisBasicInformationByRoom(String roomId) {
        List<AisleBalanceRes> resList = new ArrayList<>();
        AisleIndexPageReqVO aisleIndexPageReqVO = new AisleIndexPageReqVO();
        aisleIndexPageReqVO.setRoomId(Integer.valueOf(roomId));
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(aisleIndexPageReqVO);
        List<AisleIndexDO> aisleIndexDOList = aisleIndexDOPageResult.getList();

        for (AisleIndexDO aisleIndexDO : aisleIndexDOList) {
            AisleBalanceRes aisleBalanceRes = new AisleBalanceRes();
            aisleBalanceRes.setId(aisleIndexDO.getId());
            aisleBalanceRes.setName(aisleIndexDO.getAisleName());
            List<AisleIndexDO> aisleIndexDOS = new ArrayList<>();
            aisleIndexDOS.add(aisleIndexDO);
            List mutiRedis = getMutiRedis(aisleIndexDOS);

            for (Object o : mutiRedis) {
                if (Objects.isNull(o)) {
                    continue;
                }
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
                aisleBalanceRes.setLoadRate(jsonObject.getDouble("load_factor"));
                aisleBalanceRes.setStatus(jsonObject.getInteger("status"));
                aisleBalanceRes.setEleCapacity(aisleIndexDO.getPowerCapacity());
                JSONObject aislePower = jsonObject.getJSONObject("aisle_power");
                JSONObject totalData = aislePower.getJSONObject("total_data");
                if (Objects.nonNull(totalData)) {
                    aisleBalanceRes.setPowApparentTotal(totalData.getDouble("pow_apparent"));
                    aisleBalanceRes.setPowActiveTotal(totalData.getDouble("pow_active"));
                    aisleBalanceRes.setPowReactiveTotal(totalData.getDouble("pow_reactive"));
                    aisleBalanceRes.setPowFactorTotal(totalData.getDouble("power_factor"));
                    aisleBalanceRes.setEleActive(totalData.getDouble("ele_active"));
                }
                JSONObject pathA = aislePower.getJSONObject("path_a");
                if (Objects.nonNull(pathA)) {
                    aisleBalanceRes.setPowApparentA(pathA.getDouble("pow_apparent"));
                    aisleBalanceRes.setPowActiveA(pathA.getDouble("pow_active"));
                    aisleBalanceRes.setPowReactiveA(pathA.getDouble("pow_reactive"));
                }
                JSONObject pathB = aislePower.getJSONObject("path_b");
                if (Objects.nonNull(pathB)) {
                    aisleBalanceRes.setPowApparentB(pathB.getDouble("pow_apparent"));
                    aisleBalanceRes.setPowActiveB(pathB.getDouble("pow_active"));
                    aisleBalanceRes.setPowReactiveB(pathB.getDouble("pow_reactive"));
                }
                if (pathA != null && pathB != null){
                    Double aPow = pathA.getDouble("pow_active");
                    Double bPow = pathB.getDouble("pow_active");
                    Double proportion = calculateProportion(aPow, bPow);
                    aisleBalanceRes.setRateA(proportion);
                }
            }
            resList.add(aisleBalanceRes);
        }
        return resList;
    }


    private Double calculateProportion(Double apow, Double bpow) {
        double percentageValue = 50.0;

        if (apow == null && bpow == null) {
            percentageValue = -1.0;
        } else if (apow != null && bpow == null) {
            percentageValue = 100.0;
        } else if (apow == null && bpow != null) {
            percentageValue = 0.0;
        } else if (apow != 0 && bpow == 0) {
            percentageValue = 100.0;
        } else if (apow == 0 && bpow != 0) {
            percentageValue = 0.0;
        } else if (apow != 0 && bpow != 0) {
            percentageValue = apow / (apow + bpow) * 100;
        }

        return percentageValue;
    }

    @Override
    public Map getEleByRoom(String roomId, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        AisleIndexPageReqVO aisleIndexPageReqVO = new AisleIndexPageReqVO();
        aisleIndexPageReqVO.setRoomId(Integer.valueOf(roomId));
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(aisleIndexPageReqVO);
        List<AisleIndexDO> aisleIndexDOList = aisleIndexDOPageResult.getList();
        HashMap result = new HashMap<>();
        AisleLineResBase barRes = new AisleLineResBase();
        BarSeries barSeries = new BarSeries();
        if (CollectionUtil.isEmpty(aisleIndexDOList)){
            return  result;
        }
        for (AisleIndexDO aisleIndexDO : aisleIndexDOList) {
            Integer id = aisleIndexDO.getId();
            try {
                if (id != null) {
                    String index = null;
                    boolean isSameDay = false;
                    if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                        index = "aisle_ele_total_realtime";
                        if (oldTime.equals(newTime)) {
                            newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                        }
                        isSameDay = true;
                    } else {
                        index = "aisle_eq_total_day";
                        oldTime = oldTime.plusDays(1);
                        newTime = newTime.plusDays(1);
                        isSameDay = false;
                    }
                    String startTime = localDateTimeToString(oldTime);
                    String endTime = localDateTimeToString(newTime);
                    List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                    if (cabinetData.size() == 0){
                        return result;
                    }
                    float totalEq = 0f;
                    if (isSameDay) {
                        List<AisleEleTotalRealtimeDo> busList = new ArrayList<>();
                        for (String str : cabinetData) {
                            AisleEleTotalRealtimeDo aisleEleTotalRealtimeDo = JsonUtils.parseObject(str, AisleEleTotalRealtimeDo.class);
                            busList.add(aisleEleTotalRealtimeDo);
                        }
                        //计算实时用电量
                        for (int i = 0; i < cabinetData.size() - 1; i++) {
                            totalEq += (float) busList.get(i + 1).getEleTotal() - (float) busList.get(i).getEleTotal();
                        }
                        barSeries.getData().add((totalEq));
                        barRes.getTime().add(aisleIndexDO.getAisleName());
                    } else {
                        for (String str : cabinetData) {
                            AisleEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
                            totalEq += totalDayDo.getEqValue();
                        }
                        barSeries.getData().add((totalEq));
                        barRes.getTime().add(aisleIndexDO.getAisleName());
                    }
                }
            } catch (Exception e) {
                log.error("获取数据失败", e);
            }
        }
        barRes.getSeries().add(barSeries);
        result.put("barRes",barRes);
        return result;
    }

    @Override
    public Map getReportConsumeDataById(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        AisleLineResBase barRes = new AisleLineResBase();
        BarSeries barSeries = new BarSeries();
        try {
            if (id != null) {
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "aisle_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "aisle_eq_total_day";
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
                if (isSameDay) {
                    for (String str : cabinetData) {
                        nowTimes++;
                        AisleEleTotalRealtimeDo aisleEleTotalRealtimeDo = JsonUtils.parseObject(str, AisleEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = aisleEleTotalRealtimeDo.getEleTotal();
                        }
                        if (nowTimes > 1) {
                            barSeries.getData().add((float) (aisleEleTotalRealtimeDo.getEleTotal() - lastEq));
                            barRes.getTime().add(aisleEleTotalRealtimeDo.getCreateTime().toString("HH:mm:ss"));
                        }
                        lastEq = aisleEleTotalRealtimeDo.getEleTotal();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "ele_total");
                    AisleEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, AisleEleTotalRealtimeDo.class);
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
                    for (String str : cabinetData) {
                        nowTimes++;
                        AisleEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
                        totalEq += totalDayDo.getEqValue();
                        barSeries.getData().add((float) totalDayDo.getEqValue());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "eq_value");
                    AisleEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, AisleEqTotalDayDo.class);
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
        AisleLineResBase totalLineRes = new AisleLineResBase();
        AisleLineResBase aLineRes = new AisleLineResBase();
        AisleLineResBase bLineRes = new AisleLineResBase();

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
                    index = "aisle_hda_pow_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "aisle_hda_pow_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                List<AislePowHourDo> aislePowHourDos = cabinetData.stream().map(str -> JsonUtils.parseObject(str, AislePowHourDo.class)).collect(Collectors.toList());

                if (org.springframework.util.CollectionUtils.isEmpty(cabinetData)) {
                    return result;
                }

                LineSeries totalApparentPow = new LineSeries();
                totalApparentPow.setName("总平均视在功率");
                LineSeries totalActivePow = new LineSeries();
                totalActivePow.setName("总平均有功功率");
                LineSeries totalReactivePow = new LineSeries();
                totalReactivePow.setName("总平均无功功率");
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);
                totalLineRes.getSeries().add(totalReactivePow);


                LineSeries apparentPowA = new LineSeries();
                apparentPowA.setName("A路平均视在功率");
                LineSeries activePowA = new LineSeries();
                activePowA.setName("A路平均有功功率");
                LineSeries reactivePowA = new LineSeries();
                reactivePowA.setName("A路平均无功功率");
                aLineRes.getSeries().add(apparentPowA);
                aLineRes.getSeries().add(activePowA);
                aLineRes.getSeries().add(reactivePowA);


                LineSeries apparentPowB = new LineSeries();
                apparentPowB.setName("B路平均视在功率");
                LineSeries activePowB = new LineSeries();
                activePowB.setName("B路平均有功功率");
                LineSeries reactivePowB = new LineSeries();
                reactivePowB.setName("B路平均无功功率");
                bLineRes.getSeries().add(apparentPowB);
                bLineRes.getSeries().add(activePowB);
                bLineRes.getSeries().add(reactivePowB);

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    aislePowHourDos.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalReactivePow.getData().add(hourdo.getReactiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm:ss"));

                        apparentPowA.getData().add(hourdo.getApparentAAvgValue());
                        activePowA.getData().add(hourdo.getActiveAAvgValue());
                        reactivePowA.getData().add(hourdo.getReactiveAAvgValue());
                        aLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm:ss"));

                        apparentPowB.getData().add(hourdo.getApparentBAvgValue());
                        activePowB.getData().add(hourdo.getActiveBAvgValue());
                        reactivePowB.getData().add(hourdo.getReactiveBAvgValue());
                        bLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm:ss"));
                    });
                } else {
                    aislePowHourDos.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalReactivePow.getData().add(hourdo.getReactiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));

                        apparentPowA.getData().add(hourdo.getApparentAAvgValue());
                        activePowA.getData().add(hourdo.getActiveAAvgValue());
                        reactivePowA.getData().add(hourdo.getReactiveAAvgValue());
                        aLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));

                        apparentPowB.getData().add(hourdo.getApparentBAvgValue());
                        activePowB.getData().add(hourdo.getActiveBAvgValue());
                        reactivePowB.getData().add(hourdo.getReactiveBAvgValue());
                        bLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));
                    });
                }

                String apparentTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_total_max_value");
                AislePowHourDo totalMaxApparent = JsonUtils.parseObject(apparentTotalMaxValue, AislePowHourDo.class);
                String apparentTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_total_min_value");
                AislePowHourDo totalMinApparent = JsonUtils.parseObject(apparentTotalMinValue, AislePowHourDo.class);

                String activeTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_total_max_value");
                AislePowHourDo totalMaxActive = JsonUtils.parseObject(activeTotalMaxValue, AislePowHourDo.class);
                String activeTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_total_min_value");
                AislePowHourDo totalMinActive = JsonUtils.parseObject(activeTotalMinValue, AislePowHourDo.class);


                String apparentAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_a_max_value");
                AislePowHourDo maxApparentA = JsonUtils.parseObject(apparentAMaxValue, AislePowHourDo.class);
                String apparentAMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_a_min_value");
                AislePowHourDo minApparentA = JsonUtils.parseObject(apparentAMinValue, AislePowHourDo.class);

                String activeAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_a_max_value");
                AislePowHourDo maxActiveA = JsonUtils.parseObject(activeAMaxValue, AislePowHourDo.class);
                String activeAMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_a_min_value");
                AislePowHourDo minActiveA = JsonUtils.parseObject(activeAMinValue, AislePowHourDo.class);

                String apparentBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_b_max_value");
                AislePowHourDo maxApparentB = JsonUtils.parseObject(apparentBMaxValue, AislePowHourDo.class);
                String apparentBMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_b_min_value");
                AislePowHourDo minApparentB = JsonUtils.parseObject(apparentBMinValue, AislePowHourDo.class);

                String activeBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_b_max_value");
                AislePowHourDo maxActiveB = JsonUtils.parseObject(activeBMaxValue, AislePowHourDo.class);
                String activeBMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_b_min_value");
                AislePowHourDo minActiveB = JsonUtils.parseObject(activeBMinValue, AislePowHourDo.class);

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
    public Map getReportPowDataByTypeAndId(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        AisleLineResBase totalLineRes = new AisleLineResBase();
        AisleLineResBase aLineRes = new AisleLineResBase();
        AisleLineResBase bLineRes = new AisleLineResBase();

        try {
            if (id != null) {
                String index = null;

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "aisle_hda_pow_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "aisle_hda_pow_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                List<AislePowHourDo> powList = cabinetData.stream().map(str -> JsonUtils.parseObject(str, AislePowHourDo.class)).collect(Collectors.toList());

                if (org.springframework.util.CollectionUtils.isEmpty(cabinetData)) {
                    return result;
                }
                //总
                LineSeries totalApparentPow = new LineSeries();
                LineSeries totalActivePow = new LineSeries();
                LineSeries totalReactivePow = new LineSeries();
                List<String> totalApparentPowHappenTime = new ArrayList<>();
                List<String> totalActivePowHappenTime = new ArrayList<>();
                List<String> totalReactivePowHappenTime = new ArrayList<>();
                //A路
                LineSeries aApparentPow = new LineSeries();
                LineSeries aActivePow = new LineSeries();
                LineSeries aReactivePow = new LineSeries();
                List<String> aApparentPowHappenTime = new ArrayList<>();
                List<String> aActivePowHappenTime = new ArrayList<>();
                List<String> aReactivePowHappenTime = new ArrayList<>();
                //B路
                LineSeries bApparentPow = new LineSeries();
                LineSeries bActivePow = new LineSeries();
                LineSeries bReactivePow = new LineSeries();
                List<String> bApparentPowHappenTime = new ArrayList<>();
                List<String> bActivePowHappenTime = new ArrayList<>();
                List<String> bReactivePowHappenTime = new ArrayList<>();


                if (dataType == 1) {
                    //总
                    totalApparentPowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getApparentTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalActivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getActiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalReactivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getReactiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    //A路
                    aApparentPowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getApparentAMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    aActivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getActiveAMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    aReactivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getReactiveAMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    //B路
                    bApparentPowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getApparentBMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    bActivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getActiveBMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    bReactivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getReactiveBMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());

                } else if (dataType == -1) {
                    totalApparentPowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getApparentTotalMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalActivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getActiveTotalMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalReactivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getReactiveTotalMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    //A路
                    aApparentPowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getApparentAMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    aActivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getActiveAMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    aReactivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getReactiveAMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    //B路
                    bApparentPowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getApparentBMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    bActivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getActiveBMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    bReactivePowHappenTime = powList.stream().map(AislePowHourDo -> AislePowHourDo.getReactiveBMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }
                totalApparentPow.setHappenTime(totalApparentPowHappenTime);
                totalActivePow.setHappenTime(totalActivePowHappenTime);
                totalReactivePow.setHappenTime(totalReactivePowHappenTime);

                aApparentPow.setHappenTime(aApparentPowHappenTime);
                aActivePow.setHappenTime(aActivePowHappenTime);
                aReactivePow.setHappenTime(aReactivePowHappenTime);

                bApparentPow.setHappenTime(bApparentPowHappenTime);
                bActivePow.setHappenTime(bActivePowHappenTime);
                bReactivePow.setHappenTime(bReactivePowHappenTime);

                for (AislePowHourDo boxTotalHourDo : powList) {
                    if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                        totalLineRes.getTime().add(boxTotalHourDo.getCreateTime().toString("HH:mm"));
                        aLineRes.getTime().add(boxTotalHourDo.getCreateTime().toString("HH:mm"));
                        bLineRes.getTime().add(boxTotalHourDo.getCreateTime().toString("HH:mm"));
                    } else {
                        totalLineRes.getTime().add(boxTotalHourDo.getCreateTime().toString("yyyy-MM-dd"));
                        aLineRes.getTime().add(boxTotalHourDo.getCreateTime().toString("yyyy-MM-dd"));
                        bLineRes.getTime().add(boxTotalHourDo.getCreateTime().toString("yyyy-MM-dd"));
                    }
                }


                if (dataType == 1) {
                    totalApparentPow.setName("总最大视在功率");
                    totalActivePow.setName("总最大有功功率");
                    totalReactivePow.setName("总最大无功功率");
                    aApparentPow.setName("A路最大视在功率");
                    aActivePow.setName("A路最大有功功率");
                    aReactivePow.setName("A路最大无功功率");
                    bApparentPow.setName("B路最大视在功率");
                    bActivePow.setName("B路最大有功功率");
                    bReactivePow.setName("B路最大无功功率");
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalMaxValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalMaxValue());
                        totalReactivePow.getData().add(hourdo.getReactiveTotalMaxValue());

                        aApparentPow.getData().add(hourdo.getApparentAMaxValue());
                        aActivePow.getData().add(hourdo.getActiveAMaxValue());
                        aReactivePow.getData().add(hourdo.getReactiveAMaxValue());

                        bApparentPow.getData().add(hourdo.getApparentBMaxValue());
                        bActivePow.getData().add(hourdo.getActiveBMaxValue());
                        bReactivePow.getData().add(hourdo.getReactiveBMaxValue());
                    });
                } else if (dataType == 0) {
                    totalApparentPow.setName("总平均视在功率");
                    totalActivePow.setName("总平均有功功率");
                    totalReactivePow.setName("总平均无功功率");
                    aApparentPow.setName("A路平均视在功率");
                    aActivePow.setName("A路平均有功功率");
                    aReactivePow.setName("A路平均无功功率");
                    bApparentPow.setName("B路平均视在功率");
                    bActivePow.setName("B路平均有功功率");
                    bReactivePow.setName("B路平均无功功率");
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalReactivePow.getData().add(hourdo.getReactiveTotalAvgValue());

                        aApparentPow.getData().add(hourdo.getApparentAAvgValue());
                        aActivePow.getData().add(hourdo.getActiveAAvgValue());
                        aReactivePow.getData().add(hourdo.getReactiveAAvgValue());

                        bApparentPow.getData().add(hourdo.getApparentBAvgValue());
                        bActivePow.getData().add(hourdo.getActiveBAvgValue());
                        bReactivePow.getData().add(hourdo.getReactiveBAvgValue());
                    });
                } else if (dataType == -1) {
                    totalApparentPow.setName("总最小视在功率");
                    totalActivePow.setName("总最小有功功率");
                    totalReactivePow.setName("总最小无功功率");
                    aApparentPow.setName("A路最小视在功率");
                    aActivePow.setName("A路最小有功功率");
                    aReactivePow.setName("A路最小无功功率");
                    bApparentPow.setName("B路最小视在功率");
                    bActivePow.setName("B路最小有功功率");
                    bReactivePow.setName("B路最小无功功率");
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalMinValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalMinValue());
                        totalReactivePow.getData().add(hourdo.getReactiveTotalAvgValue());

                        aApparentPow.getData().add(hourdo.getApparentAMinValue());
                        aActivePow.getData().add(hourdo.getActiveAMinValue());
                        aReactivePow.getData().add(hourdo.getReactiveAMinValue());

                        bApparentPow.getData().add(hourdo.getApparentBMinValue());
                        bActivePow.getData().add(hourdo.getActiveBMinValue());
                        bReactivePow.getData().add(hourdo.getReactiveBMinValue());
                    });
                }
                processPowMavMin(powList, dataType, result);
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);
                totalLineRes.getSeries().add(totalReactivePow);
                aLineRes.getSeries().add(aApparentPow);
                aLineRes.getSeries().add(aActivePow);
                aLineRes.getSeries().add(aReactivePow);
                bLineRes.getSeries().add(bApparentPow);
                bLineRes.getSeries().add(bActivePow);
                bLineRes.getSeries().add(bReactivePow);
                result.put("aLineRes", aLineRes);
                result.put("bLineRes", bLineRes);
                result.put("totalLineRes", totalLineRes);

            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public Map aisleHdaLineHisdataReportKey(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map<String, Object> resultMap = new HashMap<>();
        AisleLineResBase curResBase = new AisleLineResBase();
        AisleLineResBase volResBase = new AisleLineResBase();
        List<String> xTime = new ArrayList<>();
        String index = (timeType == 0 || oldTime.toLocalDate().equals(newTime.toLocalDate())) ? "aisle_hda_line_hour" : "aisle_hda_line_day";
        boolean isSameDay = (timeType == 0 || oldTime.toLocalDate().equals(newTime.toLocalDate()));

        if (!isSameDay) {
            oldTime = oldTime.plusDays(1).withHour(0).withMinute(0).withSecond(0);
            newTime = newTime.plusDays(1).withHour(23).withMinute(59).withSecond(59);
        }
        List<String> strData = new ArrayList<>();
        String startTime = localDateTimeToString(oldTime);
        String endTime = localDateTimeToString(newTime);

        try {
            SearchRequest searchRequest = new SearchRequest(index);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.termQuery("aisle_id", id));
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(startTime)
                    .to(endTime));
            searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            searchSourceBuilder.size(1000);
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse != null) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    String str = hit.getSourceAsString();
                    AisleHdaLineHour houResVO = JsonUtils.parseObject(str, AisleHdaLineHour.class);
                    boolean b = DataProcessingUtils.processLineHisData(houResVO, resultMap, isSameDay, DataTypeEnums.fromValue(dataType));
                    if (!b){
                        return new HashMap();
                    }
                    strData.add(str);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<AisleHdaLineHour> powList = strData.stream().map(str -> JsonUtils.parseObject(str, AisleHdaLineHour.class)).collect(Collectors.toList());
        processLineMavMin(powList,dataType,resultMap);

        int aIndex = 1;
        int bIndex = 2;
        // 处理结果
        for (int lineId = 1; lineId <= 3; lineId++) {
            String lineKey = "dayList" + lineId;
            Map<String, Object> lineData = (Map<String, Object>) resultMap.get(lineKey);
            if (lineData != null && !(((List<AisleHdaLineHour>) lineData.get("data")).isEmpty())) {
                LineSeries curASeries = new LineSeries();
                LineSeries volASeries = new LineSeries();

                LineSeries curBSeries = new LineSeries();
                LineSeries volBSeries = new LineSeries();

                resultMap.put("curName" + aIndex, "A路L" + lineId + "相电流");
                resultMap.put("volName" + aIndex, "A路L" + lineId + "相电压");

                curASeries.setName("A-L" + lineId);
                volASeries.setName("A-L" + lineId);

                resultMap.put("curName" + bIndex, "B路L" + lineId + "相电流");
                resultMap.put("volName" + bIndex, "B路L" + lineId + "相电压");

                curBSeries.setName("B-L" + lineId);
                volBSeries.setName("B-L" + lineId);

                curASeries.setData((List<Float>) lineData.get("curADataList"));
                curASeries.setHappenTime((List<String>) lineData.get("curAHappenTime"));
                volASeries.setData((List<Float>) lineData.get("volADataList"));
                volASeries.setHappenTime((List<String>) lineData.get("volAHappenTime"));

                curBSeries.setData((List<Float>) lineData.get("curBDataList"));
                curBSeries.setHappenTime((List<String>) lineData.get("curBHappenTime"));
                volBSeries.setData((List<Float>) lineData.get("volBDataList"));
                volBSeries.setHappenTime((List<String>) lineData.get("volBHappenTime"));

                curResBase.getSeries().add(curASeries);
                volResBase.getSeries().add(volASeries);
                curResBase.getSeries().add(curBSeries);
                volResBase.getSeries().add(volBSeries);
            }
            resultMap.put("lineNumber", lineId);
            aIndex = aIndex+2;
            bIndex = bIndex+2;
        }

        // 添加时间轴数据
        List<String> uniqueDateTimes = (List<String>) resultMap.getOrDefault("dateTimes", new ArrayList<>());
        xTime = uniqueDateTimes.stream().distinct().collect(Collectors.toList());
        curResBase.setTime(xTime);
        volResBase.setTime(xTime);
        resultMap.put("curRes", curResBase);
        resultMap.put("volRes", volResBase);
        return resultMap;
    }



    public void processLineMavMin(List<AisleHdaLineHour> powList, Integer dataType, Map<String, Object> result) {
        //电流
        //A相
        PowerData lineCurARoadA = new PowerData();
        PowerData lineCurARoadB = new PowerData();

        //B相
        PowerData lineCurBRoadA = new PowerData();
        PowerData lineCurBRoadB = new PowerData();

        //C相
        PowerData lineCurCRoadA = new PowerData();
        PowerData lineCurCRoadB = new PowerData();

        //电压
        //A相
        PowerData lineVolARoadA = new PowerData();
        PowerData lineVolARoadB = new PowerData();

        //B相
        PowerData lineVolBRoadA = new PowerData();
        PowerData lineVolBRoadB = new PowerData();

        //C相
        PowerData lineVolCRoadA = new PowerData();
        PowerData lineVolCRoadB = new PowerData();
        for (AisleHdaLineHour aisleHdaLineHour : powList) {
            int lineId = aisleHdaLineHour.getLineId();
            switch (lineId) {
                case 0:
                    updatePowerData(lineCurARoadA, safeFloatValue(aisleHdaLineHour.getCurAMaxValue()), safeLocalDateTime(aisleHdaLineHour.getCurAMaxTime()), safeFloatValue(aisleHdaLineHour.getCurAAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getCurAMinValue()), safeLocalDateTime(aisleHdaLineHour.getCurAMinTime()), dataType);
                    updatePowerData(lineCurARoadB, safeFloatValue(aisleHdaLineHour.getCurBMaxValue()), safeLocalDateTime(aisleHdaLineHour.getCurBMaxTime()), safeFloatValue(aisleHdaLineHour.getCurBAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getCurBMinValue()), safeLocalDateTime(aisleHdaLineHour.getCurBMinTime()), dataType);

                    updatePowerData(lineVolARoadA, safeFloatValue(aisleHdaLineHour.getVolAMaxValue()), safeLocalDateTime(aisleHdaLineHour.getVolABaxTime()), safeFloatValue(aisleHdaLineHour.getVolAAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getVolAMinValue()), safeLocalDateTime(aisleHdaLineHour.getVolAMinTime()), dataType);
                    updatePowerData(lineVolARoadB, safeFloatValue(aisleHdaLineHour.getVolBMaxValue()), safeLocalDateTime(aisleHdaLineHour.getVolBMaxTime()), safeFloatValue(aisleHdaLineHour.getVolBAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getVolBMinValue()), safeLocalDateTime(aisleHdaLineHour.getVolBMinTime()), dataType);
                    break;
                case 1:
                    updatePowerData(lineCurBRoadA, safeFloatValue(aisleHdaLineHour.getCurAMaxValue()), safeLocalDateTime(aisleHdaLineHour.getCurAMaxTime()), safeFloatValue(aisleHdaLineHour.getCurAAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getCurAMinValue()), safeLocalDateTime(aisleHdaLineHour.getCurAMinTime()), dataType);
                    updatePowerData(lineCurBRoadB, safeFloatValue(aisleHdaLineHour.getCurBMaxValue()), safeLocalDateTime(aisleHdaLineHour.getCurBMaxTime()), safeFloatValue(aisleHdaLineHour.getCurBAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getCurBMinValue()), safeLocalDateTime(aisleHdaLineHour.getCurBMinTime()), dataType);

                    updatePowerData(lineVolBRoadA, safeFloatValue(aisleHdaLineHour.getVolAMaxValue()), safeLocalDateTime(aisleHdaLineHour.getVolABaxTime()), safeFloatValue(aisleHdaLineHour.getVolAAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getVolAMinValue()), safeLocalDateTime(aisleHdaLineHour.getVolAMinTime()), dataType);
                    updatePowerData(lineVolBRoadB, safeFloatValue(aisleHdaLineHour.getVolBMaxValue()), safeLocalDateTime(aisleHdaLineHour.getVolBMaxTime()), safeFloatValue(aisleHdaLineHour.getVolBAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getVolBMinValue()), safeLocalDateTime(aisleHdaLineHour.getVolBMinTime()), dataType);
                    break;
                case 2:
                    updatePowerData(lineCurCRoadA, safeFloatValue(aisleHdaLineHour.getCurAMaxValue()), safeLocalDateTime(aisleHdaLineHour.getCurAMaxTime()), safeFloatValue(aisleHdaLineHour.getCurAAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getCurAMinValue()), safeLocalDateTime(aisleHdaLineHour.getCurAMinTime()), dataType);
                    updatePowerData(lineCurCRoadB, safeFloatValue(aisleHdaLineHour.getCurBMaxValue()), safeLocalDateTime(aisleHdaLineHour.getCurBMaxTime()), safeFloatValue(aisleHdaLineHour.getCurBAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getCurBMinValue()), safeLocalDateTime(aisleHdaLineHour.getCurBMinTime()), dataType);

                    updatePowerData(lineVolCRoadA, safeFloatValue(aisleHdaLineHour.getVolAMaxValue()), safeLocalDateTime(aisleHdaLineHour.getVolABaxTime()), safeFloatValue(aisleHdaLineHour.getVolAAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getVolAMinValue()), safeLocalDateTime(aisleHdaLineHour.getVolAMinTime()), dataType);
                    updatePowerData(lineVolCRoadB, safeFloatValue(aisleHdaLineHour.getVolBMaxValue()), safeLocalDateTime(aisleHdaLineHour.getVolBMaxTime()), safeFloatValue(aisleHdaLineHour.getVolBAvgValue())
                            , safeFloatValue(aisleHdaLineHour.getVolBMinValue()), safeLocalDateTime(aisleHdaLineHour.getVolBMinTime()), dataType);
                    break;
                default:
                    // 处理解默认情况
                    break;
            }
        }

        //设置数据
        //电流
        result.put("curMaxValue"+1, lineCurARoadA.maxValue);
        result.put("curMaxTime"+1,lineCurARoadA.maxTime);
        result.put("curMinValue"+1,lineCurARoadA.minValue);
        result.put("curMinTime"+1,lineCurARoadA.minTime);

        result.put("curMaxValue"+2, lineCurARoadB.maxValue);
        result.put("curMaxTime"+2,lineCurARoadB.maxTime);
        result.put("curMinValue"+2,lineCurARoadB.minValue);
        result.put("curMinTime"+2,lineCurARoadB.minTime);

        result.put("curMaxValue"+3, lineCurBRoadA.maxValue);
        result.put("curMaxTime"+3,lineCurBRoadA.maxTime);
        result.put("curMinValue"+3,lineCurBRoadA.minValue);
        result.put("curMinTime"+3,lineCurBRoadA.minTime);

        result.put("curMaxValue"+4, lineCurBRoadB.maxValue);
        result.put("curMaxTime"+4,lineCurBRoadB.maxTime);
        result.put("curMinValue"+4,lineCurBRoadB.minValue);
        result.put("curMinTime"+4,lineCurBRoadB.minTime);

        result.put("curMaxValue"+5, lineCurCRoadA.maxValue);
        result.put("curMaxTime"+5,lineCurCRoadA.maxTime);
        result.put("curMinValue"+5,lineCurCRoadA.minValue);
        result.put("curMinTime"+5,lineCurCRoadA.minTime);

        result.put("curMaxValue"+6, lineCurCRoadB.maxValue);
        result.put("curMaxTime"+6,lineCurCRoadB.maxTime);
        result.put("curMinValue"+6,lineCurCRoadB.minValue);
        result.put("curMinTime"+6,lineCurCRoadB.minTime);
        //电压
        result.put("volMaxValue" + 1, lineVolARoadA.maxValue);
        result.put("volMaxTime" + 1, lineVolARoadA.maxTime);
        result.put("volMinValue" + 1, lineVolARoadA.minValue);
        result.put("volMinTime" + 1, lineVolARoadA.minTime);

        result.put("volMaxValue" + 2, lineVolARoadB.maxValue);
        result.put("volMaxTime" + 2, lineVolARoadB.maxTime);
        result.put("volMinValue" + 2, lineVolARoadB.minValue);
        result.put("volMinTime" + 2, lineVolARoadB.minTime);

        result.put("volMaxValue" + 3, lineVolBRoadA.maxValue);
        result.put("volMaxTime" + 3, lineVolBRoadA.maxTime);
        result.put("volMinValue" + 3, lineVolBRoadA.minValue);
        result.put("volMinTime" + 3, lineVolBRoadA.minTime);

        result.put("volMaxValue" + 4, lineVolBRoadB.maxValue);
        result.put("volMaxTime" + 4, lineVolBRoadB.maxTime);
        result.put("volMinValue" + 4, lineVolBRoadB.minValue);
        result.put("volMinTime" + 4, lineVolBRoadB.minTime);

        result.put("volMaxValue" + 5, lineVolCRoadA.maxValue);
        result.put("volMaxTime" + 5, lineVolCRoadA.maxTime);
        result.put("volMinValue" + 5, lineVolCRoadA.minValue);
        result.put("volMinTime" + 5, lineVolCRoadA.minTime);

        result.put("volMaxValue" + 6, lineVolCRoadB.maxValue);
        result.put("volMaxTime" + 6, lineVolCRoadB.maxTime);
        result.put("volMinValue" + 6, lineVolCRoadB.minValue);
        result.put("volMinTime" + 6, lineVolCRoadB.minTime);
    }

    private float safeFloatValue(Number value) {
        return value != null ? value.floatValue() : 0.0f;
    }

    private String safeLocalDateTime(String value) {
        return value != null ? value : "";
    }

    public void processPowMavMin(List<AislePowHourDo> powList, Integer dataType, Map<String, Object> result) {
        PowerData apparentPowData = new PowerData();
        PowerData activePowData = new PowerData();
        PowerData reactivePowData = new PowerData();

        PowerData aApparentPowData = new PowerData();
        PowerData aActivePowData = new PowerData();
        PowerData aReactivePowData = new PowerData();

        PowerData bApparentPowData = new PowerData();
        PowerData bActivePowData = new PowerData();
        PowerData bReactivePowData = new PowerData();

        for (AislePowHourDo boxTotalHourDo : powList) {
            updatePowerData(apparentPowData, boxTotalHourDo.getApparentTotalMaxValue(), boxTotalHourDo.getApparentTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getApparentTotalAvgValue(), boxTotalHourDo.getApparentTotalMinValue(), boxTotalHourDo.getApparentTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(activePowData, boxTotalHourDo.getActiveTotalMaxValue(), boxTotalHourDo.getActiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getActiveTotalAvgValue(), boxTotalHourDo.getActiveTotalMinValue(), boxTotalHourDo.getActiveTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(reactivePowData, boxTotalHourDo.getReactiveTotalMaxValue(), boxTotalHourDo.getReactiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getReactiveTotalAvgValue(), boxTotalHourDo.getReactiveTotalMinValue(), boxTotalHourDo.getReactiveTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);

            updatePowerData(aApparentPowData, boxTotalHourDo.getApparentAMaxValue(), boxTotalHourDo.getApparentAMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getApparentAAvgValue(), boxTotalHourDo.getApparentAMinValue(), boxTotalHourDo.getApparentAMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(aActivePowData, boxTotalHourDo.getActiveAMaxValue(), boxTotalHourDo.getActiveAMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getActiveAAvgValue(), boxTotalHourDo.getActiveAMinValue(), boxTotalHourDo.getActiveAMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(aReactivePowData, boxTotalHourDo.getReactiveAMaxValue(), boxTotalHourDo.getReactiveAMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getReactiveAAvgValue(), boxTotalHourDo.getReactiveAMinValue(), boxTotalHourDo.getReactiveAMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);

            updatePowerData(bApparentPowData, boxTotalHourDo.getApparentBMaxValue(), boxTotalHourDo.getApparentBMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getApparentBAvgValue(), boxTotalHourDo.getApparentBMinValue(), boxTotalHourDo.getApparentBMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(bActivePowData, boxTotalHourDo.getActiveBMaxValue(), boxTotalHourDo.getActiveBMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getActiveBAvgValue(), boxTotalHourDo.getActiveBMinValue(), boxTotalHourDo.getActiveBMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(bReactivePowData, boxTotalHourDo.getReactiveBMaxValue(), boxTotalHourDo.getReactiveBMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getReactiveBAvgValue(), boxTotalHourDo.getReactiveBMinValue(), boxTotalHourDo.getReactiveBMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);

        }
        //总
        result.put("apparentPowMaxValue", apparentPowData.getMaxValue());
        result.put("apparentPowMaxTime", apparentPowData.getMaxTime());
        result.put("apparentPowMinValue", apparentPowData.getMinValue());
        result.put("apparentPowMinTime", apparentPowData.getMinTime());
        result.put("activePowMaxValue", activePowData.getMaxValue());
        result.put("activePowMaxTime", activePowData.getMaxTime());
        result.put("activePowMinValue", activePowData.getMinValue());
        result.put("activePowMinTime", activePowData.getMinTime());
        result.put("reactivePowMaxValue", reactivePowData.getMaxValue());
        result.put("reactivePowMaxTime", reactivePowData.getMaxTime());
        result.put("reactivePowMinValue", reactivePowData.getMinValue());
        result.put("reactivePowMinTime", reactivePowData.getMinTime());
        //A路
        result.put("apparentAPowMaxValue", aApparentPowData.getMaxValue());
        result.put("apparentAPowMaxTime", aApparentPowData.getMaxTime());
        result.put("apparentAPowMinValue", aApparentPowData.getMinValue());
        result.put("apparentAPowMinTime", aApparentPowData.getMinTime());
        result.put("activeAPowMaxValue", aActivePowData.getMaxValue());
        result.put("activeAPowMaxTime", aActivePowData.getMaxTime());
        result.put("activeAPowMinValue", aActivePowData.getMinValue());
        result.put("activeAPowMinTime", aActivePowData.getMinTime());
        result.put("reactiveAPowMaxValue", aReactivePowData.getMaxValue());
        result.put("reactiveAPowMaxTime", aReactivePowData.getMaxTime());
        result.put("reactiveAPowMinValue", aReactivePowData.getMinValue());
        result.put("reactiveAPowMinTime", aReactivePowData.getMinTime());
        //B路
        result.put("apparentBPowMaxValue", bApparentPowData.getMaxValue());
        result.put("apparentBPowMaxTime", bApparentPowData.getMaxTime());
        result.put("apparentBPowMinValue", bApparentPowData.getMinValue());
        result.put("apparentBPowMinTime", bApparentPowData.getMinTime());
        result.put("activeBPowMaxValue", bActivePowData.getMaxValue());
        result.put("activeBPowMaxTime", bActivePowData.getMaxTime());
        result.put("activeBPowMinValue", bActivePowData.getMinValue());
        result.put("activeBPowMinTime", bActivePowData.getMinTime());
        result.put("reactiveBPowMaxValue", bReactivePowData.getMaxValue());
        result.put("reactiveBPowMaxTime", bReactivePowData.getMaxTime());
        result.put("reactiveBPowMinValue", bReactivePowData.getMinValue());
        result.put("reactiveBPowMinTime", bReactivePowData.getMinTime());
    }

    /**
     * 更新数值
     *
     * @param powerData
     * @param maxValue
     * @param maxTime
     * @param minValue
     * @param minTime
     * @param dataType
     */
    private void updatePowerData(PowerData powerData, Float maxValue, String maxTime, Float avgValue, Float minValue, String minTime, Integer dataType) {
        if (dataType == 1) {
            updateExtremes(powerData, maxValue, maxTime, maxValue, maxTime);
        } else if (dataType == 0) {
            updateExtremes(powerData, avgValue, "无", avgValue, "无");
        } else if (dataType == -1) {
            updateExtremes(powerData, minValue, minTime, minValue, minTime);
        }
    }

    private void updateExtremes(PowerData powerData, Float maxValue, String maxTime, Float minValue, String minTime) {
        if (powerData.getMaxValue() <= maxValue) {
            powerData.setMaxValue(maxValue);
            powerData.setMaxTime(maxTime);
        }
        if (powerData.getMinValue() >= minValue) {
            powerData.setMinValue(minValue);
            powerData.setMinTime(minTime);
        }
    }

    /**
     * 数值辅助类
     */
    private static class PowerData {
        private int maxId;
        private int minId;
        private Float maxValue = 0f;
        private Float minValue = Float.MAX_VALUE;
        private String maxTime = "";
        private String minTime = "";

        public int getMaxId() {
            return maxId;
        }

        public void setMaxId(int id) {
            this.maxId = id;
        }

        public int getMinId() {
            return minId;
        }

        public void setMinId(int id) {
            this.minId = id;
        }


        public Float getMaxValue() {
            return maxValue;
        }

        public void setMaxValue(Float maxValue) {
            this.maxValue = maxValue;
        }

        public String getMaxTime() {
            return maxTime;
        }

        public void setMaxTime(String maxTime) {
            this.maxTime = maxTime;
        }

        public Float getMinValue() {
            return minValue;
        }

        public void setMinValue(Float minValue) {
            this.minValue = minValue;
        }

        public String getMinTime() {
            return minTime;
        }

        public void setMinTime(String minTime) {
            this.minTime = minTime;
        }
    }

    @Override
    public Map getAislePFLine(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        AisleLineResBase totalLineRes = new AisleLineResBase();
        result.put("pfLineRes", totalLineRes);
        try {

            String index = null;

            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                index = "aisle_hda_pow_hour";
                if (oldTime.equals(newTime)) {
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }

            } else {
                index = "aisle_hda_pow_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }

            String startTime = localDateTimeToString(oldTime);
            String endTime = localDateTimeToString(newTime);
            List<String> data = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
            List<AislePowHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, AislePowHourDo.class)).collect(Collectors.toList());

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
    public Map getAislePFLineByType(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        AisleLineResBase totalLineRes = new AisleLineResBase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        result.put("pfLineRes", totalLineRes);
        try {

            String index = null;
            boolean isSameDay = (timeType == 0 || oldTime.toLocalDate().equals(newTime.toLocalDate()));
            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                index = "aisle_hda_pow_hour";
                if (oldTime.equals(newTime)) {
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }

            } else {
                index = "aisle_hda_pow_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }

            String startTime = localDateTimeToString(oldTime);
            String endTime = localDateTimeToString(newTime);
            List<String> data = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
            List<AislePowHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, AislePowHourDo.class)).collect(Collectors.toList());

            //处理数据
            for (AislePowHourDo aislePowHourDo : powList) {
                DataProcessingUtils.collectPhaseData(aislePowHourDo, result, isSameDay, DataTypeEnums.fromValue(dataType));
            }
            String lineKey = "dayList";
            Map<String, Object> lineData = (Map<String, Object>) result.get(lineKey);
            if (lineData != null && !(((List<AislePowHourDo>) lineData.get("data")).isEmpty())) {
                LineSeries totalSeries = new LineSeries();
                LineSeries aSeries = new LineSeries();
                LineSeries bSeries = new LineSeries();

                totalSeries.setName(getSeriesName("总", FACTOR_FORMAT, dataType));
                aSeries.setName(getSeriesName("A路", FACTOR_FORMAT, dataType));
                bSeries.setName(getSeriesName("B路", FACTOR_FORMAT, dataType));

                totalSeries.setData((List<Float>) lineData.get("factorTotalList"));
                totalSeries.setHappenTime((List<String>) lineData.get("totalHappenTime"));
                aSeries.setData((List<Float>) lineData.get("factorAList"));
                aSeries.setHappenTime((List<String>) lineData.get("AHappenTime"));
                bSeries.setData((List<Float>) lineData.get("factorBList"));
                bSeries.setHappenTime((List<String>) lineData.get("BHappenTime"));

                // 添加时间轴数据
                List<String> uniqueDateTimes = (List<String>) result.getOrDefault("dateTimes", new ArrayList<>());
//                List<String> xTime = uniqueDateTimes.stream().distinct().collect(Collectors.toList());

                Map<String, Object> analyzeFactorData = DataProcessingUtils.analyzeFactorData((List<AislePowHourDo>) lineData.get("data"), dataType);
                DataProcessingUtils.FactorTotalResult totalFactor = (DataProcessingUtils.FactorTotalResult) analyzeFactorData.get("totalFactor");
                DataProcessingUtils.FactorAResult aFactor = (DataProcessingUtils.FactorAResult) analyzeFactorData.get("aFactor");
                DataProcessingUtils.FactorBResult bFactor = (DataProcessingUtils.FactorBResult) analyzeFactorData.get("bFactor");

                if (dataType != 0) {
                    result.put("pName" + 1, "总功率因数");
                    result.put("fMax" + 1, totalFactor.totalFactorMax);
                    result.put("fMaxTime" + 1, sdf.format(totalFactor.totalFactorMaxTime));
                    result.put("fMin" + 1, totalFactor.totalFactorMin);
                    result.put("fMinTime" + 1, sdf.format(totalFactor.totalFactorMinTime));

                    result.put("pName" + 2, "A路功率因数");
                    result.put("fMax" + 2, aFactor.aFactorMax);
                    result.put("fMaxTime" + 2, sdf.format(aFactor.aFactorMaxTime));
                    result.put("fMin" + 2, aFactor.aFactorMin);
                    result.put("fMinTime" + 2, sdf.format(aFactor.aFactorMinTime));

                    result.put("pName" + 3, "B路功率因数");
                    result.put("fMax" + 3, bFactor.bFactorMax);
                    result.put("fMaxTime" + 3, sdf.format(bFactor.bFactorMaxTime));
                    result.put("fMin" + 3, bFactor.bFactorMin);
                    result.put("fMinTime" + 3, sdf.format(bFactor.bFactorMinTime));
                } else {
                    result.put("pName" + 1, "总功率因数");
                    result.put("fMax" + 1, totalFactor.totalFactorMax);
                    result.put("fMaxTime" + 1, "无");
                    result.put("fMin" + 1, totalFactor.totalFactorMin);
                    result.put("fMinTime" + 1, "无");

                    result.put("pName" + 2, "A路功率因数");
                    result.put("fMax" + 2, aFactor.aFactorMax);
                    result.put("fMaxTime" + 2, "无");
                    result.put("fMin" + 2, aFactor.aFactorMin);
                    result.put("fMinTime" + 2, "无");

                    result.put("pName" + 3, "B路功率因数");
                    result.put("fMax" + 3, bFactor.bFactorMax);
                    result.put("fMaxTime" + 3, "无");
                    result.put("fMin" + 3, bFactor.bFactorMin);
                    result.put("fMinTime" + 3, "无");
                }
                totalLineRes.getSeries().add(totalSeries);
                totalLineRes.getSeries().add(aSeries);
                totalLineRes.getSeries().add(bSeries);
                totalLineRes.setTime(uniqueDateTimes);
            }
            result.put("pfLineRes", totalLineRes);

        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }


    private String getSeriesName(String preFix, String type, Integer dataType) {
        String midName = "";
        switch (dataType) {
            case 1:
                midName = "最大";
                break;
            case 0:
                midName = "平均";
                break;
            case -1:
                midName = "最小";
                break;
        }

        return preFix + midName + type;
    }



    private List getMutiRedis(List<AisleIndexDO> list) {
        List<String> devKeys = list.stream().map(busIndexDo -> REDIS_KEY_AISLE + busIndexDo.getId()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    private List getMutiBusRedis(List<String> devKey) {
        List<String> devKeys = devKey.stream().map(string -> REDIS_KEY_BUS + string).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    private void getPosition(List<? extends AisleIndexRespVO> res) {
        if (CollectionUtils.isAnyEmpty(res)) {
            return;
        }
        List<Integer> roomIds = res.stream().map(AisleIndexRespVO::getRoomId).collect(Collectors.toList());
        Map<Integer, String> roomMap = roomIndexMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getRoomName));
        res.forEach(aisleIndexRespVO -> {
            aisleIndexRespVO.setLocation(roomMap.get(aisleIndexRespVO.getRoomId()) + SPLIT_KEY + aisleIndexRespVO.getName());
        });
    }


    private Map<Integer, String> getPositionByIds(List<Integer> roomIds) {
        Map<Integer, String> roomMap = new HashMap<>();
        if (CollectionUtils.isAnyEmpty(roomIds)) {
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
    private List<AisleEqTrendDTO> dayTrend(int id) throws IOException {
        List<AisleEqTrendDTO> trendDTOList = new ArrayList<>();

        //今日
        String startTime = "";
        String endTime = DateUtil.formatDateTime(DateTime.now());

        //昨日
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));

        List<String> list = getData(startTime, endTime, id, AISLE_ELE_TOTAL_REALTIME);

        List<AisleEleTotalRealtimeDo> yesterdayList = new ArrayList<>();
        List<AisleEleTotalRealtimeDo> todayList = new ArrayList<>();
        String finalStartTime = startTime;
        list.forEach(str -> {
            AisleEleTotalRealtimeDo realtimeDo = JsonUtils.parseObject(str, AisleEleTotalRealtimeDo.class);
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
        Map<String, AisleEqTrendDTO> map = new HashMap<>();
        //昨日数据处理
        for (int i = 0; i < yesterdayList.size() - 1; i++) {

            //前一个时间点
            AisleEleTotalRealtimeDo reDo = yesterdayList.get(i);
            //当前时间点
            AisleEleTotalRealtimeDo thisDo = yesterdayList.get(i + 1);

            String dateTime = thisDo.getCreateTime().toString("HH") + ":00";
            log.info("reDo : " + reDo.getEleTotal() + "thisDo : " + thisDo.getEleTotal());
            //避免负数
            double eq = (thisDo.getEleTotal() - reDo.getEleTotal()) < 0 ? 0 : thisDo.getEleTotal() - reDo.getEleTotal();

            AisleEqTrendDTO busEqTrendDTO = new AisleEqTrendDTO();
            busEqTrendDTO.setLastEq(eq);
            busEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, busEqTrendDTO);

        }
        //今日数据处理
        for (int i = 0; i < todayList.size() - 1; i++) {

            //前一个时间点
            AisleEleTotalRealtimeDo reDo = todayList.get(i);
            //当前时间点
            AisleEleTotalRealtimeDo thisDo = todayList.get(i + 1);

            String dateTime = thisDo.getCreateTime().toString("HH") + ":00";
            log.info("reDo : " + reDo.getEleTotal() + "thisDo : " + thisDo.getEleTotal());

            //避免负数
            double eq = (thisDo.getEleTotal() - reDo.getEleTotal()) < 0 ? 0 : thisDo.getEleTotal() - reDo.getEleTotal();

            AisleEqTrendDTO busEqTrendDTO = map.get(dateTime);
            if (Objects.isNull(busEqTrendDTO)) {
                busEqTrendDTO = new AisleEqTrendDTO();
            }
            busEqTrendDTO.setEq(eq);
            busEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, busEqTrendDTO);
        }

        map.keySet().forEach(key -> trendDTOList.add(map.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(AisleEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }

    /**
     * 周趋势
     *
     * @param id
     * @return
     */
    private List<AisleEqTrendDTO> weekTrend(int id) throws IOException {
        List<AisleEqTrendDTO> trendDTOList = new ArrayList<>();

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

        List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_DAY);

        Map<String, AisleEqTotalDayDo> weekMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            weekMap.put(dateTime, realtimeDo);

        });
        Map<Integer, AisleEqTrendDTO> data = new HashMap<>();
        //本周数据
        thisWeek.keySet().forEach(key -> {

            String date = thisWeek.get(key);

            AisleEqTotalDayDo realtimeDo = weekMap.get(date);

            AisleEqTrendDTO trendDTO = new AisleEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<Integer, String> lastWeek = getWeek(startTime);
        //上周数据
        lastWeek.keySet().forEach(key -> {

            String date = lastWeek.get(key);

            AisleEqTotalDayDo realtimeDo = weekMap.get(date);

            AisleEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new AisleEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(AisleEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }


    /**
     * 月趋势
     *
     * @param id
     * @return
     */
    private List<AisleEqTrendDTO> monthTrend(int id) throws IOException {
        List<AisleEqTrendDTO> trendDTOList = new ArrayList<>();

        //本月
        String startTime = "";


        //上月第一天
        Calendar lastMonthFirstDateCal = Calendar.getInstance();
        lastMonthFirstDateCal.add(Calendar.MONTH, -1);
        lastMonthFirstDateCal.set(Calendar.DAY_OF_MONTH, 1);

        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(lastMonthFirstDateCal.getTime()));

        String endTime = DateUtil.formatDateTime(DateTime.now());

        List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_DAY);

        Map<String, AisleEqTotalDayDo> monthMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            monthMap.put(dateTime, realtimeDo);

        });
        Map<String, AisleEqTrendDTO> data = new HashMap<>();

        Map<String, String> thisMonth = getThisMonth();
        //本月数据
        thisMonth.keySet().forEach(key -> {

            String date = thisMonth.get(key);

            AisleEqTotalDayDo realtimeDo = monthMap.get(date);

            AisleEqTrendDTO trendDTO = new AisleEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<String, String> lastMonth = getLastMonth();
        //上月数据
        lastMonth.keySet().forEach(key -> {

            String date = lastMonth.get(key);

            AisleEqTotalDayDo realtimeDo = monthMap.get(date);

            AisleEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new AisleEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(AisleEqTrendDTO::getDateTime)).collect(Collectors.toList());


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
    private void getDayChain(int id, AisleEleChainDTO chainDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //日环比
        //今日
        startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double todayEq = getDayEq(startTime, endTime, id);
        chainDTO.setTodayEq(new BigDecimal(todayEq).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        //昨日
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_DAY);
        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalDayDo dayDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
            String dateTime = dayDo.getCreateTime().toString(HOUR_FORMAT);
            if (Objects.equals(dayDo.getEqValue(), 0.0)) {
                eqMap.put(dateTime, dayDo.getEndEle());
            } else {
                eqMap.put(dateTime, dayDo.getEqValue());
            }
        });
        //前日
        String lastTime = DateUtil.formatDate(DateUtil.parse(startTime, HOUR_FORMAT));
        double eveEq = eqMap.containsKey(lastTime) ? eqMap.get(lastTime) : 0;

        //昨天
        String thisTime = DateUtil.formatDate(DateUtil.beginOfDay(DateTime.now()));
        double lastEq = eqMap.containsKey(thisTime) ? eqMap.get(thisTime) : 0;

        NumberFormat percentInstance = NumberFormat.getPercentInstance();
        // 设置保留几位小数，这里设置的是保留两位小数
        percentInstance.setMinimumFractionDigits(2);

        chainDTO.setYesterdayEq(new BigDecimal(lastEq).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());

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
    private void getWeekChain(int id, AisleEleChainDTO chainDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //周环比
        //本周
        startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double thisWeekEq = getDayEq(startTime, endTime, id);
        chainDTO.setThisWeekEq(new BigDecimal(thisWeekEq).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        //上周
        LocalDate today = LocalDate.now();
        //上周第一天
        DayOfWeek todayOfWeek = today.getDayOfWeek();
        int sub = todayOfWeek.getValue() + 6;
        LocalDate lastWeekFirst = today.minusDays(sub);


        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(Date.from(lastWeekFirst.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_WEEK);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalWeekDo weekDo = JsonUtils.parseObject(str, AisleEqTotalWeekDo.class);
            String dateTime = weekDo.getCreateTime().toString(HOUR_FORMAT);
//            eqMap.put(dateTime, weekDo.getEqValue());
            if (Objects.equals(weekDo.getEqValue(), 0.0)) {
                eqMap.put(dateTime, weekDo.getEndEle());
            } else {
                eqMap.put(dateTime, weekDo.getEqValue());
            }
        });
        //上上周
        String lastTime = DateUtil.formatDate(DateUtil.parse(startTime, HOUR_FORMAT));
        double eveEq = eqMap.containsKey(lastTime) ? eqMap.get(lastTime) : 0;

        //上周
        String thisTime = DateUtil.formatDate(DateUtil.beginOfWeek(DateTime.now()));
        double lastEq = eqMap.containsKey(thisTime) ? eqMap.get(thisTime) : 0;
        chainDTO.setLastWeekEq(new BigDecimal(lastEq).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());

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
    private void getMonthChain(int id, AisleEleChainDTO chainDTO) throws IOException {
        String startTime = "";
        String endTime = "";
        //月环比
        //本月
        startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
        endTime = DateUtil.formatDateTime(DateTime.now());

        double thisMonthEq = getDayEq(startTime, endTime, id);

        chainDTO.setThisMonthEq(new BigDecimal(thisMonthEq).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        //上月
        //上月第一天
        Calendar lastMonthFirstDateCal = Calendar.getInstance();
        lastMonthFirstDateCal.add(Calendar.MONTH, -1);
        lastMonthFirstDateCal.set(Calendar.DAY_OF_MONTH, 1);

        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(lastMonthFirstDateCal.getTime()));


        List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_MONTH);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalMonthDo monthDo = JsonUtils.parseObject(str, AisleEqTotalMonthDo.class);
            String dateTime = monthDo.getCreateTime().toString(HOUR_FORMAT);
//            eqMap.put(dateTime, monthDo.getEqValue());
            if (Objects.equals(monthDo.getEqValue(), 0.0)) {
                eqMap.put(dateTime, monthDo.getEndEle());
            } else {
                eqMap.put(dateTime, monthDo.getEqValue());
            }
        });
        //上上月
        String lastMonthTime = DateUtil.formatDate(DateUtil.parse(startTime, HOUR_FORMAT));
        double eveEq = eqMap.containsKey(lastMonthTime) ? eqMap.get(lastMonthTime) : 0;
        //上月
        String thisMonthTime = DateUtil.formatDate(DateUtil.beginOfMonth(DateTime.now()));
        double lastMonthEq = eqMap.containsKey(thisMonthTime) ? eqMap.get(thisMonthTime) : 0;
        chainDTO.setLastMonthEq(new BigDecimal(lastMonthEq).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());

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
        AisleEleTotalRealtimeDo endRealtimeDo = getEleData(startTime, endTime,
                SortOrder.DESC,
                AISLE_ELE_TOTAL_REALTIME, id);
        AisleEleTotalRealtimeDo startRealtimeDo = getEleData(startTime, endTime,
                SortOrder.ASC,
                AISLE_ELE_TOTAL_REALTIME, id);
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
    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termsQuery("aisle_id", ids))));
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
        } catch (Exception e) {
            log.error("柜列查询es数据失败" + e);
        }
        return null;
    }

    /**
     * 获取数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ids       机柜id列表
     * @param index     索引表
     */
    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index, String[] heads) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.fetchSource(heads, null);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termsQuery("aisle_id", ids))));
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
        } catch (Exception e) {
            log.error("柜列查询es数据失败" + e);
        }
        return null;
    }

    /**
     * 获取数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param id        机柜id列表
     * @param index     索引表
     */
    private List<String> getData(String startTime, String endTime, Integer id, String index, String[] heads) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.fetchSource(heads, null);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termQuery("aisle_id", id))));
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
        } catch (Exception e) {
            log.error("柜列查询es数据失败" + e);
        }
        return null;
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
    private List<String> getData(String startTime, String endTime, AislePowVo powVo, String index) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                    .must(QueryBuilders.termQuery("aisle_id", powVo.getId())));
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
        } catch (Exception e) {
            log.error("柜列查询es数据失败" + e);
        }
        return null;
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
    private List<String> getData(String startTime, String endTime, int id, String index) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                    .must(QueryBuilders.termQuery(AISLE_ID, id))));
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
        } catch (Exception e) {
            log.error("获取es数据失败" + e);
        }
        return null;
    }

    private List getData(String startTime, String endTime, int id, String index, Class classz) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                    .must(QueryBuilders.termQuery(AISLE_ID, id))));
            builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
            // 设置搜索条件
            searchRequest.source(builder);
            builder.size(2000);

            List list = new ArrayList<>();
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse != null) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    String str = hit.getSourceAsString();
                    Object obj = JsonUtils.parseObject(str, classz);
                    if (Objects.nonNull(obj)) {
                        list.add(obj);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            log.error("获取es数据失败" + e);
        }
        return null;
    }

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index, String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery(AISLE_ID, ids))));
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
                .must(QueryBuilders.termsQuery(AISLE_ID, ids))));
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

    /**
     * @param startTime
     * @param endTime
     * @param sortOrder
     * @param index
     * @param id
     * @return
     */
    private AisleEleTotalRealtimeDo getEleData(String startTime, String endTime, SortOrder sortOrder, String index, int id) {
        AisleEleTotalRealtimeDo realtimeDo = new AisleEleTotalRealtimeDo();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD)
                            .gte(startTime)
                            .lt(endTime))
                    .must(QueryBuilders.termQuery(AISLE_ID, id))));

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
                realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), AisleEleTotalRealtimeDo.class);
            }
            return realtimeDo;
        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return realtimeDo;
    }

    private Map<Integer, MaxValueAndCreateTime> getAisleLinePowMaxData(String startTime, String endTime, List<Integer> ids, String index, String maxField, String maxFieldTime) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("aisle_id", ids))));

        builder.aggregation(
                AggregationBuilders.terms("group_by_aisle_id")
                        .field("aisle_id")
                        .size(10000)
                        .order(BucketOrder.aggregation("max", false))
                        .subAggregation(AggregationBuilders.max("max").field(maxField))
                        .subAggregation(AggregationBuilders.topHits("top_docs")
                                .size(1)
                                .fetchSource(new String[]{maxFieldTime}, null)
                                .sort(SortBuilders.fieldSort(maxField).order(SortOrder.DESC)))
        );


        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(0);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 初始化结果Map
        Map<Integer, MaxValueAndCreateTime> resultMap = new HashMap<>();
        // 获取group_by_aisle_id聚合结果
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_aisle_id");
        for (Terms.Bucket boxIdBucket : groupByBoxId.getBuckets()) {
            Integer boxId = boxIdBucket.getKeyAsNumber().intValue();
            MaxValueAndCreateTime maxValueAndCreateTime = new MaxValueAndCreateTime();
            ParsedMax max = (ParsedMax) boxIdBucket.getAggregations().get("max");
            maxValueAndCreateTime.setMaxValue(max.getValue());
            ParsedTopHits topHits = (ParsedTopHits) boxIdBucket.getAggregations().get("top_docs");
            if (topHits.getHits().getHits().length != 0) {
                SearchHit topHit = topHits.getHits().getHits()[0]; // 取第一个top hit
                Map<String, Object> sourceAsMap = topHit.getSourceAsMap();
                maxValueAndCreateTime.setMaxTime(new DateTime(sourceAsMap.get(maxFieldTime).toString(), "yyyy-MM-dd HH:mm:ss"));
            }

            // 将lineIdMap添加到resultMap中
            resultMap.put(boxId, maxValueAndCreateTime);
        }
        return resultMap;
    }

    private Map<Integer, Map<String, String>> getDevKey(List<Integer> aisleIds) {
        Map<Integer, Map<String, String>> map = new HashMap<>();
        if (CollectionUtils.isAnyEmpty(aisleIds)) {
            return map;
        }
        List<AisleBar> aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapperX<AisleBar>().inIfPresent(AisleBar::getAisleId, aisleIds));
        map = aisleBars.stream()
                .collect(Collectors.groupingBy(
                        AisleBar::getAisleId,
                        Collectors.toMap(AisleBar::getPath, AisleBar::getBusKey)
                ));
        return map;
//        res.forEach(aisleIndexRespVO -> {
//            if (aisleBarMap.get(aisleIndexRespVO.getId()) == null) {
//                return;
//            }
//            aisleIndexRespVO.setDevKeyA(aisleBarMap.get(aisleIndexRespVO.getId()).get("A"));
//            aisleIndexRespVO.setDevKeyB(aisleBarMap.get(aisleIndexRespVO.getId()).get("B"));
//        });
    }

    private Map getESTotalAndIds(String index, String startTime, String endTime, Integer pageSize, Integer pageNo) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder
                .from(pageNo * pageSize)
                .size(pageSize)
                .query(QueryBuilders.rangeQuery("create_time.keyword").gte(startTime).lte(endTime))
                .sort("aisle_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("aisle_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("aisle_id").precisionThreshold(10000));

        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                AislePowHourDo hourDo = JsonUtils.parseObject(str, AislePowHourDo.class);
                sortValues.add(hourDo.getAisleId());
            }
        }
        Long totalRes = 0L;
        Cardinality totalSizeAggregation = searchResponse.getAggregations().get("total_size");
        if (totalSizeAggregation != null) {
            totalRes = totalSizeAggregation.getValue();
        }

        result.put("total", totalRes);
        result.put("ids", sortValues);
        return result;
    }

    private Map getESTotalAndIds(String index, String startTime, String endTime, Integer pageSize, Integer pageNo, List<Integer> ids) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder
                .from(pageNo * pageSize)
                .size(pageSize)
                .query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                        .must(QueryBuilders.termsQuery("aisle_id", ids))))
                .sort("aisle_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("aisle_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("aisle_id").precisionThreshold(10000));

        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                AislePowHourDo hourDo = JsonUtils.parseObject(str, AislePowHourDo.class);
                sortValues.add(hourDo.getAisleId());
            }
        }
        Long totalRes = 0L;
        Cardinality totalSizeAggregation = searchResponse.getAggregations().get("total_size");
        if (totalSizeAggregation != null) {
            totalRes = totalSizeAggregation.getValue();
        }

        result.put("total", totalRes);
        result.put("ids", sortValues);
        return result;
    }

    private String localDateTimeToString(LocalDateTime time) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
    }


}