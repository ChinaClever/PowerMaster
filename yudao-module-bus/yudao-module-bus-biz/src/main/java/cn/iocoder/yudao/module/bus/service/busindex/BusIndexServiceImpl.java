package cn.iocoder.yudao.module.bus.service.busindex;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEleTotalDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.tem.BusTemHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.total.BusTotalHourDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.constant.BusConstants;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusTemDetailRes;
import cn.iocoder.yudao.module.bus.dal.dataobject.buscurbalancecolor.BusCurbalanceColorDO;
import cn.iocoder.yudao.module.bus.dal.mysql.buscurbalancecolor.BusCurbalanceColorMapper;
import cn.iocoder.yudao.module.bus.util.TimeUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
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
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_PATTERN;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.constant.BusConstants.*;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.*;

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
        busIndexMapper.deleteById(id);
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
        List<BusIndexRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BusIndexDO busIndexDO : list) {
            BusIndexRes busIndexRes = new BusIndexRes();
            busIndexRes.setStatus(busIndexDO.getRunStatus());
            res.add(busIndexRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + busIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray loadRate = lineItemList.getJSONArray("load_rate");
            List<Double> rateList = loadRate.toList(Double.class);
            if(rateList.size() > 1) {
                busIndexRes.setALoadRate(loadRate.getDouble(0));
                busIndexRes.setBLoadRate(loadRate.getDouble(1));
                busIndexRes.setCLoadRate(loadRate.getDouble(2));
            } else{
                busIndexRes.setALoadRate(loadRate.getDouble(0));
            }
            rateList.sort(Collections.reverseOrder());
            Double biggest = rateList.get(0) * 100;
            if (biggest == 0){
                busIndexRes.setColor(0);
            } else if (biggest < 30){
                busIndexRes.setColor(1);
            } else if (biggest < 60){
                busIndexRes.setColor(2);
            } else if (biggest < 90){
                busIndexRes.setColor(3);
            } else if (biggest >= 90){
                busIndexRes.setColor(4);
            }
            if(pageReqVO.getColor() != null){
                if(!pageReqVO.getColor().contains(busIndexRes.getColor())){
                    continue;
                }
            }

        }

        return new PageResult<>(res,busIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BusRedisDataRes> getBusRedisPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusRedisDataRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BusIndexDO busIndexDO : list) {
            BusRedisDataRes busRedisDataRes = new BusRedisDataRes();
            busRedisDataRes.setStatus(busIndexDO.getRunStatus());
            res.add(busRedisDataRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + busIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray volValue = lineItemList.getJSONArray("vol_value");
            JSONArray volStatus = lineItemList.getJSONArray("vol_status");
            JSONArray curValue = lineItemList.getJSONArray("cur_value");
            JSONArray curStatus = lineItemList.getJSONArray("cur_status");
            JSONArray powValue = lineItemList.getJSONArray("pow_value");
            JSONArray powStatus = lineItemList.getJSONArray("pow_status");
            JSONArray powReactive = lineItemList.getJSONArray("pow_reactive");
            for (int i = 0; i < 3; i++) {
                double vol = volValue.getDoubleValue(i);
                Integer volSta = volStatus.getInteger(i);
                double cur = curValue.getDoubleValue(i);
                Integer curSta =curStatus.getInteger(i);
                double activePow = powValue.getDoubleValue(i);
                Integer activePowSta =powStatus.getInteger(i);
                double reactivePow = powReactive.getDoubleValue(i);
                if (i == 0){
                    busRedisDataRes.setACur(cur);
                    busRedisDataRes.setACurStatus(curSta);
                    if(curSta != 0){
                        busRedisDataRes.setACurColor("red");
                    }
                    busRedisDataRes.setAVol(vol);
                    busRedisDataRes.setAVolStatus(volSta);
                    if(volSta != 0){
                        busRedisDataRes.setAVolColor("red");
                    }
                    busRedisDataRes.setAActivePow(activePow);
                    busRedisDataRes.setAActivePowStatus(activePowSta);
                    if(activePowSta != 0){
                        busRedisDataRes.setAActivePowColor("red");
                    }
                    busRedisDataRes.setAReactivePow(reactivePow);
                }else if(i == 1){
                    busRedisDataRes.setBCur(cur);
                    busRedisDataRes.setBCurStatus(curSta);
                    if(curSta != 0){
                        busRedisDataRes.setBCurColor("red");
                    }
                    busRedisDataRes.setBVol(vol);
                    busRedisDataRes.setBVolStatus(volSta);
                    if(volSta != 0){
                        busRedisDataRes.setBVolColor("red");
                    }
                    busRedisDataRes.setBActivePow(activePow);
                    busRedisDataRes.setBActivePowStatus(activePowSta);
                    if(activePowSta != 0){
                        busRedisDataRes.setBActivePowColor("red");
                    }
                    busRedisDataRes.setBReactivePow(reactivePow);
                }else if(i == 2){
                    busRedisDataRes.setCCur(cur);
                    busRedisDataRes.setCCurStatus(curSta);
                    if(curSta != 0){
                        busRedisDataRes.setCCurColor("red");
                    }
                    busRedisDataRes.setCVol(vol);
                    busRedisDataRes.setCVolStatus(volSta);
                    if(volSta != 0){
                        busRedisDataRes.setCVolColor("red");
                    }
                    busRedisDataRes.setCActivePow(activePow);
                    busRedisDataRes.setCActivePowStatus(activePowSta);
                    if(activePowSta != 0){
                        busRedisDataRes.setCActivePowColor("red");
                    }
                    busRedisDataRes.setCReactivePow(reactivePow);
                }
            }

        }
        return new PageResult<>(res,busIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BusIndexDTO> getEqPage(BusIndexPageReqVO pageReqVO) {
        try {
            PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
            List<BusIndexDO> busIndexDOList = busIndexDOPageResult.getList();
            List<BusIndexDTO> result = new ArrayList<>();
            List<Integer> ids = busIndexDOList.stream().map(BusIndexDO::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)){
                return new PageResult<>(result, busIndexDOPageResult.getTotal());
            }
            //昨日
            busIndexDOList.forEach(busIndexDO -> {
                result.add(new BusIndexDTO().setId(busIndexDO.getId()).setRunStatus(busIndexDO.getRunStatus()));
            });
            String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            String endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  yesterdayList = getData(startTime,endTime, ids,"bus_eq_total_day");
            Map<Integer,Double> yesterdayMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(yesterdayList)){
                yesterdayList.forEach(str -> {
                    BusEqTotalDayDo dayDo = JsonUtils.parseObject(str, BusEqTotalDayDo.class);
                    yesterdayMap.put(dayDo.getBusId(),dayDo.getEq());
                });
            }

            //上周
            startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
            endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  weekList = getData(startTime,endTime, ids,"bus_eq_total_week");
            Map<Integer,Double> weekMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(weekList)){
                weekList.forEach(str -> {
                    BusEqTotalWeekDo weekDo = JsonUtils.parseObject(str, BusEqTotalWeekDo.class);
                    weekMap.put(weekDo.getBusId(),weekDo.getEq());
                });
            }

            //上月
            startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
            endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  monthList = getData(startTime,endTime, ids,"bus_eq_total_month");
            Map<Integer,Double> monthMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(monthList)){
                monthList.forEach(str -> {
                    BusEqTotalMonthDo monthDo = JsonUtils.parseObject(str, BusEqTotalMonthDo.class);
                    monthMap.put(monthDo.getBusId(),monthDo.getEq());
                });
            }

            result.forEach(dto -> {
                dto.setYesterdayEq(yesterdayMap.get(dto.getId()));
                dto.setLastWeekEq(weekMap.get(dto.getId()));
                dto.setLastMonthEq(monthMap.get(dto.getId()));
            });
            return new PageResult<>(result, busIndexDOPageResult.getTotal());
        }catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public PageResult<BusBalanceDataRes> getBusBalancePage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        BusCurbalanceColorDO busCurbalanceColorDO = busCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusBalanceDataRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BusIndexDO busIndexDO : list) {
            BusBalanceDataRes busBalanceDataRes = new BusBalanceDataRes();
            busBalanceDataRes.setStatus(busIndexDO.getRunStatus());
            res.add(busBalanceDataRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + busIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray volValue = lineItemList.getJSONArray("vol_value");
            JSONArray curValue = lineItemList.getJSONArray("cur_value");
            JSONObject busTotalData = jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data");
            JSONArray curAlarmArr = lineItemList.getJSONArray("cur_max");
            curAlarmArr.sort(Collections.reverseOrder());
            double maxVal = curAlarmArr.getDouble(0);
            List<Double> temp = curValue.toList(Double.class);
            temp.sort(Collections.reverseOrder());
            double a = temp.get(0) - temp.get(2);
            int color = 0;
            for (int i = 0; i < 3; i++) {
                double vol = volValue.getDoubleValue(i);
                double cur = curValue.getDoubleValue(i);
                if (i == 0){
                    busBalanceDataRes.setACur(cur);
                    busBalanceDataRes.setAVol(vol);
                }else if(i == 1){
                    busBalanceDataRes.setBCur(cur);
                    busBalanceDataRes.setBVol(vol);
                }else if(i == 2){
                    busBalanceDataRes.setCCur(cur);
                    busBalanceDataRes.setCVol(vol);
                }
            }
            if (busCurbalanceColorDO == null) {
                if (a >= maxVal * 0.2) {
                    if (busTotalData.getDouble("cur_unbalance") < 15) {
                        color = 2;
                    } else if (busTotalData.getDouble("cur_unbalance") < 30) {
                        color = 3;
                    } else {
                        color = 4;
                    }
                } else {
                    color = 1;
                }
            } else {
                if (a >= maxVal * 0.2) {
                    if (busTotalData.getDouble("cur_unbalance") < busCurbalanceColorDO.getRangeOne()) {
                        color = 2;
                    } else if (busTotalData.getDouble("cur_unbalance") < busCurbalanceColorDO.getRangeFour()) {
                        color = 3;
                    } else {
                        color = 4;
                    }
                } else {
                    color = 1;
                }
            }
            busBalanceDataRes.setCurUnbalance(busTotalData.getDouble("cur_unbalance"));
            busBalanceDataRes.setVolUnbalance(busTotalData.getDouble("vol_unbalance"));
            busBalanceDataRes.setColor(color);
        }
        return new PageResult<>(res,busIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BusTemRes> getBusTemPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusTemRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BusIndexDO busIndexDO : list) {
            BusTemRes busTemRes = new BusTemRes();
            busTemRes.setStatus(busIndexDO.getRunStatus());
            busTemRes.setBusId(busIndexDO.getId());
            res.add(busTemRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + busIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject envItemList = jsonObject.getJSONObject("env_item_list");
            JSONArray temValue = envItemList.getJSONArray("tem_value");
            JSONArray temStatus = envItemList.getJSONArray("tem_status");
            for (int i = 0; i < 4; i++) {
                double tem = temValue.getDoubleValue(i);
                Integer temSta = temStatus.getInteger(i);
                if (i == 0){
                    busTemRes.setATem(tem);
                    busTemRes.setATemStatus(temSta);
                    if(temSta != 0){
                        busTemRes.setATemColor("red");
                    }
                }else if(i == 1){
                    busTemRes.setBTem(tem);
                    busTemRes.setBTemStatus(temSta);
                    if(temSta != 0){
                        busTemRes.setBTemColor("red");
                    }
                }else if(i == 2){
                    busTemRes.setCTem(tem);
                    busTemRes.setCTemStatus(temSta);
                    if(temSta != 0){
                        busTemRes.setCTemColor("red");
                    }
                }else if(i == 3){
                    busTemRes.setNTem(tem);
                    busTemRes.setNTemStatus(temSta);
                    if(temSta != 0){
                        busTemRes.setNTemColor("red");
                    }
                }
            }

        }
        return new PageResult<>(res,busIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BusPFRes> getBusPFPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusPFRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BusIndexDO busIndexDO : list) {
            BusPFRes busPFRes = new BusPFRes();
            busPFRes.setStatus(busIndexDO.getRunStatus());
            busPFRes.setBusId(busIndexDO.getId());
            res.add(busPFRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + busIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray pfValue = lineItemList.getJSONArray("power_factor");

            for (int i = 0; i < 3; i++) {
                double pf = pfValue.getDoubleValue(i);
                if (i == 0){
                    busPFRes.setApf(pf);
                }else if(i == 1){
                    busPFRes.setBpf(pf);
                }else if(i == 2){
                    busPFRes.setCpf(pf);
                }
            }
            busPFRes.setTotalPf(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDoubleValue("power_factor"));
        }
        return new PageResult<>(res,busIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BusHarmonicRes> getBusHarmonicPage(BusIndexPageReqVO pageReqVO) {
        PageResult<BusIndexDO> busIndexDOPageResult = busIndexMapper.selectPage(pageReqVO);
        List<BusIndexDO> list = busIndexDOPageResult.getList();
        List<BusHarmonicRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BusIndexDO busIndexDO : list) {
            BusHarmonicRes busHarmonicRes = new BusHarmonicRes();
            busHarmonicRes.setStatus(busIndexDO.getRunStatus());
            busHarmonicRes.setDevKey(busIndexDO.getDevKey());
            busHarmonicRes.setBusId(busIndexDO.getId());
            res.add(busHarmonicRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + busIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray curThd = lineItemList.getJSONArray("cur_thd");
            JSONArray volThd = lineItemList.getJSONArray("vol_thd");
            for (int i = 0; i < 3; i++) {
                double curThdValue = curThd.getDoubleValue(i);
                double volThdValue = volThd.getDoubleValue(i);
                if (i == 0){
                    busHarmonicRes.setAcurThd(curThdValue);
                    busHarmonicRes.setAvolThd(volThdValue);
                }else if(i == 1){
                    busHarmonicRes.setBcurThd(curThdValue);
                    busHarmonicRes.setBvolThd(volThdValue);
                }else if(i == 2){
                    busHarmonicRes.setCcurThd(curThdValue);
                    busHarmonicRes.setCvolThd(volThdValue);
                }
            }

        }
        return new PageResult<>(res,busIndexDOPageResult.getTotal());
    }



    @Override
    public List<String> getDevKeyList() {
        List<String> result = busIndexMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(BusIndexDO::getDevKey).collect(Collectors.toList());
        return result;
    }

    @Override
    public PageResult<BusLineRes> getBusLineDevicePage(BusIndexPageReqVO pageReqVO) {
        try {
            PageResult<BusIndexDO> busIndexPageResult = null;
            List<BusLineRes> result = new ArrayList<>();
//            if(pageReqVO.getBusIds() != null && !pageReqVO.getBusIds().isEmpty()) {
//                List<String> ipAddrList = new ArrayList<>();
//                List<BusPdu> cabinetPduList = cabinetPduMapper.selectList(new LambdaQueryWrapperX<BusPdu>().inIfPresent(BusPdu::getBusId, pageReqVO.getBusIds()));
//                if(cabinetPduList != null && cabinetPduList.size() > 0){
//                    for (BusPdu cabinetPdu : cabinetPduList) {
//                        if (!StringUtils.isEmpty(cabinetPdu.getPduIpA())){
//                            ipAddrList.add(cabinetPdu.getPduIpA());
//                        }
//                        if (!StringUtils.isEmpty(cabinetPdu.getPduIpB())){
//                            ipAddrList.add(cabinetPdu.getPduIpB());
//                        }
//                    }
//                }else{
//                    return new PageResult<BusLineRes>(result,0L);
//                }
//                busIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<BusIndexDO>()
//                        .likeIfPresent(BusIndexDO::getDevKey,pageReqVO.getDevKey()).inIfPresent(BusIndexDO::getIpAddr,ipAddrList));
//            }else{
//                busIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<BusIndexDO>()
//                        .likeIfPresent(BusIndexDO::getDevKey,pageReqVO.getDevKey()));
//            }

            busIndexPageResult = busIndexMapper.selectPage(pageReqVO);

            List<BusIndexDO> busIndices = busIndexPageResult.getList();

//            Set<String> ipAddrSet = new HashSet<>();
//            Set<Integer> cascadeAddrSet = new HashSet<>();
//            for (BusIndexDO busIndex : busIndices) {
//                ipAddrSet.add(busIndex.getIpAddr());
//                cascadeAddrSet.add(busIndex.getCascadeAddr());
//            }
//
//            // 批量查询 BusPdu 表
//            List<BusPdu> cabinetPdus = cabinetPduMapper.selectList(new LambdaQueryWrapperX<BusPdu>()
//                    .in(BusPdu::getPduIpA, ipAddrSet).in(BusPdu::getCasIdA, cascadeAddrSet)
//                    .or().in(BusPdu::getPduIpB,ipAddrSet).in(BusPdu::getCasIdB,cascadeAddrSet));
//
//            // 将查询结果按 ipAddr 和 cascadeAddr 分组
//            Map<String, List<BusPdu>> cabinetPduAMap = cabinetPdus.stream()
//                    .filter(cabinetPdu -> ipAddrSet.contains(cabinetPdu.getPduIpA()) && cascadeAddrSet.contains(cabinetPdu.getCasIdA()))
//                    .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduIpA() + "-" + cabinetPdu.getCasIdA()));
//
//            Map<String, List<BusPdu>> cabinetPduBMap = cabinetPdus.stream()
//                    .filter(cabinetPdu -> ipAddrSet.contains(cabinetPdu.getPduIpB()) && cascadeAddrSet.contains(cabinetPdu.getCasIdB()))
//                    .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduIpB() + "-" + cabinetPdu.getCasIdB()));

            if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            }
            List<Integer> ids = busIndices.stream().map(BusIndexDO::getId).collect(Collectors.toList());
            List<String> esCurMaxResult = null;
            List<String> esPowMaxResult = null;
            Map<Integer,Map<Integer, BusLineHourDo>> curMap = new HashMap<>();
            Map<Integer,Map<Integer,BusLineHourDo>> powMap = new HashMap<>();
            String index = null;
            if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "bus_hda_line_hour";
            } else {
                index = "bus_hda_line_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            esCurMaxResult = getBusLineData(startTime,endTime,ids,index,"cur_max_value");
            esPowMaxResult = getBusLineData(startTime,endTime,ids,index,"pow_active_max_value");
            if (!CollectionUtils.isEmpty(esCurMaxResult)){
                esCurMaxResult.forEach(str -> {
                    BusLineHourDo hourDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                    curMap.computeIfAbsent(hourDo.getBusId(), k -> new HashMap<>()).put(hourDo.getLineId(), hourDo);
                });
            }

            if (!CollectionUtils.isEmpty(esPowMaxResult)){
                esPowMaxResult.forEach(str -> {
                    BusLineHourDo hourDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                    powMap.computeIfAbsent(hourDo.getBusId(), k -> new HashMap<>()).put(hourDo.getLineId(), hourDo);
                });
            }

            for (BusIndexDO busIndex : busIndices) {
                Integer id = busIndex.getId().intValue();
                if (curMap.get(id) == null){
                    continue;
                }
                BusLineRes busLineRes = new BusLineRes();
                busLineRes.setStatus(busIndex.getRunStatus());
                
                busLineRes.setBusId(busIndex.getId());
                busLineRes.setDevKey(busIndex.getDevKey());

                BusLineHourDo curl1 = curMap.get(id).get(1);
                busLineRes.setL1MaxCur(curl1.getCurMaxValue());
                busLineRes.setL1MaxCurTime(curl1.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                BusLineHourDo curl2 = curMap.get(id).get(2);
                if(curl2 != null){
                    busLineRes.setL2MaxCur(curl2.getCurMaxValue());
                    busLineRes.setL2MaxCurTime(curl2.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                BusLineHourDo curl3 = curMap.get(id).get(3);
                if(curl3 != null){
                    busLineRes.setL3MaxCur(curl3.getCurMaxValue());
                    busLineRes.setL3MaxCurTime(curl3.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                BusLineHourDo powl1 = powMap.get(id).get(1);
                busLineRes.setL1MaxPow(powl1.getPowActiveMaxValue());
                busLineRes.setL1MaxPowTime(powl1.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                BusLineHourDo powl2 = powMap.get(id).get(2);
                if(powl2 != null) {
                    busLineRes.setL2MaxPow(powl2.getPowActiveMaxValue());
                    busLineRes.setL2MaxPowTime(powl2.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                BusLineHourDo powl3 = powMap.get(id).get(3);
                if(powl3 != null) {
                    busLineRes.setL3MaxPow(powl3.getPowActiveMaxValue());
                    busLineRes.setL3MaxPowTime(powl3.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                result.add(busLineRes);
            }
            return new PageResult<BusLineRes>(result,busIndexPageResult.getTotal());

        }catch (Exception e){
            log.error("获取数据失败：", e);
        }


        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public Map getBusTemDetail(BusIndexPageReqVO pageReqVO) {
        try {
            pageReqVO.setNewTime(pageReqVO.getOldTime().withHour(23).withMinute(59).withSecond(59));
            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(pageReqVO.getBusId());
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<String> busTemHour = getData(startTime,endTime, ids, "bus_tem_hour");
            List<BusTemHourDo> strList = busTemHour.stream()
                    .map(str -> JsonUtils.parseObject(str, BusTemHourDo.class))
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
            HashMap<String, Object> resultMap = new HashMap<>();
            resultMap.put("chart", result);
            resultMap.put("table", tableList);
            return resultMap;
        }catch (Exception e){
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
            List<String> busHdaLineHour = getData(startTime,endTime, ids, "bus_hda_line_hour");
            List<BusLineHourDo> strList = busHdaLineHour.stream()
                    .map(str -> JsonUtils.parseObject(str, BusLineHourDo.class))
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

            if (strList == null || strList.size() == 0){
                return resultMap;
            }

            Map<Integer, List<BusLineHourDo>> pfMap = strList.stream().collect(Collectors.groupingBy(busLineHourDo -> busLineHourDo.getLineId()));

            int i = 0;
            for (BusLineHourDo busLineHourDo : pfMap.get(1)) {
                BusPFTableRes busPFTableRes = new BusPFTableRes();
                result.getPowerFactorAvgValueA().add(busLineHourDo.getPowerFactorAvgValue());
                result.getTime().add(busLineHourDo.getCreateTime().toString("HH:mm"));
                busPFTableRes.setPowerFactorAvgValueA(busLineHourDo.getPowerFactorAvgValue());
                busPFTableRes.setTime(busLineHourDo.getCreateTime().toString("HH:mm"));
                tableList.add(busPFTableRes);
                i++;
            }
            int j = 0;
            for (BusLineHourDo busLineHourDo : pfMap.get(2)) {
                result.getPowerFactorAvgValueB().add(busLineHourDo.getPowerFactorAvgValue());
                if (i == 0 || j >= i){
                    continue;
                }else if(j < i){
                    tableList.get(j).setPowerFactorAvgValueB(busLineHourDo.getPowerFactorAvgValue());
                    j++;
                }
            }
            j= 0;
            for (BusLineHourDo busLineHourDo : pfMap.get(3)) {
                result.getPowerFactorAvgValueC().add(busLineHourDo.getPowerFactorAvgValue());
                if (i == 0 || j >= i){
                    continue;
                }else if(j < i){
                    tableList.get(j).setPowerFactorAvgValueC(busLineHourDo.getPowerFactorAvgValue());
                    j++;
                }
            }



            return resultMap;
        }catch (Exception e){
            log.error("获取数据失败：", e);
        }
        return null;
    }

    @Override
    public BusHarmonicRedisRes getHarmonicRedis(BusIndexPageReqVO pageReqVO) {
        Integer harmonicType = 0;
        BusHarmonicRedisRes result = new BusHarmonicRedisRes();
        if(pageReqVO.getHarmonicType() > 2){
            harmonicType = pageReqVO.getHarmonicType() - 3;
        }else{
            harmonicType = pageReqVO.getHarmonicType();
        }
        ValueOperations ops = redisTemplate.opsForValue();

        JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + pageReqVO.getDevKey());
        if (jsonObject == null){
            return result;
        }
        JSONArray jsonArray = null;
        if (pageReqVO.getHarmonicType() > 2){
            jsonArray = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list").getJSONArray("cur_thd");
        }else{
            jsonArray = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list").getJSONArray("vol_thd");
        }
        List<Float> harmoicList = new ArrayList<>();
        List<Integer> times = new ArrayList<>();
        for (int i = 1; i < 33; i++) {
            times.add(i);
        }
        for (int i = harmonicType; i < jsonArray.size(); i+=3) {
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
            Integer lineId = 0;
            if (pageReqVO.getHarmonicType() == 0 || pageReqVO.getHarmonicType() == 3){
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
            List<String> busHdaLineHour = getBusHarmonicData(startTime, endTime, ids, lines,"bus_hda_line_realtime");
            busHdaLineHour.forEach(str ->{
                BusLineHourDo busLineHourDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                result.getTime().add(busLineHourDo.getCreateTime().toString("HH:mm"));
                if(pageReqVO.getHarmonicType() < 3){
                    float[] volThd = busLineHourDo.getVolThd();
                    for (int i = 0; i < volThd.length; i++) {
                        LineSeries lineSeries = result.getSeries().get(i + 1);
                        lineSeries.setName( (i+1) + "次谐波");
                        lineSeries.getData().add(volThd[i]);
                    }
                }else{
                    float[] curThd = busLineHourDo.getCurThd();
                    for (int i = 0; i < curThd.length; i++) {
                        LineSeries lineSeries = result.getSeries().get(i + 1);
                        lineSeries.setName( (i+1) + "次谐波");
                        lineSeries.getData().add(curThd[i]);
                    }
                }
            });
            return result;
        } catch (Exception e){
            log.error("获取数据失败",e);
        }

        return result;
    }

    @Override
    public Integer getBusIdByDevKey(String devKey) {
        BusIndexDO busIndexDO = busIndexMapper.selectOne(BusIndexDO::getDevKey, devKey);
        if (busIndexDO == null){
            return null;
        }else{
            return busIndexDO.getId();
        }
    }


    public static final String HOUR_FORMAT = "yyyy-MM-dd HH";

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
            List<String> yesterdayData = getData(startTime, endTime, vo, "bus_hda_total_hour");


            List<BusActivePowTrendDTO> yesterdayList = new ArrayList<>();
            yesterdayData.forEach(str -> {
                BusTotalHourDo hourDo = JsonUtils.parseObject(str, BusTotalHourDo.class);
                BusActivePowTrendDTO dto = new BusActivePowTrendDTO();
                dto.setActivePow(hourDo.getPowActiveAvgValue());
                String dateTime = hourDo.getCreateTime().toString(HOUR_FORMAT) + TIME_STR;
                dto.setDateTime(dateTime);
//                log.info("dateTime : " + dateTime );
                yesterdayList.add(dto);
            });


            startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            endTime = DateUtil.formatDateTime(DateTime.now());

            log.info("startTime : " + startTime + "endTime：" + endTime);
            //获取今日数据
            List<BusActivePowTrendDTO> todayList = new ArrayList<>();

            List<String> todayData = getData(startTime, endTime, vo,  "bus_hda_total_hour");
            todayData.forEach(str -> {
                BusTotalHourDo hourDo = JsonUtils.parseObject(str, BusTotalHourDo.class);
                String dateTime = hourDo.getCreateTime().toString(HOUR_FORMAT) + TIME_STR;
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
    /**
     * 获取日环比
     *
     * @param id
     * @param chainDTO
     * @return
     */
    private void getDayChain(int id, BusEleChainDTO chainDTO) throws IOException {
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

    }

    /**
     * 获取周环比
     *
     * @param id
     * @param chainDTO
     */
    private void getWeekChain(int id, BusEleChainDTO chainDTO) throws IOException {
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
    }

    /**
     * 获取月环比
     *
     * @param id
     * @param chainDTO
     */
    private void getMonthChain(int id, BusEleChainDTO chainDTO) throws IOException {
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
                BusConstants.BUS_ELE_TOTAL_REALTIME, id);
        BusEleTotalDo startRealtimeDo = getEleData(startTime, endTime,
                SortOrder.ASC,
                BusConstants.BUS_ELE_TOTAL_REALTIME, id);
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
        String startTime = "";
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
        String startTime = "";


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
            builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + BusConstants.KEYWORD)
                            .gte(startTime)
                            .lt(endTime))
                    .must(QueryBuilders.termQuery(BUS_ID, id))));

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
            if (null != sophistsHits.getHits() && sophistsHits.getHits().length>0){
                SearchHit hit = sophistsHits.getHits()[0];
                realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), BusEleTotalDo.class);
            }
            return realtimeDo;
        } catch (Exception e) {
            log.error("获取数据异常：", e);
        }
        return realtimeDo;
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
        builder.size(1000);

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
        builder.size(1000);

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
                .must(QueryBuilders.termsQuery("bus_id", ids))));
        builder.sort(CREATE_TIME + ".keyword", SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1000);

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

    private List<String> getBusHarmonicData(String startTime, String endTime, List<Integer> ids, List<Integer> lines,String index) throws IOException {
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
        builder.size(1000);

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

    private List<String> getBusLineData(String startTime, String endTime, List<Integer> ids, String index, String sort) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("bus_id", ids))));
        builder.sort(sort, SortOrder.DESC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1000);

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

    private String localDateTimeToString(LocalDateTime time){
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
}