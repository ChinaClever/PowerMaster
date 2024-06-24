package cn.iocoder.yudao.module.bus.service.busindex;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.tem.BusTemHourDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusTemDetailRes;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.BusIndexDTO;
import cn.iocoder.yudao.module.bus.dal.dataobject.buscurbalancecolor.BusCurbalanceColorDO;
import cn.iocoder.yudao.module.bus.dal.mysql.buscurbalancecolor.BusCurbalanceColorMapper;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
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
//            if(pageReqVO.getCabinetIds() != null && !pageReqVO.getCabinetIds().isEmpty()) {
//                List<String> ipAddrList = new ArrayList<>();
//                List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>().inIfPresent(CabinetPdu::getCabinetId, pageReqVO.getCabinetIds()));
//                if(cabinetPduList != null && cabinetPduList.size() > 0){
//                    for (CabinetPdu cabinetPdu : cabinetPduList) {
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

            busIndexPageResult = busIndexMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<BusIndexDO>()
                    .likeIfPresent(BusIndexDO::getDevKey,pageReqVO.getDevKey()));

            List<BusIndexDO> busIndices = busIndexPageResult.getList();

//            Set<String> ipAddrSet = new HashSet<>();
//            Set<Integer> cascadeAddrSet = new HashSet<>();
//            for (BusIndexDO busIndex : busIndices) {
//                ipAddrSet.add(busIndex.getIpAddr());
//                cascadeAddrSet.add(busIndex.getCascadeAddr());
//            }
//
//            // 批量查询 CabinetPdu 表
//            List<CabinetPdu> cabinetPdus = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>()
//                    .in(CabinetPdu::getPduIpA, ipAddrSet).in(CabinetPdu::getCasIdA, cascadeAddrSet)
//                    .or().in(CabinetPdu::getPduIpB,ipAddrSet).in(CabinetPdu::getCasIdB,cascadeAddrSet));
//
//            // 将查询结果按 ipAddr 和 cascadeAddr 分组
//            Map<String, List<CabinetPdu>> cabinetPduAMap = cabinetPdus.stream()
//                    .filter(cabinetPdu -> ipAddrSet.contains(cabinetPdu.getPduIpA()) && cascadeAddrSet.contains(cabinetPdu.getCasIdA()))
//                    .collect(Collectors.groupingBy(cabinetPdu -> cabinetPdu.getPduIpA() + "-" + cabinetPdu.getCasIdA()));
//
//            Map<String, List<CabinetPdu>> cabinetPduBMap = cabinetPdus.stream()
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
}