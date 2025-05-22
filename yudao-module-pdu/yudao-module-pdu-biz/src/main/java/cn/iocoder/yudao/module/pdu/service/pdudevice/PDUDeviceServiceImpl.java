package cn.iocoder.yudao.module.pdu.service.pdudevice;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.loop.PduHdaLoopBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetPduMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.common.vo.CabinetPduResVO;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.BarSeries;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.CabinetChartResBase;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.LineSeries;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.PDUCurbalanceColorDO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.curbalancecolor.PDUCurbalanceColorMapper;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndex;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndexMapper;
import cn.iocoder.yudao.module.pdu.enums.DataNameType;
import cn.iocoder.yudao.module.pdu.enums.DataType;
import cn.iocoder.yudao.module.pdu.enums.PduDataTypeEnum;
import cn.iocoder.yudao.module.pdu.utils.PduAnalysisResult;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.aggregations.pipeline.ParsedSimpleValue;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
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
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_PDU;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;


/**
 * PDU设备 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class PDUDeviceServiceImpl implements PDUDeviceService {


    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Resource
    private PduIndexMapper pDUDeviceMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private CabinetPduMapper cabinetPduMapper;

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;

    @Autowired
    private AisleIndexMapper aisleIndexMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Resource
    private PDUCurbalanceColorMapper PDUCurbalanceColorMapper;

    @Override
    public PageResult<PDUDeviceDO> getPDUDevicePage(PDUDevicePageReqVO pageReqVO) {
        Page<PduIndex> pduIndexPageResult;
        List<PDUDeviceDO> result = new ArrayList<>();
        if (pageReqVO.getCabinetIds() != null && !pageReqVO.getCabinetIds().isEmpty()) {
            List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>()
                    .inIfPresent(CabinetPdu::getCabinetId, pageReqVO.getCabinetIds()));
            if (cabinetPduList != null && cabinetPduList.size() > 0) {
                List<String> devKeyList = new ArrayList<>();
                List<String> pduKeya = cabinetPduList.stream().map(CabinetPdu::getPduKeyA).collect(Collectors.toList());
                List<String> pduKeyb = cabinetPduList.stream().map(CabinetPdu::getPduKeyB).collect(Collectors.toList());
                devKeyList.addAll(pduKeya);
                devKeyList.addAll(pduKeyb);
                pageReqVO.setPduKeyList(devKeyList);
            } else {
                return new PageResult<PDUDeviceDO>(result, 0L);
            }
        }
        if (pageReqVO.getCurbance() == null) {
            pduIndexPageResult = pDUDeviceMapper.selectQuery(new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
        } else {
            pduIndexPageResult = pDUDeviceMapper.selectCurbanceQuery(new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
        }
        List<PduIndex> pduIndices = pduIndexPageResult.getRecords();
        List redisList = getMutiRedis(pduIndices);

        for (PduIndex pduIndex : pduIndices) {
            PDUDeviceDO pduDeviceDO = new PDUDeviceDO();
            pduDeviceDO.setStatus(pduIndex.getRunStatus());
            pduDeviceDO.setId(pduIndex.getId());
            pduDeviceDO.setDevKey(pduIndex.getPduKey());
            pduDeviceDO.setDeleted(pduIndex.getIsDeleted().equals(1));
            pduDeviceDO.setColor(pduIndex.getCurUnbalanceStatus());
            result.add(pduDeviceDO);
        }
        Map<String, PDUDeviceDO> resMap = result.stream().collect(Collectors.toMap(PDUDeviceDO::getDevKey, Function.identity()));
        setLocation(pduIndices, result);
        for (Object o : redisList) {
            if (Objects.isNull(o)) {
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));

            String devKey = jsonObject.getString("dev_ip") + "-" + jsonObject.getString("addr");
            PDUDeviceDO pduDeviceDO = resMap.get(devKey);

            if (jsonObject == null || jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list") == null || jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").size() <= 0) {
                continue;
            }
            JSONObject pduTgData = jsonObject.getJSONObject("pdu_data").getJSONObject("pdu_total_data");
            List<Double> curArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("cur_value").toList(Double.class);
            List<Double> volArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("vol_value").toList(Double.class);
            Double curUnbalance = pduTgData.getDoubleValue("cur_unbalance");
            Double volUnbalance = pduTgData.getDoubleValue("vol_unbalance");
            //开始写进result
            pduDeviceDO.setPf(pduTgData.getDoubleValue("power_factor"));
            pduDeviceDO.setEle(pduTgData.getDoubleValue("ele_active"));
            pduDeviceDO.setPow(pduTgData.getDoubleValue("pow_active"));
            pduDeviceDO.setApparentPow(pduTgData.getDoubleValue("pow_apparent"));
            pduDeviceDO.setReactivePow(pduTgData.getDoubleValue("pow_reactive"));
            pduDeviceDO.setDataUpdateTime(jsonObject.getString("sys_time"));
            pduDeviceDO.setPduAlarm(jsonObject.getString("pdu_alarm"));

            if (curArr.size() > 1) {
                pduDeviceDO.setAcur(new BigDecimal(curArr.get(0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                pduDeviceDO.setBcur(new BigDecimal(curArr.get(1)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                pduDeviceDO.setCcur(new BigDecimal(curArr.get(2)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            } else {
                pduDeviceDO.setAcur(new BigDecimal(curArr.get(0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            pduDeviceDO.setAvol(new BigDecimal(volArr.get(0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

            pduDeviceDO.setBvol(new BigDecimal(volArr.size() > 1 ? volArr.get(1) : 0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            pduDeviceDO.setCvol(new BigDecimal(volArr.size() > 2 ? volArr.get(2) : 0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            pduDeviceDO.setCurUnbalance(new BigDecimal(curUnbalance).setScale(2, RoundingMode.HALF_UP).doubleValue());
            pduDeviceDO.setVolUnbalance(new BigDecimal(volUnbalance).setScale(2, RoundingMode.HALF_UP).doubleValue());
//            pduDeviceDO.setColor(color);
        }
        return new PageResult<PDUDeviceDO>(result, pduIndexPageResult.getTotal());
    }

    @Override
    public PageResult<PDUDeviceDO> getDeletedPDUDevicePage(PDUDevicePageReqVO pageReqVO) {
        PageResult<PduIndex> pduIndexPageResult = null;
        List<PDUDeviceDO> result = new ArrayList<>();

        pduIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PduIndex>()
                .inIfPresent(PduIndex::getPduKey, pageReqVO.getPduKeyList())
                .likeIfPresent(PduIndex::getPduKey, pageReqVO.getDevKey())
                .inIfPresent(PduIndex::getRunStatus, pageReqVO.getStatus())
                .eq(PduIndex::getIsDeleted, DelEnums.DELETE.getStatus())
                .orderByDesc(PduIndex::getUpdateTime));

        List<PduIndex> pduIndices = pduIndexPageResult.getList();

        for (PduIndex pduIndex : pduIndices) {
            PDUDeviceDO pduDeviceDO = new PDUDeviceDO();
            pduDeviceDO.setStatus(pduIndex.getRunStatus());
            pduDeviceDO.setId(pduIndex.getId());
            pduDeviceDO.setDevKey(pduIndex.getPduKey());
            pduDeviceDO.setDeleted(pduIndex.getIsDeleted().equals(1));
            pduDeviceDO.setDataUpdateTime(localDateTimeToString(pduIndex.getUpdateTime()));
            result.add(pduDeviceDO);
        }
        setLocation(pduIndices, result);
        return new PageResult<PDUDeviceDO>(result, pduIndexPageResult.getTotal());
    }

    @Override
    public PageResult<PDULineRes> getPDULineDevicePage(PDUDevicePageReqVO pageReqVO) {
        try {
            List<PDULineRes> result = new ArrayList<>();
            if (pageReqVO.getCabinetIds() != null && !pageReqVO.getCabinetIds().isEmpty()) {
                List<String> devKeyList = new ArrayList<>();
                List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>()
                        .inIfPresent(CabinetPdu::getCabinetId, pageReqVO.getCabinetIds()));
                if (cabinetPduList != null && !cabinetPduList.isEmpty()) {
                    for (CabinetPdu cabinetPdu : cabinetPduList) {
                        if (!StringUtils.isEmpty(cabinetPdu.getPduKeyA())) {
                            devKeyList.add(cabinetPdu.getPduKeyA());
                        }
                        if (!StringUtils.isEmpty(cabinetPdu.getPduKeyB())) {
                            devKeyList.add(cabinetPdu.getPduKeyB());
                        }
                    }
                } else {
                    return new PageResult<>(result, 0L);
                }
            }
            Page<PduIndex> page = pDUDeviceMapper.selectQuery(new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
            List<PduIndex> records = page.getRecords();
            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            }
            Map<Integer, Map<Integer, MaxValueAndCreateTime>> curMap;
            Map<Integer, Map<Integer, MaxValueAndCreateTime>> powMap;
            String index = null;
            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "pdu_hda_line_hour";
            } else {
                index = "pdu_hda_line_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());

            List<Integer> pduIds = records.stream().map(PduIndex::getId).collect(Collectors.toList());
            curMap = getPDULineCurMaxData(startTime, endTime, pduIds, index);
            powMap = getPDULinePowMaxData(startTime, endTime, pduIds, index);
            Map<Integer, PduIndex> map = records.stream().collect(Collectors.toMap(PduIndex::getId, x -> x));

            for (Integer pduId : map.keySet()) {
                PDULineRes pduLineRes = new PDULineRes();
                PduIndex pduIndex = map.get(pduId);
                if (Objects.nonNull(pduIndex)) {
                    Integer id = pduIndex.getId().intValue();

                    pduLineRes.setStatus(pduIndex.getRunStatus());
                    pduLineRes.setPduId(pduIndex.getId());
                    pduLineRes.setDevKey(pduIndex.getPduKey());
                } else {
                    pduLineRes.setPduId(pduId);
                }
                if (Objects.nonNull(curMap.get(pduId))) {
                    MaxValueAndCreateTime curl1 = curMap.get(pduId).get(1);
                    pduLineRes.setL1MaxCur(curl1.getMaxValue().floatValue());
                    pduLineRes.setL1MaxCurTime(curl1.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                    MaxValueAndCreateTime curl2 = curMap.get(pduId).get(2);
                    if (curl2 != null) {
                        pduLineRes.setL2MaxCur(curl2.getMaxValue().floatValue());
                        pduLineRes.setL2MaxCurTime(curl2.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                    }
                    MaxValueAndCreateTime curl3 = curMap.get(pduId).get(3);
                    if (curl3 != null) {
                        pduLineRes.setL3MaxCur(curl3.getMaxValue().floatValue());
                        pduLineRes.setL3MaxCurTime(curl3.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                    }
                }
                if (Objects.nonNull(powMap.get(pduId))) {
                    MaxValueAndCreateTime powl1 = powMap.get(pduId).get(1);
                    pduLineRes.setL1MaxPow(powl1.getMaxValue().floatValue());
                    pduLineRes.setL1MaxPowTime(powl1.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                    MaxValueAndCreateTime powl2 = powMap.get(pduId).get(2);
                    if (powl2 != null) {
                        pduLineRes.setL2MaxPow(powl2.getMaxValue().floatValue());
                        pduLineRes.setL2MaxPowTime(powl2.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                    }
                    MaxValueAndCreateTime powl3 = powMap.get(pduId).get(3);
                    if (powl3 != null) {
                        pduLineRes.setL3MaxPow(powl3.getMaxValue().floatValue());
                        pduLineRes.setL3MaxPowTime(powl3.getMaxTime().toString("yyyy-MM-dd HH:mm"));
                    }
                }
                result.add(pduLineRes);
            }

            setLocation(records, result);
            return new PageResult<PDULineRes>(result, page.getTotal());

        } catch (Exception e) {
            log.error("获取数据失败", e);
        }


        return new PageResult<>(new ArrayList<>(), 0L);
    }


    @Override
    public PageResult<PDULineRes> getPDUMaxCurData(PDUDevicePageReqVO pageReqVO) {
        try {
            List<PDULineRes> resultList = new ArrayList<>();
            PDULineRes result = new PDULineRes();

            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            }
            String index = null;
            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "pdu_hda_line_hour";
            } else {
                index = "pdu_hda_line_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            MaxCurAndOtherData maxCurAndOtherData = getMaxCurMaxValue(startTime, endTime, index, pageReqVO.getFlagVlaue());

            result.setPduId(maxCurAndOtherData.getPdu_id());
            result.setL1MaxCur(maxCurAndOtherData.getMaxValue().floatValue());
            result.setL1MaxCurTime(maxCurAndOtherData.getMaxTime().toString("yyyy-MM-dd HH:mm"));
            PduIndex pdu = pDUDeviceMapper.selectById(maxCurAndOtherData.getPdu_id());
            if (Objects.nonNull(pdu))
                result.setDevKey(pdu.getPduKey());
            resultList.add(result);
            List<Integer> pduIds = new ArrayList<>();
            pduIds.add(maxCurAndOtherData.getPdu_id());
            List<PduIndex> pdus = pDUDeviceMapper.selectBatchIds(pduIds);
            setLocation(pdus, resultList);
            return new PageResult<>(resultList, 1L);
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public String getLocationByDevKey(String devKey) {
        List<PDUDeviceDO> result = new ArrayList<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(PduIndex::getPduKey, devKey);
        if (pduIndex == null) {
            return null;
        }
        List<PduIndex> pduIndices = new ArrayList<>();
        pduIndices.add(pduIndex);
        PDUDeviceDO pduDeviceDO = new PDUDeviceDO();
        pduDeviceDO.setStatus(pduIndex.getRunStatus());
        pduDeviceDO.setId(pduIndex.getId());
        pduDeviceDO.setDevKey(pduIndex.getPduKey());
        pduDeviceDO.setDeleted(pduIndex.getIsDeleted().equals(1));
        result.add(pduDeviceDO);
        setLocation(pduIndices, result);
        return result.get(0).getLocation();
    }


    @Override
    public Integer getPDUMaxLineId(PDUDevicePageReqVO pageReqVO) {
        try {
            String index = null;
            if (pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "pdu_hda_line_hour";
            } else {
                index = "pdu_hda_line_day";
            }
            // 创建搜索请求对象
            SearchRequest searchRequest = new SearchRequest(index);

            // 创建搜索源构建器并定义聚合
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.aggregation(AggregationBuilders.max("max_line_id").field("line_id"));

            // 设置查询源
            searchRequest.source(sourceBuilder);

            // 执行搜索请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 获取最大值
            Max maxId = searchResponse.getAggregations().get("max_line_id");
            return (int) maxId.getValue();
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return 0;
    }

    @Override
    public List<String> getDevKeyList() {
        List<String> result = pDUDeviceMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(PduIndex::getPduKey).collect(Collectors.toList());
        return result;
    }

    @Override
    public List<String> getIpList() {
        List<String> result = pDUDeviceMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(PduIndex::getIpAddr).collect(Collectors.toList());
        return result;
    }


    @Override
    public String getDisplayDataByDevKey(String devKey) {

        if (StringUtils.isEmpty(devKey)) {
            return null;
        } else {
            ValueOperations ops = redisTemplate.opsForValue();
            JSONObject jsonObject = (JSONObject) ops.get("packet:pdu:" + devKey);
            return jsonObject != null ? jsonObject.toJSONString() : null;
        }
    }

    @Override
    public Map getHistoryDataByDevKey(String devKey, String type) {
        Object obj = redisTemplate.opsForValue().get("packet:pdu:" + devKey);
        Integer size = 1;
        if (Objects.nonNull(obj)) {
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(obj));
            JSONObject lineItemList = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list");
            List<Double> curList = lineItemList.getJSONArray("cur_value").toList(Double.class);
            size = curList.size();
        }
        HashMap result = new HashMap<>();
        result.put("size", size);
        List<String> dateTimes = new ArrayList<>();
        List<Double> apparentList = new ArrayList<>();//视在功率
        List<Double> activeList = new ArrayList<>();//有功功率
        List<Double> factorList = new ArrayList<>();
        List<Double> reactiveList = new ArrayList<>();

        List<Double> apparentListMax = new ArrayList<>();
        List<Double> activeListMax = new ArrayList<>();
        List<Double> factorListMax = new ArrayList<>();
        List<Double> reactiveListMax = new ArrayList<>();

        List<Double> apparentListMin = new ArrayList<>();
        List<Double> activeListMin = new ArrayList<>();
        List<Double> factorListMin = new ArrayList<>();
        List<Double> reactiveListMin = new ArrayList<>();

        List<String> apparentListMaxTime = new ArrayList<>();
        List<String> activeListMaxTime = new ArrayList<>();
        List<String> factorListMaxTime = new ArrayList<>();
        List<String> reactiveListMaxTime = new ArrayList<>();

        List<String> apparentListMinTime = new ArrayList<>();
        List<String> activeListMinTime = new ArrayList<>();
        List<String> factorListMinTime = new ArrayList<>();
        List<String> reactiveListMinTime = new ArrayList<>();

        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
        if (pduIndex == null) {
            return result;
        }
        Integer id = pduIndex.getId();
        // 构建查询请求
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String index = null;
        String indexLine = null;
        String startTime = null;
        String endTime = formatter.format(now);
        String[] heads = new String[]{};
        switch (type) {
            case "oneHour":
                index = "pdu_hda_total_realtime";
                startTime = formatter.format(now.minusHours(1));
                indexLine = "pdu_hda_line_realtime";
                heads = new String[]{"pdu_id", "line_id", "power_factor", "create_time"};
                break;
            case "twentyfourHour":
                index = "pdu_hda_total_hour";
                startTime = formatter.format(now.minusHours(25));
                indexLine = "pdu_hda_line_hour";
                heads = new String[]{"pdu_id", "line_id", "power_factor_avg_value", "power_factor_max_value", "power_factor_max_time",
                        "power_factor_min_value", "power_factor_min_time", "create_time"};
                break;
            case "seventytwoHour":
                index = "pdu_hda_total_hour";
                startTime = formatter.format(now.minusHours(73));
                indexLine = "pdu_hda_line_hour";
                heads = new String[]{"pdu_id", "line_id", "power_factor_avg_value", "power_factor_max_value", "power_factor_max_time",
                        "power_factor_min_value", "power_factor_min_time", "create_time"};
                break;
        }
        SearchResponse searchResponse = getData(startTime, endTime, id, index);

        if (searchResponse == null) {
            return result;
        }
        SearchHits hits = searchResponse.getHits();
        Long totalHits = hits.getTotalHits().value;
        if (totalHits == 0) {
            return result;
        }
        for (SearchHit hit : hits) {
            String str = hit.getSourceAsString();
            switch (type) {
                case "oneHour":
                    PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                    apparentList.add(Double.valueOf(pduHdaTotalRealtimeDo.getApparentPow()));
                    activeList.add(Double.valueOf(pduHdaTotalRealtimeDo.getActivePow()));
                    factorList.add(Double.valueOf(pduHdaTotalRealtimeDo.getPowerFactor()));
                    reactiveList.add(Double.valueOf(pduHdaTotalRealtimeDo.getPowReactive()));
                    dateTimes.add(pduHdaTotalRealtimeDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
                    break;
                case "twentyfourHour":
                case "seventytwoHour":
                    PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaTotalHourDo.class);
                    apparentList.add(Double.valueOf(pduHdaTotalHourDo.getApparentPowAvgValue()));
                    activeList.add(Double.valueOf(pduHdaTotalHourDo.getActivePowAvgValue()));
                    reactiveList.add(Double.valueOf(pduHdaTotalHourDo.getPowReactiveAvgValue()));
                    factorList.add(Double.valueOf(pduHdaTotalHourDo.getPowerFactorAvgValue()));

                    apparentListMax.add(Double.valueOf(pduHdaTotalHourDo.getApparentPowMaxValue()));
                    activeListMax.add(Double.valueOf(pduHdaTotalHourDo.getActivePowMaxValue()));
                    reactiveListMax.add(Double.valueOf(pduHdaTotalHourDo.getPowReactiveMaxValue()));
                    factorListMax.add(Double.valueOf(pduHdaTotalHourDo.getPowerFactorMaxValue()));

                    apparentListMaxTime.add(pduHdaTotalHourDo.getApparentPowMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                    activeListMaxTime.add(pduHdaTotalHourDo.getActivePowMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                    reactiveListMaxTime.add(pduHdaTotalHourDo.getPowReactiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                    factorListMaxTime.add(pduHdaTotalHourDo.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss"));

                    apparentListMin.add(Double.valueOf(pduHdaTotalHourDo.getApparentPowMinValue()));
                    activeListMin.add(Double.valueOf(pduHdaTotalHourDo.getActivePowMinValue()));
                    reactiveListMin.add(Double.valueOf(pduHdaTotalHourDo.getPowReactiveMinValue()));
                    factorListMin.add(Double.valueOf(pduHdaTotalHourDo.getPowerFactorMinValue()));
                    apparentListMinTime.add(pduHdaTotalHourDo.getApparentPowMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                    activeListMinTime.add(pduHdaTotalHourDo.getActivePowMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                    reactiveListMinTime.add(pduHdaTotalHourDo.getPowReactiveMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                    factorListMinTime.add(pduHdaTotalHourDo.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                    dateTimes.add(pduHdaTotalHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
                    break;
                default:
            }
        }
        result.put("reactiveList", reactiveList);
        result.put("apparentList", apparentList);
        result.put("activeList", activeList);
        result.put("factorList", factorList);
        result.put("reactiveListMax", reactiveListMax);
        result.put("apparentListMax", apparentListMax);
        result.put("activeListMax", activeListMax);
        result.put("factorListMax", factorListMax);
        result.put("reactiveListMin", reactiveListMin);
        result.put("apparentListMin", apparentListMin);
        result.put("activeListMin", activeListMin);
        result.put("factorListMin", factorListMin);
        result.put("reactiveListMaxTime", reactiveListMaxTime);
        result.put("apparentListMaxTime", apparentListMaxTime);
        result.put("activeListMaxTime", activeListMaxTime);
        result.put("factorListMaxTime", factorListMaxTime);
        result.put("reactiveListMinTime", reactiveListMinTime);
        result.put("apparentListMinTime", apparentListMinTime);
        result.put("activeListMinTime", activeListMinTime);
        result.put("factorListMinTime", factorListMinTime);
        result.put("dateTimes", dateTimes);
        if (size > 1) {
            List<Double> factorLista = new ArrayList<>();
            List<Double> factorListb = new ArrayList<>();
            List<Double> factorListc = new ArrayList<>();
            List<Double> factorListMaxa = new ArrayList<>();
            List<Double> factorListMaxb = new ArrayList<>();
            List<Double> factorListMaxc = new ArrayList<>();
            List<String> factorListMaxTimea = new ArrayList<>();
            List<String> factorListMaxTimeb = new ArrayList<>();
            List<String> factorListMaxTimec = new ArrayList<>();

            List<Double> factorListMina = new ArrayList<>();
            List<Double> factorListMinb = new ArrayList<>();
            List<Double> factorListMinc = new ArrayList<>();
            List<String> factorListMinTimea = new ArrayList<>();
            List<String> factorListMinTimeb = new ArrayList<>();
            List<String> factorListMinTimec = new ArrayList<>();

            List<String> list = getData(startTime, endTime, id, heads, indexLine);
            Map timeListMap = new HashMap<>();
            if (Objects.equals("oneHour", type)) {
                timeListMap = list.stream().map(str -> JsonUtils.parseObject(str, PduHdaLineRealtimeDo.class))
                        .collect(Collectors.groupingBy(i -> i.getCreateTime().toString("yyyy-MM-dd HH:mm:ss")));
                for (String time : dateTimes) {
                    List<PduHdaLineRealtimeDo> dos = (List<PduHdaLineRealtimeDo>) timeListMap.get(time);
                    dos.forEach(iter -> {
                        switch (iter.getLineId()) {
                            case 1:
                                factorLista.add(Double.valueOf(iter.getPowerFactor()));
                                break;
                            case 2:
                                factorListb.add(Double.valueOf(iter.getPowerFactor()));
                                break;
                            case 3:
                                factorListc.add(Double.valueOf(iter.getPowerFactor()));
                                break;
                            default:
                                break;
                        }
                    });
                }
            } else {
                timeListMap = list.stream().map(str -> JsonUtils.parseObject(str, PduHdaLineHourDo.class))
                        .collect(Collectors.groupingBy(i -> i.getCreateTime().toString("yyyy-MM-dd HH:mm:ss")));
                for (String time : dateTimes) {
                    List<PduHdaLineHourDo> dos = (List<PduHdaLineHourDo>) timeListMap.get(time);
                    dos.forEach(iter -> {
                        switch (iter.getLineId()) {
                            case 1:
                                factorLista.add(Double.valueOf(iter.getPowerFactorAvgValue()));
                                factorListMaxa.add(Double.valueOf(iter.getPowerFactorMaxValue()));
                                factorListMaxTimea.add(iter.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                                factorListMina.add(Double.valueOf(iter.getPowerFactorMinValue()));
                                factorListMinTimea.add(iter.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                                break;
                            case 2:
                                factorListb.add(Double.valueOf(iter.getPowerFactorAvgValue()));
                                factorListMaxb.add(Double.valueOf(iter.getPowerFactorMaxValue()));
                                factorListMaxTimeb.add(iter.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                                factorListMinb.add(Double.valueOf(iter.getPowerFactorMinValue()));
                                factorListMinTimeb.add(iter.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                                break;
                            case 3:
                                factorListc.add(Double.valueOf(iter.getPowerFactorAvgValue()));
                                factorListMaxc.add(Double.valueOf(iter.getPowerFactorMaxValue()));
                                factorListMaxTimec.add(iter.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                                factorListMinc.add(Double.valueOf(iter.getPowerFactorMinValue()));
                                factorListMinTimec.add(iter.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                                break;
                            default:
                                break;
                        }
                    });
                }
            }
            result.put("factorLista", factorLista);
            result.put("factorListb", factorListb);
            result.put("factorListc", factorListc);
            result.put("factorListMaxa", factorListMaxa);
            result.put("factorListMaxb", factorListMaxb);
            result.put("factorListMaxc", factorListMaxc);
            result.put("factorListMaxTimea", factorListMaxTimea);
            result.put("factorListMaxTimeb", factorListMaxTimeb);
            result.put("factorListMaxTimec", factorListMaxTimec);
            result.put("factorListMina", factorListMina);
            result.put("factorListMinb", factorListMinb);
            result.put("factorListMinc", factorListMinc);
            result.put("factorListMinTimea", factorListMinTimea);
            result.put("factorListMinTimeb", factorListMinTimeb);
            result.put("factorListMinTimec", factorListMinTimec);
        }
        return result;
    }

    public static List<Double> calculateReactivePower(List<Double> apparentList, List<Double> activeList) {
        if (apparentList.size() != activeList.size()) {
            throw new IllegalArgumentException("apparentList和activeList的长度必须相同");
        }
        return IntStream.range(0, apparentList.size())
                .mapToDouble(i -> {
                    double reactivePower = Math.sqrt(Math.pow(apparentList.get(i), 2) - Math.pow(activeList.get(i), 2));
                    return reactivePower < 0 ? 0 : reactivePower;
                })
                .boxed()
                .collect(Collectors.toList());
    }

    @Override
    public Map getPduHdaLineHisdataKey(String devKey, String type) {
        Map result = new HashMap<>();
        String key = StrUtils.redisKeyByLoginId(null, "/pdu/PDU-device/pduHdaLineHisdata", devKey + SPLIT_KEY + type);
        Object obj = redisTemplate.opsForValue().get(key);
        if (ObjectUtil.isNotEmpty(obj)) {
            JSONObject jsonObject = JSONObject.parseObject(obj.toString());
            result = JSON.toJavaObject(jsonObject, Map.class);
            return result;
        }

        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
        if (pduIndex != null) {
            Integer id = pduIndex.getId();
            // 构建查询请求
            SearchRequest searchRequest = null;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime pastTime = null;


//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String index = "";

            if ("oneHour".equals(type)) {
                pastTime = now.minusHours(1);
                pastTime = pastTime.minusMinutes(1);
                searchRequest = new SearchRequest("pdu_hda_line_realtime");
                index = "pdu_hda_line_realtime";
            } else if ("twentyfourHour".equals(type)) {
                pastTime = now.minusHours(25);
                searchRequest = new SearchRequest("pdu_hda_line_hour");
                index = "pdu_hda_line_hour";
            } else if ("seventytwoHour".equals(type)) {
                pastTime = now.minusHours(73);
                searchRequest = new SearchRequest("pdu_hda_line_day");
                index = "pdu_hda_line_hour";
            }

            String curMaxValue = null;
            String curMinValue = null;
            String volMaxValue = null;
            String volMinValue = null;
            PduHdaLineHouResVO totalCurMax = null;
            PduHdaLineHouResVO totalCurMin = null;
            PduHdaLineHouResVO totalVolMax = null;
            PduHdaLineHouResVO totalVolMin = null;


            // 构建查询请求
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", id));
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(formatter.format(pastTime))
                    .to(formatter.format(now)));
            searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            searchSourceBuilder.size(1000); // 设置返回的最大结果数

            searchRequest.source(searchSourceBuilder);
            List<PduHdaLineRealtimeResVO> oneHourList1 = new ArrayList<>();
            List<PduHdaLineRealtimeResVO> oneHourList2 = new ArrayList<>();
            List<PduHdaLineRealtimeResVO> oneHourList3 = new ArrayList<>();

            List<PduHdaLineHouResVO> dayList1 = new ArrayList<>();
            List<PduHdaLineHouResVO> dayList2 = new ArrayList<>();
            List<PduHdaLineHouResVO> dayList3 = new ArrayList<>();
            List<String> dateTimes = new ArrayList<>();


            // 执行查询请求
            try {
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                if (searchResponse != null) {
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit hit : hits) {
                        String str = hit.getSourceAsString();
                        switch (type) {
                            case "oneHour":
                                PduHdaLineRealtimeResVO realtimeDO = JsonUtils.parseObject(str, PduHdaLineRealtimeResVO.class);
                                switch (realtimeDO.getLineId()) {
                                    case 1:
                                        oneHourList1.add(realtimeDO);
                                        result.put("l", oneHourList1);
                                        break;
                                    case 2:
                                        oneHourList2.add(realtimeDO);
                                        result.put("ll", oneHourList2);
                                        break;
                                    case 3:
                                        oneHourList3.add(realtimeDO);
                                        result.put("lll", oneHourList3);
                                        break;
                                    default:
                                }
                                dateTimes.add(realtimeDO.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
                                break;
                            case "twentyfourHour":
                            case "seventytwoHour":
                                PduHdaLineHouResVO houResVO = JsonUtils.parseObject(str, PduHdaLineHouResVO.class);
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
                                break;
                            default:
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!Objects.equals("oneHour", type)) {
                result.put("l", dayList1);
                result.put("ll", dayList2);
                result.put("lll", dayList3);

                try {
                    curMaxValue = getMaxData(formatter.format(pastTime), formatter.format(now), Arrays.asList(Integer.valueOf(id.intValue())), index, "cur_max_value");
                    totalCurMax = JsonUtils.parseObject(curMaxValue, PduHdaLineHouResVO.class);
                    curMinValue = getMaxData(formatter.format(pastTime), formatter.format(now), Arrays.asList(Integer.valueOf(id.intValue())), index, "cur_min_value");
                    totalCurMin = JsonUtils.parseObject(curMinValue, PduHdaLineHouResVO.class);
                    volMaxValue = getMaxData(formatter.format(pastTime), formatter.format(now), Arrays.asList(Integer.valueOf(id.intValue())), index, "vol_max_value");
                    totalVolMax = JsonUtils.parseObject(volMaxValue, PduHdaLineHouResVO.class);
                    volMinValue = getMaxData(formatter.format(pastTime), formatter.format(now), Arrays.asList(Integer.valueOf(id.intValue())), index, "vol_min_value");
                    totalVolMin = JsonUtils.parseObject(volMinValue, PduHdaLineHouResVO.class);


                    result.put("curAMaxValue", totalCurMax.getCurMaxValue());
                    result.put("curAMaxTime", sdf.format(totalCurMax.getCurMaxTime()));
                    result.put("curAMinValue", totalCurMin.getCurMinValue());
                    result.put("curAMinTime", sdf.format(totalCurMin.getCurMinTime()));
                    result.put("volAMaxValue", totalVolMax.getVolMaxValue());
                    result.put("volAMaxTime", sdf.format(totalVolMax.getVolMaxTime()));
                    result.put("volAMinValue", totalVolMin.getVolMinValue());
                    result.put("volAMinTime", sdf.format(totalVolMin.getVolMinTime()));

                } catch (Exception e) {
                    log.error("获取数据失败", e);
                }
            }
            result.put("dateTimes", dateTimes.stream().distinct().collect(Collectors.toList()));
            redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result), 5, TimeUnit.MINUTES);


            return result;
        } else {
            return result;
        }
    }


    @Override
    public Map pduHdaLineHisdataReportKey(String devKey, Integer type, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map<String, Object> resultMap = new HashMap<>();
        CabinetChartResBase curResBase = new CabinetChartResBase();
        CabinetChartResBase volResBase = new CabinetChartResBase();
        SimpleDateFormat sdfS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String key = StrUtils.redisKeyByLoginId(null, "/pdu/PDU-device/pduHdaLineHisdataReportKey", devKey + SPLIT_KEY + type);
        Object redisObj = redisTemplate.opsForValue().get(key);
        if (ObjectUtil.isNotEmpty(redisObj)) {
            return JSON.parseObject(redisObj.toString(), new HashMap<String, Object>().getClass());
        }

        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapper<PduIndex>().eq(PduIndex::getPduKey, devKey));
        if (pduIndex != null) {
            Integer id = pduIndex.getId();
            String index = (type == 0 || oldTime.toLocalDate().equals(newTime.toLocalDate())) ? "pdu_hda_line_hour" : "pdu_hda_line_day";
            boolean isSameDay = (type == 0 || oldTime.toLocalDate().equals(newTime.toLocalDate()));

            if (!isSameDay) {
                oldTime = oldTime.plusDays(1).withHour(0).withMinute(0).withSecond(0);
                newTime = newTime.plusDays(1).withHour(23).withMinute(59).withSecond(59);
            }

            String startTime = localDateTimeToString(oldTime);
            String endTime = localDateTimeToString(newTime);

            try {
                SearchRequest searchRequest = new SearchRequest(index);
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", id));
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
                        PduHdaLineHouResVO houResVO = JsonUtils.parseObject(str, PduHdaLineHouResVO.class);
                        processLineHisData(houResVO, resultMap, isSameDay, DataType.fromValue(dataType));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 处理结果
            for (int lineId = 1; lineId <= 3; lineId++) {
                String lineKey = "dayList" + lineId;
                Map<String, Object> lineData = (Map<String, Object>) resultMap.get(lineKey);
                if (lineData != null && !(((List<PduHdaLineHouResVO>) lineData.get("data")).isEmpty())) {
                    LineSeries curSeries = new LineSeries();
                    LineSeries volSeries = new LineSeries();
                    resultMap.put("curName" + lineId, lineId == 1 ? "A相电流" : lineId == 2 ? "B相电流" : "C相电流");
                    resultMap.put("volName" + lineId, lineId == 1 ? "A相电压" : lineId == 2 ? "B相电压" : "C相电压");
                    curSeries.setName(getSeriesName("电流", lineId, dataType));
                    volSeries.setName(getSeriesName("电压", lineId, dataType));

                    curSeries.setData((List<Float>) lineData.get("curDataList"));
                    curSeries.setHappenTime((List<String>) lineData.get("curHappenTime"));
                    volSeries.setData((List<Float>) lineData.get("volDataList"));
                    volSeries.setHappenTime((List<String>) lineData.get("volHappenTime"));

                    Map<String, Object> analyzedData = PduAnalysisResult.analyzePduData((List<PduHdaLineHouResVO>) lineData.get("data"), dataType);
                    PduAnalysisResult.CurrentResult currentResult = (PduAnalysisResult.CurrentResult) analyzedData.get("current");
                    PduAnalysisResult.VoltageResult voltageResult = (PduAnalysisResult.VoltageResult) analyzedData.get("voltage");

                    if (dataType != 0) {
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
                }
                resultMap.put("lineNumber", lineId);
            }

            // 添加时间轴数据
            List<String> uniqueDateTimes = (List<String>) resultMap.getOrDefault("dateTimes", new ArrayList<>());
            List<String> xTime = uniqueDateTimes.stream().distinct().collect(Collectors.toList());

            curResBase.setTime(xTime);
            volResBase.setTime(xTime);
            resultMap.put("dateTimes", xTime);
            resultMap.put("curRes", curResBase);
            resultMap.put("volRes", volResBase);

            // 将结果缓存到 Redis
            redisTemplate.opsForValue().set(key, JSON.toJSONString(resultMap), 5, TimeUnit.MINUTES);
        }

        return resultMap;
    }


    public Map getPduHdaLineHisdataKeyByCabinet(Long cabinetId, String type, LocalDateTime oldTime, LocalDateTime newTime) {
        HashMap resultAB = new HashMap<>();
        CabinetIndex cabinetIndex = cabinetIndexMapper.selectOne(new LambdaQueryWrapperX<CabinetIndex>().eq(CabinetIndex::getId, cabinetId));
        if (cabinetIndex.getPduBox().equals(true)) {
            throw exception(NOT_PDU);
        }
        CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, cabinetId));
        String pduKeyA = cabinetPdu.getPduKeyA();
        HashMap result = new HashMap<>();
        HashMap resultB = new HashMap<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, pduKeyA));
        if (pduIndex != null) {
            Integer id = pduIndex.getId();
            // 构建查询请求
            SearchRequest searchRequest = null;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime pastTime = null;
            if ("oneHour".equals(type)) {
                pastTime = now.minusHours(1);
                pastTime = pastTime.minusMinutes(1);
                searchRequest = new SearchRequest("pdu_hda_line_realtime");
            } else if ("twentyfourHour".equals(type)) {
                pastTime = now.minusHours(25);
                searchRequest = new SearchRequest("pdu_hda_line_hour");
            } else if ("seventytwoHour".equals(type)) {
                pastTime = now.minusHours(73);
                searchRequest = new SearchRequest("pdu_hda_line_day");
            }

            // 构建查询请求
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", id));
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(formatter.format(oldTime))
                    .to(formatter.format(newTime)));
            searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            searchSourceBuilder.size(1000); // 设置返回的最大结果数

            searchRequest.source(searchSourceBuilder);
            List<PduHdaLineRealtimeResVO> oneHourList1 = new ArrayList<>();
            List<PduHdaLineRealtimeResVO> oneHourList2 = new ArrayList<>();
            List<PduHdaLineRealtimeResVO> oneHourList3 = new ArrayList<>();

            List<PduHdaLineHouResVO> dayList1 = new ArrayList<>();
            List<PduHdaLineHouResVO> dayList2 = new ArrayList<>();
            List<PduHdaLineHouResVO> dayList3 = new ArrayList<>();
            List<String> dateTimes = new ArrayList<>();


            // 执行查询请求
            try {
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                if (searchResponse != null) {
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit hit : hits) {
                        String str = hit.getSourceAsString();
                        switch (type) {
                            case "oneHour":
                                PduHdaLineRealtimeResVO realtimeDO = JsonUtils.parseObject(str, PduHdaLineRealtimeResVO.class);
                                switch (realtimeDO.getLineId()) {
                                    case 1:
                                        oneHourList1.add(realtimeDO);
                                        result.put("l", oneHourList1);
                                        break;
                                    case 2:
                                        oneHourList2.add(realtimeDO);
                                        result.put("ll", oneHourList2);
                                        break;
                                    case 3:
                                        oneHourList3.add(realtimeDO);
                                        result.put("lll", oneHourList3);
                                        break;
                                    default:
                                }
                                dateTimes.add(realtimeDO.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
                                break;
                            case "twentyfourHour":
                            case "seventytwoHour":
                                PduHdaLineHouResVO houResVO = JsonUtils.parseObject(str, PduHdaLineHouResVO.class);
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
                                break;
                            default:
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!Objects.equals("oneHour", type)) {
                result.put("l", dayList1);
                result.put("ll", dayList2);
                result.put("lll", dayList3);
            }
            result.put("dateTimes", dateTimes.stream().distinct().collect(Collectors.toList()));
            resultAB.put("A", result);

        } else {
            return resultAB;
        }
        String pduKeyB = cabinetPdu.getPduKeyB();
        PduIndex pduIndex1 = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, pduKeyB));
        if (pduIndex1 != null) {
            Integer id = pduIndex1.getId();
            // 构建查询请求
            SearchRequest searchRequest = null;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime pastTime = null;
            if ("oneHour".equals(type)) {
                pastTime = now.minusHours(1);
                pastTime = pastTime.minusMinutes(1);
                searchRequest = new SearchRequest("pdu_hda_line_realtime");
            } else if ("twentyfourHour".equals(type)) {
                pastTime = now.minusHours(25);
                searchRequest = new SearchRequest("pdu_hda_line_hour");
            } else if ("seventytwoHour".equals(type)) {
                pastTime = now.minusHours(73);
                searchRequest = new SearchRequest("pdu_hda_line_day");
            }

            // 构建查询请求
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", id));
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(formatter.format(pastTime))
                    .to(formatter.format(now)));
            searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
            searchSourceBuilder.size(1000); // 设置返回的最大结果数

            searchRequest.source(searchSourceBuilder);
            List<PduHdaLineRealtimeResVO> oneHourList1 = new ArrayList<>();
            List<PduHdaLineRealtimeResVO> oneHourList2 = new ArrayList<>();
            List<PduHdaLineRealtimeResVO> oneHourList3 = new ArrayList<>();

            List<PduHdaLineHouResVO> dayList1 = new ArrayList<>();
            List<PduHdaLineHouResVO> dayList2 = new ArrayList<>();
            List<PduHdaLineHouResVO> dayList3 = new ArrayList<>();
            List<String> dateTimes = new ArrayList<>();

            // 执行查询请求
            try {
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                if (searchResponse != null) {
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit hit : hits) {
                        String str = hit.getSourceAsString();
                        switch (type) {
                            case "oneHour":
                                PduHdaLineRealtimeResVO realtimeDO = JsonUtils.parseObject(str, PduHdaLineRealtimeResVO.class);
                                switch (realtimeDO.getLineId()) {
                                    case 1:
                                        oneHourList1.add(realtimeDO);
                                        resultB.put("l", oneHourList1);
                                        break;
                                    case 2:
                                        oneHourList2.add(realtimeDO);
                                        resultB.put("ll", oneHourList2);
                                        break;
                                    case 3:
                                        oneHourList3.add(realtimeDO);
                                        resultB.put("lll", oneHourList3);
                                        break;
                                    default:
                                }
                                dateTimes.add(realtimeDO.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
                                break;
                            case "twentyfourHour":
                            case "seventytwoHour":
                                PduHdaLineHouResVO houResVO = JsonUtils.parseObject(str, PduHdaLineHouResVO.class);
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
                                break;
                            default:
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!Objects.equals("oneHour", type)) {
                resultB.put("l", dayList1);
                resultB.put("ll", dayList2);
                resultB.put("lll", dayList3);
            }
            resultB.put("dateTimes", dateTimes.stream().distinct().collect(Collectors.toList()));
            resultAB.put("B", resultB);

        } else {
            return resultAB;
        }
        return resultAB;

    }

    @Override
    public Map getPduMaxLine(PDURequireDetailReq pduRequireDetailReq) {
        Integer id = pduRequireDetailReq.getId();
        String type = pduRequireDetailReq.getType();
        LocalDateTime startTime = pduRequireDetailReq.getStartTime();
        LocalDateTime endTime = pduRequireDetailReq.getEndTime();
        HashMap result = new HashMap<>();
        // 构建查询请求
        SearchRequest searchRequest = null;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = null;
        if ("hour".equals(type)) {
            pastTime = startTime;
            searchRequest = new SearchRequest("pdu_hda_line_hour");
        } else if ("day".equals(type)) {
            pastTime = startTime;
            now = endTime;
            searchRequest = new SearchRequest("pdu_hda_line_day");
        }

        // 构建查询请求
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", id));
        searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                .from(formatter.format(pastTime))
                .to(formatter.format(now)));
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(1000); // 设置返回的最大结果数

        searchRequest.source(searchSourceBuilder);
        List<PduHdaLineMaxResVO> dayList1 = new ArrayList<>();
        List<PduHdaLineMaxResVO> dayList2 = new ArrayList<>();
        List<PduHdaLineMaxResVO> dayList3 = new ArrayList<>();
        List<String> dateTimes = new ArrayList<>();
        // 执行查询请求
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse != null) {
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits) {
                    String str = hit.getSourceAsString();
                    PduHdaLineMaxResVO houResVO = JsonUtils.parseObject(str, PduHdaLineMaxResVO.class);
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.put("l", dayList1);
        result.put("ll", dayList2);
        result.put("lll", dayList3);
        result.put("dateTimes", dateTimes.stream().distinct().collect(Collectors.toList()));
        return result;
    }

    @Override
    public PduBalanceDeatilRes getPDUDeviceDetail(String key) {
        QueryWrapper<PduIndex> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pdu_key", key);
        PduIndex pduIndex = pDUDeviceMapper.selectOne(queryWrapper);
        PduBalanceDeatilRes result = new PduBalanceDeatilRes();
        if (pduIndex.getRunStatus() == 5) {
            result = null;
            return result;
        }
        Integer curUnbalanceStatus = pduIndex.getCurUnbalanceStatus();

        PDUCurbalanceColorDO PDUCurbalanceColorDO = PDUCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get(REDIS_KEY_PDU + key);
        if (jsonObject == null) {
            return result;
        }
        JSONObject pduTgData = jsonObject.getJSONObject("pdu_data").getJSONObject("pdu_total_data");
        JSONObject lineItemList = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list");
        List<Double> curList = lineItemList.getJSONArray("cur_value").toList(Double.class);
        List<Double> volList = lineItemList.getJSONArray("vol_value").toList(Double.class);

        Double curUnbalance = pduTgData.getDoubleValue("cur_unbalance");
        Double volUnbalance = pduTgData.getDoubleValue("vol_unbalance");
        result.setCur_value(curList);
        result.setVol_value(volList);
        result.setCurUnbalance(new BigDecimal(curUnbalance).setScale(2, RoundingMode.HALF_UP).doubleValue());
        result.setVolUnbalance(new BigDecimal(volUnbalance).setScale(2, RoundingMode.HALF_UP).doubleValue());
        JSONArray curAlarmArr = lineItemList.getJSONArray("cur_alarm_max");
        List<Double> sortAlarmArr = curAlarmArr.toList(Double.class);
        sortAlarmArr.sort(Collections.reverseOrder());
        double maxVal = sortAlarmArr.get(0);
//        List<Double> temp = curValue.toList(Double.class);
//        curList.sort(Collections.reverseOrder());
        double a = Collections.max(curList) - Collections.min(curList);// curList.get(0) - curList.get(2);
        int color = 0;
        if (PDUCurbalanceColorDO == null) {
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
                if (curUnbalance < PDUCurbalanceColorDO.getRangeOne()) {
                    color = 2;
                } else if (curUnbalance < PDUCurbalanceColorDO.getRangeFour()) {
                    color = 3;
                } else {
                    color = 4;
                }
            } else {
                color = 1;
            }
        }
        result.setColor(curUnbalanceStatus);
        return result;
    }

    @Override
    public List<PduTrendVO> getPudBalanceTrend(Integer pduId, Integer timeType) {
        List<PduTrendVO> result = new ArrayList<>();
        try {
            List<Integer> ids = Arrays.asList(pduId);
            String startTime;
            String endTime;
            LocalDateTime now = LocalDateTime.now();
            String index = "pdu_hda_line_hour";
            startTime = LocalDateTimeUtil.format(now.minusHours(24), "yyyy-MM-dd HH:mm:ss");
            endTime = LocalDateTimeUtil.format(now, "yyyy-MM-dd HH:mm:ss");
            if (Objects.equals(timeType, 0)) {
                index = "pdu_hda_line_realtime";
                startTime = LocalDateTimeUtil.format(now.minusHours(1), "yyyy-MM-dd HH:mm:ss");
                endTime = LocalDateTimeUtil.format(now, "yyyy-MM-dd HH:mm:ss");
            }
            List<String> data = getData(startTime, endTime, ids, index);
            Map<String, List> timeBus = new HashMap<>();
            data.forEach(str -> {
                if (Objects.equals(timeType, 0)) {
                    PduHdaLineRealtimeDo hourDo = JsonUtils.parseObject(str, PduHdaLineRealtimeDo.class);
                    String dateTime = DateUtil.format(hourDo.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                    List<PduHdaLineRealtimeDo> lineHourDos = timeBus.get(dateTime);
                    if (CollectionUtils.isEmpty(lineHourDos)) {
                        lineHourDos = new ArrayList<>();
                    }
                    lineHourDos.add(hourDo);
                    timeBus.put(dateTime, lineHourDos);
                } else {
                    PduHdaLineHourDo hourDo = JsonUtils.parseObject(str, PduHdaLineHourDo.class);
                    String dateTime = DateUtil.format(hourDo.getCreateTime(), "yyyy-MM-dd HH");
                    List<PduHdaLineHourDo> lineHourDos = timeBus.get(dateTime);
                    if (CollectionUtils.isEmpty(lineHourDos)) {
                        lineHourDos = new ArrayList<>();
                    }
                    lineHourDos.add(hourDo);
                    timeBus.put(dateTime, lineHourDos);
                }
            });

            timeBus.keySet().forEach(dateTime -> {
                PduTrendVO trendDTO = new PduTrendVO();
                trendDTO.setDateTime(dateTime);
                List<Map<String, Object>> cur = new ArrayList<>();
                List<Map<String, Object>> vol = new ArrayList<>();
                //获取每个时间段数据
                if (Objects.equals(timeType, 0)) {
                    List<PduHdaLineRealtimeDo> boxLineHourDos = timeBus.get(dateTime);
                    boxLineHourDos.forEach(hourDo -> {
                        Map<String, Object> curMap = new HashMap<>();
                        curMap.put("lineId", hourDo.getLineId());
                        curMap.put("curValue", hourDo.getCur());
                        Map<String, Object> volMap = new HashMap<>();
                        volMap.put("lineId", hourDo.getLineId());
                        volMap.put("volValue", hourDo.getVol());
                        cur.add(curMap);
                        vol.add(volMap);
                    });
                } else {
                    List<PduHdaLineHourDo> boxLineHourDos = timeBus.get(dateTime);
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
            return result.stream().sorted(Comparator.comparing(PduTrendVO::getDateTime)).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public PduDeviceCountResVO getPDUDeviceCount() {

        return pDUDeviceMapper.getPDUDeviceCount();
    }

    @Override
    public BalancedDistributionStatisticsVO getBalancedDistribution() {
        BalancedDistributionStatisticsVO vo = pDUDeviceMapper.getBalancedDistribution();
//        Set<String> keys = redisTemplate.keys("packet:pdu:*");
//        List<Object> list = redisTemplate.opsForValue().multiGet(keys);
//        PDUCurbalanceColorDO PDUCurbalanceColorDO = PDUCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
//        BalancedDistributionStatisticsVO vo = new BalancedDistributionStatisticsVO();
//        for (Object o : list) {
//            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
//            JSONObject pduTgData = jsonObject.getJSONObject("pdu_data").getJSONObject("pdu_total_data");
//            JSONArray curArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("cur_value");
//
//            JSONArray curAlarmArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("cur_alarm_max");
//            curAlarmArr.sort(Collections.reverseOrder());
//            double maxVal = curAlarmArr.getDouble(0);
//            List<Double> temp = curArr.toList(Double.class);
//            Double curUnbalance;
//
//            temp.sort(Collections.reverseOrder());
//            if (temp.size() >= 2) {
//                double a = temp.get(0) - temp.get(2);
//                curUnbalance = pduTgData.getDoubleValue("cur_unbalance");
//
//                if (PDUCurbalanceColorDO == null) {
//                    if (a >= maxVal * 0.2) {
//                        if (curUnbalance < 15) {
//                            vo.setLessFifteen(vo.getLessFifteen()+1);
//                        } else if (curUnbalance < 30) {
//                            vo.setGreaterFifteen(vo.getGreaterFifteen()+1);
//                        } else {
//                            vo.setGreaterThirty(vo.getGreaterThirty()+1);
//                        }
//                    } else {
//                        vo.setSmallCurrent(vo.getSmallCurrent() + 1);
//                    }
//                } else {
//                    if (a >= maxVal * 0.2) {
//                        if (curUnbalance < PDUCurbalanceColorDO.getRangeOne()) {
//                            vo.setLessFifteen(vo.getLessFifteen()+1);
//                        } else if (curUnbalance < PDUCurbalanceColorDO.getRangeFour()) {
//                            vo.setGreaterFifteen(vo.getGreaterFifteen()+1);
//                        } else {
//                            vo.setGreaterThirty(vo.getGreaterThirty()+1);
//                        }
//                    } else {
//                        vo.setSmallCurrent(vo.getSmallCurrent() + 1);
//                    }
//                }
//            }
//        }
        return vo;
    }


    @Override
    public Map getChartNewDataByPduDevKey(String devKey, LocalDateTime oldTime, String type) {
        HashMap result = new HashMap<>();

        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
        if (pduIndex != null) {
            Integer id = pduIndex.getId();
            // 构建查询请求
            SearchRequest searchRequest = null;
            LocalDateTime newTime = null;
            if ("oneHour".equals(type)) {
                newTime = oldTime.plusMinutes(1);
                newTime = newTime.plusSeconds(20);
                // 构建查询请求
                searchRequest = new SearchRequest("pdu_hda_total_realtime");
            } else if ("twentyfourHour".equals(type)) {
                newTime = oldTime.plusHours(1);
                newTime = newTime.plusMinutes(20);
                // 构建查询请求
                searchRequest = new SearchRequest("pdu_hda_total_hour");
            }

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", id));
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(formatter.format(oldTime))
                    .to(formatter.format(newTime)));
            searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
            searchSourceBuilder.size(1); // 设置返回的最大结果数
            searchRequest.source(searchSourceBuilder);

            double apparent = 0;
            double active = 0;
            String dateTime = "";
            try {
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                if (searchResponse != null) {
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit hit : hits) {
                        String str = hit.getSourceAsString();
                        if ("oneHour".equals(type)) {
                            PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                            apparent = pduHdaTotalRealtimeDo.getApparentPow();
                            active = pduHdaTotalRealtimeDo.getActivePow();
                            dateTime = pduHdaTotalRealtimeDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        } else if ("twentyfourHour".equals(type)) {
                            PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaTotalHourDo.class);
                            apparent = pduHdaTotalHourDo.getApparentPowAvgValue();
                            active = pduHdaTotalHourDo.getActivePowAvgValue();
                            dateTime = pduHdaTotalHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        }

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            result.put("apparent", apparent);
            result.put("active", active);
            result.put("dateTime", dateTime);

            return result;
        } else {
            return result;
        }

    }

    @Override
    public Map getReportConsumeDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase barRes = new CabinetChartResBase();
        BarSeries barSeries = new BarSeries();
        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
            if (pduIndex != null) {
                String index = null;
                boolean isSameDay = false;
                Integer Id = pduIndex.getId();
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "pdu_eq_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                // TODO 电力计算错误，待电力计算错误解决再实现功能
                List<String> realtimeData = getData(startTime, endTime, Arrays.asList(Id.intValue()), "pdu_ele_total_realtime");
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Id.intValue()), index);
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay) {
                    for (String str : cabinetData) {
                        nowTimes++;
                        PduEleTotalRealtimeDo eleDO = JsonUtils.parseObject(str, PduEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = eleDO.getEle();
                        }
                        if (nowTimes > 1) {
                            barSeries.getData().add((float) (eleDO.getEle() - lastEq));
                            barRes.getTime().add(eleDO.getCreateTime().toString("HH:mm"));
                        }
                        lastEq = eleDO.getEle();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "ele_active");
                    PduEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, PduEleTotalRealtimeDo.class);
                    if (eleMaxValue != null) {
                        maxEle = eleMaxValue.getEle();
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
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "eq_value");
                    PduEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, PduEqTotalDayDo.class);
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

//    @Override
//    public Map getReportConsumeDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
//        Map result = new HashMap<>();
//        CabinetChartResBase barRes = new CabinetChartResBase();
//        BarSeries barSeries = new BarSeries();
//        try {
//            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
//            if (pduIndex != null) {
//                String index = null;
//                boolean isSameDay = false;
//                Integer Id = pduIndex.getId();
//                index = "pdu_ele_total_realtime";
//                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
//                    if (oldTime.equals(newTime)) {
//                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
//                    }
//                    isSameDay = true;
//                } else {
//                    oldTime = oldTime.plusDays(1);
//                    newTime = newTime.plusDays(1);
//                    isSameDay = false;
//                }
//                String startTime = localDateTimeToString(oldTime);
//                String endTime = localDateTimeToString(newTime);
//                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Id.intValue()), index);
//                //月报起始电能值
//                Double firstMEq = 0D;
//                Double lastMEq = 0D;
//                PduEleTotalRealtimeDo firstDo = JsonUtils.parseObject(cabinetData.get(0), PduEleTotalRealtimeDo.class);
//                firstMEq = firstDo.getEle();
//                PduEleTotalRealtimeDo endDo = JsonUtils.parseObject(cabinetData.get(cabinetData.size() - 1), PduEleTotalRealtimeDo.class);
//                lastMEq = endDo.getEle();
//                Double firstEq = 0D;
//                Double lastEq = 0D;
//                Double totalEq = 0D;
//                Double maxEle = 0D;
//                String maxEleTime = null;
//                int nowTimes = 0;
//
//                if (isSameDay) {
//                    for (String str : cabinetData) {
//                        nowTimes++;
//                        PduEleTotalRealtimeDo eleDO = JsonUtils.parseObject(str, PduEleTotalRealtimeDo.class);
//                        if (nowTimes == 1) {
//                            firstEq = eleDO.getEle();
//                        }
//                        if (nowTimes > 1) {
//                            barSeries.getData().add((float) (eleDO.getEle() - lastEq));
//                            barRes.getTime().add(eleDO.getCreateTime().toString("HH:mm"));
//                        }
//                        lastEq = eleDO.getEle();
//                    }
//                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "ele_active");
//                    PduEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, PduEleTotalRealtimeDo.class);
//                    if (eleMaxValue != null) {
//                        maxEle = eleMaxValue.getEle();
//                        maxEleTime = eleMaxValue.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
//                    }
//                    barRes.getSeries().add(barSeries);
//                    result.put("totalEle", totalEq);
//                    result.put("maxEle", maxEle);
//                    result.put("maxEleTime", maxEleTime);
//                    result.put("firstEq", firstEq);
//                    result.put("lastEq", lastEq);
//                    result.put("barRes", barRes);
//
//                } else {
//
//                    List<PduEleTotalRealtimeDo> realtimeDoList = new ArrayList<>();
//                    for (String str : cabinetData) {
//                        nowTimes++;
//                        PduEleTotalRealtimeDo eleDO = JsonUtils.parseObject(str, PduEleTotalRealtimeDo.class);
//                        realtimeDoList.add(eleDO);
//                        totalEq += eleDO.getEle();
//                    }
//                    // 按日期分组
//                    Map<String, List<PduEleTotalRealtimeDo>> groupedByDate = realtimeDoList.stream()
//                            .collect(Collectors.groupingBy(ele -> ele.getCreateTime().toString("yyyy-MM-dd")));
//
//                    TreeMap<String, List<PduEleTotalRealtimeDo>> stringListTreeMap = new TreeMap<>(groupedByDate);
//                    int i = 0;
//                    Map<Integer,List<PduEleTotalRealtimeDo>> orgData = new HashMap();
//                    Map<Integer,List<PduEleTotalRealtimeDo>> afterData = new HashMap();
//                    for (Map.Entry<String, List<PduEleTotalRealtimeDo>> entry : stringListTreeMap.entrySet()) {
//                        if (i==0){
//                            orgData.put(i,entry.getValue());
//                        }else {
//                            orgData.put(i,entry.getValue());
//                            afterData.put(i,entry.getValue());
//                        }
//                        i++;
//                    }
//                    int j = 0;
//                    for (Map.Entry<String, List<PduEleTotalRealtimeDo>> entry : stringListTreeMap.entrySet()) {
//                        String date = entry.getKey();
//                        Double dayTotal = 0.0;
//                        List<PduEleTotalRealtimeDo> valueList = entry.getValue();
//                        if (valueList.size() == 24) {
//                            dayTotal = afterData.get(j+1).get(0).getEle() - orgData.get(j).get(0).getEle();
//                        }else {
//                            int size = orgData.get(j).size();
//                            dayTotal = orgData.get(j).get(size-1).getEle() - orgData.get(j).get(0).getEle();
//                        }
//                        System.out.println("Date: " + date);
//                        barSeries.getData().add(dayTotal.floatValue());
//                        barRes.getTime().add(date);
//
//                        j++;
//                    }
//                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), "pdu_eq_total_day", "eq_value");
//                    PduEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, PduEqTotalDayDo.class);
//                    if (eqMaxValue != null) {
//                        maxEle = eqMaxValue.getEq();
//                        maxEleTime = eqMaxValue.getStartTime().toString("yyyy-MM-dd HH:mm:ss");
//                    }
//
//
//                    barRes.getSeries().add(barSeries);
//                    result.put("totalEle", lastMEq - firstMEq);
//                    result.put("maxEle", maxEle);
//                    result.put("firstEq", firstMEq);
//                    result.put("lastEq", lastMEq);
//                    result.put("maxEleTime", maxEleTime);
//                    result.put("barRes", barRes);
//                }
//            }
//        } catch (Exception e) {
//            log.error("获取数据失败", e);
//        }
//        return result;
//    }

    @Override
    public Map getPDUPFLine(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));

            if (pduIndex != null) {
                String pfIndex = null;
                Integer Id = pduIndex.getId();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    pfIndex = "pdu_hda_total_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                } else {
                    pfIndex = "pdu_hda_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                List<String> happenTime = null;

                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), pfIndex);
                List<PduHdaTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, PduHdaTotalHourDo.class)).collect(Collectors.toList());

                LineSeries totalPFLine = new LineSeries();
                processPFMavMin(powList, dataType, timeType, totalLineRes, totalPFLine, result, oldTime, newTime);
                if (dataType == 1) {
                    happenTime = powList.stream().map(PduHdaTotalHourDo -> PduHdaTotalHourDo.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                } else if (dataType == -1) {
                    happenTime = powList.stream().map(PduHdaTotalHourDo -> PduHdaTotalHourDo.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }

                totalLineRes.getSeries().add(totalPFLine);
                totalPFLine.setHappenTime(happenTime);
                //获取相功率因数
                SearchRequest searchRequest = null;
                String index = "";

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_hda_line_hour";
                } else {
                    index = "pdu_hda_line_day";
                }

                searchRequest = new SearchRequest(index);
                // 构建查询请求
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", Id));
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                        .from(formatter.format(oldTime))
                        .to(formatter.format(newTime)));
                searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                searchSourceBuilder.size(1000); // 设置返回的最大结果数
                searchRequest.source(searchSourceBuilder);
                try {
                    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                    if (searchResponse != null) {
                        SearchHits hits = searchResponse.getHits();

                        processData(hits, dataType, result, totalLineRes);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                result.put("pfLineRes", totalLineRes);

            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    /**
     * 功率曲线数据处理
     *
     * @param hits
     * @param dataType
     * @param result
     * @param totalLineRes
     */
    public void processData(SearchHits hits, Integer dataType, Map result, CabinetChartResBase totalLineRes) {
        //描述数据收集
        Float lineAMax = -0.1f;
        Float lineAMin = 1.1f;
        String lineAMaxTime = "";
        String lineAMinTime = "";

        Float lineBMax = -0.1f;
        Float lineBMin = 1.1f;
        String lineBMaxTime = "";
        String lineBMinTime = "";

        Float lineCMax = -0.1f;
        Float lineCMin = 1.1f;
        String lineCMaxTime = "";
        String lineCMinTime = "";

        LineSeries lineASeries = new LineSeries();
        LineSeries lineBSeries = new LineSeries();
        LineSeries lineCSeries = new LineSeries();

        List<String> lineAHappenTime = new ArrayList<>();
        List<String> lineBHappenTime = new ArrayList<>();
        List<String> lineCHappenTime = new ArrayList<>();
        int index = 2;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : hits) {

            String str = hit.getSourceAsString();
            PduHdaLineHouResVO houResVO = JsonUtils.parseObject(str, PduHdaLineHouResVO.class);
            switch (houResVO.getLineId()) {
                case 1:
                    //判断数据类型（最大最小平均）
                    if (dataType == 1) {
                        if (houResVO.getPowerFactorMaxValue() > lineAMax) {
                            lineAMax = houResVO.getPowerFactorMaxValue();
                            lineAMaxTime = sdf.format(houResVO.getPowerFactorMaxTime());
                        }
                        if (houResVO.getPowerFactorMaxValue() < lineAMin) {
                            lineAMin = houResVO.getPowerFactorMaxValue();
                            lineAMinTime = sdf.format(houResVO.getPowerFactorMaxTime());

                        }
                        lineASeries.setName(PduDataTypeEnum.FACTOR_LINE_A_MAX.getDataType());
                        lineASeries.getData().add(houResVO.getPowerFactorMaxValue());

                        lineAHappenTime.add(sdf.format(houResVO.getPowerFactorMaxTime()));
                    } else if (dataType == 0) {
                        lineAMaxTime = "无";
                        lineAMinTime = "无";
                        if (houResVO.getPowerFactorAvgValue() > lineAMax) {
                            lineAMax = houResVO.getPowerFactorAvgValue();
                        }
                        if (houResVO.getPowerFactorAvgValue() < lineAMin) {
                            lineAMin = houResVO.getPowerFactorAvgValue();
                        }
                        lineASeries.setName(PduDataTypeEnum.FACTOR_LINE_A_AVG.getDataType());
                        lineASeries.getData().add(houResVO.getPowerFactorAvgValue());

                    } else if (dataType == -1) {
                        if (houResVO.getPowerFactorMinValue() > lineAMax) {
                            lineAMax = houResVO.getPowerFactorMinValue();
                            lineAMaxTime = sdf.format(houResVO.getPowerFactorMinTime());
                        }
                        if (houResVO.getPowerFactorMinValue() < lineAMin) {
                            lineAMin = houResVO.getPowerFactorMinValue();
                            lineAMinTime = sdf.format(houResVO.getPowerFactorMinTime());
                        }
                        lineASeries.setName(PduDataTypeEnum.FACTOR_LINE_A_MIN.getDataType());
                        lineASeries.getData().add(houResVO.getPowerFactorMinValue());
                        lineAHappenTime.add(sdf.format(houResVO.getPowerFactorMinTime()));

                    }
                    result.put("lineName" + index, "A相功率因素");
                    result.put("lineMax" + index, lineAMax);
                    result.put("lineMin" + index, lineAMin);
                    result.put("lineMaxTime" + index, lineAMaxTime);
                    result.put("lineMinTime" + index, lineAMinTime);
                    index++;

                    break;
                case 2:
                    if (dataType == 1) {
                        if (houResVO.getPowerFactorMaxValue() > lineBMax) {
                            lineBMax = houResVO.getPowerFactorMaxValue();
                            lineBMaxTime = sdf.format(houResVO.getPowerFactorMaxTime());
                        }
                        if (houResVO.getPowerFactorMaxValue() < lineBMin) {
                            lineBMin = houResVO.getPowerFactorMaxValue();
                            lineBMinTime = sdf.format(houResVO.getPowerFactorMaxTime());
                        }
                        lineBSeries.setName(PduDataTypeEnum.FACTOR_LINE_B_MAX.getDataType());
                        lineBSeries.getData().add(houResVO.getPowerFactorMaxValue());
                        lineBHappenTime.add(sdf.format(houResVO.getPowerFactorMaxTime()));


                    } else if (dataType == 0) {
                        lineBMaxTime = "无";
                        lineBMinTime = "无";
                        if (houResVO.getPowerFactorAvgValue() > lineBMax) {
                            lineBMax = houResVO.getPowerFactorAvgValue();

                        }
                        if (houResVO.getPowerFactorAvgValue() < lineBMin) {
                            lineBMin = houResVO.getPowerFactorAvgValue();
                        }
                        lineBSeries.setName(PduDataTypeEnum.FACTOR_LINE_B_AVG.getDataType());
                        lineBSeries.getData().add(houResVO.getPowerFactorAvgValue());


                    } else if (dataType == -1) {
                        if (houResVO.getPowerFactorMinValue() > lineBMax) {
                            lineBMax = houResVO.getPowerFactorMinValue();
                            lineBMaxTime = sdf.format(houResVO.getPowerFactorMinTime());
                        }
                        if (houResVO.getPowerFactorMinValue() < lineBMin) {
                            lineBMin = houResVO.getPowerFactorMinValue();
                            lineBMinTime = sdf.format(houResVO.getPowerFactorMinTime());
                        }
                        lineBSeries.setName(PduDataTypeEnum.FACTOR_LINE_B_MIN.getDataType());
                        lineBSeries.getData().add(houResVO.getPowerFactorMinValue());
                        lineBHappenTime.add(sdf.format(houResVO.getPowerFactorMinTime()));

                    }
                    result.put("lineName" + index, "B相功率因素");
                    result.put("lineMax" + index, lineBMax);
                    result.put("lineMin" + index, lineBMin);
                    result.put("lineMaxTime" + index, lineBMaxTime);
                    result.put("lineMinTime" + index, lineBMinTime);
                    index++;

                    break;
                case 3:
                    if (dataType == 1) {
                        if (houResVO.getPowerFactorMaxValue() > lineCMax) {
                            lineCMax = houResVO.getPowerFactorMaxValue();
                            lineCMaxTime = sdf.format(houResVO.getPowerFactorMaxTime());
                        }
                        if (houResVO.getPowerFactorMaxValue() < lineCMin) {
                            lineCMin = houResVO.getPowerFactorMaxValue();
                            lineCMinTime = sdf.format(houResVO.getPowerFactorMaxTime());
                        }
                        lineCSeries.setName(PduDataTypeEnum.FACTOR_LINE_C_MAX.getDataType());
                        lineCSeries.getData().add(houResVO.getPowerFactorMaxValue());
                        lineCHappenTime.add(sdf.format(houResVO.getPowerFactorMaxTime()));

                    } else if (dataType == 0) {
                        lineCMaxTime = "无";
                        lineCMinTime = "无";
                        if (houResVO.getPowerFactorAvgValue() > lineCMax) {
                            lineCMax = houResVO.getPowerFactorAvgValue();
                        }
                        if (houResVO.getPowerFactorAvgValue() < lineCMin) {
                            lineCMin = houResVO.getPowerFactorAvgValue();
                        }
                        lineCSeries.setName(PduDataTypeEnum.FACTOR_LINE_C_AVG.getDataType());
                        lineCSeries.getData().add(houResVO.getPowerFactorAvgValue());


                    } else if (dataType == -1) {
                        if (houResVO.getPowerFactorMinValue() > lineCMax) {
                            lineCMax = houResVO.getPowerFactorMinValue();
                            lineCMaxTime = sdf.format(houResVO.getPowerFactorMinTime());
                        }
                        if (houResVO.getPowerFactorMinValue() < lineCMin) {
                            lineCMin = houResVO.getPowerFactorMinValue();
                            lineCMinTime = sdf.format(houResVO.getPowerFactorMinTime());
                        }
                        lineCSeries.setName(PduDataTypeEnum.FACTOR_LINE_C_MIN.getDataType());
                        lineCSeries.getData().add(houResVO.getPowerFactorMinValue());
                        lineCHappenTime.add(sdf.format(houResVO.getPowerFactorMinTime()));

                    }
                    result.put("lineName" + index, "C相功率因素");
                    result.put("lineMax" + index, lineCMax);
                    result.put("lineMin" + index, lineCMin);
                    result.put("lineMaxTime" + index, lineCMaxTime);
                    result.put("lineMinTime" + index, lineCMinTime);
                    index++;

                    break;
                default:
            }

        }
        lineASeries.setHappenTime(lineAHappenTime);
        lineBSeries.setHappenTime(lineBHappenTime);
        lineCSeries.setHappenTime(lineCHappenTime);
        totalLineRes.getSeries().add(lineASeries);
        totalLineRes.getSeries().add(lineBSeries);
        totalLineRes.getSeries().add(lineCSeries);


    }


    @Override
    public int deletePDU(String devKey) throws Exception {
        PduIndex index = pDUDeviceMapper.selectOne("pdu_key", devKey);
        if (Objects.isNull(index)) {
            return -1;
        }
        //逻辑删除
        pDUDeviceMapper.update(new LambdaUpdateWrapper<PduIndex>()
                .eq(PduIndex::getPduKey, devKey)
                .set(PduIndex::getIsDeleted, DelEnums.DELETE.getStatus())
        );
        return Math.toIntExact(index.getId());
    }

    @Override
    public int restorePDU(String devKey) throws Exception {
        PduIndex index = pDUDeviceMapper.selectOne("pdu_key", devKey);
        if (Objects.isNull(index)) {
            return -1;
        }
        //逻辑删除
        pDUDeviceMapper.update(new LambdaUpdateWrapper<PduIndex>()
                .eq(PduIndex::getPduKey, devKey)
                .set(PduIndex::getIsDeleted, DelEnums.NO_DEL.getStatus())
        );
        return Math.toIntExact(index.getId());
    }

    @Override
    public Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));

            if (pduIndex != null) {
                String index = null;
                Integer Id = pduIndex.getId();

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_hda_total_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                } else {
                    index = "pdu_hda_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> data = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index);
                List<PduHdaTotalHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, PduHdaTotalHourDo.class)).collect(Collectors.toList());

                LineSeries totalApparentPow = new LineSeries();
                LineSeries totalActivePow = new LineSeries();
                LineSeries totalReactivePow = new LineSeries();

                List<String> totalApparentPowHappenTime = null;
                List<String> totalActivePowHappenTime = null;
                List<String> totalReactivePowHappenTime = null;

                PowerData apparentPow = new PowerData();
                PowerData activePow = new PowerData();
                PowerData reactivePow = new PowerData();

                //判断时间区间
                boolean timeFlag = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    timeFlag = true;
                }
                for (PduHdaTotalHourDo pduHdaTotalHourDo : powList) {

                    updatePowerData(apparentPow, pduHdaTotalHourDo.getApparentPowMaxValue(), pduHdaTotalHourDo.getApparentPowMaxTime().toString("yyyy-MM-dd HH:mm:ss")
                            , pduHdaTotalHourDo.getApparentPowAvgValue(), pduHdaTotalHourDo.getApparentPowMinValue()
                            , pduHdaTotalHourDo.getApparentPowMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);


                    updatePowerData(activePow, pduHdaTotalHourDo.getActivePowMaxValue(), pduHdaTotalHourDo.getActivePowMaxTime().toString("yyyy-MM-dd HH:mm:ss")
                            , pduHdaTotalHourDo.getActivePowAvgValue(), pduHdaTotalHourDo.getActivePowMinValue()
                            , pduHdaTotalHourDo.getActivePowMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);


                    updatePowerData(reactivePow, pduHdaTotalHourDo.getPowReactiveMaxValue(), pduHdaTotalHourDo.getPowReactiveMaxTime().toString("yyyy-MM-dd HH:mm:ss")
                            , pduHdaTotalHourDo.getPowReactiveAvgValue(), pduHdaTotalHourDo.getPowReactiveMinValue()
                            , pduHdaTotalHourDo.getPowReactiveMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType);

                    if (timeFlag) {
                        totalLineRes.getTime().add(pduHdaTotalHourDo.getCreateTime().toString("HH:mm"));
                    } else {
                        totalLineRes.getTime().add(pduHdaTotalHourDo.getCreateTime().toString("yyyy-MM-dd"));
                    }
                }

                result.put("apparentPowMaxValue", apparentPow.getMaxValue());
                result.put("apparentPowMaxTime", apparentPow.getMaxTime());
                result.put("apparentPowMinValue", apparentPow.getMinValue());
                result.put("apparentPowMinTime", apparentPow.getMinTime());

                result.put("activePowMaxValue", activePow.getMaxValue());
                result.put("activePowMaxTime", activePow.getMaxTime());
                result.put("activePowMinValue", activePow.getMinValue());
                result.put("activePowMinTime", activePow.getMinTime());

                result.put("reactivePowMaxValue", reactivePow.getMaxValue());
                result.put("reactivePowMaxTime", reactivePow.getMaxTime());
                result.put("reactivePowMinValue", reactivePow.getMinValue());
                result.put("reactivePowMinTime", reactivePow.getMinTime());


                //判断是设置的数据类型（最大值、最小值、平均值）
                if (dataType == 1) {
                    totalApparentPow.setName(PduDataTypeEnum.APPARENT_TOTAL_MAX.getDataType());
                    totalActivePow.setName(PduDataTypeEnum.ACTIVE_TOTAL_MAX.getDataType());
                    totalReactivePow.setName(PduDataTypeEnum.REACTIVE_TOTAL_MAX.getDataType());
                    for (PduHdaTotalHourDo pduHdaTotalHourDo : powList) {

                        totalApparentPow.getData().add(pduHdaTotalHourDo.getApparentPowMaxValue());
                        totalActivePow.getData().add(pduHdaTotalHourDo.getActivePowMaxValue());
                        totalReactivePow.getData().add(pduHdaTotalHourDo.getPowReactiveMaxValue());
//                        if (timeFlag) {
//                            totalLineRes.getTime().add(pduHdaTotalHourDo.getCreateTime().toString("HH:mm"));
//                        } else {
//                            totalLineRes.getTime().add(pduHdaTotalHourDo.getCreateTime().toString("yyyy-MM-dd"));
//                        }
                    }

                } else if (dataType == 0) {
                    totalApparentPow.setName(PduDataTypeEnum.APPARENT_TOTAL_AVG.getDataType());
                    totalActivePow.setName(PduDataTypeEnum.ACTIVE_TOTAL_AVG.getDataType());
                    totalReactivePow.setName(PduDataTypeEnum.REACTIVE_TOTAL_AVG.getDataType());
                    for (PduHdaTotalHourDo pduHdaTotalHourDo : powList) {

                        totalApparentPow.getData().add(pduHdaTotalHourDo.getApparentPowAvgValue());
                        totalActivePow.getData().add(pduHdaTotalHourDo.getActivePowAvgValue());
                        totalReactivePow.getData().add(pduHdaTotalHourDo.getPowReactiveAvgValue());

                    }
                } else if (dataType == -1) {
                    totalApparentPow.setName(PduDataTypeEnum.APPARENT_TOTAL_MIN.getDataType());
                    totalActivePow.setName(PduDataTypeEnum.ACTIVE_TOTAL_MIN.getDataType());
                    totalReactivePow.setName(PduDataTypeEnum.REACTIVE_TOTAL_MIN.getDataType());
                    for (PduHdaTotalHourDo pduHdaTotalHourDo : powList) {

                        totalApparentPow.getData().add(pduHdaTotalHourDo.getApparentPowMinValue());
                        totalActivePow.getData().add(pduHdaTotalHourDo.getActivePowMinValue());
                        totalReactivePow.getData().add(pduHdaTotalHourDo.getPowReactiveMinValue());
                    }
                }
                if (dataType == 1) {
                    totalApparentPowHappenTime = powList.stream().map(PduHdaTotalHourDo -> PduHdaTotalHourDo.getApparentPowMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalActivePowHappenTime = powList.stream().map(PduHdaTotalHourDo -> PduHdaTotalHourDo.getActivePowMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalReactivePowHappenTime = powList.stream().map(PduHdaTotalHourDo -> PduHdaTotalHourDo.getPowReactiveMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                } else if (dataType == -1) {
                    totalApparentPowHappenTime = powList.stream().map(PduHdaTotalHourDo -> PduHdaTotalHourDo.getApparentPowMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalActivePowHappenTime = powList.stream().map(PduHdaTotalHourDo -> PduHdaTotalHourDo.getActivePowMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    totalReactivePowHappenTime = powList.stream().map(PduHdaTotalHourDo -> PduHdaTotalHourDo.getPowReactiveMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }
                totalApparentPow.setHappenTime(totalApparentPowHappenTime);
                totalActivePow.setHappenTime(totalActivePowHappenTime);
                totalReactivePow.setHappenTime(totalReactivePowHappenTime);
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
        if (powerData.getMaxValue() < maxValue) {
            powerData.setMaxValue(maxValue);
            powerData.setMaxTime(maxTime);
        }
        if (powerData.getMinValue() > minValue) {
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
    public Map getReportOutLetDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase cabinetChartResBase = new CabinetChartResBase();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
        if (pduIndex != null) {
            Integer pduId = pduIndex.getId();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                if (oldTime.equals(newTime)) {
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                HashMap<String, String> scriptMap = new HashMap<>();
                scriptMap.put("maxEleActive", "max_ele_active.value");
                scriptMap.put("minEleActive", "min_ele_active.value");

                HashMap<String, String> selectMap = new HashMap<>();
                selectMap.put("key", "eleValue.value");

                // 创建时间分布搜索请求
                SearchRequest pduOutLetTotalRealRequest = new SearchRequest("pdu_ele_outlet");
                SearchSourceBuilder pduOutLetTotalRealSourceBuilder = new SearchSourceBuilder();

                pduOutLetTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));
                //映射sum的sum_ele_active为sumEle

                pduOutLetTotalRealSourceBuilder.aggregation(
                        AggregationBuilders.terms("group_by_outlet").field("outlet_id").size(1000)
                                .subAggregation(AggregationBuilders.min("min_ele_active").field("ele_active"))
                                .subAggregation(AggregationBuilders.max("max_ele_active").field("ele_active"))
                                .subAggregation(PipelineAggregatorBuilders.bucketScript("eleValue", scriptMap, new Script("params.maxEleActive - params.minEleActive")))
                                .subAggregation(PipelineAggregatorBuilders.bucketSelector("eleValue_range", selectMap, new Script("params.key >= 0"))));

                pduOutLetTotalRealSourceBuilder.size(0); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduOutLetTotalRealRequest.source(pduOutLetTotalRealSourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduOutLetTotalRealRequest);

                List<Float> eleValue = new ArrayList<>();
                List<String> outLetId = new ArrayList<>();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if (pduPowTotalRealDisResponse != null) {
                        for (Terms.Bucket bucket : ((ParsedLongTerms) pduPowTotalRealDisResponse.getAggregations().get("group_by_outlet")).getBuckets()) {
                            String outlet = "输出位" + bucket.getKeyAsString();
                            ParsedSimpleValue eleValue1 = bucket.getAggregations().get("eleValue");

                            eleValue.add((float) eleValue1.value());
                            outLetId.add(outlet);
                        }
                    }

                    // 创建一个自定义的Comparator，用于eleValue的降序排序
                    Comparator<Integer> comparator = Comparator.comparingDouble(eleValue::get);

                    // 创建一个索引列表，用于保存eleValue的原始索引位置
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < eleValue.size(); i++) {
                        indexes.add(i);
                    }

                    // 使用Collections.sort()方法对indexes进行排序，并根据indexes的排序结果更新eleValue和outLetId列表
                    Collections.sort(indexes, comparator);
                    List<Float> sortedEleValue = new ArrayList<>();
                    List<String> sortedOutLetId = new ArrayList<>();
                    for (int i = 0; i < indexes.size(); i++) {
                        int index = indexes.get(i);
                        sortedEleValue.add(eleValue.get(index));
                        sortedOutLetId.add(outLetId.get(index));
                    }

                    if (sortedEleValue.size() > 12) {
                        sortedEleValue = sortedEleValue.subList(sortedEleValue.size() - 12, sortedEleValue.size());
                        sortedOutLetId = outLetId.subList(sortedOutLetId.size() - 12, sortedOutLetId.size());
                    }

                    BarSeries barSeries = new BarSeries();
                    barSeries.setLabel("{ show: true, position: 'right' }");
                    barSeries.setData(sortedEleValue);
                    cabinetChartResBase.setTime(sortedOutLetId);
                    cabinetChartResBase.getSeries().add(barSeries);


                    result.put("eleValue", sortedEleValue);
                    result.put("outLetId", sortedOutLetId);
                    result.put("barRes", cabinetChartResBase);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest pduOutLetTotalDayRequest = new SearchRequest("pdu_eq_outlet_day");
                SearchSourceBuilder pduOutLetTotalDaySourceBuilder = new SearchSourceBuilder();

                pduOutLetTotalDaySourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));

                //映射sum的sum_ele_active为sumEle
                Map<String, String> map = new HashMap<>();
                map.put("sumEq", "sum_eq");
                pduOutLetTotalDaySourceBuilder.aggregation(AggregationBuilders
                        .terms("by_outlet_id").field("outlet_id").order(BucketOrder.aggregation("sum_eq", true)).size(24)
                        .subAggregation(AggregationBuilders.sum("sum_eq").field("eq_value"))
                        //筛选sumEle > 0的
                        .subAggregation(PipelineAggregatorBuilders.bucketSelector("positive_sum_eq", map, new Script("params.sumEq >= 0"))));

                pduOutLetTotalDaySourceBuilder.size(0); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduOutLetTotalDayRequest.source(pduOutLetTotalDaySourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduOutLetTotalDayRequest);

                List<Float> eqValue = new ArrayList<>();
                List<String> outLetId = new ArrayList<>();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();


                    if (pduPowTotalRealDisResponse != null) {
                        for (Terms.Bucket bucket : ((ParsedLongTerms) pduPowTotalRealDisResponse.getAggregations().get("by_outlet_id")).getBuckets()) {
                            String outlet = "输出位" + bucket.getKeyAsString();
                            Sum sum = bucket.getAggregations().get("sum_eq");
                            double sumEleActive = sum.getValue();
                            eqValue.add((float) sumEleActive);
                            outLetId.add(outlet);
                        }
                    }

//                    if (eqValue.size() > 12) {
//                        eqValue = eqValue.subList(eqValue.size() - 12, eqValue.size());
//                        outLetId = outLetId.subList(outLetId.size() - 12, outLetId.size());
//                    }


                    BarSeries barSeries = new BarSeries();
                    barSeries.setLabel("{ show: true, position: 'right' }");
                    barSeries.setData(eqValue);
                    cabinetChartResBase.setTime(outLetId);
                    cabinetChartResBase.getSeries().add(barSeries);
                    result.put("eleValue", eqValue);
                    result.put("outLetId", outLetId);
                    result.put("barRes", cabinetChartResBase);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        } else {
            return result;
        }

    }

//    @Override
//    public Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
//        Map result = new HashMap<>();
//        CabinetChartResBase lineRes = new CabinetChartResBase();
//        CabinetChartHumResBase humeRes = new CabinetChartHumResBase();
//        try {
//            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
//            if (pduIndex != null) {
//                Integer Id = pduIndex.getId();
//                String index = null;
//                boolean isSameDay = false;
//                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
//                    index = "pdu_env_hour";
//                    if (oldTime.equals(newTime)) {
//                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
//                    }
//                    isSameDay = true;
//                } else {
//                    index = "pdu_env_day";
//                    oldTime = oldTime.plusDays(1);
//                    newTime = newTime.plusDays(1);
//                    isSameDay = false;
//                }
//                String startTime = localDateTimeToString(oldTime);
//                String endTime = localDateTimeToString(newTime);
//                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index);
//                Map<Integer, List<PduEnvHourDo>> envMap = cabinetData.stream()
//                        .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
//                        .collect(Collectors.groupingBy(PduEnvHourDo::getSensorId));
//                boolean isFisrt = false;
//                    List<String> time = null;
//                List<String> humTime = null;
//                List<String> humHappenTime = null;
//                List<String> temHappenTime = null;
//
//
//                for (int i = 1; i < 6; i++) {
//                    if (CollectionUtil.isEmpty(envMap.get(i))) {
//                        continue;
//                    }
//                    //温度湿度数据收集
//                    LineSeries lineSeries = new LineSeries();
//                    HumSeries humSeries = new HumSeries();
//
//
//                    lineSeries.setName("温度传感器" + i + "号");
//                    humSeries.setName("湿度传感器" + i + "号");
//                    List<PduEnvHourDo> hourDoList = envMap.get(i);
//                    List<Float> temAvg = hourDoList.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
//                    List<Integer> humData = hourDoList.stream().map(PduEnvHourDo::getHumAvgValue).collect(Collectors.toList());
//                    temHappenTime = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
//
//                    humHappenTime = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getHumMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
//
//                    lineSeries.setData(temAvg);
//                    humSeries.setData(humData);
//                    if (!isFisrt) {
//                        if (!isSameDay) {
//                            time = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
//                        } else {
//                            time = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
//                        }
//                        lineRes.setTime(time);
//                        lineRes.setHappenTime(temHappenTime);
//                        humeRes.setTime(time);
//                        humSeries.setHappenTime(humHappenTime);
//                        isFisrt = true;
//                    }
//
//                    lineRes.getSeries().add(lineSeries);
//                    humeRes.getSeries().add(humSeries);
//                }
//                String temMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "tem_max_value");
//                PduEnvHourDo temMax = JsonUtils.parseObject(temMaxValue, PduEnvHourDo.class);
//                String temMinValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "tem_min_value");
//                PduEnvHourDo temMin = JsonUtils.parseObject(temMinValue, PduEnvHourDo.class);
//
//                String humMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "hum_max_value");
//                PduEnvHourDo humMax = JsonUtils.parseObject(humMaxValue, PduEnvHourDo.class);
//                String humMinValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index, "hum_min_value");
//                PduEnvHourDo humMin = JsonUtils.parseObject(humMinValue, PduEnvHourDo.class);
//
//                result.put("lineRes", lineRes);
//                result.put("humRes", humeRes);
//                if (temMax != null) {
//                    result.put("temMaxValue", temMax.getTemMaxValue());
//                    result.put("temMaxTime", temMax.getTemMaxTime());
//                    result.put("temMaxSensorId", temMax.getSensorId());
//                }
//                if (temMin != null) {
//                    result.put("temMinValue", temMin.getTemMinValue());
//                    result.put("temMinTime", temMin.getTemMinTime());
//                    result.put("temMinSensorId", temMin.getSensorId());
//                }
//
//                if (humMax != null) {
//                    result.put("humMaxValue", humMax.getTemMaxValue());
//                    result.put("humMaxTime", humMax.getTemMaxTime());
//                    result.put("humMaxSensorId", humMax.getSensorId());
//                }
//                if (humMin != null) {
//                    result.put("humMinValue", humMin.getTemMinValue());
//                    result.put("humMinTime", humMin.getTemMinTime());
//                    result.put("humMinSensorId", humMin.getSensorId());
//                }
//
//                return result;
//            }
//        } catch (Exception e) {
//            log.error("获取数据失败", e);
//        }
//        return result;
//    }

    @Override
    public Map getReportLoopDataDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        CabinetChartResBase lineRes = new CabinetChartResBase();
        Map result = new HashMap<>();
        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
            if (pduIndex != null) {
                Integer Id = pduIndex.getId();
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_hda_loop_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "pdu_hda_loop_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index);
                Map<Integer, List<PduHdaLoopBaseDo>> envMap = cabinetData.stream()
                        .map(str -> JsonUtils.parseObject(str, PduHdaLoopBaseDo.class))
                        .collect(Collectors.groupingBy(PduHdaLoopBaseDo::getLoopId));
                boolean isFisrt = false;
                List<String> time = null;


                for (int i = 1; i < envMap.size() + 1; i++) {
                    if (CollectionUtil.isEmpty(envMap.get(i))) {
                        continue;
                    }
                    List<String> happenTime = new ArrayList<>();
                    List<PduHdaLoopBaseDo> hourDoList = envMap.get(i);
                    //数据收集
                    LineSeries lineSeries = new LineSeries();
                    List<Float> loopCur = null;

                    //收集描述信息（峰值、谷值、发生时间）
                    Float loopMax = 0f;
                    Float loopMin = Float.MAX_VALUE;

                    String loopMaxTime = "";
                    String loopMinTime = "";
                    //收集曲线数据（dataType（1=最大值,0=平均值,-1=最小值））
                    if (dataType == 1) {
                        String name = "回路" + i + PduDataTypeEnum.CURRENT_MAX.getDataType();
                        lineSeries.setName(name);
                        loopCur = hourDoList.stream().map(PduHdaLoopBaseDo::getCurMaxValue).collect(Collectors.toList());
                        happenTime = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                        for (PduHdaLoopBaseDo pduHdaLoopBaseDo : hourDoList) {
                            if (loopMax < pduHdaLoopBaseDo.getCurMaxValue()) {
                                loopMax = pduHdaLoopBaseDo.getCurMaxValue();
                                loopMaxTime = pduHdaLoopBaseDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                            if (loopMin > pduHdaLoopBaseDo.getCurMaxValue()) {
                                loopMin = pduHdaLoopBaseDo.getCurMaxValue();
                                loopMinTime = pduHdaLoopBaseDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                        }
                    } else if (dataType == 0) {
                        String name = "回路" + i + PduDataTypeEnum.CURRENT_AVG.getDataType();
                        lineSeries.setName(name);
                        loopCur = hourDoList.stream().map(PduHdaLoopBaseDo::getCurAvgValue).collect(Collectors.toList());
                        for (PduHdaLoopBaseDo pduHdaLoopBaseDo : hourDoList) {
                            if (loopMax < pduHdaLoopBaseDo.getCurAvgValue()) {
                                loopMax = pduHdaLoopBaseDo.getCurAvgValue();
                                loopMaxTime = "无";
                            }
                            if (loopMin > pduHdaLoopBaseDo.getCurAvgValue()) {
                                loopMin = pduHdaLoopBaseDo.getCurAvgValue();
                                loopMinTime = "无";
                            }
                        }
                    } else if (dataType == -1) {
                        String name = "回路" + i + PduDataTypeEnum.CURRENT_MIN.getDataType();
                        lineSeries.setName(name);
                        loopCur = hourDoList.stream().map(PduHdaLoopBaseDo::getCurMinValue).collect(Collectors.toList());
                        happenTime = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getCurMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                        for (PduHdaLoopBaseDo pduHdaLoopBaseDo : hourDoList) {
                            if (loopMax < pduHdaLoopBaseDo.getCurMinValue()) {
                                loopMax = pduHdaLoopBaseDo.getCurMinValue();
                                loopMaxTime = pduHdaLoopBaseDo.getCurMinTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                            if (loopMin > pduHdaLoopBaseDo.getCurMinValue()) {
                                loopMin = pduHdaLoopBaseDo.getCurMinValue();
                                loopMinTime = pduHdaLoopBaseDo.getCurMinTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                        }
                    }
                    lineSeries.setData(loopCur);
                    if (!isFisrt) {
                        if (!isSameDay) {
                            time = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                        } else {
                            time = hourDoList.stream().map(PduHdaLoopBaseDo -> PduHdaLoopBaseDo.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                        }
                        lineRes.setTime(time);
                        isFisrt = true;
                    }
                    lineSeries.setHappenTime(happenTime);
                    lineRes.getSeries().add(lineSeries);

                    result.put("loopMax" + i, loopMax);
                    result.put("loopMin" + i, loopMin);
                    result.put("loopMaxTime" + i, loopMaxTime);
                    result.put("loopMinTime" + i, loopMinTime);
                }

                result.put("lineRes", lineRes);
            }

            return result;
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    /**
     * 获取pdu报表输出位电流数据
     *
     * @param devKey
     * @param timeType
     * @param oldTime
     * @param newTime
     * @param dataType
     * @return
     */
    @Override
    public Map getReportOutLetCurDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {

        Map result = new HashMap<>();
        TreeMap dataResult = new TreeMap();

        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
            if (pduIndex != null) {
                Integer Id = pduIndex.getId();
                String index = null;
                boolean isSameDay = false;
                boolean isFisrt = false;
                List<String> time = null;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_hda_loop_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "pdu_hda_loop_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }

                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index);
                Map<Integer, List<PduHdaLoopBaseDo>> envMap = cabinetData.stream()
                        .map(str -> JsonUtils.parseObject(str, PduHdaLoopBaseDo.class))
                        .collect(Collectors.groupingBy(PduHdaLoopBaseDo::getLoopId));

                int loopSize = envMap.size();

                //查询该pdu下输出位数据
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_hda_outlet_hour";
                } else {
                    index = "pdu_hda_outlet_day";
                }

                Float curMax = -1f;
                Float curMin = 1000f;
                String curMaxTime = "";
                String curMinTime = "";

                List<String> outLetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index);
                Map<Integer, List<PduHdaOutletBaseDo>> outLetMap = outLetData.stream()
                        .map(str -> JsonUtils.parseObject(str, PduHdaOutletBaseDo.class))
                        .collect(Collectors.groupingBy(PduHdaOutletBaseDo::getOutletId));
                List<PduHdaOutletBaseDo> outletBaseDoList = outLetMap.get(1);         //用来获取y轴的时间
                int outLetSize = outLetMap.size();
                int dataIndex = 1; // 从1开始
                // 输出位和回路的关系：一个回路可以含多个输出位，即6回路 24输出位 折线图应该 展示6个，每个含4回路（24/6=4）
                for (int i = 1; i <= loopSize; i++) {
                    // 数据收集
                    CabinetChartResBase lineRes = new CabinetChartResBase();
                    List<String> happenTime = new ArrayList<>();
                    for (int j = dataIndex; j < dataIndex + outLetSize / loopSize; j++) {
                        LineSeries lineSeries = new LineSeries();
                        List<PduHdaOutletBaseDo> pduHdaOutletBaseDos = outLetMap.get(j);
                        if (pduHdaOutletBaseDos != null && !pduHdaOutletBaseDos.isEmpty()) {
                            if (dataType == 1) {
                                String name = "输出位" + j + PduDataTypeEnum.CURRENT_MAX.getDataType();
                                lineSeries.setName(name);
                                lineSeries.setProjectName("输出位" + j);
                                List<Float> outLetCur = pduHdaOutletBaseDos.stream().map(PduHdaOutletBaseDo::getCurMaxValue).collect(Collectors.toList());
                                lineSeries.setData(outLetCur);
                                happenTime = pduHdaOutletBaseDos.stream().map(PduHdaOutletBaseDo -> PduHdaOutletBaseDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                                //获取极值描述信息（峰值、谷值）
                                for (PduHdaOutletBaseDo pduHdaOutletBaseDo : pduHdaOutletBaseDos) {
                                    if (curMax < pduHdaOutletBaseDo.getCurMaxValue()) {
                                        curMax = pduHdaOutletBaseDo.getCurMaxValue();
                                        curMaxTime = pduHdaOutletBaseDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                                    }
                                    if (curMin > pduHdaOutletBaseDo.getCurMaxValue()) {
                                        curMin = pduHdaOutletBaseDo.getCurMaxValue();
                                        curMinTime = pduHdaOutletBaseDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                                    }
                                }
                                lineSeries.setMaxValue(curMax);
                                lineSeries.setMaxTime(curMaxTime);
                                lineSeries.setMinValue(curMin);
                                lineSeries.setMinTime(curMinTime);

                            } else if (dataType == 0) {
                                String name = "输出位" + j + PduDataTypeEnum.CURRENT_AVG.getDataType();
                                lineSeries.setName(name);
                                lineSeries.setProjectName("输出位" + j);
                                List<Float> outLetCur = pduHdaOutletBaseDos.stream().map(PduHdaOutletBaseDo::getCurAvgValue).collect(Collectors.toList());
                                lineSeries.setData(outLetCur);
                                //获取极值描述信息（峰值、谷值）
                                for (PduHdaOutletBaseDo pduHdaOutletBaseDo : pduHdaOutletBaseDos) {
                                    if (curMax < pduHdaOutletBaseDo.getCurAvgValue()) {
                                        curMax = pduHdaOutletBaseDo.getCurAvgValue();
                                        curMaxTime = "无";
                                    }
                                    if (curMin > pduHdaOutletBaseDo.getCurAvgValue()) {
                                        curMin = pduHdaOutletBaseDo.getCurAvgValue();
                                        curMinTime = "无";
                                    }
                                }
                                lineSeries.setMaxValue(curMax);
                                lineSeries.setMaxTime(curMaxTime);
                                lineSeries.setMinValue(curMin);
                                lineSeries.setMinTime(curMinTime);
                            } else if (dataType == -1) {
                                String name = "输出位" + j + PduDataTypeEnum.CURRENT_MIN.getDataType();
                                lineSeries.setName(name);
                                lineSeries.setProjectName("输出位" + j);
                                List<Float> outLetCur = pduHdaOutletBaseDos.stream().map(PduHdaOutletBaseDo::getCurMinValue).collect(Collectors.toList());
                                lineSeries.setData(outLetCur);
                                happenTime = pduHdaOutletBaseDos.stream().map(PduHdaOutletBaseDo -> PduHdaOutletBaseDo.getCurMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                                //获取极值描述信息（峰值、谷值）
                                for (PduHdaOutletBaseDo pduHdaOutletBaseDo : pduHdaOutletBaseDos) {
                                    if (curMax < pduHdaOutletBaseDo.getCurMinValue()) {
                                        curMax = pduHdaOutletBaseDo.getCurMinValue();
                                        curMaxTime = pduHdaOutletBaseDo.getCurMinTime().toString("yyyy-MM-dd HH:mm:ss");
                                    }
                                    if (curMin > pduHdaOutletBaseDo.getCurMinValue()) {
                                        curMin = pduHdaOutletBaseDo.getCurMinValue();
                                        curMinTime = pduHdaOutletBaseDo.getCurMinTime().toString("yyyy-MM-dd HH:mm:ss");
                                    }
                                }
                                lineSeries.setMaxValue(curMax);
                                lineSeries.setMaxTime(curMaxTime);
                                lineSeries.setMinValue(curMin);
                                lineSeries.setMinTime(curMinTime);
                            }
                            lineSeries.setHappenTime(happenTime);
                            lineRes.getSeries().add(lineSeries);
                        }
//                        dataResult.put("curMax"+j,curMax);
//                        dataResult.put("curMaxTime"+j,curMaxTime);
//                        dataResult.put("curMin"+j,curMin);
//                        dataResult.put("curMinTime"+j,curMinTime);

                    }
                    if (!isFisrt) {
                        if (!isSameDay) {
                            time = outletBaseDoList.stream().map(PduHdaOutletBaseDo -> PduHdaOutletBaseDo.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                        } else {
                            time = outletBaseDoList.stream().map(PduHdaOutletBaseDo -> PduHdaOutletBaseDo.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                        }

                        isFisrt = true;
                    }
                    lineRes.setTime(time);
                    dataResult.put("dataRes" + i, lineRes);

                    dataIndex += outLetSize / loopSize; // 更新dataIndex （每个回路下要有dataIndex个输出位）
                }

                result.put("outRes", dataResult);
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }


        return result;
    }

    @Override
    public Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        CabinetChartResBase lineRes = new CabinetChartResBase();
        CabinetChartHumResBase humeRes = new CabinetChartHumResBase();
        try {
            PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getPduKey, devKey));
            if (pduIndex != null) {
                Integer Id = pduIndex.getId();
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "pdu_env_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "pdu_env_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(Id.intValue())), index);
                Map<Integer, List<PduEnvHourDo>> envMap = cabinetData.stream()
                        .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                        .collect(Collectors.groupingBy(PduEnvHourDo::getSensorId));
                boolean isFisrt = false;
                List<String> time = null;

                List<String> humHappenTime = null;
                List<String> temHappenTime = null;


                for (int i = 1; i < 6; i++) {
                    if (CollectionUtil.isEmpty(envMap.get(i))) {
                        continue;
                    }
                    //温度湿度数据收集
                    LineSeries lineSeries = new LineSeries();
                    HumSeries humSeries = new HumSeries();


                    lineSeries.setName("温度传感器" + i + "号");
                    humSeries.setName("温度传感器" + i + "号");
                    List<PduEnvHourDo> hourDoList = envMap.get(i);

                    //判断需要什么数值的数据（最大值，最小值，平均值）
                    List<Float> temAvg = null;
                    List<Integer> humData = null;

                    Float temMax = -274f;
                    Float temMin = 1000f;
                    String temMaxTime = "";
                    String temMinTime = "";

                    Integer humMax = -100;
                    Integer hunMin = 1000;
                    String humMaxTime = "";
                    String humMinTime = "";


                    if (dataType == 1) {
                        temAvg = hourDoList.stream().map(PduEnvHourDo::getTemMaxValue).collect(Collectors.toList());
                        humData = hourDoList.stream().map(PduEnvHourDo::getHumMaxValue).collect(Collectors.toList());
                        for (PduEnvHourDo pduEnvHourDo : hourDoList) {
                            if (temMax < pduEnvHourDo.getTemMaxValue()) {
                                temMax = pduEnvHourDo.getTemMaxValue();
                                temMaxTime = pduEnvHourDo.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                            if (temMin > pduEnvHourDo.getTemMaxValue()) {
                                temMin = pduEnvHourDo.getTemMaxValue();
                                temMinTime = pduEnvHourDo.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                            }

                            if (humMax < pduEnvHourDo.getHumMaxValue()) {
                                humMax = pduEnvHourDo.getHumMaxValue();
                                humMaxTime = pduEnvHourDo.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                            if (hunMin > pduEnvHourDo.getHumMaxValue()) {
                                hunMin = pduEnvHourDo.getHumMaxValue();
                                humMinTime = pduEnvHourDo.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                        }

                        temHappenTime = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                        humHappenTime = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getHumMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    } else if (dataType == 0) {
                        temAvg = hourDoList.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                        humData = hourDoList.stream().map(PduEnvHourDo::getHumAvgValue).collect(Collectors.toList());
                        for (PduEnvHourDo pduEnvHourDo : hourDoList) {
                            if (temMax < pduEnvHourDo.getTemAvgValue()) {
                                temMax = pduEnvHourDo.getTemAvgValue();
                                temMaxTime = "无";
                            }
                            if (temMin > pduEnvHourDo.getTemAvgValue()) {
                                temMin = pduEnvHourDo.getTemAvgValue();
                                temMinTime = "无";
                            }

                            if (humMax < pduEnvHourDo.getHumAvgValue()) {
                                humMax = pduEnvHourDo.getHumAvgValue();
                                humMaxTime = "无";
                            }
                            if (hunMin > pduEnvHourDo.getHumAvgValue()) {
                                hunMin = pduEnvHourDo.getHumAvgValue();
                                humMinTime = "无";
                            }
                        }

                    } else if (dataType == -1) {
                        temAvg = hourDoList.stream().map(PduEnvHourDo::getTemMinValue).collect(Collectors.toList());
                        humData = hourDoList.stream().map(PduEnvHourDo::getHumMinValue).collect(Collectors.toList());
                        for (PduEnvHourDo pduEnvHourDo : hourDoList) {
                            if (temMax < pduEnvHourDo.getTemMinValue()) {
                                temMax = pduEnvHourDo.getTemMinValue();
                                temMaxTime = pduEnvHourDo.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                            if (temMin > pduEnvHourDo.getTemMinValue()) {
                                temMin = pduEnvHourDo.getTemMinValue();
                                temMinTime = pduEnvHourDo.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss");
                            }

                            if (humMax < pduEnvHourDo.getHumMinValue()) {
                                humMax = pduEnvHourDo.getHumMinValue();
                                humMaxTime = pduEnvHourDo.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                            if (hunMin > pduEnvHourDo.getHumMinValue()) {
                                hunMin = pduEnvHourDo.getHumMinValue();
                                humMinTime = pduEnvHourDo.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss");
                            }
                        }
                        temHappenTime = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                        humHappenTime = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getHumMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    }

                    lineSeries.setData(temAvg);
                    humSeries.setData(humData);
                    if (!isFisrt) {
                        if (!isSameDay) {
                            time = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                        } else {
                            time = hourDoList.stream().map(pduEnvHourDo -> pduEnvHourDo.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                        }
                        lineRes.setTime(time);
                        humeRes.setTime(time);
                        isFisrt = true;
                    }
                    humSeries.setHappenTime(humHappenTime);
                    lineSeries.setHappenTime(temHappenTime);
                    lineRes.getSeries().add(lineSeries);
                    humeRes.getSeries().add(humSeries);

                    result.put("temMax" + i, temMax);
                    result.put("temMin" + i, temMin);
                    result.put("temMaxTime" + i, temMaxTime);
                    result.put("temMinTime" + i, temMinTime);

                    result.put("humMax" + i, humMax);
                    result.put("humMin" + i, hunMin);
                    result.put("humMaxTime" + i, humMaxTime);
                    result.put("humMinTime" + i, humMinTime);
                }

                result.put("lineRes", lineRes);
                result.put("humRes", humeRes);
                return result;
            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }


    private List<String> getPDULineData(String startTime, String endTime, List<Integer> ids, String index, String sort) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))));
        builder.sort(sort, SortOrder.DESC);
        // 设置搜索条件
        searchRequest.source(builder);


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

    private Map<Integer, Map<Integer, MaxValueAndCreateTime>> getPDULineCurMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))));

        builder.aggregation(
                AggregationBuilders.terms("group_by_pdu_id")
                        .field("pdu_id")
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

        List<String> list = new ArrayList<>();
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 初始化结果Map
        Map<Integer, Map<Integer, MaxValueAndCreateTime>> resultMap = new HashMap<>();
        // 获取group_by_box_id聚合结果
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_pdu_id");
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

    private Map<Integer, Map<Integer, MaxValueAndCreateTime>> getPDULinePowMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))));

        builder.aggregation(
                AggregationBuilders.terms("group_by_pdu_id")
                        .field("pdu_id")
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

        List<String> list = new ArrayList<>();
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);


        // 初始化结果Map
        Map<Integer, Map<Integer, MaxValueAndCreateTime>> resultMap = new HashMap<>();
        // 获取group_by_box_id聚合结果
        Terms groupByBoxId = searchResponse.getAggregations().get("group_by_pdu_id");
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


    private List<String> getData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termsQuery("pdu_id", ids))));
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
            log.error("查询es数据失败" + e);
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
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termsQuery("pdu_id", ids))));
            builder.sort(CREATE_TIME + ".keyword", SortOrder.DESC);
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
            log.error("查询es数据失败" + e);
        }
        return null;
    }


    private SearchResponse getData(String startTime, String endTime, Integer id, String index) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();

            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termQuery("pdu_id", id))));
            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
            // 设置搜索条件
            searchRequest.source(builder);
            builder.size(10000);
            // 执行ES请求
            List<String> list = new ArrayList<>();
            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            return searchResponse;
        } catch (Exception e) {
            log.error("查询es数据失败" + e);
        }
        return null;
    }

    private List<String> getData(String startTime, String endTime, Integer id, String[] heads, String index) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.fetchSource(heads, null);
            //获取需要处理的数据
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                    .must(QueryBuilders.termQuery("pdu_id", id))));
            builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
            // 设置搜索条件
            searchRequest.source(builder);
            builder.size(10000);
            // 执行ES请求
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
            log.error("查询es数据失败" + e);
        }
        return null;
    }


    public MaxCurAndOtherData getMaxCurMaxValue(String startTime, String endTime, String index, int flaValue) throws IOException {
        // 创建搜索请求
        SearchRequest searchRequest = new SearchRequest(index);

        // 构建搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").from(startTime).to(endTime))
        );
        String[] includeFields;
        if (flaValue == 0) {
            // 排序字段
            searchSourceBuilder.sort("cur_max_value", SortOrder.DESC);
            // 限制字段，只取需要的字段
            includeFields = new String[]{"cur_max_value", "cur_max_time", "line_id", "pdu_id"};
        } else {
            // 排序字段
            searchSourceBuilder.sort("pow_active_max_value", SortOrder.DESC);
            // 限制字段，只取需要的字段
            includeFields = new String[]{"pow_active_max_value", "pow_active_max_time", "line_id", "pdu_id"};
        }

        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includeFields, null);
        searchSourceBuilder.fetchSource(fetchSourceContext);

        // 设置请求的源
        searchRequest.source(searchSourceBuilder);

        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 解析响应
        SearchHits hits = searchResponse.getHits();
        if (hits.getHits().length > 0) {
            var hit = hits.getAt(0); // 由于已按 cur_max_value 降序排序，取第一个即为最大值
            var sourceAsMap = hit.getSourceAsMap();
            // 构建结果对象
            MaxCurAndOtherData result = new MaxCurAndOtherData();
            if (flaValue == 0) {
                result.setMaxValue((Double) sourceAsMap.get("cur_max_value"));
                result.setMaxTime(new DateTime(sourceAsMap.get("cur_max_time").toString(), "yyyy-MM-dd HH:mm:ss"));
                result.setLine_id((Integer) sourceAsMap.get("line_id"));
                result.setPdu_id((Integer) sourceAsMap.get("pdu_id"));
            } else {
                // 构建结果对象
                result.setMaxValue((Double) sourceAsMap.get("pow_active_max_value"));
                result.setMaxTime(new DateTime(sourceAsMap.get("pow_active_max_time").toString(), "yyyy-MM-dd HH:mm:ss"));
                result.setLine_id((Integer) sourceAsMap.get("line_id"));
                result.setPdu_id((Integer) sourceAsMap.get("pdu_id"));
            }
            return result;
        } else {
            throw new RuntimeException("No data found for the specified time range.");
        }
    }

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index, String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))));
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

    private String getMaxDataNew(String startTime, String endTime, List<Integer> ids, Integer businessIds, String index, String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids)).must(QueryBuilders.termQuery("line_id", businessIds))));
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

    private String getPDUMaxData(String startTime, String endTime, List<Integer> ids, List<Integer> sensorIds, String index, String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))
                .must(QueryBuilders.termsQuery("sensor_id", sensorIds))));
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
                .must(QueryBuilders.termsQuery("pdu_id", ids))));
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

    private String getPDUMinData(String startTime, String endTime, List<Integer> ids, List<Integer> sensorIds, String index, String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))
                .must(QueryBuilders.termsQuery("sensor_id", sensorIds))));
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

    private String localDateTimeToString(LocalDateTime time) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
    }

    private void setLocation(List<PduIndex> pduIndices, List<? extends PDUDeviceDO> result) {
        if (CollectionUtils.isEmpty(result) || CollectionUtils.isEmpty(pduIndices)) {
            return;
        }

        List<String> pduKey = pduIndices.stream().map(PduIndex::getPduKey).collect(Collectors.toList());
        List<CabinetPduResVO> cabinetPdus = cabinetIndexMapper.selectCabinetPduList(pduKey);

        // 将查询结果按 ipAddr 和 cascadeAddr 分组
        Map<String, List<CabinetPduResVO>> cabinetPduAMap = cabinetPdus.stream()
                .filter(vo -> pduKey.contains(vo.getPduKeyA()))
                .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduKeyA()));


        Map<String, List<CabinetPduResVO>> cabinetPduBMap = cabinetPdus.stream()
                .filter(vo -> pduKey.contains(vo.getPduKeyB()))
                .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduKeyB()));

        List<Integer> roomIds = cabinetPdus.stream()
                .map(CabinetPduResVO::getRoomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (roomIds.isEmpty()) {
            roomIds.add(0);
        }
        List<RoomIndex> roomIndices = roomIndexMapper.selectBatchIds(roomIds);

        Map<Integer, String> roomMap = roomIndices.stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getRoomName));
        List<Integer> cabIds = cabinetPdus.stream().filter(dto -> dto.getAisleId() != 0).map(CabinetPduResVO::getAisleId).collect(Collectors.toList());
        Map<Integer, String> aisleMap;
        if (!CollectionUtils.isEmpty(cabIds)) {
            List<AisleIndex> aisleIndexList = aisleIndexMapper.selectBatchIds(cabIds);
            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleMap = aisleIndexList.stream().collect(Collectors.toMap(AisleIndex::getId, AisleIndex::getAisleName));
            } else {
                aisleMap = new HashMap<>();
            }
        } else {
            aisleMap = new HashMap<>();
        }

        result.forEach(pduIndex -> {
            String localtion = null;
            List<CabinetPduResVO> cabinetPduAList = cabinetPduAMap.get(pduIndex.getDevKey());
            List<CabinetPduResVO> cabinetPduBList = cabinetPduBMap.get(pduIndex.getDevKey());

            if (cabinetPduAList != null && !cabinetPduAList.isEmpty()) {
                CabinetPduResVO cabinetIndex = cabinetPduAList.get(0); // 假设结果唯一
                if (Objects.nonNull(cabinetIndex)) {
                    if (cabinetIndex.getAisleId() != 0) {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + aisleMap.get(cabinetIndex.getAisleId()) + "-" + cabinetIndex.getCabinetName() + "-" + "A路";
                    } else {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + cabinetIndex.getCabinetName() + "-" + "A路";
                    }
                }
            }

            if (cabinetPduBList != null && !cabinetPduBList.isEmpty()) {
                CabinetPduResVO cabinetIndex = cabinetPduBList.get(0); // 假设结果唯一
                if (Objects.nonNull(cabinetIndex)) {
                    if (cabinetIndex.getAisleId() != 0) {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + aisleMap.get(cabinetIndex.getAisleId()) + "-" + cabinetIndex.getCabinetName() + "-" + "B路";
                    } else {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + cabinetIndex.getCabinetName() + "-" + "B路";
                    }
                }
            }
            if (StringUtils.isEmpty(localtion)) {
//                pduIndex.setLocation(pduIndex.getDevKey());
                pduIndex.setLocation("未绑定");
            } else {
                pduIndex.setLocation(localtion);
            }
        });
    }


    @Override
    public Map<String, String> setLocation(List<String> pduKeys) {

        Map<String, String> locationMap = new HashMap<>();
        if (CollectionUtils.isEmpty(pduKeys)) {
            return locationMap;
        }
        List<CabinetPduResVO> cabinetPdus = cabinetIndexMapper.selectCabinetPduList(pduKeys);

        // 将查询结果按 ipAddr 和 cascadeAddr 分组
        Map<String, List<CabinetPduResVO>> cabinetPduAMap = cabinetPdus.stream()
                .filter(vo -> pduKeys.contains(vo.getPduKeyA()))
                .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduKeyA()));

        Map<String, List<CabinetPduResVO>> cabinetPduBMap = cabinetPdus.stream()
                .filter(vo -> pduKeys.contains(vo.getPduKeyB()))
                .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduKeyB()));

        List<Integer> roomIds = cabinetPdus.stream()
                .map(CabinetPduResVO::getRoomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (roomIds.isEmpty()) {
            roomIds.add(0);
        }
        List<RoomIndex> roomIndices = roomIndexMapper.selectList(new LambdaQueryWrapper<RoomIndex>().in(RoomIndex::getId, roomIds).eq(RoomIndex::getIsDelete, 0));
        if (CollectionUtils.isEmpty(roomIndices)) {
            return locationMap;
        }
        Map<Integer, String> roomMap = roomIndices.stream().collect(Collectors.toMap(RoomIndex::getId, RoomIndex::getRoomName));
        List<Integer> cabIds = cabinetPdus.stream().filter(dto -> dto.getAisleId() != 0).map(CabinetPduResVO::getAisleId).collect(Collectors.toList());
        Map<Integer, String> aisleMap;
        if (!CollectionUtils.isEmpty(cabIds)) {
            List<AisleIndex> aisleIndexList = aisleIndexMapper.selectList(new LambdaQueryWrapper<AisleIndex>().in(AisleIndex::getId, cabIds).eq(AisleIndex::getIsDelete, 0));
            if (!CollectionUtils.isEmpty(aisleIndexList)) {
                aisleMap = aisleIndexList.stream().collect(Collectors.toMap(AisleIndex::getId, AisleIndex::getAisleName));
            } else {
                aisleMap = new HashMap<>();
            }
        } else {
            aisleMap = new HashMap<>();
        }

        pduKeys.forEach(pduIndex -> {
            String localtion = null;
            List<CabinetPduResVO> cabinetPduAList = cabinetPduAMap.get(pduIndex);
            List<CabinetPduResVO> cabinetPduBList = cabinetPduBMap.get(pduIndex);

            if (cabinetPduAList != null && !cabinetPduAList.isEmpty()) {
                CabinetPduResVO cabinetIndex = cabinetPduAList.get(0); // 假设结果唯一
                if (Objects.nonNull(cabinetIndex)) {
                    if (cabinetIndex.getAisleId() != 0) {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + aisleMap.get(cabinetIndex.getAisleId()) + "-" + cabinetIndex.getCabinetName() + "-" + "A路";
                    } else {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + cabinetIndex.getCabinetName() + "-" + "A路";
                    }
                }
            }

            if (cabinetPduBList != null && !cabinetPduBList.isEmpty()) {
                CabinetPduResVO cabinetIndex = cabinetPduBList.get(0); // 假设结果唯一
                if (Objects.nonNull(cabinetIndex)) {
                    if (cabinetIndex.getAisleId() != 0) {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + aisleMap.get(cabinetIndex.getAisleId()) + "-" + cabinetIndex.getCabinetName() + "-" + "B路";
                    } else {
                        localtion = roomMap.get(cabinetIndex.getRoomId()) + "-" + cabinetIndex.getCabinetName() + "-" + "B路";
                    }
                }
            }
            if (StringUtils.isEmpty(localtion)) {
                locationMap.put(pduIndex, "未绑定");
            } else {
                locationMap.put(pduIndex, localtion);
            }
        });
        return locationMap;
    }


    private List getMutiRedis(List<PduIndex> list) {
        List<String> devKeys = list.stream().map(pduIndex -> REDIS_KEY_PDU + pduIndex.getPduKey()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }

    private Map getESTotalPduId(String index, String startTime, String endTime) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder
                .query(QueryBuilders.rangeQuery("create_time.keyword").gte(startTime).lte(endTime))
                .sort("pdu_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("pdu_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("pdu_id").precisionThreshold(10000));
        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                PduHdaLineHourDo hourDo = JsonUtils.parseObject(str, PduHdaLineHourDo.class);
                sortValues.add(hourDo.getPduId());
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

    private Map getESTotalPduId(String index, String startTime, String endTime, Integer pageSize, Integer pageNo) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder
                .from(pageNo * pageSize)
                .size(pageSize)
                .query(QueryBuilders.rangeQuery("create_time.keyword").gte(startTime).lte(endTime))
                .sort("pdu_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("pdu_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("pdu_id").precisionThreshold(10000));
        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                PduHdaLineHourDo hourDo = JsonUtils.parseObject(str, PduHdaLineHourDo.class);
                sortValues.add(hourDo.getPduId());
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

    private Map getESTotalPduId(String index, String startTime, String endTime, Integer pageSize, Integer pageNo, List<Integer> ids) throws IOException {
        HashMap<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);

        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder
                .from(pageNo * pageSize)
                .size(pageSize)
                .query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                        .must(QueryBuilders.termsQuery("pdu_id", ids))))
                .sort("pdu_id", SortOrder.ASC)
                .collapse(new CollapseBuilder("pdu_id"))
                .aggregation(AggregationBuilders.cardinality("total_size").field("pdu_id").precisionThreshold(10000));
        searchRequest.source(builder);
        List<Integer> sortValues = new ArrayList<>();
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if (searchResponse != null) {
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                PduHdaLineHourDo hourDo = JsonUtils.parseObject(str, PduHdaLineHourDo.class);
                sortValues.add(hourDo.getPduId());
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

    /**
     * 收集相电压电流数据
     *
     * @param houResVO
     * @param resultMap
     * @param isSameDay
     * @param dataType
     */
    private void processLineHisData(PduHdaLineHouResVO houResVO, Map<String, Object> resultMap, boolean isSameDay, DataType dataType) {
        int lineId = houResVO.getLineId();
        String lineKey = "dayList" + lineId;

        Map<String, Object> lineData = (Map<String, Object>) resultMap.computeIfAbsent(lineKey, k -> new HashMap<>());
        ((List<PduHdaLineHouResVO>) lineData.computeIfAbsent("data", k -> new ArrayList<>())).add(houResVO);
        ((List<Float>) lineData.computeIfAbsent("curDataList", k -> new ArrayList<>())).add(getCurValue(houResVO, dataType));
        ((List<Float>) lineData.computeIfAbsent("volDataList", k -> new ArrayList<>())).add(getVolValue(houResVO, dataType));
        ((List<String>) lineData.computeIfAbsent("curHappenTime", k -> new ArrayList<>())).add(formatCurTime(houResVO, dataType));
        ((List<String>) lineData.computeIfAbsent("volHappenTime", k -> new ArrayList<>())).add(formatVolTime(houResVO, dataType));

        List<String> dateTimes = (List<String>) resultMap.computeIfAbsent("dateTimes", k -> new ArrayList<>());
        dateTimes.add(isSameDay ? sdf.format(houResVO.getCreateTime()) : dateOnlyFormat.format(houResVO.getCreateTime()));
    }

    /**
     * 处理相电流值
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private Float getCurValue(PduHdaLineHouResVO houResVO, DataType dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getCurMaxValue().floatValue();
            case AVG:
                return houResVO.getCurValue().floatValue();
            case MIN:
                return houResVO.getCurMinValue().floatValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理相电压值
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private Float getVolValue(PduHdaLineHouResVO houResVO, DataType dataType) {
        switch (dataType) {
            case MAX:
                return houResVO.getVolMaxValue().floatValue();
            case AVG:
                return houResVO.getVolValue().floatValue();
            case MIN:
                return houResVO.getVolMinValue().floatValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理相电流发生时间
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private String formatCurTime(PduHdaLineHouResVO houResVO, DataType dataType) {
        switch (dataType) {
            case MAX:
                return sdf.format(houResVO.getCurMaxTime());
            case AVG:
                return "无";
            case MIN:
                return sdf.format(houResVO.getCurMinTime());
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 处理相电压发生时间
     *
     * @param houResVO
     * @param dataType
     * @return
     */
    private String formatVolTime(PduHdaLineHouResVO houResVO, DataType dataType) {
        switch (dataType) {
            case MAX:
                return sdf.format(houResVO.getVolMaxTime());
            case AVG:
                return "无";
            case MIN:
                return sdf.format(houResVO.getVolMinTime());
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    /**
     * 获取曲线名称
     *
     * @param type
     * @param lineId
     * @param dataType
     * @return
     */
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


    /**
     * 处理功率因素数据
     *
     * @param powList
     * @param dataType
     * @param timeType
     * @param totalLineRes
     * @param totalPFLine
     * @param result
     * @param oldTime
     * @param newTime
     */
    public void processPFMavMin(List<PduHdaTotalHourDo> powList, Integer dataType, Integer timeType, CabinetChartResBase totalLineRes
            , LineSeries totalPFLine, Map<String, Object> result, LocalDateTime oldTime, LocalDateTime newTime) {
        Float totalMax = 0f;
        Float totalMin = Float.MAX_VALUE;
        String totalMaxTime = "";
        String totalMinTime = "";

        for (PduHdaTotalHourDo pduHdaTotalHourDo : powList) {
            Float value = getPFValue(pduHdaTotalHourDo, dataType);
            String time = getPFTime(pduHdaTotalHourDo, dataType);

            totalPFLine.setName(getPduDataType(dataType));
            totalPFLine.getData().add(value);

            if (totalMax < value) {
                totalMax = value;
                totalMaxTime = time;
            }
            if (totalMin > value) {
                totalMin = value;
                totalMinTime = time;
            }

            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                totalLineRes.getTime().add(pduHdaTotalHourDo.getCreateTime().toString("HH:mm"));
            } else {
                totalLineRes.getTime().add(pduHdaTotalHourDo.getCreateTime().toString("yyyy-MM-dd"));
            }
        }

        result.put("lineName" + 1, "总功率因素");
        result.put("lineMax" + 1, totalMax);
        result.put("lineMin" + 1, totalMin);
        result.put("lineMaxTime" + 1, totalMaxTime);
        result.put("lineMinTime" + 1, totalMinTime);
    }

    private Float getPFValue(PduHdaTotalHourDo pduHdaTotalHourDo, Integer dataType) {
        switch (dataType) {
            case 1:
                return pduHdaTotalHourDo.getPowerFactorMaxValue();
            case 0:
                return pduHdaTotalHourDo.getPowerFactorAvgValue();
            case -1:
                return pduHdaTotalHourDo.getPowerFactorMinValue();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    private String getPFTime(PduHdaTotalHourDo pduHdaTotalHourDo, Integer dataType) {
        switch (dataType) {
            case 1:
                return pduHdaTotalHourDo.getPowerFactorMaxTime().toString("yyyy-MM-dd HH:mm:ss");
            case 0:
                return "无";
            case -1:
                return pduHdaTotalHourDo.getPowerFactorMinTime().toString("yyyy-MM-dd HH:mm:ss");
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

    private String getPduDataType(Integer dataType) {
        switch (dataType) {
            case 1:
                return PduDataTypeEnum.FACTOR_TOTAL_MAX.getDataType();
            case 0:
                return PduDataTypeEnum.FACTOR_TOTAL_AVG.getDataType();
            case -1:
                return PduDataTypeEnum.FACTOR_TOTAL_MIN.getDataType();
            default:
                throw new IllegalArgumentException("Invalid data type: " + dataType);
        }
    }

}