package cn.iocoder.yudao.module.aisle.service.aisleindex;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.ele.AisleEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.aisle.pow.AislePowHourDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.mapper.AisleBarMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.util.TimeUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.aisle.constant.AisleConstants;
import cn.iocoder.yudao.module.aisle.dal.mysql.aisleindex.AisleIndexCopyMapper;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.ParsedTopHits;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo.*;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.AISLE_ID;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.SPLIT_KEY_BUS;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.*;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.AISLE_ELE_TOTAL_REALTIME;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.AISLE_EQ_TOTAL_DAY;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.AISLE_EQ_TOTAL_MONTH;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.AISLE_EQ_TOTAL_WEEK;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.KEYWORD;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.REDIS_KEY_BUS;
import static cn.iocoder.yudao.module.aisle.constant.AisleConstants.SPLIT_KEY;
import static cn.iocoder.yudao.module.aisle.enums.ErrorCodeConstants.*;

/**
 * 通道列 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
@Slf4j
public class AisleIndexServiceImpl implements AisleIndexService {

    @Resource
    private AisleIndexCopyMapper aisleIndexCopyMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private AisleBarMapper aisleBarMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient client;

    public static final String DAY_FORMAT = "dd";
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
    public void deleteIndex(Integer id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        aisleIndexCopyMapper.deleteById(id);
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
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
        List<AisleIndexDO> list = aisleIndexDOPageResult.getList();
        List<Integer> aisleIds = list.stream().map(AisleIndexDO::getId).collect(Collectors.toList());
        List<AisleBar> aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapperX<AisleBar>().inIfPresent(AisleBar::getAisleId, aisleIds));
        List<String> devKey = aisleBars.stream().map(AisleBar::getBarKey).collect(Collectors.toList());
        Map<String, Map<Integer, String>> aisleBarMap = aisleBars.stream()
                .collect(Collectors.groupingBy(
                        AisleBar::getBarKey,
                        Collectors.toMap(AisleBar::getAisleId, AisleBar::getPath)
                ));
        List<AisleIndexRes> res = new ArrayList<>();
        List redisList = getMutiBusRedis(devKey);
        for (AisleIndexDO aisleIndexDO : list) {
            AisleIndexRes aisleIndexRes = new AisleIndexRes();
            aisleIndexRes.setId(aisleIndexDO.getId());
            aisleIndexRes.setName(aisleIndexDO.getName());
            aisleIndexRes.setRoomId(aisleIndexDO.getRoomId());
            res.add(aisleIndexRes);
        }
        Map<Integer, AisleIndexRes> resMap = res.stream().collect(Collectors.toMap(AisleIndexRes::getId, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String busDevKey = jsonObject.getString("dev_ip") + SPLIT_KEY_BUS + jsonObject.getString("bus_name");
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray loadRate = lineItemList.getJSONArray("load_rate");
            List<Double> rateList = loadRate.toList(Double.class);
            for (Integer i : aisleBarMap.get(busDevKey).keySet()) {
                AisleIndexRes aisleIndexRes = resMap.get(i);
                String path = aisleBarMap.get(busDevKey).get(i);
                LoadRate rate = new LoadRate();
                if(rateList.size() > 1) {
                    rate.setALoadRate(loadRate.getDouble(0));
                    rate.setBLoadRate(loadRate.getDouble(1));
                    rate.setCLoadRate(loadRate.getDouble(2));
                } else{
                    rate.setALoadRate(loadRate.getDouble(0));
                }
                rateList.sort(Collections.reverseOrder());
                Double biggest = rateList.get(0) * 100;
                Integer color = 0;
                if (biggest == 0){
                    color= 0;
                } else if (biggest < 30){
                    color = 1;
                } else if (biggest < 60){
                    color = 2;
                } else if (biggest < 90){
                    color = 3;
                } else if (biggest >= 90){
                    color = 4;
                }
                if (path.equals("A")){
                    if(rateList.size() > 1) {
                        aisleIndexRes.setALoadRateA(loadRate.getDouble(0));
                        aisleIndexRes.setBLoadRateA(loadRate.getDouble(1));
                        aisleIndexRes.setCLoadRateA(loadRate.getDouble(2));
                    }else {
                        aisleIndexRes.setALoadRateA(loadRate.getDouble(0));
                    }
                    aisleIndexRes.setColorA(color);
                    aisleIndexRes.setDevKeyA(busDevKey);
                }else{
                    if(rateList.size() > 1) {
                        aisleIndexRes.setALoadRateB(loadRate.getDouble(0));
                        aisleIndexRes.setBLoadRateB(loadRate.getDouble(1));
                        aisleIndexRes.setCLoadRateB(loadRate.getDouble(2));
                    }else{
                        aisleIndexRes.setALoadRateB(loadRate.getDouble(0));
                    }
                    aisleIndexRes.setColorB(color);
                    aisleIndexRes.setDevKeyB(busDevKey);
                }
            }
        }

        return new PageResult<>(res,aisleIndexDOPageResult.getTotal());
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
        List<AisleIndexDO> list = aisleIndexDOPageResult.getList();
        List<AislePowerRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (AisleIndexDO aisleIndexDO : list) {
            AislePowerRes aislePowerRes = new AislePowerRes();
            aislePowerRes.setId(aisleIndexDO.getId());
            aislePowerRes.setName(aisleIndexDO.getName());
            aislePowerRes.setRoomId(aisleIndexDO.getRoomId());
            res.add(aislePowerRes);
        }
        Map<Integer, AislePowerRes> resMap = res.stream().collect(Collectors.toMap(AislePowerRes::getId, Function.identity()));
        getPosition(res);
        getDevKey(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            Integer aisleKey = jsonObject.getInteger("aisle_key") ;
            AislePowerRes aislePowerRes = resMap.get(aisleKey);
            JSONObject totalData = jsonObject.getJSONObject("aisle_power").getJSONObject("total_data");
            JSONObject pathA = jsonObject.getJSONObject("aisle_power").getJSONObject("path_a");
            JSONObject pathB = jsonObject.getJSONObject("aisle_power").getJSONObject("path_b");
            if (totalData != null){
                aislePowerRes.setEleActiveTotal(totalData.getDouble("ele_active"));
                aislePowerRes.setPowApparentTotal(totalData.getDouble("pow_apparent"));
                aislePowerRes.setPowActiveTotal(totalData.getDouble("pow_active"));
                aislePowerRes.setPowReactiveTotal(totalData.getDouble("pow_reactive"));
            }
            if (pathA != null){
                aislePowerRes.setEleActiveA(pathA.getDouble("ele_active"));
                aislePowerRes.setPowApparentA(pathA.getDouble("pow_apparent"));
                aislePowerRes.setPowActiveA(pathA.getDouble("pow_active"));
                aislePowerRes.setPowReactiveA(pathA.getDouble("pow_reactive"));
            }
            if (pathB != null){
                aislePowerRes.setEleActiveB(pathB.getDouble("ele_active"));
                aislePowerRes.setPowApparentB(pathB.getDouble("pow_apparent"));
                aislePowerRes.setPowActiveB(pathB.getDouble("pow_active"));
                aislePowerRes.setPowReactiveB(pathB.getDouble("pow_reactive"));
            }

        }
        return new PageResult<>(res,aisleIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<AisleEQRes> getEqPage(AisleIndexPageReqVO pageReqVO) {
        try {
            PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
            List<AisleIndexDO> aisleIndexDOList = aisleIndexDOPageResult.getList();
            List<AisleEQRes> result = new ArrayList<>();
            List<Integer> ids = aisleIndexDOList.stream().map(AisleIndexDO::getId).collect(Collectors.toList());
            if (org.springframework.util.CollectionUtils.isEmpty(ids)){
                return new PageResult<>(result, aisleIndexDOPageResult.getTotal());
            }
            //昨日
            aisleIndexDOList.forEach(aisleIndexDO -> {
                AisleEQRes res = new AisleEQRes();
                res.setId(aisleIndexDO.getId());
                res.setName(aisleIndexDO.getName());
                res.setRoomId(aisleIndexDO.getRoomId());
                result.add(res);
            });
            String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            String endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  yesterdayList = getData(startTime,endTime, ids,"aisle_eq_total_day");
            Map<Integer,Double> yesterdayMap = new HashMap<>();
            if (!org.springframework.util.CollectionUtils.isEmpty(yesterdayList)){
                yesterdayList.forEach(str -> {
                    AisleEqTotalDayDo dayDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
                    yesterdayMap.put(dayDo.getAisleId(),dayDo.getEqValue());
                });
            }

            //上周
            startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
            endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  weekList = getData(startTime,endTime, ids,"aisle_eq_total_week");
            Map<Integer,Double> weekMap = new HashMap<>();
            if (!org.springframework.util.CollectionUtils.isEmpty(weekList)){
                weekList.forEach(str -> {
                    AisleEqTotalWeekDo weekDo = JsonUtils.parseObject(str, AisleEqTotalWeekDo.class);
                    weekMap.put(weekDo.getAisleId(),weekDo.getEqValue());
                });
            }

            //上月
            startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
            endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  monthList = getData(startTime,endTime, ids,"aisle_eq_total_month");
            Map<Integer,Double> monthMap = new HashMap<>();
            if (!org.springframework.util.CollectionUtils.isEmpty(monthList)){
                monthList.forEach(str -> {
                    AisleEqTotalMonthDo monthDo = JsonUtils.parseObject(str, AisleEqTotalMonthDo.class);
                    monthMap.put(monthDo.getAisleId(),monthDo.getEqValue());
                });
            }

            result.forEach(dto -> {
                dto.setYesterdayEq(yesterdayMap.get(dto.getId()));
                dto.setLastWeekEq(weekMap.get(dto.getId()));
                dto.setLastMonthEq(monthMap.get(dto.getId()));
            });
            getPosition(result);
            return new PageResult<>(result, aisleIndexDOPageResult.getTotal());
        }catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public PageResult<AislePfRes> getAislePFPage(AisleIndexPageReqVO pageReqVO) {
        PageResult<AisleIndexDO> aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);
        List<AisleIndexDO> list = aisleIndexDOPageResult.getList();
        List<Integer> aisleIds = list.stream().map(AisleIndexDO::getId).collect(Collectors.toList());
        List<AisleBar> aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapperX<AisleBar>().inIfPresent(AisleBar::getAisleId, aisleIds));
        List<String> devKey = aisleBars.stream().map(AisleBar::getBarKey).collect(Collectors.toList());
        Map<String, Map<Integer, String>> aisleBarMap = aisleBars.stream()
                .collect(Collectors.groupingBy(
                        AisleBar::getBarKey,
                        Collectors.toMap(AisleBar::getAisleId, AisleBar::getPath)
                ));
        List<AislePfRes> res = new ArrayList<>();
        List redisList = getMutiBusRedis(devKey);
        for (AisleIndexDO aisleIndexDO : list) {
            AislePfRes aislePfRes = new AislePfRes();
            aislePfRes.setId(aisleIndexDO.getId());
            aislePfRes.setName(aisleIndexDO.getName());
            aislePfRes.setRoomId(aisleIndexDO.getRoomId());
            res.add(aislePfRes);
        }
        Map<Integer, AislePfRes> resMap = res.stream().collect(Collectors.toMap(AislePfRes::getId, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String busDevKey = jsonObject.getString("dev_ip") + SPLIT_KEY_BUS + jsonObject.getString("bus_name");
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray pfValue = lineItemList.getJSONArray("power_factor");

            for (Integer i : aisleBarMap.get(busDevKey).keySet()) {
                AislePfRes aislePfRes = resMap.get(i);
                String path = aisleBarMap.get(busDevKey).get(i);
                List<Double> pfList = new ArrayList<>();
                for (int j = 0; j < 3; j++) {
                    double pf = pfValue.getDoubleValue(i);
                    pfList.add(pf);
                }
                if (path.equals("A")){
                    aislePfRes.setApfA(pfList.get(0));
                    aislePfRes.setBpfA(pfList.get(1));
                    aislePfRes.setCpfA(pfList.get(2));
                    aislePfRes.setTotalPfA(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDoubleValue("power_factor"));
                    aislePfRes.setDevKeyA(busDevKey);
                }else{
                    aislePfRes.setApfB(pfList.get(0));
                    aislePfRes.setBpfB(pfList.get(1));
                    aislePfRes.setCpfB(pfList.get(2));
                    aislePfRes.setTotalPfB(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDoubleValue("power_factor"));
                    aislePfRes.setDevKeyB(busDevKey);
                }
            }
        }

        return new PageResult<>(res,aisleIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<AisleLineMaxRes> getAisleLineMaxPage(AisleIndexPageReqVO pageReqVO) {
        try {
            PageResult<AisleIndexDO> aisleIndexDOPageResult = null;
            List<AisleLineMaxRes> result = new ArrayList<>();
            
            aisleIndexDOPageResult = aisleIndexCopyMapper.selectPage(pageReqVO);

            List<AisleIndexDO> aisleIndexDOList = aisleIndexDOPageResult.getList();

            if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            }
            List<Integer> ids = aisleIndexDOList.stream().map(AisleIndexDO::getId).collect(Collectors.toList());

            Map<Integer,MaxValueAndCreateTime> powTotalMap ;
            Map<Integer,MaxValueAndCreateTime> powAMap ;
            Map<Integer,MaxValueAndCreateTime> powBMap ;
            String index = null;
            if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "aisle_hda_pow_hour";
            } else {
                index = "aisle_hda_pow_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());

            powTotalMap = getAisleLinePowMaxData(startTime,endTime,ids,index,"active_total_max_value","active_total_max_time");
            powAMap = getAisleLinePowMaxData(startTime,endTime,ids,index,"active_a_max_value","active_a_max_time");
            powBMap = getAisleLinePowMaxData(startTime,endTime,ids,index,"active_b_max_value","active_b_max_time");

            for (AisleIndexDO aisleIndexDO : aisleIndexDOList) {
                Integer id = aisleIndexDO.getId().intValue();
                if (powTotalMap.get(id) == null){
                    continue;
                }

                AisleLineMaxRes aisleLineMaxRes = new AisleLineMaxRes();

                aisleLineMaxRes.setId(aisleIndexDO.getId());
                aisleLineMaxRes.setName(aisleIndexDO.getName());
                aisleLineMaxRes.setRoomId(aisleIndexDO.getRoomId());

                MaxValueAndCreateTime powTotal = powTotalMap.get(id);
                aisleLineMaxRes.setMaxPowTotal(powTotal.getMaxValue().floatValue());
                aisleLineMaxRes.setMaxPowTotalTime(powTotal.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                MaxValueAndCreateTime powA = powAMap.get(id);
                if(powA != null) {
                    aisleLineMaxRes.setMaxPowA(powA.getMaxValue().floatValue());
                    aisleLineMaxRes.setMaxPowATime(powA.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                MaxValueAndCreateTime powB = powBMap.get(id);
                if(powB != null) {
                    aisleLineMaxRes.setMaxPowB(powB.getMaxValue().floatValue());
                    aisleLineMaxRes.setMaxPowBTime(powB.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                result.add(aisleLineMaxRes);
            }
            if(!org.springframework.util.CollectionUtils.isEmpty(result)){
                getPosition(result);
            }

            return new PageResult<AisleLineMaxRes>(result,aisleIndexDOPageResult.getTotal());
        }catch (Exception e){
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

            log.info("startTime : " + startTime + "endTime：" + endTime);
            //获取昨日数据
            List<String> yesterdayData = getData(startTime, endTime, vo, "aisle_hda_pow_hour");


            List<AisleActivePowTrendDTO> yesterdayList = new ArrayList<>();
            yesterdayData.forEach(str -> {
                AislePowHourDo hourDo = JsonUtils.parseObject(str, AislePowHourDo.class);
                AisleActivePowTrendDTO dto = new AisleActivePowTrendDTO();
                dto.setActivePow(hourDo.getActiveTotalAvgValue());
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH") + TIME_STR;
                dto.setDateTime(dateTime);
//                log.info("dateTime : " + dateTime );
                yesterdayList.add(dto);
            });


            startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());

            log.info("startTime : " + startTime + "endTime：" + endTime);
            //获取今日数据
            List<AisleActivePowTrendDTO> todayList = new ArrayList<>();

            List<String> todayData = getData(startTime, endTime, vo,  "aisle_hda_pow_hour");
            todayData.forEach(str -> {
                AislePowHourDo hourDo = JsonUtils.parseObject(str, AislePowHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH") + TIME_STR;
                AisleActivePowTrendDTO dto = new AisleActivePowTrendDTO();
                if (Objects.isNull(dto)) {
                    dto = new AisleActivePowTrendDTO();
                }
                dto.setActivePow(hourDo.getActiveTotalAvgValue());
                dto.setDateTime(dateTime);
//                log.info("dateTime : " + dateTime );
                todayList.add(dto);
            });

            powDTO.setYesterdayList(yesterdayList);
            powDTO.setTodayList(todayList);
            //获取峰值
            AisleActivePowTrendDTO yesterdayMax = yesterdayList.stream().max(Comparator.comparing(AisleActivePowTrendDTO::getActivePow)).orElse(new AisleActivePowTrendDTO());
            AisleActivePowTrendDTO todayMax = todayList.stream().max(Comparator.comparing(AisleActivePowTrendDTO::getActivePow)).orElse(new AisleActivePowTrendDTO());
            powDTO.setTodayMax(todayMax.getActivePow());
            powDTO.setTodayMaxTime(todayMax.getDateTime());
            powDTO.setYesterdayMaxTime(yesterdayMax.getDateTime());
            powDTO.setYesterdayMax(yesterdayMax.getActivePow());

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
            if (pageReqVO.getTimeType() == 0){
                index = AISLE_HDA_POW_HOUR;
                startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
                endTime = localDateTimeToString(LocalDateTime.now());
            }else {
                index = AISLE_HDA_POW_DAY;
            }
            List<Integer> ids = Arrays.asList(pageReqVO.getId());
            List<String> data = getData(startTime,endTime,ids,index);

            result.getSeries().add(new RequirementLineSeries().setName("总共最大功率"));
            result.getSeries().add(new RequirementLineSeries().setName("A路最大功率"));
            result.getSeries().add(new RequirementLineSeries().setName("B路最大功率"));
            List<AislePowHourDo> aislePowHourList = data.stream().map(str -> JsonUtils.parseObject(str, AislePowHourDo.class)).collect(Collectors.toList());

            aislePowHourList.forEach(hour ->{
                result.getTime().add(hour.getActiveTotalMaxTime().toString("yyyy-MM-dd HH"));
                result.getSeries().get(0).getData().add(hour.getActiveTotalMaxValue());
                ((RequirementLineSeries)result.getSeries().get(0)).getMaxTime().add(hour.getActiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.getSeries().get(1).getData().add(hour.getActiveAMaxValue());
                ((RequirementLineSeries)result.getSeries().get(1)).getMaxTime().add(hour.getActiveAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.getSeries().get(2).getData().add(hour.getActiveBMaxValue());
                ((RequirementLineSeries)result.getSeries().get(2)).getMaxTime().add(hour.getActiveBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
            });

            return result;
        }catch (Exception e){
            log.error("获取数据失败",e);
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
            aisleBalanceRes.setName(aisleIndexDO.getName());
            result.add(aisleBalanceRes);
        });
        getPosition(result);
        Map<Integer, AisleBalanceRes> resMap = result.stream().collect(Collectors.toMap(AisleBalanceRes::getId, Function.identity()));
        for (Object o : mutiRedis) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            Integer aisleKey = jsonObject.getInteger("aisle_key") ;
            AisleBalanceRes aisleBalanceRes = resMap.get(aisleKey);
            JSONObject aislePower = jsonObject.getJSONObject("aisle_power");
            JSONObject totalData = aislePower.getJSONObject("total_data");
            if (Objects.nonNull(totalData)){
                aisleBalanceRes.setPowApparentTotal(totalData.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveTotal(totalData.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveTotal(totalData.getDouble("pow_reactive"));
            }

            JSONObject pathA = aislePower.getJSONObject("path_a");
            if (Objects.nonNull(pathA)){
                aisleBalanceRes.setPowApparentA(pathA.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveA(pathA.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveA(pathA.getDouble("pow_reactive"));
            }


            JSONObject pathB = aislePower.getJSONObject("path_b");
            if (Objects.nonNull(pathB)){
                aisleBalanceRes.setPowApparentB(pathB.getDouble("pow_apparent"));
                aisleBalanceRes.setPowActiveB(pathB.getDouble("pow_active"));
                aisleBalanceRes.setPowReactiveB(pathB.getDouble("pow_reactive"));
            }

            if(aisleBalanceRes.getPowApparentA() != null && aisleBalanceRes.getPowApparentA() != 0 && aisleBalanceRes.getPowApparentTotal() != null && aisleBalanceRes.getPowApparentTotal() != 0){
                aisleBalanceRes.setRateA((aisleBalanceRes.getPowApparentA() / aisleBalanceRes.getPowApparentTotal())*100);
            }
        }
        return new PageResult<>(result,aisleIndexDOPageResult.getTotal());
    }

    @Override
    public Map getReportConsumeDataById(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        AisleLineResBase barRes = new AisleLineResBase();
        BarSeries barSeries = new BarSeries();
        try {
            if(id != null){
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
                if (isSameDay){
                    for (String str : cabinetData) {
                        nowTimes++;
                        AisleEleTotalRealtimeDo aisleEleTotalRealtimeDo = JsonUtils.parseObject(str, AisleEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = aisleEleTotalRealtimeDo.getEleTotal();
                        }
                        if (nowTimes > 1){
                            barSeries.getData().add((float)(aisleEleTotalRealtimeDo.getEleTotal() -lastEq));
                            barRes.getTime().add(aisleEleTotalRealtimeDo.getCreateTime().toString("HH:mm:ss"));
                        }
                        lastEq = aisleEleTotalRealtimeDo.getEleTotal();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "ele_total");
                    AisleEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, AisleEleTotalRealtimeDo.class);
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
                        AisleEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, AisleEqTotalDayDo.class);
                        totalEq += totalDayDo.getEqValue();
                        barSeries.getData().add((float)totalDayDo.getEqValue());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "eq_value");
                    AisleEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, AisleEqTotalDayDo.class);
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
        AisleLineResBase totalLineRes = new AisleLineResBase();
        AisleLineResBase aLineRes = new AisleLineResBase();
        AisleLineResBase bLineRes = new AisleLineResBase();

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
                    aislePowHourDos.forEach(hourdo -> {
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
                    aislePowHourDos.forEach(hourdo -> {
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
    public Map getAislePFLine(Integer id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        AisleLineResBase totalLineRes = new AisleLineResBase();
        result.put("pfLineRes",totalLineRes);
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

    private List getMutiRedis(List<AisleIndexDO> list){
        List<String> devKeys = list.stream().map(busIndexDo -> REDIS_KEY_AISLE + busIndexDo.getId()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    private List getMutiBusRedis(List<String> devKey){
        List<String> devKeys = devKey.stream().map(string -> REDIS_KEY_BUS + string).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    private void getPosition(List<? extends AisleIndexRespVO> res){
        if (CollectionUtils.isAnyEmpty(res)){
            return;
        }
        List<Integer> roomIds = res.stream().map(AisleIndexRespVO::getRoomId).collect(Collectors.toList());
        Map<Integer, String> roomMap = roomIndexMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getName));
        res.forEach(aisleIndexRespVO -> {
            aisleIndexRespVO.setLocation(roomMap.get(aisleIndexRespVO.getRoomId()) + SPLIT_KEY +aisleIndexRespVO.getName());
        });
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
        chainDTO.setTodayEq(todayEq);
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
    private void getWeekChain(int id, AisleEleChainDTO chainDTO) throws IOException {
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

        List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_WEEK);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalWeekDo weekDo = JsonUtils.parseObject(str, AisleEqTotalWeekDo.class);
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
    private void getMonthChain(int id, AisleEleChainDTO chainDTO) throws IOException {
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


        List<String> list = getData(startTime, endTime, id, AISLE_EQ_TOTAL_MONTH);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            AisleEqTotalMonthDo monthDo = JsonUtils.parseObject(str, AisleEqTotalMonthDo.class);
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
    private List<String> getData(String startTime, String endTime, AislePowVo powVo, String index) throws IOException {
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

    }

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index,String order) throws IOException {
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

    private String getMinData(String startTime, String endTime, List<Integer> ids, String index,String order) throws IOException {
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
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + AisleConstants.KEYWORD)
                            .gte(startTime)
                            .lt(endTime))
                    .must(QueryBuilders.termQuery(AISLE_ID, id))));

            // 嵌套聚合
            // 设置聚合查询
            String top = "top";
            AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                    .size(1).sort(CREATE_TIME + AisleConstants.KEYWORD, sortOrder);

            builder.aggregation(topAgg);

            // 设置搜索条件
            searchRequest.source(builder);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();

            TopHits tophits = aggregations.get(top);
            SearchHits sophistsHits = tophits.getHits();
            if (null != sophistsHits.getHits() && sophistsHits.getHits().length>0){
                SearchHit hit = sophistsHits.getHits()[0];
                realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), AisleEleTotalRealtimeDo.class);
            }
            return realtimeDo;
        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return realtimeDo;
    }
    
    private  Map<Integer, MaxValueAndCreateTime> getAisleLinePowMaxData(String startTime, String endTime, List<Integer> ids, String index,String maxField,String maxFieldTime) throws IOException {
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
        // 获取group_by_box_id聚合结果
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_aisle_id");
        for (Terms.Bucket boxIdBucket : groupByBoxId.getBuckets()) {
            Integer boxId = boxIdBucket.getKeyAsNumber().intValue();
            MaxValueAndCreateTime maxValueAndCreateTime = new MaxValueAndCreateTime();
            ParsedMax max = (ParsedMax)boxIdBucket.getAggregations().get("max");
            maxValueAndCreateTime.setMaxValue(max.getValue());
            ParsedTopHits topHits = (ParsedTopHits) boxIdBucket.getAggregations().get("top_docs");
            if (topHits.getHits().getHits().length != 0) {
                SearchHit topHit = topHits.getHits().getHits()[0]; // 取第一个top hit
                Map<String, Object> sourceAsMap = topHit.getSourceAsMap();
                maxValueAndCreateTime.setMaxTime(new DateTime(sourceAsMap.get(maxFieldTime).toString(),"yyyy-MM-dd HH:mm:ss"));
            }

            // 将lineIdMap添加到resultMap中
            resultMap.put(boxId, maxValueAndCreateTime);
        }
        return resultMap;
    }

    private void getDevKey(List<? extends AisleIndexRespVO> res){
        if (CollectionUtils.isAnyEmpty(res)){
            return;
        }
        List<Integer> aisleIds = res.stream().map(AisleIndexRespVO::getId).collect(Collectors.toList());
        List<AisleBar> aisleBars = aisleBarMapper.selectList(new LambdaQueryWrapperX<AisleBar>().inIfPresent(AisleBar::getAisleId, aisleIds));
        Map<Integer, Map<String, String>> aisleBarMap = aisleBars.stream()
                .collect(Collectors.groupingBy(
                        AisleBar::getAisleId,
                        Collectors.toMap(AisleBar::getPath, AisleBar::getBarKey)
                ));
        res.forEach(aisleIndexRespVO -> {
            if(aisleBarMap.get(aisleIndexRespVO.getId()) == null){
                return;
            }
            aisleIndexRespVO.setDevKeyA(aisleBarMap.get(aisleIndexRespVO.getId()).get("A"));
            aisleIndexRespVO.setDevKeyB(aisleBarMap.get(aisleIndexRespVO.getId()).get("B"));
        });
    }

    private String localDateTimeToString(LocalDateTime time){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
    }
    
    
}