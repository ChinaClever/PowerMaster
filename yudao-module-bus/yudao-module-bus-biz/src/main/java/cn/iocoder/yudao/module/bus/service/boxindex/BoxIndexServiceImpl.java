package cn.iocoder.yudao.module.bus.service.boxindex;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEleTotalDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.box.line.BoxLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.box.line.BoxLineRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.box.tem.BoxTemHourDo;
import cn.iocoder.yudao.framework.common.entity.es.box.total.BoxTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.box.total.BoxTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBox;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetBox;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.constant.BoxConstants;
import cn.iocoder.yudao.module.bus.constant.BusConstants;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.MaxValueAndCreateTime;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO.BusAisleBarQueryVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxcurbalancecolor.BoxCurbalanceColorDO;
import cn.iocoder.yudao.module.bus.dal.mysql.boxcurbalancecolor.BoxCurbalanceColorMapper;
import cn.iocoder.yudao.module.bus.dal.mysql.boxharmoniccolor.BoxHarmonicColorMapper;
import cn.iocoder.yudao.module.bus.dal.mysql.boxindex.BoxIndexCopyMapper;
import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;
import cn.iocoder.yudao.module.bus.util.TimeUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
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

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.REDIS_KEY_AISLE;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.constant.BoxConstants.*;
import static cn.iocoder.yudao.module.bus.constant.BusConstants.SPLIT_KEY;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.INDEX_NOT_EXISTS;
import static cn.iocoder.yudao.module.bus.service.busindex.BusIndexServiceImpl.REDIS_KEY_CABINET;

/**
 * 始端箱索引 Service 实现类
 *
 * @author clever
 */
@Slf4j
@Service
@Validated
public class BoxIndexServiceImpl implements BoxIndexService {

    @Resource
    private BoxIndexCopyMapper boxIndexCopyMapper;
    @Autowired
    private BoxIndexMapper boxIndexMapper;
    @Resource
    private BoxCurbalanceColorMapper boxCurbalanceColorMapper;

    @Resource
    private BoxHarmonicColorMapper boxHarmonicColorMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private BusIndexMapper busIndexMapper;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private AisleBoxMapper aisleBoxMapper;

    @Autowired
    private CabinetBusMapper cabinetBusMapper;

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;

    @Autowired
    private AisleBarMapper aisleBarMapper;

    public static final String DAY_FORMAT = "dd";

    @Override
    public Long createIndex(BoxIndexSaveReqVO createReqVO) {
        // 插入
        BoxIndex index = BeanUtils.toBean(createReqVO, BoxIndex.class);
        boxIndexCopyMapper.insert(index);
        // 返回
        return new Long(index.getId());
    }

    @Override
    public void updateIndex(BoxIndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        BoxIndex updateObj = BeanUtils.toBean(updateReqVO, BoxIndex.class);
        boxIndexCopyMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Long id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        //boxIndexCopyMapper.deleteById(id);
        //逻辑删除
        boxIndexCopyMapper.update(new LambdaUpdateWrapper<BoxIndex>()
                .eq(BoxIndex::getId, id)
                .set(BoxIndex::getIsDeleted, DelEnums.DELETE.getStatus())
        );
    }

    @Override
    public void restoreIndex(Long id) {
        // 校验存在
        validateIndexExists(id);

        //逻辑恢复
        boxIndexCopyMapper.update(new LambdaUpdateWrapper<BoxIndex>()
                .eq(BoxIndex::getId, id)
                .set(BoxIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
        );
    }

    @Override
    public PageResult<BoxIndexRes> getDeletedPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxIndexRes> res = new ArrayList<>();
        for (BoxIndex boxIndexDO : list) {
            BoxIndexRes boxIndexRes = new BoxIndexRes();
            boxIndexRes.setStatus(boxIndexDO.getRunStatus());
            boxIndexRes.setBoxId(boxIndexDO.getId());
            boxIndexRes.setDevKey(boxIndexDO.getBoxKey());
            boxIndexRes.setBoxName(boxIndexDO.getBoxName());
            res.add(boxIndexRes);
        }
        getPosition(res);
        return new PageResult<>(res, boxIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BusCurLinePageResVO> getBoxLineCurLinePage(BoxIndexPageReqVO pageReqVO) throws IOException {
        PageResult<BusCurLinePageResVO> page = new PageResult<>();
        String startTime;
        String endTime;
        String key;
        if (pageReqVO.getTimeType() == 0) {
            key = BOX_HDA_LINE_HOUR;
            startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
            endTime = localDateTimeToString(LocalDateTime.now());
        } else {
            startTime = localDateTimeToString(pageReqVO.getOldTime());
            endTime = localDateTimeToString(pageReqVO.getNewTime());
            key = BOX_HDA_LINE_DAY;
        }
        BoxIndex boxIndex = boxIndexCopyMapper.selectById(pageReqVO.getBoxId());
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
                .must(QueryBuilders.termQuery("box_id", pageReqVO.getBoxId()))));
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

                resVO.setBusId(boxIndex.getId()).setDevKey(boxIndex.getBoxKey());
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
    public List<BusCurLinePageResVO> getBoxLineCurLineExcel(BoxIndexPageReqVO pageReqVO) throws IOException {
        String startTime;
        String endTime;
        String key;
        if (pageReqVO.getTimeType() == 0) {
            key = BOX_HDA_LINE_HOUR;
            startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
            endTime = localDateTimeToString(LocalDateTime.now());
        } else {
            startTime = localDateTimeToString(pageReqVO.getOldTime());
            endTime = localDateTimeToString(pageReqVO.getNewTime());
            key = BOX_HDA_LINE_DAY;
        }
        BoxIndex boxIndex = boxIndexCopyMapper.selectById(pageReqVO.getBoxId());

        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(key);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        builder.trackTotalHits(true);
        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termQuery("box_id", pageReqVO.getBoxId()))));
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

                resVO.setBusId(boxIndex.getId()).setDevKey(boxIndex.getBoxKey());
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

    @Override
    public Map getAvgBoxHdaLineForm(BoxIndexPageReqVO pageReqVO) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, pageReqVO.getDevKey()));
        if (boxIndex != null) {
            String index;
            if (pageReqVO.getTimeType().equals(0)) {
                index = "bus_hda_line_hou r";
            } else {
                index = "bus_hda_line_day";
            }
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            builder.trackTotalHits(true);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(pageReqVO.getOldTime()).lte(pageReqVO.getNewTime()))
                    .must(QueryBuilders.termQuery("bus_id", boxIndex.getId()))));
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
            dateTimes.stream().distinct().collect(Collectors.toList());
            map.put("A", dayList1);
            map.put("B", dayList2);
            map.put("C", dayList3);
            map.put("dateTimes", dateTimes);
        }
        return map;
    }

    @Override
    public LineBoxMaxResVO getBoxLineMax(BusIndexPageReqVO pageReqVO) throws IOException {
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
            index = "box_hda_line_hour";
        } else {
            index = "box_hda_line_day";
        }
        String startTime = localDateTimeToString(pageReqVO.getOldTime());
        String endTime = localDateTimeToString(pageReqVO.getNewTime());

        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.size(1);
        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                .gte(startTime).lt(endTime))));
        builder.sort("cur_max_value", SortOrder.DESC);
//        builder.aggregation(AggregationBuilders.max("max_date").field("cur_max_value"));
        // 设置搜索条件
        searchRequest.source(builder);
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        LineBoxMaxResVO resVO = JsonUtils.parseObject(searchResponse.getHits().getAt(0).getSourceAsString(), LineBoxMaxResVO.class);
        if (Objects.nonNull(resVO)) {
            BoxIndex boxIndex = boxIndexCopyMapper.selectById(resVO.getBusId());
            resVO.setDevKey(boxIndex.getBoxKey());
            resVO.setBusName(boxIndex.getBoxName());
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

            List<BusAisleBarQueryVO> records = busIndexMapper.selectBoxPageList(boxIndex.getBoxKey().split(","));
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
                Integer aisleId = keyMap.get(boxIndex.getBoxKey());
                String localtion = positonMap.get(aisleId);
                if (Objects.nonNull(aislePathMap.get(boxIndex.getBoxKey()).getPath())) {
                    resVO.setLocation(localtion + aislePathMap.get(boxIndex.getBoxKey()).getPath() + "路");
                } else {
                    resVO.setLocation(localtion + "路");
                }
            }
        }
        return resVO;
    }

    @Override
    public BusIndexStatisticsResVO getBoxIndexStatistics() {
        return boxIndexCopyMapper.getBoxIndexStatistics();
    }

    @Override
    public BoxBalanceStatisticsVO getBoxBalanceStatistics() {
        return boxIndexCopyMapper.getBoxBalanceStatistics();
    }


    private void validateIndexExists(Long id) {
        if (boxIndexCopyMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public BoxIndex getIndex(Long id) {
        return boxIndexCopyMapper.selectById(id);
    }

    @Override
    public PageResult<BoxIndexRes> getIndexPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxIndexRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (BoxIndex boxIndexDO : list) {
            BoxIndexRes boxIndexRes = new BoxIndexRes();
            boxIndexRes.setStatus(boxIndexDO.getRunStatus());
            boxIndexRes.setBoxId(boxIndexDO.getId());
            boxIndexRes.setDevKey(boxIndexDO.getBoxKey());
            boxIndexRes.setBoxName(boxIndexDO.getBoxName());
            res.add(boxIndexRes);
        }
        Map<String, BoxIndexRes> resMap = res.stream().collect(Collectors.toMap(BoxIndexRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            if (jsonObject.getJSONObject("box_data").getJSONObject("box_cfg").getInteger("box_type") == 1) {
                continue;
            }
            String devKey = jsonObject.getString("dev_ip") + "-" + jsonObject.getString("bar_id") + "-" + jsonObject.getString("addr");
            BoxIndexRes boxIndexRes = resMap.get(devKey);
            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
            JSONArray loadRate = lineItemList.getJSONArray("load_rate");
            List<Double> rateList = loadRate.toList(Double.class);
            if (rateList.size() > 1) {
                boxIndexRes.setALoadRate(loadRate.getInteger(0));
                boxIndexRes.setBLoadRate(loadRate.getInteger(1));
                boxIndexRes.setCLoadRate(loadRate.getInteger(2));
            } else {
                boxIndexRes.setALoadRate(loadRate.getInteger(0));
            }
            rateList.sort(Collections.reverseOrder());
            Double biggest = rateList.get(0);
            if (biggest == 0) {
                boxIndexRes.setColor(0);
            } else if (biggest / 100 < 30) {
                boxIndexRes.setColor(1);
            } else if (biggest / 100 < 60) {
                boxIndexRes.setColor(2);
            } else if (biggest / 100 < 90) {
                boxIndexRes.setColor(3);
            } else if (biggest / 100 >= 90) {
                boxIndexRes.setColor(4);
            }
            if (pageReqVO.getColor() != null) {
                if (!pageReqVO.getColor().contains(boxIndexRes.getColor())) {
                    res.removeIf(box -> box.getBoxId().equals(boxIndexRes.getBoxId()));
                }
            }
        }
        return new PageResult<>(res, boxIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BoxRedisDataRes> getBoxRedisPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxRedisDataRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (BoxIndex boxIndexDO : list) {
            BoxRedisDataRes boxRedisDataRes = new BoxRedisDataRes();
            boxRedisDataRes.setStatus(boxIndexDO.getRunStatus());
            boxRedisDataRes.setBoxId(boxIndexDO.getId());
            boxRedisDataRes.setDevKey(boxIndexDO.getBoxKey());
            boxRedisDataRes.setBoxName(boxIndexDO.getBoxName());
            res.add(boxRedisDataRes);
        }
        Map<String, BoxRedisDataRes> resMap = res.stream().collect(Collectors.toMap(BoxRedisDataRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            if (jsonObject.getJSONObject("box_data").getJSONObject("box_cfg").getInteger("box_type") == 1) {
                continue;
            }
            String devKey = jsonObject.getString("dev_ip") + "-" + jsonObject.getString("bar_id") + "-" + jsonObject.getString("addr");
            BoxRedisDataRes boxRedisDataRes = resMap.get(devKey);
            //相数据解析
            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");

            JSONArray volValue = lineItemList.getJSONArray("vol_value");
            JSONArray curValue = lineItemList.getJSONArray("cur_value");
            JSONArray powValue = lineItemList.getJSONArray("pow_active");
            JSONArray powReactive = lineItemList.getJSONArray("pow_reactive");
            JSONArray powApparent = lineItemList.getJSONArray("pow_apparent");
            JSONArray powFactor = lineItemList.getJSONArray("power_factor");

            List<Double> phaseVolValue = new ArrayList<>();
            List<Double> phaseCurValue = new ArrayList<>();
            List<Double> phasePowValue = new ArrayList<>();
            List<Double> phaseReactivePowValue = new ArrayList<>();
            List<Double> phaseApparentPowValue = new ArrayList<>();
            List<Double> phasePowFactor = new ArrayList<>();

            phaseVolValue = volValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            phaseCurValue = curValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            phasePowValue = powValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            phaseReactivePowValue = powReactive.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            phaseApparentPowValue = powApparent.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            phasePowFactor = powFactor.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());


            boxRedisDataRes.setPhaseVol(phaseVolValue);
            boxRedisDataRes.setPhaseCur(phaseCurValue);
            boxRedisDataRes.setPhaseActivePow(phasePowValue);
            boxRedisDataRes.setPhaseReactivePow(phaseReactivePowValue);
            boxRedisDataRes.setPhaseApparentPow(phaseApparentPowValue);
            boxRedisDataRes.setPhasePowFactor(phasePowFactor);
            //回路数据解析
            JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");
            volValue = loopItemList.getJSONArray("vol_value");
            JSONArray volStatus = loopItemList.getJSONArray("vol_status");
            curValue = loopItemList.getJSONArray("cur_value");
            JSONArray curStatus = loopItemList.getJSONArray("cur_status");
            powValue = loopItemList.getJSONArray("pow_value");
            JSONArray powStatus = loopItemList.getJSONArray("pow_status");
            powReactive = loopItemList.getJSONArray("pow_reactive");

            List<String> loopCurColor = new ArrayList<>();
            List<String> loopVolColor = new ArrayList<>();
            List<String> loopPowColor = new ArrayList<>();

            List<Double> loopCurValue;
            List<Integer> loopCurStatus;
            List<Double> loopVolValue;
            List<Integer> loopVolStatus;
            List<Double> loopPowValue;
            List<Integer> loopPowStatus;
            List<Double> loopReactivePowValue;


            loopCurValue = curValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            loopCurStatus = curStatus.stream().mapToInt(value -> Integer.parseInt(value.toString())).boxed().collect(Collectors.toList());

            loopVolValue = volValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            loopVolStatus = volStatus.stream().mapToInt(value -> Integer.parseInt(value.toString())).boxed().collect(Collectors.toList());

            loopPowValue = powValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            loopPowStatus = powStatus.stream().mapToInt(value -> Integer.parseInt(value.toString())).boxed().collect(Collectors.toList());

            loopReactivePowValue = powReactive.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());

            for (Integer curState : loopCurStatus) {
                if (curState != 0) {
                    loopCurColor.add("red");
                }
            }
            for (Integer volState : loopVolStatus) {
                if (volState != 0) {
                    loopVolColor.add("red");
                }
            }
            for (Integer powState : loopPowStatus) {
                if (powState != 0) {
                    loopPowColor.add("red");
                }
            }
            boxRedisDataRes.setLoopCur(loopCurValue);
            boxRedisDataRes.setLoopCurStatus(loopCurStatus);
            boxRedisDataRes.setLoopCurColor(loopCurColor);
            boxRedisDataRes.setLoopVol(loopVolValue);
            boxRedisDataRes.setLoopVolStatus(loopVolStatus);
            boxRedisDataRes.setLoopVolColor(loopVolColor);
            boxRedisDataRes.setLoopActivePow(loopPowValue);
            boxRedisDataRes.setLoopActivePowStatus(loopPowStatus);
            boxRedisDataRes.setLoopActivePowColor(loopPowColor);
            boxRedisDataRes.setLoopReactivePow(loopReactivePowValue);

            //输出位数据解析
            JSONObject outletItemList = jsonObject.getJSONObject("box_data").getJSONObject("outlet_item_list");
            powValue = outletItemList.getJSONArray("pow_active");
            JSONArray reactivePowValue = outletItemList.getJSONArray("pow_reactive");
            JSONArray apparentPowValue = outletItemList.getJSONArray("pow_apparent");
            powFactor = outletItemList.getJSONArray("power_factor");

            List<Double> outletPowValue;
            List<Double> outletReactivePowValue;
            List<Double> outletApparentPowValue;
            List<Double> outletPowFactor;

            outletPowValue = powValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            outletReactivePowValue = reactivePowValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            outletApparentPowValue = apparentPowValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            outletPowFactor = powFactor.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());

            boxRedisDataRes.setOutletActivePow(outletPowValue);
            boxRedisDataRes.setOutletReactivePow(outletReactivePowValue);
            boxRedisDataRes.setOutletApparentPow(outletApparentPowValue);
            boxRedisDataRes.setOutletPowFactor(outletPowFactor);
        }
        return new PageResult<>(res, boxIndexDOPageResult.getTotal());
    }

    @Override
    public PowerRedisDataRes getBoxPowerRedisData(String devKey) {
        PowerRedisDataRes result = new PowerRedisDataRes();
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get(REDIS_KEY_BOX + devKey);
        if (jsonObject == null) {
            return result;
        }
        JSONArray temArr = jsonObject.getJSONObject("env_item_list").getJSONArray("tem_value");
        result.setTempA(temArr.getDouble(0));
        result.setTempB(temArr.getDouble(1));
        result.setTempC(temArr.getDouble(2));
        result.setTempN(temArr.getDouble(3));
        JSONObject boxTotalData = jsonObject.getJSONObject("box_data").getJSONObject("box_total_data");
        JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");
        JSONArray volValue = loopItemList.getJSONArray("vol_value");
        JSONArray curValue = loopItemList.getJSONArray("cur_value");
        List<Double> curList = curValue.toList(Double.class);
        List<Double> volList = volValue.toList(Double.class);
        curList.sort(Comparator.naturalOrder());
        volList.sort(Comparator.naturalOrder());
        Double curAvg = (curList.get(0) + curList.get(1) + curList.get(2)) / 3;
        Double volAvg = (volList.get(0) + volList.get(1) + volList.get(2)) / 3;
        Double curUnbalance = curAvg == 0 ? 0 : (curList.get(2) - curAvg) / curAvg * 100;
        Double volUnbalance = volAvg == 0 ? 0 : (volList.get(2) - volList.get(0)) / volAvg * 100;
        result.setVub(curUnbalance);
        result.setCub(volUnbalance);
        result.setPf(boxTotalData.getDouble("power_factor"));
        result.setS(boxTotalData.getDouble("pow_apparent"));
        result.setP(boxTotalData.getDouble("pow_active"));
        result.setQ(boxTotalData.getDouble("pow_reactive"));
        result.setUpdateTime(jsonObject.getString("datetime"));
        JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
        result.setIa(curValue.getDouble(0));
        result.setIb(curValue.getDouble(1));
        result.setIc(curValue.getDouble(2));
        result.setUa(volValue.getDouble(0));
        result.setUb(volValue.getDouble(1));
        result.setUc(volValue.getDouble(2));
        result.setUab(result.getUa() * 1.732);
        result.setUbc(result.getUb() * 1.732);
        result.setUca(result.getUc() * 1.732);
        JSONArray curMax = loopItemList.getJSONArray("cur_max");
        JSONArray volMax = loopItemList.getJSONArray("vol_max");
        Double fInstalledCapacity = 0D;
        for (int i = 0; i < 3; i++) {
            fInstalledCapacity += curMax.getDouble(i) * volMax.getDouble(i);
        }
        result.setFInstalledCapacity(fInstalledCapacity / 1000);
        BoxIndexPageReqVO reqVO = new BoxIndexPageReqVO();
        reqVO.setDevKey(devKey);
        reqVO.setTimeType(0);

        List<BoxLineRes> list = getBoxLineDevicePage(reqVO).getList();
        if (!list.isEmpty()) {
            BoxLineRes boxLineRes = list.get(0);
            result.setMd(boxLineRes.getL1MaxPow().doubleValue() + boxLineRes.getL2MaxPow().doubleValue() + boxLineRes.getL3MaxPow().doubleValue());
        } else {
            result.setMd(0.0);
        }

        JSONArray curThd = lineItemList.getJSONArray("cur_thd");
        result.setIaTHD(curThd.getDouble(0));
        result.setIbTHD(curThd.getDouble(1));
        result.setIcTHD(curThd.getDouble(2));
        result.setLoadFactor((result.getP() / result.getFInstalledCapacity()) * 100);
        return result;
    }

    @Override
    public BusLineResBase getBoxLoadRateLine(BoxIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        try {
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<Integer> ids = Arrays.asList(pageReqVO.getBoxId());
            List<String> boxHdaLineRealtime = getData(startTime, endTime, ids, "box_hda_line_realtime");
            Map<Integer, List<BoxLineRealtimeDo>> lineMap = boxHdaLineRealtime.stream()
                    .map(str -> JsonUtils.parseObject(str, BoxLineRealtimeDo.class))
                    .collect(Collectors.groupingBy(BoxLineRealtimeDo::getLineId));
            boolean first = false;
            for (int i = 1; i < 4; i++) {
                if (lineMap.get(i) != null) {
                    List<BoxLineRealtimeDo> boxLineRealtimeDos = lineMap.get(i);
                    List<Float> loadRate = boxLineRealtimeDos.stream().map(BoxLineRealtimeDo::getLoadRate).collect(Collectors.toList());
                    LineSeries lineSeries = new LineSeries();
                    if (!first) {
                        List<String> time = boxLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                        result.setTime(time);
                    }
                    if (i == 1) {
                        lineSeries.setName("A相负载率");
                    } else if (i == 2) {
                        lineSeries.setName("B相负载率");
                    } else {
                        lineSeries.setName("C相负载率");
                    }
                    lineSeries.setData(loadRate);
                    result.getSeries().add(lineSeries);
                }
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public BusLineResBase getBoxPowActiveLine(BoxIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        try {
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<Integer> ids = Arrays.asList(pageReqVO.getBoxId());
            List<String> boxHdaLineRealtime = getData(startTime, endTime, ids, "box_hda_line_realtime");
            List<String> boxHdaTotalRealtime = getData(startTime, endTime, ids, "box_hda_total_realtime");
            LineSeries lineSeries = new LineSeries();
            lineSeries.setName("P");
            boxHdaTotalRealtime.forEach(str -> {
                BoxTotalRealtimeDo esDo = JsonUtils.parseObject(str, BoxTotalRealtimeDo.class);
                lineSeries.getData().add(esDo.getPowActive());
            });
            result.getSeries().add(lineSeries);
            Map<Integer, List<BoxLineRealtimeDo>> lineMap = boxHdaLineRealtime.stream()
                    .map(str -> JsonUtils.parseObject(str, BoxLineRealtimeDo.class))
                    .collect(Collectors.groupingBy(BoxLineRealtimeDo::getLineId));
            boolean first = false;
            for (int i = 1; i < 4; i++) {
                if (lineMap.get(i) != null) {
                    List<BoxLineRealtimeDo> boxLineRealtimeDos = lineMap.get(i);
                    List<Float> powActive = boxLineRealtimeDos.stream().map(BoxLineRealtimeDo::getPowActive).collect(Collectors.toList());
                    LineSeries series = new LineSeries();
                    if (!first) {
                        List<String> time = boxLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                        result.setTime(time);
                    }
                    if (i == 1) {
                        series.setName("Pa");
                    } else if (i == 2) {
                        series.setName("Pb");
                    } else {
                        series.setName("Pc");
                    }
                    series.setData(powActive);
                    result.getSeries().add(series);
                }
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public BusLineResBase getBoxPowReactiveLine(BoxIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        try {
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<Integer> ids = Arrays.asList(pageReqVO.getBoxId());
            List<String> boxHdaLineRealtime = getData(startTime, endTime, ids, "box_hda_line_realtime");
            List<String> boxHdaTotalRealtime = getData(startTime, endTime, ids, "box_hda_total_realtime");
            LineSeries lineSeries = new LineSeries();
            lineSeries.setName("Q");
            boxHdaTotalRealtime.forEach(str -> {
                BoxTotalRealtimeDo esDo = JsonUtils.parseObject(str, BoxTotalRealtimeDo.class);
                lineSeries.getData().add(esDo.getPowReactive());
            });
            result.getSeries().add(lineSeries);
            Map<Integer, List<BoxLineRealtimeDo>> lineMap = boxHdaLineRealtime.stream()
                    .map(str -> JsonUtils.parseObject(str, BoxLineRealtimeDo.class))
                    .collect(Collectors.groupingBy(BoxLineRealtimeDo::getLineId));
            boolean first = false;
            for (int i = 1; i < 4; i++) {
                if (lineMap.get(i) != null) {
                    List<BoxLineRealtimeDo> boxLineRealtimeDos = lineMap.get(i);
                    List<Float> powReactive = boxLineRealtimeDos.stream().map(BoxLineRealtimeDo::getPowReactive).collect(Collectors.toList());
                    LineSeries series = new LineSeries();
                    if (!first) {
                        List<String> time = boxLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
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
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }


    @Override
    public PageResult<BoxIndexDTO> getEqPage(BoxIndexPageReqVO pageReqVO) {
        try {
            PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
            List<BoxIndex> boxIndexDOList = boxIndexDOPageResult.getList();
            List<BoxIndexDTO> result = new ArrayList<>();
            List<Integer> ids = boxIndexDOList.stream().map(BoxIndex::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                return new PageResult<>(result, boxIndexDOPageResult.getTotal());
            }
            //昨日
            boxIndexDOList.forEach(boxIndex -> {
                BoxIndexDTO boxIndexDTO = new BoxIndexDTO().setId(boxIndex.getId()).setRunStatus(boxIndex.getRunStatus());
                boxIndexDTO.setDevKey(boxIndex.getBoxKey());
                boxIndexDTO.setBoxName(boxIndex.getBoxName());
                result.add(boxIndexDTO);
            });
            String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            String endTime = DateUtil.formatDateTime(DateTime.now());
            List<String> yesterdayList = getData(startTime, endTime, ids, "box_eq_total_day");
            Map<Integer, Double> yesterdayMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(yesterdayList)) {
                yesterdayList.forEach(str -> {
                    BoxEqTotalDayDo dayDo = JsonUtils.parseObject(str, BoxEqTotalDayDo.class);
                    yesterdayMap.put(dayDo.getBoxId(), dayDo.getEq());
                });
            }

            //上周
            startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());
            List<String> weekList = getData(startTime, endTime, ids, "box_eq_total_week");
            Map<Integer, Double> weekMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(weekList)) {
                weekList.forEach(str -> {
                    BoxEqTotalWeekDo weekDo = JsonUtils.parseObject(str, BoxEqTotalWeekDo.class);
                    weekMap.put(weekDo.getBoxId(), weekDo.getEq());
                });
            }

            //上月
            startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());
            List<String> monthList = getData(startTime, endTime, ids, "box_eq_total_month");
            Map<Integer, Double> monthMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(monthList)) {
                monthList.forEach(str -> {
                    BoxEqTotalMonthDo monthDo = JsonUtils.parseObject(str, BoxEqTotalMonthDo.class);
                    monthMap.put(monthDo.getBoxId(), monthDo.getEq());
                });
            }

            result.forEach(dto -> {
                dto.setYesterdayEq(yesterdayMap.get(dto.getId()));
                dto.setLastWeekEq(weekMap.get(dto.getId()));
                dto.setLastMonthEq(monthMap.get(dto.getId()));
                if (dto.getYesterdayEq() == null) {
                    dto.setYesterdayEq(0.0);
                }
                if (dto.getLastWeekEq() == null) {
                    dto.setLastWeekEq(0.0);
                }
                if (dto.getLastMonthEq() == null) {
                    dto.setLastMonthEq(0.0);
                }
            });
            getPosition(result);
            if (pageReqVO.getTimeGranularity().equals("yesterday")) {
                result.sort(Comparator.comparing(BoxIndexDTO::getYesterdayEq).reversed());
            } else if (pageReqVO.getTimeGranularity().equals("lastWeek")) {
                result.sort(Comparator.comparing(BoxIndexDTO::getLastWeekEq).reversed());
            } else if (pageReqVO.getTimeGranularity().equals("lastMonth")) {
                result.sort(Comparator.comparing(BoxIndexDTO::getLastMonthEq).reversed());
            }
            return new PageResult<>(result, boxIndexDOPageResult.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public PageResult<BoxIndexDTO> getMaxEq(BoxIndexPageReqVO pageReqVO) {
        try {
            PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
            List<BoxIndex> boxIndexDOList = boxIndexDOPageResult.getList();
            List<BoxIndexDTO> result = new ArrayList<>();
            List<Integer> ids = boxIndexDOList.stream().map(BoxIndex::getId).collect(Collectors.toList());
            String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant())));
            String endTime = DateUtil.formatDateTime(DateUtil.endOfDay(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant())));

            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest("box_eq_total_day");
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termsQuery("box_id", ids))));
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
                BoxIndexDTO boxIndexDTO = new BoxIndexDTO();
                boxIndexDTO.setYesterdayEq((Double) sourceAsMap.get("eq_value"));
                boxIndexDTO.setBoxId((Integer) sourceAsMap.get("box_id"));
                BoxIndex boxIndex = boxIndexCopyMapper.selectOne(BoxIndex::getId, boxIndexDTO.getBoxId());
                boxIndexDTO.setDevKey(boxIndex.getBoxKey());
                boxIndexDTO.setBoxName(boxIndex.getBoxName());
                boxIndexDTO.setId(0);//借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
                result.add(boxIndexDTO);
            }


            //上周
            String startTime1 = DateUtil.formatDateTime(DateUtil.beginOfWeek(Date.from(LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant())));
            String endTime1 = DateUtil.formatDateTime(DateUtil.endOfWeek(Date.from(LocalDateTime.now().minusWeeks(1).atZone(ZoneId.systemDefault()).toInstant())));
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest1 = new SearchRequest("box_eq_total_week");
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder1 = new SearchSourceBuilder();

            //获取需要处理的数据
            builder1.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime1).lte(endTime1))
                    .must(QueryBuilders.termsQuery("box_id", ids))));
            builder1.sort("eq_value", SortOrder.DESC);
            // 设置搜索条件
            searchRequest1.source(builder1);
            builder1.size(1);
            // 执行ES请求
            SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
            SearchHit[] hits1 = searchResponse1.getHits().getHits();
            if (hits1.length > 0) {
                // 获取最大值和时间字段
                Map<String, Object> sourceAsMap = hits1[0].getSourceAsMap();
                BoxIndexDTO boxIndexDTO = new BoxIndexDTO();
                boxIndexDTO.setLastWeekEq((Double) sourceAsMap.get("eq_value"));
                boxIndexDTO.setBoxId((Integer) sourceAsMap.get("box_id"));
                BoxIndex boxIndexDO = boxIndexCopyMapper.selectOne(BoxIndex::getId, boxIndexDTO.getBoxId());
                boxIndexDTO.setDevKey(boxIndexDO.getBoxKey());
                boxIndexDTO.setBoxName(boxIndexDO.getBoxName());
                boxIndexDTO.setId(1);//借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
                result.add(boxIndexDTO);
            }

            //上月
            String startTime2 = DateUtil.formatDateTime(DateUtil.beginOfMonth(Date.from(LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toInstant())));
            String endTime2 = DateUtil.formatDateTime(DateUtil.endOfMonth(Date.from(LocalDateTime.now().minusMonths(1).atZone(ZoneId.systemDefault()).toInstant())));
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest2 = new SearchRequest("box_eq_total_month");
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder2 = new SearchSourceBuilder();

            //获取需要处理的数据
            builder2.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime2).lte(endTime2))
                    .must(QueryBuilders.termsQuery("box_id", ids))));
            builder2.sort("eq_value", SortOrder.DESC);
            // 设置搜索条件
            searchRequest2.source(builder2);
            builder2.size(1);

            // 执行ES请求
            SearchResponse searchResponse2 = client.search(searchRequest2, RequestOptions.DEFAULT);
            SearchHit[] hits2 = searchResponse2.getHits().getHits();
            if (hits2.length > 0) {
                // 获取最大值和时间字段
                Map<String, Object> sourceAsMap = hits2[0].getSourceAsMap();
                BoxIndexDTO boxIndexDTO = new BoxIndexDTO();
                boxIndexDTO.setLastMonthEq((Double) sourceAsMap.get("eq_value"));
                boxIndexDTO.setBoxId((Integer) sourceAsMap.get("box_id"));
                BoxIndex boxIndexDO = boxIndexCopyMapper.selectOne(BoxIndex::getId, boxIndexDTO.getBoxId());
                boxIndexDTO.setDevKey(boxIndexDO.getBoxKey());
                boxIndexDTO.setBoxName(boxIndexDO.getBoxName());
                boxIndexDTO.setId(2);//借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
                result.add(boxIndexDTO);
            }
            getPosition(result);
            return new PageResult<>(result, boxIndexDOPageResult.getTotal());
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
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

            log.info("startTime : " + startTime + "endTime：" + endTime);
            //获取昨日数据
            List<String> yesterdayData = getData(startTime, endTime, vo, "box_hda_total_hour");


            List<BusActivePowTrendDTO> yesterdayList = new ArrayList<>();
            yesterdayData.forEach(str -> {
                BoxTotalHourDo hourDo = JsonUtils.parseObject(str, BoxTotalHourDo.class);
                BusActivePowTrendDTO dto = new BusActivePowTrendDTO();
                dto.setActivePow(hourDo.getPowActiveAvgValue());
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH") + TIME_STR;
                dto.setDateTime(dateTime);
//                log.info("dateTime : " + dateTime );
                yesterdayList.add(dto);
            });


            startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());

            log.info("startTime : " + startTime + "endTime：" + endTime);
            //获取今日数据
            List<BusActivePowTrendDTO> todayList = new ArrayList<>();

            List<String> todayData = getData(startTime, endTime, vo, "box_hda_total_hour");
            todayData.forEach(str -> {
                BoxTotalHourDo hourDo = JsonUtils.parseObject(str, BoxTotalHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH") + TIME_STR;
                BusActivePowTrendDTO dto = new BusActivePowTrendDTO();
                if (Objects.isNull(dto)) {
                    dto = new BusActivePowTrendDTO();
                }
                dto.setActivePow(hourDo.getPowActiveAvgValue());
                dto.setDateTime(dateTime);
//                log.info("dateTime : " + dateTime );
                todayList.add(dto);
            });

            powDTO.setYesterdayList(yesterdayList);
            powDTO.setTodayList(todayList);
            //获取峰值
            BusActivePowTrendDTO yesterdayMax = yesterdayList.stream().max(Comparator.comparing(BusActivePowTrendDTO::getActivePow)).orElse(new BusActivePowTrendDTO());
            BusActivePowTrendDTO todayMax = todayList.stream().max(Comparator.comparing(BusActivePowTrendDTO::getActivePow)).orElse(new BusActivePowTrendDTO());
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
    public BusEleChainDTO getEleChain(int id) {
        BusEleChainDTO chainDTO = new BusEleChainDTO();
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
    public PageResult<BoxBalanceDataRes> getBoxBalancePage(BoxIndexPageReqVO pageReqVO) {

        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        BoxCurbalanceColorDO boxCurbalanceColorDO = boxCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxBalanceDataRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (BoxIndex boxIndexDO : list) {
            BoxBalanceDataRes boxBalanceDataRes = new BoxBalanceDataRes();
            boxBalanceDataRes.setStatus(boxIndexDO.getRunStatus());
            boxBalanceDataRes.setBoxId(boxIndexDO.getId());
            boxBalanceDataRes.setDevKey(boxIndexDO.getBoxKey());
            boxBalanceDataRes.setBoxName(boxIndexDO.getBoxName());
            res.add(boxBalanceDataRes);
        }
        Map<String, BoxBalanceDataRes> resMap = res.stream().collect(Collectors.toMap(BoxBalanceDataRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            if (jsonObject.getJSONObject("box_data").getJSONObject("box_cfg").getInteger("box_type") == 1) {
                continue;
            }
            String devKey = jsonObject.getString("dev_ip") + "-" + jsonObject.getString("bar_id") + "-" + jsonObject.getString("addr");
            BoxBalanceDataRes boxBalanceDataRes = resMap.get(devKey);
            JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");
            JSONArray volValue = loopItemList.getJSONArray("vol_value");
            JSONArray curValue = loopItemList.getJSONArray("cur_value");
            List<Double> curList = curValue.toList(Double.class);
            List<Double> volList = volValue.toList(Double.class);
            curList.sort(Comparator.naturalOrder());
            volList.sort(Comparator.naturalOrder());
            Double curAvg = (curList.get(0) + curList.get(1) + curList.get(2)) / 3;
            Double volAvg = (volList.get(0) + volList.get(1) + volList.get(2)) / 3;
            Double curUnbalance = curAvg == 0 ? 0 : (curList.get(2) - curAvg) / curAvg * 100;
            Double volUnbalance = volAvg == 0 ? 0 : (volList.get(2) - volList.get(0)) / volAvg * 100;
            JSONArray curAlarmArr = loopItemList.getJSONArray("cur_max");
            List<Double> sortAlarmArr = curAlarmArr.toList(Double.class);
            sortAlarmArr.sort(Collections.reverseOrder());
            double maxVal = sortAlarmArr.get(0);
            List<Double> temp = curValue.toList(Double.class);
            temp.sort(Collections.reverseOrder());
            double a = temp.get(0) - temp.get(2);
            int color;
            for (int i = 0; i < 3; i++) {
                double vol = volValue.getDoubleValue(i);
                double cur = curValue.getDoubleValue(i);
                if (i == 0) {
                    boxBalanceDataRes.setACur(cur);
                    boxBalanceDataRes.setAVol(vol);
                } else if (i == 1) {
                    boxBalanceDataRes.setBCur(cur);
                    boxBalanceDataRes.setBVol(vol);
                } else if (i == 2) {
                    boxBalanceDataRes.setCCur(cur);
                    boxBalanceDataRes.setCVol(vol);
                }
            }
            if (boxCurbalanceColorDO == null) {
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
                    if (curUnbalance < boxCurbalanceColorDO.getRangeOne()) {
                        color = 2;
                    } else if (curUnbalance < boxCurbalanceColorDO.getRangeFour()) {
                        color = 3;
                    } else {
                        color = 4;
                    }
                } else {
                    color = 1;
                }
            }
            boxBalanceDataRes.setCurUnbalance(curUnbalance);
            boxBalanceDataRes.setVolUnbalance(volUnbalance);
            boxBalanceDataRes.setColor(color);
            if (pageReqVO.getColor() != null) {
                if (!pageReqVO.getColor().contains(boxBalanceDataRes.getColor())) {
                    res.removeIf(box -> box.getBoxId().equals(boxBalanceDataRes.getBoxId()));
                }
            }
        }
        return new PageResult<>(res, boxIndexDOPageResult.getTotal());
    }

    @Override
    public BusBalanceDeatilRes getBoxBalanceDetail(String devKey) {
        BusBalanceDeatilRes result = new BusBalanceDeatilRes();
        BoxCurbalanceColorDO boxCurbalanceColorDO = boxCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + devKey);
        if (jsonObject == null) {
            return result;
        }
        JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");
        JSONArray curValue = loopItemList.getJSONArray("cur_value");
        JSONArray volValue = loopItemList.getJSONArray("vol_value");
        List<Double> curList = curValue.toList(Double.class);
        List<Double> volList = volValue.toList(Double.class);
        curList.sort(Comparator.naturalOrder());
        volList.sort(Comparator.naturalOrder());
        Double curAvg = (curList.get(0) + curList.get(1) + curList.get(2)) / 3;
        Double volAvg = (volList.get(0) + volList.get(1) + volList.get(2)) / 3;
        Double curUnbalance = curAvg == 0 ? 0 : (curList.get(2) - curAvg) / curAvg * 100;
        Double volUnbalance = volAvg == 0 ? 0 : (volList.get(2) - volList.get(0)) / volAvg * 100;
        result.setCur_value(curValue.toArray(Float.class));
        result.setVol_value(volValue.toArray(Float.class));
        result.setCurUnbalance(curUnbalance);
        result.setVolUnbalance(volUnbalance);
        JSONArray curAlarmArr = loopItemList.getJSONArray("cur_max");
        List<Double> sortAlarmArr = curAlarmArr.toList(Double.class);
        sortAlarmArr.sort(Collections.reverseOrder());
        double maxVal = sortAlarmArr.get(0);
        List<Double> temp = curValue.toList(Double.class);
        temp.sort(Collections.reverseOrder());
        double a = temp.get(0) - temp.get(2);
        int color;
        if (boxCurbalanceColorDO == null) {
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
                if (curUnbalance < boxCurbalanceColorDO.getRangeOne()) {
                    color = 2;
                } else if (curUnbalance < boxCurbalanceColorDO.getRangeFour()) {
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
    public List<BusTrendDTO> getBoxBalanceTrend(Integer boxId) {
        List<BusTrendDTO> result = new ArrayList<>();
        try {
            DateTime end = DateTime.now();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            DateTime start = DateTime.of(calendar.getTime());

            String startTime = DateUtil.formatDateTime(start);
            String endTime = DateUtil.formatDateTime(end);
            List<Integer> ids = Arrays.asList(boxId);
            List<String> data = getData(startTime, endTime, ids, BOX_HDA_LINE_HOUR);
            Map<String, List<BoxLineHourDo>> timeBus = new HashMap<>();
            data.forEach(str -> {
                BoxLineHourDo hourDo = JsonUtils.parseObject(str, BoxLineHourDo.class);

                String dateTime = DateUtil.format(hourDo.getCreateTime(), "yyyy-MM-dd HH");
                List<BoxLineHourDo> lineHourDos = timeBus.get(dateTime);
                if (CollectionUtils.isEmpty(lineHourDos)) {
                    lineHourDos = new ArrayList<>();
                }
                lineHourDos.add(hourDo);
                timeBus.put(dateTime, lineHourDos);
            });

            timeBus.keySet().forEach(dateTime -> {
                //获取每个时间段数据
                List<BoxLineHourDo> boxLineHourDos = timeBus.get(dateTime);

                BusTrendDTO trendDTO = new BusTrendDTO();
                trendDTO.setDateTime(dateTime);
                //获取相数据
                List<Map<String, Object>> cur = new ArrayList<>();
                List<Map<String, Object>> vol = new ArrayList<>();
                boxLineHourDos.forEach(hourDo -> {
                    Map<String, Object> curMap = new HashMap<>();
                    curMap.put("lineId", hourDo.getLineId());
                    curMap.put("curValue", hourDo.getCurAvgValue());
                    Map<String, Object> volMap = new HashMap<>();
                    volMap.put("lineId", hourDo.getLineId());
                    volMap.put("volValue", hourDo.getVolAvgValue());
                    cur.add(curMap);
                    vol.add(volMap);
                });
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
    public PageResult<BoxTemRes> getBoxTemPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxTemRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (BoxIndex boxIndexDO : list) {
            BoxTemRes boxTemRes = new BoxTemRes();
            boxTemRes.setStatus(boxIndexDO.getRunStatus());
            boxTemRes.setBoxId(boxIndexDO.getId());
            boxTemRes.setDevKey(boxIndexDO.getBoxKey());
            boxTemRes.setBoxName(boxIndexDO.getBoxName());
            res.add(boxTemRes);
        }
        Map<String, BoxTemRes> resMap = res.stream().collect(Collectors.toMap(BoxTemRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + "-" + jsonObject.getString("bar_id") + "-" + jsonObject.getString("addr");
            BoxTemRes boxTemRes = resMap.get(devKey);
            JSONObject envItemList = jsonObject.getJSONObject("env_item_list");
            JSONArray temValue = envItemList.getJSONArray("tem_value");
            JSONArray temStatus = envItemList.getJSONArray("tem_status");
            for (int i = 0; i < 4; i++) {
                double tem = temValue.getDoubleValue(i);
                Integer temSta = temStatus.getInteger(i);
                if (i == 0) {
                    boxTemRes.setATem(tem);
                    boxTemRes.setATemStatus(temSta);
                    if (temSta != 0) {
                        boxTemRes.setATemColor("red");
                    }
                } else if (i == 1) {
                    boxTemRes.setBTem(tem);
                    boxTemRes.setBTemStatus(temSta);
                    if (temSta != 0) {
                        boxTemRes.setBTemColor("red");
                    }
                } else if (i == 2) {
                    boxTemRes.setCTem(tem);
                    boxTemRes.setCTemStatus(temSta);
                    if (temSta != 0) {
                        boxTemRes.setCTemColor("red");
                    }
                } else if (i == 3) {
                    boxTemRes.setNTem(tem);
                    boxTemRes.setNTemStatus(temSta);
                    if (temSta != 0) {
                        boxTemRes.setNTemColor("red");
                    }
                }
            }
        }
        return new PageResult<>(res, boxIndexDOPageResult.getTotal());
    }

    @Override
    public Map getBoxTemDetail(BoxIndexPageReqVO pageReqVO) {
        try {
            pageReqVO.setNewTime(pageReqVO.getOldTime().withHour(23).withMinute(59).withSecond(59));
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(pageReqVO.getBoxId());
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<String> boxTemHour = getData(startTime, endTime, ids, "box_tem_hour");
            List<BoxTemHourDo> strList = boxTemHour.stream()
                    .map(str -> JsonUtils.parseObject(str, BoxTemHourDo.class))
                    .collect(Collectors.toList());

            BusTemDetailRes result = new BusTemDetailRes();
            result.setTemAvgValueA(new ArrayList<>());
            result.setTemAvgValueB(new ArrayList<>());
            result.setTemAvgValueC(new ArrayList<>());
            result.setTemAvgValueN(new ArrayList<>());
            result.setTemAvgTime(new ArrayList<>());

            List<BusTemTableRes> tableList = new ArrayList<>();


            strList.forEach(boxTemHourDo -> {
                BusTemTableRes busTemTableRes = new BusTemTableRes();
                busTemTableRes.setTemAvgValueA(boxTemHourDo.getTemAAvgValue());
                busTemTableRes.setTemAvgValueB(boxTemHourDo.getTemBAvgValue());
                busTemTableRes.setTemAvgValueC(boxTemHourDo.getTemCAvgValue());
                busTemTableRes.setTemAvgValueN(boxTemHourDo.getTemNAvgValue());
                busTemTableRes.setTemAvgTime(boxTemHourDo.getCreateTime().toString("HH:mm"));
                tableList.add(busTemTableRes);
                result.getTemAvgValueA().add(boxTemHourDo.getTemAAvgValue());
                result.getTemAvgValueB().add(boxTemHourDo.getTemBAvgValue());
                result.getTemAvgValueC().add(boxTemHourDo.getTemCAvgValue());
                result.getTemAvgValueN().add(boxTemHourDo.getTemNAvgValue());
                result.getTemAvgTime().add(boxTemHourDo.getCreateTime().toString("HH:mm"));
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
    public PageResult<BoxPFRes> getBoxPFPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxPFRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (BoxIndex boxIndexDO : list) {
            BoxPFRes boxPFRes = new BoxPFRes();
            boxPFRes.setStatus(boxIndexDO.getRunStatus());
            boxPFRes.setBoxId(boxIndexDO.getId());
            boxPFRes.setDevKey(boxIndexDO.getBoxKey());
            boxPFRes.setBoxName(boxIndexDO.getBoxName());
            res.add(boxPFRes);
        }
        Map<String, BoxPFRes> resMap = res.stream().collect(Collectors.toMap(BoxPFRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            if (jsonObject.getJSONObject("box_data").getJSONObject("box_cfg").getInteger("box_type") == 1) {
                continue;
            }
            String devKey = jsonObject.getString("dev_ip") + "-" + jsonObject.getString("bar_id") + "-" + jsonObject.getString("addr");
            BoxPFRes boxPFRes = resMap.get(devKey);

            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
            JSONArray phasePFValue = lineItemList.getJSONArray("power_factor");

            JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");
            JSONArray loopPFValue = loopItemList.getJSONArray("power_factor");

            JSONObject outletItemList = jsonObject.getJSONObject("box_data").getJSONObject("outlet_item_list");
            JSONArray outletPFValue = outletItemList.getJSONArray("power_factor");

            List<Double> phasePowFactor;
            List<Double> loopPowFactor;
            List<Double> outletPowFactor;

            phasePowFactor = phasePFValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            loopPowFactor = loopPFValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());
            outletPowFactor = outletPFValue.stream().mapToDouble(value -> Double.parseDouble(value.toString())).boxed().collect(Collectors.toList());

            boxPFRes.setPhasePowFactor(phasePowFactor);
            boxPFRes.setLoopPowFactor(loopPowFactor);
            boxPFRes.setOutletPowFactor(outletPowFactor);

            boxPFRes.setTotalPowFactor(jsonObject.getJSONObject("box_data").getJSONObject("box_total_data").getDoubleValue("power_factor"));
        }
        return new PageResult<>(res, boxIndexDOPageResult.getTotal());
    }

    @Override
    public Map getBoxPFDetail(BoxIndexPageReqVO pageReqVO) {
        try {
            pageReqVO.setNewTime(pageReqVO.getOldTime().withHour(23).withMinute(59).withSecond(59));
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(pageReqVO.getBoxId());
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<String> boxHdaLineHour = getData(startTime, endTime, ids, "box_hda_line_hour");
            List<BoxLineHourDo> strList = boxHdaLineHour.stream()
                    .map(str -> JsonUtils.parseObject(str, BoxLineHourDo.class))
                    .collect(Collectors.toList());

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

            Map<Integer, List<BoxLineHourDo>> pfMap = strList.stream().collect(Collectors.groupingBy(boxLineHourDo -> boxLineHourDo.getLineId()));

            int i = 0;
            for (BoxLineHourDo boxLineHourDo : pfMap.get(1)) {
                BusPFTableRes busPFTableRes = new BusPFTableRes();
                result.getPowerFactorAvgValueA().add(boxLineHourDo.getPowerFactorAvgValue());
                result.getTime().add(boxLineHourDo.getCreateTime().toString("HH:mm"));
                busPFTableRes.setPowerFactorAvgValueA(boxLineHourDo.getPowerFactorAvgValue());
                busPFTableRes.setTime(boxLineHourDo.getCreateTime().toString("HH:mm"));
                tableList.add(busPFTableRes);
                i++;
            }
            int j = 0;
            for (BoxLineHourDo boxLineHourDo : pfMap.get(2)) {
                result.getPowerFactorAvgValueB().add(boxLineHourDo.getPowerFactorAvgValue());
                if (i == 0 || j >= i) {
                    break;
                } else if (j < i) {
                    tableList.get(j).setPowerFactorAvgValueB(boxLineHourDo.getPowerFactorAvgValue());
                    j++;
                }
            }
            j = 0;
            for (BoxLineHourDo boxLineHourDo : pfMap.get(3)) {
                result.getPowerFactorAvgValueC().add(boxLineHourDo.getPowerFactorAvgValue());
                if (i == 0 || j >= i) {
                    break;
                } else if (j < i) {
                    tableList.get(j).setPowerFactorAvgValueC(boxLineHourDo.getPowerFactorAvgValue());
                    j++;
                }
            }
            return resultMap;
        } catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return null;
    }


    @Override
    public PageResult<BoxHarmonicRes> getBoxHarmonicPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxHarmonicRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (BoxIndex boxIndexDO : list) {
            BoxHarmonicRes boxHarmonicRes = new BoxHarmonicRes();
            boxHarmonicRes.setStatus(boxIndexDO.getRunStatus());
            boxHarmonicRes.setBoxId(boxIndexDO.getId());
            boxHarmonicRes.setDevKey(boxIndexDO.getBoxKey());
            boxHarmonicRes.setBoxName(boxIndexDO.getBoxName());
            res.add(boxHarmonicRes);
        }
        Map<String, BoxHarmonicRes> resMap = res.stream().collect(Collectors.toMap(BoxHarmonicRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            if (jsonObject.getJSONObject("box_data").getJSONObject("box_cfg").getInteger("box_type") == 1) {
                continue;
            }
            String devKey = jsonObject.getString("dev_ip") + "-" + jsonObject.getString("bar_id") + "-" + jsonObject.getString("addr");
            BoxHarmonicRes boxHarmonicRes = resMap.get(devKey);
            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
            JSONArray curThd = lineItemList.getJSONArray("cur_thd");
            for (int i = 0; i < 3; i++) {

                BigDecimal curThdValue = BigDemicalUtil.safeDivide(curThd.getDoubleValue(i), 100);
                if (i == 0) {
                    boxHarmonicRes.setAcurThd(curThdValue.doubleValue());
                } else if (i == 1) {
                    boxHarmonicRes.setBcurThd(curThdValue.doubleValue());
                } else if (i == 2) {
                    boxHarmonicRes.setCcurThd(curThdValue.doubleValue());
                }
            }
        }
        return new PageResult<>(res, boxIndexDOPageResult.getTotal());
    }

    @Override
    public BusHarmonicRedisRes getHarmonicRedis(BoxIndexPageReqVO pageReqVO) {
        Integer harmonicType;
        BusHarmonicRedisRes result = new BusHarmonicRedisRes();

        harmonicType = pageReqVO.getHarmonicType() - 3;

        ValueOperations ops = redisTemplate.opsForValue();

        JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + pageReqVO.getDevKey());
        if (jsonObject == null) {
            return result;
        }
        JSONArray jsonArray;

        jsonArray = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list").getJSONArray("cur_thd");

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
    public BusHarmonicLineRes getHarmonicLine(BoxIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();

        result.getSeries().add(new LineSeries());
        result.getSeries().add(new LineSeries());
        SeriesBase lineSeries = result.getSeries().get(1);
        lineSeries.setName("电流平均谐波");
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
            List<Integer> ids = Arrays.asList(pageReqVO.getBoxId());
            List<Integer> lines = Arrays.asList(lineId);
            List<String> boxHdaLine = getBoxHarmonicData(startTime, endTime, ids, lines, "box_hda_line_realtime");
            boxHdaLine.forEach(str -> {
                BoxLineHourDo boxLineHourDo = JsonUtils.parseObject(str, BoxLineHourDo.class);
                result.getTime().add(boxLineHourDo.getCreateTime().toString("HH:mm"));
                lineSeries.getData().add(boxLineHourDo.getCurThdAvgValue());
            });
            return result;
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }

        return result;
    }

    @Override
    public Integer getBoxIdByDevKey(String devKey) {
        BoxIndex boxIndex = boxIndexCopyMapper.selectOne(BoxIndex::getBoxKey, devKey);
        if (boxIndex == null) {
            return null;
        } else {
            return boxIndex.getId();
        }
    }


    @Override
    public List<String> getDevKeyList() {
        return boxIndexCopyMapper.selectList().stream()
                .limit(10)
                .filter(boxIndex -> boxIndex.getBoxType() == 0)
                .map(BoxIndex::getBoxKey).collect(Collectors.toList());
    }

    @Override
    public PageResult<BoxLineRes> getBoxLineDevicePage(BoxIndexPageReqVO pageReqVO) {
        try {
            List<BoxIndex> searchList = boxIndexCopyMapper.selectList(new LambdaQueryWrapperX<BoxIndex>().inIfPresent(BoxIndex::getBoxKey, pageReqVO.getBoxDevKeyList()));
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
                index = "box_hda_line_hour";
            } else {
                index = "box_hda_line_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());

            Map esTotalAndIds = getESTotalAndIds(index, startTime, endTime, pageReqVO.getPageSize(), pageReqVO.getPageNo() - 1, searchList.stream().map(BoxIndex::getId).collect(Collectors.toList()));

            Long total = (Long) esTotalAndIds.get("total");
            if (total == 0) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            List<Integer> ids = (List<Integer>) esTotalAndIds.get("ids");

            curMap = getBoxLineCurMaxData(startTime, endTime, ids, index);
            powMap = getBoxLinePowMaxData(startTime, endTime, ids, index);

            List<BoxLineRes> result = new ArrayList<>();

            List<BoxIndex> boxIndices = boxIndexCopyMapper.selectList(new LambdaQueryWrapperX<BoxIndex>()
                    .inIfPresent(BoxIndex::getId, ids));

            for (BoxIndex boxIndex : boxIndices) {
                Integer id = boxIndex.getId();
                if (curMap.get(id) == null) {
                    continue;
                }
                BoxLineRes boxLineRes = new BoxLineRes();
                boxLineRes.setStatus(boxIndex.getRunStatus());

                boxLineRes.setBoxId(boxIndex.getId());
                boxLineRes.setDevKey(boxIndex.getBoxKey());
                boxLineRes.setBoxName(boxIndex.getBoxName());

                MaxValueAndCreateTime curl1 = curMap.get(id).get(1);
                boxLineRes.setL1MaxCur(curl1.getMaxValue().floatValue());
                boxLineRes.setL1MaxCurTime(curl1.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                MaxValueAndCreateTime curl2 = curMap.get(id).get(2);
                if (curl2 != null) {
                    boxLineRes.setL2MaxCur(curl2.getMaxValue().floatValue());
                    boxLineRes.setL2MaxCurTime(curl2.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                MaxValueAndCreateTime curl3 = curMap.get(id).get(3);
                if (curl3 != null) {
                    boxLineRes.setL3MaxCur(curl3.getMaxValue().floatValue());
                    boxLineRes.setL3MaxCurTime(curl3.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                MaxValueAndCreateTime powl1 = powMap.get(id).get(1);
                boxLineRes.setL1MaxPow(powl1.getMaxValue().floatValue());
                boxLineRes.setL1MaxPowTime(powl1.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                MaxValueAndCreateTime powl2 = powMap.get(id).get(2);
                if (powl2 != null) {
                    boxLineRes.setL2MaxPow(powl2.getMaxValue().floatValue());
                    boxLineRes.setL2MaxPowTime(powl2.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                MaxValueAndCreateTime powl3 = powMap.get(id).get(3);
                if (powl3 != null) {
                    boxLineRes.setL3MaxPow(powl3.getMaxValue().floatValue());
                    boxLineRes.setL3MaxPowTime(powl3.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                result.add(boxLineRes);
            }

            if (!CollectionUtils.isEmpty(result)) {
                getPosition(result);
            }

            return new PageResult<BoxLineRes>(result, total);
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public BusLineResBase getBoxLineCurLine(BoxIndexPageReqVO pageReqVO) {
        BusLineResBase result = new BusLineResBase();
        try {
            String startTime;
            String endTime;
            String index;
            if (pageReqVO.getTimeType() == 0) {
                index = BOX_HDA_LINE_HOUR;
                startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
                endTime = localDateTimeToString(LocalDateTime.now());
            } else {
                index = BOX_HDA_LINE_DAY;
                startTime = localDateTimeToString(pageReqVO.getOldTime());
                endTime = localDateTimeToString(pageReqVO.getNewTime());
            }
            List<Integer> ids = Arrays.asList(pageReqVO.getBoxId());
            List<String> data = getData(startTime, endTime, ids, index);

            if (pageReqVO.getLineType() == 0) {
                result.getSeries().add(new RequirementLineSeries().setName("A路最大电流"));
                result.getSeries().add(new RequirementLineSeries().setName("B路最大电流"));
                result.getSeries().add(new RequirementLineSeries().setName("C路最大电流"));
                data.forEach(str -> {
                    BoxLineHourDo lineDo = JsonUtils.parseObject(str, BoxLineHourDo.class);
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
                    BoxLineHourDo lineDo = JsonUtils.parseObject(str, BoxLineHourDo.class);
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
    public Map getReportConsumeDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase barRes = new BusLineResBase();
        BarSeries barSeries = new BarSeries();
        try {
            BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, devKey));
            if (boxIndex != null) {
                String index;
                boolean isSameDay;
                Integer Id = boxIndex.getId();
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "box_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "box_eq_total_day";
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
                        BoxEleTotalDo eleDO = JsonUtils.parseObject(str, BoxEleTotalDo.class);
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
                    BoxEleTotalDo eleMaxValue = JsonUtils.parseObject(eleMax, BoxEleTotalDo.class);
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
                        BoxEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, BoxEqTotalDayDo.class);
                        totalEq += totalDayDo.getEq();
                        barSeries.getData().add((float) totalDayDo.getEq());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Id), index, "eq_value");
                    BoxEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, BoxEqTotalDayDo.class);
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
    public Map getBoxPFLine(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase totalLineRes = new BusLineResBase();
        result.put("pfLineRes", totalLineRes);
        try {
            BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, devKey));

            if (boxIndex != null) {
                String index;
                Integer Id = boxIndex.getId();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "box_hda_total_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "box_hda_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }

                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getData(startTime, endTime, Arrays.asList(Id), index);
                List<BoxTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, BoxTotalHourDo.class)).collect(Collectors.toList());

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
        try {
            BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, devKey));

            if (boxIndex != null) {
                String index;
                Integer Id = boxIndex.getId();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "box_hda_total_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "box_hda_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }

                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getData(startTime, endTime, Arrays.asList(Id), index);
                List<BoxTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, BoxTotalHourDo.class)).collect(Collectors.toList());

                LineSeries totalApparentPow = new LineSeries();
                totalApparentPow.setName("总平均视在功率");
                LineSeries totalActivePow = new LineSeries();
                totalActivePow.setName("总平均有功功率");
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);


                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getPowApparentAvgValue());
                        totalActivePow.getData().add(hourdo.getPowActiveAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm"));

                    });
                } else {
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getPowApparentAvgValue());
                        totalActivePow.getData().add(hourdo.getPowActiveAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));

                    });
                }

                String apparentTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "pow_apparent_max_value");
                BoxTotalHourDo totalMaxApparent = JsonUtils.parseObject(apparentTotalMaxValue, BoxTotalHourDo.class);
                String apparentTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Id), index, "pow_apparent_min_value");
                BoxTotalHourDo totalMinApparent = JsonUtils.parseObject(apparentTotalMinValue, BoxTotalHourDo.class);

                String activeTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "pow_active_max_value");
                BoxTotalHourDo totalMaxActive = JsonUtils.parseObject(activeTotalMaxValue, BoxTotalHourDo.class);
                String activeTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Id), index, "pow_active_min_value");
                BoxTotalHourDo totalMinActive = JsonUtils.parseObject(activeTotalMinValue, BoxTotalHourDo.class);

                result.put("totalLineRes", totalLineRes);
                if (Objects.nonNull(totalMaxApparent)) {
                    result.put("apparentPowMaxValue", totalMaxApparent.getPowApparentMaxValue());
                    result.put("apparentPowMaxTime", totalMaxApparent.getPowApparentMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (Objects.nonNull(totalMinApparent)) {
                    result.put("apparentPowMinValue", totalMinApparent.getPowApparentMinValue());
                    result.put("apparentPowMinTime", totalMinApparent.getPowApparentMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (Objects.nonNull(totalMaxActive)) {
                    result.put("activePowMaxValue", totalMaxActive.getPowActiveMaxValue());
                    result.put("activePowMaxTime", totalMaxActive.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (Objects.nonNull(totalMinActive)) {
                    result.put("activePowMinValue", totalMinActive.getPowActiveMinValue());
                    result.put("activePowMinTime", totalMinActive.getPowActiveMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

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
            BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, devKey));
            if (boxIndex != null) {
                Integer Id = boxIndex.getId();
                String index;
                boolean isSameDay;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "box_tem_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "box_tem_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> busData = getData(startTime, endTime, Arrays.asList(Id), index);
                List<BoxTemHourDo> temList = busData.stream()
                        .map(str -> JsonUtils.parseObject(str, BoxTemHourDo.class))
                        .collect(Collectors.toList());

                List<String> time;
                LineSeries seriesA = new LineSeries();
                List<Float> temA = temList.stream().map(BoxTemHourDo::getTemAAvgValue).collect(Collectors.toList());
                seriesA.setName("A相平均温度");
                seriesA.setData(temA);
                LineSeries seriesB = new LineSeries();
                List<Float> temB = temList.stream().map(BoxTemHourDo::getTemBAvgValue).collect(Collectors.toList());
                seriesB.setName("B相平均温度");
                seriesB.setData(temB);
                LineSeries seriesC = new LineSeries();
                List<Float> temC = temList.stream().map(BoxTemHourDo::getTemCAvgValue).collect(Collectors.toList());
                seriesC.setName("C相平均温度");
                seriesC.setData(temC);
                LineSeries seriesN = new LineSeries();
                List<Float> temN = temList.stream().map(BoxTemHourDo::getTemNAvgValue).collect(Collectors.toList());
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
                BoxTemHourDo temMaxA = JsonUtils.parseObject(temAMaxValue, BoxTemHourDo.class);
                String temAMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_a_min_value");
                BoxTemHourDo temMinA = JsonUtils.parseObject(temAMinValue, BoxTemHourDo.class);
                if (temMaxA != null) {
                    result.put("temAMaxValue", temMaxA.getTemAMaxValue());
                    result.put("temAMaxTime", temMaxA.getTemAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (temMinA != null) {
                    result.put("temAMinValue", temMinA.getTemAMinValue());
                    result.put("temAMinTime", temMinA.getTemAMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                String temBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_b_max_value");
                BoxTemHourDo temMaxB = JsonUtils.parseObject(temBMaxValue, BoxTemHourDo.class);
                String temBMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_b_min_value");
                BoxTemHourDo temMinB = JsonUtils.parseObject(temBMinValue, BoxTemHourDo.class);
                if (temMaxB != null) {
                    result.put("temBMaxValue", temMaxB.getTemBMaxValue());
                    result.put("temBMaxTime", temMaxB.getTemBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (temMinB != null) {
                    result.put("temBMinValue", temMinB.getTemBMinValue());
                    result.put("temBMinTime", temMinB.getTemBMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                String temCMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_c_max_value");
                BoxTemHourDo temMaxC = JsonUtils.parseObject(temCMaxValue, BoxTemHourDo.class);
                String temCMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_c_min_value");
                BoxTemHourDo temMinC = JsonUtils.parseObject(temCMinValue, BoxTemHourDo.class);
                if (temMaxC != null) {
                    result.put("temCMaxValue", temMaxC.getTemCMaxValue());
                    result.put("temCMaxTime", temMaxC.getTemCMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if (temMinC != null) {
                    result.put("temCMinValue", temMinC.getTemCMinValue());
                    result.put("temCMinTime", temMinC.getTemCMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                String temNMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_n_max_value");
                BoxTemHourDo temMaxN = JsonUtils.parseObject(temNMaxValue, BoxTemHourDo.class);
                String temNMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_n_min_value");
                BoxTemHourDo temMinN = JsonUtils.parseObject(temNMinValue, BoxTemHourDo.class);
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
    public String getBoxRedisByDevKey(String devKey) {
        if (StringUtils.isEmpty(devKey)) {
            return null;
        } else {
            ValueOperations ops = redisTemplate.opsForValue();
            JSONObject jsonObject = (JSONObject) ops.get(REDIS_KEY_BOX + devKey);
            return jsonObject != null ? jsonObject.toJSONString() : null;
        }
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
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termQuery(BOX_ID, id))));
        builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(3000);

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
                .must(QueryBuilders.termQuery("box_id", powVo.getId())));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(3000);

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
    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("box_id", ids))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(3000);

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

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index, String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("box_id", ids))));
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
                .must(QueryBuilders.termsQuery("box_id", ids))));
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

    private Map<Integer, Map<Integer, MaxValueAndCreateTime>> getBoxLineCurMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("box_id", ids))));

        builder.aggregation(
                AggregationBuilders.terms("group_by_box_id")
                        .field("box_id")
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

        // 解析聚合结果 todo
        // 初始化结果Map
        Map<Integer, Map<Integer, MaxValueAndCreateTime>> resultMap = new HashMap<>();
        // 获取group_by_box_id聚合结果
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_box_id");
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

    private Map<Integer, Map<Integer, MaxValueAndCreateTime>> getBoxLinePowMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("box_id", ids))));

        builder.aggregation(
                AggregationBuilders.terms("group_by_box_id")
                        .field("box_id")
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
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_box_id");
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

        List<String> list = getData(startTime, endTime, id, BOX_ELE_TOTAL_REALTIME);

        List<BoxEleTotalDo> yesterdayList = new ArrayList<>();
        List<BoxEleTotalDo> todayList = new ArrayList<>();
        String finalStartTime = startTime;
        list.forEach(str -> {
            BoxEleTotalDo realtimeDo = JsonUtils.parseObject(str, BoxEleTotalDo.class);
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
            BoxEleTotalDo reDo = yesterdayList.get(i);
            //当前时间点
            BoxEleTotalDo thisDo = yesterdayList.get(i + 1);

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
            BoxEleTotalDo reDo = todayList.get(i);
            //当前时间点
            BoxEleTotalDo thisDo = todayList.get(i + 1);

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

        List<String> list = getData(startTime, endTime, id, BOX_EQ_TOTAL_DAY);

        Map<String, BoxEqTotalDayDo> weekMap = new HashMap<>();

        list.forEach(str -> {
            BoxEqTotalDayDo realtimeDo = JsonUtils.parseObject(str, BoxEqTotalDayDo.class);
            String dateTime = DateUtil.formatDate(realtimeDo.getCreateTime());
            weekMap.put(dateTime, realtimeDo);

        });
        Map<Integer, BusEqTrendDTO> data = new HashMap<>();
        //本周数据
        thisWeek.keySet().forEach(key -> {

            String date = thisWeek.get(key);

            BoxEqTotalDayDo realtimeDo = weekMap.get(date);

            BusEqTrendDTO trendDTO = new BusEqTrendDTO();
            trendDTO.setEq(Objects.nonNull(realtimeDo) ? realtimeDo.getEq() : 0);
            trendDTO.setDateTime(String.valueOf(key));
            data.put(key, trendDTO);


        });

        Map<Integer, String> lastWeek = getWeek(startTime);
        //上周数据
        lastWeek.keySet().forEach(key -> {

            String date = lastWeek.get(key);

            BoxEqTotalDayDo realtimeDo = weekMap.get(date);

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

        List<String> list = getData(startTime, endTime, id, BOX_EQ_TOTAL_DAY);

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
    private void getDayChain(int id, BusEleChainDTO chainDTO) throws IOException {
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

        List<String> list = getData(startTime, endTime, id, BOX_EQ_TOTAL_DAY);
        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            BoxEqTotalDayDo dayDo = JsonUtils.parseObject(str, BoxEqTotalDayDo.class);
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

    }

    /**
     * 获取周环比
     *
     * @param id
     * @param chainDTO
     */
    private void getWeekChain(int id, BusEleChainDTO chainDTO) throws IOException {
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

        List<String> list = getData(startTime, endTime, id, BOX_EQ_TOTAL_WEEK);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            BoxEqTotalDayDo weekDo = JsonUtils.parseObject(str, BoxEqTotalDayDo.class);
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
    }

    /**
     * 获取月环比
     *
     * @param id
     * @param chainDTO
     */
    private void getMonthChain(int id, BusEleChainDTO chainDTO) throws IOException {
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


        List<String> list = getData(startTime, endTime, id, BOX_EQ_TOTAL_MONTH);

        Map<String, Double> eqMap = new HashMap<>();

        list.forEach(str -> {
            BoxEqTotalDayDo monthDo = JsonUtils.parseObject(str, BoxEqTotalDayDo.class);
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
        BoxEleTotalDo endRealtimeDo = getEleData(startTime, endTime,
                SortOrder.DESC,
                BoxConstants.BOX_ELE_TOTAL_REALTIME, id);
        BoxEleTotalDo startRealtimeDo = getEleData(startTime, endTime,
                SortOrder.ASC,
                BoxConstants.BOX_ELE_TOTAL_REALTIME, id);
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
     * @param startTime
     * @param endTime
     * @param sortOrder
     * @param index
     * @param id
     * @return
     */
    private BoxEleTotalDo getEleData(String startTime, String endTime, SortOrder sortOrder, String index, int id) {
        BoxEleTotalDo realtimeDo = new BoxEleTotalDo();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + BusConstants.KEYWORD)
                            .gte(startTime)
                            .lt(endTime))
                    .must(QueryBuilders.termQuery(BOX_ID, id))));

            // 嵌套聚合
            // 设置聚合查询
            String top = "top";
            AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                    .size(1).sort(CREATE_TIME + BusConstants.KEYWORD, sortOrder);

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
                realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), BoxEleTotalDo.class);
            }
            return realtimeDo;
        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return realtimeDo;
    }

    private List<String> getBoxHarmonicData(String startTime, String endTime, List<Integer> ids, List<Integer> lines, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("box_id", ids))
                .must(QueryBuilders.termsQuery("line_id", lines))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(3000);

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
     * 获取设备位置
     *
     * @return
     */
    public void getPosition(List<? extends BoxResBase> res) {
        ValueOperations ops = redisTemplate.opsForValue();
        List<String> devKeyList = res.stream().map(BoxResBase::getDevKey).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(devKeyList)) {
            return;
        }
        //设备位置
        String devPosition;
        //柜列
        List<AisleBox> aisleBoxList = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
                .in(AisleBox::getBoxKey, devKeyList));
        if (!CollectionUtils.isEmpty(aisleBoxList)) {
            List<Integer> aisleBarIds = aisleBoxList.stream().map(AisleBox::getAisleBarId).collect(Collectors.toList());
            List<AisleBar> aisleBars = aisleBarMapper.selectBatchIds(aisleBarIds);
            Map<Integer, String> pathMap = aisleBars.stream().collect(Collectors.toMap(AisleBar::getId, AisleBar::getPath));
            Map<String, AisleBox> aisleBoxKeyMap = aisleBoxList.stream().collect(Collectors.toMap(AisleBox::getBoxKey, Function.identity()));
            Map<Integer, String> positionMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(aisleBoxList)) {
                List<String> redisKeys = aisleBoxList.stream().map(aisle -> REDIS_KEY_AISLE + aisle.getAisleId()).collect(Collectors.toList());
                List aisles = ops.multiGet(redisKeys);
                if (!CollectionUtils.isEmpty(aisleBoxList)) {
                    for (Object aisle : aisles) {
                        JSONObject json = JSON.parseObject(JSON.toJSONString(aisle));
                        positionMap.put(json.getInteger("aisle_key"), json.getString("room_name") + SPLIT_KEY
                                + json.getString("aisle_name"));
                    }
                }
            }
            res.forEach(box -> {
                if (aisleBoxKeyMap.get(box.getDevKey()) != null) {
                    AisleBox aisleBox = aisleBoxKeyMap.get(box.getDevKey());
                    Integer aisleId = aisleBox.getAisleId();
                    box.setLocation(positionMap.get(aisleId) + SPLIT_KEY + pathMap.get(aisleBox.getAisleBarId()) + "路");
                }
            });
        }

        List<BoxResBase> resNotInAisle = res.stream().filter(boxRes -> StringUtils.isEmpty(boxRes.getLocation())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resNotInAisle)) {
            return;
        }
        List<String> resNotInAisleDevKeys = resNotInAisle.stream().map(BoxResBase::getDevKey).collect(Collectors.toList());
        Map<String, BoxResBase> resNotInAisleMap = resNotInAisle.stream().collect(Collectors.toMap(BoxResBase::getDevKey, Function.identity()));
        List<CabinetBox> cabinetBus = cabinetBusMapper.selectList(new LambdaQueryWrapperX<CabinetBox>().in(CabinetBox::getBoxKeyA, resNotInAisleDevKeys).or().in(CabinetBox::getBoxKeyB, resNotInAisleDevKeys));
        if (CollectionUtils.isEmpty(cabinetBus)) {
            return;
        }
        List<Integer> cabinetIds = cabinetBus.stream().map(CabinetBox::getCabinetId).collect(Collectors.toList());
        Map<Integer, String> cabinetBusMapA = cabinetBus.stream().filter(cabinet -> cabinet.getBoxKeyA() != null).collect(Collectors.toMap(CabinetBox::getCabinetId, CabinetBox::getBoxKeyA));
        Map<Integer, String> cabinetBusMapB = cabinetBus.stream().filter(cabinet -> cabinet.getBoxKeyB() != null).collect(Collectors.toMap(CabinetBox::getCabinetId, CabinetBox::getBoxKeyB));

        List<CabinetIndex> cabinetIndices = cabinetIndexMapper.selectBatchIds(cabinetIds);
        List<String> cabinetRedisKeys = cabinetIndices.stream().map(index -> REDIS_KEY_CABINET + index.getRoomId() + SPLIT_KEY + index.getId()).collect(Collectors.toList());

        List cabinets = ops.multiGet(cabinetRedisKeys);
        if (!CollectionUtils.isEmpty(cabinets)) {
            for (Object cabinet : cabinets) {
                JSONObject json = JSON.parseObject(JSON.toJSONString(cabinet));
                devPosition = json.getString("room_name") + SPLIT_KEY + json.getString("cabinet_name");
                if (!StringUtils.isEmpty(json.getString("aisle_name"))) {
                    devPosition += SPLIT_KEY + json.getString("aisle_name");
                }
                Integer cabinetId = Integer.valueOf(json.getString("cabinet_key").split("-")[1]);
                String devKeyA = cabinetBusMapA.get(cabinetId);
                if (!StringUtils.isEmpty(devKeyA)) {
                    BoxResBase box = resNotInAisleMap.get(devKeyA);
                    if (Objects.nonNull(box) && box.getLocation() == null) {
                        box.setLocation(devPosition + SPLIT_KEY + "A路" + SPLIT_KEY + box.getBoxName());
                    }
                }
                String devKeyB = cabinetBusMapB.get(cabinetId);
                if (!StringUtils.isEmpty(devKeyB)) {
                    BoxResBase box = resNotInAisleMap.get(devKeyB);
                    if (Objects.nonNull(box) && box.getLocation() == null) {
                        box.setLocation(devPosition + SPLIT_KEY + "B路" + SPLIT_KEY + box.getBoxName());
                    }
                }
            }
        }


    }

    private Map getESTotalAndIds(String index, String startTime, String endTime, Integer pageSize, Integer pageNo) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder
                .from(pageNo * pageSize)
                .size(pageSize)
                .query(QueryBuilders.rangeQuery("create_time.keyword").gte(startTime).lte(endTime))
                .sort("box_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("box_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("box_id").precisionThreshold(10000));

        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                BoxLineHourDo hourDo = JsonUtils.parseObject(str, BoxLineHourDo.class);
                sortValues.add(hourDo.getBoxId());
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
                        .must(QueryBuilders.termsQuery("box_id", ids))))
                .sort("box_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("box_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("box_id").precisionThreshold(10000));
        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                BoxLineHourDo hourDo = JsonUtils.parseObject(str, BoxLineHourDo.class);
                sortValues.add(hourDo.getBoxId());
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

    private List getMutiRedis(List<BoxIndex> list) {
        List<String> devKeys = list.stream().map(boxIndex -> "packet:box:" + boxIndex.getBoxKey()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }
}