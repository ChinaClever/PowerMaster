package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowHourDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowRealtimeDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.module.cabinet.dto.*;
import cn.iocoder.yudao.module.cabinet.service.CabinetPowService;
import cn.iocoder.yudao.framework.common.util.TimeUtil;
import cn.iocoder.yudao.module.cabinet.vo.CabinetPowVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.*;

/**
 * @author luowei
 * @version 1.0
 * @description: 功率数据业务
 * @date 2024/4/30 11:21
 */
@Slf4j
@Service
public class CabinetPowServiceImpl implements CabinetPowService {

    @Autowired
    private RestHighLevelClient client;

    public static final String HOUR_FORMAT = "yyyy-MM-dd HH";

    public static final String TIME_STR = ":00:00";

    @Override
    public List<CabinetPowDTO> getPowList(CabinetPowVo vo) {
        List<CabinetPowDTO> list = new ArrayList<>();
        try {
            //近一个小时
            if (vo.getType().equals(HOUR)) {
                DateTime end = DateTime.now();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                DateTime start = DateTime.of(calendar.getTime());

                String startTime = DateUtil.formatDateTime(start);
                String endTime = DateUtil.formatDateTime(end);

                List<String> data = getData(startTime, endTime, vo, CABINET_HDA_POW_REALTIME);
                data.forEach(str -> {
                    CabinetPowRealtimeDo cabinetPowRealtimeDo = JsonUtils.parseObject(str, CabinetPowRealtimeDo.class);
                    CabinetPowDTO dto = new CabinetPowDTO();
                    dto.setActivePow(cabinetPowRealtimeDo.getActiveTotal());
                    dto.setApparentPow(cabinetPowRealtimeDo.getApparentTotal());
                    dto.setReactivePow(cabinetPowRealtimeDo.getActiveTotal());
                    dto.setPowerFactor(cabinetPowRealtimeDo.getFactorTotal());
                    dto.setLoadRate(cabinetPowRealtimeDo.getLoadRate());
                    dto.setDateTime(cabinetPowRealtimeDo.getCreateTime());
                    list.add(dto);
                });
                //近24小时
            } else if (vo.getType().equals(DAY)) {
                DateTime end = DateTime.now();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, -24);
                DateTime start = DateTime.of(calendar.getTime());

                String startTime = DateUtil.formatDateTime(start);
                String endTime = DateUtil.formatDateTime(end);

                List<String> data = getData(startTime, endTime, vo, CABINET_HDA_POW_HOUR);
                data.forEach(str -> {
                    CabinetPowHourDo hourDo = JsonUtils.parseObject(str, CabinetPowHourDo.class);
                    CabinetPowDTO dto = new CabinetPowDTO();
                    dto.setActivePow(hourDo.getActiveTotalAvgValue());
                    dto.setApparentPow(hourDo.getApparentTotalAvgValue());
                    dto.setReactivePow(hourDo.getReactiveTotalAvgValue());
                    dto.setPowerFactor(hourDo.getFactorTotalAvgValue());
                    dto.setLoadRate(hourDo.getLoadRateTotalAvgValue());
                    dto.setDateTime(hourDo.getCreateTime());
                    list.add(dto);
                });

            }

            return list;
        } catch (Exception e) {
            log.error("获取数据异常： ", e);
        }

        return list;

    }

    @Override
    public CabinetMaxActivePowDTO getActivePow(CabinetPowVo vo) {
        CabinetMaxActivePowDTO powDTO = new CabinetMaxActivePowDTO();
        try {

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            String startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));
            String endTime = DateUtil.formatDateTime(TimeUtil.getEndOfDay(calendar.getTime()));

            log.info("startTime : " + startTime + "endTime：" + endTime);
            //获取昨日数据
            List<String> yesterdayData = getData(startTime, endTime, vo, CABINET_HDA_POW_HOUR);

            LocalDate old = LocalDate.now().minusDays(1);
            LocalDate now = LocalDate.now();
            //获取今日数据
            List<CabinetMaxActivePowTrendDTO> todayList = new ArrayList<>();
            List<CabinetMaxActivePowTrendDTO> yesterdayList = new ArrayList<>();

            for (int i = 0; i < 24; i++) {
                String oldDay = LocalDateTimeUtil.format(LocalDateTime.of(old, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                CabinetMaxActivePowTrendDTO dto = new CabinetMaxActivePowTrendDTO();
                dto.setDateTime(oldDay);
                dto.setMaxActivePow("0");
                dto.setMaxActivePowTime(oldDay);
                yesterdayList.add(dto);
                CabinetMaxActivePowTrendDTO dto1 = new CabinetMaxActivePowTrendDTO();
                String nowDay = LocalDateTimeUtil.format(LocalDateTime.of(now, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                dto1.setDateTime(nowDay);
                dto1.setMaxActivePow("");
                dto1.setMaxActivePowTime(nowDay);
                todayList.add(dto1);
            }

//            List<CabinetActivePowTrendDTO> yesterdayList = new ArrayList<>();
            Map<String, CabinetMaxActivePowTrendDTO> yesMap = yesterdayList.stream().collect(Collectors.toMap(CabinetMaxActivePowTrendDTO::getDateTime, x -> x));
            yesterdayData.forEach(str -> {
                CabinetPowHourDo hourDo = JsonUtils.parseObject(str, CabinetPowHourDo.class);
                String dateTime = LocalDateTimeUtil.format(LocalDateTimeUtil.parse(hourDo.getCreateTime(),"yyyy-MM-dd HH:mm:ss"),"yyyy-MM-dd HH:mm");
                CabinetMaxActivePowTrendDTO dto = yesMap.get(dateTime);
                dto.setMaxActivePow(String.valueOf(BigDemicalUtil.setScale(hourDo.getActiveTotalMaxValue(), 3)));
                dto.setMaxActivePowTime(hourDo.getActiveTotalMaxTime());
            });


            startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());

//            log.info("startTime : " + startTime + "endTime：" + endTime);

            Map<String, CabinetMaxActivePowTrendDTO> todayMap = todayList.stream().collect(Collectors.toMap(CabinetMaxActivePowTrendDTO::getDateTime, x -> x));
            List<String> todayData = getData(startTime, endTime, vo, CABINET_HDA_POW_HOUR);
            todayData.forEach(str -> {
                CabinetPowHourDo hourDo = JsonUtils.parseObject(str, CabinetPowHourDo.class);
                String dateTime = LocalDateTimeUtil.format(LocalDateTimeUtil.parse(hourDo.getCreateTime(),"yyyy-MM-dd HH:mm:ss"),"yyyy-MM-dd HH:mm");
                CabinetMaxActivePowTrendDTO dto = todayMap.get(dateTime);
                dto.setMaxActivePow(String.valueOf(BigDemicalUtil.setScale(hourDo.getActiveTotalMaxValue(), 3)));
                dto.setMaxActivePowTime(hourDo.getActiveTotalMaxTime());
            });
            powDTO.setYesterdayList(yesterdayList);
            powDTO.setTodayList(todayList);
            //获取峰值
            CabinetMaxActivePowTrendDTO yesterdayMax = yesterdayList.stream().max(Comparator.comparing(CabinetMaxActivePowTrendDTO::getMaxActivePow)).orElse(new CabinetMaxActivePowTrendDTO());
            CabinetMaxActivePowTrendDTO todayMax = todayList.stream().max(Comparator.comparing(CabinetMaxActivePowTrendDTO::getMaxActivePow)).orElse(new CabinetMaxActivePowTrendDTO());
            powDTO.setTodayMax(Float.valueOf(todayMax.getMaxActivePow()));
            powDTO.setTodayMaxTime(todayMax.getDateTime());
            powDTO.setYesterdayMaxTime(yesterdayMax.getDateTime());
            powDTO.setYesterdayMax(Float.valueOf(yesterdayMax.getMaxActivePow()));



            return powDTO;
        } catch (Exception e) {
            log.error("获取数据失败： ", e);
        }
        return powDTO;

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
    private List<String> getData(String startTime, String endTime, CabinetPowVo powVo, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termQuery(CABINET_ID, powVo.getId())));
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
}
