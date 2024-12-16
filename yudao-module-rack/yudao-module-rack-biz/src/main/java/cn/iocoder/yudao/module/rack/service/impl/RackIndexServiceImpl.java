package cn.iocoder.yudao.module.rack.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.rack.ele.RackEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.rack.ele.RackEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.rack.pow.RackPowHourDo;
import cn.iocoder.yudao.framework.common.entity.es.rack.pow.RackPowRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.exception.ServerException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.PduIndex;
import cn.iocoder.yudao.module.rack.dto.RackEqTrendDTO;
import cn.iocoder.yudao.module.rack.dto.RackIndexDTO;
import cn.iocoder.yudao.module.rack.dto.RackPowDTO;
import cn.iocoder.yudao.framework.common.mapper.RackIndexDoMapper;
import cn.iocoder.yudao.module.rack.service.RackIndexService;
import cn.iocoder.yudao.module.rack.vo.RackIndexVo;
import cn.iocoder.yudao.module.rack.vo.RackSaveVo;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.module.rack.constant.RackConstant.KEYWORD;
import static cn.iocoder.yudao.module.rack.constant.RackConstant.*;
import static cn.iocoder.yudao.module.rack.enums.ErrorCodeConstants.NAME_REPEAT;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架业务
 * @date 2024/5/27 13:48
 */
@Service
@Slf4j
public class RackIndexServiceImpl implements RackIndexService {


    @Resource
    private RackIndexDoMapper rackIndexDoMapper;

    @Resource
    RedisTemplate redisTemplate;

    @Value("${rack-cal-refresh-url}")
    public String adder;
    @Resource
    private RestHighLevelClient client;
    public static final String HOUR_FORMAT = "yyyy-MM-dd";

    public static final String HOUR = "HH";

    public static final String DAY_FORMAT = "dd";

    @Override
    public PageResult<JSONObject> getRackPage(RackIndexVo vo) {
        try {
            //获取列表
            if (Objects.nonNull(vo.getRackIds()) && CollectionUtils.isEmpty(vo.getRackIds())) {
                List<Integer> list = new ArrayList<>();
                list.add(-1);
                vo.setRackIds(list);
            }
            //获取列表
            if (Objects.nonNull(vo.getRackIds()) && CollectionUtils.isEmpty(vo.getRackIds())) {
                List<Integer> list = new ArrayList<>();
                list.add(-1);
                vo.setRackIds(list);
            }
            Page<RackIndex> page = new Page<>(vo.getPageNo(), vo.getPageSize());

            Page<RackIndex> result = rackIndexDoMapper.selectPage(page, new LambdaQueryWrapperX<RackIndex>()
                    .eq(RackIndex::getIsDelete, DelEnums.NO_DEL.getStatus())
                    .like(StringUtils.isNotEmpty(vo.getRackName()), RackIndex::getRackName, vo.getRackName())
                    .like(StringUtils.isNotEmpty(vo.getCompany()), RackIndex::getCompany, vo.getCompany())
                    .like(StringUtils.isNotEmpty(vo.getType()), RackIndex::getRackType, vo.getType())
                    .in(!CollectionUtils.isEmpty(vo.getCabinetIds()), RackIndex::getCabinetId, vo.getRackIds())
                    .in(!CollectionUtils.isEmpty(vo.getRackIds()), RackIndex::getId, vo.getRackIds()));

            List<JSONObject> indexRes = new ArrayList<>();

            if (Objects.nonNull(result) && !CollectionUtils.isEmpty(result.getRecords())) {
                result.getRecords().forEach(dto -> {
                    String key = REDIS_KEY_RACK + dto.getId();
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
                    if (Objects.nonNull(jsonObject)) {
                        jsonObject.put("type", dto.getRackType());
                        indexRes.add(jsonObject);
                    } else {
                        indexRes.add(new JSONObject());
                    }
                });
            }
            return new PageResult<>(indexRes, result.getTotal());
        } catch (Exception e) {
            log.error("获取列表失败：", e);
        }

        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public JSONObject getRackDataDetail(int id) {

        try {
            //获取redis数据
            String key = REDIS_KEY_RACK + id;
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key)));
            return jsonObject;
        } catch (Exception e) {
            log.error("获取基础数据失败: ", e);
        }
        return new JSONObject();
    }

    @Override
    public RackIndexDTO getRackDetail(int id) {

        RackIndexDTO rackIndexDTO = new RackIndexDTO();

        try {
            RackIndex rackIndex = rackIndexDoMapper.selectById(id);

            rackIndexDTO = BeanUtils.toBean(rackIndex, RackIndexDTO.class);

            rackIndexDTO.setRackId(id);
            JSONObject jsonObject = getRackDataDetail(id);
            rackIndexDTO.setRackName(jsonObject.getString("rack_name"));
            rackIndexDTO.setRoomName(jsonObject.getString("room_name"));

            return rackIndexDTO;

        } catch (Exception e) {
            log.error("获取详情失败，", e);
        }
        return rackIndexDTO;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RackIndex> batchSave(RackSaveVo vo) {
        try {
            //新增
            if (!CollectionUtils.isEmpty(vo.getRacks())) {
                List<RackIndex> racks = vo.getRacks();

                List<RackIndex> inserts = racks.stream().filter(rackIndex -> rackIndex.getId() == 0).collect(Collectors.toList());

                inserts.forEach(rackIndex -> {

                    //判断名称是否重复
                    RackIndex index = rackIndexDoMapper.selectOne(new LambdaQueryWrapper<RackIndex>()
                            .eq(RackIndex::getRackName, rackIndex.getRackName())
                            .eq(RackIndex::getCabinetId, vo.getCabinetId()));
                    if (Objects.nonNull(index)) {
                        throw new ServerException(NAME_REPEAT.getCode(), NAME_REPEAT.getMsg());
                    }

                    rackIndex.setCabinetId(vo.getCabinetId());
                    rackIndex.setCabinetId(vo.getRoomId());
                    rackIndexDoMapper.insert(rackIndex);
                });

                List<RackIndex> updates = racks.stream().filter(rackIndex -> rackIndex.getId() > 0).collect(Collectors.toList());
                updates.forEach(rackIndex ->
                {
                    RackIndex index = rackIndexDoMapper.selectOne(new LambdaQueryWrapper<RackIndex>()
                            .eq(RackIndex::getRackName, rackIndex.getRackName())
                            .eq(RackIndex::getCabinetId, vo.getCabinetId())
                            .ne(RackIndex::getId, rackIndex.getId()));
                    if (Objects.nonNull(index)) {
                        throw new ServerException(NAME_REPEAT.getCode(), NAME_REPEAT.getMsg());
                    }
                    rackIndex.setCabinetId(vo.getCabinetId());
                    rackIndex.setCabinetId(vo.getRoomId());
                    rackIndexDoMapper.updateById(rackIndex);

                });
            }
            return rackIndexDoMapper.selectList(new LambdaQueryWrapperX<RackIndex>()
                    .eq(RackIndex::getIsDelete, DelEnums.NO_DEL.getStatus())
                    .eq(RackIndex::getCabinetId, vo.getCabinetId()));
        } finally {
            //刷新机柜计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDel(List<Integer> ids) {
        //删除列表为空
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        try {
            List<RackIndex> rackIndexList = rackIndexDoMapper.selectList(new LambdaQueryWrapperX<RackIndex>()
                    .in(RackIndex::getId, ids));
            if (CollectionUtils.isEmpty(rackIndexList)) {
                return;
            }
            rackIndexList.forEach(rackIndex -> {
                //已经删除则物理删除
                if (rackIndex.getIsDelete() == DelEnums.DELETE.getStatus()) {
                    rackIndexDoMapper.deleteById(rackIndex);
                } else {
                    //逻辑删除
                    rackIndexDoMapper.update(new LambdaUpdateWrapper<RackIndex>()
                            .eq(RackIndex::getId, rackIndex.getId())
                            .set(RackIndex::getIsDelete, DelEnums.DELETE.getStatus()));
                }

                //删除key
                String key = REDIS_KEY_RACK + rackIndex.getId();

                boolean flag = redisTemplate.delete(key);
                log.info("key: " + key + " flag : " + flag);
            });

        } finally {
            //刷新机柜计算服务缓存
            log.info("刷新计算服务缓存 --- " + adder);
            HttpUtil.get(adder);
        }
    }

    @Override
    public List<RackPowDTO> rackPowTrend(Integer id, String type) {
        List<RackPowDTO> list = new ArrayList<>();
        try {
            //近一小时
            if (type.equals("HOUR")) {
                DateTime end = DateTime.now();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                DateTime start = DateTime.of(calendar.getTime());

                String startTime = DateUtil.formatDateTime(start);
                String endTime = DateUtil.formatDateTime(end);

                List<String> data = getData(startTime, endTime, id, RACK_HDA_POW_REALTIME);
                data.forEach(str -> {
                    RackPowRealtimeDo rackPowRealtimeDo = JsonUtils.parseObject(str, RackPowRealtimeDo.class);
                    RackPowDTO dto = new RackPowDTO();
                    dto.setActivePow(rackPowRealtimeDo.getActiveTotal());
                    dto.setApparentPow(rackPowRealtimeDo.getApparentTotal());
                    dto.setDateTime(DateUtil.formatDateTime(rackPowRealtimeDo.getCreateTime()));
                    list.add(dto);
                });
                //近24小时
            } else if (type.equals(DAY)) {
                DateTime end = DateTime.now();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, -24);
                DateTime start = DateTime.of(calendar.getTime());

                String startTime = DateUtil.formatDateTime(start);
                String endTime = DateUtil.formatDateTime(end);

                List<String> data = getData(startTime, endTime, id, RACK_HDA_POW_HOUR);
                data.forEach(str -> {
                    RackPowHourDo hourDo = JsonUtils.parseObject(str, RackPowHourDo.class);
                    RackPowDTO dto = new RackPowDTO();
                    dto.setActivePow(hourDo.getActiveTotalAvgValue());
                    dto.setApparentPow(hourDo.getApparentTotalAvgValue());
                    dto.setDateTime(DateUtil.formatDateTime(hourDo.getCreateTime()));
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
    public List<RackEqTrendDTO> eqTrend(int id, String type) {
        List<RackEqTrendDTO> list = new ArrayList<>();
        try {
            //当日
            if (type.equals(DAY)) {
                list.addAll(dayTrend(id));
            }

            //当周
            log.info("week:" + type);
            if (type.equals(WEEK)) {
                log.info("week:" + type);
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

    /**
     * 获取es数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param id        请求参数
     * @param index     索引表
     * @return
     * @throws IOException
     */
    private List<String> getData(String startTime, String endTime, Integer id, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termQuery(RACK_ID, id)));
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
     * 日趋势
     *
     * @param id
     * @return
     */
    private List<RackEqTrendDTO> dayTrend(int id) throws IOException {
        List<RackEqTrendDTO> trendDTOList = new ArrayList<>();

        //今日
        String startTime = "";
        String endTime = DateUtil.formatDateTime(DateTime.now());

        //昨日
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        startTime = DateUtil.formatDateTime(getStartOfDay(calendar.getTime()));

        List<String> list = getData(startTime, endTime, id, RACK_ELE_TOTAL_REALTIME);

        List<RackEleTotalRealtimeDo> yesterdayList = new ArrayList<>();
        List<RackEleTotalRealtimeDo> todayList = new ArrayList<>();
        String finalStartTime = startTime;
        list.forEach(str -> {
            RackEleTotalRealtimeDo realtimeDo = JsonUtils.parseObject(str, RackEleTotalRealtimeDo.class);
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
        Map<String, RackEqTrendDTO> map = new HashMap<>();
        //昨日数据处理
        for (int i = 0; i < yesterdayList.size() - 1; i++) {

            //前一个时间点
            RackEleTotalRealtimeDo reDo = yesterdayList.get(i);
            //当前时间点
            RackEleTotalRealtimeDo thisDo = yesterdayList.get(i + 1);

            String dateTime = DateUtil.format(thisDo.getCreateTime(), HOUR) + ":00";
            log.info("reDo : " + reDo.getEleTotal() + "thisDo : " + thisDo.getEleTotal());
            //避免负数
            double eq = (thisDo.getEleTotal() - reDo.getEleTotal()) < 0 ? 0 : thisDo.getEleTotal() - reDo.getEleTotal();

            RackEqTrendDTO rackEqTrendDTO = new RackEqTrendDTO();
            rackEqTrendDTO.setLastEq(eq);
            rackEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, rackEqTrendDTO);

        }
        //今日数据处理
        for (int i = 0; i < todayList.size() - 1; i++) {

            //前一个时间点
            RackEleTotalRealtimeDo reDo = todayList.get(i);
            //当前时间点
            RackEleTotalRealtimeDo thisDo = todayList.get(i + 1);

            String dateTime = DateUtil.format(thisDo.getCreateTime(), HOUR) + ":00";
            log.info("reDo : " + reDo.getEleTotal() + "thisDo : " + thisDo.getEleTotal());

            //避免负数
            double eq = (thisDo.getEleTotal() - reDo.getEleTotal()) < 0 ? 0 : thisDo.getEleTotal() - reDo.getEleTotal();

            RackEqTrendDTO rackEqTrendDTO = map.get(dateTime);
            if (Objects.isNull(rackEqTrendDTO)) {
                rackEqTrendDTO = new RackEqTrendDTO();
            }
            rackEqTrendDTO.setEq(eq);
            rackEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, rackEqTrendDTO);
        }

        map.keySet().forEach(key -> trendDTOList.add(map.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(RackEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }

    /**
     * 周趋势
     *
     * @param id
     * @return
     */
    private List<RackEqTrendDTO> weekTrend(int id) throws IOException {
        List<RackEqTrendDTO> trendDTOList = new ArrayList<>();

        //本周
        String startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));

        Map<Integer, String> thisWeek = getWeek(startTime);


        //上周
        LocalDate today = LocalDate.now();
        //上周第一天
        DayOfWeek todayOfWeek = today.getDayOfWeek();
        int sub = todayOfWeek.getValue() + 6;
        LocalDate lastWeekFirst = today.minusDays(sub);
        startTime = DateUtil.formatDateTime(getStartOfDay(Date.from(lastWeekFirst.atStartOfDay(ZoneId.systemDefault()).toInstant())));

        String endTime = DateUtil.formatDateTime(DateTime.now());

        List<String> list = getData(startTime, endTime, id, RACK_EQ_TOTAL_DAY);

        Map<String, RackEqTotalDayDo> weekMap = new HashMap<>();

        list.forEach(str -> {
            RackEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, RackEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            weekMap.put(dateTime, realtimeDo);

        });
        Map<Integer, RackEqTrendDTO> data = new HashMap<>();
        //本周数据
        thisWeek.keySet().forEach(key -> {

            String date = thisWeek.get(key);

            RackEqTotalDayDo realtimeDo = weekMap.get(date);

            RackEqTrendDTO trendDTO = new RackEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<Integer, String> lastWeek = getWeek(startTime);
        //上周数据
        lastWeek.keySet().forEach(key -> {

            String date = lastWeek.get(key);

            RackEqTotalDayDo realtimeDo = weekMap.get(date);

            RackEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new RackEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(RackEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }

    /**
     * 月趋势
     *
     * @param id
     * @return
     */
    private List<RackEqTrendDTO> monthTrend(int id) throws IOException {
        List<RackEqTrendDTO> trendDTOList = new ArrayList<>();

        //本月
        String startTime = "";


        //上月第一天
        Calendar lastMonthFirstDateCal = Calendar.getInstance();
        lastMonthFirstDateCal.add(Calendar.MONTH, -1);
        lastMonthFirstDateCal.set(Calendar.DAY_OF_MONTH, 1);

        startTime = DateUtil.formatDateTime(getStartOfDay(lastMonthFirstDateCal.getTime()));

        String endTime = DateUtil.formatDateTime(DateTime.now());

        List<String> list = getData(startTime, endTime, id, RACK_EQ_TOTAL_DAY);

        Map<String, RackEqTotalDayDo> monthMap = new HashMap<>();

        list.forEach(str -> {
            RackEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, RackEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            monthMap.put(dateTime, realtimeDo);

        });
        Map<String, RackEqTrendDTO> data = new HashMap<>();

        Map<String, String> thisMonth = getThisMonth();
        //本月数据
        thisMonth.keySet().forEach(key -> {

            String date = thisMonth.get(key);

            RackEqTotalDayDo realtimeDo = monthMap.get(date);

            RackEqTrendDTO trendDTO = new RackEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<String, String> lastMonth = getLastMonth();
        //上月数据
        lastMonth.keySet().forEach(key -> {

            String date = lastMonth.get(key);

            RackEqTotalDayDo realtimeDo = monthMap.get(date);

            RackEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new RackEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEqValue() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(RackEqTrendDTO::getDateTime)).collect(Collectors.toList());


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

    // 获得某天最小时间 2020-02-17 00:00:00
    public static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public void deleteData(String startTime,String endTime) {

        Calendar endDay = Calendar.getInstance();
        endDay.add(Calendar.YEAR, -2);



        endTime = DateUtil.formatDateTime(endDay.getTime());
        log.info("endTime: " + endTime);

        GetAliasesRequest request = new GetAliasesRequest();
            GetAliasesResponse getAliasesResponse = null;

        try {
            getAliasesResponse = client.indices().getAlias(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, Set<AliasMetaData>> map = getAliasesResponse.getAliases();
            Set<String> indices = map.keySet();
        String finalEndTime = endTime;
        indices.forEach(index -> {
               while (true) {
                   // 构建查询请求
                   SearchRequest searchRequest = new SearchRequest(index); // 替换为实际的索引名称
                   SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                   sourceBuilder.query(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(finalEndTime));
                   sourceBuilder.size(10000); // 设置每次查询的文档数量
                   searchRequest.source(sourceBuilder);
                   // 执行查询请求
                   SearchResponse searchResponse = null;
                   try {
                       searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                   } catch (IOException e) {
                       throw new RuntimeException(e);
                   }
                   TotalHits totalHits = searchResponse.getHits().getTotalHits(); // 获取查询结果的总命中数
                   log.info("  size  :" + totalHits.value);

                   if (totalHits.value == 0){
                       break;
                   }
                    BulkRequest bulkRequest = new BulkRequest();
                    for (SearchHit hit : searchResponse.getHits().getHits()) {
                        String documentId = hit.getId();
                        if (StringUtils.isEmpty(documentId)){
                            continue;
                        }
                        DeleteRequest deleteRequest = new DeleteRequest(index, documentId); // 替换为实际的索引名称和文档类型
                        bulkRequest.add(deleteRequest);
                    }
                    BulkResponse bulkResponse = null;
                    try {
                        bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    // 处理删除结果
                    if (bulkResponse.hasFailures()) {
                        System.out.println("Failed to delete documents");
                    } else {
                        System.out.println("Delete documents successfully"   + "   " + index);
                    }

                }
            });
    }

    @Override
    public IPage<RackIndex> findRackIndexAll(int pageNo, int pageSize, String[] ipArray) {
        Page<RackIndex> page = new Page<RackIndex>(pageNo, pageSize);
        LambdaQueryWrapper<RackIndex> queryWrapper = new LambdaQueryWrapper<RackIndex>();
        if (ipArray != null && ipArray.length != 0) {
            queryWrapper.in(RackIndex::getId,ipArray);
        }
        queryWrapper.orderByDesc(RackIndex::getCreateTime);
        return rackIndexDoMapper.selectPage(page,queryWrapper);
    }

    @Override
    public List<RackIndex> findRackIndexToList(String[] ipArray) {
        LambdaQueryWrapper<RackIndex> queryWrapper = new LambdaQueryWrapper<RackIndex>();
        if (ipArray != null && ipArray.length != 0) {
            queryWrapper.in(RackIndex::getId,ipArray);
        }
        queryWrapper.orderByDesc(RackIndex::getCreateTime);
        return rackIndexDoMapper.selectList(queryWrapper);
    }

    @Override
    public String getAddressById(String devKey) {
        return null;
    }

}
