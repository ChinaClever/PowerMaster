package cn.iocoder.yudao.module.bus.service.boxindex;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.iocoder.yudao.framework.common.entity.es.box.BoxBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.oulet.BoxEleOutletDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.oulet.BoxEqOutletDayDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEleTotalDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.box.line.BoxLineBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.box.line.BoxLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.box.line.BoxLineRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.box.outlet.BoxOutletBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.box.outlet.BoxOutletHourDo;
import cn.iocoder.yudao.framework.common.entity.es.box.tem.BoxTemHourDo;
import cn.iocoder.yudao.framework.common.entity.es.box.total.BoxTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.box.total.BoxTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEleTotalDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.tem.BusTemHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.total.BusTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.loop.PduHdaLoopBaseDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBox;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetBox;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.enums.DataTypeEnums;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.vo.AisleBoxResVO;
import cn.iocoder.yudao.framework.common.vo.CabinetBoxResVO;
import cn.iocoder.yudao.framework.excel.core.handler.SpringHttpUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.constant.BusConstants;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.MaxValueAndCreateTime;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxcurbalancecolor.BoxCurbalanceColorDO;
import cn.iocoder.yudao.module.bus.dal.mysql.boxcurbalancecolor.BoxCurbalanceColorMapper;
import cn.iocoder.yudao.module.bus.dal.mysql.boxharmoniccolor.BoxHarmonicColorMapper;
import cn.iocoder.yudao.module.bus.dal.mysql.boxindex.BoxIndexCopyMapper;
import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;
import cn.iocoder.yudao.module.bus.enums.DataNameType;
import cn.iocoder.yudao.module.bus.service.busindex.BusIndexServiceImpl;
import cn.iocoder.yudao.module.bus.util.DataProcessingUtils;
import cn.iocoder.yudao.module.bus.util.PduAnalysisResult;
import cn.iocoder.yudao.module.bus.util.TimeUtil;
import cn.iocoder.yudao.module.bus.vo.BalanceStatisticsVO;
import cn.iocoder.yudao.module.bus.vo.BoxNameVO;
import cn.iocoder.yudao.module.bus.vo.LoadRateStatus;
import cn.iocoder.yudao.module.bus.vo.ReportBasicInformationResVO;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.REDIS_KEY_AISLE;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_BOX;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.constant.BoxConstants.*;
import static cn.iocoder.yudao.module.bus.constant.BusConstants.SPLIT_KEY;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.INDEX_NOT_EXISTS;
import static cn.iocoder.yudao.module.bus.service.busindex.BusIndexServiceImpl.REDIS_KEY_CABINET;
import static cn.iocoder.yudao.module.bus.util.DataProcessingUtils.collectPhaseData;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.KEY_SHORT;

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

    public static final String HOUR_FORMAT = "yyyy-MM-dd";

    public static final String TIME_STR = ":00:00";

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");

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

    /**
     * 获得已经删除插接箱负荷分页
     *
     * @param pageReqVO
     * @return
     */
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
        HashMap<String, Object> resultMap = new HashMap<>();
        BusLineResBase curResBase = new BusLineResBase();
        BusLineResBase volResBase = new BusLineResBase();
        BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, pageReqVO.getDevKey()));
        if (boxIndex != null) {
            String index;
            if (pageReqVO.getTimeType().equals(0)) {
                index = "box_hda_line_hour";
            } else {
                index = "box_hda_line_day";
            }
            String start = LocalDateTimeUtil.format(pageReqVO.getOldTime(), "yyyy-MM-dd HH:mm:ss");
            String end = LocalDateTimeUtil.format(pageReqVO.getNewTime(), "yyyy-MM-dd HH:mm:ss");
            List<String> list = getEsStrings(index, start, end, boxIndex);
            boolean isSameDay = (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate()));
            SimpleDateFormat sdfS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (String hit : list) {
                BusHdaLineAvgResVO houResVO = JsonUtils.parseObject(hit, BusHdaLineAvgResVO.class);
                DataProcessingUtils.collectPhaseData(houResVO, resultMap, isSameDay, DataTypeEnums.fromValue(pageReqVO.getDataType()));
            }

            for (int lineId = 1; lineId <= 3; lineId++) {
                String lineKey = "dayList" + lineId;
                Map<String, Object> lineData = (Map<String, Object>) resultMap.get(lineKey);
                if (lineData != null && !(((List<BusHdaLineAvgResVO>) lineData.get("data")).isEmpty())) {
                    LineSeries curSeries = new LineSeries();
                    LineSeries volSeries = new LineSeries();
                    resultMap.put("curName" + lineId, lineId == 1 ? "A相电流" : lineId == 2 ? "B相电流" : "C相电流");
                    resultMap.put("volName" + lineId, lineId == 1 ? "A相电压" : lineId == 2 ? "B相电压" : "C相电压");
                    curSeries.setName(getSeriesName("电流", lineId, pageReqVO.getDataType()));
                    volSeries.setName(getSeriesName("电压", lineId, pageReqVO.getDataType()));

                    curSeries.setData((List<Float>) lineData.get("curDataList"));
                    curSeries.setHappenTime((List<String>) lineData.get("curHappenTime"));
                    volSeries.setData((List<Float>) lineData.get("volDataList"));
                    volSeries.setHappenTime((List<String>) lineData.get("volHappenTime"));

                    Map<String, Object> analyzedData = PduAnalysisResult.analyzePduData((List<BusHdaLineAvgResVO>) lineData.get("data"), pageReqVO.getDataType());
                    PduAnalysisResult.CurrentResult currentResult = (PduAnalysisResult.CurrentResult) analyzedData.get("current");
                    PduAnalysisResult.VoltageResult voltageResult = (PduAnalysisResult.VoltageResult) analyzedData.get("voltage");

                    if (pageReqVO.getDataType() != 0) {
                        resultMap.put("curMaxValue" + lineId, currentResult.maxCurValue);
                        resultMap.put("curMaxTime" + lineId, sdfS.format(currentResult.maxCurTime));
                        resultMap.put("curMinValue" + lineId, currentResult.minCurValue);
                        resultMap.put("curMinTime" + lineId, sdfS.format(currentResult.minCurTime));
                        resultMap.put("volMaxValue" + lineId, voltageResult.maxVolValue);
                        resultMap.put("volMaxTime" + lineId, sdfS.format(voltageResult.maxVolTime));
                        resultMap.put("volMinValue" + lineId, voltageResult.minVolValue);
                        resultMap.put("volMinTime" + lineId, sdfS.format(voltageResult.minVolTime));
                    } else {
                        resultMap.put("curMaxValue" + lineId, currentResult.maxCurValue);
                        resultMap.put("curMaxTime" + lineId, "无");
                        resultMap.put("curMinValue" + lineId, currentResult.minCurValue);
                        resultMap.put("curMinTime" + lineId, "无");
                        resultMap.put("volMaxValue" + lineId, voltageResult.maxVolValue);
                        resultMap.put("volMaxTime" + lineId, "无");
                        resultMap.put("volMinValue" + lineId, voltageResult.minVolValue);
                        resultMap.put("volMinTime" + lineId, "无");
                    }


                    curResBase.getSeries().add(curSeries);
                    volResBase.getSeries().add(volSeries);

                    // 添加时间轴数据
                    List<String> uniqueDateTimes = (List<String>) resultMap.getOrDefault("dateTimes", new ArrayList<>());
                    List<String> xTime = uniqueDateTimes.stream().distinct().collect(Collectors.toList());

                    curResBase.setTime(xTime);
                    volResBase.setTime(xTime);
                    resultMap.put("dateTimes", xTime);
                    resultMap.put("curRes", curResBase);
                    resultMap.put("volRes", volResBase);
                }
            }
        }
        return resultMap;
    }


    private String getSeriesName(String type, int lineId, int dataType) {
        String lineName = "";
        switch (lineId) {
            case 1:
                lineName = "A";
                break;
            case 2:
                lineName = "B";
                break;
            case 3:
                lineName = "C";
                break;
        }

        return lineName + "相" + DataNameType.fromValue(dataType).name() + type + "曲线";
    }


    private List<String> getEsStrings(String index, String start, String end, BoxIndex boxIndex) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.size(10000);
            builder.trackTotalHits(true);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                            .gte(start)
                            .lte(end))
                    .must(QueryBuilders.termQuery("box_id", boxIndex.getId()))));
            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
            // 设置搜索条件
            searchRequest.source(builder);
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<String> list = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                if (sourceAsString.length() > 2) {
                    list.add(sourceAsString);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("插接箱报表查询异常:" + e);
            return null;
        }
    }

    @Override
    public LineBoxMaxResVO getBoxLineMax(BoxIndexPageReqVO pageReqVO) throws IOException {
        ValueOperations ops = redisTemplate.opsForValue();
        String index;
        String indexOut;
        String startTime;
        String endTime;
        if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
            pageReqVO.setNewTime(LocalDateTime.now());
            pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            startTime = localDateTimeToString(pageReqVO.getOldTime());
            endTime = localDateTimeToString(pageReqVO.getNewTime());

            index = "box_hda_line_hour";
            indexOut = "box_hda_outlet_hour";
        } else {

            pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
            pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            startTime = localDateTimeToString(pageReqVO.getOldTime());
            endTime = localDateTimeToString(pageReqVO.getNewTime());

            index = "box_hda_line_day";
            indexOut = "box_hda_outlet_day";
        }
        String orderKey;
        SearchResponse searchResponse = null;
        LineBoxMaxResVO resVO = null;
        switch (pageReqVO.getBoxType()) {
            case 1:
                searchResponse = getBoxLineMaxEs(index, startTime, endTime, "cur_max_value");
                break;
            case 2:
                searchResponse = getBoxLineMaxEs(index, startTime, endTime, "pow_active_max_value");
                break;
            case 3:
                searchResponse = getBoxLineMaxEs(indexOut, startTime, endTime, "pow_active_max_value");
                searchResponse = getBoxLineMaxEs(indexOut, startTime, endTime, "pow_apparent_max_value");
                break;
            default:
        }

        if (searchResponse == null) {
            return resVO;
        }
        if (searchResponse.getHits().getTotalHits().value > 0) {
            resVO = JsonUtils.parseObject(searchResponse.getHits().getAt(0).getSourceAsString(), LineBoxMaxResVO.class);
            if (Objects.equals(pageReqVO.getBoxType(), 3)) {
                switch (resVO.getOutletId()) {
                    case 1:
                        resVO.setLineName("输出位1");
                        break;
                    case 2:
                        resVO.setLineName("输出位2");
                        break;
                    case 3:
                        resVO.setLineName("输出位3");
                        break;
                    default:
                }
            } else {
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
            }
            BoxIndex boxIndex = boxIndexCopyMapper.selectById(resVO.getBusId());
            if (Objects.nonNull(boxIndex)) {
                resVO.setDevKey(boxIndex.getBoxKey());
                resVO.setBusName(boxIndex.getBoxName());
                Map<String, String> positionByKey = getPositionByKey(boxIndex.getBoxKey());
                resVO.setLocation(positionByKey.get(boxIndex.getBoxKey()));
            }
        }
        return resVO;
    }

    private SearchResponse getBoxLineMaxEs(String index, String startTime, String endTime, String orderKey) throws IOException {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.size(1);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                    .gte(startTime).lt(endTime))));
            builder.sort(orderKey, SortOrder.DESC);
            // 设置搜索条件
            searchRequest.source(builder);
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            return searchResponse;
        } catch (Exception e) {
            log.error("获取需量最大错误" + e);
        }
        return null;
    }

    @Override
    public BusIndexStatisticsResVO getBoxIndexStatistics() {
        return boxIndexCopyMapper.getBoxIndexStatistics();
    }

    @Override
    public BalanceStatisticsVO getBoxBalanceStatistics() {
        return boxIndexCopyMapper.getBoxBalanceStatistics();
    }

    @Override
    public ReportBasicInformationResVO getReportBasicInformationResVO(BoxIndexPageReqVO pageReqVO) {
        BoxIndex boxIndex = boxIndexMapper.selectOne(new LambdaUpdateWrapper<BoxIndex>().eq(BoxIndex::getBoxKey, pageReqVO.getDevKey()));
        ReportBasicInformationResVO vo = new ReportBasicInformationResVO();
        Object obj;
        try {
            obj = redisTemplate.opsForValue().get(REDIS_KEY_BOX + boxIndex.getBoxKey());
        } catch (Exception e) {
            throw exception(NOT_BOX);
        }
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));

        ArrayList<String> list = new ArrayList<>();
        list.add(boxIndex.getBoxKey());
        Map<String, String> positionByKey = getPositionByKeys(list);
        vo.setLocation(positionByKey.get(boxIndex.getBoxKey()));
        vo.setDevKey(boxIndex.getBoxKey());

        vo.setRunStatus(boxIndex.getRunStatus());
        if (Objects.nonNull(jsonObject)) {
            vo.setPowApparent(jsonObject.getJSONObject("box_data").getJSONObject("box_total_data").getBigDecimal("pow_apparent"));
            vo.setPowerFactor(jsonObject.getJSONObject("box_data").getJSONObject("box_total_data").getBigDecimal("power_factor"));
        }
        return vo;
    }

    @Override
    public LoadRateStatus getBoxIndexLoadRateStatus() {
        return boxIndexCopyMapper.getBoxIndexLoadRateStatus();
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
            boxIndexRes.setLoadRateStatus(boxIndexDO.getLoadRateStatus());
            res.add(boxIndexRes);
        }
        Map<String, BoxIndexRes> resMap = res.stream().collect(Collectors.toMap(BoxIndexRes::getDevKey, Function.identity()));
        List<String> keys = res.stream().map(BoxResBase::getDevKey).distinct().collect(Collectors.toList());
        Map<String, BoxNameVO> roomByKeys = getRoomByKeys(keys);
//        getPosition(res);
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
            BoxNameVO boxNameVO = roomByKeys.get(devKey);
            if (Objects.nonNull(boxNameVO)) {
                boxIndexRes.setLocation(boxNameVO.getLocaltion());
                boxIndexRes.setBusName(boxNameVO.getBusName());
                boxIndexRes.setRoomName(boxNameVO.getRoomName());
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
        List<String> keys = res.stream().map(BoxResBase::getDevKey).distinct().collect(Collectors.toList());
        Map<String, BoxNameVO> roomByKeys = getRoomByKeys(keys);
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

            List<Double> phaseVolValue = lineItemList.getList("vol_value", Double.class);
            List<Double> phaseCurValue = lineItemList.getList("cur_value", Double.class);
            List<Double> phasePowValue = lineItemList.getList("pow_active", Double.class);
            List<Double> phaseReactivePowValue = lineItemList.getList("pow_reactive", Double.class);
            List<Double> phaseApparentPowValue = lineItemList.getList("pow_apparent", Double.class);
            List<Double> phasePowFactor = lineItemList.getList("power_factor", Double.class);

            boxRedisDataRes.setPhaseVol(phaseVolValue);
            boxRedisDataRes.setPhaseCur(phaseCurValue);
            boxRedisDataRes.setPhaseActivePow(phasePowValue);
            boxRedisDataRes.setPhaseReactivePow(phaseReactivePowValue);
            boxRedisDataRes.setPhaseApparentPow(phaseApparentPowValue);
            boxRedisDataRes.setPhasePowFactor(phasePowFactor);
            //回路数据解析
            JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");
            List<Double> loopVolValue = loopItemList.getList("vol_value", Double.class);
            List<Integer> loopVolStatus = loopItemList.getList("vol_status", Integer.class);
            List<Double> loopCurValue = loopItemList.getList("cur_value", Double.class);
            List<Integer> loopCurStatus = loopItemList.getList("cur_status", Integer.class);
            List<Double> loopPowValue = loopItemList.getList("pow_value", Double.class);
            List<Integer> loopPowStatus = loopItemList.getList("pow_status", Integer.class);
            List<Double> loopReactivePowValue = loopItemList.getList("pow_reactive", Double.class);

            List<String> loopCurColor = new ArrayList<>();
            List<String> loopVolColor = new ArrayList<>();
            List<String> loopPowColor = new ArrayList<>();
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
            List<Double> outletPowValue = outletItemList.getList("pow_active", Double.class);
            List<Double> outletReactivePowValue = outletItemList.getList("pow_reactive", Double.class);
            List<Double> outletApparentPowValue = outletItemList.getList("pow_apparent", Double.class);
            List<Double> outletPowFactor = outletItemList.getList("power_factor", Double.class);

            boxRedisDataRes.setOutletActivePow(outletPowValue);
            boxRedisDataRes.setOutletReactivePow(outletReactivePowValue);
            boxRedisDataRes.setOutletApparentPow(outletApparentPowValue);
            boxRedisDataRes.setOutletPowFactor(outletPowFactor);

            JSONObject boxTotalData = jsonObject.getJSONObject("box_data").getJSONObject("box_total_data");
            boxRedisDataRes.setPowActive(boxTotalData.getBigDecimal("pow_active"));//总有功功率
            boxRedisDataRes.setPowApparent(boxTotalData.getBigDecimal("pow_apparent"));//总视在功率
            boxRedisDataRes.setPowReactive(boxTotalData.getBigDecimal("pow_reactive"));//总无功功率

            BoxNameVO boxNameVO = roomByKeys.get(devKey);
            if (Objects.nonNull(boxNameVO)) {
                boxRedisDataRes.setLocation(boxNameVO.getLocaltion());
                boxRedisDataRes.setBusName(boxNameVO.getBusName());
                boxRedisDataRes.setRoomName(boxNameVO.getRoomName());
            }

        }
        return new PageResult<>(res, boxIndexDOPageResult.getTotal());
    }

    @Override
    public BoxPowerDetailRedisResVO getBoxPowerRedisData(String devKey, String type) throws IOException {
        BoxIndex boxIndex = boxIndexMapper.selectOne(new LambdaQueryWrapper<BoxIndex>().eq(BoxIndex::getBoxKey, devKey));

        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get(REDIS_KEY_BOX + devKey);
        BoxPowerDetailRedisResVO vo = new BoxPowerDetailRedisResVO();
        if (jsonObject == null) {
            return vo;
        }
        vo.setBusName(jsonObject.getString("bus_name"));
        vo.setBoxName(jsonObject.getString("box_name"));
        vo.setDevKey(devKey);
        vo.setUpdateTime(jsonObject.getString("datetime"));//当前时间
        JSONArray temArr = jsonObject.getJSONObject("env_item_list").getJSONArray("tem_value");//温度
        vo.setTemValue(temArr.toList(Double.class));

        JSONObject boxTotalData = jsonObject.getJSONObject("box_data").getJSONObject("box_total_data");//总
        vo.setTotalPowerFactor(boxTotalData.getDouble("power_factor"));
        vo.setTotalPowApparent(boxTotalData.getDouble("pow_apparent"));
        vo.setTotalPowActive(boxTotalData.getDouble("pow_active"));
        vo.setTotalPowReactive(boxTotalData.getDouble("pow_reactive"));
        vo.setTotalEleActive(boxTotalData.getDouble("ele_active"));

        Integer loopNum = jsonObject.getJSONObject("box_data").getJSONObject("box_cfg").getInteger("loop_num");
        JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");//回路
        List<Integer> breakerStatus = jsonObject.getJSONObject("box_data").getJSONObject("box_cfg").getList("breaker_status", Integer.class);
        List<BoxLoopItemResVO> list = new ArrayList<>();
        for (Integer i = 0; i < loopNum; i++) {
            List<Double> curList = loopItemList.getList("cur_value", Double.class);//电流
            List<Double> volList = loopItemList.getList("vol_value", Double.class);//电压
            List<Double> powReactive = loopItemList.getList("pow_reactive", Double.class);//无功功率
            List<Double> powApparent = loopItemList.getList("pow_apparent", Double.class);//视在功率
            List<Double> powValue = loopItemList.getList("pow_value", Double.class);//有功功率值
            List<Double> powerFactor = loopItemList.getList("power_factor", Double.class);//功率因素
            List<Double> eleActive = loopItemList.getList("ele_active", Double.class);//电能
            BoxLoopItemResVO loopItemResVO = new BoxLoopItemResVO();
            loopItemResVO.setLoopId(i + 1);
            if (i + 1 <= breakerStatus.size()) {
                loopItemResVO.setBreakerStatus(breakerStatus.get(i));
            }
            loopItemResVO.setLoopCurValue(BigDecimal.valueOf(curList.get(i)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            loopItemResVO.setLoopVolValue(BigDecimal.valueOf(volList.get(i)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            loopItemResVO.setLoopPowReactive(BigDecimal.valueOf(powReactive.get(i)).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            loopItemResVO.setLoopPowApparent(BigDecimal.valueOf(powApparent.get(i)).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            loopItemResVO.setLoopPowValue(BigDecimal.valueOf(powValue.get(i)).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            loopItemResVO.setLoopPowerFactor(BigDecimal.valueOf(powerFactor.get(i)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            loopItemResVO.setLoopEleActive(BigDecimal.valueOf(eleActive.get(i)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            list.add(loopItemResVO);
        }
        vo.setBoxLoopItemResVO(list);

        JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");//相数据
        List<Double> volList = lineItemList.getList("vol_value", Double.class);
        List<Double> curList = lineItemList.getList("cur_value", Double.class);
        vo.setLineVolValue(volList);
        vo.setLineCurValue(curList);
        vo.setLinePowActive(lineItemList.getList("pow_active", Double.class));
        vo.setLinePowApparent(lineItemList.getList("pow_apparent", Double.class));
        vo.setLinePowReactive(lineItemList.getList("pow_reactive", Double.class));
        vo.setLineLoadRate(lineItemList.getList("load_rate", Double.class));
        vo.setLineEleActive(lineItemList.getList("ele_active", Double.class));
        vo.setLineEleReactive(lineItemList.getList("ele_reactive", Double.class));
        vo.setLinePowerFactor(lineItemList.getList("power_factor", Double.class));
        vo.setLineCurThd(lineItemList.getList("cur_thd", Double.class));

        Double curAvg = curList.stream().mapToDouble(i -> i).average().getAsDouble();
        Double volAvg = volList.stream().mapToDouble(i -> i).average().getAsDouble();
        Double curUnbalance = curAvg == 0 ? 0 : (Collections.max(curList) - curAvg) / curAvg * 100;
        Double volUnbalance = volAvg == 0 ? 0 : (Collections.max(volList) - Collections.min(volList)) / volAvg * 100;
        vo.setVub(curUnbalance);//电流均衡
        vo.setCub(volUnbalance);//电压均衡
        List<Double> loadRate = lineItemList.getList("load_rate", Double.class);
        vo.setLoadFactor(loadRate.stream().mapToDouble(i -> i).average().getAsDouble());

        JSONObject outletItemList = jsonObject.getJSONObject("box_data").getJSONObject("outlet_item_list");
        vo.setOutletPowActive(outletItemList.getList("pow_active", Double.class));
        vo.setOutletPowApparent(outletItemList.getList("pow_apparent", Double.class));
        vo.setOutletPowReactive(outletItemList.getList("pow_reactive", Double.class));
        vo.setOutletEleActive(outletItemList.getList("ele_active", Double.class));
        vo.setOutletEleApparent(outletItemList.getList("ele_apparent", Double.class));
        vo.setOutletEleReactive(outletItemList.getList("ele_reactive", Double.class));
        vo.setOutletPowerFactor(outletItemList.getList("power_factor", Double.class));

        if (true) {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.termsQuery("box_id", boxIndex.getId().toString()));
            // 搜索源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQuery);
            searchSourceBuilder.size(1);
            searchSourceBuilder.fetchSource(new String[]{"create_time", "load_rate"}, null);
            searchSourceBuilder.sort("load_rate", SortOrder.DESC);
            SearchRequest searchRequest1 = new SearchRequest();
            searchRequest1.indices("box_hda_line_realtime");
            //query条件--正常查询条件
            searchRequest1.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse1.getHits();
            if (hits.getTotalHits().value > 0) {
                SearchHit hit = hits.getAt(0);
                vo.setLoadFactorTime(hit.getSourceAsMap().get("create_time").toString());
                vo.setLoadFactorValue((Double) hit.getSourceAsMap().get("load_rate"));
            }
        }
        if (true) {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.termsQuery("box_id", boxIndex.getId().toString()));
            // 搜索源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQuery);
            searchSourceBuilder.size(1);
            searchSourceBuilder.fetchSource(new String[]{"create_time", "pow_active"}, null);
            searchSourceBuilder.sort("pow_active", SortOrder.DESC);
            SearchRequest searchRequest1 = new SearchRequest();
            searchRequest1.indices("box_hda_line_realtime");
            //query条件--正常查询条件
            searchRequest1.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse1.getHits();
            if (hits.getTotalHits().value > 0) {
                SearchHit hit = hits.getAt(0);
                vo.setPowActiveTime(hit.getSourceAsMap().get("create_time").toString());
                vo.setPowActiveValue(hit.getSourceAsMap().get("pow_active").toString());
            }
        }
        return vo;
    }

    public Map getCabinetDistributionFactor(int id, String type) {
        String startTime = null;
        String endTime = null;
        String index = null;
        switch (type) {
            case "day":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusDays(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
                index = "cabinet_hda_pow_hour";
                break;
            case "hour":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusHours(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
                index = "box_hda_line_realtime";
                break;
            case "today":
                startTime = LocalDateTimeUtil.format(LocalDateTime.MIN, "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
                index = "box_hda_line_hour";
                break;
            case "threeDay":
                startTime = LocalDateTimeUtil.format(LocalDateTime.now().minusDays(3), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
                index = "box_hda_line_hour";
                break;
            default:
        }

        Map map = new HashMap();
        //day,today,threeDay
        List<Map<String, Object>> data = getDataEs(startTime, endTime, Collections.singletonList(id),
                index, Map.class);
        List<BigDecimal> factorA = new ArrayList<>();
        List<BigDecimal> factorB = new ArrayList<>();
        List<BigDecimal> factorC = new ArrayList<>();
        List<String> createTime = new ArrayList<>();
        Map<Integer, List<Map<String, Object>>> lineMap = data.stream().collect(Collectors.groupingBy(i -> (Integer) i.get("line_id")));

        switch (index) {
            case "box_hda_line_realtime":
                factorA = lineMap.get(1).stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("load_rate").toString()), 100)).collect(Collectors.toList());
                factorB = lineMap.get(2).stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("load_rate").toString()), 100)).collect(Collectors.toList());
                factorC = lineMap.get(3).stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("load_rate").toString()), 100)).collect(Collectors.toList());
                createTime = lineMap.get(1).stream().map(i -> String.valueOf(i.get("create_time"))).collect(Collectors.toList());
                break;
            case "box_hda_line_hour":
                factorA = lineMap.get(1).stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("load_rate").toString()), 100)).collect(Collectors.toList());
                factorB = lineMap.get(2).stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("load_rate").toString()), 100)).collect(Collectors.toList());
                factorC = lineMap.get(3).stream().map(i -> BigDemicalUtil.safeMultiply(Double.parseDouble(i.get("load_rate").toString()), 100)).collect(Collectors.toList());
                createTime = lineMap.get(1).stream().map(i -> String.valueOf(i.get("create_time"))).collect(Collectors.toList());
                break;
        }
        map.put("factorA", factorA);
        map.put("factorB", factorB);
        map.put("factorC", factorC);
        map.put("day", createTime);
        return map;
    }

    private List getDataEs(String startTime, String endTime, List<Integer> ids, String index, Class objClass) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                    .must(QueryBuilders.termsQuery("box_id", ids))));
            builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
            // 设置搜索条件
            searchRequest.source(builder);
            builder.size(10000);

            List list = new ArrayList<>();
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse != null) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    list.add(JsonUtils.parseObject(hit.getSourceAsString(), objClass));
                }
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
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
    public PageResult<BoxIndexDTO> getEqPage1(BoxIndexPageReqVO pageReqVO) {
        String indices = null;
        LocalDate now = LocalDate.now();
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now();
        String startTime = null;
        String endTime = null;
        List<BoxIndex> boxIndices1 = boxIndexMapper.selectList(new LambdaUpdateWrapper<BoxIndex>().eq(BoxIndex::getBoxType, 1));
        List<Integer> boxIds = boxIndices1.stream().map(BoxIndex::getId).collect(Collectors.toList());
        Integer total = 0;
        switch (pageReqVO.getTimeGranularity()) {
            case "yesterday":
                indices = "box_eq_total_day";
                startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                break;
            case "lastWeek":
                indices = "box_eq_total_week";
                startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                break;
            case "lastMonth":
                indices = "box_eq_total_month";
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
                    .mustNot(QueryBuilders.termQuery("box_id", boxIds))));

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.indices(indices);
            searchRequest.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            List<BoxEqTotalDayDo> list = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits()) {
                BoxEqTotalDayDo dayDo = JsonUtils.parseObject(hit.getSourceAsString(), BoxEqTotalDayDo.class);
                list.add(dayDo);
            }

            if (!CollectionUtils.isEmpty(list)) {
                List<Integer> ids = list.stream().map(BoxBaseDo::getBoxId).collect(Collectors.toList());
                List<BoxIndex> boxIndices = boxIndexCopyMapper.selectList(new LambdaUpdateWrapper<BoxIndex>().in(BoxIndex::getId, ids));
                Map<Integer, BoxIndex> boxIndexMap = boxIndices.stream().collect(Collectors.toMap(BoxIndex::getId, x -> x));
                List<BoxIndexDTO> result = new ArrayList<>();

                startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                List<String> yesterdayList = getData(startTime, endTime, ids, "box_eq_total_day");
                Map<Integer, Double> yesterdayMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(yesterdayList)) {
                    yesterdayList.forEach(str -> {
                        BoxEqTotalDayDo dayDo = JsonUtils.parseObject(str, BoxEqTotalDayDo.class);
                        yesterdayMap.put(dayDo.getBoxId(), dayDo.getEq());
                    });
                }

                //上周
                startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
                List<String> weekList = getData(startTime, endTime, ids, "box_eq_total_week");
                Map<Integer, Double> weekMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(weekList)) {
                    weekList.forEach(str -> {
                        BoxEqTotalWeekDo weekDo = JsonUtils.parseObject(str, BoxEqTotalWeekDo.class);
                        weekMap.put(weekDo.getBoxId(), weekDo.getEq());
                    });
                }

                //上月
                startTime = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
                List<String> monthList = getData(startTime, endTime, ids, "box_eq_total_month");
                Map<Integer, Double> monthMap = new HashMap<>();
                if (!CollectionUtils.isEmpty(monthList)) {
                    monthList.forEach(str -> {
                        BoxEqTotalMonthDo monthDo = JsonUtils.parseObject(str, BoxEqTotalMonthDo.class);
                        monthMap.put(monthDo.getBoxId(), monthDo.getEq());
                    });
                }

                Map<String, BoxNameVO> map = getRoomByKeys(result.stream().map(BoxResBase::getDevKey).collect(Collectors.toList()));
                list.forEach(vo -> {
                    Integer id = vo.getBoxId();
                    BoxIndex boxIndex = boxIndexMap.get(id);
                    BoxIndexDTO boxIndexDTO = new BoxIndexDTO().setId(boxIndex.getId()).setRunStatus(boxIndex.getRunStatus());
                    boxIndexDTO.setDevKey(boxIndex.getBoxKey());
                    BoxNameVO nameVO = map.get(boxIndex.getBoxKey());
                    if (Objects.nonNull(nameVO)) {
                        boxIndexDTO.setBusName(nameVO.getBusName());
                        boxIndexDTO.setLocation(nameVO.getLocaltion());
                        boxIndexDTO.setRoomName(nameVO.getRoomName());
                    }
                    boxIndexDTO.setBoxName(boxIndex.getBoxName());
                    boxIndexDTO.setYesterdayEq(yesterdayMap.get(id));
                    boxIndexDTO.setLastWeekEq(weekMap.get(id));
                    boxIndexDTO.setLastMonthEq(monthMap.get(id));
                    if (boxIndexDTO.getYesterdayEq() == null) {
                        boxIndexDTO.setYesterdayEq(0.0);
                    }
                    if (boxIndexDTO.getLastWeekEq() == null) {
                        boxIndexDTO.setLastWeekEq(0.0);
                    }
                    if (boxIndexDTO.getLastMonthEq() == null) {
                        boxIndexDTO.setLastMonthEq(0.0);
                    }
                    result.add(boxIndexDTO);
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
    public PageResult<BoxIndexDTO> getEqPage(BoxIndexPageReqVO pageReqVO) {
        try {
            pageReqVO.setIsDeleted(0);
            PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
            List<BoxIndex> boxIndexDOList = boxIndexDOPageResult.getList();

            List<BoxIndexDTO> result = new ArrayList<>();
            List<Integer> ids = boxIndexDOList.stream().map(BoxIndex::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                return new PageResult<>(result, boxIndexDOPageResult.getTotal());
            }
            boxIndexDOList.forEach(boxIndex -> {
                BoxIndexDTO boxIndexDTO = new BoxIndexDTO().setId(boxIndex.getId()).setRunStatus(boxIndex.getRunStatus());
                boxIndexDTO.setDevKey(boxIndex.getBoxKey());
                boxIndexDTO.setBoxName(boxIndex.getBoxName());
                result.add(boxIndexDTO);
            });
            //昨日
            LocalDate now = LocalDate.now();
            // 获取昨天的日期
            LocalDate yesterday = LocalDate.now();
            String startTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
            String endTime = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
            List<String> yesterdayList = getData(startTime, endTime, ids, "box_eq_total_day");
            Map<Integer, Double> yesterdayMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(yesterdayList)) {
                yesterdayList.forEach(str -> {
                    BoxEqTotalDayDo dayDo = JsonUtils.parseObject(str, BoxEqTotalDayDo.class);
                    yesterdayMap.put(dayDo.getBoxId(), dayDo.getEq());
                });
            }

            //上周
            startTime = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
            endTime = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
            List<String> weekList = getData(startTime, endTime, ids, "box_eq_total_week");
            Map<Integer, Double> weekMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(weekList)) {
                weekList.forEach(str -> {
                    BoxEqTotalWeekDo weekDo = JsonUtils.parseObject(str, BoxEqTotalWeekDo.class);
                    weekMap.put(weekDo.getBoxId(), weekDo.getEq());
                });
            }

            //上月
            startTime = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
            endTime = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
            List<String> monthList = getData(startTime, endTime, ids, "box_eq_total_month");
            Map<Integer, Double> monthMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(monthList)) {
                monthList.forEach(str -> {
                    BoxEqTotalMonthDo monthDo = JsonUtils.parseObject(str, BoxEqTotalMonthDo.class);
                    monthMap.put(monthDo.getBoxId(), monthDo.getEq());
                });
            }
            Map<String, BoxNameVO> map = getRoomByKeys(result.stream().map(BoxResBase::getDevKey).collect(Collectors.toList()));
            result.forEach(dto -> {
//                dto.setLocation(map.get(dto.getDevKey()));
                BoxNameVO nameVO = map.get(dto.getDevKey());
                if (Objects.nonNull(nameVO)) {
                    dto.setBusName(nameVO.getBusName());
                    dto.setLocation(nameVO.getLocaltion());
                    dto.setRoomName(nameVO.getRoomName());
                }
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

//            getPosition(result);
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
    public Map getAvgBoxHdaLoopForm(BoxIndexPageReqVO pageReqVO) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        BusLineResBase linCureRes = new BusLineResBase();
        BusLineResBase linVoleRes = new BusLineResBase();
        BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, pageReqVO.getDevKey()));
        Object obj = redisTemplate.opsForValue().get(REDIS_KEY_BOX + boxIndex.getBoxKey());
        if (boxIndex != null) {
            String index;
            boolean isSameDay = false;
            if (pageReqVO.getTimeType().equals(0)) {
                index = "box_hda_loop_hour";
                isSameDay = true;
            } else {
                index = "box_hda_loop_day";
                isSameDay = false;
            }

            String start = LocalDateTimeUtil.format(pageReqVO.getOldTime(), "yyyy-MM-dd HH:mm:ss");
            String end = LocalDateTimeUtil.format(pageReqVO.getNewTime(), "yyyy-MM-dd HH:mm:ss");
            List<String> cabinetData = getEsStrings(index, start, end, boxIndex);
            Map<Integer, List<BusHdaLoopAvgResVO>> envMap = cabinetData.stream()
                    .map(str -> JsonUtils.parseObject(str, BusHdaLoopAvgResVO.class))
                    .collect(Collectors.groupingBy(BusHdaLoopAvgResVO::getLoopId));
            boolean isFisrt = false;
            List<String> time = null;

            for (int i = 1; i < envMap.size() + 1; i++) {
                if (CollectionUtil.isEmpty(envMap.get(i))) {
                    continue;
                }
                List<String> curHappenTime = new ArrayList<>();
                List<String> volHappenTime = new ArrayList<>();
                List<BusHdaLoopAvgResVO> hourDoList = envMap.get(i);
                //数据收集
                LineSeries linCureSeries = new LineSeries();
                LineSeries linVoleSeries = new LineSeries();
                List<Float> loopCur = null;
                List<Float> loopVol = null;
                //收集描述信息（峰值、谷值、发生时间）
                Float loopMax = 0f;
                Float loopMin = Float.MAX_VALUE;
                Float loopVolMax = 0f;
                Float loopVolMin = Float.MAX_VALUE;

                String loopMaxTime = "";
                String loopMinTime = "";
                String loopVolMaxTime = "";
                String loopVolMinTime = "";

                //收集曲线数据（dataType（1=最大值,0=平均值,-1=最小值））
                if (pageReqVO.getDataType() == 1) {
                    linCureSeries.setName(getCurVolSeriesName("电流", i, pageReqVO.getDataType()));
                    linVoleSeries.setName(getCurVolSeriesName("电压", i, pageReqVO.getDataType()));
                    loopCur = hourDoList.stream().map(BusHdaLoopAvgResVO::getCurMaxValue).collect(Collectors.toList());
                    loopVol = hourDoList.stream().map(BusHdaLoopAvgResVO::getVolMaxValue).collect(Collectors.toList());

                    curHappenTime = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    volHappenTime = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getVolMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());

                    for (BusHdaLoopAvgResVO pduHdaLoopBaseDo : hourDoList) {
                        if (loopMax <= pduHdaLoopBaseDo.getCurMaxValue()) {
                            loopMax = pduHdaLoopBaseDo.getCurMaxValue();
                            loopMaxTime = pduHdaLoopBaseDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                        if (loopMin >= pduHdaLoopBaseDo.getCurMaxValue()) {
                            loopMin = pduHdaLoopBaseDo.getCurMaxValue();
                            loopMinTime = pduHdaLoopBaseDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                        }

                        if (loopVolMax <= pduHdaLoopBaseDo.getVolMaxValue()) {
                            loopVolMax = pduHdaLoopBaseDo.getVolMaxValue();
                            loopVolMaxTime = pduHdaLoopBaseDo.getVolMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                        if (loopVolMin >= pduHdaLoopBaseDo.getVolMaxValue()) {
                            loopVolMin = pduHdaLoopBaseDo.getVolMaxValue();
                            loopVolMinTime = pduHdaLoopBaseDo.getVolMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                    }
                } else if (pageReqVO.getDataType() == 0) {
                    linCureSeries.setName(getCurVolSeriesName("电流", i, pageReqVO.getDataType()));
                    linVoleSeries.setName(getCurVolSeriesName("电压", i, pageReqVO.getDataType()));
                    loopCur = hourDoList.stream().map(BusHdaLoopAvgResVO::getCurAvgValue).collect(Collectors.toList());
                    loopVol = hourDoList.stream().map(BusHdaLoopAvgResVO::getVolAvgValue).collect(Collectors.toList());
                    for (BusHdaLoopAvgResVO pduHdaLoopBaseDo : hourDoList) {
                        if (loopMax <= pduHdaLoopBaseDo.getCurAvgValue()) {
                            loopMax = pduHdaLoopBaseDo.getCurAvgValue();
                            loopMaxTime = "无";
                        }
                        if (loopMin >= pduHdaLoopBaseDo.getCurAvgValue()) {
                            loopMin = pduHdaLoopBaseDo.getCurAvgValue();
                            loopMinTime = "无";
                        }

                        if (loopVolMax <= pduHdaLoopBaseDo.getVolAvgValue()) {
                            loopVolMax = pduHdaLoopBaseDo.getVolAvgValue();
                            loopVolMaxTime = "无";
                        }
                        if (loopVolMin >= pduHdaLoopBaseDo.getVolAvgValue()) {
                            loopVolMin = pduHdaLoopBaseDo.getVolAvgValue();
                            loopVolMinTime = "无";
                        }
                    }
                } else if (pageReqVO.getDataType() == -1) {
                    linCureSeries.setName(getCurVolSeriesName("电流", i, pageReqVO.getDataType()));
                    linVoleSeries.setName(getCurVolSeriesName("电压", i, pageReqVO.getDataType()));
                    loopCur = hourDoList.stream().map(BusHdaLoopAvgResVO::getCurMinValue).collect(Collectors.toList());
                    loopVol = hourDoList.stream().map(BusHdaLoopAvgResVO::getCurMinValue).collect(Collectors.toList());
                    curHappenTime = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getCurMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    volHappenTime = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getVolMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());

                    for (BusHdaLoopAvgResVO pduHdaLoopBaseDo : hourDoList) {
                        if (loopMax <= pduHdaLoopBaseDo.getCurMinValue()) {
                            loopMax = pduHdaLoopBaseDo.getCurMinValue();
                            loopMaxTime = pduHdaLoopBaseDo.getCurMinTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                        if (loopMin >= pduHdaLoopBaseDo.getCurMinValue()) {
                            loopMin = pduHdaLoopBaseDo.getCurMinValue();
                            loopMinTime = pduHdaLoopBaseDo.getCurMinTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                        if (loopVolMax <= pduHdaLoopBaseDo.getVolMinValue()) {
                            loopVolMax = pduHdaLoopBaseDo.getVolMinValue();
                            loopVolMaxTime = pduHdaLoopBaseDo.getVolMinTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                        if (loopVolMin >= pduHdaLoopBaseDo.getVolMinValue()) {
                            loopVolMin = pduHdaLoopBaseDo.getVolMinValue();
                            loopVolMinTime = pduHdaLoopBaseDo.getVolMinTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                    }
                }
                linCureSeries.setData(loopCur);
                linVoleSeries.setData(loopVol);
                if (!isFisrt) {
                    if (!isSameDay) {
                        time = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                    } else {
                        time = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                    }
                    linCureRes.setTime(time);
                    linVoleRes.setTime(time);
                    isFisrt = true;
                }
                linCureSeries.setHappenTime(curHappenTime);
                linVoleSeries.setHappenTime(volHappenTime);
                linCureRes.getSeries().add(linCureSeries);
                linVoleRes.getSeries().add(linVoleSeries);

                result.put("linCureRes", linCureRes);
                result.put("linVoleRes", linVoleRes);

                result.put("loopVolMax" + i, loopVolMax);
                result.put("loopVolMaxTime" + i, loopVolMaxTime);
                result.put("loopVolMin" + i, loopVolMin);
                result.put("loopVolMinTime" + i, loopVolMinTime);
                result.put("VPName" + i, "回路" + i + "电压");

                result.put("loopCurMax" + i, loopMax);
                result.put("loopCurMin" + i, loopMin);
                result.put("loopCurMaxTime" + i, loopMaxTime);
                result.put("loopCurMinTime" + i, loopMinTime);
                result.put("CPName" + i, "回路" + i + "电流");
            }
        }
        return result;
    }

    private String getCurVolSeriesName(String type, int Id, int dataType) {
        String lineName = "C" + Id;
//        return lineName+ Id + DataNameType.fromValue(dataType).name() + type + "曲线";
        return lineName;
    }

    @Override
    public BusIndexStatisticsResVO getBoxIndexStatisticsAll() {
        return boxIndexCopyMapper.getBoxIndexStatisticsAll();
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
        return boxIndexMapper.findKeys(key, flag);
    }


    @Override
    public List<BoxIndexMaxEqResVO> getMaxEq() {
        List<BoxIndexMaxEqResVO> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        // 获取昨天的日期
        LocalDate yesterday = LocalDate.now();

        // 昨天的起始时间（00:00:00）
        String start = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        String end = LocalDateTimeUtil.format(yesterday.atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");
        //借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
        extractedMaxEq("box_eq_total_day", start, end, result, 0);

        //上周
        start = LocalDateTimeUtil.format(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atTime(LocalTime.MIN), "yyyy-MM-dd HH:mm:ss");
        end = LocalDateTimeUtil.format(now.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX), "yyyy-MM-dd HH:mm:ss");

        extractedMaxEq("box_eq_total_week", start,
                end, result, 1);

        //上月
        start = LocalDateTimeUtil.format(now.withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");
        end = LocalDateTimeUtil.format(now.plusMonths(1).withDayOfMonth(1), "yyyy-MM-dd HH:mm:ss");

        extractedMaxEq("box_eq_total_month", start, end, result, 2);

        List<String> collect = result.stream().map(BoxResBase::getDevKey).collect(Collectors.toList());
        Map<String, String> map = getPositionByKeys(collect);
        for (BoxIndexMaxEqResVO resVO : result) {
            String location = map.get(resVO.getDevKey());
            resVO.setLocation(location);
        }
        return result;
    }

    private void extractedMaxEq(String indexEs, String startTime, String endTime, List<BoxIndexMaxEqResVO> result, Integer type) {
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
                BoxIndexMaxEqResVO boxIndexDTO = new BoxIndexMaxEqResVO();
                boxIndexDTO.setMaxEq((Double) sourceAsMap.get("eq_value"));
                boxIndexDTO.setBoxId((Integer) sourceAsMap.get("box_id"));
                BoxIndex boxIndex = boxIndexCopyMapper.selectOne(BoxIndex::getId, boxIndexDTO.getBoxId());
                boxIndexDTO.setDevKey(boxIndex.getBoxKey());
                boxIndexDTO.setBoxName(boxIndex.getBoxName());
                boxIndexDTO.setId(type);//借用id值来辅助判断是哪个时间的集合，0为昨天，1为上周，2为上月
                result.add(boxIndexDTO);
            }
        } catch (Exception e) {
            log.error("插接箱用能最大查询异常：" + e);
        }
    }


    @Override
    public BusActivePowDTO getActivePow(BusPowVo vo) {
        BusActivePowDTO powDTO = new BusActivePowDTO();
        try {

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            String startTime = DateUtil.formatDateTime(TimeUtil.getStartOfDay(calendar.getTime()));
            String endTime = DateUtil.formatDateTime(TimeUtil.getEndOfDay(calendar.getTime()));

//            log.info("startTime : " + startTime + "endTime：" + endTime);
            //获取昨日数据
            List<String> yesterdayData = getData(startTime, endTime, vo, "box_hda_total_hour");

            LocalDate old = LocalDate.now().minusDays(1);
            LocalDate now = LocalDate.now();
            List<BusActivePowTrendDTO> yesterdayList = new ArrayList<>();
            List<BusActivePowTrendDTO> todayList = new ArrayList<>();

            for (int i = 0; i < 24; i++) {
                String oldDay = LocalDateTimeUtil.format(LocalDateTime.of(old, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                BusActivePowTrendDTO dto = new BusActivePowTrendDTO();
                dto.setDateTime(oldDay);
                dto.setActivePow("0");
                dto.setActivePowMax("");
                dto.setActivePowMin("");
                yesterdayList.add(dto);
                BusActivePowTrendDTO dto1 = new BusActivePowTrendDTO();
                String nowDay = LocalDateTimeUtil.format(LocalDateTime.of(now, LocalTime.of(i, 0, 0)), "yyyy-MM-dd HH:mm");
                dto1.setDateTime(nowDay);
                dto1.setActivePow("");
                dto.setActivePowMax("");
                dto.setActivePowMin("");
                todayList.add(dto1);
            }
            Map<String, BusActivePowTrendDTO> yesMap = yesterdayList.stream().collect(Collectors.toMap(BusActivePowTrendDTO::getDateTime, x -> x));
            yesterdayData.forEach(str -> {
                BoxTotalHourDo hourDo = JsonUtils.parseObject(str, BoxTotalHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH:mm");// + TIME_STR
                BusActivePowTrendDTO dto = yesMap.get(dateTime);
                dto.setActivePow(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveAvgValue(), 3)));
                dto.setActivePowMax(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveMaxValue(), 3)));
                dto.setActivePowMaxTime(hourDo.getPowActiveMaxTime());
                dto.setActivePowMin(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveMinValue(), 3)));
                dto.setActivePowMinTime(hourDo.getPowActiveMinTime());
            });

            startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());

//            log.info("startTime : " + startTime + "endTime：" + endTime);
            //获取今日数据
            Map<String, BusActivePowTrendDTO> todayMap = todayList.stream().collect(Collectors.toMap(BusActivePowTrendDTO::getDateTime, x -> x));
            List<String> todayData = getData(startTime, endTime, vo, "box_hda_total_hour");
            todayData.forEach(str -> {
                BoxTotalHourDo hourDo = JsonUtils.parseObject(str, BoxTotalHourDo.class);
                String dateTime = hourDo.getCreateTime().toString("yyyy-MM-dd HH:mm");
                BusActivePowTrendDTO dto = todayMap.get(dateTime);
                dto.setActivePow(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveAvgValue(), 3)));
                dto.setActivePowMax(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveMaxValue(), 3)));
                dto.setActivePowMaxTime(hourDo.getPowActiveMaxTime());
                dto.setActivePowMin(String.valueOf(BigDemicalUtil.setScale(hourDo.getPowActiveMinValue(), 3)));
                dto.setActivePowMinTime(hourDo.getPowActiveMinTime());
            });

            powDTO.setYesterdayList(yesterdayList);
            powDTO.setTodayList(todayList);
            //获取峰值
            BusActivePowTrendDTO yesterdayMax = yesterdayList.stream().max(Comparator.comparing(BusActivePowTrendDTO::getActivePowMax)).orElse(new BusActivePowTrendDTO());
            BusActivePowTrendDTO todayMax = todayList.stream().max(Comparator.comparing(BusActivePowTrendDTO::getActivePowMax)).orElse(new BusActivePowTrendDTO());
            powDTO.setTodayMax(Float.valueOf(todayMax.getActivePowMax()));
            powDTO.setTodayMaxTime(DateFormatUtils.format(todayMax.getActivePowMaxTime(), "yyyy-MM-dd HH:mm"));
            powDTO.setYesterdayMaxTime(DateFormatUtils.format(yesterdayMax.getActivePowMaxTime(), "yyyy-MM-dd HH:mm"));
            powDTO.setYesterdayMax(Float.valueOf(yesterdayMax.getActivePowMax()));

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
//        BoxCurbalanceColorDO boxCurbalanceColorDO = boxCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxBalanceDataRes> res = new ArrayList<>();
        List redisList = getMutiRedis(list);
        for (BoxIndex boxIndexDO : list) {
            BoxBalanceDataRes boxBalanceDataRes = new BoxBalanceDataRes();
            boxBalanceDataRes.setStatus(boxIndexDO.getRunStatus());
            boxBalanceDataRes.setBoxId(boxIndexDO.getId());
            boxBalanceDataRes.setDevKey(boxIndexDO.getBoxKey());
            boxBalanceDataRes.setBoxName(boxIndexDO.getBoxName());
            boxBalanceDataRes.setColor(boxIndexDO.getCurUnbalanceStatus());
            res.add(boxBalanceDataRes);
        }
        Map<String, BoxBalanceDataRes> resMap = res.stream().collect(Collectors.toMap(BoxBalanceDataRes::getDevKey, Function.identity()));
        List<String> keys = res.stream().map(BoxResBase::getDevKey).distinct().collect(Collectors.toList());
        Map<String, BoxNameVO> roomByKeys = getRoomByKeys(keys);
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

            BoxNameVO boxNameVO = roomByKeys.get(devKey);
            if (Objects.nonNull(boxNameVO)) {
                boxBalanceDataRes.setLocation(boxNameVO.getLocaltion());
                boxBalanceDataRes.setBusName(boxNameVO.getBusName());
                boxBalanceDataRes.setRoomName(boxNameVO.getRoomName());
            }


            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
            List<Double> curList = lineItemList.getList("cur_value", Double.class);
            List<Double> volList = lineItemList.getList("vol_value", Double.class);

            Double curAvg = curList.stream().mapToDouble(i -> i).average().getAsDouble();
            Double volAvg = volList.stream().mapToDouble(i -> i).average().getAsDouble();
            Double curUnbalance = curAvg == 0 ? 0 : (Collections.max(curList) - curAvg) / curAvg * 100;
            Double volUnbalance = volAvg == 0 ? 0 : (Collections.max(volList) - Collections.min(volList)) / volAvg * 100;
            for (int i = 0; i < 3; i++) {
                double vol = volList.get(i);
                double cur = curList.get(i);
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
            boxBalanceDataRes.setCurUnbalance(curUnbalance);
            boxBalanceDataRes.setVolUnbalance(volUnbalance);
//            if (pageReqVO.getColor() != null) {
//                if (!pageReqVO.getColor().contains(boxBalanceDataRes.getColor())) {
//                    res.removeIf(box -> box.getBoxId().equals(boxBalanceDataRes.getBoxId()));
//                }
//            }
        }
        return new PageResult<>(res, boxIndexDOPageResult.getTotal());
    }

    @Override
    public BusBalanceDeatilRes getBoxBalanceDetail(String devKey) {
        BusBalanceDeatilRes result = new BusBalanceDeatilRes();
        BoxIndex boxIndex = boxIndexMapper.selectOne(new LambdaQueryWrapper<BoxIndex>().eq(BoxIndex::getBoxKey, devKey).last("limit 1"));
        result.setBoxName(boxIndex.getBoxName());
        BoxCurbalanceColorDO boxCurbalanceColorDO = boxCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + devKey);
        if (jsonObject == null) {
            return result;
        }

        result.setBusName(jsonObject.getString("bus_name"));
        JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
        List<Double> curList = lineItemList.getList("cur_value", Double.class);
        List<Double> volList = lineItemList.getList("vol_value", Double.class);

        Double curAvg = curList.stream().mapToDouble(i -> i).average().getAsDouble();
        Double volAvg = volList.stream().mapToDouble(i -> i).average().getAsDouble();
        Double curUnbalance = curAvg == 0 ? 0 : (Collections.max(curList) - curAvg) / curAvg * 100;
        Double volUnbalance = volAvg == 0 ? 0 : (Collections.max(volList) - Collections.min(volList)) / volAvg * 100;

        result.setCur_value(curList);
        result.setVol_value(volList);
        result.setCurUnbalance(curUnbalance);
        result.setVolUnbalance(volUnbalance);
        double maxVal = 63.0;
        double a = Collections.max(curList) - Collections.min(curList);
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
    public List<BusTrendDTO> getBoxBalanceTrend(Integer boxId, Integer timeType) {
        List<BusTrendDTO> result = new ArrayList<>();
        try {
            List<Integer> ids = Arrays.asList(boxId);
            String startTime;
            String endTime;
            LocalDateTime now = LocalDateTime.now();
            String index = BOX_HDA_LINE_HOUR;
            startTime = LocalDateTimeUtil.format(now.minusHours(24), "yyyy-MM-dd HH:mm:ss");
            endTime = LocalDateTimeUtil.format(now, "yyyy-MM-dd HH:mm:ss");
            if (Objects.equals(timeType, 0)) {
                index = "box_hda_line_realtime";
                startTime = LocalDateTimeUtil.format(now.minusHours(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now, "yyyy-MM-dd HH:mm:ss");
            }
            List<String> data = getData(startTime, endTime, ids, index);

            Map<String, List> timeBus = new HashMap<>();
            data.forEach(str -> {
                if (Objects.equals(timeType, 0)) {
                    BoxLineRealtimeDo hourDo = JsonUtils.parseObject(str, BoxLineRealtimeDo.class);
                    String dateTime = DateUtil.format(hourDo.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                    List<BoxLineRealtimeDo> lineHourDos = timeBus.get(dateTime);
                    if (CollectionUtils.isEmpty(lineHourDos)) {
                        lineHourDos = new ArrayList<>();
                    }
                    lineHourDos.add(hourDo);
                    timeBus.put(dateTime, lineHourDos);
                } else {
                    BoxLineHourDo hourDo = JsonUtils.parseObject(str, BoxLineHourDo.class);
                    String dateTime = DateUtil.format(hourDo.getCreateTime(), "yyyy-MM-dd HH");
                    List<BoxLineHourDo> lineHourDos = timeBus.get(dateTime);
                    if (CollectionUtils.isEmpty(lineHourDos)) {
                        lineHourDos = new ArrayList<>();
                    }
                    lineHourDos.add(hourDo);
                    timeBus.put(dateTime, lineHourDos);
                }
            });

            timeBus.keySet().forEach(dateTime -> {
                BusTrendDTO trendDTO = new BusTrendDTO();
                trendDTO.setDateTime(dateTime);
                //获取相数据
                List<Map<String, Object>> cur = new ArrayList<>();
                List<Map<String, Object>> vol = new ArrayList<>();
                //获取每个时间段数据
                if (Objects.equals(timeType, 0)) {
                    List<BoxLineRealtimeDo> boxLineHourDos = timeBus.get(dateTime);
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
                    List<BoxLineHourDo> boxLineHourDos = timeBus.get(dateTime);
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
                trendDTO.setCur(cur.stream().distinct().collect(Collectors.toList()));
                trendDTO.setVol(vol.stream().distinct().collect(Collectors.toList()));
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
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPageAll(pageReqVO);
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
        List<String> keys = res.stream().map(BoxResBase::getDevKey).distinct().collect(Collectors.toList());
        Map<String, BoxNameVO> roomByKeys = getRoomByKeys(keys);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + "-" + jsonObject.getString("bar_id") + "-" + jsonObject.getString("addr");
            BoxTemRes boxTemRes = resMap.get(devKey);

            BoxNameVO boxNameVO = roomByKeys.get(devKey);
            if (Objects.nonNull(boxNameVO)) {
                boxTemRes.setLocation(boxNameVO.getLocaltion());
                boxTemRes.setBusName(boxNameVO.getBusName());
                boxTemRes.setRoomName(boxNameVO.getRoomName());
            }

            boxTemRes.setBusName(jsonObject.getString("bus_name"));
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
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<BoxTemHourDo> strList = getData(startTime, endTime, pageReqVO.getBoxId(), "box_tem_hour", BoxTemHourDo.class);
            strList.sort(Comparator.comparing(BoxTemHourDo::getCreateTime));

            HashMap<String, Object> resultMap = new HashMap<>();

            resultMap.put("table", strList);
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
        List<String> keys = res.stream().map(BoxResBase::getDevKey).distinct().collect(Collectors.toList());
        Map<String, BoxNameVO> roomByKeys = getRoomByKeys(keys);
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
            BoxNameVO boxNameVO = roomByKeys.get(devKey);
            if (Objects.nonNull(boxNameVO)) {
                boxPFRes.setLocation(boxNameVO.getLocaltion());
                boxPFRes.setBusName(boxNameVO.getBusName());
                boxPFRes.setRoomName(boxNameVO.getRoomName());
            }
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
        Map<String, List> map = new HashMap<>();
        try {
            pageReqVO.setNewTime(pageReqVO.getOldTime().withHour(23).withMinute(59).withSecond(59));
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(pageReqVO.getBoxId());
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            if (Objects.isNull(pageReqVO.getNewTime())) {
                pageReqVO.setNewTime(LocalDateTime.now());
            }
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<BoxPFDetail> strList = getData(startTime, endTime, pageReqVO.getBoxId(), "box_hda_outlet_hour", BoxPFDetail.class);
            if (CollectionUtils.isEmpty(strList)) {
                return map;
            }
//            strList.sort(Comparator.comparing(BoxPFDetail::getCreateTime));
//            Map<String, List<BoxPFDetail>> pfMap = strList.stream().collect(Collectors.groupingBy(i -> String.valueOf(i.getOutletId())));
            Map<String, List<BoxPFDetail>> pfMap = strList.stream().collect(Collectors.groupingBy(i -> String.valueOf(i.getCreateTime())));
            List<BoxPFDetailTotalResVO> resVOList = new ArrayList<>();
            for (String key : pfMap.keySet()) {

                List<BoxPFDetail> list = pfMap.get(key);
                BoxPFDetailTotalResVO resVO = new BoxPFDetailTotalResVO();
                resVO.setCreateTime(key);
                for (BoxPFDetail detail : list) {
                    switch (detail.getOutletId()) {
                        case 1:
                            resVO.setPowerFactorAvgValue(detail.getPowerFactorAvgValue());
                            resVO.setPowerFactorMaxValue(detail.getPowerFactorMaxValue());
                            resVO.setPowerFactorMaxTime(detail.getPowerFactorMaxTime());
                            resVO.setPowerFactorMinValue(detail.getPowerFactorMinValue());
                            resVO.setPowerFactorMinTime(detail.getPowerFactorMinTime());
                            break;
                        case 2:
                            resVO.setPowerFactorAvgValueb(detail.getPowerFactorAvgValue());
                            resVO.setPowerFactorMaxValueb(detail.getPowerFactorMaxValue());
                            resVO.setPowerFactorMaxTimeb(detail.getPowerFactorMaxTime());
                            resVO.setPowerFactorMinValueb(detail.getPowerFactorMinValue());
                            resVO.setPowerFactorMinTimeb(detail.getPowerFactorMinTime());
                            break;
                        case 3:
                            resVO.setPowerFactorAvgValuec(detail.getPowerFactorAvgValue());
                            resVO.setPowerFactorMaxValuec(detail.getPowerFactorMaxValue());
                            resVO.setPowerFactorMaxTimec(detail.getPowerFactorMaxTime());
                            resVO.setPowerFactorMinValuec(detail.getPowerFactorMinValue());
                            resVO.setPowerFactorMinTimec(detail.getPowerFactorMinTime());
                            break;
                        default:
                            break;
                    }
                }
                resVOList.add(resVO);
            }
            map.put("time", strList.stream().map(BoxPFDetail::getCreateTime).distinct().collect(Collectors.toList()));
            resVOList.sort(Comparator.comparing(BoxPFDetailTotalResVO::getCreateTime));
            map.put("table", resVOList);
            return map;
        } catch (Exception e) {
            log.error("获取数据失败：", e);
            return null;
        }
    }

    @Override
    public Map getBoxPFDetailNow(BoxIndexPageReqVO pageReqVO) {
        BoxIndex boxIndex = boxIndexMapper.selectById(pageReqVO.getBoxId());
        Object o = redisTemplate.opsForValue().get(REDIS_KEY_BOX + boxIndex.getBoxKey());
        if (Objects.isNull(o)) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(o));
        Map<String, List> map = new HashMap<>();


        String time = jsonObject.getString("sys_time");
        JSONObject outItem = jsonObject.getJSONObject("box_data").getJSONObject("outlet_item_list");
        if (Objects.isNull(outItem)) {
            return map;
        }
        List<BigDecimal> powerFactor = outItem.getList("power_factor", BigDecimal.class);
        if (CollectionUtils.isEmpty(powerFactor)) {
            return map;
        }

        List<BoxPFDetailTotalResVO> resVOList = new ArrayList<>();
        BoxPFDetailTotalResVO resVO = new BoxPFDetailTotalResVO();
        resVO.setCreateTime(time);
        resVOList.add(resVO);
        for (int i = 0; i < powerFactor.size(); i++) {
            switch (i) {
                case 0:
                    resVO.setPowerFactorAvgValue(powerFactor.get(i));
                    break;
                case 1:
                    resVO.setPowerFactorAvgValueb(powerFactor.get(i));
                    break;
                case 2:
                    resVO.setPowerFactorAvgValuec(powerFactor.get(i));
                    break;
                default:
                    break;
            }
        }
        map.put("time", resVOList.stream().map(BoxPFDetailTotalResVO::getCreateTime).distinct().collect(Collectors.toList()));
        map.put("table", resVOList);
        return map;
    }

    @Override
    public Map getAvgBoxHdaOutletEleForm(BoxIndexPageReqVO pageReqVO) {
        Map map = new HashMap<>();
        BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, pageReqVO.getDevKey()));
        if (boxIndex != null) {
            String index;
            boolean isSameDay;
            if (pageReqVO.getTimeType().equals(0)) {
                index = "box_ele_outlet";
                isSameDay = true;
            } else {
                index = "box_eq_outlet_day";
                isSameDay = false;
            }
            String start = LocalDateTimeUtil.format(pageReqVO.getOldTime(), "yyyy-MM-dd HH:mm:ss");
            String end = LocalDateTimeUtil.format(pageReqVO.getNewTime(), "yyyy-MM-dd HH:mm:ss");
            List<String> list = getEsStrings(index, start, end, boxIndex);
            if (CollectionUtils.isEmpty(list)) {
                return map;
            }

            Double outLetMax = 0d;
            Double outLetMin = Double.MAX_VALUE;
            String outLetMaxTime = "";
            String outLetMinTime = "";

            if (isSameDay) {
                List<BoxEleOutletDo> baseDos = list.stream().map(i -> JsonUtils.parseObject(i, BoxEleOutletDo.class)).collect(Collectors.toList());
                Map<Integer, List<BoxEleOutletDo>> groupedByOutletId = baseDos.stream()
                        .collect(Collectors.groupingBy(BoxEleOutletDo::getOutletId));
                List<BoxEleOutletDo> boxEleOutletDos1 = groupedByOutletId.get(1);
                List<BoxEleOutletDo> boxEleOutletDos2 = groupedByOutletId.get(2);
                List<BoxEleOutletDo> boxEleOutletDos3 = groupedByOutletId.get(3);

                int dataIndex1 = 0;
                int dataIndex2 = 0;
                int dataIndex3 = 0;
                if (!CollectionUtils.isEmpty(boxEleOutletDos1)) {
                    List<EleCost> eleCosts = new ArrayList<>();
                    for (int i = 0; i < boxEleOutletDos1.size() - 1; i++) {
                        EleCost eleCost = new EleCost();
                        eleCost.setOutletId(1);
                        eleCost.setEq(boxEleOutletDos1.get(dataIndex1 + 1).getEleActive() - boxEleOutletDos1.get(dataIndex1).getEleActive());
                        eleCost.setCreateTime(boxEleOutletDos1.get(dataIndex1).getCreateTime());
                        eleCosts.add(eleCost);
                        dataIndex1++;
                    }
                    map.put(1,eleCosts);
                    for (EleCost cost : eleCosts) {
                        if (outLetMax < cost.getEq()) {
                            outLetMax = cost.getEq();
                            outLetMaxTime = cost.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                        if (outLetMin > cost.getEq()) {
                            outLetMin = cost.getEq();
                            outLetMinTime = cost.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                    }
                    map.put("pName" + 1, "输出位1电量消耗极值");
                    map.put("outLetMax" + 1, outLetMax);
                    map.put("outLetMaxTime" + 1, outLetMaxTime);
                    map.put("outLetMin" + 1, outLetMin);
                    map.put("outLetMinTime" + 1, outLetMinTime);
                }

                if (!CollectionUtils.isEmpty(boxEleOutletDos2)) {
                    List<EleCost> eleCosts = new ArrayList<>();
                    for (int i = 0; i < boxEleOutletDos2.size() - 1; i++) {
                        EleCost eleCost = new EleCost();
                        eleCost.setOutletId(2);
                        eleCost.setEq(boxEleOutletDos2.get(dataIndex2 + 1).getEleActive() - boxEleOutletDos2.get(dataIndex2).getEleActive());
                        eleCosts.add(eleCost);
                        dataIndex2++;
                    }
                    map.put(2,eleCosts);
                    for (EleCost cost : eleCosts) {
                        if (outLetMax < cost.getEq()) {
                            outLetMax = cost.getEq();
                            outLetMaxTime = cost.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                        if (outLetMin > cost.getEq()) {
                            outLetMin = cost.getEq();
                            outLetMinTime = cost.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                    }
                    map.put("pName" + 2, "输出位2电量消耗极值");
                    map.put("outLetMax" + 2, outLetMax);
                    map.put("outLetMaxTime" + 2, outLetMaxTime);
                    map.put("outLetMin" + 2, outLetMin);
                    map.put("outLetMinTime" + 2, outLetMinTime);
                }

                if (!CollectionUtils.isEmpty(boxEleOutletDos3)) {
                    List<EleCost> eleCosts = new ArrayList<>();
                    for (int i = 0; i < boxEleOutletDos3.size() - 1; i++) {
                        EleCost eleCost = new EleCost();
                        eleCost.setOutletId(3);
                        eleCost.setEq(boxEleOutletDos3.get(dataIndex3 + 1).getEleActive() - boxEleOutletDos3.get(dataIndex3).getEleActive());
                        eleCosts.add(eleCost);
                        dataIndex2++;
                    }
                    map.put(3,eleCosts);
                    for (EleCost cost : eleCosts) {
                        if (outLetMax < cost.getEq()) {
                            outLetMax = cost.getEq();
                            outLetMaxTime = cost.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                        if (outLetMin > cost.getEq()) {
                            outLetMin = cost.getEq();
                            outLetMinTime = cost.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                    }
                    map.put("pName" + 3, "输出位3电量消耗极值");
                    map.put("outLetMax" + 3, outLetMax);
                    map.put("outLetMaxTime" + 3, outLetMaxTime);
                    map.put("outLetMin" + 3, outLetMin);
                    map.put("outLetMinTime" + 3, outLetMinTime);
                }

//                map = eleCosts.stream().collect(Collectors.groupingBy(EleCost::getOutletId));
                List<String> collect1 = baseDos.stream().map(i -> DateUtil.format(i.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).distinct().collect(Collectors.toList());
                map.put("time", collect1);
            } else {
                List<BoxEqOutletDayDo> baseDos = list.stream().map(i -> JsonUtils.parseObject(i, BoxEqOutletDayDo.class)).collect(Collectors.toList());
                map = baseDos.stream().collect(Collectors.groupingBy(BoxEqOutletDayDo::getOutletId));
                //处理极值
                for (int i = 1; i < 4; i++) {
                    List<BoxEqOutletDayDo> eleList = (List<BoxEqOutletDayDo>) map.get(i);
                    for (BoxEqOutletDayDo boxEqOutletDayDo : eleList) {
                        if (outLetMax < boxEqOutletDayDo.getEq()) {
                            outLetMax = boxEqOutletDayDo.getEq();
                            outLetMaxTime = boxEqOutletDayDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                        if (outLetMin > boxEqOutletDayDo.getEq()) {
                            outLetMin = boxEqOutletDayDo.getEq();
                            outLetMinTime = boxEqOutletDayDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }
                    }
                    map.put("pName" + i, "输出位" + i + "电量消耗极值");
                    map.put("outLetMax" + i, outLetMax);
                    map.put("outLetMaxTime" + i, outLetMaxTime);
                    map.put("outLetMin" + i, outLetMin);
                    map.put("outLetMinTime" + i, outLetMinTime);
                }


                List<String> collect1 = baseDos.stream().map(i -> DateUtil.format(i.getCreateTime(), "yyyy-MM-dd HH:mm:ss")).distinct().collect(Collectors.toList());
                map.put("time", collect1);

            }
        }
        return map;
    }

    private static class EleCost {
        @Setter
        @Getter
        @JsonProperty("outlet_id")
        private int outletId;
        /**
         * 电量
         */
        @Setter
        @Getter
        @JsonProperty("eq_value")
        private double eq;

        /**
         * 创建时间
         */
        @Getter
        @Setter
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        @JsonProperty("create_time")
        private DateTime createTime;


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
        List<String> keys = res.stream().map(BoxResBase::getDevKey).distinct().collect(Collectors.toList());
        Map<String, BoxNameVO> roomByKeys = getRoomByKeys(keys);
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
            boxHarmonicRes.setBusName(jsonObject.getString("bus_name"));
            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
            JSONArray curThd = lineItemList.getJSONArray("cur_thd");
            BoxNameVO boxNameVO = roomByKeys.get(devKey);
            if (Objects.nonNull(boxNameVO)) {
                boxHarmonicRes.setLocation(boxNameVO.getLocaltion());
                boxHarmonicRes.setBusName(boxNameVO.getBusName());
                boxHarmonicRes.setRoomName(boxNameVO.getRoomName());
            }
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
    public BoxHarmonicLineResVO getHarmonicLine(BoxIndexPageReqVO pageReqVO) {
        BoxHarmonicLineResVO result = new BoxHarmonicLineResVO();

        Object o = redisTemplate.opsForValue().get(REDIS_KEY_BOX + pageReqVO.getDevKey());
        if (Objects.nonNull(o)) {
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(o));
            result.setBusName(jsonObject.getString("bus_name"));
            result.setBoxName(jsonObject.getString("box_name"));
        }

        pageReqVO.setNewTime(pageReqVO.getOldTime().withHour(23).withMinute(59).withSecond(59));
        try {
            List<Integer> lines = new ArrayList<>();
            lines.add(1);
            lines.add(2);
            lines.add(3);
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<Integer> ids = Arrays.asList(pageReqVO.getBoxId());

            List<BoxLineRealtimeDo> boxHdaLine = getBoxHarmonicData(startTime, endTime, ids, "box_hda_line_realtime", BoxLineRealtimeDo.class);
            if (ObjectUtils.isNotEmpty(boxHdaLine)) {
                result.setTime(boxHdaLine.stream().map(i -> i.getCreateTime().toString("HH:mm:ss")).distinct().collect(Collectors.toList()));
                Map<Integer, List<BoxLineRealtimeDo>> collect = boxHdaLine.stream().collect(Collectors.groupingBy(BoxLineRealtimeDo::getLineId));
                List<BoxLineRealtimeDo> line1 = collect.get(1);
                List<BoxLineRealtimeDo> line2 = collect.get(2);
                List<BoxLineRealtimeDo> line3 = collect.get(3);
                if (Objects.nonNull(line1)) {
                    result.setLineOne(line1.stream().map(BoxLineRealtimeDo::getCurThd).collect(Collectors.toList()));
                }
                if (Objects.nonNull(line2)) {
                    result.setLinetwe(line2.stream().map(BoxLineRealtimeDo::getCurThd).collect(Collectors.toList()));
                }
                if (Objects.nonNull(line3)) {
                    result.setLinethree(line3.stream().map(BoxLineRealtimeDo::getCurThd).collect(Collectors.toList()));
                }
            }
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
            List<Integer> idList = new ArrayList<>();
            LambdaQueryWrapper<BoxIndex> queryWrapperX = new LambdaQueryWrapperX<BoxIndex>()
                    .eq(BoxIndex::getIsDeleted, false)
                    .eq(BoxIndex::getBoxType, 0);
            if (ObjectUtils.isNotEmpty(pageReqVO.getDevKey()) || ObjectUtils.isNotEmpty(pageReqVO.getBoxDevKeyList())) {
                queryWrapperX.and(wq -> wq.in(ObjectUtils.isNotEmpty(pageReqVO.getBoxDevKeyList()), BoxIndex::getBoxKey, pageReqVO.getBoxDevKeyList()).or()
                        .like(ObjectUtils.isNotEmpty(pageReqVO.getDevKey()), BoxIndex::getBoxKey, pageReqVO.getDevKey()));
                List<BoxIndex> searchList = boxIndexCopyMapper.selectList(queryWrapperX);
                idList = searchList.stream().map(BoxIndex::getId).collect(Collectors.toList());
            }
            String index;
            String indexOut;
            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
                index = "box_hda_line_hour";
                indexOut = "box_hda_outlet_hour";
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
                index = "box_hda_line_day";
                indexOut = "box_hda_outlet_day";
            }

            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());

            Map esTotalAndIds = getESTotalAndIds(index, startTime, endTime, pageReqVO.getPageSize(), pageReqVO.getPageNo() - 1, idList);
            if (Objects.isNull(esTotalAndIds)) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            Long total = (Long) esTotalAndIds.get("total");
            if (total == 0) {
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            List<Integer> ids = (List<Integer>) esTotalAndIds.get("ids");
            Map<Integer, Map<Integer, MaxValueAndCreateTime>> map = new HashMap<>();
            Map<Integer, Map<Integer, MaxValueAndCreateTime>> map2 = new HashMap<>();
            //1-相电流 2-相有功功率 3-输出位有功功率 4-输出位视在功率 5-输出位无功功率
            switch (pageReqVO.getBoxType()) {
                case 1:
                    map = getBoxLineCurMaxData(startTime, endTime, ids, index,
                            "max_cur", "cur_max_value", "cur_max_time", "line_id");
                    break;
                case 2:
                    map = getBoxLineCurMaxData(startTime, endTime, ids, index,
                            "max_pow", "pow_active_max_value", "pow_active_max_time", "line_id");
                    break;
                case 3:
                    map = getBoxLineCurMaxData(startTime, endTime, ids, indexOut,
                            "max_outlet_pow_active", "pow_active_max_value", "pow_active_max_time", "outlet_id");

                    map2 = getBoxLineCurMaxData(startTime, endTime, ids, indexOut,
                            "max_outlet_pow_apparent", "pow_apparent_max_value", "pow_apparent_max_time", "outlet_id");
                    break;
                default:
                    break;
            }
            List<BoxLineRes> result = new ArrayList<>();

            List<BoxIndex> boxIndices = boxIndexCopyMapper.selectList(new LambdaQueryWrapperX<BoxIndex>()
                    .inIfPresent(BoxIndex::getId, ids));

            List<String> keys = boxIndices.stream().map(BoxIndex::getBoxKey).distinct().collect(Collectors.toList());
            Map<String, BoxNameVO> roomByKeys = getRoomByKeys(keys);

            for (BoxIndex boxIndex : boxIndices) {
                Integer id = boxIndex.getId();

                BoxLineRes boxLineRes = new BoxLineRes();
                boxLineRes.setStatus(boxIndex.getRunStatus());

                boxLineRes.setBoxId(boxIndex.getId());
                boxLineRes.setDevKey(boxIndex.getBoxKey());
                boxLineRes.setBoxName(boxIndex.getBoxName());

                BoxNameVO boxNameVO = roomByKeys.get(boxIndex.getBoxKey());
                if (Objects.nonNull(boxNameVO)) {
                    boxLineRes.setLocation(boxNameVO.getLocaltion());
                    boxLineRes.setBusName(boxNameVO.getBusName());
                    boxLineRes.setRoomName(boxNameVO.getRoomName());
                }

                if (map.get(id) == null) {
                    continue;
                }

                Map<Integer, MaxValueAndCreateTime> maxValue = map.get(id);
                if (Objects.nonNull(maxValue)) {
                    MaxValueAndCreateTime value = maxValue.get(1);
                    if (Objects.nonNull(value)) {
                        boxLineRes.setL1MaxValue(value.getMaxValue().floatValue());
                        boxLineRes.setL1MaxValueTime(value.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                    }
                    MaxValueAndCreateTime value2 = maxValue.get(2);
                    if (value2 != null) {
                        boxLineRes.setL2MaxValue(value2.getMaxValue().floatValue());
                        boxLineRes.setL2MaxValueTime(value2.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                    }
                    MaxValueAndCreateTime value3 = maxValue.get(3);
                    if (value3 != null) {
                        boxLineRes.setL3MaxValue(value3.getMaxValue().floatValue());
                        boxLineRes.setL3MaxValueTime(value3.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                    }
                }
                if (Objects.equals(pageReqVO.getBoxType(), 3)) {
                    Map<Integer, MaxValueAndCreateTime> maxValueb = map2.get(id);
                    if (Objects.nonNull(maxValue)) {
                        MaxValueAndCreateTime value = maxValueb.get(1);
                        if (Objects.nonNull(value)) {
                            boxLineRes.setL1MaxValueb(value.getMaxValue().floatValue());
                            boxLineRes.setL1MaxValueTimeb(value.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                        }
                        MaxValueAndCreateTime value2 = maxValueb.get(2);
                        if (value2 != null) {
                            boxLineRes.setL2MaxValueb(value2.getMaxValue().floatValue());
                            boxLineRes.setL2MaxValueTimeb(value2.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                        }
                        MaxValueAndCreateTime value3 = maxValueb.get(3);
                        if (value3 != null) {
                            boxLineRes.setL3MaxValueb(value3.getMaxValue().floatValue());
                            boxLineRes.setL3MaxValueTimeb(value3.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                        }
                    }
                }
                result.add(boxLineRes);
            }
            return new PageResult<BoxLineRes>(result, total);
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public BusLineResBase getBoxLineCurLine(BoxIndexPageReqVO pageReqVO) {
        if (Objects.isNull(pageReqVO.getBoxId()) && StringUtils.isNotEmpty(pageReqVO.getDevKey())) {
            BoxIndex boxIndex = boxIndexMapper.selectOne(new LambdaQueryWrapper<BoxIndex>()
                    .eq(BoxIndex::getBoxKey, pageReqVO.getDevKey())
                    .eq(BoxIndex::getIsDeleted, 0).last("limit 1"));
            pageReqVO.setBoxId(boxIndex.getId());
        }
        BusLineResBase result = new BusLineResBase();
        try {
            String startTime;
            String endTime;
            String index;
            String indexOut;
            if (pageReqVO.getTimeType() == 0) {
                index = BOX_HDA_LINE_HOUR;
                startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
                endTime = localDateTimeToString(LocalDateTime.now());
                indexOut = "box_hda_outlet_hour";
            } else {
                index = BOX_HDA_LINE_DAY;
                startTime = localDateTimeToString(pageReqVO.getOldTime());
                endTime = localDateTimeToString(pageReqVO.getNewTime());
                indexOut = "box_hda_outlet_Day";
            }
            List<Integer> ids = Arrays.asList(pageReqVO.getBoxId());
            List<String> data;
            if (Objects.equals(pageReqVO.getBoxType(), 3)) {
                data = getData(startTime, endTime, ids, indexOut, new String[]{"box_id", "outlet_id", "pow_active_max_time", "pow_active_max_value",
                        "pow_apparent_max_time", "pow_apparent_max_value", "pow_reactive_max_time", "pow_reactive_max_value", "create_time"});
            } else {
                data = getData(startTime, endTime, ids, index, new String[]{"box_id", "line_id", "cur_max_value", "cur_max_time",
                        "pow_active_max_value", "pow_active_max_time", "create_time"});
            }
            if (CollectionUtils.isEmpty(data)) {
                return result;
            }
            if (pageReqVO.getBoxType() == 1) {
                List<BoxLineHourDo> lineHourDos = data.stream().map(i -> JsonUtils.parseObject(i, BoxLineHourDo.class))
                        .sorted(Comparator.comparing(BoxLineBaseDo::getCreateTime)).collect(Collectors.toList());
                result.getSeries().add(new RequirementLineSeries().setName("A路最大电流"));
                result.getSeries().add(new RequirementLineSeries().setName("B路最大电流"));
                result.getSeries().add(new RequirementLineSeries().setName("C路最大电流"));
                lineHourDos.forEach(lineDo -> {
                    if (lineDo.getLineId() == 1) {
                        result.getTime().add(lineDo.getCurMaxTime().toString("yyyy-MM-dd HH"));
                    }
                    result.getSeries().get(lineDo.getLineId() - 1).getData().add(lineDo.getCurMaxValue());
                    ((RequirementLineSeries) result.getSeries().get(lineDo.getLineId() - 1)).getMaxTime().add(lineDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                });
            }
            if (pageReqVO.getBoxType() == 2) {
                List<BoxLineHourDo> lineHourDos = data.stream().map(i -> JsonUtils.parseObject(i, BoxLineHourDo.class))
                        .sorted(Comparator.comparing(BoxLineBaseDo::getCreateTime)).collect(Collectors.toList());
                result.getSeries().add(new RequirementLineSeries().setName("A路最大功率"));
                result.getSeries().add(new RequirementLineSeries().setName("B路最大功率"));
                result.getSeries().add(new RequirementLineSeries().setName("C路最大功率"));

                lineHourDos.forEach(lineDo -> {
                    if (lineDo.getLineId() == 1) {
                        result.getTime().add(lineDo.getPowActiveMaxTime().toString("yyyy-MM-dd HH"));
                    }
                    result.getSeries().get(lineDo.getLineId() - 1).getData().add(lineDo.getPowActiveMaxValue());
                    ((RequirementLineSeries) result.getSeries().get(lineDo.getLineId() - 1)).getMaxTime().add(lineDo.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                });
            }
            if (pageReqVO.getBoxType() == 3) {
                Map<Integer, List<BoxOutletHourDo>> outMap = data.stream().map(i -> JsonUtils.parseObject(i, BoxOutletHourDo.class))
                        .sorted(Comparator.comparing(BoxOutletHourDo::getCreateTime)).collect(Collectors.groupingBy(BoxOutletBaseDo::getOutletId));

                List<String> time = data.stream().map(i -> JsonUtils.parseObject(i, BoxOutletHourDo.class)).sorted(Comparator.comparing(BoxOutletHourDo::getCreateTime))
                        .map(i -> i.getCreateTime().toString("yyyy-MM-dd HH")).distinct().collect(Collectors.toList());

                result.setTime(time);
                List<BoxOutletHourDo> boxOutleta = outMap.get(1);
                if (!CollectionUtils.isEmpty(boxOutleta)) {
                    result.getSeries().add(new RequirementLineSeries().setName("输出位1最大有功功率").setData(boxOutleta));
                }
                List<BoxOutletHourDo> boxOutletb = outMap.get(2);
                if (!CollectionUtils.isEmpty(boxOutletb)) {
                    result.getSeries().add(new RequirementLineSeries().setName("输出位2最大有功功率").setData(boxOutletb));
                }
                List<BoxOutletHourDo> boxOutletc = outMap.get(3);
                if (!CollectionUtils.isEmpty(boxOutletc)) {
                    result.getSeries().add(new RequirementLineSeries().setName("输出位3最大有功功率").setData(boxOutletc));
                }
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
                List<String> cabinetData = getDataNew(startTime, endTime, Arrays.asList(Id), index);
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay) {
                    List<BoxEleTotalDo> busList = new ArrayList<>();

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

                        busList.add(eleDO);
                    }
                    //计算实时用电量
                    List<BoxEleTotalDo> dayEqList = new ArrayList<>();
                    for (int i = 0; i < cabinetData.size() - 1; i++) {
                        BoxEleTotalDo dayEleDo = new BoxEleTotalDo();
                        totalEq += (float) busList.get(i + 1).getEleActive() - (float) busList.get(i).getEleActive();
                        dayEleDo.setEleActive(busList.get(i + 1).getEleActive() - busList.get(i).getEleActive());
                        dayEleDo.setCreateTime(busList.get(i).getCreateTime());
                        dayEqList.add(dayEleDo);
                    }
                    dayEqList.sort(Comparator.comparing(BoxEleTotalDo::getEleActive));

                    maxEle = dayEqList.get(dayEqList.size() - 1).getEleActive();
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
                        PduEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, PduEqTotalDayDo.class);
                        if (dataIndex == 0) {
                            firstEq = totalDayDo.getStartEle();
                        }
                        if (dataIndex == cabinetData.size() - 1) {
                            lastEq = totalDayDo.getEndEle();
                        }
                        totalEq += (float) totalDayDo.getEq();
                        barSeries.getData().add((float) totalDayDo.getEq());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                        dataIndex++;
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Id), index, "eq_value");
                    BoxEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, BoxEqTotalDayDo.class);
                    if (eqMaxValue != null) {
                        maxEle = eqMaxValue.getEq();
                        maxEleTime = eqMaxValue.getStartTime().toString("yyyy-MM-dd HH:mm:ss");
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("totalEle", totalEq);
                    result.put("firstEq", firstEq);
                    result.put("lastEq", lastEq);
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
    public Map getBoxPFLine(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        BusLineResBase totalLineRes = new BusLineResBase();
        result.put("pfLineRes", totalLineRes);
        try {
            BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, devKey));

            if (boxIndex != null) {
                String index;
                String outIndex;
                Integer Id = boxIndex.getId();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "box_hda_total_hour";
                    outIndex = "box_hda_outlet_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "box_hda_total_day";
                    outIndex = "box_hda_outlet_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }

                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getDataNew(startTime, endTime, Arrays.asList(Id), index);
                List<String> outData1 = getOutData(startTime, endTime, Arrays.asList(Id), outIndex, 1);
                List<BoxOutletBaseDo> collect1 = outData1.stream().map(str -> JsonUtils.parseObject(str, BoxOutletBaseDo.class)).collect(Collectors.toList());
                List<String> outData2 = getOutData(startTime, endTime, Arrays.asList(Id), outIndex, 2);
                List<BoxOutletBaseDo> collect2 = outData2.stream().map(str -> JsonUtils.parseObject(str, BoxOutletBaseDo.class)).collect(Collectors.toList());
                List<String> outData3 = getOutData(startTime, endTime, Arrays.asList(Id), outIndex, 3);
                List<BoxOutletBaseDo> collect3 = outData3.stream().map(str -> JsonUtils.parseObject(str, BoxOutletBaseDo.class)).collect(Collectors.toList());


                List<BoxTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, BoxTotalHourDo.class)).collect(Collectors.toList());


                LineSeries totalPFLine = new LineSeries();
                LineSeries PFOutOne = new LineSeries();
                LineSeries PFOutTwo = new LineSeries();
                LineSeries PFOutThree = new LineSeries();


                List<String> pfHappenTime = new ArrayList<>();
                List<String> out1HappenTime = new ArrayList<>();
                List<String> out2HappenTime = new ArrayList<>();
                List<String> out3HappenTime = new ArrayList<>();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    powList.forEach(hourdo -> {
                        if (dataType == 0) {
                            totalPFLine.getData().add(getPfValue(hourdo, DataTypeEnums.fromValue(dataType)));
                            pfHappenTime.add(formatPFTime(hourdo, DataTypeEnums.fromValue(dataType)));
                        }
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm"));

                    });
                    collect1.forEach(hourdo -> {
                        PFOutOne.getData().add(getOutLetValue(hourdo, DataTypeEnums.fromValue(dataType)));
                        out1HappenTime.add(formatOutLetTime(hourdo, DataTypeEnums.fromValue(dataType)));
                    });
                    collect2.forEach(hourdo -> {
                        PFOutTwo.getData().add(getOutLetValue(hourdo, DataTypeEnums.fromValue(dataType)));
                        out2HappenTime.add(formatOutLetTime(hourdo, DataTypeEnums.fromValue(dataType)));
                    });
                    collect3.forEach(hourdo -> {
                        PFOutThree.getData().add(getOutLetValue(hourdo, DataTypeEnums.fromValue(dataType)));
                        out3HappenTime.add(formatOutLetTime(hourdo, DataTypeEnums.fromValue(dataType)));
                    });
                } else {
                    powList.forEach(hourdo -> {
                        if (dataType == 0) {
                            totalPFLine.getData().add(getPfValue(hourdo, DataTypeEnums.fromValue(dataType)));
                            pfHappenTime.add(formatPFTime(hourdo, DataTypeEnums.fromValue(dataType)));
                        }


                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));
                    });
                    collect1.forEach(hourdo -> {
                        PFOutOne.getData().add(getOutLetValue(hourdo, DataTypeEnums.fromValue(dataType)));
                        out1HappenTime.add(formatOutLetTime(hourdo, DataTypeEnums.fromValue(dataType)));
                    });
                    collect2.forEach(hourdo -> {
                        PFOutTwo.getData().add(getOutLetValue(hourdo, DataTypeEnums.fromValue(dataType)));
                        out2HappenTime.add(formatOutLetTime(hourdo, DataTypeEnums.fromValue(dataType)));
                    });
                    collect3.forEach(hourdo -> {
                        PFOutThree.getData().add(getOutLetValue(hourdo, DataTypeEnums.fromValue(dataType)));
                        out3HappenTime.add(formatOutLetTime(hourdo, DataTypeEnums.fromValue(dataType)));
                    });
                }

                if (!CollectionUtils.isEmpty(totalPFLine.getData())) {
                    totalPFLine.setName(getPfSeriesName("功率因素", 0, dataType));
                    processPfMavMin(powList, dataType, result, 4);
                    totalPFLine.setHappenTime(pfHappenTime);
                    totalLineRes.getSeries().add(totalPFLine);
                    result.put("pName" + 4, "总功率因素");
                }
                if (!CollectionUtils.isEmpty(PFOutOne.getData())) {
                    PFOutOne.setName(getPfSeriesName("功率因素", 1, dataType));
                    processOutLetMavMin(collect1, dataType, result, 1);
                    PFOutOne.setHappenTime(out1HappenTime);
                    totalLineRes.getSeries().add(PFOutOne);
                    result.put("pName" + 1, "输出位一功率因素");
                }
                if (!CollectionUtils.isEmpty(PFOutTwo.getData())) {
                    PFOutTwo.setName(getPfSeriesName("功率因素", 2, dataType));
                    processOutLetMavMin(collect2, dataType, result, 2);
                    PFOutTwo.setHappenTime(out2HappenTime);
                    totalLineRes.getSeries().add(PFOutTwo);
                    result.put("pName" + 2, "输出位二功率因素");
                }
                if (!CollectionUtils.isEmpty(PFOutThree.getData())) {
                    PFOutThree.setName(getPfSeriesName("功率因素", 3, dataType));
                    processOutLetMavMin(collect3, dataType, result, 3);
                    PFOutThree.setHappenTime(out3HappenTime);
                    totalLineRes.getSeries().add(PFOutThree);
                    result.put("pName" + 3, "输出位三功率因素");
                }

                result.put("pfLineRes", totalLineRes);
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }


    public void processPfMavMin(List<BoxTotalHourDo> powList, Integer dataType, Map<String, Object> result, Integer index) {
//        PowerData pfData = new PowerData();
//        for (BoxTotalHourDo boxTotalHourDo : powList) {
//            updatePowerData(pfData, boxTotalHourDo.getPowerFactorMaxValue(), boxTotalHourDo.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getPowerFactorAvgValue(), boxTotalHourDo.getPowerFactorMinValue(), boxTotalHourDo.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
//        }

        //因为总功率因素没有最大值和最小值的数据所以先手动处理
        Float powFactorMax = 0f;
        String powFactorMaxTime = "无";
        Float powFactorMin = Float.MAX_VALUE;
        String powFactorMinTime = "无";
        for (BoxTotalHourDo boxTotalHourDo : powList) {
            if (powFactorMax < boxTotalHourDo.getPowerFactorAvgValue()) {
                powFactorMax = boxTotalHourDo.getPowerFactorAvgValue();
            }
            if (powFactorMin > boxTotalHourDo.getPowerFactorAvgValue()) {
                powFactorMin = boxTotalHourDo.getPowerFactorAvgValue();
            }
        }

        result.put("powFactorMax" + index, powFactorMax);
        result.put("powFactorMaxTime" + index, powFactorMaxTime);
        result.put("powFactorMin" + index, powFactorMin);
        result.put("powFactorMinTime" + index, powFactorMinTime);
    }

    public void processOutLetMavMin(List<BoxOutletBaseDo> powList, Integer dataType, Map<String, Object> result, Integer index) {
        PowerData pfData = new PowerData();
        for (BoxOutletBaseDo boxOutletBaseDo : powList) {
            updatePowerData(pfData, boxOutletBaseDo.getPowerFactorMaxValue(), boxOutletBaseDo.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss")
                    , boxOutletBaseDo.getPowerFactorAvgValue(), boxOutletBaseDo.getPowerFactorMinValue()
                    , boxOutletBaseDo.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
        }
        result.put("powFactorMax" + index, pfData.getMaxValue());
        result.put("powFactorMaxTime" + index, pfData.getMaxTime());
        result.put("powFactorMin" + index, pfData.getMinValue());
        result.put("powFactorMinTime" + index, pfData.getMinTime());
    }


    private String getPfSeriesName(String type, int Id, int dataType) {
        String lineName = "";
        switch (Id) {
            case 0:
                lineName = "总";
                break;
            case 1:
                lineName = "输出位一";
                break;
            case 2:
                lineName = "输出位二";
                break;
            case 3:
                lineName = "输出位三";
                break;
        }

        return lineName + DataNameType.fromValue(dataType).name() + type + "曲线";
    }

    /**
     * 获取总功率因素
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private Float getPfValue(BoxTotalHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getPowerFactorMaxValue();
            case AVG:
                return houResVO.getPowerFactorAvgValue();
            case MIN:
                return houResVO.getPowerFactorMinValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 获取总功率因素发生时间
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private String formatPFTime(BoxTotalHourDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return sdf.format(houResVO.getPowerFactorMaxTime());
            case AVG:
                return "无";
            case MIN:
                return sdf.format(houResVO.getPowerFactorMinTime());
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 获取输出位功率因素
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private Float getOutLetValue(BoxOutletBaseDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getPowerFactorMaxValue();
            case AVG:
                return houResVO.getPowerFactorAvgValue();
            case MIN:
                return houResVO.getPowerFactorMinValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 获取输出位功率因素发生时间
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private String formatOutLetTime(BoxOutletBaseDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return sdf.format(houResVO.getPowerFactorMaxTime());
            case AVG:
                return "无";
            case MIN:
                return sdf.format(houResVO.getPowerFactorMinTime());
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    @Override
    public Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        BusLineResBase totalLineRes = new BusLineResBase();

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
                List<String> data = getDataNew(startTime, endTime, Arrays.asList(Id), index);
                List<BoxTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, BoxTotalHourDo.class)).collect(Collectors.toList());


                LineSeries totalApparentPow = new LineSeries();
                LineSeries totalActivePow = new LineSeries();
                LineSeries totalReactivePow = new LineSeries();
                List<String> totalApparentPowHappenTime = new ArrayList<>();
                List<String> totalActivePowHappenTime = new ArrayList<>();
                List<String> totalReactivePowHappenTime = new ArrayList<>();

                if (dataType == 1) {
                    totalApparentPowHappenTime = powList.stream().map(BoxTotalHourDo -> BoxTotalHourDo.getPowApparentMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalActivePowHappenTime = powList.stream().map(BoxTotalHourDo -> BoxTotalHourDo.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalReactivePowHappenTime = powList.stream().map(BoxTotalHourDo -> BoxTotalHourDo.getPowReactiveMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                } else if (dataType == -1) {
                    totalApparentPowHappenTime = powList.stream().map(BoxTotalHourDo -> BoxTotalHourDo.getPowApparentMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalActivePowHappenTime = powList.stream().map(BoxTotalHourDo -> BoxTotalHourDo.getPowActiveMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalReactivePowHappenTime = powList.stream().map(BoxTotalHourDo -> BoxTotalHourDo.getPowReactiveMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }
                totalApparentPow.setHappenTime(totalApparentPowHappenTime);
                totalActivePow.setHappenTime(totalActivePowHappenTime);
                totalReactivePow.setHappenTime(totalReactivePowHappenTime);

                for (BoxTotalHourDo boxTotalHourDo : powList) {
                    if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                        totalLineRes.getTime().add(boxTotalHourDo.getCreateTime().toString("HH:mm"));
                    } else {
                        totalLineRes.getTime().add(boxTotalHourDo.getCreateTime().toString("yyyy-MM-dd"));
                    }
                }
                if (dataType == 1) {
                    totalApparentPow.setName("总最大视在功率");
                    totalActivePow.setName("总最大有功功率");
                    totalReactivePow.setName("总最大无功功率");
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getPowApparentMaxValue());
                        totalActivePow.getData().add(hourdo.getPowActiveMaxValue());
                        totalReactivePow.getData().add(hourdo.getPowReactiveMaxValue());
                    });
                } else if (dataType == 0) {
                    totalApparentPow.setName("总平均视在功率");
                    totalActivePow.setName("总平均有功功率");
                    totalReactivePow.setName("总平均无功功率");
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getPowApparentAvgValue());
                        totalActivePow.getData().add(hourdo.getPowActiveAvgValue());
                        totalReactivePow.getData().add(hourdo.getPowReactiveAvgValue());
                    });
                } else if (dataType == -1) {
                    totalApparentPow.setName("总最小视在功率");
                    totalActivePow.setName("总最小有功功率");
                    totalReactivePow.setName("总最小无功功率");
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getPowApparentMinValue());
                        totalActivePow.getData().add(hourdo.getPowActiveMinValue());
                        totalReactivePow.getData().add(hourdo.getPowReactiveMinValue());
                    });
                }
                processPowMavMin(powList, dataType, result);
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);
                totalLineRes.getSeries().add(totalReactivePow);
                result.put("totalLineRes", totalLineRes);

            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    public void processPowMavMin(List<BoxTotalHourDo> powList, Integer dataType, Map<String, Object> result) {
        PowerData apparentPowData = new PowerData();
        PowerData activePowData = new PowerData();
        PowerData reactivePowData = new PowerData();

        for (BoxTotalHourDo boxTotalHourDo : powList) {
            updatePowerData(apparentPowData, boxTotalHourDo.getPowApparentMaxValue(), boxTotalHourDo.getPowApparentMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getPowApparentAvgValue(), boxTotalHourDo.getPowApparentMinValue(), boxTotalHourDo.getPowApparentMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(activePowData, boxTotalHourDo.getPowActiveMaxValue(), boxTotalHourDo.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getPowActiveAvgValue(), boxTotalHourDo.getPowActiveMinValue(), boxTotalHourDo.getPowActiveMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(reactivePowData, boxTotalHourDo.getPowReactiveMaxValue(), boxTotalHourDo.getPowReactiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTotalHourDo.getPowReactiveAvgValue(), boxTotalHourDo.getPowReactiveMinValue(), boxTotalHourDo.getPowReactiveMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
        }

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
    }

    @Override
    public Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
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
                List<Float> temA = new ArrayList<>();
                List<String> temAHappenTime = new ArrayList<>();
                LineSeries seriesB = new LineSeries();
                List<Float> temB = new ArrayList<>();
                List<String> temBHappenTime = new ArrayList<>();
                LineSeries seriesC = new LineSeries();
                List<Float> temC = new ArrayList<>();
                List<String> temCHappenTime = new ArrayList<>();
                LineSeries seriesN = new LineSeries();
                List<Float> temN = new ArrayList<>();
                List<String> temNHappenTime = new ArrayList<>();

                if (dataType == 1) {
                    seriesA.setName("A相最大温度");
                    seriesB.setName("B相最大温度");
                    seriesC.setName("C相最大温度");
                    seriesN.setName("N相最大温度");
                    temA = temList.stream().map(BoxTemHourDo::getTemAMaxValue).collect(Collectors.toList());
                    temAHappenTime = temList.stream().map(BusTemHourDo -> BusTemHourDo.getTemAMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    temB = temList.stream().map(BoxTemHourDo::getTemBMaxValue).collect(Collectors.toList());
                    temBHappenTime = temList.stream().map(BusTemHourDo -> BusTemHourDo.getTemBMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    temC = temList.stream().map(BoxTemHourDo::getTemCMaxValue).collect(Collectors.toList());
                    temCHappenTime = temList.stream().map(BusTemHourDo -> BusTemHourDo.getTemCMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    temN = temList.stream().map(BoxTemHourDo::getTemNMaxValue).collect(Collectors.toList());
                    temNHappenTime = temList.stream().map(BusTemHourDo -> BusTemHourDo.getTemNMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                } else if (dataType == 0) {
                    seriesA.setName("A相平均温度");
                    seriesB.setName("B相平均温度");
                    seriesC.setName("C相平均温度");
                    seriesN.setName("N相平均温度");
                    temA = temList.stream().map(BoxTemHourDo::getTemAAvgValue).collect(Collectors.toList());
                    temB = temList.stream().map(BoxTemHourDo::getTemBAvgValue).collect(Collectors.toList());
                    temC = temList.stream().map(BoxTemHourDo::getTemCAvgValue).collect(Collectors.toList());
                    temN = temList.stream().map(BoxTemHourDo::getTemNAvgValue).collect(Collectors.toList());
                } else if (dataType == -1) {
                    seriesA.setName("A相最小温度");
                    seriesB.setName("B相最小温度");
                    seriesC.setName("C相最小温度");
                    seriesN.setName("N相最小温度");
                    temA = temList.stream().map(BoxTemHourDo::getTemAMinValue).collect(Collectors.toList());
                    temAHappenTime = temList.stream().map(BoxTemHourDo -> BoxTemHourDo.getTemAMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    temB = temList.stream().map(BoxTemHourDo::getTemBMinValue).collect(Collectors.toList());
                    temBHappenTime = temList.stream().map(BoxTemHourDo -> BoxTemHourDo.getTemBMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    temC = temList.stream().map(BoxTemHourDo::getTemCMinValue).collect(Collectors.toList());
                    temCHappenTime = temList.stream().map(BoxTemHourDo -> BoxTemHourDo.getTemCMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    temN = temList.stream().map(BoxTemHourDo::getTemNMinValue).collect(Collectors.toList());
                    temNHappenTime = temList.stream().map(BoxTemHourDo -> BoxTemHourDo.getTemNMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }
                processTemMavMin(temList, dataType, result);

                seriesA.setData(temA);
                seriesA.setHappenTime(temAHappenTime);
                seriesB.setData(temB);
                seriesB.setHappenTime(temBHappenTime);
                seriesC.setData(temC);
                seriesC.setHappenTime(temCHappenTime);
                seriesN.setData(temN);
                seriesN.setHappenTime(temNHappenTime);

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

                result.put("lineRes", lineRes);
                return result;
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    private void processTemMavMin(List<BoxTemHourDo> temList, Integer dataType, Map<String, Object> result) {
        PowerData temAData = new PowerData();
        temAData.setMaxValue(Float.MIN_VALUE);
        PowerData temBData = new PowerData();
        temBData.setMaxValue(Float.MIN_VALUE);
        PowerData temCData = new PowerData();
        temCData.setMaxValue(Float.MIN_VALUE);
        PowerData temDData = new PowerData();
        temDData.setMaxValue(Float.MIN_VALUE);
        for (BoxTemHourDo boxTemHourDo : temList) {
            updatePowerData(temAData, boxTemHourDo.getTemAMaxValue(), boxTemHourDo.getTemAMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTemHourDo.getTemAAvgValue(), boxTemHourDo.getTemAMinValue(), boxTemHourDo.getTemAMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(temBData, boxTemHourDo.getTemBMaxValue(), boxTemHourDo.getTemBMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTemHourDo.getTemBAvgValue(), boxTemHourDo.getTemBMinValue(), boxTemHourDo.getTemBMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(temCData, boxTemHourDo.getTemCMaxValue(), boxTemHourDo.getTemCMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTemHourDo.getTemCAvgValue(), boxTemHourDo.getTemCMinValue(), boxTemHourDo.getTemCMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
            updatePowerData(temDData, boxTemHourDo.getTemNMaxValue(), boxTemHourDo.getTemNMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxTemHourDo.getTemNAvgValue(), boxTemHourDo.getTemNMinValue(), boxTemHourDo.getTemNMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
        }
        result.put("temAMaxValue", temAData.getMaxValue());
        result.put("temAMaxTime", temAData.getMaxTime());
        result.put("temAMinValue", temAData.getMinValue());
        result.put("temAMinTime", temAData.getMinTime());
        result.put("temBMaxValue", temBData.getMaxValue());
        result.put("temBMaxTime", temBData.getMaxTime());
        result.put("temBMinValue", temBData.getMinValue());
        result.put("temBMinTime", temBData.getMinTime());
        result.put("temCMaxValue", temCData.getMaxValue());
        result.put("temCMaxTime", temCData.getMaxTime());
        result.put("temCMinValue", temCData.getMinValue());
        result.put("temCMinTime", temCData.getMinTime());
        result.put("temNMaxValue", temDData.getMaxValue());
        result.put("temNMaxTime", temDData.getMaxTime());
        result.put("temNMinValue", temDData.getMinValue());
        result.put("temNMinTime", temDData.getMinTime());
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
        private Float maxValue = 0f;
        private Float minValue = Float.MAX_VALUE;
        private String maxTime = "";
        private String minTime = "";

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
//        builder.sort(CREATE_TIME + KEYWORD, SortOrder.ASC);
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
     * @param heads
     */
    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index, String[] heads) throws IOException {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.fetchSource(heads, null);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                    .must(QueryBuilders.termsQuery("box_id", ids))));
//            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
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
                    if (str.length() > 2) {
                        list.add(str);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private List<String> getDataNew(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        try {
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
                    if (str.length() > 2) {
                        list.add(str);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                    .must(QueryBuilders.termsQuery("box_id", ids))));
//            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
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
                    if (str.length() > 2) {
                        list.add(str);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private List getData(String startTime, String endTime, Integer id, String index, Class cls) throws IOException {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                    .must(QueryBuilders.termQuery("box_id", id))));

            // 设置搜索条件
            searchRequest.source(builder);
            builder.size(3000);
            List list = new ArrayList<>();
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse != null) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    String str = hit.getSourceAsString();
                    if (str.length() > 2) {
                        list.add(JsonUtils.parseObject(str, cls));
                    }
                }
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private List<String> getOutData(String startTime, String endTime, List<Integer> ids, String index, Integer outletId) throws IOException {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //    builder.fetchSource(heads, null);

            // 构建布尔查询，组合多个条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            // 添加时间范围条件
            boolQuery.must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime));
            // 添加 box_id 条件
            boolQuery.must(QueryBuilders.termsQuery("box_id", ids));

            // 添加 outlet_id 条件
            if (outletId != null) {
                boolQuery.must(QueryBuilders.termQuery("outlet_id", outletId));
            }

            // 使用 constant_score 查询包装布尔查询
            builder.query(QueryBuilders.constantScoreQuery(boolQuery));
            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
//            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
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
                    if (str.length() > 2) {
                        list.add(str);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            log.error(e.getMessage());
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

    private Map<Integer, Map<Integer, MaxValueAndCreateTime>> getBoxLineCurMaxData(String startTime, String endTime, List<Integer> ids, String index,
                                                                                   String bucketKey, String fieldKey, String fieldTime, String indexKey) throws IOException {

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
                        .subAggregation(AggregationBuilders.terms("by_" + indexKey)
                                .field(indexKey)
                                .size(1000)
                                .order(BucketOrder.aggregation(bucketKey, false))
                                .subAggregation(AggregationBuilders.max(bucketKey).field(fieldKey))
                                .subAggregation(AggregationBuilders.topHits("top_docs")
                                        .size(1)
                                        .fetchSource(new String[]{fieldTime}, null)
                                        .sort(SortBuilders.fieldSort(fieldKey).order(SortOrder.DESC))))
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
            Terms byLineId = boxIdBucket.getAggregations().get("by_" + indexKey);
            for (Terms.Bucket lineIdBucket : byLineId.getBuckets()) {
                Integer lineId = lineIdBucket.getKeyAsNumber().intValue();
                MaxValueAndCreateTime maxValueAndCreateTime = new MaxValueAndCreateTime();
                // 获取max_cur聚合结果
                ParsedMax maxCur = (ParsedMax) lineIdBucket.getAggregations().get(bucketKey);
                maxValueAndCreateTime.setMaxValue(maxCur.getValue());

                // 获取top_hits聚合结果
                ParsedTopHits topHits = (ParsedTopHits) lineIdBucket.getAggregations().get("top_docs");
                if (topHits.getHits().getHits().length != 0) {
                    SearchHit topHit = topHits.getHits().getHits()[0]; // 取第一个top hit
                    Map<String, Object> sourceAsMap = topHit.getSourceAsMap();
                    maxValueAndCreateTime.setMaxTime(new DateTime(sourceAsMap.get(fieldTime).toString(), "yyyy-MM-dd HH:mm:ss"));
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
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                        .gte(startTime).lt(endTime))
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
        } catch (Exception e) {
            log.error("插接箱日环比错误" + e);
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
        } catch (Exception e) {
            log.error("插接箱用能环比" + e);
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
        } catch (Exception e) {
            log.error("插接箱用能月环比错误" + e);
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
        BoxEleTotalDo endRealtimeDo = getEleData(startTime, endTime,
                SortOrder.DESC,
                BOX_ELE_TOTAL_REALTIME, id);
        BoxEleTotalDo startRealtimeDo = getEleData(startTime, endTime,
                SortOrder.ASC,
                BOX_ELE_TOTAL_REALTIME, id);
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

    private List getBoxHarmonicData(String startTime, String endTime, List<Integer> ids, String index, Class cla) {
        List list = new ArrayList<>();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termsQuery("box_id", ids))));//.must(QueryBuilders.termsQuery("line_id", lines))
            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
            // 设置搜索条件
            searchRequest.source(builder);
            builder.size(10000);
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse != null) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {

                    Object obj = JsonUtils.parseObject(hit.getSourceAsString(), cla);
                    list.add(obj);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("获取 harmonic 数据异常：", e);
            return list;
        }
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
                if (Objects.isNull(cabinet)) {
                    continue;
                }
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
        try {
            HashMap<String, Object> result = new HashMap<>();
            SearchRequest searchRequest = new SearchRequest(index);

            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder
                    .from(pageNo * pageSize)
                    .size(pageSize)
                    .query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))))
//                            .must(QueryBuilders.termsQuery("box_id", ids))))
                    .sort("box_id", SortOrder.ASC)
                    .collapse(new CollapseBuilder("box_id"))
                    .aggregation(AggregationBuilders.cardinality("total_size").field("box_id").precisionThreshold(10000));
            if (!CollectionUtils.isEmpty(ids)) {
                builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("box_id", ids))));
            }
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
        } catch (Exception e) {
            log.error("需量监测错误" + e);
            return null;
        }
    }

    private List getMutiRedis(List<BoxIndex> list) {
        List<String> devKeys = list.stream().map(boxIndex -> "packet:box:" + boxIndex.getBoxKey()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    public Map<String, String> getPositionByKey(String key) {
        List<String> list = new ArrayList<>();
        list.add(key);
        return getPositionByKeys(list);
    }

    public Map<String, BoxNameVO> getRoomByKeys(List<String> keys) {
        Map<String, BoxNameVO> map = new HashMap<>();
        if (CollectionUtils.isEmpty(keys)) {
            return map;
        }
        Map<String, List<AisleBoxResVO>> collect = new HashMap<>();
        List<CabinetBoxResVO> cabinetBoxResVOList = cabinetBusMapper.selectCabinetByBoxKey(keys);
        List<AisleBoxResVO> aisleBoxList = aisleBoxMapper.selectAisleByBoxKey(keys);
        if (!CollectionUtils.isEmpty(aisleBoxList)) {
            collect = aisleBoxList.stream().collect(Collectors.groupingBy(AisleBoxResVO::getBoxKey));
        }
        Map<String, List<CabinetBoxResVO>> aPath = new HashMap<>();
        Map<String, List<CabinetBoxResVO>> bPath = new HashMap<>();
        if (!CollectionUtils.isEmpty(cabinetBoxResVOList)) {
            aPath = cabinetBoxResVOList.stream().collect(Collectors.groupingBy(CabinetBoxResVO::getBoxKeyA));
            bPath = cabinetBoxResVOList.stream().collect(Collectors.groupingBy(CabinetBoxResVO::getBoxKeyB));
        }
        for (String iter : keys) {
            StringJoiner joiner = new StringJoiner("-");
            Object o = redisTemplate.opsForValue().get(REDIS_KEY_BOX + iter);
            BoxNameVO vo = new BoxNameVO();

            if (Objects.nonNull(o)) {
                String busName = JSONObject.parseObject(JSONObject.toJSONString(o)).getString("bus_name");
                String boxName = JSONObject.parseObject(JSONObject.toJSONString(o)).getString("box_name");
                vo.setBusName(busName).setBoxName(boxName);
            }
            if (aPath.containsKey(iter)) {
                List<CabinetBoxResVO> cabinetBoxResVOS = aPath.get(iter);
                vo.setRoomName(cabinetBoxResVOS.get(0).getRoomName());

            } else if (bPath.containsKey(iter)) {
                List<CabinetBoxResVO> cabinetBoxResVOS = bPath.get(iter);
                vo.setRoomName(cabinetBoxResVOS.get(0).getRoomName());

            } else if (collect.containsKey(iter)) {
                List<AisleBoxResVO> aisleBoxResVOS = collect.get(iter);
                vo.setRoomName(aisleBoxResVOS.get(0).getRoomName());

            }
            if (StringUtils.isNotEmpty(vo.getRoomName())) {
                joiner.add(vo.getRoomName());
                if (StringUtils.isNotEmpty(vo.getBusName())) {
                    joiner.add(vo.getBusName());
                }
            } else {
                joiner.add(iter);
            }
            vo.setLocaltion(joiner.toString());
            map.put(iter, vo);
        }
        return map;
    }

    @Override
    public void getHarmonicLineExcel(BoxIndexPageReqVO pageReqVO) throws IOException {
        BoxHarmonicLineResVO result = getHarmonicLine(pageReqVO);
        List<String> time = result.getTime();
        List<Float> lineOne = result.getLineOne();
        List<Float> linetwe = result.getLinetwe();
        List<Float> linethree = result.getLinethree();
        List<BoxHarmonicLineDetailResVO> list = new ArrayList<>();
        for (int i = 0; i < time.size(); i++) {
            BoxHarmonicLineDetailResVO resVO = new BoxHarmonicLineDetailResVO();
            resVO.setTime(time.get(i));
            resVO.setLineOne(lineOne.get(i));
            resVO.setLinetwe(linetwe.get(i));
            resVO.setLinethree(linethree.get(i));
            list.add(resVO);
        }
        ExcelUtils.write(SpringHttpUtils.getResponse(), "插接箱谐波监测详情.xlsx", "数据", BoxHarmonicLineDetailResVO.class, list);
    }

    @Override
    public Map getAvgBoxHdaOutletForm(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {

        Map<String, Object> resultMap = new HashMap<>();
        BusLineResBase totalLineRes = new BusLineResBase();
        try {
            BoxIndex boxIndex = boxIndexCopyMapper.selectOne(new LambdaQueryWrapperX<BoxIndex>().eq(BoxIndex::getBoxKey, devKey));
            if (boxIndex != null) {
                String index;
                Integer Id = boxIndex.getId();
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "box_hda_outlet_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "box_hda_outlet_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getDataNew(startTime, endTime, Arrays.asList(Id), index);
                List<BoxOutletBaseDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, BoxOutletBaseDo.class)).collect(Collectors.toList());
                Map<Integer, List<BoxOutletBaseDo>> powMap = powList.stream().collect(Collectors.groupingBy(BoxOutletBaseDo::getOutletId));
                // 使用 Set 去重
                Set<String> uniqueTimeSet = new HashSet<>();

                for (BoxOutletBaseDo boxOutletBaseDo : powList) {
                    if (timeType == 0 || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                        String time = boxOutletBaseDo.getCreateTime().toString("HH:mm");
                        uniqueTimeSet.add(time);
                    } else {
                        String time = boxOutletBaseDo.getCreateTime().toString("yyyy-MM-dd");
                        uniqueTimeSet.add(time);
                    }
                }

                // 将 Set 转换为 List
                List<String> uniqueTimeList = new ArrayList<>(uniqueTimeSet);
                uniqueTimeList.sort(String::compareTo);
                // 添加到 totalLineRes 的时间列表
                totalLineRes.getTime().addAll(uniqueTimeList);

                for (int i = 1; i <= 3; i++) {
                    //数据处理
                    List<BoxOutletBaseDo> boxOutletBaseDos = powMap.get(i);
                    if (!CollectionUtils.isEmpty(boxOutletBaseDos)) {

                        switch (i) {
                            case 1:
                                resultMap.put("pName" + i, "输出位一有功功率极值");
                                break;
                            case 2:
                                resultMap.put("pName" + i, "输出位二有功功率极值");
                                break;
                            case 3:
                                resultMap.put("pName" + i, "输出位三有功功率极值");
                                break;
                            default:
                        }
                        processBoxHdaOutlet(totalLineRes, resultMap, boxOutletBaseDos, dataType, i);
                        processOutLetActiveMavMin(boxOutletBaseDos, dataType, resultMap, i);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        resultMap.put("totalLineRes", totalLineRes);
        return resultMap;
    }

    /**
     * 获取曲线名称
     *
     * @param type
     * @param outLetId
     * @param dataType
     * @return
     */
    private String getOutLetSeriesName(String type, int outLetId, int dataType) {
        String lineName = "";
        switch (outLetId) {
            case 1:
                lineName = "输出位一";
                break;
            case 2:
                lineName = "输出位二";
                break;
            case 3:
                lineName = "输出位三";
                break;
        }

        return lineName + DataNameType.fromValue(dataType).name() + type + "曲线";
    }

    /**
     * 处理输出位有功功率数据
     *
     * @param totalLineRes
     * @param resultMap
     * @param boxOutletBaseDos
     * @param dataType
     */
    private void processBoxHdaOutlet(BusLineResBase totalLineRes, Map<String, Object> resultMap, List<BoxOutletBaseDo> boxOutletBaseDos, Integer dataType, Integer index) {
        LineSeries activePow = new LineSeries();
        List<String> happenTime = new ArrayList<>();
        activePow.setName(getOutLetSeriesName("有功功率", index, dataType));
        for (BoxOutletBaseDo boxOutletBaseDo : boxOutletBaseDos) {
            activePow.getData().add(getOutLetActiveValue(boxOutletBaseDo, DataTypeEnums.fromValue(dataType)));
            happenTime.add(formatOutLetActiveTime(boxOutletBaseDo, DataTypeEnums.fromValue(dataType)));
        }
        activePow.setHappenTime(happenTime);
        totalLineRes.getSeries().add(activePow);
    }

    /**
     * 获取总功率因素
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private Float getOutLetActiveValue(BoxOutletBaseDo houResVO, DataTypeEnums dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getPowActiveMaxValue();
            case AVG:
                return houResVO.getPowActiveAvgValue();
            case MIN:
                return houResVO.getPowActiveMinValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 获取总功率因素发生时间
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private String formatOutLetActiveTime(BoxOutletBaseDo houResVO, DataTypeEnums dataType) {
        System.out.println("===BoxOutletBaseDo===" + houResVO);
        switch (dataType) {
            case MAX:
                return sdf.format(houResVO.getPowActiveMaxTime());
            case AVG:
                return "无";
            case MIN:
                return sdf.format(houResVO.getPowActiveMinTime());
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }


    public void processOutLetActiveMavMin(List<BoxOutletBaseDo> powList, Integer dataType, Map<String, Object> result, Integer index) {
        PowerData activePowData = new PowerData();

        for (BoxOutletBaseDo boxOutletBaseDo : powList) {
            updatePowerData(activePowData, boxOutletBaseDo.getPowActiveMaxValue(), boxOutletBaseDo.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"), boxOutletBaseDo.getPowActiveAvgValue(), boxOutletBaseDo.getPowActiveMinValue(), boxOutletBaseDo.getPowActiveMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);
        }
        result.put("activePowMaxValue" + index, activePowData.getMaxValue());
        result.put("activePowMaxTime" + index, activePowData.getMaxTime());
        result.put("activePowMinValue" + index, activePowData.getMinValue());
        result.put("activePowMinTime" + index, activePowData.getMinTime());
    }


    public Map<String, String> getPositionByKeys(List<String> keys) {
        Map<String, String> map = new HashMap<>();
        if (CollectionUtils.isEmpty(keys)) {
            return map;
        }
        ValueOperations ops = redisTemplate.opsForValue();
        List<BoxIndex> boxIndices = boxIndexMapper.selectList(new LambdaUpdateWrapper<BoxIndex>().in(BoxIndex::getBoxKey, keys));
        Map<String, String> collect = boxIndices.stream().collect(Collectors.toMap(BoxIndex::getBoxKey, BoxIndex::getBoxName));

        //柜列
        List<AisleBox> aisleBoxList = aisleBoxMapper.selectList(new LambdaQueryWrapper<AisleBox>()
                .in(AisleBox::getBoxKey, keys));
        if (!CollectionUtils.isEmpty(aisleBoxList)) {
            List<Integer> aisleBarIds = aisleBoxList.stream().map(AisleBox::getAisleBarId).collect(Collectors.toList());
            List<AisleBar> aisleBars = aisleBarMapper.selectBatchIds(aisleBarIds);
            Map<Integer, String> pathMap = aisleBars.stream().collect(Collectors.toMap(AisleBar::getId, AisleBar::getPath));
            Map<Integer, AisleBox> aisleBoxKeyMap = aisleBoxList.stream().collect(Collectors.toMap(AisleBox::getAisleId, Function.identity()));

//            Map<Integer, String> positionMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(aisleBoxList)) {
                List<String> redisKeys = aisleBoxList.stream().map(aisle -> REDIS_KEY_AISLE + aisle.getAisleId()).collect(Collectors.toList());
                List aisles = ops.multiGet(redisKeys);
                if (!CollectionUtils.isEmpty(aisleBoxList)) {
                    for (Object aisle : aisles) {
                        JSONObject json = JSON.parseObject(JSON.toJSONString(aisle));
                        AisleBox aisleKey = aisleBoxKeyMap.get(json.getInteger("aisle_key"));
                        if (Objects.nonNull(aisleKey)) {
                            String boxKey = aisleKey.getBoxKey();
                            map.put(boxKey, json.getString("room_name") + SPLIT_KEY
                                    + json.getString("aisle_name") + SPLIT_KEY + pathMap.get(aisleKey.getAisleBarId()) + "路");
                        }
                    }
                }
            }
        }
        keys.removeAll(map.keySet());
        if (CollectionUtils.isEmpty(keys)) {
            return map;
        }
        List<CabinetBoxResVO> cabs = cabinetBusMapper.selectCabinetByBoxKey(keys);
        //设备位置
        String devPosition;

        if (!CollectionUtils.isEmpty(cabs)) {
            Map<String, List<CabinetBoxResVO>> collect1 = cabs.stream().collect(Collectors.groupingBy(CabinetBoxResVO::getBoxKeyA));
            Map<String, List<CabinetBoxResVO>> collect2 = cabs.stream().collect(Collectors.groupingBy(CabinetBoxResVO::getBoxKeyB));
            for (String key : keys) {
                List<CabinetBoxResVO> resVOS = collect1.get(key);
                if (!CollectionUtils.isEmpty(resVOS)) {
                    devPosition = resVOS.get(0).getRoomName();//+ SPLIT_KEY +resVOS.get(0).getCabinetName()
                    if (Objects.isNull(collect.get(key))) {
                        map.put(key, devPosition + SPLIT_KEY + "A路");
                    } else {
                        map.put(key, devPosition + SPLIT_KEY + "A路" + SPLIT_KEY + collect.get(key));
                    }
                } else {
                    resVOS = collect2.get(key);
                    if (!CollectionUtils.isEmpty(resVOS)) {
                        devPosition = resVOS.get(0).getRoomName();
                        if (Objects.isNull(collect.get(key))) {
                            map.put(key, devPosition + SPLIT_KEY + "B路");
                        } else {
                            map.put(key, devPosition + SPLIT_KEY + "B路" + SPLIT_KEY + collect.get(key));
                        }
                    }
                }
            }
        }
        return map;
    }
}