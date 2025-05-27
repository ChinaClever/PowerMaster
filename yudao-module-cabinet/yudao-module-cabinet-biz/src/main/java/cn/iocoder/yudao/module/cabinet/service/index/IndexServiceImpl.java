package cn.iocoder.yudao.module.cabinet.service.index;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetEnvSensor;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.enums.DataTypeEnums;
import cn.iocoder.yudao.framework.common.mapper.*;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.PduIndex;
import cn.iocoder.yudao.module.cabinet.dal.mysql.temcolor.TemColorMapper;
import cn.iocoder.yudao.module.cabinet.mapper.*;
import cn.iocoder.yudao.module.cabinet.service.temcolor.TemColorService;
import cn.iocoder.yudao.module.cabinet.utils.DataProcessingUtils;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.cabinet.dal.mysql.index.CabIndexMapper;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.REDIS_KEY_PDU;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.cabinet.enums.ErrorCodeConstants.*;

/**
 * 机柜索引 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class IndexServiceImpl implements IndexService {

    @Resource
    private CabIndexMapper cabIndexMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private AisleIndexMapper aisleIndexMapper;

    @Autowired
    private CabinetPduMapper cabinetPduMapper;

    @Autowired
    private NewPDUIndexMapper pduIndexMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CabinetEnvSensorMapper cabinetEnvSensorMapper;

    @Resource
    private TemColorMapper temColorMapper;

    @Autowired
    private TemColorService temColorService;

    @Autowired
    private RestHighLevelClient client;

    @Resource
    private RackIndexDoMapper rackIndexDoMapper;

    public static final String FACTOR_FORMAT = "功率因素";

    @Override
    public Integer createIndex(IndexSaveReqVO createReqVO) {
        // 插入
        IndexDO index = BeanUtils.toBean(createReqVO, IndexDO.class);
        cabIndexMapper.insert(index);
        // 返回
        return index.getId();
    }

    @Override
    public void updateIndex(IndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        IndexDO updateObj = BeanUtils.toBean(updateReqVO, IndexDO.class);
        cabIndexMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Integer id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        cabIndexMapper.deleteById(id);
    }

    private void validateIndexExists(Integer id) {
        if (cabIndexMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public IndexDO getIndex(Integer id) {
        return cabIndexMapper.selectById(id);
    }

    @Override
    public PageResult<IndexDO> getIndexPage(IndexPageReqVO pageReqVO) {
        return cabIndexMapper.selectPage(pageReqVO);
    }


    @Override
    public Map getReportConsumeDataById(String Id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase barRes = new CabinetChartResBase();
        BarSeries barSeries = new BarSeries();
        //获取与pdu的绑定关系
        CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, Integer.valueOf(Id)));
        if (cabinetPdu != null) {
            String pduKeyA = cabinetPdu.getPduKeyA();
            String pduKeyB = cabinetPdu.getPduKeyB();
            result.put("pduKeyA", pduKeyA);
            result.put("pduKeyB", pduKeyB);
        }
        try {
            if (Id != null) {
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "cabinet_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "cabinet_eq_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getCabinetData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index);
                if(CollectionUtil.isEmpty(cabinetData)){
                    return result;
                }
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay) {
                    List<CabinetEleTotalRealtimeDo> busList = new ArrayList<>();
                    for (String str : cabinetData) {
                        nowTimes++;
                        CabinetEleTotalRealtimeDo cabinetEleTotalRealtimeDo = JsonUtils.parseObject(str, CabinetEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = cabinetEleTotalRealtimeDo.getEleTotal();
                        }
                        if (nowTimes > 1) {
                            barSeries.getData().add((float) (cabinetEleTotalRealtimeDo.getEleTotal() - lastEq));
                            barRes.getTime().add(cabinetEleTotalRealtimeDo.getCreateTime().split(" ")[1]);
                        }
                        lastEq = cabinetEleTotalRealtimeDo.getEleTotal();
                        busList.add(cabinetEleTotalRealtimeDo);
                    }

                    //计算实时用电量
                    List<CabinetEleTotalRealtimeDo> dayEqList = new ArrayList<>();
                    for (int i = 0; i < cabinetData.size() - 1; i++) {
                        CabinetEleTotalRealtimeDo dayEleDo = new CabinetEleTotalRealtimeDo();
                        totalEq += (float) busList.get(i + 1).getEleTotal() - (float) busList.get(i).getEleTotal();
                        dayEleDo.setEleTotal(busList.get(i + 1).getEleTotal() - busList.get(i).getEleTotal());
                        dayEleDo.setCreateTime(busList.get(i+1).getCreateTime());
                        dayEqList.add(dayEleDo);
                    }
                    dayEqList.sort(Comparator.comparing(CabinetEleTotalRealtimeDo::getEleTotal));
                    maxEle = dayEqList.get(dayEqList.size() - 1).getEleTotal();
                    maxEleTime = dayEqList.get(dayEqList.size() - 1).getCreateTime();

//
//                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "ele_total");
//                    CabinetEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, CabinetEleTotalRealtimeDo.class);
//                    if (eleMaxValue != null) {
//                        maxEle = eleMaxValue.getEleTotal();
//                        maxEleTime = eleMaxValue.getCreateTime();
//                    }
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
                        CabinetEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, CabinetEqTotalDayDo.class);
                        totalEq += totalDayDo.getEqValue();
                        barSeries.getData().add((float) totalDayDo.getEqValue());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                        if (dataIndex == 0) {
                            firstEq = totalDayDo.getStartEle();
                        }
                        if (dataIndex == cabinetData.size() - 1) {
                            lastEq = totalDayDo.getEndEle();
                        }
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "eq_value");
                    CabinetEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, CabinetEqTotalDayDo.class);
                    if (eqMaxValue != null) {
                        maxEle = eqMaxValue.getEqValue();
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
    public Map getReportPowDataById(String Id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        CabinetChartResBase aLineRes = new CabinetChartResBase();
        CabinetChartResBase bLineRes = new CabinetChartResBase();

        try {
            if (Id != null) {
                String index = null;

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "cabinet_hda_pow_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "cabinet_hda_pow_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getCabinetData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index);
                List<CabinetPowHourDo> cabinetPowHourDoList = cabinetData.stream().map(str -> JsonUtils.parseObject(str, CabinetPowHourDo.class)).collect(Collectors.toList());


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
                apparentPowA.setName("A平均视在功率");
                LineSeries activePowA = new LineSeries();
                activePowA.setName("A平均有功功率");
                LineSeries reactivePowA = new LineSeries();
                reactivePowA.setName("A平均无功功率");
                aLineRes.getSeries().add(apparentPowA);
                aLineRes.getSeries().add(activePowA);
                aLineRes.getSeries().add(reactivePowA);


                LineSeries apparentPowB = new LineSeries();
                apparentPowB.setName("B平均视在功率");
                LineSeries activePowB = new LineSeries();
                activePowB.setName("B平均有功功率");
                LineSeries reactivePowB = new LineSeries();
                reactivePowB.setName("B平均无功功率");
                bLineRes.getSeries().add(apparentPowB);
                bLineRes.getSeries().add(activePowB);
                bLineRes.getSeries().add(reactivePowB);

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    cabinetPowHourDoList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalReactivePow.getData().add(hourdo.getReactiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().split(" ")[1]);

                        apparentPowA.getData().add(hourdo.getApparentAAvgValue());
                        activePowA.getData().add(hourdo.getActiveAAvgValue());
                        reactivePowA.getData().add(hourdo.getReactiveAAvgValue());
                        aLineRes.getTime().add(hourdo.getCreateTime().split(" ")[1]);

                        apparentPowB.getData().add(hourdo.getApparentBAvgValue());
                        activePowB.getData().add(hourdo.getActiveBAvgValue());
                        reactivePowB.getData().add(hourdo.getReactiveBAvgValue());
                        bLineRes.getTime().add(hourdo.getCreateTime().split(" ")[1]);
                    });
                } else {
                    cabinetPowHourDoList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalReactivePow.getData().add(hourdo.getReactiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().split(" ")[0]);

                        apparentPowA.getData().add(hourdo.getApparentAAvgValue());
                        activePowA.getData().add(hourdo.getActiveAAvgValue());
                        reactivePowA.getData().add(hourdo.getReactiveAAvgValue());
                        aLineRes.getTime().add(hourdo.getCreateTime().split(" ")[0]);

                        apparentPowB.getData().add(hourdo.getApparentBAvgValue());
                        activePowB.getData().add(hourdo.getActiveBAvgValue());
                        reactivePowB.getData().add(hourdo.getReactiveBAvgValue());
                        bLineRes.getTime().add(hourdo.getCreateTime().split(" ")[0]);
                    });
                }

                String apparentTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_total_max_value");
                CabinetPowHourDo totalMaxApparent = JsonUtils.parseObject(apparentTotalMaxValue, CabinetPowHourDo.class);
                String apparentTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_total_min_value");
                CabinetPowHourDo totalMinApparent = JsonUtils.parseObject(apparentTotalMinValue, CabinetPowHourDo.class);

                String activeTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_total_max_value");
                CabinetPowHourDo totalMaxActive = JsonUtils.parseObject(activeTotalMaxValue, CabinetPowHourDo.class);
                String activeTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_total_min_value");
                CabinetPowHourDo totalMinActive = JsonUtils.parseObject(activeTotalMinValue, CabinetPowHourDo.class);

                String reactiveTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "reactive_total_max_value");
                CabinetPowHourDo totalMaxReactive = JsonUtils.parseObject(reactiveTotalMaxValue, CabinetPowHourDo.class);
                String reactiveTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "reactive_total_min_value");
                CabinetPowHourDo totalMinReactive = JsonUtils.parseObject(reactiveTotalMinValue, CabinetPowHourDo.class);

                String apparentAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_a_max_value");
                CabinetPowHourDo maxApparentA = JsonUtils.parseObject(apparentAMaxValue, CabinetPowHourDo.class);
                String apparentAMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_a_min_value");
                CabinetPowHourDo minApparentA = JsonUtils.parseObject(apparentAMinValue, CabinetPowHourDo.class);

                String activeAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_a_max_value");
                CabinetPowHourDo maxActiveA = JsonUtils.parseObject(activeAMaxValue, CabinetPowHourDo.class);
                String activeAMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_a_min_value");
                CabinetPowHourDo minActiveA = JsonUtils.parseObject(activeAMinValue, CabinetPowHourDo.class);

                String reactiveAMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "reactive_a_max_value");
                CabinetPowHourDo maxReactiveA = JsonUtils.parseObject(reactiveAMaxValue, CabinetPowHourDo.class);
                String reactiveAMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "reactive_a_min_value");
                CabinetPowHourDo minReactiveA = JsonUtils.parseObject(reactiveAMinValue, CabinetPowHourDo.class);


                String apparentBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_b_max_value");
                CabinetPowHourDo maxApparentB = JsonUtils.parseObject(apparentBMaxValue, CabinetPowHourDo.class);
                String apparentBMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "apparent_b_min_value");
                CabinetPowHourDo minApparentB = JsonUtils.parseObject(apparentBMinValue, CabinetPowHourDo.class);

                String activeBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_b_max_value");
                CabinetPowHourDo maxActiveB = JsonUtils.parseObject(activeBMaxValue, CabinetPowHourDo.class);
                String activeBMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "active_b_min_value");
                CabinetPowHourDo minActiveB = JsonUtils.parseObject(activeBMinValue, CabinetPowHourDo.class);

                String reactiveBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "reactive_b_max_value");
                CabinetPowHourDo maxReactiveB = JsonUtils.parseObject(reactiveBMaxValue, CabinetPowHourDo.class);
                String reactiveBMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index, "reactive_b_min_value");
                CabinetPowHourDo minReactiveB = JsonUtils.parseObject(reactiveBMinValue, CabinetPowHourDo.class);


                result.put("totalLineRes", totalLineRes);
                result.put("aLineRes", aLineRes);
                result.put("bLineRes", bLineRes);

                result.put("apparentPowMaxValue", totalMaxApparent.getApparentTotalMaxValue());
                result.put("apparentPowMaxTime", totalMaxApparent.getApparentTotalMaxTime());
                result.put("apparentPowMinValue", totalMinApparent.getApparentTotalMinValue());
                result.put("apparentPowMinTime", totalMinApparent.getApparentTotalMinTime());
                result.put("activePowMaxValue", totalMaxActive.getActiveTotalMaxValue());
                result.put("activePowMaxTime", totalMaxActive.getActiveTotalMaxTime());
                result.put("activePowMinValue", totalMinActive.getActiveTotalMinValue());
                result.put("activePowMinTime", totalMinActive.getActiveTotalMinTime());
                result.put("reactivePowMaxValue", totalMaxReactive.getReactiveTotalMaxValue());
                result.put("reactivePowMaxTime", totalMaxReactive.getReactiveTotalMaxTime());
                result.put("reactivePowMinValue", totalMinReactive.getReactiveTotalMinValue());
                result.put("reactivePowMinTime", totalMinReactive.getReactiveTotalMinTime());

                result.put("AapparentPowMaxValue", maxApparentA.getApparentAMaxValue());
                result.put("AapparentPowMaxTime", maxApparentA.getApparentAMaxTime());
                result.put("AapparentPowMinValue", minApparentA.getApparentAMinValue());
                result.put("AapparentPowMinTime", minApparentA.getApparentAMinTime());
                result.put("AactivePowMaxValue", maxActiveA.getActiveAMaxValue());
                result.put("AactivePowMaxTime", maxActiveA.getActiveAMaxTime());
                result.put("AactivePowMinValue", minActiveA.getActiveAMinValue());
                result.put("AactivePowMinTime", minActiveA.getActiveAMinTime());
                result.put("AreactivePowMaxValue", maxReactiveA.getReactiveAMaxValue());
                result.put("AreactivePowMaxTime", maxReactiveA.getReactiveAMaxTime());
                result.put("AreactivePowMinValue", minReactiveA.getReactiveAMinValue());
                result.put("AreactivePowMinTime", minReactiveA.getReactiveAMinTime());


                result.put("BapparentPowMaxValue", maxApparentB.getApparentBMaxValue());
                result.put("BapparentPowMaxTime", maxApparentB.getApparentBMaxTime());
                result.put("BapparentPowMinValue", minApparentB.getApparentBMinValue());
                result.put("BapparentPowMinTime", minApparentB.getApparentBMinTime());
                result.put("BactivePowMaxValue", maxActiveB.getActiveBMaxValue());
                result.put("BactivePowMaxTime", maxActiveB.getActiveBMaxTime());
                result.put("BactivePowMinValue", minActiveB.getActiveBMinValue());
                result.put("BactivePowMinTime", minActiveB.getActiveBMinTime());
                result.put("BreactivePowMaxValue", maxReactiveB.getReactiveBMaxValue());
                result.put("BreactivePowMaxTime", maxReactiveB.getReactiveBMaxTime());
                result.put("BreactivePowMinValue", minReactiveB.getReactiveBMinValue());
                result.put("BreactivePowMinTime", minReactiveB.getReactiveBMinTime());

            }
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public Map getReportPowDataByIdAndType(String Id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        CabinetChartResBase aLineRes = new CabinetChartResBase();
        CabinetChartResBase bLineRes = new CabinetChartResBase();

        try {
            if (Id != null) {
                String index = null;

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "cabinet_hda_pow_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "cabinet_hda_pow_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getCabinetData(startTime, endTime, Arrays.asList(Integer.valueOf(Id)), index);
                List<CabinetPowHourDo> powList = cabinetData.stream().map(str -> JsonUtils.parseObject(str, CabinetPowHourDo.class)).collect(Collectors.toList());

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
                    totalApparentPowHappenTime = powList.stream().map(CabinetPowBaseDo::getApparentTotalMaxTime).collect(Collectors.toList());
                    totalActivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getActiveTotalMaxTime).collect(Collectors.toList());
                    totalReactivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getReactiveTotalMaxTime).collect(Collectors.toList());
                    //A路
                    aApparentPowHappenTime = powList.stream().map(CabinetPowBaseDo::getApparentAMaxTime).collect(Collectors.toList());
                    aActivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getActiveAMaxTime).collect(Collectors.toList());
                    aReactivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getReactiveAMaxTime).collect(Collectors.toList());
                    //B路
                    bApparentPowHappenTime = powList.stream().map(CabinetPowBaseDo::getApparentBMaxTime).collect(Collectors.toList());
                    bActivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getActiveBMaxTime).collect(Collectors.toList());
                    bReactivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getReactiveBMaxTime).collect(Collectors.toList());

                } else if (dataType == -1) {
                    totalApparentPowHappenTime = powList.stream().map(CabinetPowBaseDo::getApparentTotalMinTime).collect(Collectors.toList());
                    totalActivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getActiveTotalMinTime).collect(Collectors.toList());
                    totalReactivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getReactiveTotalMinTime).collect(Collectors.toList());
                    //A路
                    aApparentPowHappenTime = powList.stream().map(CabinetPowBaseDo::getApparentAMinTime).collect(Collectors.toList());
                    aActivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getActiveAMinTime).collect(Collectors.toList());
                    aReactivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getReactiveAMinTime).collect(Collectors.toList());
                    //B路
                    bApparentPowHappenTime = powList.stream().map(CabinetPowBaseDo::getApparentBMinTime).collect(Collectors.toList());
                    bActivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getActiveBMinTime).collect(Collectors.toList());
                    bReactivePowHappenTime = powList.stream().map(CabinetPowBaseDo::getReactiveBMinTime).collect(Collectors.toList());
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

                for (CabinetPowHourDo boxTotalHourDo : powList) {
                    if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                        totalLineRes.getTime().add(boxTotalHourDo.getCreateTime().split(" ")[1]);
                        aLineRes.getTime().add(boxTotalHourDo.getCreateTime().split(" ")[1]);
                        bLineRes.getTime().add(boxTotalHourDo.getCreateTime().split(" ")[1]);
                    } else {
                        totalLineRes.getTime().add(boxTotalHourDo.getCreateTime().split(" ")[0]);
                        aLineRes.getTime().add(boxTotalHourDo.getCreateTime().split(" ")[0]);
                        bLineRes.getTime().add(boxTotalHourDo.getCreateTime().split(" ")[0]);
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


    public void processPowMavMin(List<CabinetPowHourDo> powList, Integer dataType, Map<String, Object> result) {
        PowerData apparentPowData = new PowerData();
        PowerData activePowData = new PowerData();
        PowerData reactivePowData = new PowerData();

        PowerData aApparentPowData = new PowerData();
        PowerData aActivePowData = new PowerData();
        PowerData aReactivePowData = new PowerData();

        PowerData bApparentPowData = new PowerData();
        PowerData bActivePowData = new PowerData();
        PowerData bReactivePowData = new PowerData();

        for (CabinetPowHourDo boxTotalHourDo : powList) {
            updatePowerData(apparentPowData, boxTotalHourDo.getApparentTotalMaxValue(), boxTotalHourDo.getApparentTotalMaxTime(), boxTotalHourDo.getApparentTotalAvgValue(), boxTotalHourDo.getApparentTotalMinValue(), boxTotalHourDo.getApparentTotalMinTime(), dataType);
            updatePowerData(activePowData, boxTotalHourDo.getActiveTotalMaxValue(), boxTotalHourDo.getActiveTotalMaxTime(), boxTotalHourDo.getActiveTotalAvgValue(), boxTotalHourDo.getActiveTotalMinValue(), boxTotalHourDo.getActiveTotalMinTime(), dataType);
            updatePowerData(reactivePowData, boxTotalHourDo.getReactiveTotalMaxValue(), boxTotalHourDo.getReactiveTotalMaxTime(), boxTotalHourDo.getReactiveTotalAvgValue(), boxTotalHourDo.getReactiveTotalMinValue(), boxTotalHourDo.getReactiveTotalMinTime(), dataType);

            updatePowerData(aApparentPowData, boxTotalHourDo.getApparentAMaxValue(), boxTotalHourDo.getApparentAMaxTime(), boxTotalHourDo.getApparentAAvgValue(), boxTotalHourDo.getApparentAMinValue(), boxTotalHourDo.getApparentAMinTime(), dataType);
            updatePowerData(aActivePowData, boxTotalHourDo.getActiveAMaxValue(), boxTotalHourDo.getActiveAMaxTime(), boxTotalHourDo.getActiveAAvgValue(), boxTotalHourDo.getActiveAMinValue(), boxTotalHourDo.getActiveAMinTime(), dataType);
            updatePowerData(aReactivePowData, boxTotalHourDo.getReactiveAMaxValue(), boxTotalHourDo.getReactiveAMaxTime(), boxTotalHourDo.getReactiveAAvgValue(), boxTotalHourDo.getReactiveAMinValue(), boxTotalHourDo.getReactiveAMinTime(), dataType);

            updatePowerData(bApparentPowData, boxTotalHourDo.getApparentBMaxValue(), boxTotalHourDo.getApparentBMaxTime(), boxTotalHourDo.getApparentBAvgValue(), boxTotalHourDo.getApparentBMinValue(), boxTotalHourDo.getApparentBMinTime(), dataType);
            updatePowerData(bActivePowData, boxTotalHourDo.getActiveBMaxValue(), boxTotalHourDo.getActiveBMaxTime(), boxTotalHourDo.getActiveBAvgValue(), boxTotalHourDo.getActiveBMinValue(), boxTotalHourDo.getActiveBMinTime(), dataType);
            updatePowerData(bReactivePowData, boxTotalHourDo.getReactiveBMaxValue(), boxTotalHourDo.getReactiveBMaxTime(), boxTotalHourDo.getReactiveBAvgValue(), boxTotalHourDo.getReactiveBMinValue(), boxTotalHourDo.getReactiveBMinTime(), dataType);


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

    private void updateTemHumData(PowerData powerData, Float maxValue, String maxTime, Float avgValue, Float minValue, String minTime, Integer dataType, Integer id) {
        if (dataType == 1) {
            updateTemHumExtremes(powerData, maxValue, maxTime, maxValue, maxTime, id);
        } else if (dataType == 0) {
            updateTemHumExtremes(powerData, avgValue, "无", avgValue, "无", id);
        } else if (dataType == -1) {
            updateTemHumExtremes(powerData, minValue, minTime, minValue, minTime, id);
        }
    }

    private void updateTemHumExtremes(PowerData powerData, Float maxValue, String maxTime, Float minValue, String minTime, Integer id) {
        if (powerData.getMaxValue() <= maxValue) {
            powerData.setMaxValue(maxValue);
            powerData.setMaxTime(maxTime);
            powerData.setMaxId(id);
        }
        if (powerData.getMinValue() >= minValue) {
            powerData.setMinValue(minValue);
            powerData.setMinTime(minTime);
            powerData.setMinId(id);
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
    public Map getCabinetEnvIceTemAndHumData(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase temResult = new CabinetChartResBase();
        CabinetChartResBase humResult = new CabinetChartResBase();
        result.put("temResult", temResult);
        result.put("humResult", humResult);
        result.put("temMaxValue", null);
        result.put("temMaxTime", null);
        result.put("temMaxSensorId", null);
        result.put("temMinValue", null);
        result.put("temMinTime", null);
        result.put("temMinSensorId", null);
        result.put("humMaxValue", null);
        result.put("humMaxTime", null);
        result.put("humMaxSensorId", null);
        result.put("humMinValue", null);
        result.put("humMinTime", null);
        result.put("humMinSensorId", null);
        try {
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id), false);
            if (cabinetPdu == null) {
                return result;
            }
            List<String> devKeyList = new ArrayList<>();
            Map<String, String> pduMap = new HashMap<>();
            pduMap.put("A", cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            pduMap.put("B", cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyA())) {
                devKeyList.add(cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            }
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyB())) {
                devKeyList.add(cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            }
            if (CollectionUtil.isEmpty(devKeyList)) {
                return result;
            }
            List<PduIndex> pduIndices = pduIndexMapper.selectList(new LambdaQueryWrapperX<PduIndex>().in(PduIndex::getPduKey, devKeyList));
            Map<String, Integer> pduIdMap = pduIndices.stream().collect(Collectors.toMap(PduIndex::getPduKey, PduIndex::getId));
            String whichIndex = "pdu_env_hour";
            LocalDateTime now = LocalDateTime.now();
            if (timeType == 2) {
                whichIndex = "pdu_env_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            } else {
                oldTime = now.minusHours(25);
                newTime = now;
            }
            List<CabinetEnvSensor> cabinetEnvSensors = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 1)
                    .eq(CabinetEnvSensor::getSensorType, 0)
                    .orderByAsc(CabinetEnvSensor::getPosition));
            List<Integer> searchIds = cabinetEnvSensors.stream().filter(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu()))) != null).map(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu())))).collect(Collectors.toList());
            List<Integer> sensorIds = cabinetEnvSensors.stream().map(CabinetEnvSensor::getSensorId).collect(Collectors.toList());
            List<String> data = getData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex);
            if (CollectionUtil.isEmpty(data)) {
                return result;
            }
            Map<Integer, Map<Integer, List<PduEnvHourDo>>> pduEnvHourDoMap = data.stream()
                    .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                    .collect(Collectors.groupingBy(PduEnvHourDo::getPduId, Collectors.groupingBy(PduEnvHourDo::getSensorId)));
            List<String> time = null;
            boolean isFisrt = false;
            for (CabinetEnvSensor cabinetEnvSensor : cabinetEnvSensors) {
                int position = cabinetEnvSensor.getPosition();
                if (pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))) == null) {
                    continue;
                }
                List<PduEnvHourDo> pduEnvHourDo = pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))).get(cabinetEnvSensor.getSensorId());
                List<Float> temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                List<Float> humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumAvgValue).map(Float::valueOf).collect(Collectors.toList());
                LineSeries temLineSeries = new LineSeries();
                LineSeries humLineSeries = new LineSeries();
                String temName = null;
                String humName = null;
                if (position == 1) {
                    temName = "上层温度传感器";
                    humName = "上层湿度传感器";
                } else if (position == 2) {
                    temName = "中层温度传感器";
                    humName = "中层湿度传感器";
                } else if (position == 3) {
                    temName = "下层温度传感器";
                    humName = "下层湿度传感器";
                }
                temLineSeries.setName(temName);
                temLineSeries.setData(temList);
                humLineSeries.setName(humName);
                humLineSeries.setData(humList);
                if (!isFisrt) {
                    if (timeType == 2) {

                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                    } else {
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                    }
                    temResult.setTime(time);
                    humResult.setTime(time);
                    isFisrt = true;
                }
                temResult.getSeries().add(temLineSeries);
                humResult.getSeries().add(humLineSeries);
            }
            String temMaxValue = getPDUMaxData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "tem_max_value");
            PduEnvHourDo temMax = JsonUtils.parseObject(temMaxValue, PduEnvHourDo.class);
            String temMinValue = getPDUMinData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "tem_min_value");
            PduEnvHourDo temMin = JsonUtils.parseObject(temMinValue, PduEnvHourDo.class);
            String humMaxValue = getPDUMaxData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "hum_max_value");
            PduEnvHourDo humMax = JsonUtils.parseObject(humMaxValue, PduEnvHourDo.class);
            String humMinValue = getPDUMinData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "hum_min_value");
            PduEnvHourDo humMin = JsonUtils.parseObject(humMinValue, PduEnvHourDo.class);
            if (temMax != null) {
                result.put("temMaxValue", temMax.getTemMaxValue());
                result.put("temMaxTime", temMax.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("temMaxSensorId", temMax.getSensorId());
            }
            if (temMin != null) {
                result.put("temMinValue", temMin.getTemMinValue());
                result.put("temMinTime", temMin.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("temMinSensorId", temMin.getSensorId());
            }
            if (humMax != null) {
                result.put("humMaxValue", humMax.getHumMaxValue());
                result.put("humMaxTime", humMax.getHumMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("humMaxSensorId", humMax.getSensorId());
            }
            if (humMin != null) {
                result.put("humMinValue", humMin.getHumMinValue());
                result.put("humMinTime", humMin.getHumMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("humMinSensorId", humMin.getSensorId());
            }
            return result;
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public Map getCabinetEnvIceTemAndHumDataByType(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        CabinetChartResBase temResult = new CabinetChartResBase();
        CabinetChartResBase humResult = new CabinetChartResBase();

        try {
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id), false);
            if (cabinetPdu == null) {
                return result;
            }
            List<String> devKeyList = new ArrayList<>();
            Map<String, String> pduMap = new HashMap<>();
            pduMap.put("A", cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            pduMap.put("B", cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyA())) {
                devKeyList.add(cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            }
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyB())) {
                devKeyList.add(cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            }
            if (CollectionUtil.isEmpty(devKeyList)) {
                return result;
            }
            List<PduIndex> pduIndices = pduIndexMapper.selectList(new LambdaQueryWrapperX<PduIndex>().in(PduIndex::getPduKey, devKeyList));
            Map<String, Integer> pduIdMap = pduIndices.stream().collect(Collectors.toMap(PduIndex::getPduKey, PduIndex::getId));
            String whichIndex = "pdu_env_hour";
            LocalDateTime now = LocalDateTime.now();
            if (timeType == 1) {
                whichIndex = "pdu_env_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            } else {
//                oldTime = now.minusHours(25);
                newTime = now;
            }
            List<CabinetEnvSensor> cabinetEnvSensors = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 1)
                    .eq(CabinetEnvSensor::getSensorType, 0)
                    .orderByAsc(CabinetEnvSensor::getPosition));
            List<Integer> searchIds = cabinetEnvSensors.stream().filter(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu()))) != null).map(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu())))).collect(Collectors.toList());
            List<Integer> sensorIds = cabinetEnvSensors.stream().map(CabinetEnvSensor::getSensorId).collect(Collectors.toList());
            List<String> data = getData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex);
            if (CollectionUtil.isEmpty(data)) {
                return result;
            }
            Map<Integer, Map<Integer, List<PduEnvHourDo>>> pduEnvHourDoMap = data.stream()
                    .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                    .collect(Collectors.groupingBy(PduEnvHourDo::getPduId, Collectors.groupingBy(PduEnvHourDo::getSensorId)));
            List<String> time = null;
            boolean isFisrt = false;
            for (CabinetEnvSensor cabinetEnvSensor : cabinetEnvSensors) {
                int position = cabinetEnvSensor.getPosition();
                if (pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))) == null) {
                    continue;
                }
                List<PduEnvHourDo> pduEnvHourDo = pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))).get(cabinetEnvSensor.getSensorId());

                List<Float> temList = new ArrayList<>();
                List<Float> humList = new ArrayList<>();
                List<String> temHappenTime = new ArrayList<>();
                List<String> humHappenTime = new ArrayList<>();
                //处理最大值最小值平均值
                if (dataType == 1) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemMaxValue).collect(Collectors.toList());
                    temHappenTime = pduEnvHourDo.stream().map(item -> item.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumMaxValue).map(Float::valueOf).collect(Collectors.toList());
                    humHappenTime = pduEnvHourDo.stream().map(item -> item.getHumMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                } else if (dataType == 0) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumAvgValue).map(Float::valueOf).collect(Collectors.toList());
                } else if (dataType == -1) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemMinValue).collect(Collectors.toList());
                    temHappenTime = pduEnvHourDo.stream().map(item -> item.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumMinValue).map(Float::valueOf).collect(Collectors.toList());
                    humHappenTime = pduEnvHourDo.stream().map(item -> item.getHumMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }
//                processTemHumMavMin(pduEnvHourDo, dataType, result);


                LineSeries temLineSeries = new LineSeries();
                LineSeries humLineSeries = new LineSeries();
                String temName = null;
                String humName = null;
                if (position == 1) {
                    temName = "上层温度传感器";
                    humName = "上层湿度传感器";
                } else if (position == 2) {
                    temName = "中层温度传感器";
                    humName = "中层湿度传感器";
                } else if (position == 3) {
                    temName = "下层温度传感器";
                    humName = "下层湿度传感器";
                }
                temLineSeries.setName(temName);
                temLineSeries.setData(temList);
                temLineSeries.setHappenTime(temHappenTime);
                humLineSeries.setName(humName);
                humLineSeries.setData(humList);
                humLineSeries.setHappenTime(humHappenTime);
                if (!isFisrt) {
                    if (timeType == 2) {

                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                    } else {
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                    }
                    temResult.setTime(time);
                    humResult.setTime(time);
                    isFisrt = true;
                }
                temResult.getSeries().add(temLineSeries);
                humResult.getSeries().add(humLineSeries);
            }

        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        result.put("temResult", temResult);
        result.put("humResult", humResult);


        return result;
    }


    private void processTemHumMavMin(List<PduEnvHourDo> temList, Integer dataType, Map<String, Object> result, String temName, String humName, int dataIndex) {
        PowerData tem = new PowerData();
        PowerData hum = new PowerData();
        for (PduEnvHourDo pduEnvHourDo : temList) {

            updateTemHumData(tem, pduEnvHourDo.getTemMaxValue(), pduEnvHourDo.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss"), pduEnvHourDo.getTemAvgValue()
                    , pduEnvHourDo.getTemMinValue(), pduEnvHourDo.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType, pduEnvHourDo.getSensorId());
            updateTemHumData(hum, (float) pduEnvHourDo.getHumMaxValue(), pduEnvHourDo.getHumMaxTime().toString("yyyy-MM-dd HH:mm:ss"), (float) pduEnvHourDo.getHumAvgValue()
                    , (float) pduEnvHourDo.getHumMinValue(), pduEnvHourDo.getHumMinTime().toString("yyyy-MM-dd HH:mm:ss"), dataType, pduEnvHourDo.getSensorId());
        }

        result.put("temName"+dataIndex,temName);
        result.put("temMaxValue"+dataIndex, tem.getMaxValue());
        result.put("temMaxTime"+dataIndex, tem.getMaxTime());
        result.put("temMaxSensorId"+dataIndex, tem.getMaxId());
        result.put("temMinValue"+dataIndex, tem.getMinValue());
        result.put("temMinTime"+dataIndex, tem.getMinTime());
        result.put("temMinSensorId"+dataIndex, tem.getMinId());

        result.put("humName"+dataIndex,humName);
        result.put("humMaxValue"+dataIndex, hum.getMinValue());
        result.put("humMaxTime"+dataIndex, hum.getMaxTime());
        result.put("humMaxSensorId"+dataIndex, hum.getMaxId());
        result.put("humMinValue"+dataIndex, hum.getMinValue());
        result.put("humMinTime"+dataIndex, hum.getMinTime());
        result.put("humMinSensorId"+dataIndex, hum.getMinId());
    }

    @Override
    public Map getCabinetEnvHotTemAndHumDataByType(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        CabinetChartResBase temResult = new CabinetChartResBase();
        CabinetChartResBase humResult = new CabinetChartResBase();

        try {
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id), false);
            if (cabinetPdu == null) {
                return result;
            }
            List<String> devKeyList = new ArrayList<>();
            Map<String, String> pduMap = new HashMap<>();
            pduMap.put("A", cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            pduMap.put("B", cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyA())) {
                devKeyList.add(cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            }
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyB())) {
                devKeyList.add(cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            }
            if (CollectionUtil.isEmpty(devKeyList)) {
                return result;
            }
            List<PduIndex> pduIndices = pduIndexMapper.selectList(new LambdaQueryWrapperX<PduIndex>().in(PduIndex::getPduKey, devKeyList));
            Map<String, Integer> pduIdMap = pduIndices.stream().collect(Collectors.toMap(PduIndex::getPduKey, PduIndex::getId));
            String whichIndex = "pdu_env_hour";
            LocalDateTime now = LocalDateTime.now();
            if (timeType == 2) {
                whichIndex = "pdu_env_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            } else {
//                oldTime = now.minusHours(25);
                newTime = now;
            }
            List<CabinetEnvSensor> cabinetEnvSensors = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 2)
                    .eq(CabinetEnvSensor::getSensorType, 0)
                    .orderByAsc(CabinetEnvSensor::getPosition));
            List<Integer> searchIds = cabinetEnvSensors.stream().filter(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu()))) != null).map(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu())))).collect(Collectors.toList());
            List<Integer> sensorIds = cabinetEnvSensors.stream().map(CabinetEnvSensor::getSensorId).collect(Collectors.toList());
            List<String> data = getData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex);
            if (CollectionUtil.isEmpty(data)) {
                return result;
            }
            Map<Integer, Map<Integer, List<PduEnvHourDo>>> pduEnvHourDoMap = data.stream()
                    .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                    .collect(Collectors.groupingBy(PduEnvHourDo::getPduId, Collectors.groupingBy(PduEnvHourDo::getSensorId)));
            List<String> time = null;
            boolean isFisrt = false;
            for (CabinetEnvSensor cabinetEnvSensor : cabinetEnvSensors) {
                int position = cabinetEnvSensor.getPosition();
                Integer i = pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())));
                if (pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))) == null) {
                    continue;
                }
                List<PduEnvHourDo> pduEnvHourDo = pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))).get(cabinetEnvSensor.getSensorId());
                List<Float> temList = new ArrayList<>();
                List<Float> humList = new ArrayList<>();
                List<String> temHappenTime = new ArrayList<>();
                List<String> humHappenTime = new ArrayList<>();
                //处理最大值最小值平均值
                if (dataType == 1) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemMaxValue).collect(Collectors.toList());
                    temHappenTime = pduEnvHourDo.stream().map(item -> item.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumMaxValue).map(Float::valueOf).collect(Collectors.toList());
                    humHappenTime = pduEnvHourDo.stream().map(item -> item.getHumMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                } else if (dataType == 0) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumAvgValue).map(Float::valueOf).collect(Collectors.toList());
                } else if (dataType == -1) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemMinValue).collect(Collectors.toList());
                    temHappenTime = pduEnvHourDo.stream().map(item -> item.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumMinValue).map(Float::valueOf).collect(Collectors.toList());
                    humHappenTime = pduEnvHourDo.stream().map(item -> item.getHumMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }
//                processTemHumMavMin(pduEnvHourDo, dataType, result);

                LineSeries temLineSeries = new LineSeries();
                LineSeries humLineSeries = new LineSeries();
                String temName = null;
                String humName = null;
                if (position == 1) {
                    temName = "上层温度传感器";
                    humName = "上层湿度传感器";
                } else if (position == 2) {
                    temName = "中层温度传感器";
                    humName = "中层湿度传感器";
                } else if (position == 3) {
                    temName = "下层温度传感器";
                    humName = "下层湿度传感器";
                }
                temLineSeries.setName(temName);
                temLineSeries.setData(temList);
                temLineSeries.setHappenTime(temHappenTime);
                humLineSeries.setName(humName);
                humLineSeries.setData(humList);
                humLineSeries.setHappenTime(humHappenTime);
                if (!isFisrt) {
                    if (timeType == 2) {
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                    } else {
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                    }
                    temResult.setTime(time);
                    humResult.setTime(time);
                    isFisrt = true;
                }
                temResult.getSeries().add(temLineSeries);
                humResult.getSeries().add(humLineSeries);
            }

        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        result.put("temResult", temResult);
        result.put("humResult", humResult);
        return result;
    }

    @Override
    public Map getCabinetEnvIceAndHotTemAndHumData(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        CabinetChartResBase temResult = new CabinetChartResBase();
        CabinetChartResBase humResult = new CabinetChartResBase();
        int dataIndex = 1;
        try {
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id), false);
            if (cabinetPdu == null) {
                return result;
            }
            List<String> devKeyList = new ArrayList<>();
            Map<String, String> pduMap = new HashMap<>();
            pduMap.put("A", cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            pduMap.put("B", cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyA())) {
                devKeyList.add(cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            }
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyB())) {
                devKeyList.add(cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            }
            if (CollectionUtil.isEmpty(devKeyList)) {
                return result;
            }
            List<PduIndex> pduIndices = pduIndexMapper.selectList(new LambdaQueryWrapperX<PduIndex>().in(PduIndex::getPduKey, devKeyList));
            Map<String, Integer> pduIdMap = pduIndices.stream().collect(Collectors.toMap(PduIndex::getPduKey, PduIndex::getId));
            String whichIndex = "pdu_env_hour";
            LocalDateTime now = LocalDateTime.now();
            if (timeType == 1) {
                whichIndex = "pdu_env_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            } else {
//                oldTime = now.minusHours(25);
                newTime = now;
            }
            List<CabinetEnvSensor> iCabinetEnvSensors = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 1)
                    .eq(CabinetEnvSensor::getSensorType, 0)
                    .orderByAsc(CabinetEnvSensor::getPosition));
            List<Integer> iSearchIds = iCabinetEnvSensors.stream().filter(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu()))) != null).map(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu())))).collect(Collectors.toList());
            List<Integer> iSensorIds = iCabinetEnvSensors.stream().map(CabinetEnvSensor::getSensorId).collect(Collectors.toList());
            List<String> iData = getData(localDateTimeToString(oldTime), localDateTimeToString(newTime), iSearchIds, iSensorIds, whichIndex);
            if (CollectionUtil.isEmpty(iData)) {
                return result;
            }
            Map<Integer, Map<Integer, List<PduEnvHourDo>>> iPduEnvHourDoMap = iData.stream()
                    .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                    .collect(Collectors.groupingBy(PduEnvHourDo::getPduId, Collectors.groupingBy(PduEnvHourDo::getSensorId)));
            List<String> time = null;
            boolean isFisrt = false;
            for (CabinetEnvSensor cabinetEnvSensor : iCabinetEnvSensors) {
                int position = cabinetEnvSensor.getPosition();
                if (iPduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))) == null) {
                    continue;
                }
                List<PduEnvHourDo> pduEnvHourDo = iPduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))).get(cabinetEnvSensor.getSensorId());

                List<Float> temList = new ArrayList<>();
                List<Float> humList = new ArrayList<>();
                List<String> temHappenTime = new ArrayList<>();
                List<String> humHappenTime = new ArrayList<>();
                //处理最大值最小值平均值
                if (dataType == 1) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemMaxValue).collect(Collectors.toList());
                    temHappenTime = pduEnvHourDo.stream().map(item -> item.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumMaxValue).map(Float::valueOf).collect(Collectors.toList());
                    humHappenTime = pduEnvHourDo.stream().map(item -> item.getHumMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                } else if (dataType == 0) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumAvgValue).map(Float::valueOf).collect(Collectors.toList());
                } else if (dataType == -1) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemMinValue).collect(Collectors.toList());
                    temHappenTime = pduEnvHourDo.stream().map(item -> item.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumMinValue).map(Float::valueOf).collect(Collectors.toList());
                    humHappenTime = pduEnvHourDo.stream().map(item -> item.getHumMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }


                LineSeries temLineSeries = new LineSeries();
                LineSeries humLineSeries = new LineSeries();
                String temName = null;
                String humName = null;
//                if (position == 1) {
//                    temName = "冷通道上层温度传感器";
//                    humName = "冷通道上层湿度传感器";
//                } else if (position == 2) {
//                    temName = "冷通道中层温度传感器";
//                    humName = "冷通道中层湿度传感器";
//                } else if (position == 3) {
//                    temName = "冷通道下层温度传感器";
//                    humName = "冷通道下层湿度传感器";
//                }

                if (position == 1) {
                    temName = "前-上温度传感器";
                    humName = "前-上湿度传感器";
                } else if (position == 2) {
                    temName = "前-中温度传感器";
                    humName = "前-中湿度传感器";
                } else if (position == 3) {
                    temName = "前-下温度传感器";
                    humName = "前-下湿度传感器";
                }
                processTemHumMavMin(pduEnvHourDo, dataType, result, temName, humName, dataIndex);
                dataIndex++;
                temLineSeries.setName(temName);
                temLineSeries.setData(temList);
                temLineSeries.setHappenTime(temHappenTime);
                humLineSeries.setName(humName);
                humLineSeries.setData(humList);
                humLineSeries.setHappenTime(humHappenTime);
                if (!isFisrt) {
                    if (timeType == 1) {

                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                    } else {
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                    }
                    temResult.setTime(time);
                    humResult.setTime(time);
                    isFisrt = true;
                }
                temResult.getSeries().add(temLineSeries);
                humResult.getSeries().add(humLineSeries);
                result.put("temResult", temResult);
                result.put("humResult", humResult);
            }


            List<CabinetEnvSensor> hCabinetEnvSensors = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 2)
                    .eq(CabinetEnvSensor::getSensorType, 0)
                    .orderByAsc(CabinetEnvSensor::getPosition));
            List<Integer> hSearchIds = hCabinetEnvSensors.stream().filter(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu()))) != null).map(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu())))).collect(Collectors.toList());
            List<Integer> hSensorIds = hCabinetEnvSensors.stream().map(CabinetEnvSensor::getSensorId).collect(Collectors.toList());
            List<String> hData = getData(localDateTimeToString(oldTime), localDateTimeToString(newTime), hSearchIds, hSensorIds, whichIndex);
            if (CollectionUtil.isEmpty(hData)) {
                return result;
            }
            Map<Integer, Map<Integer, List<PduEnvHourDo>>> hPduEnvHourDoMap = hData.stream()
                    .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                    .collect(Collectors.groupingBy(PduEnvHourDo::getPduId, Collectors.groupingBy(PduEnvHourDo::getSensorId)));
            isFisrt = false;
            for (CabinetEnvSensor cabinetEnvSensor : hCabinetEnvSensors) {
                int position = cabinetEnvSensor.getPosition();
                Integer i = pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())));
                if (hPduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))) == null) {
                    continue;
                }
                List<PduEnvHourDo> pduEnvHourDo = hPduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))).get(cabinetEnvSensor.getSensorId());
                List<Float> temList = new ArrayList<>();
                List<Float> humList = new ArrayList<>();
                List<String> temHappenTime = new ArrayList<>();
                List<String> humHappenTime = new ArrayList<>();
                //处理最大值最小值平均值
                if (dataType == 1) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemMaxValue).collect(Collectors.toList());
                    temHappenTime = pduEnvHourDo.stream().map(item -> item.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumMaxValue).map(Float::valueOf).collect(Collectors.toList());
                    humHappenTime = pduEnvHourDo.stream().map(item -> item.getHumMaxTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                } else if (dataType == 0) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumAvgValue).map(Float::valueOf).collect(Collectors.toList());
                } else if (dataType == -1) {
                    temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemMinValue).collect(Collectors.toList());
                    temHappenTime = pduEnvHourDo.stream().map(item -> item.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                    humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumMinValue).map(Float::valueOf).collect(Collectors.toList());
                    humHappenTime = pduEnvHourDo.stream().map(item -> item.getHumMinTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }


                LineSeries temLineSeries = new LineSeries();
                LineSeries humLineSeries = new LineSeries();
                String temName = null;
                String humName = null;
//                if (position == 1) {
//                    temName = "热通道上层温度传感器";
//                    humName = "热通道上层湿度传感器";
//                } else if (position == 2) {
//                    temName = "热通道中层温度传感器";
//                    humName = "热通道中层湿度传感器";
//                } else if (position == 3) {
//                    temName = "热通道下层温度传感器";
//                    humName = "热通道下层湿度传感器";
//                }
                if (position == 1) {
                    temName = "后-上温度传感器";
                    humName = "后-上湿度传感器";
                } else if (position == 2) {
                    temName = "后-中温度传感器";
                    humName = "后-中湿度传感器";
                } else if (position == 3) {
                    temName = "后-下温度传感器";
                    humName = "后-下湿度传感器";
                }
                processTemHumMavMin(pduEnvHourDo, dataType, result, temName, humName, dataIndex);
                dataIndex++;
                temLineSeries.setName(temName);
                temLineSeries.setData(temList);
                temLineSeries.setHappenTime(temHappenTime);
                humLineSeries.setName(humName);
                humLineSeries.setData(humList);
                humLineSeries.setHappenTime(humHappenTime);
                if (!isFisrt) {
                    if (timeType == 1) {
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                    } else {
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                    }
                    temResult.setTime(time);
                    humResult.setTime(time);
                    isFisrt = true;
                }
                temResult.getSeries().add(temLineSeries);
                humResult.getSeries().add(humLineSeries);
            }

        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        result.put("temResult", temResult);
        result.put("humResult", humResult);

        return result;
    }


    @Override
    public Map getCabinetEnvHotTemAndHumData(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase temResult = new CabinetChartResBase();
        CabinetChartResBase humResult = new CabinetChartResBase();
        result.put("temResult", temResult);
        result.put("humResult", humResult);
        result.put("temResult", temResult);
        result.put("humResult", humResult);
        result.put("temMaxValue", null);
        result.put("temMaxTime", null);
        result.put("temMaxSensorId", null);
        result.put("temMinValue", null);
        result.put("temMinTime", null);
        result.put("temMinSensorId", null);
        result.put("humMaxValue", null);
        result.put("humMaxTime", null);
        result.put("humMaxSensorId", null);
        result.put("humMinValue", null);
        result.put("humMinTime", null);
        result.put("humMinSensorId", null);
        try {
            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id), false);
            if (cabinetPdu == null) {
                return result;
            }
            List<String> devKeyList = new ArrayList<>();
            Map<String, String> pduMap = new HashMap<>();
            pduMap.put("A", cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            pduMap.put("B", cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyA())) {
                devKeyList.add(cabinetPdu.getPduKeyA());// + '-' + cabinetPdu.getCasIdA());
            }
            if (!StringUtils.isEmpty(cabinetPdu.getPduKeyB())) {
                devKeyList.add(cabinetPdu.getPduKeyB());// + '-' + cabinetPdu.getCasIdB());
            }
            if (CollectionUtil.isEmpty(devKeyList)) {
                return result;
            }
            List<PduIndex> pduIndices = pduIndexMapper.selectList(new LambdaQueryWrapperX<PduIndex>().in(PduIndex::getPduKey, devKeyList));
            Map<String, Integer> pduIdMap = pduIndices.stream().collect(Collectors.toMap(PduIndex::getPduKey, PduIndex::getId));
            String whichIndex = "pdu_env_hour";
            LocalDateTime now = LocalDateTime.now();
            if (timeType == 2) {
                whichIndex = "pdu_env_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            } else {
                oldTime = now.minusHours(25);
                newTime = now;
            }
            List<CabinetEnvSensor> cabinetEnvSensors = cabinetEnvSensorMapper.selectList(new LambdaQueryWrapperX<CabinetEnvSensor>()
                    .eq(CabinetEnvSensor::getCabinetId, id)
                    .eq(CabinetEnvSensor::getChannel, 2)
                    .eq(CabinetEnvSensor::getSensorType, 0)
                    .orderByAsc(CabinetEnvSensor::getPosition));
            List<Integer> searchIds = cabinetEnvSensors.stream().filter(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu()))) != null).map(env -> pduIdMap.get(pduMap.get(String.valueOf(env.getPathPdu())))).collect(Collectors.toList());
            List<Integer> sensorIds = cabinetEnvSensors.stream().map(CabinetEnvSensor::getSensorId).collect(Collectors.toList());
            List<String> data = getData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex);
            if (CollectionUtil.isEmpty(data)) {
                return result;
            }
            Map<Integer, Map<Integer, List<PduEnvHourDo>>> pduEnvHourDoMap = data.stream()
                    .map(str -> JsonUtils.parseObject(str, PduEnvHourDo.class))
                    .collect(Collectors.groupingBy(PduEnvHourDo::getPduId, Collectors.groupingBy(PduEnvHourDo::getSensorId)));
            List<String> time = null;
            boolean isFisrt = false;
            for (CabinetEnvSensor cabinetEnvSensor : cabinetEnvSensors) {
                int position = cabinetEnvSensor.getPosition();
                Integer i = pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())));
                if (pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))) == null) {
                    continue;
                }
                List<PduEnvHourDo> pduEnvHourDo = pduEnvHourDoMap.get(pduIdMap.get(pduMap.get(String.valueOf(cabinetEnvSensor.getPathPdu())))).get(cabinetEnvSensor.getSensorId());
                List<Float> temList = pduEnvHourDo.stream().map(PduEnvHourDo::getTemAvgValue).collect(Collectors.toList());
                List<Float> humList = pduEnvHourDo.stream().map(PduEnvHourDo::getHumAvgValue).map(Float::valueOf).collect(Collectors.toList());
                LineSeries temLineSeries = new LineSeries();
                LineSeries humLineSeries = new LineSeries();
                String temName = null;
                String humName = null;
                if (position == 1) {
                    temName = "上层温度传感器";
                    humName = "上层湿度传感器";
                } else if (position == 2) {
                    temName = "中层温度传感器";
                    humName = "中层湿度传感器";
                } else if (position == 3) {
                    temName = "下层温度传感器";
                    humName = "下层湿度传感器";
                }
                temLineSeries.setName(temName);
                temLineSeries.setData(temList);
                humLineSeries.setName(humName);
                humLineSeries.setData(humList);
                if (!isFisrt) {
                    if (timeType == 2) {
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("yyyy-MM-dd")).collect(Collectors.toList());
                    } else {
                        time = pduEnvHourDo.stream().map(pduEnvHour -> pduEnvHour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                    }
                    temResult.setTime(time);
                    humResult.setTime(time);
                    isFisrt = true;
                }
                temResult.getSeries().add(temLineSeries);
                humResult.getSeries().add(humLineSeries);
            }
            String temMaxValue = getPDUMaxData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "tem_max_value");
            PduEnvHourDo temMax = JsonUtils.parseObject(temMaxValue, PduEnvHourDo.class);
            String temMinValue = getPDUMinData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "tem_min_value");
            PduEnvHourDo temMin = JsonUtils.parseObject(temMinValue, PduEnvHourDo.class);
            String humMaxValue = getPDUMaxData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "hum_max_value");
            PduEnvHourDo humMax = JsonUtils.parseObject(humMaxValue, PduEnvHourDo.class);
            String humMinValue = getPDUMinData(localDateTimeToString(oldTime), localDateTimeToString(newTime), searchIds, sensorIds, whichIndex, "hum_min_value");
            PduEnvHourDo humMin = JsonUtils.parseObject(humMinValue, PduEnvHourDo.class);
            if (temMax != null) {
                result.put("temMaxValue", temMax.getTemMaxValue());
                result.put("temMaxTime", temMax.getTemMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("temMaxSensorId", temMax.getSensorId());
            }
            if (temMin != null) {
                result.put("temMinValue", temMin.getTemMinValue());
                result.put("temMinTime", temMin.getTemMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("temMinSensorId", temMin.getSensorId());
            }
            if (humMax != null) {
                result.put("humMaxValue", humMax.getHumMaxValue());
                result.put("humMaxTime", humMax.getHumMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("humMaxSensorId", humMax.getSensorId());
            }
            if (humMin != null) {
                result.put("humMinValue", humMin.getHumMinValue());
                result.put("humMinTime", humMin.getHumMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("humMinSensorId", humMin.getSensorId());
            }
            return result;
        } catch (Exception e) {
            log.error("获取数据失败", e);
        }
        return result;
    }

    @Override
    public Map getCabinetPFLine(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        result.put("pfLineRes", totalLineRes);
        try {

            String index = null;

            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                index = "cabinet_hda_pow_hour";
                if (oldTime.equals(newTime)) {
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }

            } else {
                index = "cabinet_hda_pow_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }

            String startTime = localDateTimeToString(oldTime);
            String endTime = localDateTimeToString(newTime);
            List<String> data = getCabinetData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
            List<CabinetPowHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, CabinetPowHourDo.class)).collect(Collectors.toList());

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
    public Map getCabinetPFLineByType(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType) {
        Map result = new HashMap<>();
        CabinetChartResBase totalLineRes = new CabinetChartResBase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {

            String index = null;

            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                index = "cabinet_hda_pow_hour";
                if (oldTime.equals(newTime)) {
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }

            } else {
                index = "cabinet_hda_pow_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }
            boolean isSameDay = (timeType == 0 || oldTime.toLocalDate().equals(newTime.toLocalDate()));
            String startTime = localDateTimeToString(oldTime);
            String endTime = localDateTimeToString(newTime);
            List<String> data = getCabinetData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
            List<CabinetPowHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, CabinetPowHourDo.class)).collect(Collectors.toList());

            //处理数据
            for (CabinetPowHourDo cabinetPowHourDo : powList) {
                DataProcessingUtils.collectPhaseData(cabinetPowHourDo, result, isSameDay, DataTypeEnums.fromValue(dataType));
            }

            String lineKey = "dayList";
            Map<String, Object> lineData = (Map<String, Object>) result.get(lineKey);
            if (lineData != null && !(((List<CabinetPowHourDo>) lineData.get("data")).isEmpty())) {

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

                Map<String, Object> analyzeFactorData = DataProcessingUtils.analyzeFactorData((List<CabinetPowHourDo>) lineData.get("data"), dataType);
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


    @Override
    public List<Integer> idList() {
        return cabIndexMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(IndexDO::getId).collect(Collectors.toList());
    }

    @Override
    public List<CabinetRackRspVO> getRackByCabinet(Integer id) {
        List<CabinetRackRspVO> vos = new ArrayList<>();
        // 查询 CabinetPdu
        CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>().eq(CabinetPdu::getCabinetId, id));
        if (cabinetPdu == null) {
            return vos;
        }
        // 查询 RackIndex 列表
        List<RackIndex> rackIndexList = rackIndexDoMapper.selectList(new LambdaQueryWrapperX<RackIndex>().eq(RackIndex::getCabinetId, id));
        if (CollectionUtils.isEmpty(rackIndexList)) {
            return vos;
        }
        for (RackIndex rackIndex : rackIndexList) {
            List<Integer> outletIdA = rackIndex.getOutletIdA();
            List<Integer> outletIdB = rackIndex.getOutletIdB();
            String pduKeyA = cabinetPdu.getPduKeyA();
            String pduKeyB = cabinetPdu.getPduKeyB();
            // 获取 A 相 pdu 数据
            Object redisValueA = redisTemplate.opsForValue().get(REDIS_KEY_PDU + pduKeyA);
            JSONObject pduDataA = null;
            if (redisValueA != null) {
                JSONObject jsonObjectA = (JSONObject) redisValueA;
                pduDataA = JSONObject.parseObject(jsonObjectA.getString("pdu_data"));
            }
            // 获取 B 相 pdu 数据
            Object redisValueB = redisTemplate.opsForValue().get(REDIS_KEY_PDU + pduKeyB);
            JSONObject pduDataB = null;
            if (redisValueB != null) {
                JSONObject jsonObjectB = (JSONObject) redisValueB;
                pduDataB = JSONObject.parseObject(jsonObjectB.getString("pdu_data"));
            }
            // 计算 A 相功率
            double powActive = 0;
            if (pduDataA != null) {
                JSONObject pduTotalDataA = JSONObject.parseObject(pduDataA.getString("pdu_total_data"));
                if (pduTotalDataA != null) {
                    powActive = pduTotalDataA.getDoubleValue("pow_active");
                }
            }
            // 计算 B 相功率
            double powActiveB = 0;
            if (pduDataB != null) {
                JSONObject pduTotalDataB = JSONObject.parseObject(pduDataB.getString("pdu_total_data"));
                if (pduTotalDataB != null) {
                    powActiveB = pduTotalDataB.getDoubleValue("pow_active");
                }
            }
            // 计算总功率
            double totalPowActive = powActive + powActiveB;
            // 获取 cur_value 数组
            JSONArray curValueArray = new JSONArray();
            if (pduDataA != null) {
                JSONObject outputItemList = pduDataA.getJSONObject("output_item_list");
                if (outputItemList != null) {
                    curValueArray = outputItemList.getJSONArray("cur_value");
                }
            }
            // 计算 A 相电流总和
            double sumA = 0;
            for (Integer index : outletIdA) {
                if (index >= 0 && index < curValueArray.size()) {
                    sumA += curValueArray.getDoubleValue(index);
                }
            }
            // 计算 B 相电流总和
            double sumB = 0;
            for (Integer index : outletIdB) {
                if (index >= 0 && index < curValueArray.size()) {
                    sumB += curValueArray.getDoubleValue(index);
                }
            }
            // 创建 CabinetRackRspVO 对象并添加到结果列表
            CabinetRackRspVO vo = CabinetRackRspVO.builder()
                    .id(rackIndex.getId())
                    .name(rackIndex.getRackName())
                    .totalPower(totalPowActive)
                    .aCurrent(sumA)
                    .bCurrent(sumB)
                    .build();
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public Map<String, Double> getEleByCabinet(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) throws IOException {
        Map<String, Double> result = new HashMap<>();
        String index = "cabinet_ele_total_realtime";
        List<Integer> list = Arrays.asList(Integer.valueOf(id));

        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件
        SearchSourceBuilder builder = new SearchSourceBuilder();

        // 获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(oldTime).lte(newTime))
                .must(QueryBuilders.termsQuery("cabinet_id", list))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1);

        // 执行查询
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        Double eleTotal = null;
        // 处理查询结果
        if (searchResponse.getHits().getHits().length > 0) {
            SearchHit hit = searchResponse.getHits().getHits()[0];
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            // 提取需要的字段
            eleTotal = (Double) sourceAsMap.get("ele_total");
        }

        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest1 = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件
        SearchSourceBuilder builder1 = new SearchSourceBuilder();

        // 获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(oldTime).lte(newTime))
                .must(QueryBuilders.termsQuery("cabinet_id", list))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.DESC);
        // 设置搜索条件
        searchRequest1.source(builder1);
        builder1.size(1);

        // 执行查询
        SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
        Double eleTotal1 = null;
        // 处理查询结果
        if (searchResponse1.getHits().getHits().length > 0) {
            SearchHit hit = searchResponse1.getHits().getHits()[0];
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            // 提取需要的字段
            eleTotal1 = (Double) sourceAsMap.get("ele_total");
        }
        Double ele = null;
        if (eleTotal != null && eleTotal1 != null) {
            ele = eleTotal - eleTotal1;
        }
        result.put("ele", ele);
        return result;
    }


    /**
     * 获取数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param ids       机柜id列表
     * @param index     索引表
     */
    private List<String> getData(String startTime, String endTime, List<Integer> ids, List<Integer> sensorIds, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("pdu_id", ids))
                .must(QueryBuilders.termsQuery("sensor_id", sensorIds))));
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

    private List<String> getCabinetData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("cabinet_id", ids))));
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

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index, String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("cabinet_id", ids))));
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
                .must(QueryBuilders.termsQuery("cabinet_id", ids))));
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


}