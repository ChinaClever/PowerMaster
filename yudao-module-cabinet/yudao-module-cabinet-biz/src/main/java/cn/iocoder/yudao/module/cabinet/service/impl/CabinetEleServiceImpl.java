package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineHourDo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduIndexDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.cabinet.constant.CabConstants;
import cn.iocoder.yudao.module.cabinet.dto.CabinetEleChainDTO;
import cn.iocoder.yudao.module.cabinet.dto.CabinetEqTrendDTO;
import cn.iocoder.yudao.module.cabinet.dto.CabinetPduCurTrendDTO;
import cn.iocoder.yudao.module.cabinet.mapper.CabPduIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetPduMapper;
import cn.iocoder.yudao.module.cabinet.service.CabinetEleService;
import cn.iocoder.yudao.framework.common.util.TimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CABINET_EQ_TOTAL_DAY;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CABINET_ID;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.KEYWORD;
import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.*;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜能耗数据
 * @date 2024/5/6 17:07
 */
@EnableAsync
@Slf4j
@Service
public class CabinetEleServiceImpl implements CabinetEleService {

    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private CabinetPduMapper cabinetPduMapper;
    @Autowired
    private CabPduIndexMapper pduIndexMapper;

    public static final String HOUR_FORMAT = "yyyy-MM-dd";

    public static final String HOUR = "HH";

    public static final String DAY_FORMAT = "dd";

    @Override
    public CabinetEleChainDTO getEleChain(int id) {
        CabinetEleChainDTO chainDTO = new CabinetEleChainDTO();
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
    public List<CabinetEqTrendDTO> eqTrend(int id, String type) {
        List<CabinetEqTrendDTO> list = new ArrayList<>();
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
    public List<CabinetPduCurTrendDTO> curTrend(int id) {

        List<CabinetPduCurTrendDTO> list = new ArrayList<>();

        try{
            DateTime end = DateTime.now();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            DateTime start = DateTime.of(calendar.getTime());

            String startTime = DateUtil.formatDateTime(start);
            String endTime = DateUtil.formatDateTime(end);
            //获取pdu
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapper<CabinetPdu>()
                    .eq(CabinetPdu::getCabinetId,id));
            List<String> keys = new ArrayList<>();


            String aKey;
            String bKey;

            if (Objects.nonNull(cabinetPdu)){
                String aPdu = cabinetPdu.getPduIpA();
                String bPdu = cabinetPdu.getPduIpB();
                if (StringUtils.isNotEmpty(aPdu)){
                    aKey = aPdu + SPLIT + cabinetPdu.getCasIdA();
                    keys.add(aKey);
                } else {
                    aKey = "";
                }
                if (StringUtils.isNotEmpty(bPdu)){
                    bKey = bPdu  + SPLIT + cabinetPdu.getCasIdB();
                    keys.add(bKey);
                } else {
                    bKey = "";
                }
            } else {
                bKey = "";
                aKey = "";
            }

            List<Integer> pduIds = new ArrayList<>();
            Map<Integer,String> keyMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(keys)){
                List<PduIndexDo> pduIndexDos = pduIndexMapper.selectList(new LambdaQueryWrapper<PduIndexDo>()
                        .in(PduIndexDo::getDevKey,keys));
                if (!CollectionUtils.isEmpty(pduIndexDos)){
                    pduIds.addAll(pduIndexDos.stream().map(PduIndexDo::getId).distinct().collect(Collectors.toList()));
                    pduIndexDos.forEach(pduIndexDo -> {
                        keyMap.put(pduIndexDo.getId(),pduIndexDo.getDevKey());
                    });
                }
            }
            if (!CollectionUtils.isEmpty(pduIds)){

                Map<String,List<PduHdaLineHourDo>> timePdus = new HashMap<>();

                List<String> data = getPduData(startTime, endTime, pduIds, PDU_HDA_LINE_HOUR);
                data.forEach(str -> {
                    PduHdaLineHourDo hourDo = JsonUtils.parseObject(str, PduHdaLineHourDo.class);

                    String dateTime  = DateUtil.format(hourDo.getCreateTime(), "yyyy-MM-dd HH") ;
                    List<PduHdaLineHourDo> lineHourDos = timePdus.get(dateTime);
                    if (CollectionUtils.isEmpty(lineHourDos)) {
                        lineHourDos = new ArrayList<>();
                    }
                    lineHourDos.add(hourDo);
                    timePdus.put(dateTime,lineHourDos);
                });

                timePdus.keySet().forEach(dateTime -> {
                    //获取每个时间段数据
                    List<PduHdaLineHourDo> pduHdaLineHourDos = timePdus.get(dateTime);
                    Map<Integer,List<PduHdaLineHourDo>>  pduLines = pduHdaLineHourDos.stream().collect(Collectors.groupingBy(PduHdaLineHourDo::getPduId));

                    CabinetPduCurTrendDTO trendDTO = new CabinetPduCurTrendDTO();
                    trendDTO.setDateTime(dateTime);
                    //获取相数据
                    pduLines.keySet().forEach(pduId -> {
                        trendDTO.setAKey(aKey);
                        trendDTO.setBKey(bKey);

                        //A路电流
                        List<Map<String,Object>> curA = new ArrayList<>();
                        if (aKey.equals(keyMap.get(pduId))){
                            List<PduHdaLineHourDo> pdus = pduLines.get(pduId);
                            pdus.forEach(hourDo -> {
                                Map<String,Object> curMap = new HashMap<>();
                                curMap.put("pduId",hourDo.getPduId());
                                curMap.put("lineId",hourDo.getLineId());
                                curMap.put("curValue",hourDo.getCurAvgValue());
                                curA.add(curMap);

                            });
                            trendDTO.setCurA(curA);
                        }
                        //B路电流
                        List<Map<String,Object>> curB = new ArrayList<>();
                        if (bKey.equals(keyMap.get(pduId))){
                            List<PduHdaLineHourDo> pdus = pduLines.get(pduId);
                            pdus.forEach(hourDo -> {
                                Map<String,Object> curMap = new HashMap<>();
                                curMap.put("pduId",hourDo.getPduId());
                                curMap.put("lineId",hourDo.getLineId());
                                curMap.put("curValue",hourDo.getCurAvgValue());
                                curB.add(curMap);
                            });
                            trendDTO.setCurB(curB);
                        }
                    });
                    list.add(trendDTO);
                });
            }
            return list.stream().sorted(Comparator.comparing(CabinetPduCurTrendDTO::getDateTime)).collect(Collectors.toList());
        }catch (Exception e){
            log.error("获取数据失败，",e);
        }

        return list;

    }

    /**
     * 日趋势
     *
     * @param id
     * @return
     */
    private List<CabinetEqTrendDTO> dayTrend(int id) throws IOException {
        List<CabinetEqTrendDTO> trendDTOList = new ArrayList<>();

        //今日
        String startTime = "";
        String endTime = DateUtil.formatDateTime(DateTime.now());

        //昨日
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));

        List<String> list = getData(startTime, endTime, id, CABINET_ELE_TOTAL_REALTIME);

        List<CabinetEleTotalRealtimeDo> yesterdayList = new ArrayList<>();
        List<CabinetEleTotalRealtimeDo> todayList = new ArrayList<>();
        String finalStartTime = startTime;
        list.forEach(str -> {
            CabinetEleTotalRealtimeDo realtimeDo = JsonUtils.parseObject(str, CabinetEleTotalRealtimeDo.class);
            String dateTime = DateUtil.formatDate(DateUtil.parse(realtimeDo.getCreateTime(), HOUR_FORMAT));
            //昨天
            if (finalStartTime.contains(dateTime)) {
                yesterdayList.add(realtimeDo);
            }
            //今日
            if (endTime.contains(dateTime)) {
                todayList.add(realtimeDo);
            }

        });
        Map<String, CabinetEqTrendDTO> map = new HashMap<>();
        //昨日数据处理
        for (int i = 0; i < yesterdayList.size() - 1; i++) {

            //前一个时间点
            CabinetEleTotalRealtimeDo reDo = yesterdayList.get(i);
            //当前时间点
            CabinetEleTotalRealtimeDo thisDo = yesterdayList.get(i + 1);

            String dateTime = DateUtil.format(DateUtil.parse(thisDo.getCreateTime(), NORM_DATETIME_PATTERN), HOUR) + ":00";
            log.info("reDo : " + reDo.getEleTotal() + "thisDo : " + thisDo.getEleTotal());
            //避免负数
            double eq = (thisDo.getEleTotal() - reDo.getEleTotal()) < 0 ? 0 : thisDo.getEleTotal() - reDo.getEleTotal();

            CabinetEqTrendDTO cabinetEqTrendDTO = new CabinetEqTrendDTO();
            cabinetEqTrendDTO.setLastEq(eq);
            cabinetEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, cabinetEqTrendDTO);

        }
        //今日数据处理
        for (int i = 0; i < todayList.size() - 1; i++) {

            //前一个时间点
            CabinetEleTotalRealtimeDo reDo = todayList.get(i);
            //当前时间点
            CabinetEleTotalRealtimeDo thisDo = todayList.get(i + 1);

            String dateTime = DateUtil.format(DateUtil.parse(thisDo.getCreateTime(), NORM_DATETIME_PATTERN), HOUR) + ":00";
            log.info("reDo : " + reDo.getEleTotal() + "thisDo : " + thisDo.getEleTotal());

            //避免负数
            double eq = (thisDo.getEleTotal() - reDo.getEleTotal()) < 0 ? 0 : thisDo.getEleTotal() - reDo.getEleTotal();

            CabinetEqTrendDTO cabinetEqTrendDTO = map.get(dateTime);
            if (Objects.isNull(cabinetEqTrendDTO)) {
                cabinetEqTrendDTO = new CabinetEqTrendDTO();
            }
            cabinetEqTrendDTO.setEq(eq);
            cabinetEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, cabinetEqTrendDTO);
        }

        map.keySet().forEach(key -> trendDTOList.add(map.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(CabinetEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }

    /**
     * 周趋势
     *
     * @param id
     * @return
     */
    private List<CabinetEqTrendDTO> weekTrend(int id) throws IOException {
        List<CabinetEqTrendDTO> trendDTOList = new ArrayList<>();

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

        List<String> list = getData(startTime, endTime, id, CABINET_EQ_TOTAL_DAY);

        Map<String, CabinetEqTotalDayDo> weekMap = new HashMap<>();

        list.forEach(str -> {
            CabinetEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, CabinetEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(DateUtil.parse(realtimeDo.getCreateTime(), HOUR_FORMAT));
            weekMap.put(dateTime, realtimeDo);

        });
        Map<Integer, CabinetEqTrendDTO> data = new HashMap<>();
        //本周数据
        thisWeek.keySet().forEach(key -> {

            String date = thisWeek.get(key);

            CabinetEqTotalDayDo realtimeDo = weekMap.get(date);

            CabinetEqTrendDTO trendDTO = new CabinetEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<Integer, String> lastWeek = getWeek(startTime);
        //上周数据
        lastWeek.keySet().forEach(key -> {

            String date = lastWeek.get(key);

            CabinetEqTotalDayDo realtimeDo = weekMap.get(date);

            CabinetEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new CabinetEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(CabinetEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }

    /**
     * 月趋势
     *
     * @param id
     * @return
     */
    private List<CabinetEqTrendDTO> monthTrend(int id) throws IOException {
        List<CabinetEqTrendDTO> trendDTOList = new ArrayList<>();

        //本月
        String startTime = "";


        //上月第一天
        Calendar lastMonthFirstDateCal = Calendar.getInstance();
        lastMonthFirstDateCal.add(Calendar.MONTH, -1);
        lastMonthFirstDateCal.set(Calendar.DAY_OF_MONTH, 1);

        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(lastMonthFirstDateCal.getTime()));

        String endTime = DateUtil.formatDateTime(DateTime.now());

        List<String> list = getData(startTime, endTime, id, CABINET_EQ_TOTAL_DAY);

        Map<String, CabinetEqTotalDayDo> monthMap = new HashMap<>();

        list.forEach(str -> {
            CabinetEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, CabinetEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(DateUtil.parse(realtimeDo.getCreateTime(), HOUR_FORMAT));
            monthMap.put(dateTime, realtimeDo);

        });
        Map<String, CabinetEqTrendDTO> data = new HashMap<>();

        Map<String, String> thisMonth = getThisMonth();
        //本月数据
        thisMonth.keySet().forEach(key -> {

            String date = thisMonth.get(key);

            CabinetEqTotalDayDo realtimeDo = monthMap.get(date);

            CabinetEqTrendDTO trendDTO = new CabinetEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<String, String> lastMonth = getLastMonth();
        //上月数据
        lastMonth.keySet().forEach(key -> {

            String date = lastMonth.get(key);

            CabinetEqTotalDayDo realtimeDo = monthMap.get(date);

            CabinetEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new CabinetEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(CabinetEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }


    /**
     * 获取日环比
     *
     * @param id
     * @param chainDTO
     * @return
     */
    private void getDayChain(int id, CabinetEleChainDTO chainDTO) throws IOException {
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

        List<String> list = getData(startTime, endTime, id, CABINET_EQ_TOTAL_DAY);
        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            CabinetEqTotalDayDo dayDo = JsonUtils.parseObject(str, CabinetEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(DateUtil.parse(dayDo.getCreateTime(), HOUR_FORMAT));
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
    private void getWeekChain(int id, CabinetEleChainDTO chainDTO) throws IOException {
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

        List<String> list = getData(startTime, endTime, id, CABINET_EQ_TOTAL_WEEK);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            CabinetEqTotalWeekDo weekDo = JsonUtils.parseObject(str, CabinetEqTotalWeekDo.class);
            String dateTime = DateUtil.formatDate(DateUtil.parse(weekDo.getCreateTime(), HOUR_FORMAT));
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
    private void getMonthChain(int id, CabinetEleChainDTO chainDTO) throws IOException {
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


        List<String> list = getData(startTime, endTime, id, CABINET_EQ_TOTAL_MONTH);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            CabinetEqTotalMonthDo monthDo = JsonUtils.parseObject(str, CabinetEqTotalMonthDo.class);
            String dateTime = DateUtil.formatDate(DateUtil.parse(monthDo.getCreateTime(), HOUR_FORMAT));
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
        CabinetEleTotalRealtimeDo endRealtimeDo = getEleData(startTime, endTime,
                SortOrder.DESC,
                CabConstants.CABINET_ELE_TOTAL_REALTIME, id);
        CabinetEleTotalRealtimeDo startRealtimeDo = getEleData(startTime, endTime,
                SortOrder.ASC,
                CabConstants.CABINET_ELE_TOTAL_REALTIME, id);
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
     * @param startTime
     * @param endTime
     * @param sortOrder
     * @param index
     * @param id
     * @return
     */
    private CabinetEleTotalRealtimeDo getEleData(String startTime, String endTime, SortOrder sortOrder, String index, int id) {
        CabinetEleTotalRealtimeDo realtimeDo = new CabinetEleTotalRealtimeDo();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + CabConstants.KEYWORD)
                            .gte(startTime)
                            .lt(endTime))
                    .must(QueryBuilders.termQuery(CABINET_ID, id))));

            // 嵌套聚合
            // 设置聚合查询
            String top = "top";
            AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                    .size(1).sort(CREATE_TIME + CabConstants.KEYWORD, sortOrder);

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
                realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), CabinetEleTotalRealtimeDo.class);
            }
            return realtimeDo;
        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return realtimeDo;
    }

    /**
     * 按天获取
     *
     * @param startTime
     * @param endTime
     * @param id
     * @param index
     * @return
     */
    private double getEleDataByDay(String startTime, String endTime, int id, String index) {
        log.info("startTime : " + startTime + "endTime：" + endTime);
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + CabConstants.KEYWORD)
                            .gte(startTime)
                            .lt(endTime))
                    .must(QueryBuilders.termQuery(CABINET_ID, id)));

            // 设置聚合查询
            builder.aggregation(AggregationBuilders.sum(EQ_VALUE).field(EQ_VALUE));


            // 设置搜索条件
            searchRequest.source(builder);
            // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();

            Sum eqs = aggregations.get(EQ_VALUE);

            return eqs.getValue();
        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return 0;
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
                .must(QueryBuilders.termQuery(CABINET_ID, id))));
        builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(10000);

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
    private List<String> getPduData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery(PDU_ID, ids))));
        builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(10000);

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
     * 获取小时
     *
     * @return
     */
    private List<String> getHour() {

        List<String> hours = new ArrayList<>();

        Date start = DateUtil.beginOfDay(DateTime.now());
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 24; i++) {
            calendar.setTime(start);
            calendar.add(Calendar.HOUR_OF_DAY, i);
            String hour = DateUtil.format(DateUtil.beginOfDay(DateTime.now()), HOUR);
            hours.add(hour);
        }
        return hours;
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
}
