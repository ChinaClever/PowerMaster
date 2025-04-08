package cn.iocoder.yudao.module.bus.service.busindex;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.entity.es.bus.BusBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEleTotalDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.tem.BusTemHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.total.BusTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.total.BusTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.mapper.AisleBarMapper;
import cn.iocoder.yudao.framework.common.mapper.BoxIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetBusMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.MaxValueAndCreateTime;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailRespVO;
import cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO.BusAisleBarQueryVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.buscurbalancecolor.BusCurbalanceColorDO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.module.bus.dal.mysql.buscurbalancecolor.BusCurbalanceColorMapper;
import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;
import cn.iocoder.yudao.module.bus.util.TimeUtil;
import cn.iocoder.yudao.module.bus.vo.BalanceStatisticsVO;
import cn.iocoder.yudao.module.bus.vo.BusNameVO;
import cn.iocoder.yudao.module.bus.vo.LoadRateStatus;
import cn.iocoder.yudao.module.bus.vo.ReportBasicInformationResVO;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
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

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.REDIS_KEY_AISLE;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.constant.BoxConstants.REDIS_KEY_BOX;
import static cn.iocoder.yudao.module.bus.constant.BusConstants.*;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.INDEX_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.KEY_SHORT;

/**
 * 始端箱索引 Service 实现类
 *
 * @author clever
 */
@Slf4j
@Service
@Validated
public class BusIndexServiceImpl implements BusIndexService {

    @Resource
    private BusIndexMapper busIndexMapper;

    @Resource
    private BusCurbalanceColorMapper busCurbalanceColorMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private AisleBarMapper aisleBarMapper;

    @Autowired
    private CabinetBusMapper cabinetBusMapper;

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;

    @Autowired
    private BoxIndexMapper boxIndexMapper;

    public static final String REDIS_KEY_CABINET = "packet:cabinet:";

    public static final String DAY_FORMAT = "dd";

    @Override
    public Long createIndex(BusIndexSaveReqVO createReqVO) {
        // 插入
        BusIndexDO index = BeanUtils.toBean(createReqVO, BusIndexDO.class);
        busIndexMapper.insert(index);
        // 返回
        return new Long(index.getId());
    }

    @Override
    public void updateIndex(BusIndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        BusIndexDO updateObj = BeanUtils.toBean(updateReqVO, BusIndexDO.class);
        busIndexMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Long id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        //busIndexMapper.deleteById(id);
        //逻辑删除
        busIndexMapper.update(new LambdaUpdateWrapper<BusIndexDO>()
                .eq(BusIndexDO::getId, id)
                .set(BusIndexDO::getIsDeleted, DelEnums.DELETE.getStatus())
        );
    }

    @Override
    public void restoreIndex(Long id) {
        // 校验存在
        validateIndexExists(id);
        //逻辑恢复
        busIndexMapper.update(new LambdaUpdateWrapper<BusIndexDO>()
                .eq(BusIndexDO::getId, id)
                .set(BusIndexDO::getIsDeleted, DelEnums.NO_DEL.getStatus())
        );
    }

    @Override
    public BusPowerLoadDetailRespVO getPeakDemand(BusIndexPageReqVO pageReqVO) throws IOException {
        // 返回数据
        BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaUpdateWrapper<BusIndexDO>().eq(BusIndexDO::getBusKey, pageReqVO.getDevKey()).last("limit 1"));
        BusPowerLoadDetailRespVO respVO = new BusPowerLoadDetailRespVO();
        if (ObjectUtils.isEmpty(busIndexDO)) {
            return respVO;
        }
        String startTime;
        String endTime;
        if (pageReqVO.getTimeGranularity().equals("近一小时") || pageReqVO.getTimeGranularity().equals("今天")) {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices("bus_hda_total_realtime");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            if (pageReqVO.getTimeGranularity().equals("近一小时")) {
                startTime = localDateTimeToString(LocalDateTime.now().minusHours(1));
                endTime = localDateTimeToString(LocalDateTime.now());
                searchSourceBuilder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                        .must(QueryBuilders.termQuery("bus_id", busIndexDO.getId()))));
            } else {
                startTime = localDateTimeToString(pageReqVO.getOldTime());
                endTime = localDateTimeToString(pageReqVO.getNewTime());
                searchSourceBuilder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                        .must(QueryBuilders.termQuery("bus_id", busIndexDO.getId()))));
            }
            searchSourceBuilder.sort("pow_apparent", SortOrder.DESC);
            // 执行搜索
            searchRequest.source(searchSourceBuilder);
            searchSourceBuilder.size(1);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            // 获取第一个结果
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (hits.length > 0) {
                // 获取最大值和时间字段
                Map<String, Object> sourceAsMap = hits[0].getSourceAsMap();
                respVO.setPeakDemand((Double) sourceAsMap.get("pow_apparent"));
                respVO.setPeakDemandTime((String) sourceAsMap.get("create_time"));
            }
        } else if (pageReqVO.getTimeGranularity().equals("近一天") || pageReqVO.getTimeGranularity().equals("近三天")) {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices("bus_hda_total_hour");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            if (pageReqVO.getTimeGranularity().equals("近一天")) {
                startTime = localDateTimeToString(LocalDateTime.now().minusDays(1));
            } else {
                startTime = localDateTimeToString(LocalDateTime.now().minusDays(3));
            }
            endTime = localDateTimeToString(LocalDateTime.now());
            searchSourceBuilder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                    .must(QueryBuilders.termQuery("bus_id", busIndexDO.getId()))));
            searchSourceBuilder.sort("pow_apparent_max_value", SortOrder.DESC);
            // 执行搜索
            searchRequest.source(searchSourceBuilder);
            searchSourceBuilder.size(1);
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 获取第一个结果
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (hits.length > 0) {
                // 获取最大值和时间字段
                Map<String, Object> sourceAsMap = hits[0].getSourceAsMap();
                respVO.setPeakDemand((Double) sourceAsMap.get("pow_apparent_max_value"));
                respVO.setPeakDemandTime((String) sourceAsMap.get("pow_apparent_max_time"));
            }
        }
        return respVO;
    }

    @Override
    public List<BusIndexMaxEqResVO> getMaxEq() {
        List<BusIndexMaxEqResVO> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now();

        // 昨天的起始时间（00:00:00）
        String start = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        String end = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
        //借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
        extractedMaxEq("bus_eq_total_day", start, end, result, 0);

        //上周
        start = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        end = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
        extractedMaxEq("bus_eq_total_week", start, end, result, 1);

        //上月
        start = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
        end = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
        extractedMaxEq("bus_eq_total_month", start, end, result, 2);

        List<String> collect = result.stream().map(BusResBase::getDevKey).collect(Collectors.toList());
        Map<String, BusNameVO> roomByKeys = getRoomByKeys(collect);
        for (BusIndexMaxEqResVO resVO : result) {
            BusNameVO vo = roomByKeys.get(resVO.getDevKey());
            if (Objects.nonNull(vo)) {
                vo.setLocaltion(vo.getLocaltion());
                vo.setRoomName(vo.getRoomName());
            }
        }
        return result;
    }

    private void extractedMaxEq(String indexEs, String startTime, String endTime, List<BusIndexMaxEqResVO> result, Integer type) {
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
                BusIndexMaxEqResVO busIndexDTO = new BusIndexMaxEqResVO();
                busIndexDTO.setMaxEq((Double) sourceAsMap.get("eq_value"));
                busIndexDTO.setBusId((Integer) sourceAsMap.get("bus_id"));
                BusIndexDO busIndex = busIndexMapper.selectOne(BusIndexDO::getId, busIndexDTO.getBusId());
                busIndexDTO.setDevKey(busIndex.getBusKey());
                busIndexDTO.setBusName(busIndex.getBusName());
                busIndexDTO.setId(type);//借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
                result.add(busIndexDTO);
            }
        } catch (Exception e) {
            log.error("插接箱用能最大查询异常：" + e);
        }
    }

    private Map<String, Object> extracted(String startTime, String endTime, String index) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
            ));
            TermsAggregationBuilder aggregation = AggregationBuilders.terms("terms").field("bus_id").order(BucketOrder.aggregation("sum_eq", false)).size(1)
                    .subAggregation(AggregationBuilders.sum("sum_eq").field("eq_value"));

            builder.aggregation(aggregation);

            // 设置搜索条件
            searchRequest.source(builder);
            builder.size(1);
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            // 处理聚合结果
            Aggregations aggregations = searchResponse.getAggregations();
            Terms busIdTerms = aggregations.get("terms");
            Map<String, Object> map = new HashMap<>();
            for (Terms.Bucket entry : busIdTerms.getBuckets()) {
                ParsedSum eqValueSum = entry.getAggregations().get("sum_eq");
                BusIndexDO busIndexDO = busIndexMapper.selectById(entry.getKeyAsNumber().intValue());
                if (Objects.nonNull(busIndexDO))
                    map.put(busIndexDO.getBusKey(), eqValueSum.getValue());
            }
            return map;
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 跟据key查询路径
     * 返回map<key,路径>
     *
     * @param busKeys
     * @return
     */
    public Map<String, String> getPositionByKey(List<String> busKeys) {
        if (CollectionUtils.isEmpty(busKeys)) {
            return null;
        }
        ValueOperations ops = redisTemplate.opsForValue();
        Map<String, String> map = new HashMap<>();
        //柜列
        List<AisleBar> aisleBar = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                .in(!CollectionUtils.isEmpty(busKeys), AisleBar::getBusKey, busKeys));
        Map<String, String> aislePathMap = aisleBar.stream().collect(Collectors.toMap(AisleBar::getBusKey, AisleBar::getPath));
        Map<Integer, String> aisleBarKeyMap = aisleBar.stream().collect(Collectors.toMap(AisleBar::getAisleId, AisleBar::getBusKey));

        if (!CollectionUtils.isEmpty(aisleBar)) {
            Set<String> redisKeys = aisleBar.stream().map(aisle -> REDIS_KEY_AISLE + aisle.getAisleId()).collect(Collectors.toSet());
            List aisles = ops.multiGet(redisKeys);
            if (!CollectionUtils.isEmpty(aisleBar)) {
                for (Object aisle : aisles) {
                    if (aisle == null) {
                        continue;
                    }
                    JSONObject json = JSON.parseObject(JSON.toJSONString(aisle));
                    String busKey = aisleBarKeyMap.get(json.getInteger("aisle_key"));
                    map.put(busKey, json.getString("room_name") + SPLIT_KEY
                            + json.getString("aisle_name") + SPLIT_KEY + aislePathMap.get(busKey) + "路");
                }
            }
        }
        return map;
    }
//        busKeys.removeAll(map.keySet());
//
//        if (CollectionUtils.isEmpty(busKeys)) {
//            return map;
//        }
//        List<CabinetBox> cabinetBus = cabinetBusMapper.selectList(new LambdaQueryWrapperX<CabinetBox>()
//                .in(CabinetBox::getBoxKeyA, busKeys).or().in(CabinetBox::getBoxKeyB, busKeys));
//        if (CollectionUtils.isEmpty(cabinetBus)) {
//            return map;
//        }
//        List<Integer> cabinetIds = cabinetBus.stream().map(CabinetBox::getCabinetId).collect(Collectors.toList());
//        Map<Integer, String> cabinetBusMapA = cabinetBus.stream().filter(cabinet -> cabinet.getBoxKeyA() != null).collect(Collectors.toMap(CabinetBox::getCabinetId,CabinetBox::getBoxKeyA));
//        Map<Integer, String> cabinetBusMapB = cabinetBus.stream().filter(cabinet -> cabinet.getBoxKeyB() != null).collect(Collectors.toMap(CabinetBox::getCabinetId, CabinetBox::getBoxKeyB));
//
//        List<CabinetIndex> cabinetIndices = cabinetIndexMapper.selectBatchIds(cabinetIds);
//        List<String> cabinetRedisKeys = cabinetIndices.stream().map(index -> REDIS_KEY_CABINET + index.getRoomId() + SPLIT_KEY + index.getId()).collect(Collectors.toList());
//        //设备位置
//        String devPosition;
//        List cabinets = ops.multiGet(cabinetRedisKeys);
//        if (!CollectionUtils.isEmpty(cabinets)) {
//            for (Object cabinet : cabinets) {
//                JSONObject json = JSON.parseObject(JSON.toJSONString(cabinet));
//                devPosition = json.getString("room_name") + SPLIT_KEY + json.getString("cabinet_name");
//                if (!StringUtils.isEmpty(json.getString("aisle_name"))) {
//                    devPosition += SPLIT_KEY + json.getString("aisle_name");
//                }
//                Integer cabinetId = Integer.valueOf(json.getString("cabinet_key").split("-")[1]);
//                String devKeyA = cabinetBusMapA.get(cabinetId);
//                if (!StringUtils.isEmpty(devKeyA)) {
//                    map.put(devKeyA,devPosition + SPLIT_KEY + "A路");
//                }
//                String devKeyB = cabinetBusMapB.get(cabinetId);
//                if (!StringUtils.isEmpty(devKeyB)) {
//                    map.put(devKeyB,devPosition + SPLIT_KEY + "B路");
//                }
//            }
//        }
//        return map;
//    }


    @Override
    public PageResult<BusCurLinePageResVO> getBusLineCurLinePage(BusIndexPageReqVO pageReqVO) throws IOException {
        PageResult<BusCurLinePageResVO> page = new PageResult<>();
        String startTime;
        String endTime;
        String key;
        if (pageReqVO.getTimeType() == 0) {
            key = BUS_HDA_LINE_HOUR;
            startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
            endTime = localDateTimeToString(LocalDateTime.now());
        } else {
            startTime = localDateTimeToString(pageReqVO.getOldTime());
            endTime = localDateTimeToString(pageReqVO.getNewTime());
            key = BUS_HDA_LINE_DAY;
        }
        BusIndexDO busIndexDO = busIndexMapper.selectById(pageReqVO.getBusId());
        int pageNo = pageReqVO.getPageNo();
        int pageSize = pageReqVO.getPageSize();
        int index = (pageNo - 1) * pageSize;
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(key);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.from(index);
        // 最后一页请求超过一万，pageSize设置成请求刚好一万条
        if (index + pageSize > 10000) {
            builder.size(10000 - index);
        } else {
            builder.size(pageSize);
        }
        builder.trackTotalHits(true);
        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termQuery("bus_id", pageReqVO.getBusId()))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            List<BusCurLinePageResVO> list = new ArrayList<>();
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                BusCurLinePageResVO resVO = JsonUtils.parseObject(hit.getSourceAsString(), BusCurLinePageResVO.class);

                resVO.setBusId(busIndexDO.getId()).setDevKey(busIndexDO.getBusKey());
                switch (resVO.getLineId()) {
                    case 1:
                        resVO.setLine("A路");
                        break;
                    case 2:
                        resVO.setLine("B路");
                        break;
                    case 3:
                        resVO.setLine("C路");
                        break;
                    default:
                }
                list.add(resVO);
            }
            // 匹配到的总记录数
            Long totalHits = hits.getTotalHits().value;
            page.setList(list).setTotal(totalHits);
            return page;
        }
        return null;
    }

    @Override
    public List<BusCurLinePageResVO> getBusLineCurLineExcel(BusIndexPageReqVO pageReqVO) throws IOException {
        String startTime;
        String endTime;
        String key;
        if (pageReqVO.getTimeType() == 0) {
            key = BUS_HDA_LINE_HOUR;
            startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
            endTime = localDateTimeToString(LocalDateTime.now());
        } else {
            startTime = localDateTimeToString(pageReqVO.getOldTime());
            endTime = localDateTimeToString(pageReqVO.getNewTime());
            key = BUS_HDA_LINE_DAY;
        }
        BusIndexDO busIndexDO = busIndexMapper.selectById(pageReqVO.getBusId());

        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(key);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        builder.trackTotalHits(true);
        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termQuery("bus_id", pageReqVO.getBusId()))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            List<BusCurLinePageResVO> list = new ArrayList<>();
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                BusCurLinePageResVO resVO = JsonUtils.parseObject(hit.getSourceAsString(), BusCurLinePageResVO.class);

                resVO.setBusId(busIndexDO.getId()).setDevKey(busIndexDO.getBusKey());
                switch (resVO.getLineId()) {
                    case 1:
                        resVO.setLine("A路");
                        break;
                    case 2:
                        resVO.setLine("B路");
                        break;
                    case 3:
                        resVO.setLine("C路");
                        break;
                    default:
                }
                list.add(resVO);
            }
            return list;
        }
        return null;
    }


    private void validateIndexExists(Long id) {
        if (busIndexMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public BusIndexDO getIndex(Long id) {
        return busIndexMapper.selectById(id);
    }

    @Override
    public PageResult<BusIndexRes> getIndexPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusIndexRes> res = getBusIndexRes(pageReqVO, list);
        return new PageResult<>(res, busIndexDOPageResult.getTotal());
    }

    private List<BusIndexRes> getBusIndexRes(BusIndexPageReqVO pageReqVO, List<BusIndexDO> list) {
        List<BusIndexRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);

        for (BusIndexDO busIndexDO : list) {
            BusIndexRes busIndexRes = new BusIndexRes();
            busIndexRes.setStatus(busIndexDO.getRunStatus());
            busIndexRes.setBusId(busIndexDO.getId());
            busIndexRes.setDevKey(busIndexDO.getBusKey());
            busIndexRes.setBusName(busIndexDO.getBusName());
            busIndexRes.setColor(busIndexDO.getLoadRateStatus());
            busIndexRes.setStatus(busIndexDO.getRunStatus());
            res.add(busIndexRes);
        }
        Map<String, BusIndexRes> resMap = res.stream().collect(Collectors.toMap(BusIndexRes::getDevKey, Function.identity()));
        List<String> keys = res.stream().map(BusResBase::getDevKey).collect(Collectors.toList());
        Map<String, BusNameVO> roomByKeys = getRoomByKeys(keys);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));

            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id");
            //String devKey = jsonObject.getString("dev_ip") + '_' + jsonObject.getString("bus_name");
            BusIndexRes busIndexRes = resMap.get(devKey);
            BusNameVO vo = roomByKeys.get(devKey);
            if (Objects.nonNull(vo)) {
                busIndexRes.setLocation(vo.getLocaltion());
                busIndexRes.setRoomName(vo.getRoomName());
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray loadRate = lineItemList.getJSONArray("load_rate");
            List<Double> rateList = loadRate.toList(Double.class);

            if (rateList.size() > 1) {
                busIndexRes.setALoadRate(loadRate.getInteger(0));
                busIndexRes.setBLoadRate(loadRate.getInteger(1));
                busIndexRes.setCLoadRate(loadRate.getInteger(2));
            } else {
                busIndexRes.setALoadRate(loadRate.getInteger(0));
            }
        }
        return res;
    }

    @Override
    public List<BusIndexRes> getIndexPageExcel(BusIndexPageReqVO reqVO) {
        List<BusIndexDO> list = busIndexMapper.selectList(new LambdaQueryWrapperX<BusIndexDO>()
                .eq(BusIndexDO::getIsDeleted, 0)
                .eqIfPresent(BusIndexDO::getBusKey, reqVO.getDevKey())
                .inIfPresent(BusIndexDO::getBusKey, reqVO.getBusDevKeyList())
                .inIfPresent(BusIndexDO::getId, reqVO.getBusIds())
                .eqIfPresent(BusIndexDO::getIpAddr, reqVO.getIpAddr())
                .eqIfPresent(BusIndexDO::getBusId, reqVO.getBarId())
                .eqIfPresent(BusIndexDO::getNodeId, reqVO.getNodeIp())
                .eqIfPresent(BusIndexDO::getIsDeleted, reqVO.getIsDeleted())
                .betweenIfPresent(BusIndexDO::getCreateTime, reqVO.getCreateTime())
                .ne(ObjectUtil.isNotEmpty(reqVO.getStatus()), BusIndexDO::getRunStatus, 0)
                .orderByAsc(BusIndexDO::getId));
        return getBusIndexRes(reqVO, list);
    }

    @Override
    public Map getAvgBusHdaLineForm(BusIndexPageReqVO pageReqVO) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getBusKey, pageReqVO.getDevKey()));
        if (busIndexDO != null) {
            Integer Id = busIndexDO.getId();
            String index;
            if (pageReqVO.getTimeType().equals(0)) {
                index = "bus_hda_line_hour";
            } else {
                index = "bus_hda_line_day";
            }
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.size(10000);
            builder.trackTotalHits(true);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                            .gte(LocalDateTimeUtil.format(pageReqVO.getOldTime(), "yyyy-MM-dd HH:mm:ss"))
                            .lte(LocalDateTimeUtil.format(pageReqVO.getNewTime(), "yyyy-MM-dd HH:mm:ss")))
                    .must(QueryBuilders.termQuery("bus_id", busIndexDO.getId()))));
            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
            // 设置搜索条件
            searchRequest.source(builder);

            List<BusHdaLineAvgResVO> dayList1 = new ArrayList<>();
            List<BusHdaLineAvgResVO> dayList2 = new ArrayList<>();
            List<BusHdaLineAvgResVO> dayList3 = new ArrayList<>();
            List<String> dateTimes = new ArrayList<>();
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : searchResponse.getHits()) {
                BusHdaLineAvgResVO houResVO = JsonUtils.parseObject(hit.getSourceAsString(), BusHdaLineAvgResVO.class);
                switch (houResVO.getLineId()) {
                    case 1:
                        dayList1.add(houResVO);
                        break;
                    case 2:
                        dayList2.add(houResVO);
                        break;
                    case 3:
                        dayList3.add(houResVO);
                        break;
                    default:
                }
                dateTimes.add(houResVO.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
            }
            map.put("A", dayList1);
            map.put("B", dayList2);
            map.put("C", dayList3);
            map.put("dateTimes", dateTimes.stream().distinct().collect(Collectors.toList()));
        }
        return map;
    }

    @Override
    public LineMaxResVO getBusLineMax(BusIndexPageReqVO pageReqVO) throws IOException {

        ValueOperations ops = redisTemplate.opsForValue();

        if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
            pageReqVO.setNewTime(LocalDateTime.now());
            pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
        } else {
            pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
            pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
        }

        String index;
        if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
            index = "bus_hda_line_hour";
        } else {
            index = "bus_hda_line_day";
        }
        String startTime = localDateTimeToString(pageReqVO.getOldTime());
        String endTime = localDateTimeToString(pageReqVO.getNewTime());

        String mess = getSearchResponse(index, startTime, endTime, pageReqVO.getFlagVlaue());
        if (mess == null) {
            return null;
        }
        LineMaxResVO resVO = JsonUtils.parseObject(mess, LineMaxResVO.class);
        if (Objects.nonNull(resVO)) {
            BusIndexDO busIndexDO = busIndexMapper.selectById(resVO.getBusId());
            if (busIndexDO == null) {
                return null;
            }
            resVO.setDevKey(busIndexDO.getBusKey());
            resVO.setBusName(busIndexDO.getBusName());
            switch (resVO.getLineId()) {
                case 1:
                    resVO.setLineName("A相");
                    break;
                case 2:
                    resVO.setLineName("B相");
                    break;
                case 3:
                    resVO.setLineName("C相");
                    break;
                default:
            }

            List<BusAisleBarQueryVO> records = busIndexMapper.selectPageList(busIndexDO.getBusKey().split(","));
            if (CollectionUtil.isNotEmpty(records)) {
                Map<String, BusAisleBarQueryVO> aislePathMap = records.stream().collect(Collectors.toMap(BusAisleBarQueryVO::getDevKey, x -> x));
                Set<String> redisKeys = records.stream().map(aisle -> REDIS_KEY_AISLE + aisle.getAisleId()).collect(Collectors.toSet());
                List aisles = ops.multiGet(redisKeys);
                Map<Integer, String> positonMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(records)) {
                    for (Object aisle : aisles) {
                        if (aisle == null) {
                            continue;
                        }
                        JSONObject json = JSON.parseObject(JSON.toJSONString(aisle));
                        String devPosition = json.getString("room_name") + SPLIT_KEY
                                + json.getString("aisle_name") + SPLIT_KEY;
                        positonMap.put(json.getInteger("aisle_key"), devPosition);
                    }
                }
                Map<String, Integer> keyMap = records.stream().filter(item -> ObjectUtils.isNotEmpty(item.getBarKey()))
                        .collect(Collectors.toMap(BusAisleBarQueryVO::getBarKey, val -> val.getAisleId()));// x -> x, (oldVal, newVal) -> newVal));
                Integer aisleId = keyMap.get(busIndexDO.getBusKey());
                String localtion = positonMap.get(aisleId);
                if (Objects.nonNull(aislePathMap.get(busIndexDO.getBusKey()).getPath())) {
                    resVO.setLocation(localtion + aislePathMap.get(busIndexDO.getBusKey()).getPath() + "路");
                } else {
                    resVO.setLocation(localtion + "路");
                }
            }
        }
        return resVO;
    }

    private String getSearchResponse(String index, String startTime, String endTime, int flagValue) throws IOException {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.size(1);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                    .gte(startTime).lt(endTime))));
            if (flagValue == 0) {
                builder.sort("cur_max_value", SortOrder.DESC);
            } else {
                builder.sort("pow_active_max_value", SortOrder.DESC);
            }
//        builder.aggregation(AggregationBuilders.max("max_date").field("cur_max_value"));
            // 设置搜索条件
            searchRequest.source(builder);
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.getHits().getTotalHits().value == 0) {
                return null;
            }
            return searchResponse.getHits().getAt(0).getSourceAsString();
        } catch (Exception e) {
            log.error("母线需量报错" + e);
            return null;
        }
    }

    @Override
    public BusIndexStatisticsResVO getBusIndexStatistics() {
        return busIndexMapper.selectBusIndexStatistics();
    }

    @Override
    public LoadRateStatus getBusIndexLoadRateStatus() {
        return busIndexMapper.selectBusIndexLoadRateStatus();
    }

    @Override
    public ReportBasicInformationResVO getReportBasicInformationResVO(BusIndexPageReqVO pageReqVO) {
        BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaUpdateWrapper<BusIndexDO>().eq(BusIndexDO::getBusKey, pageReqVO.getDevKey()));
        if (busIndexDO == null) {
            return new ReportBasicInformationResVO();
        }
        ReportBasicInformationResVO vo = new ReportBasicInformationResVO();
        Object obj = redisTemplate.opsForValue().get(REDIS_KEY_BUS + busIndexDO.getBusKey());
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));

        ArrayList<String> list = new ArrayList<>();
        list.add(busIndexDO.getBusKey());
        Map<String, String> positionByKey = getPositionByKey(list);
        vo.setLocation(positionByKey.get(busIndexDO.getBusKey()));
        vo.setDevKey(busIndexDO.getBusKey());
        vo.setRunStatus(busIndexDO.getRunStatus());
        if (Objects.nonNull(jsonObject)) {
            vo.setPowApparent(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getBigDecimal("pow_apparent"));
            vo.setPowerFactor(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getBigDecimal("power_factor"));
        }
        return vo;
    }

    @Override
    public List<BoxReportcopyResVO> getReportBasicInformationByBusResVO(BusIndexPageReqVO pageReqVO) {

        String devKey = pageReqVO.getDevKey();

        QueryWrapper<BoxIndex> queryWrapper = new QueryWrapper();
        queryWrapper.select("box_key", "run_status");
        queryWrapper.eq("bus_key", devKey);
        List<BoxIndex> boxIndexList = boxIndexMapper.selectList(queryWrapper);
        List<String> boxKeyList = new ArrayList<>();
        List<BoxReportcopyResVO> vos = new ArrayList<>();
        for (BoxIndex boxIndex : boxIndexList) {
            BoxReportcopyResVO vo = new BoxReportcopyResVO();
            vo.setDevKey(boxIndex.getBoxKey());
            vo.setRunStatus(boxIndex.getRunStatus());
            ValueOperations ops = redisTemplate.opsForValue();
            JSONObject jsonObject = (JSONObject) ops.get(REDIS_KEY_BOX + boxIndex.getBoxKey());
            // 获取 box_name 字段的值
            if (Objects.isNull(jsonObject)) {
                vos.add(vo);
                return vos;
            }
            String boxName = jsonObject.getString("box_name");
            vo.setBoxName(boxName);
            // 获取 box_total_data 中的 pow_apparent 字段的值
            JSONObject boxData1 = jsonObject.getJSONObject("box_data");
            JSONObject boxTotalData = boxData1.getJSONObject("box_total_data");
            if (boxTotalData != null) {
                Double powApparent = boxTotalData.getDouble("pow_apparent");
                vo.setPowApparent(powApparent);
                System.out.println("box_total_data.pow_apparent: " + powApparent);
            } else {
                System.out.println("box_total_data is null");
            }
            // 获取 outlet_item_list 中的 pow_active 数组的值
            JSONObject boxData = jsonObject.getJSONObject("box_data");
            if (boxData != null) {
                JSONObject outletItemList = boxData.getJSONObject("outlet_item_list");
                if (outletItemList != null) {
                    JSONArray powActiveArray = outletItemList.getJSONArray("pow_active");
                    if (powActiveArray != null) {
                        for (int i = 0; i < powActiveArray.size(); i++) {
                            Double powActiveValue = powActiveArray.getDouble(i);
                            if (i == 0) {
                                vo.setPowActiveOne(powActiveValue);
                            } else if (i == 1) {
                                vo.setPowActiveTwo(powActiveValue);
                            } else if (i == 2) {
                                vo.setPowActiveThree(powActiveValue);
                            }
                            System.out.println("outlet_item_list.pow_active[" + i + "]: " + powActiveValue);
                        }
                    } else {
                        System.out.println("outlet_item_list.pow_active array is null");
                    }
                } else {
                    System.out.println("outlet_item_list is null");
                }
            } else {
                System.out.println("box_data is null");
            }
            vos.add(vo);
            if (jsonObject != null) {
                boxKeyList.add(jsonObject.toJSONString());
            }
        }
        return vos;

    }

    @Override
    public BalanceStatisticsVO getBusBalanceStatistics() {
        return busIndexMapper.getBusBalanceStatistics();
    }

    @Override
    public PageResult<BusIndexDTO> getEqPage1(BusIndexPageReqVO pageReqVO) {
        String indices = null;
        LocalDate now = LocalDate.now();
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now();
        String startTime = null;
        String endTime = null;
        Integer total = 0;
        switch (pageReqVO.getTimeGranularity()) {
            case "yesterday":
                indices = "bus_eq_total_day";
                startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                break;
            case "lastWeek":
                indices = "bus_eq_total_week";
                startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                break;
            case "lastMonth":
                indices = "bus_eq_total_month";
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
                    .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))));

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            searchRequest.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<BusEqTotalDayDo> list = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits()) {
                BusEqTotalDayDo dayDo = JsonUtils.parseObject(hit.getSourceAsString(), BusEqTotalDayDo.class);
                list.add(dayDo);
            }

            if (!CollectionUtils.isEmpty(list)) {
                List<Integer> ids = list.stream().map(BusBaseDo::getBusId).collect(Collectors.toList());
                List<BusIndexDO> busIndexDOS = busIndexMapper.selectList(new LambdaUpdateWrapper<BusIndexDO>().in(BusIndexDO::getId, ids));
                List<String> keys = busIndexDOS.stream().map(BusIndexDO::getBusKey).collect(Collectors.toList());
                Map<Integer, BusIndexDO> busIndexMap = busIndexDOS.stream().collect(Collectors.toMap(BusIndexDO::getId, x -> x));

                startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                List<String> yesterdayList = getData(startTime, endTime, ids, "bus_eq_total_day");
                Map<Integer, Double> yesterdayMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(yesterdayList)) {
                    yesterdayList.forEach(str -> {
                        BusEqTotalDayDo dayDo = JsonUtils.parseObject(str, BusEqTotalDayDo.class);
                        yesterdayMap.put(dayDo.getBusId(), dayDo.getEq());
                    });
                }

                //上周
                startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                List<String> weekList = getData(startTime, endTime, ids, "bus_eq_total_week");
                Map<Integer, Double> weekMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(weekList)) {
                    weekList.forEach(str -> {
                        BusEqTotalWeekDo weekDo = JsonUtils.parseObject(str, BusEqTotalWeekDo.class);
                        weekMap.put(weekDo.getBusId(), weekDo.getEq());
                    });
                }

                //上月
                startTime = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                List<String> monthList = getData(startTime, endTime, ids, "bus_eq_total_month");
                Map<Integer, Double> monthMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(monthList)) {
                    monthList.forEach(str -> {
                        BusEqTotalMonthDo monthDo = JsonUtils.parseObject(str, BusEqTotalMonthDo.class);
                        monthMap.put(monthDo.getBusId(), monthDo.getEq());
                    });
                }
                List<BusIndexDTO> result = new ArrayList<>();
                Map<String, BusNameVO> voMap = getRoomByKeys(keys);
                list.forEach(vo -> {
                    Integer id = vo.getBusId();
                    BusIndexDO busIndex = busIndexMap.get(id);
                    BusIndexDTO busIndexDTO = new BusIndexDTO().setId(busIndex.getId()).setRunStatus(busIndex.getRunStatus());
                    busIndexDTO.setDevKey(busIndex.getBusKey());
                    BusNameVO nameVO = voMap.get(busIndex.getBusKey());
                    if (Objects.nonNull(nameVO)) {
                        busIndexDTO.setRoomName(nameVO.getRoomName());
                        busIndexDTO.setLocation(nameVO.getLocaltion());
                    }
                    busIndexDTO.setBusName(busIndex.getBusName());
                    busIndexDTO.setYesterdayEq(yesterdayMap.get(id));
                    busIndexDTO.setLastWeekEq(weekMap.get(id));
                    busIndexDTO.setLastMonthEq(monthMap.get(id));
                    if (busIndexDTO.getYesterdayEq() == null) {
                        busIndexDTO.setYesterdayEq(0.0);
                    }
                    if (busIndexDTO.getLastWeekEq() == null) {
                        busIndexDTO.setLastWeekEq(0.0);
                    }
                    if (busIndexDTO.getLastMonthEq() == null) {
                        busIndexDTO.setLastMonthEq(0.0);
                    }
                    result.add(busIndexDTO);
                });
                return new PageResult<>(result, searchResponse.getHits().getTotalHits().value);
            }
            return null;
        } catch (Exception e) {
            log.error("获取数据失败：", e);
            return null;
        }
    }

    @Override
    public List<String> findKeys(String key) {
        if (key == null || key.length() < 8) {
            throw exception(KEY_SHORT);
        }
        Integer flag = 0;
        if (key.length() <= 10) {
            flag = 1;
        }
        return busIndexMapper.findKeys(key, flag);
    }

    @Override
    public PageResult<BusRedisDataRes> getBusRedisPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage2(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusRedisDataRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        ValueOperations ops = redisTemplate.opsForValue();
        for (BusIndexDO busIndexDO : list) {
            BusRedisDataRes busRedisDataRes = new BusRedisDataRes();
            busRedisDataRes.setStatus(busIndexDO.getRunStatus());
            busRedisDataRes.setBusId(busIndexDO.getId());
            busRedisDataRes.setDevKey(busIndexDO.getBusKey());
            busRedisDataRes.setBusName(busIndexDO.getBusName());
            res.add(busRedisDataRes);
        }
        Map<String, BusRedisDataRes> resMap = res.stream().collect(Collectors.toMap(BusRedisDataRes::getDevKey, Function.identity()));
        List<String> keys = res.stream().map(BusResBase::getDevKey).collect(Collectors.toList());
        Map<String, BusNameVO> voMap = getRoomByKeys(keys);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id");
            BusRedisDataRes busRedisDataRes = resMap.get(devKey);
            BusNameVO vo = voMap.get(devKey);
            if (Objects.nonNull(vo)) {
                busRedisDataRes.setRoomName(vo.getRoomName());
                busRedisDataRes.setLocation(vo.getLocaltion());
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray volValue = lineItemList.getJSONArray("vol_value");
            JSONArray volStatus = lineItemList.getJSONArray("vol_status");
            JSONArray curValue = lineItemList.getJSONArray("cur_value");
            JSONArray curStatus = lineItemList.getJSONArray("cur_status");
            JSONArray powValue = lineItemList.getJSONArray("pow_value");
            JSONArray powStatus = lineItemList.getJSONArray("pow_status");
            JSONArray powReactive = lineItemList.getJSONArray("pow_reactive");
            JSONArray powApparent = lineItemList.getJSONArray("pow_apparent");
            busRedisDataRes.setDataUpdateTime(jsonObject.getString("sys_time"));
            busRedisDataRes.setPowApparent(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDouble("pow_apparent"));
            busRedisDataRes.setPowReactive(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDouble("pow_reactive"));
            busRedisDataRes.setPowValue(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDouble("pow_value"));
            busRedisDataRes.setPowStatus(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getInteger("pow_status"));
            for (int i = 0; i < 3; i++) {
                double vol = volValue.getDoubleValue(i);
                Integer volSta = volStatus.getInteger(i);
                double cur = curValue.getDoubleValue(i);
                Integer curSta = curStatus.getInteger(i);
                double activePow = powValue.getDoubleValue(i);
                Integer activePowSta = powStatus.getInteger(i);
                double reactivePow = powReactive.getDoubleValue(i);
                double apparentPow = powApparent.getDoubleValue(i);
                if (i == 0) {
                    busRedisDataRes.setACur(cur);
                    busRedisDataRes.setACurStatus(curSta);
                    if (curSta != 0) {
                        busRedisDataRes.setACurColor("red");
                    }
                    busRedisDataRes.setAVol(vol);
                    busRedisDataRes.setAVolStatus(volSta);
                    if (volSta != 0) {
                        busRedisDataRes.setAVolColor("red");
                    }
                    busRedisDataRes.setAActivePow(activePow);
                    busRedisDataRes.setAActivePowStatus(activePowSta);
                    if (activePowSta != 0) {
                        busRedisDataRes.setAActivePowColor("red");
                    }
                    busRedisDataRes.setAReactivePow(reactivePow);
                    busRedisDataRes.setAPowApparent(apparentPow);
                } else if (i == 1) {
                    busRedisDataRes.setBCur(cur);
                    busRedisDataRes.setBCurStatus(curSta);
                    if (curSta != 0) {
                        busRedisDataRes.setBCurColor("red");
                    }
                    busRedisDataRes.setBVol(vol);
                    busRedisDataRes.setBVolStatus(volSta);
                    if (volSta != 0) {
                        busRedisDataRes.setBVolColor("red");
                    }
                    busRedisDataRes.setBActivePow(activePow);
                    busRedisDataRes.setBActivePowStatus(activePowSta);
                    if (activePowSta != 0) {
                        busRedisDataRes.setBActivePowColor("red");
                    }
                    busRedisDataRes.setBReactivePow(reactivePow);
                    busRedisDataRes.setBPowApparent(apparentPow);
                } else if (i == 2) {
                    busRedisDataRes.setCCur(cur);
                    busRedisDataRes.setCCurStatus(curSta);
                    if (curSta != 0) {
                        busRedisDataRes.setCCurColor("red");
                    }
                    busRedisDataRes.setCVol(vol);
                    busRedisDataRes.setCVolStatus(volSta);
                    if (volSta != 0) {
                        busRedisDataRes.setCVolColor("red");
                    }
                    busRedisDataRes.setCActivePow(activePow);
                    busRedisDataRes.setCActivePowStatus(activePowSta);
                    if (activePowSta != 0) {
                        busRedisDataRes.setCActivePowColor("red");
                    }
                    busRedisDataRes.setCReactivePow(reactivePow);
                    busRedisDataRes.setCPowApparent(apparentPow);
                }
            }
        }
        return new PageResult<>(res, busIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BusIndexDTO> getEqPage(BusIndexPageReqVO pageReqVO) {
        try {
            // 创建BoolQueryBuilder对象
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
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

            PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
            List<BusIndexDO> busIndexDOList = busIndexDOPageResult.getList();
            List<BusIndexDTO> result = new ArrayList<>();
            List<Integer> ids = busIndexDOList.stream().map(BusIndexDO::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                return new PageResult<>(result, busIndexDOPageResult.getTotal());
            }

            //昨日
            LocalDate now = LocalDate.now();
            // 获取昨天的日期
            LocalDate yesterday = LocalDate.now();
            String startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
            String endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
            List<String> yesterdayList = getData(startTime, endTime, ids, "bus_eq_total_day");
            Map<Integer, Double> yesterdayMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(yesterdayList)) {
                yesterdayList.forEach(str -> {
                    BusEqTotalDayDo dayDo = JsonUtils.parseObject(str, BusEqTotalDayDo.class);
                    yesterdayMap.put(dayDo.getBusId(), dayDo.getEq());
                });
            }

            //上周
            startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
            endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
            List<String> weekList = getData(startTime, endTime, ids, "bus_eq_total_week");
            Map<Integer, Double> weekMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(weekList)) {
                weekList.forEach(str -> {
                    BusEqTotalWeekDo weekDo = JsonUtils.parseObject(str, BusEqTotalWeekDo.class);
                    weekMap.put(weekDo.getBusId(), weekDo.getEq());
                });
            }

            //上月
            startTime = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
            endTime = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
            List<String> monthList = getData(startTime, endTime, ids, "bus_eq_total_month");
            Map<Integer, Double> monthMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(monthList)) {
                monthList.forEach(str -> {
                    BusEqTotalMonthDo monthDo = JsonUtils.parseObject(str, BusEqTotalMonthDo.class);
                    monthMap.put(monthDo.getBusId(), monthDo.getEq());
                });
            }
            List<String> keys = busIndexDOList.stream().map(BusIndexDO::getBusKey).collect(Collectors.toList());
            Map<String, BusNameVO> voMap = getRoomByKeys(keys);
            busIndexDOList.forEach(busIndexDO -> {
                BusIndexDTO res = new BusIndexDTO().setId(busIndexDO.getId()).setRunStatus(busIndexDO.getRunStatus());
                res.setDevKey(busIndexDO.getBusKey()).setBusName(busIndexDO.getBusName());
                BusNameVO vo = voMap.get(busIndexDO.getBusKey());
                if (Objects.nonNull(vo)) {
                    res.setLocation(vo.getLocaltion());
                    res.setRoomName(vo.getRoomName());
                }
                res.setYesterdayEq(yesterdayMap.get(busIndexDO.getId()));
                res.setLastWeekEq(weekMap.get(busIndexDO.getId()));
                res.setLastMonthEq(monthMap.get(busIndexDO.getId()));
                if (res.getYesterdayEq() == null) {
                    res.setYesterdayEq(0.0);
                }
                if (res.getLastWeekEq() == null) {
                    res.setLastWeekEq(0.0);
                }
                if (res.getLastMonthEq() == null) {
                    res.setLastMonthEq(0.0);
                }
                result.add(res);
            });
            if (pageReqVO.getTimeGranularity().equals("yesterday")) {
                result.sort(Comparator.comparing(BusIndexDTO::getYesterdayEq).reversed());
            } else if (pageReqVO.getTimeGranularity().equals("lastWeek")) {
                result.sort(Comparator.comparing(BusIndexDTO::getLastWeekEq).reversed());
            } else if (pageReqVO.getTimeGranularity().equals("lastMonth")) {
                result.sort(Comparator.comparing(BusIndexDTO::getLastMonthEq).reversed());
            }
            return new PageResult<>(result, busIndexDOPageResult.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public PageResult<BusBalanceDataRes> getBusBalancePage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);

        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusBalanceDataRes> res = new ArrayList<>();
        List<String> keys = list.stream().map(BusIndexDO::getBusKey).collect(Collectors.toList());
        Map<String, BusNameVO> voMap = getRoomByKeys(keys);

        List redisList = getMutiRedis(list);
        Map<String, Object> map = (Map<String, Object>) redisList.stream().filter(i -> Objects.nonNull(i)).collect(Collectors.toMap(i ->
                JSON.parseObject(JSON.toJSONString(i)).getString("dev_ip") + '-' +
                        JSON.parseObject(JSON.toJSONString(i)).getString("bar_id"), Function.identity()));
        for (BusIndexDO busIndexDO : list) {
            BusBalanceDataRes busBalanceDataRes = new BusBalanceDataRes();
            res.add(busBalanceDataRes);
            busBalanceDataRes.setStatus(busIndexDO.getRunStatus());
            busBalanceDataRes.setBusId(busIndexDO.getId());
            busBalanceDataRes.setDevKey(busIndexDO.getBusKey());
            busBalanceDataRes.setBusName(busIndexDO.getBusName());
            busBalanceDataRes.setColor(busIndexDO.getCurUnbalanceStatus());

            BusNameVO vo = voMap.get(busIndexDO.getBusKey());
            if (Objects.nonNull(vo)) {
                busBalanceDataRes.setRoomName(vo.getRoomName());
                busBalanceDataRes.setLocation(vo.getLocaltion());
            }
            Object o = map.get(busIndexDO.getBusKey());

            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));

            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray volValue = lineItemList.getJSONArray("vol_value");
            JSONArray curValue = lineItemList.getJSONArray("cur_value");
            JSONObject busTotalData = jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data");
            for (int i = 0; i < 3; i++) {
                double vol = volValue.getDoubleValue(i);
                double cur = curValue.getDoubleValue(i);
                if (i == 0) {
                    busBalanceDataRes.setACur(cur);
                    busBalanceDataRes.setAVol(vol);
                } else if (i == 1) {
                    busBalanceDataRes.setBCur(cur);
                    busBalanceDataRes.setBVol(vol);
                } else if (i == 2) {
                    busBalanceDataRes.setCCur(cur);
                    busBalanceDataRes.setCVol(vol);
                }
            }
            busBalanceDataRes.setCurUnbalance(busTotalData.getDouble("cur_unbalance"));
            busBalanceDataRes.setVolUnbalance(busTotalData.getDouble("vol_unbalance"));
            if (pageReqVO.getColor() != null) {
                if (!pageReqVO.getColor().contains(busBalanceDataRes.getColor())) {
                    res.removeIf(bus -> bus.getBusId().equals(busBalanceDataRes.getBusId()));
                }
            }
        }

        return new PageResult<>(res, busIndexDOPageResult.getTotal());
    }

    @Override
    public BusBalanceDeatilRes getBusBalanceDetail(String devKey) {
        BusBalanceDeatilRes result = new BusBalanceDeatilRes();
        BusCurbalanceColorDO busCurbalanceColorDO = busCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get(REDIS_KEY_BUS + devKey);
        if (jsonObject == null) {
            return result;
        }
        JSONObject busTotalData = jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data");
        JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
        List<Double> curValue = lineItemList.getList("cur_value", Double.class);
        List<Double> volValue = lineItemList.getList("vol_value", Double.class);
        Double curUnbalance = busTotalData.getDouble("cur_unbalance");
        Double volUnbalance = busTotalData.getDouble("vol_unbalance");
        result.setCur_value(curValue);
        result.setVol_value(volValue);
        result.setCurUnbalance(curUnbalance);
        result.setVolUnbalance(volUnbalance);
        List<Double> curAlarmArrList = lineItemList.getList("cur_max", Double.class);
        double maxVal = Collections.max(curAlarmArrList);
        double a = Collections.max(curValue) - Collections.min(curValue);
        int color = 0;
        if (busCurbalanceColorDO == null) {
            if (a >= maxVal * 0.2) {
                if (curUnbalance < 15) {
                    color = 2;
                } else if (curUnbalance < 30) {
                    color = 3;
                } else {
                    color = 4;
                }
            } else {
                color = 1;
            }
        } else {
            if (a >= maxVal * 0.2) {
                if (curUnbalance < busCurbalanceColorDO.getRangeOne()) {
                    color = 2;
                } else if (curUnbalance < busCurbalanceColorDO.getRangeFour()) {
                    color = 3;
                } else {
                    color = 4;
                }
            } else {
                color = 1;
            }
        }
        result.setColor(color);
        return result;
    }

    @Override
    public List<BusTrendDTO> getBusBalanceTrend(Integer busId, Integer timeType) {
        List<BusTrendDTO> result = new ArrayList<>();
        try {
            List<Integer> ids = Arrays.asList(busId);
            String startTime;
            String endTime;
            LocalDateTime now = LocalDateTime.now();
            String index = BUS_HDA_LINE_HOUR;
            startTime = LocalDateTimeUtil.format(now.minusHours(24), "yyyy-MM-dd HH:mm:ss");
            endTime = LocalDateTimeUtil.format(now, "yyyy-MM-dd HH:mm:ss");
            if (Objects.equals(timeType, 0)) {
                index = "bus_hda_line_realtime";
                startTime = LocalDateTimeUtil.format(now.minusHours(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now, "yyyy-MM-dd HH:mm:ss");
            }
            List<String> data = getData(startTime, endTime, ids, index);
            Map<String, List> timeBus = new HashMap<>();
            data.forEach(str -> {
                if (Objects.equals(timeType, 0)) {
                    BusLineRealtimeDo hourDo = JsonUtils.parseObject(str, BusLineRealtimeDo.class);
                    String dateTime = DateUtil.format(hourDo.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                    List<BusLineRealtimeDo> lineHourDos = timeBus.get(dateTime);
                    if (CollectionUtils.isEmpty(lineHourDos)) {
                        lineHourDos = new ArrayList<>();
                    }
                    lineHourDos.add(hourDo);
                    timeBus.put(dateTime, lineHourDos);
                } else {
                    BusLineHourDo hourDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                    String dateTime = DateUtil.format(hourDo.getCreateTime(), "yyyy-MM-dd HH");
                    List<BusLineHourDo> lineHourDos = timeBus.get(dateTime);
                    if (CollectionUtils.isEmpty(lineHourDos)) {
                        lineHourDos = new ArrayList<>();
                    }
                    lineHourDos.add(hourDo);
                    timeBus.put(dateTime, lineHourDos);
                }
            });

            timeBus.keySet().forEach(dateTime -> {
                //获取每个时间段数据
                BusTrendDTO trendDTO = new BusTrendDTO();
                trendDTO.setDateTime(dateTime);
                //获取相数据
                List<Map<String, Object>> cur = new ArrayList<>();
                List<Map<String, Object>> vol = new ArrayList<>();
                if (Objects.equals(timeType, 0)) {
                    List<BusLineRealtimeDo> boxLineHourDos = timeBus.get(dateTime);
                    boxLineHourDos.forEach(hourDo -> {
                        Map<String, Object> curMap = new HashMap<>();
                        curMap.put("lineId", hourDo.getLineId());
                        curMap.put("curValue", hourDo.getCurValue());
                        Map<String, Object> volMap = new HashMap<>();
                        volMap.put("lineId", hourDo.getLineId());
                        volMap.put("volValue", hourDo.getVolValue());
                        cur.add(curMap);
                        vol.add(volMap);
                    });
                } else {
                    List<BusLineHourDo> boxLineHourDos = timeBus.get(dateTime);
                    boxLineHourDos.forEach(hourDo -> {
                        Map<String, Object> curMap = new HashMap<>();
                        curMap.put("lineId", hourDo.getLineId());
                        curMap.put("curValue", hourDo.getCurAvgValue());
                        curMap.put("curMaxValue", hourDo.getCurMaxValue());
                        curMap.put("curMinValue", hourDo.getCurMinValue());
                        Map<String, Object> volMap = new HashMap<>();
                        volMap.put("lineId", hourDo.getLineId());
                        volMap.put("volValue", hourDo.getVolAvgValue());
                        volMap.put("volMaxValue", hourDo.getVolMaxValue());
                        volMap.put("volMinValue", hourDo.getVolMinValue());
                        cur.add(curMap);
                        vol.add(volMap);
                    });
                }
                trendDTO.setCur(cur);
                trendDTO.setVol(vol);
                result.add(trendDTO);
            });
            return result.stream().sorted(Comparator.comparing(BusTrendDTO::getDateTime)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public PageResult<BusTemRes> getBusTemPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusTemRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (BusIndexDO busIndexDO : list) {
            BusTemRes busTemRes = new BusTemRes();
            busTemRes.setStatus(busIndexDO.getRunStatus());
            busTemRes.setBusId(busIndexDO.getId());
            busTemRes.setDevKey(busIndexDO.getBusKey());
            busTemRes.setBusName(busIndexDO.getBusName());
            busTemRes.setStatus(busIndexDO.getRunStatus());
            res.add(busTemRes);
        }
//        List<BusTemRes> res = BeanUtils.toBean(list, BusTemRes.class);
        Map<String, BusTemRes> resMap = res.stream().collect(Collectors.toMap(BusTemRes::getDevKey, Function.identity()));
        List<String> keys = res.stream().map(BusResBase::getDevKey).collect(Collectors.toList());
        Map<String, BusNameVO> voMap = getRoomByKeys(keys);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id");
            BusTemRes busTemRes = resMap.get(devKey);
            BusNameVO vo = voMap.get(devKey);
            if (Objects.nonNull(vo)) {
                busTemRes.setLocation(vo.getLocaltion());
                busTemRes.setRoomName(vo.getRoomName());
            }
            JSONObject envItemList = jsonObject.getJSONObject("env_item_list");
            JSONArray temValue = envItemList.getJSONArray("tem_value");
            JSONArray temStatus = envItemList.getJSONArray("tem_status");
            int status = 0;
            for (int i = 0; i < 4; i++) {
                double tem = temValue.getDoubleValue(i);
                Integer temSta = temStatus.getInteger(i);
                if (i == 0) {
                    busTemRes.setATem(tem);
                    busTemRes.setATemStatus(temSta);
                    if (temSta != 0) {
                        busTemRes.setATemColor("red");
                        status += 1;
                    }
                } else if (i == 1) {
                    busTemRes.setBTem(tem);
                    busTemRes.setBTemStatus(temSta);
                    if (temSta != 0) {
                        busTemRes.setBTemColor("red");
                        status += 1;
                    }
                } else if (i == 2) {
                    busTemRes.setCTem(tem);
                    busTemRes.setCTemStatus(temSta);
                    if (temSta != 0) {
                        busTemRes.setCTemColor("red");
                        status += 1;
                    }
                } else if (i == 3) {
                    busTemRes.setNTem(tem);
                    busTemRes.setNTemStatus(temSta);
                    if (temSta != 0) {
                        busTemRes.setNTemColor("red");
                        status += 1;
                    }
                }
            }
        }
        return new PageResult<>(res, busIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BusPFRes> getBusPFPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusPFRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);

        for (BusIndexDO busIndexDO : list) {
            BusPFRes busPFRes = new BusPFRes();
            busPFRes.setStatus(busIndexDO.getRunStatus());
            busPFRes.setBusId(busIndexDO.getId());
            busPFRes.setDevKey(busIndexDO.getBusKey());
            busPFRes.setBusName(busIndexDO.getBusName());
            res.add(busPFRes);
        }
        Map<String, BusPFRes> resMap = res.stream().collect(Collectors.toMap(BusPFRes::getDevKey, Function.identity()));
        List<String> keys = res.stream().map(BusResBase::getDevKey).collect(Collectors.toList());
        Map<String, BusNameVO> voMap = getRoomByKeys(keys);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id");
            BusPFRes busPFRes = resMap.get(devKey);
            BusNameVO vo = voMap.get(devKey);
            if (Objects.nonNull(vo)) {
                busPFRes.setRoomName(vo.getRoomName());
                busPFRes.setLocation(vo.getLocaltion());
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray pfValue = lineItemList.getJSONArray("power_factor");

            for (int i = 0; i < 3; i++) {
                double pf = pfValue.getDoubleValue(i);
                if (i == 0) {
                    busPFRes.setApf(pf);
                } else if (i == 1) {
                    busPFRes.setBpf(pf);
                } else if (i == 2) {
                    busPFRes.setCpf(pf);
                }
            }
            busPFRes.setTotalPf(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDoubleValue("power_factor"));
        }
        return new PageResult<>(res, busIndexDOPageResult.getTotal());
    }

    @Override
    public Map<String, Object> getBusPFLowest() {
        BusIndexPageReqVO pageReqVO = new BusIndexPageReqVO();
        PageResult<BusIndexDO> busIndexDOResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOResult.getList();
        List<BusPFRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        Map<String, Object> map = new HashMap<>();
        double pfInit = 1.0;
        map.put("totalPf", pfInit);

        for (BusIndexDO busIndexDO : list) {
            BusPFRes busPFRes = new BusPFRes();
            busPFRes.setStatus(busIndexDO.getRunStatus());
            busPFRes.setDevKey(busIndexDO.getBusKey());
            busPFRes.setLocation(busIndexDO.getIpAddr());
            busPFRes.setBusName(busIndexDO.getBusName());
            res.add(busPFRes);
        }
        Map<String, BusPFRes> resMap = res.stream().collect(Collectors.toMap(BusPFRes::getDevKey, Function.identity()));
        List<String> keys = res.stream().map(BusResBase::getDevKey).collect(Collectors.toList());
        Map<String, BusNameVO> voMap = getRoomByKeys(keys);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id");
            BusPFRes busPFRes = resMap.get(devKey);
            BusNameVO vo = voMap.get(devKey);
            if (Objects.nonNull(vo)) {
                busPFRes.setRoomName(vo.getRoomName());
                busPFRes.setLocation(vo.getLocaltion());
            }
            busPFRes.setTotalPf(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDoubleValue("power_factor"));

            if (busPFRes.getTotalPf() < (Double) map.get("totalPf") && busPFRes.getTotalPf() != 0) {
                map.put("totalPf", busPFRes.getTotalPf());
                map.put("location", busPFRes.getDevKey());
                map.put("ipAddr", busPFRes.getLocation());
            }
        }
        return map;
    }


    @Override
    public PageResult<BusHarmonicRes> getBusHarmonicPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusHarmonicRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (BusIndexDO busIndexDO : list) {
            BusHarmonicRes busHarmonicRes = new BusHarmonicRes();
            busHarmonicRes.setStatus(busIndexDO.getRunStatus());
            busHarmonicRes.setDevKey(busIndexDO.getBusKey());
            busHarmonicRes.setBusId(busIndexDO.getId());
            busHarmonicRes.setBusName(busIndexDO.getBusName());
            res.add(busHarmonicRes);
        }
        Map<String, BusHarmonicRes> resMap = res.stream().collect(Collectors.toMap(BusHarmonicRes::getDevKey, Function.identity()));
        List<String> keys = res.stream().map(BusResBase::getDevKey).collect(Collectors.toList());
        Map<String, BusNameVO> voMap = getRoomByKeys(keys);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id");
            BusHarmonicRes busHarmonicRes = resMap.get(devKey);
            BusNameVO vo = voMap.get(devKey);
            if (Objects.nonNull(vo)) {
                busHarmonicRes.setRoomName(vo.getRoomName());
                busHarmonicRes.setLocation(vo.getLocaltion());
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray curThd = lineItemList.getJSONArray("cur_thd");
            JSONArray volThd = lineItemList.getJSONArray("vol_thd");
            for (int i = 0; i < 3; i++) {
                BigDecimal curThdValue = BigDemicalUtil.safeDivide(curThd.getDoubleValue(i), 100);
                BigDecimal volThdValue = BigDemicalUtil.safeDivide(volThd.getDoubleValue(i), 100);
                if (i == 0) {
                    busHarmonicRes.setAcurThd(curThdValue.doubleValue());
                    busHarmonicRes.setAvolThd(volThdValue.doubleValue());
                } else if (i == 1) {
                    busHarmonicRes.setBcurThd(curThdValue.doubleValue());
                    busHarmonicRes.setBvolThd(volThdValue.doubleValue());
                } else if (i == 2) {
                    busHarmonicRes.setCcurThd(curThdValue.doubleValue());
                    busHarmonicRes.setCvolThd(volThdValue.doubleValue());
                }
            }
        }
        return new PageResult<>(res, busIndexDOPageResult.getTotal());
    }


    @Override
    public List<String> getDevKeyList() {
        return busIndexMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(BusIndexDO::getBusKey).collect(Collectors.toList());
    }

    @Override
    public PageResult<BusLineRes> getBusLineDevicePage(BusIndexPageReqVO pageReqVO) {
        try {
            LambdaQueryWrapper<BusIndexDO> queryWrapper = new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getIsDeleted, 0);
            if (ObjectUtils.isNotEmpty(pageReqVO.getDevKey()) || ObjectUtils.isNotEmpty(pageReqVO.getBusDevKeyList())) {
                queryWrapper.and(wq -> wq.in(ObjectUtils.isNotEmpty(pageReqVO.getBusDevKeyList()), BusIndexDO::getBusKey, pageReqVO.getBusDevKeyList()).or()
                        .likeLeft(ObjectUtils.isNotEmpty(pageReqVO.getDevKey()), BusIndexDO::getBusKey, pageReqVO.getDevKey()));
            }
            List<BusIndexDO> searchList = busIndexMapper.selectList(queryWrapper);

            if (CollectionUtils.isEmpty(searchList)) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            }

            Map<Integer, Map<Integer, MaxValueAndCreateTime>> curMap;
            Map<Integer, Map<Integer, MaxValueAndCreateTime>> powMap;
            String index;
            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "bus_hda_line_hour";
            } else {
                index = "bus_hda_line_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            Map esTotalAndIds = getESTotalAndIds(index, startTime, endTime, pageReqVO.getPageSize(), pageReqVO.getPageNo() - 1, searchList.stream().map(BusIndexDO::getId).collect(Collectors.toList()));

            Long total = (Long) esTotalAndIds.get("total");
            if (total == 0) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            List<Integer> ids = (List<Integer>) esTotalAndIds.get("ids");
            curMap = getBusLineCurMaxData(startTime, endTime, ids, index);
            powMap = getBusLinePowMaxData(startTime, endTime, ids, index);

            List<BusLineRes> result = new ArrayList<>();

            List<BusIndexDO> busIndices = busIndexMapper.selectList(new LambdaQueryWrapperX<BusIndexDO>()
                    .inIfPresent(BusIndexDO::getId, ids));
            List<String> keys = busIndices.stream().map(BusIndexDO::getBusKey).collect(Collectors.toList());
            Map<String, BusNameVO> voMap = getRoomByKeys(keys);
            for (BusIndexDO busIndex : busIndices) {
                Integer id = busIndex.getId();
                if (curMap.get(id) == null) {
                    continue;
                }
                BusLineRes busLineRes = new BusLineRes();
                busLineRes.setStatus(busIndex.getRunStatus());

                busLineRes.setBusId(busIndex.getId());
                busLineRes.setDevKey(busIndex.getBusKey());
                BusNameVO vo = voMap.get(busIndex.getBusKey());
                if (Objects.nonNull(vo)) {
                    busLineRes.setRoomName(vo.getRoomName());
                    busLineRes.setLocation(vo.getLocaltion());
                }
                busLineRes.setBusName(busIndex.getBusName());

                MaxValueAndCreateTime curl1 = curMap.get(id).get(1);
                busLineRes.setL1MaxCur(curl1.getMaxValue().floatValue());
                busLineRes.setL1MaxCurTime(curl1.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                MaxValueAndCreateTime curl2 = curMap.get(id).get(2);
                if (curl2 != null) {
                    busLineRes.setL2MaxCur(curl2.getMaxValue().floatValue());
                    busLineRes.setL2MaxCurTime(curl2.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                }
                MaxValueAndCreateTime curl3 = curMap.get(id).get(3);
                if (curl3 != null) {
                    busLineRes.setL3MaxCur(curl3.getMaxValue().floatValue());
                    busLineRes.setL3MaxCurTime(curl3.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                }

                MaxValueAndCreateTime powl1 = powMap.get(id).get(1);
                busLineRes.setL1MaxPow(powl1.getMaxValue().floatValue());
                busLineRes.setL1MaxPowTime(powl1.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                MaxValueAndCreateTime powl2 = powMap.get(id).get(2);
                if (powl2 != null) {
                    busLineRes.setL2MaxPow(powl2.getMaxValue().floatValue());
                    busLineRes.setL2MaxPowTime(powl2.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                }
                MaxValueAndCreateTime powl3 = powMap.get(id).get(3);
                if (powl3 != null) {
                    busLineRes.setL3MaxPow(powl3.getMaxValue().floatValue());
                    busLineRes.setL3MaxPowTime(powl3.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                }
                result.add(busLineRes);
            }
            return new PageResult<>(result, total);
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public BusLineResBase getBusLineCurLine(BusIndexPageReqVO pageReqVO) {
        BusLineResBase result = new BusLineResBase();
        try {
            String startTime;
            String endTime;
            String index;
            if (pageReqVO.getTimeType() == 0) {
                index = BUS_HDA_LINE_HOUR;
                startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
                endTime = localDateTimeToString(LocalDateTime.now());
            } else {
                startTime = localDateTimeToString(pageReqVO.getOldTime());
                endTime = localDateTimeToString(pageReqVO.getNewTime());
                index = BUS_HDA_LINE_DAY;
            }
            List<Integer> ids = Arrays.asList(pageReqVO.getBusId());
            List<String> data = getData(startTime, endTime, ids, index);

            if (pageReqVO.getLineType() == 0) {
                result.getSeries().add(new RequirementLineSeries().setName("A路最大电流"));
                result.getSeries().add(new RequirementLineSeries().setName("B路最大电流"));
                result.getSeries().add(new RequirementLineSeries().setName("C路最大电流"));
                data.forEach(str -> {
                    BusLineHourDo lineDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                    if (lineDo.getLineId() == 1) {
                        result.getTime().add(lineDo.getCurMaxTime().toString("yyyy-MM-dd HH"));
                    }
                    result.getSeries().get(lineDo.getLineId() - 1).getData().add(lineDo.getCurMaxValue());
                    ((RequirementLineSeries) result.getSeries().get(lineDo.getLineId() - 1)).getMaxTime().add(lineDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                });
            } else {
                result.getSeries().add(new RequirementLineSeries().setName("A路最大功率"));
                result.getSeries().add(new RequirementLineSeries().setName("B路最大功率"));
                result.getSeries().add(new RequirementLineSeries().setName("C路最大功率"));
                data.forEach(str -> {
                    BusLineHourDo lineDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                    if (lineDo.getLineId() == 1) {
                        result.getTime().add(lineDo.getPowActiveMaxTime().toString("yyyy-MM-dd HH"));
                    }
                    result.getSeries().get(lineDo.getLineId() - 1).getData().add(lineDo.getPowActiveMaxValue());
                    ((RequirementLineSeries) result.getSeries().get(lineDo.getLineId() - 1)).getMaxTime().add(lineDo.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                });
            }
            return result;
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public PowerRedisDataRes getBusPowerRedisData(String devKey) {
        PowerRedisDataRes result = new PowerRedisDataRes();
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + devKey);
        if (jsonObject == null) {
            return result;
        }
        JSONArray temArr = jsonObject.getJSONObject("env_item_list").getJSONArray("tem_value");
        JSONArray temStatusArr = jsonObject.getJSONObject("env_item_list").getJSONArray("tem_status");
        result.setTempA(temArr.getDouble(0));
        result.setTempB(temArr.getDouble(1));
        result.setTempC(temArr.getDouble(2));
        result.setTempN(temArr.getDouble(3));
        result.setTemStatusA(temStatusArr.getInteger(0));
        result.setTemStatusB(temStatusArr.getInteger(1));
        result.setTemStatusC(temStatusArr.getInteger(2));
        result.setTemStatusN(temStatusArr.getInteger(3));
        JSONObject busTotalData = jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data");
        result.setVub(busTotalData.getDouble("vol_unbalance"));
        result.setCub(busTotalData.getDouble("cur_unbalance"));
        result.setFr(busTotalData.getDouble("hz_value"));
        result.setPf(busTotalData.getDouble("power_factor"));
        result.setS(busTotalData.getDouble("pow_apparent"));
        result.setP(busTotalData.getDouble("pow_value"));
        result.setQ(busTotalData.getDouble("pow_reactive"));
        result.setUpdateTime(jsonObject.getString("datetime"));
        JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
        JSONArray curValue = lineItemList.getJSONArray("cur_value");
        JSONArray curStatus = lineItemList.getJSONArray("cur_status");
        result.setIa(curValue.getDouble(0));
        result.setIb(curValue.getDouble(1));
        result.setIc(curValue.getDouble(2));
        result.setCurStatusA(curStatus.getInteger(0));
        result.setCurStatusB(curStatus.getInteger(1));
        result.setCurStatusC(curStatus.getInteger(2));
        JSONArray volValue = lineItemList.getJSONArray("vol_value");
        JSONArray volStatus = lineItemList.getJSONArray("vol_status");
        result.setUa(volValue.getDouble(0));
        result.setUb(volValue.getDouble(1));
        result.setUc(volValue.getDouble(2));
        result.setVolStatusA(volStatus.getInteger(0));
        result.setVolStatusB(volStatus.getInteger(1));
        result.setVolStatusC(volStatus.getInteger(2));
        //线电压
        JSONArray VolLineStatus = lineItemList.getJSONArray("vol_line_status");
        result.setUab(result.getUa() * 1.732);
        result.setUbc(result.getUb() * 1.732);
        result.setUca(result.getUc() * 1.732);
        result.setVolLineStatusA(VolLineStatus.getInteger(0));
        result.setVolLineStatusB(VolLineStatus.getInteger(1));
        result.setVolLineStatusC(VolLineStatus.getInteger(2));
        JSONArray curMax = lineItemList.getJSONArray("cur_max");
//        JSONArray volMax = lineItemList.getJSONArray("vol_max");
        Double fInstalledCapacity = 0D;
        for (int i = 0; i < 3; i++) {
            fInstalledCapacity += curMax.getDouble(i) * 220;
        }
        result.setFInstalledCapacity(fInstalledCapacity / 1000);
        BusIndexPageReqVO reqVO = new BusIndexPageReqVO();
        reqVO.setDevKey(devKey);
        reqVO.setTimeType(0);
        List<BusLineRes> list = getBusLineDevicePage(reqVO).getList();

        if (!list.isEmpty()) {
            BusLineRes busLineRes = list.get(0);
            result.setMd(busLineRes.getL1MaxPow().doubleValue() + busLineRes.getL2MaxPow().doubleValue() + busLineRes.getL3MaxPow().doubleValue());
        } else {
            result.setMd(0.0);
        }
        JSONArray volThd = lineItemList.getJSONArray("vol_thd");
        JSONArray curThd = lineItemList.getJSONArray("cur_thd");
        result.setIaTHD(curThd.getDouble(0));
        result.setIbTHD(curThd.getDouble(1));
        result.setIcTHD(curThd.getDouble(2));
        result.setUaTHD(volThd.getDouble(0));
        result.setUbTHD(volThd.getDouble(1));
        result.setUcTHD(volThd.getDouble(2));
        result.setLoadFactor((result.getS() / result.getFInstalledCapacity()) * 100);
        return result;
    }

    @Override
    public BusLineResBase getBusLoadRateLine(BusIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        try {
            List<Integer> ids = Arrays.asList(pageReqVO.getBusId());
            String startTime = null;
            String endTime = localDateTimeToString(LocalDateTime.now());
            String index = null;
            switch (pageReqVO.getTimeGranularity()) {
                case "近一小时":
                    startTime = localDateTimeToString(LocalDateTime.now().minusHours(1));
                    index = "bus_hda_line_realtime";
                    break;
                case "今天":
                    startTime = LocalDateTimeUtil.format(LocalDate.now().atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                    index = "bus_hda_line_hour";
                    break;
                case "近一天":
                    startTime = localDateTimeToString(LocalDateTime.now().minusDays(1));
                    index = "bus_hda_line_hour";
                    break;
                case "近三天":
                    startTime = localDateTimeToString(LocalDate.now().minusDays(3).atTime(LocalTime.MIN));
                    index = "bus_hda_line_hour";
                    break;
                default:
            }

            List<String> busHdaLineRealtime = getData(startTime, endTime, ids, index);
            Map lineMap;
            if (pageReqVO.getTimeGranularity().equals("近一小时")) {
                lineMap = busHdaLineRealtime.stream()
                        .map(str -> JsonUtils.parseObject(str, BusLineRealtimeDo.class))
                        .collect(Collectors.groupingBy(BusLineRealtimeDo::getLineId));
            } else {
                lineMap = busHdaLineRealtime.stream()
                        .map(str -> JsonUtils.parseObject(str, BusLineHourDo.class))
                        .collect(Collectors.groupingBy(BusLineHourDo::getLineId));
            }
            for (int i = 1; i < 4; i++) {
                if (lineMap.get(i) != null) {
                    LineSeries lineSeries = new LineSeries();
                    if (pageReqVO.getTimeGranularity().equals("近一小时")) {
                        List<BusLineRealtimeDo> busLineRealtimeDos = (List<BusLineRealtimeDo>) lineMap.get(i);
                        List<Float> loadRate = busLineRealtimeDos.stream().map(BusLineRealtimeDo::getLoadRate).collect(Collectors.toList());
                        List<String> time = busLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                        result.setTime(time);
                        lineSeries.setData(loadRate);
                    } else {
                        List<BusLineHourDo> busLineRealtimeDos = (List<BusLineHourDo>) lineMap.get(i);
                        lineSeries.setData(busLineRealtimeDos);
                        List<String> time = busLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                        result.setTime(time);
                    }

                    if (i == 1) {
                        lineSeries.setName("A相负载率");
                    } else if (i == 2) {
                        lineSeries.setName("B相负载率");
                    } else {
                        lineSeries.setName("C相负载率");
                    }
                    result.getSeries().add(lineSeries);
                }
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public BusLineResBase getBusPowActiveLine(BusIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        try {
            List<Integer> ids = Arrays.asList(pageReqVO.getBusId());
            String startTime = null;
            String endTime = localDateTimeToString(LocalDateTime.now());
            ;
            String index = null;
            String indexTotal = null;
            switch (pageReqVO.getTimeGranularity()) {
                case "近一小时":
                    startTime = localDateTimeToString(LocalDateTime.now().minusHours(1));
                    index = "bus_hda_line_realtime";
                    indexTotal = "bus_hda_total_realtime";
                    break;
                case "今天":
                    startTime = LocalDateTimeUtil.format(LocalDate.now().atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                    index = "bus_hda_line_hour";
                    indexTotal = "bus_hda_total_hour";
                    break;
                case "近一天":
                    startTime = localDateTimeToString(LocalDateTime.now().minusDays(1));
                    index = "bus_hda_line_hour";
                    indexTotal = "bus_hda_total_hour";
                    break;
                case "近三天":
                    startTime = localDateTimeToString(LocalDate.now().minusDays(3).atTime(LocalTime.MIN));
                    index = "bus_hda_line_hour";
                    indexTotal = "bus_hda_total_hour";
                    break;
                default:
            }
            List<String> busHdaLine = getData(startTime, endTime, ids, index);
            List<String> busHdaTotal = getData(startTime, endTime, ids, indexTotal);
            LineSeries lineSeries = new LineSeries();
            lineSeries.setName("P");
            Map lineMap;
            if (Objects.equals(pageReqVO.getTimeGranularity(), "近一小时")) {
                lineMap = busHdaLine.stream()
                        .map(str -> JsonUtils.parseObject(str, BusLineRealtimeDo.class))
                        .collect(Collectors.groupingBy(BusLineRealtimeDo::getLineId));

                List<BusTotalRealtimeDo> collect = busHdaTotal.stream().map(str -> JsonUtils.parseObject(str, BusTotalRealtimeDo.class))
                        .sorted(Comparator.comparing(BusBaseDo::getCreateTime)).collect(Collectors.toList());
                lineSeries.setData(collect);
            } else {
                lineMap = busHdaLine.stream()
                        .map(str -> JsonUtils.parseObject(str, BusLineHourDo.class))
                        .collect(Collectors.groupingBy(BusLineHourDo::getLineId));

                List<BusTotalHourDo> collect = busHdaTotal.stream().map(str -> JsonUtils.parseObject(str, BusTotalHourDo.class))
                        .sorted(Comparator.comparing(BusBaseDo::getCreateTime)).collect(Collectors.toList());
                lineSeries.setData(collect);
            }
            result.getSeries().add(lineSeries);

            for (int i = 1; i < 4; i++) {
                if (lineMap.get(i) != null) {
                    LineSeries series = new LineSeries();
                    if (Objects.equals(pageReqVO.getTimeGranularity(), "近一小时")) {
                        List<BusLineRealtimeDo> busLineRealtimeDos = (List<BusLineRealtimeDo>) lineMap.get(i);
//                        List<Float> powActive = busLineRealtimeDos.stream().map(BusLineRealtimeDo::getPowActive).collect(Collectors.toList());
                        busLineRealtimeDos.sort(Comparator.comparing(BusBaseDo::getCreateTime));
                        List<String> time = busLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                        result.setTime(time);
                        series.setData(busLineRealtimeDos);
                    } else {
                        List<BusLineHourDo> busLineHourDos = (List<BusLineHourDo>) lineMap.get(i);
                        busLineHourDos.sort(Comparator.comparing(BusBaseDo::getCreateTime));
                        List<String> time = busLineHourDos.stream().map(hour -> hour.getCreateTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                        result.setTime(time);
                        series.setData(busLineHourDos);
                    }
                    if (i == 1) {
                        series.setName("a");
                    } else if (i == 2) {
                        series.setName("b");
                    } else {
                        series.setName("c");
                    }
                    result.getSeries().add(series);
                }
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public BusLineResBase getBusPowReactiveLine(BusIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        try {
            List<Integer> ids = Arrays.asList(pageReqVO.getBusId());
            if (pageReqVO.getTimeGranularity().equals("近一小时") || pageReqVO.getTimeGranularity().equals("今天")) {
                String startTime;
                String endTime;
                if (pageReqVO.getTimeGranularity().equals("近一小时")) {
                    startTime = localDateTimeToString(LocalDateTime.now().minusHours(1));
                    endTime = localDateTimeToString(LocalDateTime.now());
                } else {
                    startTime = localDateTimeToString(pageReqVO.getOldTime());
                    endTime = localDateTimeToString(pageReqVO.getNewTime());
                }
                List<String> busHdaLineRealtime = getData(startTime, endTime, ids, "bus_hda_line_realtime");
                List<String> busHdaTotalRealtime = getData(startTime, endTime, ids, "bus_hda_total_realtime");

                LineSeries lineSeries = new LineSeries();
                lineSeries.setName("Q");
                busHdaTotalRealtime.forEach(str -> {
                    BusTotalRealtimeDo esDo = JsonUtils.parseObject(str, BusTotalRealtimeDo.class);
                    lineSeries.getData().add(esDo.getPowReactive());
                });
                result.getSeries().add(lineSeries);
                Map<Integer, List<BusLineRealtimeDo>> lineMap = busHdaLineRealtime.stream()
                        .map(str -> JsonUtils.parseObject(str, BusLineRealtimeDo.class))
                        .collect(Collectors.groupingBy(BusLineRealtimeDo::getLineId));
                boolean first = false;
                for (int i = 1; i < 4; i++) {
                    if (lineMap.get(i) != null) {
                        List<BusLineRealtimeDo> busLineRealtimeDos = lineMap.get(i);
                        List<Float> powReactive = busLineRealtimeDos.stream().map(BusLineRealtimeDo::getPowReactive).collect(Collectors.toList());
                        LineSeries series = new LineSeries();
                        if (!first) {
                            List<String> time = busLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                            result.setTime(time);
                        }
                        if (i == 1) {
                            series.setName("Qa");
                        } else if (i == 2) {
                            series.setName("Qb");
                        } else {
                            series.setName("Qc");
                        }
                        series.setData(powReactive);
                        result.getSeries().add(series);
                    }
                }
            } else if (pageReqVO.getTimeGranularity().equals("近一天") || pageReqVO.getTimeGranularity().equals("近三天")) {
                String startTime;
                String endTime;
                if (pageReqVO.getTimeGranularity().equals("近一天")) {
                    startTime = localDateTimeToString(LocalDateTime.now().minusDays(1));
                } else {
                    startTime = localDateTimeToString(LocalDateTime.now().minusDays(3));
                }
                endTime = localDateTimeToString(LocalDateTime.now());
                List<String> busHdaLineHour = getData(startTime, endTime, ids, "bus_hda_line_hour");
                List<String> busHdaTotalHour = getData(startTime, endTime, ids, "bus_hda_total_hour");

                LineSeries lineSeries = new LineSeries();
                lineSeries.setName("Q");
                busHdaTotalHour.forEach(str -> {
                    BusTotalHourDo esDo = JsonUtils.parseObject(str, BusTotalHourDo.class);
                    lineSeries.getData().add(esDo.getPowReactiveAvgValue());
                });
                result.getSeries().add(lineSeries);
                Map<Integer, List<BusLineHourDo>> lineMap = busHdaLineHour.stream()
                        .map(str -> JsonUtils.parseObject(str, BusLineHourDo.class))
                        .collect(Collectors.groupingBy(BusLineHourDo::getLineId));
                boolean first = false;
                for (int i = 1; i < 4; i++) {
                    if (lineMap.get(i) != null) {
                        List<BusLineHourDo> busLineHourDos = lineMap.get(i);
                        List<Float> powReactive = busLineHourDos.stream().map(BusLineHourDo::getPowReactiveAvgValue).collect(Collectors.toList());
                        LineSeries series = new LineSeries();
                        if (!first) {
                            List<String> time = busLineHourDos.stream().map(hour -> hour.getCreateTime().toString("MM:dd HH:00")).collect(Collectors.toList());
                            result.setTime(time);
                        }
                        if (i == 1) {
                            series.setName("Qa");
                        } else if (i == 2) {
                            series.setName("Qb");
                        } else {
                            series.setName("Qc");
                        }
                        series.setData(powReactive);
                        result.getSeries().add(series);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }


    @Override
    public Map getBusTemDetail(BusIndexPageReqVO pageReqVO) {
        try {
            pageReqVO.setNewTime(pageReqVO.getOldTime().withHour(23).withMinute(59).withSecond(59));
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(pageReqVO.getBusId());
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<String> busTemHour = getData(startTime, endTime, ids, "bus_tem_hour");
            List<BusTemHourDo> strList = busTemHour.stream()
                    .map(str -> JsonUtils.parseObject(str, BusTemHourDo.class))
                    .sorted((a, b) -> {
                        DateTime timeA = a.getCreateTime();
                        DateTime timeB = b.getCreateTime();
                        return timeA.compareTo(timeB); // 升序
                    })
                    .collect(Collectors.toList());

            BusTemDetailRes result = new BusTemDetailRes();
            result.setTemAvgValueA(new ArrayList<>());
            result.setTemAvgValueB(new ArrayList<>());
            result.setTemAvgValueC(new ArrayList<>());
            result.setTemAvgValueN(new ArrayList<>());
            result.setTemAvgTime(new ArrayList<>());

            List<BusTemTableRes> tableList = new ArrayList<>();
            strList.forEach(busTemHourDo -> {
                BusTemTableRes busTemTableRes = new BusTemTableRes();
                busTemTableRes.setTemAvgValueA(busTemHourDo.getTemAAvgValue());
                busTemTableRes.setTemAvgValueB(busTemHourDo.getTemBAvgValue());
                busTemTableRes.setTemAvgValueC(busTemHourDo.getTemCAvgValue());
                busTemTableRes.setTemAvgValueN(busTemHourDo.getTemNAvgValue());
                busTemTableRes.setTemAvgTime(busTemHourDo.getCreateTime().toString("HH:mm"));
                tableList.add(busTemTableRes);
                result.getTemAvgValueA().add(busTemHourDo.getTemAAvgValue());
                result.getTemAvgValueB().add(busTemHourDo.getTemBAvgValue());
                result.getTemAvgValueC().add(busTemHourDo.getTemCAvgValue());
                result.getTemAvgValueN().add(busTemHourDo.getTemNAvgValue());
                result.getTemAvgTime().add(busTemHourDo.getCreateTime().toString("HH:mm"));
            });
            tableList.sort((a, b) -> {
                LocalTime timeA = LocalTime.parse(a.getTemAvgTime());
                LocalTime timeB = LocalTime.parse(b.getTemAvgTime());
                return timeA.compareTo(timeB); // 升序
            });
            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("chart", result);
            resultMap.put("table", tableList);
            return resultMap;
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return null;
    }

    @Override
    public Map getBusPFDetail(BusIndexPageReqVO pageReqVO) {
        try {
            pageReqVO.setNewTime(pageReqVO.getOldTime().withHour(23).withMinute(59).withSecond(59));
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(pageReqVO.getBusId());
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<BusLineHourDo> strList = getData(startTime, endTime, ids, "bus_hda_line_hour", BusLineHourDo.class);
//            List<BusLineHourDo> strList = busHdaLineHour.stream()
//                    .map(str -> JsonUtils.parseObject(str, BusLineHourDo.class))
//                    .sorted((a, b) -> {
//                        DateTime timeA = a.getCreateTime();
//                        DateTime timeB = b.getCreateTime();
//                        return timeA.compareTo(timeB); // 升序
//                    })
//                    .collect(Collectors.toList());

            HashMap<String, Object> resultMap = new HashMap<>();

            List<BusPFTableRes> tableList = new ArrayList<>();
            BusPFDetailRes result = new BusPFDetailRes();
            result.setPowerFactorAvgValueA(new ArrayList<>());
            result.setPowerFactorAvgValueB(new ArrayList<>());
            result.setPowerFactorAvgValueC(new ArrayList<>());
            result.setTime(new ArrayList<>());

            resultMap.put("chart", result);
            resultMap.put("table", tableList);

            if (strList == null || strList.size() == 0) {
                return resultMap;
            }

            Map<Integer, List<BusLineHourDo>> pfMap = strList.stream().collect(Collectors.groupingBy(busLineHourDo -> busLineHourDo.getLineId()));

            Map<DateTime, List<BusLineHourDo>> map = strList.stream().collect(Collectors.groupingBy(busLineHourDo -> busLineHourDo.getCreateTime()));
            for (DateTime time : map.keySet()) {
                BusPFTableRes busPFTableRes = new BusPFTableRes();
                List<BusLineHourDo> hourDos = map.get(time);
                if (CollectionUtils.isEmpty(hourDos)) {
                    continue;
                }
                hourDos.forEach(iter -> {
                    switch (iter.getLineId()) {
                        case 1:
                            busPFTableRes.setPowerFactorAvgValueA(iter.getPowerFactorAvgValue());
                            busPFTableRes.setPowerFactorMaxValueA(iter.getPowerFactorMaxValue());
                            busPFTableRes.setPowerFactorMaxTimeA(iter.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                            busPFTableRes.setPowerFactorMinValueA(iter.getPowerFactorMinValue());
                            busPFTableRes.setPowerFactorMinTimeA(iter.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                            break;
                        case 2:
                            busPFTableRes.setPowerFactorAvgValueB(iter.getPowerFactorAvgValue());
                            busPFTableRes.setPowerFactorMaxValueB(iter.getPowerFactorMaxValue());
                            busPFTableRes.setPowerFactorMaxTimeB(iter.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                            busPFTableRes.setPowerFactorMinValueB(iter.getPowerFactorMinValue());
                            busPFTableRes.setPowerFactorMinTimeB(iter.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                            break;
                        case 3:
                            busPFTableRes.setPowerFactorAvgValueC(iter.getPowerFactorAvgValue());
                            busPFTableRes.setPowerFactorMaxValueC(iter.getPowerFactorMaxValue());
                            busPFTableRes.setPowerFactorMaxTimeC(iter.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                            busPFTableRes.setPowerFactorMinValueC(iter.getPowerFactorMinValue());
                            busPFTableRes.setPowerFactorMinTimeC(iter.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                            break;
                        default:
                            break;
                    }
                    busPFTableRes.setTime(time.toString("yyyy-MM-dd HH:mm:ss"));
                    tableList.add(busPFTableRes);
                });


            }

//            int i = 0;
//            for (BusLineHourDo busLineHourDo : pfMap.get(1)) {
//                BusPFTableRes busPFTableRes = new BusPFTableRes();
//                result.getPowerFactorAvgValueA().add(busLineHourDo.getPowerFactorAvgValue());
//                result.getTime().add(busLineHourDo.getCreateTime().toString("HH:mm"));
//                busPFTableRes.setPowerFactorAvgValueA(busLineHourDo.getPowerFactorAvgValue());
//                busPFTableRes.setTime(busLineHourDo.getCreateTime().toString());
//                tableList.add(busPFTableRes);
//                i++;
//            }
//            int j = 0;
//            for (BusLineHourDo busLineHourDo : pfMap.get(2)) {
//                result.getPowerFactorAvgValueB().add(busLineHourDo.getPowerFactorAvgValue());
//                if (i == 0 || j >= i) {
//                    break;
//                } else if (j < i) {
//                    tableList.get(j).setPowerFactorAvgValueB(busLineHourDo.getPowerFactorAvgValue());
//                    j++;
//                }
//            }
//            j = 0;
//            for (BusLineHourDo busLineHourDo : pfMap.get(3)) {
//                result.getPowerFactorAvgValueC().add(busLineHourDo.getPowerFactorAvgValue());
//                if (i == 0 || j >= i) {
//                    break;
//                } else if (j < i) {
//                    tableList.get(j).setPowerFactorAvgValueC(busLineHourDo.getPowerFactorAvgValue());
//                    j++;
//                }
//            }
            return resultMap;
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return null;
    }

    @Override
    public BusHarmonicRedisRes getHarmonicRedis(BusIndexPageReqVO pageReqVO) {
        Integer harmonicType = 0;
        BusHarmonicRedisRes result = new BusHarmonicRedisRes();
        if (pageReqVO.getHarmonicType() > 2) {
            harmonicType = pageReqVO.getHarmonicType() - 3;
        } else {
            harmonicType = pageReqVO.getHarmonicType();
        }
        ValueOperations ops = redisTemplate.opsForValue();

        JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + pageReqVO.getDevKey());
        if (jsonObject == null) {
            return result;
        }
        JSONArray jsonArray;
        if (pageReqVO.getHarmonicType() > 2) {
            jsonArray = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list").getJSONArray("cur_thd");
        } else {
            jsonArray = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list").getJSONArray("vol_thd");
        }
        List<Float> harmoicList = new ArrayList<>();
        List<Integer> times = new ArrayList<>();
        for (int i = 1; i < 33; i++) {
            times.add(i);
        }
        for (int i = harmonicType; i < jsonArray.size(); i += 3) {
            harmoicList.add(jsonArray.getFloat(i));
        }

        result.setHarmonicList(harmoicList);
        result.setTimes(times);
        return result;
    }

    @Override
    public BusHarmonicLineRes getHarmonicLine(BusIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        for (int i = 0; i < 33; i++) {
            result.getSeries().add(new LineSeries());
        }
        pageReqVO.setNewTime(pageReqVO.getOldTime().withHour(23).withMinute(59).withSecond(59));
        try {
            Integer lineId;
            if (pageReqVO.getHarmonicType() == 0 || pageReqVO.getHarmonicType() == 3) {
                lineId = 1;
            } else if (pageReqVO.getHarmonicType() == 1 || pageReqVO.getHarmonicType() == 4) {
                lineId = 2;
            } else {
                lineId = 3;
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<Integer> ids = Arrays.asList(pageReqVO.getBusId());
            List<Integer> lines = Arrays.asList(lineId);
            List<String> busHdaLineHour = getBusHarmonicData(startTime, endTime, ids, lines, "bus_hda_line_realtime");
            busHdaLineHour.forEach(str -> {
                BusLineHourDo busLineHourDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                result.getTime().add(busLineHourDo.getCreateTime().toString("HH:mm"));
                if (pageReqVO.getHarmonicType() < 3) {
                    float[] volThd = busLineHourDo.getVolThd();
                    for (int i = 0; i < volThd.length; i++) {
                        SeriesBase lineSeries = result.getSeries().get(i + 1);
                        lineSeries.setName((i + 1) + "次谐波");
                        lineSeries.getData().add(volThd[i]);
                    }
                } else {
                    float[] curThd = busLineHourDo.getCurThd();
                    for (int i = 0; i < curThd.length; i++) {
                        SeriesBase lineSeries = result.getSeries().get(i + 1);
                        lineSeries.setName((i + 1) + "次谐波");
                        lineSeries.getData().add(curThd[i]);
                    }
                }
            });
            return result;
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }

        return result;
    }

    @Override
    public Integer getBusIdByDevKey(String devKey) {
        BusIndexDO busIndexDO = busIndexMapper.selectOne(BusIndexDO::getBusKey, devKey);
        if (busIndexDO == null) {
            return null;
        } else {
            return busIndexDO.getId();
        }
    }


    public static final String HOUR_FORMAT = "yyyy-MM-dd";

    public static final String TIME_STR = ":00:00";

    @Override
    public BusActivePowDTO getActivePow(BusPowVo vo) {
        BusActivePowDTO powDTO = new BusActivePowDTO();
        try {

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            String startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));
            String endTime = DateUtil.formatDateTime(TimeUtil.getEndOfDay(calendar.getTime()));

            LocalDate old = LocalDate.now().minusDays(1);
            LocalDate now = LocalDate.now();
            List<BusActivePowTrendDTO> yesterdayList = new ArrayList<>();
            List<BusActivePowTrendDTO> todayList = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                String oldDay = LocalDateTimeUtil.format(LocalDateTime.of(old, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                BusActivePowTrendDTO dto = new BusActivePowTrendDTO();
                dto.setDateTime(oldDay);
                dto.setActivePow("0");
                dto.setActivePowMax("0");
                dto.setActivePowMin("0");
                yesterdayList.add(dto);
                BusActivePowTrendDTO dto1 = new BusActivePowTrendDTO();
                String nowDay = LocalDateTimeUtil.format(LocalDateTime.of(now, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                dto1.setDateTime(nowDay);
                dto1.setActivePow("");
                dto1.setActivePowMax("");
                dto1.setActivePowMin("");
                todayList.add(dto1);
            }
            //获取昨日数据
            List<String> yesterdayData = getData(startTime, endTime, vo, "bus_hda_total_hour");


//            List<BusActivePowTrendDTO> yesterdayList = new ArrayList<>();
            Map<String, BusActivePowTrendDTO> yesMap = yesterdayList.stream().collect(Collectors.toMap(BusActivePowTrendDTO::getDateTime, x -> x));
            yesterdayData.forEach(str -> {
                BusTotalHourDo hourDo = JsonUtils.parseObject(str, BusTotalHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH:mm");
                BusActivePowTrendDTO dto = yesMap.get(dateTime);
                dto.setActivePow(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveAvgValue(), 3)));
                dto.setActivePowMax(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveMaxValue(), 3)));
                dto.setActivePowMin(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveMinValue(), 3)));
                dto.setActivePowMaxTime(hourDo.getPowActiveMaxTime());
                dto.setActivePowMinTime(hourDo.getPowActiveMinTime());
            });

            startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());
            //获取今日数据
//            List<BusActivePowTrendDTO> todayList = new ArrayList<>();
            Map<String, BusActivePowTrendDTO> todayMap = todayList.stream().collect(Collectors.toMap(BusActivePowTrendDTO::getDateTime, x -> x));
            List<String> todayData = getData(startTime, endTime, vo, "bus_hda_total_hour");
            todayData.forEach(str -> {
                BusTotalHourDo hourDo = JsonUtils.parseObject(str, BusTotalHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH:mm");
                BusActivePowTrendDTO dto = todayMap.get(dateTime);
                dto.setActivePow(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveAvgValue(), 3)));
                dto.setActivePowMax(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveMaxValue(), 3)));
                dto.setActivePowMin(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveMinValue(), 3)));
                dto.setActivePowMaxTime(hourDo.getPowActiveMaxTime());
                dto.setActivePowMinTime(hourDo.getPowActiveMinTime());
            });

            powDTO.setYesterdayList(yesterdayList);
            powDTO.setTodayList(todayList);
            //获取峰值
            BusActivePowTrendDTO yesterdayMax = yesterdayList.stream().filter(i -> ObjectUtils.isNotEmpty(i.getActivePowMax())).max(Comparator.comparing(BusActivePowTrendDTO::getActivePow)).orElse(new BusActivePowTrendDTO());
            BusActivePowTrendDTO todayMax = todayList.stream().filter(i -> ObjectUtils.isNotEmpty(i.getActivePowMax())).max(Comparator.comparing(BusActivePowTrendDTO::getActivePow)).orElse(new BusActivePowTrendDTO());
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
    public List<BusEqTrendDTO> eqTrend(int id, String type) {
        List<BusEqTrendDTO> list = new ArrayList<>();
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
    public BusEleChainDTO getEleChain(int id) throws IOException {
        BusEleChainDTO chainDTO = new BusEleChainDTO();
        getDayChain(id, chainDTO);
        getWeekChain(id, chainDTO);
        getMonthChain(id, chainDTO);
        return chainDTO;
    }

    @Override
    public Map getReportConsumeDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase barRes = new BusLineResBase();
        BarSeries barSeries = new BarSeries();
        try {
            BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getBusKey, devKey));
            if (busIndexDO != null) {
                String index;
                boolean isSameDay;
                Integer Id = busIndexDO.getId();
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "bus_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "bus_eq_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Id), index);
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay) {
                    for (String str : cabinetData) {
                        nowTimes++;
                        BusEleTotalDo eleDO = JsonUtils.parseObject(str, BusEleTotalDo.class);
                        if (nowTimes == 1) {
                            firstEq = eleDO.getEleActive();
                        }
                        if (nowTimes > 1) {
                            barSeries.getData().add((float) (eleDO.getEleActive() - lastEq));
                            barRes.getTime().add(eleDO.getCreateTime().toString("HH:mm"));
                        }
                        lastEq = eleDO.getEleActive();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Id), index, "ele_active");
                    BusEleTotalDo eleMaxValue = JsonUtils.parseObject(eleMax, BusEleTotalDo.class);
                    if (eleMaxValue != null) {
                        maxEle = eleMaxValue.getEleActive();
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
                        BusEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, BusEqTotalDayDo.class);
                        totalEq += totalDayDo.getEq();
                        barSeries.getData().add((float) totalDayDo.getEq());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Id), index, "eq_value");
                    BusEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, BusEqTotalDayDo.class);
                    if (eqMaxValue != null) {
                        maxEle = eqMaxValue.getEq();
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
    public Map getBusPFLine(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase totalLineRes = new BusLineResBase();
        result.put("pfLineRes", totalLineRes);
        try {
            BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getBusKey, devKey));

            if (busIndexDO != null) {
                String index;
                Integer Id = busIndexDO.getId();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "bus_hda_total_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "bus_hda_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }

                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getData(startTime, endTime, Arrays.asList(Id), index);
                List<BusTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, BusTotalHourDo.class)).collect(Collectors.toList());

                LineSeries totalPFLine = new LineSeries();
                totalPFLine.setName("总平均功率因素");

                totalLineRes.getSeries().add(totalPFLine);

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    powList.forEach(hourdo -> {
                        totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());

                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm"));

                    });
                } else {
                    powList.forEach(hourdo -> {
                        totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));
                    });
                }
                result.put("pfLineRes", totalLineRes);
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase totalLineRes = new BusLineResBase();
        result.put("totalLineRes", totalLineRes);

        result.put("apparentPowMaxValue", null);
        result.put("apparentPowMaxTime", null);
        result.put("apparentPowMinValue", null);
        result.put("apparentPowMinTime", null);
        result.put("activePowMaxValue", null);
        result.put("activePowMaxTime", null);
        result.put("activePowMinValue", null);
        result.put("activePowMinTime", null);
        result.put("reactivePowMaxTime", null);
        result.put("reactivePowMaxValue", null);
        result.put("reactivePowMinTime", null);
        result.put("reactivePowMinValue", null);
        try {
            BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getBusKey, devKey));

            if (busIndexDO != null) {
                String index;
                Integer Id = busIndexDO.getId();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "bus_hda_total_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "bus_hda_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }

                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getData(startTime, endTime, Arrays.asList(Id), index);
                List<BusTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, BusTotalHourDo.class)).collect(Collectors.toList());

                LineSeries totalApparentPow = new LineSeries();
                totalApparentPow.setName("总平均视在功率");
                LineSeries totalActivePow = new LineSeries();
                totalActivePow.setName("总平均有功功率");
                LineSeries totalReactivePow = new LineSeries();
                totalReactivePow.setName("总平均无功功率");
                totalLineRes.getSeries().add(totalReactivePow);
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);


                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getPowApparentAvgValue());
                        totalActivePow.getData().add(hourdo.getPowActiveAvgValue());
                        totalReactivePow.getData().add(hourdo.getPowReactiveAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm"));

                    });
                } else {
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getPowApparentAvgValue());
                        totalActivePow.getData().add(hourdo.getPowActiveAvgValue());
                        totalReactivePow.getData().add(hourdo.getPowReactiveAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));

                    });
                }

                String apparentTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "pow_apparent_max_value");
                BusTotalHourDo totalMaxApparent = JsonUtils.parseObject(apparentTotalMaxValue, BusTotalHourDo.class);
                String apparentTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Id), index, "pow_apparent_min_value");
                BusTotalHourDo totalMinApparent = JsonUtils.parseObject(apparentTotalMinValue, BusTotalHourDo.class);

                String activeTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "pow_active_max_value");
                BusTotalHourDo totalMaxActive = JsonUtils.parseObject(activeTotalMaxValue, BusTotalHourDo.class);
                String activeTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Id), index, "pow_active_min_value");
                BusTotalHourDo totalMinActive = JsonUtils.parseObject(activeTotalMinValue, BusTotalHourDo.class);

                String reactiveTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "pow_reactive_max_value");
                BusTotalHourDo totalMaxReactive = JsonUtils.parseObject(reactiveTotalMaxValue, BusTotalHourDo.class);
                String reactiveTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Id), index, "pow_reactive_min_value");
                BusTotalHourDo totalMinReactive = JsonUtils.parseObject(reactiveTotalMinValue, BusTotalHourDo.class);

                result.put("totalLineRes", totalLineRes);

                result.put("apparentPowMaxValue", totalMaxApparent.getPowApparentMaxValue());
                result.put("apparentPowMaxTime", totalMaxApparent.getPowApparentMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("apparentPowMinValue", totalMinApparent.getPowApparentMinValue());
                result.put("apparentPowMinTime", totalMinApparent.getPowApparentMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMaxValue", totalMaxActive.getPowActiveMaxValue());
                result.put("activePowMaxTime", totalMaxActive.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMinValue", totalMinActive.getPowActiveMinValue());
                result.put("activePowMinTime", totalMinActive.getPowActiveMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("reactivePowMaxValue", totalMaxReactive.getPowReactiveMaxValue());
                result.put("reactivePowMaxTime", totalMaxReactive.getPowReactiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("reactivePowMinValue", totalMinReactive.getPowReactiveMinValue());
                result.put("reactivePowMinTime", totalMinReactive.getPowReactiveMinTime().toString("yyyy-MM-dd HH:mm:ss"));

            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase lineRes = new BusLineResBase();
        try {
            BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getBusKey, devKey));
            if (busIndexDO != null) {
                Integer Id = busIndexDO.getId();
                String index;
                boolean isSameDay;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "bus_tem_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "bus_tem_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> busData = getData(startTime, endTime, Arrays.asList(Id), index);
                List<BusTemHourDo> temList = busData.stream()
                        .map(str -> JsonUtils.parseObject(str, BusTemHourDo.class))
                        .collect(Collectors.toList());

                List<String> time;
                LineSeries seriesA = new LineSeries();
                List<Float> temA = temList.stream().map(BusTemHourDo::getTemAAvgValue).collect(Collectors.toList());
                seriesA.setName("A相平均温度");
                seriesA.setData(temA);
                LineSeries seriesB = new LineSeries();
                List<Float> temB = temList.stream().map(BusTemHourDo::getTemBAvgValue).collect(Collectors.toList());
                seriesB.setName("B相平均温度");
                seriesB.setData(temB);
                LineSeries seriesC = new LineSeries();
                List<Float> temC = temList.stream().map(BusTemHourDo::getTemCAvgValue).collect(Collectors.toList());
                seriesC.setName("C相平均温度");
                seriesC.setData(temC);
                LineSeries seriesN = new LineSeries();
                List<Float> temN = temList.stream().map(BusTemHourDo::getTemNAvgValue).collect(Collectors.toList());
                seriesN.setName("N相平均温度");
                seriesN.setData(temN);

                if (!isSameDay) {
                    time = temList.stream().map(busTemHourDo -> busTemHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                } else {
                    time = temList.stream().map(busTemHourDo -> busTemHourDo.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                }

                lineRes.setTime(time);
                lineRes.getSeries().add(seriesA);
                lineRes.getSeries().add(seriesB);
                lineRes.getSeries().add(seriesC);
                lineRes.getSeries().add(seriesN);

                String temAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_a_max_value");
                BusTemHourDo temMaxA = JsonUtils.parseObject(temAMaxValue, BusTemHourDo.class);
                String temAMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_a_min_value");
                BusTemHourDo temMinA = JsonUtils.parseObject(temAMinValue, BusTemHourDo.class);
                if (temMaxA != null) {
                    result.put("temAMaxValue", temMaxA.getTemAMaxValue());
                    result.put("temAMaxTime", temMaxA.getTemAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (temMinA != null) {
                    result.put("temAMinValue", temMinA.getTemAMinValue());
                    result.put("temAMinTime", temMinA.getTemAMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                String temBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_b_max_value");
                BusTemHourDo temMaxB = JsonUtils.parseObject(temBMaxValue, BusTemHourDo.class);
                String temBMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_b_min_value");
                BusTemHourDo temMinB = JsonUtils.parseObject(temBMinValue, BusTemHourDo.class);
                if (temMaxB != null) {
                    result.put("temBMaxValue", temMaxB.getTemBMaxValue());
                    result.put("temBMaxTime", temMaxB.getTemBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (temMinB != null) {
                    result.put("temBMinValue", temMinB.getTemBMinValue());
                    result.put("temBMinTime", temMinB.getTemBMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                String temCMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_c_max_value");
                BusTemHourDo temMaxC = JsonUtils.parseObject(temCMaxValue, BusTemHourDo.class);
                String temCMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_c_min_value");
                BusTemHourDo temMinC = JsonUtils.parseObject(temCMinValue, BusTemHourDo.class);
                if (temMaxC != null) {
                    result.put("temCMaxValue", temMaxC.getTemCMaxValue());
                    result.put("temCMaxTime", temMaxC.getTemCMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (temMinC != null) {
                    result.put("temCMinValue", temMinC.getTemCMinValue());
                    result.put("temCMinTime", temMinC.getTemCMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                String temNMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_n_max_value");
                BusTemHourDo temMaxN = JsonUtils.parseObject(temNMaxValue, BusTemHourDo.class);
                String temNMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_n_min_value");
                BusTemHourDo temMinN = JsonUtils.parseObject(temNMinValue, BusTemHourDo.class);
                if (temMaxN != null) {
                    result.put("temNMaxValue", temMaxN.getTemNMaxValue());
                    result.put("temNMaxTime", temMaxN.getTemNMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (temMinN != null) {
                    result.put("temNMinValue", temMinN.getTemNMinValue());
                    result.put("temNMinTime", temMinN.getTemNMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                result.put("lineRes", lineRes);
                return result;
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public String getBusRedisByDevKey(String devKey) {
        if (StringUtils.isEmpty(devKey)) {
            return null;
        } else {
            ValueOperations ops = redisTemplate.opsForValue();
            JSONObject jsonObject = (JSONObject) ops.get(REDIS_KEY_BUS + devKey);
            return jsonObject != null ? jsonObject.toJSONString() : null;
        }
    }


    @Override
    public PageResult<BusIndexRes> getDeletedPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusIndexRes> res = new ArrayList<>();
        List<String> keys = list.stream().map(BusIndexDO::getBusKey).collect(Collectors.toList());
        Map<String, BusNameVO> voMap = getRoomByKeys(keys);
        for (BusIndexDO busIndexDO : list) {
            BusIndexRes busIndexRes = new BusIndexRes();
            busIndexRes.setStatus(busIndexDO.getRunStatus());
            busIndexRes.setBusId(busIndexDO.getId());
            busIndexRes.setDevKey(busIndexDO.getBusKey());
            BusNameVO vo = voMap.get(busIndexDO.getBusKey());
            if (Objects.nonNull(vo)) {
                busIndexRes.setRoomName(vo.getRoomName());
                busIndexRes.setLocation(vo.getLocaltion());
            }
            busIndexRes.setBusName(busIndexDO.getBusName());
            res.add(busIndexRes);
        }
        return new PageResult<>(res, busIndexDOPageResult.getTotal());
    }


    /**
     * 获取日环比
     *
     * @param id
     * @param chainDTO
     * @return
     */
    private void getDayChain(int id, BusEleChainDTO chainDTO) throws IOException {
        try {
            String startTime;
            String endTime;
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

            List<String> list = getData(startTime, endTime, id, BUS_EQ_TOTAL_DAY);
            Map<String, Double> eqMap = new HashMap<>();

            list.forEach(str -> {
                BusEqTotalDayDo dayDo = JsonUtils.parseObject(str, BusEqTotalDayDo.class);
                String dateTime = dayDo.getCreateTime().toString(HOUR_FORMAT);
                eqMap.put(dateTime, dayDo.getEq());

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
        } catch (Exception e) {
            log.error("始端箱日环比数据获取失败:", e);
        }
    }

    /**
     * 获取周环比
     *
     * @param id
     * @param chainDTO
     */
    private void getWeekChain(int id, BusEleChainDTO chainDTO) throws IOException {
        try {
            String startTime;
            String endTime;
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

            List<String> list = getData(startTime, endTime, id, BUS_EQ_TOTAL_WEEK);

            Map<String, Double> eqMap = new HashMap<>();

            list.forEach(str -> {
                BusEqTotalWeekDo weekDo = JsonUtils.parseObject(str, BusEqTotalWeekDo.class);
                String dateTime = weekDo.getCreateTime().toString(HOUR_FORMAT);
                eqMap.put(dateTime, weekDo.getEq());

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
        } catch (Exception e) {
            log.error("始端箱周环比数据获取失败:", e);
        }
    }

    /**
     * 获取月环比
     *
     * @param id
     * @param chainDTO
     */
    private void getMonthChain(int id, BusEleChainDTO chainDTO) throws IOException {
        try {
            String startTime;
            String endTime;
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


            List<String> list = getData(startTime, endTime, id, BUS_EQ_TOTAL_MONTH);

            Map<String, Double> eqMap = new HashMap<>();

            list.forEach(str -> {
                BusEqTotalMonthDo monthDo = JsonUtils.parseObject(str, BusEqTotalMonthDo.class);
                String dateTime = monthDo.getCreateTime().toString(HOUR_FORMAT);
                eqMap.put(dateTime, monthDo.getEq());

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
        } catch (Exception e) {
            log.error("始端箱月环比数据获取失败:", e);
        }
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
        BusEleTotalDo endRealtimeDo = getEleData(startTime, endTime,
                SortOrder.DESC,
                BUS_ELE_TOTAL_REALTIME, id);
        BusEleTotalDo startRealtimeDo = getEleData(startTime, endTime,
                SortOrder.ASC,
                BUS_ELE_TOTAL_REALTIME, id);
        //结束时间电量
        double endEle = endRealtimeDo.getEleActive();

        //开始时间电量
        double startEle = startRealtimeDo.getEleActive();

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
     * 日趋势
     *
     * @param id
     * @return
     */
    private List<BusEqTrendDTO> dayTrend(int id) throws IOException {
        List<BusEqTrendDTO> trendDTOList = new ArrayList<>();

        //今日
        String startTime;
        String endTime = DateUtil.formatDateTime(DateTime.now());

        //昨日
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));

        List<String> list = getData(startTime, endTime, id, BUS_ELE_TOTAL_REALTIME);

        List<BusEleTotalDo> yesterdayList = new ArrayList<>();
        List<BusEleTotalDo> todayList = new ArrayList<>();
        String finalStartTime = startTime;
        list.forEach(str -> {
            BusEleTotalDo realtimeDo = JsonUtils.parseObject(str, BusEleTotalDo.class);
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
        Map<String, BusEqTrendDTO> map = new HashMap<>();
        //昨日数据处理
        for (int i = 0; i < yesterdayList.size() - 1; i++) {

            //前一个时间点
            BusEleTotalDo reDo = yesterdayList.get(i);
            //当前时间点
            BusEleTotalDo thisDo = yesterdayList.get(i + 1);

            String dateTime = thisDo.getCreateTime().toString("HH") + ":00";
            log.info("reDo : " + reDo.getEleActive() + "thisDo : " + thisDo.getEleActive());
            //避免负数
            double eq = (thisDo.getEleActive() - reDo.getEleActive()) < 0 ? 0 : thisDo.getEleActive() - reDo.getEleActive();

            BusEqTrendDTO busEqTrendDTO = new BusEqTrendDTO();
            busEqTrendDTO.setLastEq(eq);
            busEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, busEqTrendDTO);

        }
        //今日数据处理
        for (int i = 0; i < todayList.size() - 1; i++) {

            //前一个时间点
            BusEleTotalDo reDo = todayList.get(i);
            //当前时间点
            BusEleTotalDo thisDo = todayList.get(i + 1);

            String dateTime = thisDo.getCreateTime().toString("HH") + ":00";
            log.info("reDo : " + reDo.getEleActive() + "thisDo : " + thisDo.getEleActive());

            //避免负数
            double eq = (thisDo.getEleActive() - reDo.getEleActive()) < 0 ? 0 : thisDo.getEleActive() - reDo.getEleActive();

            BusEqTrendDTO busEqTrendDTO = map.get(dateTime);
            if (Objects.isNull(busEqTrendDTO)) {
                busEqTrendDTO = new BusEqTrendDTO();
            }
            busEqTrendDTO.setEq(eq);
            busEqTrendDTO.setDateTime(dateTime);
            map.put(dateTime, busEqTrendDTO);
        }

        map.keySet().forEach(key -> trendDTOList.add(map.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(BusEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }

    /**
     * 周趋势
     *
     * @param id
     * @return
     */
    private List<BusEqTrendDTO> weekTrend(int id) throws IOException {
        List<BusEqTrendDTO> trendDTOList = new ArrayList<>();

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

        List<String> list = getData(startTime, endTime, id, BUS_EQ_TOTAL_DAY);

        Map<String, BusEqTotalDayDo> weekMap = new HashMap<>();

        list.forEach(str -> {
            BusEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, BusEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            weekMap.put(dateTime, realtimeDo);

        });
        Map<Integer, BusEqTrendDTO> data = new HashMap<>();
        //本周数据
        thisWeek.keySet().forEach(key -> {

            String date = thisWeek.get(key);

            BusEqTotalDayDo realtimeDo = weekMap.get(date);

            BusEqTrendDTO trendDTO = new BusEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEq() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<Integer, String> lastWeek = getWeek(startTime);
        //上周数据
        lastWeek.keySet().forEach(key -> {

            String date = lastWeek.get(key);

            BusEqTotalDayDo realtimeDo = weekMap.get(date);

            BusEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new BusEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEq() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(BusEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }


    /**
     * 月趋势
     *
     * @param id
     * @return
     */
    private List<BusEqTrendDTO> monthTrend(int id) throws IOException {
        List<BusEqTrendDTO> trendDTOList = new ArrayList<>();

        //本月
        String startTime;


        //上月第一天
        Calendar lastMonthFirstDateCal = Calendar.getInstance();
        lastMonthFirstDateCal.add(Calendar.MONTH, -1);
        lastMonthFirstDateCal.set(Calendar.DAY_OF_MONTH, 1);

        startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(lastMonthFirstDateCal.getTime()));

        String endTime = DateUtil.formatDateTime(DateTime.now());

        List<String> list = getData(startTime, endTime, id, BUS_EQ_TOTAL_DAY);

        Map<String, BusEqTotalDayDo> monthMap = new HashMap<>();

        list.forEach(str -> {
            BusEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, BusEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            monthMap.put(dateTime, realtimeDo);

        });
        Map<String, BusEqTrendDTO> data = new HashMap<>();

        Map<String, String> thisMonth = getThisMonth();
        //本月数据
        thisMonth.keySet().forEach(key -> {

            String date = thisMonth.get(key);

            BusEqTotalDayDo realtimeDo = monthMap.get(date);

            BusEqTrendDTO trendDTO = new BusEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEq() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<String, String> lastMonth = getLastMonth();
        //上月数据
        lastMonth.keySet().forEach(key -> {

            String date = lastMonth.get(key);

            BusEqTotalDayDo realtimeDo = monthMap.get(date);

            BusEqTrendDTO trendDTO = data.get(key);
            if (Objects.isNull(trendDTO)) {
                trendDTO = new BusEqTrendDTO();
            }
            trendDTO.setLastEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEq() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        data.keySet().forEach(key -> trendDTOList.add(data.get(key)));

        return trendDTOList.stream().sorted(Comparator.comparing(BusEqTrendDTO::getDateTime)).collect(Collectors.toList());


    }

    /**
     * @param startTime
     * @param endTime
     * @param sortOrder
     * @param index
     * @param id
     * @return
     */
    private BusEleTotalDo getEleData(String startTime, String endTime, SortOrder sortOrder, String index, int id) {
        BusEleTotalDo realtimeDo = new BusEleTotalDo();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD)
                            .gte(startTime)
                            .lt(endTime))
                    .must(QueryBuilders.termQuery(BUS_ID, id))));

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
                realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), BusEleTotalDo.class);
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
                .must(QueryBuilders.termsQuery("bus_id", ids))));
        builder.sort(order, SortOrder.DESC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                return hit.getSourceAsString();
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
                .must(QueryBuilders.termsQuery("bus_id", ids))));
        builder.sort(order, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1);

        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                return hit.getSourceAsString();
            }
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
    private List<String> getData(String startTime, String endTime, int id, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termQuery(BUS_ID, id))));
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
     * 获取es数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param powVo     请求参数
     * @param index     索引表
     * @return
     * @throws IOException
     */
    private List<String> getData(String startTime, String endTime, BusPowVo powVo, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termQuery("bus_id", powVo.getId())));
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
                    .must(QueryBuilders.termsQuery("bus_id", ids))));
//            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
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
            return null;
        }
    }

    private List getData(String startTime, String endTime, List<Integer> ids, String index, Class cls) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termsQuery("bus_id", ids))));
//            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
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
                    if (str.length() > 2) {
                        Object obj = JsonUtils.parseObject(str, cls);
                        list.add(obj);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> getBusHarmonicData(String startTime, String endTime, List<Integer> ids, List<Integer> lines, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("bus_id", ids))
                .must(QueryBuilders.termsQuery("line_id", lines))));
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


    private Map<Integer, Map<Integer, MaxValueAndCreateTime>> getBusLineCurMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                        .gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("bus_id", ids))));

        builder.aggregation(
                AggregationBuilders.terms("group_by_bus_id")
                        .field("bus_id")
                        .size(10000)
                        .subAggregation(AggregationBuilders.terms("by_line_id")
                                .field("line_id")
                                .size(1000)
                                .order(BucketOrder.aggregation("max_cur", false))
                                .subAggregation(AggregationBuilders.max("max_cur").field("cur_max_value"))
                                .subAggregation(AggregationBuilders.topHits("top_docs")
                                        .size(1)
                                        .fetchSource(new String[]{"cur_max_time"}, null)
                                        .sort(SortBuilders.fieldSort("cur_max_value").order(SortOrder.DESC))))
        );
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(0);
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 初始化结果Map
        Map<Integer, Map<Integer, MaxValueAndCreateTime>> resultMap = new HashMap<>();
        // 获取group_by_box_id聚合结果
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_bus_id");
        for (Terms.Bucket boxIdBucket : groupByBoxId.getBuckets()) {
            Integer boxId = boxIdBucket.getKeyAsNumber().intValue();
            Map<Integer, MaxValueAndCreateTime> lineIdMap = new HashMap<>();
            // 获取by_line_id聚合结果
            Terms byLineId = boxIdBucket.getAggregations().get("by_line_id");
            for (Terms.Bucket lineIdBucket : byLineId.getBuckets()) {
                Integer lineId = lineIdBucket.getKeyAsNumber().intValue();
                MaxValueAndCreateTime maxValueAndCreateTime = new MaxValueAndCreateTime();
                // 获取max_cur聚合结果
                ParsedMax maxCur = (ParsedMax) lineIdBucket.getAggregations().get("max_cur");
                maxValueAndCreateTime.setMaxValue(maxCur.getValue());
                // 获取top_hits聚合结果
                ParsedTopHits topHits = (ParsedTopHits) lineIdBucket.getAggregations().get("top_docs");
                if (topHits.getHits().getHits().length != 0) {
                    SearchHit topHit = topHits.getHits().getHits()[0]; // 取第一个top hit
                    Map<String, Object> sourceAsMap = topHit.getSourceAsMap();
                    maxValueAndCreateTime.setMaxTime(new DateTime(sourceAsMap.get("cur_max_time").toString(), "yyyy-MM-dd HH:mm:ss"));
                }

                // 将valueMap添加到lineIdMap中
                lineIdMap.put(lineId, maxValueAndCreateTime);
            }

            // 将lineIdMap添加到resultMap中
            resultMap.put(boxId, lineIdMap);
        }
        return resultMap;
    }

    private Map<Integer, Map<Integer, MaxValueAndCreateTime>> getBusLinePowMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("bus_id", ids))));

        builder.aggregation(
                AggregationBuilders.terms("group_by_bus_id")
                        .field("bus_id")
                        .size(10000)
                        .subAggregation(AggregationBuilders.terms("by_line_id")
                                .field("line_id")
                                .size(1000)
                                .order(BucketOrder.aggregation("max_pow", false))
                                .subAggregation(AggregationBuilders.max("max_pow").field("pow_active_max_value"))
                                .subAggregation(AggregationBuilders.topHits("top_docs")
                                        .size(1)
                                        .fetchSource(new String[]{"pow_active_max_time"}, null)
                                        .sort(SortBuilders.fieldSort("pow_active_max_value").order(SortOrder.DESC))))
        );
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(0);
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 初始化结果Map
        Map<Integer, Map<Integer, MaxValueAndCreateTime>> resultMap = new HashMap<>();
        // 获取group_by_box_id聚合结果
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_bus_id");
        for (Terms.Bucket boxIdBucket : groupByBoxId.getBuckets()) {
            Integer boxId = boxIdBucket.getKeyAsNumber().intValue();
            Map<Integer, MaxValueAndCreateTime> lineIdMap = new HashMap<>();
            // 获取by_line_id聚合结果
            Terms byLineId = boxIdBucket.getAggregations().get("by_line_id");
            for (Terms.Bucket lineIdBucket : byLineId.getBuckets()) {
                Integer lineId = lineIdBucket.getKeyAsNumber().intValue();
                MaxValueAndCreateTime maxValueAndCreateTime = new MaxValueAndCreateTime();
                // 获取max_cur聚合结果
                ParsedMax maxPow = (ParsedMax) lineIdBucket.getAggregations().get("max_pow");
                maxValueAndCreateTime.setMaxValue(maxPow.getValue());
                // 获取top_hits聚合结果
                ParsedTopHits topHits = (ParsedTopHits) lineIdBucket.getAggregations().get("top_docs");
                if (topHits.getHits().getHits().length != 0) {
                    SearchHit topHit = topHits.getHits().getHits()[0]; // 取第一个top hit
                    Map<String, Object> sourceAsMap = topHit.getSourceAsMap();
                    maxValueAndCreateTime.setMaxTime(new DateTime(sourceAsMap.get("pow_active_max_time").toString(), "yyyy-MM-dd HH:mm:ss"));
                }
                // 将valueMap添加到lineIdMap中
                lineIdMap.put(lineId, maxValueAndCreateTime);
            }
            // 将lineIdMap添加到resultMap中
            resultMap.put(boxId, lineIdMap);
        }
        return resultMap;
    }

    private String localDateTimeToString(LocalDateTime time) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
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
     * 获取设备位置
     *
     * @return
     */
    public void getPosition(List<? extends BusResBase> res) {
        if (CollectionUtils.isEmpty(res)) {
            return;
        }
        ValueOperations ops = redisTemplate.opsForValue();
        List<String> devKeyList = res.stream().map(BusResBase::getDevKey).collect(Collectors.toList());
        //设备位置

        //柜列
        List<AisleBar> aisleBar = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                .in(!CollectionUtils.isEmpty(devKeyList), AisleBar::getBusKey, devKeyList));
        Map<String, String> aislePathMap = aisleBar.stream().collect(Collectors.toMap(AisleBar::getBusKey, AisleBar::getPath));
        Map<String, Integer> aisleBarKeyMap = aisleBar.stream().collect(Collectors.toMap(AisleBar::getBusKey, AisleBar::getAisleId));
        Map<Integer, String> positonMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(aisleBar)) {
            Set<String> redisKeys = aisleBar.stream().map(aisle -> REDIS_KEY_AISLE + aisle.getAisleId()).collect(Collectors.toSet());
            List aisles = ops.multiGet(redisKeys);
            if (!CollectionUtils.isEmpty(aisleBar)) {
                for (Object aisle : aisles) {
                    if (aisle == null) {
                        continue;
                    }
                    JSONObject json = JSON.parseObject(JSON.toJSONString(aisle));
                    String devPosition;
                    devPosition = json.getString("room_name") + SPLIT_KEY
                            + json.getString("aisle_name") + SPLIT_KEY;
                    positonMap.put(json.getInteger("aisle_key"), devPosition);
                }
            }
        }
        res.forEach(bus -> {
            if (aisleBarKeyMap.get(bus.getDevKey()) != null) {
                Integer aisleId = aisleBarKeyMap.get(bus.getDevKey());
                bus.setLocation(positonMap.get(aisleId) + aislePathMap.get(bus.getDevKey()) + "路");
            }
        });
    }

    public Map<String, BusNameVO> getRoomByKeys(List<String> keys) {
        Map<String, BusNameVO> map = new HashMap<>();
        if (CollectionUtils.isEmpty(keys)) {
            return map;
        }
        ValueOperations ops = redisTemplate.opsForValue();
        //柜列
        List<AisleBar> aisleBar = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                .in(!CollectionUtils.isEmpty(keys), AisleBar::getBusKey, keys));
        Map<String, String> aislePathMap = aisleBar.stream().collect(Collectors.toMap(AisleBar::getBusKey, AisleBar::getPath));
        Map<String, Integer> aisleBarKeyMap = aisleBar.stream().collect(Collectors.toMap(AisleBar::getBusKey, AisleBar::getAisleId));
        if (!CollectionUtils.isEmpty(aisleBar)) {
            Set<String> redisKeys = aisleBar.stream().map(aisle -> REDIS_KEY_AISLE + aisle.getAisleId()).collect(Collectors.toSet());
            List aisles = ops.multiGet(redisKeys);
            Map<Integer, Object> aisleKey = (Map<Integer, Object>) aisles.stream().filter(i -> Objects.nonNull(i)).collect(Collectors.toMap(i -> JSON.parseObject(JSON.toJSONString(i)).getInteger("aisle_key"), Function.identity()));
            if (!CollectionUtils.isEmpty(aisleBar)) {
                for (String key : keys) {
                    Integer aisleId = aisleBarKeyMap.get(key);
                    Object obj = aisleKey.get(aisleId);
                    if (obj == null) {
                        continue;
                    }
                    BusNameVO vo = new BusNameVO();
                    JSONObject json = JSON.parseObject(JSON.toJSONString(obj));
                    vo.setRoomName(json.getString("room_name"));

                    StringJoiner joiner = new StringJoiner(SPLIT_KEY);
                    if (Objects.nonNull(json.getString("room_name"))) {
                        joiner.add(json.getString("room_name"));
                    } else {
                        joiner.add("未绑定");
                    }
                    joiner.add(json.getString("aisle_name"));
                    joiner.add(aislePathMap.get(key) + "路");

                    vo.setLocaltion(joiner.toString());
                    map.put(key, vo);
                }
            }
        }
        return map;
    }

    private Map getESTotalAndIds(String index, String startTime, String endTime, Integer pageSize, Integer pageNo) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder
                .from(pageNo * pageSize)
                .size(pageSize)
                .query(QueryBuilders.rangeQuery("create_time.keyword").gte(startTime).lte(endTime))
                .sort("bus_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("bus_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("bus_id").precisionThreshold(10000));
        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                BusLineHourDo hourDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                sortValues.add(hourDo.getBusId());
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
                        .must(QueryBuilders.termsQuery("bus_id", ids))))
                .sort("bus_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("bus_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("bus_id").precisionThreshold(10000));
        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                BusLineHourDo hourDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                sortValues.add(hourDo.getBusId());
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

    private List getMutiRedis(List<BusIndexDO> list) {
        List<String> devKeys = list.stream().map(busIndexDo -> REDIS_KEY_BUS + busIndexDo.getBusKey()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }
}