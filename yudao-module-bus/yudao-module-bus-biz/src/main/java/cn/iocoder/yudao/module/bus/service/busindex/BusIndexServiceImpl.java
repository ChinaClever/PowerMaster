package cn.iocoder.yudao.module.bus.service.busindex;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEleTotalDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.line.BusLineRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.tem.BusTemHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.total.BusTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.total.BusTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.total.BusTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.env.CabinetEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleBar;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetBus;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.mapper.AisleBarMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetBusMapper;
import cn.iocoder.yudao.framework.common.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.constant.BusConstants;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.MaxValueAndCreateTime;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.BoxHarmonicRes;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.BoxIndexPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusTemDetailRes;
import cn.iocoder.yudao.module.bus.dal.dataobject.buscurbalancecolor.BusCurbalanceColorDO;
import cn.iocoder.yudao.module.bus.dal.mysql.buscurbalancecolor.BusCurbalanceColorMapper;
import cn.iocoder.yudao.module.bus.util.TimeUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;

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
import javax.annotation.Resource;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
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

import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.constant.FieldConstant.REDIS_KEY_AISLE;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.constant.BoxConstants.SPLIT_KEY_BUS;
import static cn.iocoder.yudao.module.bus.constant.BusConstants.*;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.*;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
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
        List redisList = getMutiRedis(list);

        for (BusIndexDO busIndexDO : list) {
            BusIndexRes busIndexRes = new BusIndexRes();
            busIndexRes.setStatus(busIndexDO.getRunStatus());
            busIndexRes.setBusId(busIndexDO.getId());
            busIndexRes.setDevKey(busIndexDO.getDevKey());
            busIndexRes.setBusName(busIndexDO.getBusName());
            res.add(busIndexRes);
        }
        Map<String, BusIndexRes> resMap = res.stream().collect(Collectors.toMap(BusIndexRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));

            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id") + '-' + jsonObject.getString("addr");
            BusIndexRes busIndexRes = resMap.get(devKey);
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
            Double biggest = rateList.get(0) ;
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
                    res.removeIf(bus -> bus.getBusId().equals(busIndexRes.getBusId()));
                }
            }
        }
        return new PageResult<>(res,busIndexDOPageResult.getTotal());
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
            busRedisDataRes.setDevKey(busIndexDO.getDevKey());
            busRedisDataRes.setBusName(busIndexDO.getBusName());
            res.add(busRedisDataRes);
        }
        Map<String, BusRedisDataRes> resMap = res.stream().collect(Collectors.toMap(BusRedisDataRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id") + '-' + jsonObject.getString("addr");
            BusRedisDataRes busRedisDataRes = resMap.get(devKey);
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray volValue = lineItemList.getJSONArray("vol_value");
            JSONArray volStatus = lineItemList.getJSONArray("vol_status");
            JSONArray curValue = lineItemList.getJSONArray("cur_value");
            JSONArray curStatus = lineItemList.getJSONArray("cur_status");
            JSONArray powValue = lineItemList.getJSONArray("pow_value");
            JSONArray powStatus = lineItemList.getJSONArray("pow_status");
            JSONArray powReactive = lineItemList.getJSONArray("pow_reactive");
            busRedisDataRes.setDataUpdateTime(jsonObject.getString("sys_time"));
            busRedisDataRes.setPowApparent(jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDouble("pow_apparent"));
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
                BusIndexDTO res = new BusIndexDTO().setId(busIndexDO.getId()).setRunStatus(busIndexDO.getRunStatus());
                res.setDevKey(busIndexDO.getDevKey()).setBusName(busIndexDO.getBusName());

                result.add(res);
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
            getPosition(result);
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
        List redisList = getMutiRedis(list);
        for (BusIndexDO busIndexDO : list) {
            BusBalanceDataRes busBalanceDataRes = new BusBalanceDataRes();
            busBalanceDataRes.setStatus(busIndexDO.getRunStatus());
            busBalanceDataRes.setBusId(busIndexDO.getId());
            busBalanceDataRes.setDevKey(busIndexDO.getDevKey());
            busBalanceDataRes.setBusName(busIndexDO.getBusName());
            res.add(busBalanceDataRes);
        }
        Map<String, BusBalanceDataRes> resMap = res.stream().collect(Collectors.toMap(BusBalanceDataRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id") + '-' + jsonObject.getString("addr");
            BusBalanceDataRes busBalanceDataRes = resMap.get(devKey);
            JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
            JSONArray volValue = lineItemList.getJSONArray("vol_value");
            JSONArray curValue = lineItemList.getJSONArray("cur_value");
            JSONObject busTotalData = jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data");
            JSONArray curAlarmArr = lineItemList.getJSONArray("cur_max");
            List<Double> sortAlarmArr = curAlarmArr.toList(Double.class);
            sortAlarmArr.sort(Collections.reverseOrder());
            double maxVal = sortAlarmArr.get(0);
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
            if(pageReqVO.getColor() != null){
                if(!pageReqVO.getColor().contains(busBalanceDataRes.getColor())){
                    res.removeIf(bus -> bus.getBusId().equals(busBalanceDataRes.getBusId()));
                }
            }
        }
        return new PageResult<>(res,busIndexDOPageResult.getTotal());
    }

    @Override
    public BusBalanceDeatilRes getBusBalanceDetail(String devKey) {
        BusBalanceDeatilRes result = new BusBalanceDeatilRes();
        BusCurbalanceColorDO busCurbalanceColorDO = busCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + devKey);
        if (jsonObject == null){
            return result;
        }
        JSONObject busTotalData = jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data");
        JSONObject lineItemList = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list");
        JSONArray curValue = lineItemList.getJSONArray("cur_value");
        JSONArray volValue = lineItemList.getJSONArray("vol_value");
        Double curUnbalance = busTotalData.getDouble("cur_unbalance");
        Double volUnbalance = busTotalData.getDouble("vol_unbalance");
        result.setCur_value(curValue.toArray(Float.class));
        result.setVol_value(volValue.toArray(Float.class));
        result.setCurUnbalance(curUnbalance);
        result.setVolUnbalance(volUnbalance);
        JSONArray curAlarmArr = lineItemList.getJSONArray("cur_max");
        List<Double> curAlarmArrList = curAlarmArr.toList(Double.class);
        curAlarmArrList.sort(Collections.reverseOrder());
        double maxVal = curAlarmArrList.get(0);
        List<Double> temp = curValue.toList(Double.class);
        temp.sort(Collections.reverseOrder());
        double a = temp.get(0) - temp.get(2);
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
    public List<BusTrendDTO> getBusBalanceTrend(Integer busId) {
        List<BusTrendDTO> result = new ArrayList<>();
        try {
            DateTime end = DateTime.now();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            DateTime start = DateTime.of(calendar.getTime());

            String startTime = DateUtil.formatDateTime(start);
            String endTime = DateUtil.formatDateTime(end);
            List<Integer> ids = Arrays.asList(busId);
            List<String> data = getData(startTime, endTime, ids, BUS_HDA_LINE_HOUR);
            Map<String,List<BusLineHourDo>> timeBus = new HashMap<>();
            data.forEach(str -> {
                BusLineHourDo hourDo = JsonUtils.parseObject(str, BusLineHourDo.class);

                String dateTime  = DateUtil.format(hourDo.getCreateTime(), "yyyy-MM-dd HH") ;
                List<BusLineHourDo> lineHourDos = timeBus.get(dateTime);
                if (CollectionUtils.isEmpty(lineHourDos)) {
                    lineHourDos = new ArrayList<>();
                }
                lineHourDos.add(hourDo);
                timeBus.put(dateTime,lineHourDos);
            });

            timeBus.keySet().forEach(dateTime -> {
                //获取每个时间段数据
                List<BusLineHourDo> busLineHourDos = timeBus.get(dateTime);

                BusTrendDTO trendDTO = new BusTrendDTO();
                trendDTO.setDateTime(dateTime);
                //获取相数据
                List<Map<String,Object>> cur = new ArrayList<>();
                List<Map<String,Object>> vol = new ArrayList<>();
                busLineHourDos.forEach(hourDo -> {
                    Map<String,Object> curMap = new HashMap<>();
                    curMap.put("lineId",hourDo.getLineId());
                    curMap.put("curValue",hourDo.getCurAvgValue());
                    Map<String,Object> volMap = new HashMap<>();
                    volMap.put("lineId",hourDo.getLineId());
                    volMap.put("volValue",hourDo.getVolAvgValue());
                    cur.add(curMap);
                    vol.add(volMap);
                });
                trendDTO.setCur(cur);
                trendDTO.setVol(vol);

                result.add(trendDTO);
            });
            return result.stream().sorted(Comparator.comparing(BusTrendDTO::getDateTime)).collect(Collectors.toList());
        } catch (Exception e){
            log.error("获取数据失败",e);
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
            busTemRes.setDevKey(busIndexDO.getDevKey());
            busTemRes.setBusName(busIndexDO.getBusName());
            res.add(busTemRes);
        }
        Map<String, BusTemRes> resMap = res.stream().collect(Collectors.toMap(BusTemRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id") + '-' + jsonObject.getString("addr");
            BusTemRes busTemRes = resMap.get(devKey);
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
        List redisList = getMutiRedis(list);

        for (BusIndexDO busIndexDO : list) {
            BusPFRes busPFRes = new BusPFRes();
            busPFRes.setStatus(busIndexDO.getRunStatus());
            busPFRes.setBusId(busIndexDO.getId());
            busPFRes.setDevKey(busIndexDO.getDevKey());
            busPFRes.setBusName(busIndexDO.getBusName());
            res.add(busPFRes);
        }
        Map<String, BusPFRes> resMap = res.stream().collect(Collectors.toMap(BusPFRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id") + '-' + jsonObject.getString("addr");
            BusPFRes busPFRes = resMap.get(devKey);
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
        List redisList = getMutiRedis(list);
        for (BusIndexDO busIndexDO : list) {
            BusHarmonicRes busHarmonicRes = new BusHarmonicRes();
            busHarmonicRes.setStatus(busIndexDO.getRunStatus());
            busHarmonicRes.setDevKey(busIndexDO.getDevKey());
            busHarmonicRes.setBusId(busIndexDO.getId());
            busHarmonicRes.setBusName(busIndexDO.getBusName());
            res.add(busHarmonicRes);
        }
        Map<String, BusHarmonicRes> resMap = res.stream().collect(Collectors.toMap(BusHarmonicRes::getDevKey, Function.identity()));
        getPosition(res);
        for (Object o : redisList) {
            if (Objects.isNull(o)){
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(o));
            String devKey = jsonObject.getString("dev_ip") + '-' + jsonObject.getString("bar_id") + '-' + jsonObject.getString("addr");
            BusHarmonicRes busHarmonicRes = resMap.get(devKey);
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
            List<BusIndexDO> searchList = busIndexMapper.selectList(new LambdaQueryWrapperX<BusIndexDO>().inIfPresent(BusIndexDO::getDevKey, pageReqVO.getBusDevKeyList()));

            if(CollectionUtils.isEmpty(searchList)){
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            }

            Map<Integer,Map<Integer, MaxValueAndCreateTime>> curMap ;
            Map<Integer,Map<Integer, MaxValueAndCreateTime>> powMap ;
            String index = null;
            if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "bus_hda_line_hour";
            } else {
                index = "bus_hda_line_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            Map esTotalAndIds = getESTotalAndIds(index, startTime, endTime,pageReqVO.getPageSize(), pageReqVO.getPageNo() - 1,searchList.stream().map(BusIndexDO::getId).collect(Collectors.toList()));

            Long total = (Long)esTotalAndIds.get("total");
            if(total == 0){
                return new PageResult<>(new ArrayList<>(), 0L);
            }
            List<Integer> ids = (List<Integer>) esTotalAndIds.get("ids");
            curMap = getBusLineCurMaxData(startTime,endTime,ids,index);
            powMap = getBusLinePowMaxData(startTime,endTime,ids,index);

            List<BusLineRes> result = new ArrayList<>();

            List<BusIndexDO> busIndices = busIndexMapper.selectList(new LambdaQueryWrapperX<BusIndexDO>()
                    .inIfPresent(BusIndexDO::getId,ids));

            for (BusIndexDO busIndex : busIndices) {
                Integer id = busIndex.getId().intValue();
                if (curMap.get(id) == null){
                    continue;
                }
                BusLineRes busLineRes = new BusLineRes();
                busLineRes.setStatus(busIndex.getRunStatus());
                
                busLineRes.setBusId(busIndex.getId());
                busLineRes.setDevKey(busIndex.getDevKey());
                busLineRes.setBusName(busIndex.getBusName());

                MaxValueAndCreateTime curl1 = curMap.get(id).get(1);
                busLineRes.setL1MaxCur(curl1.getMaxValue().floatValue());
                busLineRes.setL1MaxCurTime(curl1.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                MaxValueAndCreateTime curl2 = curMap.get(id).get(2);
                if(curl2 != null){
                    busLineRes.setL2MaxCur(curl2.getMaxValue().floatValue());
                    busLineRes.setL2MaxCurTime(curl2.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                MaxValueAndCreateTime curl3 = curMap.get(id).get(3);
                if(curl3 != null){
                    busLineRes.setL3MaxCur(curl3.getMaxValue().floatValue());
                    busLineRes.setL3MaxCurTime(curl3.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                MaxValueAndCreateTime powl1 = powMap.get(id).get(1);
                busLineRes.setL1MaxPow(powl1.getMaxValue().floatValue());
                busLineRes.setL1MaxPowTime(powl1.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                MaxValueAndCreateTime powl2 = powMap.get(id).get(2);
                if(powl2 != null) {
                    busLineRes.setL2MaxPow(powl2.getMaxValue().floatValue());
                    busLineRes.setL2MaxPowTime(powl2.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                MaxValueAndCreateTime powl3 = powMap.get(id).get(3);
                if(powl3 != null) {
                    busLineRes.setL3MaxPow(powl3.getMaxValue().floatValue());
                    busLineRes.setL3MaxPowTime(powl3.getMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                result.add(busLineRes);
            }
            if(!CollectionUtils.isEmpty(result)){
                getPosition(result);
            }

            return new PageResult<>(result, total);
        }catch (Exception e){
            log.error("获取数据失败：", e);
        }


        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public BusLineResBase getBusLineCurLine(BusIndexPageReqVO pageReqVO) {
        BusLineResBase result = new BusLineResBase();
        try {
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            String index = null;
            if (pageReqVO.getTimeType() == 0){
                index = BUS_HDA_LINE_HOUR;
                startTime = localDateTimeToString(LocalDateTime.now().minusHours(24));
                endTime = localDateTimeToString(LocalDateTime.now());
            }else {
                index = BUS_HDA_LINE_DAY;
            }
            List<Integer> ids = Arrays.asList(pageReqVO.getBusId());
            List<String> data = getData(startTime,endTime,ids,index);

            if (pageReqVO.getLineType() == 0){
                result.getSeries().add(new RequirementLineSeries().setName("A路最大电流"));
                result.getSeries().add(new RequirementLineSeries().setName("B路最大电流"));
                result.getSeries().add(new RequirementLineSeries().setName("C路最大电流"));
                data.forEach(str -> {
                    BusLineHourDo lineDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                    if(lineDo.getLineId() == 1){
                        result.getTime().add(lineDo.getCurMaxTime().toString("yyyy-MM-dd HH"));
                    }
                    result.getSeries().get(lineDo.getLineId() - 1).getData().add(lineDo.getCurMaxValue());
                    ((RequirementLineSeries)result.getSeries().get(lineDo.getLineId() - 1)).getMaxTime().add(lineDo.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                });
            }else{
                result.getSeries().add(new RequirementLineSeries().setName("A路最大功率"));
                result.getSeries().add(new RequirementLineSeries().setName("B路最大功率"));
                result.getSeries().add(new RequirementLineSeries().setName("C路最大功率"));
                data.forEach(str -> {
                    BusLineHourDo lineDo = JsonUtils.parseObject(str, BusLineHourDo.class);
                    if(lineDo.getLineId() == 1){
                        result.getTime().add(lineDo.getPowActiveMaxTime().toString("yyyy-MM-dd HH"));
                    }
                    result.getSeries().get(lineDo.getLineId() - 1).getData().add(lineDo.getPowActiveMaxValue());
                    ((RequirementLineSeries)result.getSeries().get(lineDo.getLineId() - 1)).getMaxTime().add(lineDo.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                });
            }
            return result;
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public PowerRedisDataRes getBusPowerRedisData(String devKey) {
        PowerRedisDataRes result = new PowerRedisDataRes();
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + devKey);
        if (jsonObject == null){
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
        JSONArray volMax = lineItemList.getJSONArray("vol_max");
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
        }else {
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
        result.setLoadFactor((result.getS() / result.getFInstalledCapacity())*100);
        return result;
    }

    @Override
    public BusLineResBase getBusLoadRateLine(BusIndexPageReqVO pageReqVO) {
        if (!pageReqVO.getTimeGranularity().equals("今天")){
            return null;
        }
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        try {
            List<Integer> ids = Arrays.asList(pageReqVO.getBusId());
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            List<String> busHdaLineRealtime = getData(startTime, endTime, ids, "bus_hda_line_realtime");
            Map<Integer, List<BusLineRealtimeDo>> lineMap = busHdaLineRealtime.stream()
                    .map(str -> JsonUtils.parseObject(str, BusLineRealtimeDo.class))
                    .collect(Collectors.groupingBy(BusLineRealtimeDo::getLineId));
            boolean first = false;
            for (int i = 1; i < 4; i++) {
                if(lineMap.get(i) != null){
                    List<BusLineRealtimeDo> busLineRealtimeDos = lineMap.get(i);
                    List<Float> loadRate = busLineRealtimeDos.stream().map(BusLineRealtimeDo::getLoadRate).collect(Collectors.toList());
                    LineSeries lineSeries = new LineSeries();
                    if(!first){
                        List<String> time = busLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                        result.setTime(time);
                    }
                    if(i == 1){
                        lineSeries.setName("A相负载率");
                    }else if (i == 2){
                        lineSeries.setName("B相负载率");
                    }else{
                        lineSeries.setName("C相负载率");
                    }
                    lineSeries.setData(loadRate);
                    result.getSeries().add(lineSeries);
                }
            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public BusLineResBase getBusPowActiveLine(BusIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        try {
            List<Integer> ids = Arrays.asList(pageReqVO.getBusId());
            switch (pageReqVO.getTimeGranularity()) {
                case "今天": {
                    String startTime = localDateTimeToString(pageReqVO.getOldTime());
                    String endTime = localDateTimeToString(pageReqVO.getNewTime());
                    List<String> busHdaLineRealtime = getData(startTime, endTime, ids, "bus_hda_line_realtime");
                    List<String> busHdaTotalRealtime = getData(startTime, endTime, ids, "bus_hda_total_realtime");
                    LineSeries lineSeries = new LineSeries();
                    lineSeries.setName("P");
                    busHdaTotalRealtime.forEach( str ->{
                        BusTotalRealtimeDo esDo = JsonUtils.parseObject(str, BusTotalRealtimeDo.class);
                        lineSeries.getData().add(esDo.getPowActive());
                    });
                    result.getSeries().add(lineSeries);
                    Map<Integer, List<BusLineRealtimeDo>> lineMap = busHdaLineRealtime.stream()
                            .map(str -> JsonUtils.parseObject(str, BusLineRealtimeDo.class))
                            .collect(Collectors.groupingBy(BusLineRealtimeDo::getLineId));
                    boolean first = false;
                    for (int i = 1; i < 4; i++) {
                        if(lineMap.get(i) != null){
                            List<BusLineRealtimeDo> busLineRealtimeDos = lineMap.get(i);
                            List<Float> powActive = busLineRealtimeDos.stream().map(BusLineRealtimeDo::getPowActive).collect(Collectors.toList());
                            LineSeries series = new LineSeries();
                            if(!first){
                                List<String> time = busLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                                result.setTime(time);
                            }
                            if(i == 1){
                                series.setName("Pa");
                            }else if (i == 2){
                                series.setName("Pb");
                            }else{
                                series.setName("Pc");
                            }
                            series.setData(powActive);
                            result.getSeries().add(series);
                        }
                    }
                    break;
                }
                case "近一天": {
                    String startTime = localDateTimeToString(LocalDateTime.now().minusDays(1));
                    String endTime = localDateTimeToString(LocalDateTime.now());
                    List<String> busHdaLineHour = getData(startTime, endTime, ids, "bus_hda_line_hour");
                    List<String> busHdaTotalHour = getData(startTime, endTime, ids, "bus_hda_total_hour");

                    LineSeries lineSeries = new LineSeries();
                    lineSeries.setName("P");
                    busHdaTotalHour.forEach( str ->{
                        BusTotalHourDo esDo = JsonUtils.parseObject(str, BusTotalHourDo.class);
                        lineSeries.getData().add(esDo.getPowActiveAvgValue());
                    });
                    result.getSeries().add(lineSeries);
                    Map<Integer, List<BusLineHourDo>> lineMap = busHdaLineHour.stream()
                            .map(str -> JsonUtils.parseObject(str, BusLineHourDo.class))
                            .collect(Collectors.groupingBy(BusLineHourDo::getLineId));
                    boolean first = false;
                    for (int i = 1; i < 4; i++) {
                        if(lineMap.get(i) != null){
                            List<BusLineHourDo> busLineHourDos = lineMap.get(i);
                            List<Float> powActive = busLineHourDos.stream().map(BusLineHourDo::getPowActiveAvgValue).collect(Collectors.toList());
                            LineSeries series = new LineSeries();
                            if(!first){
                                List<String> time = busLineHourDos.stream().map(hour -> hour.getCreateTime().toString("MM:dd HH:mm")).collect(Collectors.toList());
                                result.setTime(time);
                            }
                            if(i == 1){
                                series.setName("Pa");
                            }else if (i == 2){
                                series.setName("Pb");
                            }else{
                                series.setName("Pc");
                            }
                            series.setData(powActive);
                            result.getSeries().add(series);
                        }
                    }
                    break;
                }
                case "近三天": {
                    String startTime = localDateTimeToString(LocalDateTime.now().minusDays(3));
                    String endTime = localDateTimeToString(LocalDateTime.now());
                    List<String> busHdaLineDay = getData(startTime, endTime, ids, "bus_hda_line_day");
                    List<String> busHdaTotalDay = getData(startTime, endTime, ids, "bus_hda_total_day");

                    LineSeries lineSeries = new LineSeries();
                    lineSeries.setName("P");
                    busHdaTotalDay.forEach( str ->{
                        BusTotalDayDo esDo = JsonUtils.parseObject(str, BusTotalDayDo.class);
                        lineSeries.getData().add(esDo.getPowActiveAvgValue());
                    });
                    result.getSeries().add(lineSeries);
                    Map<Integer, List<BusLineDayDo>> lineMap = busHdaLineDay.stream()
                            .map(str -> JsonUtils.parseObject(str, BusLineDayDo.class))
                            .collect(Collectors.groupingBy(BusLineDayDo::getLineId));
                    boolean first = false;
                    for (int i = 1; i < 4; i++) {
                        if(lineMap.get(i) != null){
                            List<BusLineDayDo> busLineDayDos = lineMap.get(i);
                            List<Float> powActive = busLineDayDos.stream().map(BusLineDayDo::getPowActiveAvgValue).collect(Collectors.toList());
                            LineSeries series = new LineSeries();
                            if(!first){
                                List<String> time = busLineDayDos.stream().map(hour -> hour.getCreateTime().toString("MM:dd HH:mm")).collect(Collectors.toList());
                                result.setTime(time);
                            }
                            if(i == 1){
                                series.setName("Pa");
                            }else if (i == 2){
                                series.setName("Pb");
                            }else{
                                series.setName("Pc");
                            }
                            series.setData(powActive);
                            result.getSeries().add(series);
                        }
                    }
                    break;
                }
            }

        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public BusLineResBase getBusPowReactiveLine(BusIndexPageReqVO pageReqVO) {
        BusHarmonicLineRes result = new BusHarmonicLineRes();
        try {
            List<Integer> ids = Arrays.asList(pageReqVO.getBusId());
            switch (pageReqVO.getTimeGranularity()) {
                case "今天": {
                    String startTime = localDateTimeToString(pageReqVO.getOldTime());
                    String endTime = localDateTimeToString(pageReqVO.getNewTime());
                    List<String> busHdaLineRealtime = getData(startTime, endTime, ids, "bus_hda_line_realtime");
                    List<String> busHdaTotalRealtime = getData(startTime, endTime, ids, "bus_hda_total_realtime");

                    LineSeries lineSeries = new LineSeries();
                    lineSeries.setName("Q");
                    busHdaTotalRealtime.forEach( str ->{
                        BusTotalRealtimeDo esDo = JsonUtils.parseObject(str, BusTotalRealtimeDo.class);
                        lineSeries.getData().add(esDo.getPowReactive());
                    });
                    result.getSeries().add(lineSeries);
                    Map<Integer, List<BusLineRealtimeDo>> lineMap = busHdaLineRealtime.stream()
                            .map(str -> JsonUtils.parseObject(str, BusLineRealtimeDo.class))
                            .collect(Collectors.groupingBy(BusLineRealtimeDo::getLineId));
                    boolean first = false;
                    for (int i = 1; i < 4; i++) {
                        if(lineMap.get(i) != null){
                            List<BusLineRealtimeDo> busLineRealtimeDos = lineMap.get(i);
                            List<Float> powReactive = busLineRealtimeDos.stream().map(BusLineRealtimeDo::getPowReactive).collect(Collectors.toList());
                            LineSeries series = new LineSeries();
                            if(!first){
                                List<String> time = busLineRealtimeDos.stream().map(hour -> hour.getCreateTime().toString("HH:mm")).collect(Collectors.toList());
                                result.setTime(time);
                            }
                            if(i == 1){
                                series.setName("Qa");
                            }else if (i == 2){
                                series.setName("Qb");
                            }else{
                                series.setName("Qc");
                            }
                            series.setData(powReactive);
                            result.getSeries().add(series);
                        }
                    }
                    break;
                }
                case "近一天": {
                    String startTime = localDateTimeToString(LocalDateTime.now().minusDays(1));
                    String endTime = localDateTimeToString(LocalDateTime.now());
                    List<String> busHdaLineHour = getData(startTime, endTime, ids, "bus_hda_line_hour");
                    List<String> busHdaTotalHour = getData(startTime, endTime, ids, "bus_hda_total_hour");

                    LineSeries lineSeries = new LineSeries();
                    lineSeries.setName("Q");
                    busHdaTotalHour.forEach( str ->{
                        BusTotalHourDo esDo = JsonUtils.parseObject(str, BusTotalHourDo.class);
                        lineSeries.getData().add(esDo.getPowReactiveAvgValue());
                    });
                    result.getSeries().add(lineSeries);
                    Map<Integer, List<BusLineHourDo>> lineMap = busHdaLineHour.stream()
                            .map(str -> JsonUtils.parseObject(str, BusLineHourDo.class))
                            .collect(Collectors.groupingBy(BusLineHourDo::getLineId));
                    boolean first = false;
                    for (int i = 1; i < 4; i++) {
                        if(lineMap.get(i) != null){
                            List<BusLineHourDo> busLineHourDos = lineMap.get(i);
                            List<Float> powReactive = busLineHourDos.stream().map(BusLineHourDo::getPowReactiveAvgValue).collect(Collectors.toList());
                            LineSeries series = new LineSeries();
                            if(!first){
                                List<String> time = busLineHourDos.stream().map(hour -> hour.getCreateTime().toString("MM:dd HH:mm")).collect(Collectors.toList());
                                result.setTime(time);
                            }
                            if(i == 1){
                                series.setName("Qa");
                            }else if (i == 2){
                                series.setName("Qb");
                            }else{
                                series.setName("Qc");
                            }
                            series.setData(powReactive);
                            result.getSeries().add(series);
                        }
                    }
                    break;
                }
                case "近三天": {
                    String startTime = localDateTimeToString(LocalDateTime.now().minusDays(3));
                    String endTime = localDateTimeToString(LocalDateTime.now());
                    List<String> busHdaLineDay = getData(startTime, endTime, ids, "bus_hda_line_day");
                    List<String> busHdaTotalDay = getData(startTime, endTime, ids, "bus_hda_total_day");

                    LineSeries lineSeries = new LineSeries();
                    lineSeries.setName("Q");
                    busHdaTotalDay.forEach( str ->{
                        BusTotalDayDo esDo = JsonUtils.parseObject(str, BusTotalDayDo.class);
                        lineSeries.getData().add(esDo.getPowReactiveAvgValue());
                    });
                    result.getSeries().add(lineSeries);
                    Map<Integer, List<BusLineDayDo>> lineMap = busHdaLineDay.stream()
                            .map(str -> JsonUtils.parseObject(str, BusLineDayDo.class))
                            .collect(Collectors.groupingBy(BusLineDayDo::getLineId));
                    boolean first = false;
                    for (int i = 1; i < 4; i++) {
                        if(lineMap.get(i) != null){
                            List<BusLineDayDo> busLineDayDos = lineMap.get(i);
                            List<Float> powReactive = busLineDayDos.stream().map(BusLineDayDo::getPowReactiveAvgValue).collect(Collectors.toList());
                            LineSeries series = new LineSeries();
                            if(!first){
                                List<String> time = busLineDayDos.stream().map(hour -> hour.getCreateTime().toString("MM:dd HH:mm")).collect(Collectors.toList());
                                result.setTime(time);
                            }
                            if(i == 1){
                                series.setName("Qa");
                            }else if (i == 2){
                                series.setName("Qb");
                            }else{
                                series.setName("Qc");
                            }
                            series.setData(powReactive);
                            result.getSeries().add(series);
                        }
                    }
                    break;
                }
            }
        }catch (Exception e){
            log.error("获取数据失败",e);
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
                    break;
                }else if(j < i){
                    tableList.get(j).setPowerFactorAvgValueB(busLineHourDo.getPowerFactorAvgValue());
                    j++;
                }
            }
            j= 0;
            for (BusLineHourDo busLineHourDo : pfMap.get(3)) {
                result.getPowerFactorAvgValueC().add(busLineHourDo.getPowerFactorAvgValue());
                if (i == 0 || j >= i){
                    break;
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
                        SeriesBase lineSeries = result.getSeries().get(i + 1);
                        lineSeries.setName( (i+1) + "次谐波");
                        lineSeries.getData().add(volThd[i]);
                    }
                }else{
                    float[] curThd = busLineHourDo.getCurThd();
                    for (int i = 0; i < curThd.length; i++) {
                        SeriesBase lineSeries = result.getSeries().get(i + 1);
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
            List<String> yesterdayData = getData(startTime, endTime, vo, "bus_hda_total_hour");


            List<BusActivePowTrendDTO> yesterdayList = new ArrayList<>();
            yesterdayData.forEach(str -> {
                BusTotalHourDo hourDo = JsonUtils.parseObject(str, BusTotalHourDo.class);
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

            List<String> todayData = getData(startTime, endTime, vo,  "bus_hda_total_hour");
            todayData.forEach(str -> {
                BusTotalHourDo hourDo = JsonUtils.parseObject(str, BusTotalHourDo.class);
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
    public Map getReportConsumeDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase barRes = new BusLineResBase();
        BarSeries barSeries = new BarSeries();
        try {
            BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getDevKey, devKey));
            if(busIndexDO != null) {
                String index = null;
                boolean isSameDay = false;
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
                if (isSameDay){
                    for (String str : cabinetData) {
                        nowTimes++;
                        BusEleTotalDo eleDO = JsonUtils.parseObject(str, BusEleTotalDo.class);
                        if (nowTimes == 1) {
                            firstEq = eleDO.getEleActive();
                        }
                        if (nowTimes > 1){
                            barSeries.getData().add((float)(eleDO.getEleActive() -lastEq));
                            barRes.getTime().add(eleDO.getCreateTime().toString("HH:mm"));
                        }
                        lastEq = eleDO.getEleActive();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Id), index, "ele_active");
                    BusEleTotalDo eleMaxValue = JsonUtils.parseObject(eleMax, BusEleTotalDo.class);
                    if(eleMaxValue != null){
                        maxEle = eleMaxValue.getEleActive();
                        maxEleTime = eleMaxValue.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("totalEle",totalEq);
                    result.put("maxEle",maxEle);
                    result.put("maxEleTime",maxEleTime);
                    result.put("firstEq",firstEq);
                    result.put("lastEq",lastEq);
                    result.put("barRes",barRes);
                }else {
                    for (String str : cabinetData) {
                        nowTimes++;
                        BusEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, BusEqTotalDayDo.class);
                        totalEq += totalDayDo.getEq();
                        barSeries.getData().add((float)totalDayDo.getEq());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Id), index, "eq_value");
                    BusEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, BusEqTotalDayDo.class);
                    if(eqMaxValue != null){
                        maxEle = eqMaxValue.getEq();
                        maxEleTime = eqMaxValue.getStartTime().toString("yyyy-MM-dd HH:mm:ss");
                    }
                    barRes.getSeries().add(barSeries);
                    result.put("totalEle",totalEq);
                    result.put("maxEle",maxEle);
                    result.put("maxEleTime",maxEleTime);
                    result.put("barRes",barRes);
                }
            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getBusPFLine(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase totalLineRes = new BusLineResBase();
        result.put("pfLineRes",totalLineRes);
        try {
            BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getDevKey, devKey));

            if(busIndexDO != null) {
                String index = null;
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

                if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                    powList.forEach(hourdo -> {
                        totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());

                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm"));

                    });
                }else{
                    powList.forEach(hourdo -> {
                        totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));
                    });
                }
                result.put("pfLineRes",totalLineRes);
            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase totalLineRes = new BusLineResBase();
        result.put("totalLineRes",totalLineRes);

        result.put("apparentPowMaxValue",null);
        result.put("apparentPowMaxTime",null);
        result.put("apparentPowMinValue",null);
        result.put("apparentPowMinTime",null);
        result.put("activePowMaxValue", null);
        result.put("activePowMaxTime",  null);
        result.put("activePowMinValue", null);
        result.put("activePowMinTime",  null);
        try {
            BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getDevKey, devKey));

            if(busIndexDO != null) {
                String index = null;
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
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);


                if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getPowApparentAvgValue());
                        totalActivePow.getData().add(hourdo.getPowActiveAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm"));

                    });
                }else{
                    powList.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getPowApparentAvgValue());
                        totalActivePow.getData().add(hourdo.getPowActiveAvgValue());
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

                result.put("totalLineRes",totalLineRes);

                result.put("apparentPowMaxValue",totalMaxApparent.getPowApparentMaxValue());
                result.put("apparentPowMaxTime",totalMaxApparent.getPowApparentMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("apparentPowMinValue",totalMinApparent.getPowApparentMinValue());
                result.put("apparentPowMinTime",totalMinApparent.getPowApparentMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMaxValue",totalMaxActive.getPowActiveMaxValue());
                result.put("activePowMaxTime",totalMaxActive.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMinValue",totalMinActive.getPowActiveMinValue());
                result.put("activePowMinTime",totalMinActive.getPowActiveMinTime().toString("yyyy-MM-dd HH:mm:ss"));

            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        BusLineResBase lineRes = new BusLineResBase();
        try {
            BusIndexDO busIndexDO = busIndexMapper.selectOne(new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getDevKey, devKey));
            if(busIndexDO != null) {
                Integer Id = busIndexDO.getId();
                String index = null;
                boolean isSameDay = false;
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

                List<String> time = null;
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

                if(!isSameDay){
                    time = temList.stream().map(busTemHourDo -> busTemHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss")).collect(Collectors.toList());
                }else{
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
                if(temMaxA != null){
                    result.put("temAMaxValue",temMaxA.getTemAMaxValue());
                    result.put("temAMaxTime",temMaxA.getTemAMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if(temMinA != null) {
                    result.put("temAMinValue", temMinA.getTemAMinValue());
                    result.put("temAMinTime",temMinA.getTemAMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                String temBMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_b_max_value");
                BusTemHourDo temMaxB = JsonUtils.parseObject(temBMaxValue, BusTemHourDo.class);
                String temBMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_b_min_value");
                BusTemHourDo temMinB = JsonUtils.parseObject(temBMinValue, BusTemHourDo.class);
                if(temMaxB != null){
                    result.put("temBMaxValue",temMaxB.getTemBMaxValue());
                    result.put("temBMaxTime",temMaxB.getTemBMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if(temMinB != null) {
                    result.put("temBMinValue", temMinB.getTemBMinValue());
                    result.put("temBMinTime",temMinB.getTemBMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                String temCMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_c_max_value");
                BusTemHourDo temMaxC = JsonUtils.parseObject(temCMaxValue, BusTemHourDo.class);
                String temCMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_c_min_value");
                BusTemHourDo temMinC = JsonUtils.parseObject(temCMinValue, BusTemHourDo.class);
                if(temMaxC != null){
                    result.put("temCMaxValue",temMaxC.getTemCMaxValue());
                    result.put("temCMaxTime",temMaxC.getTemCMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if(temMinC != null) {
                    result.put("temCMinValue", temMinC.getTemCMinValue());
                    result.put("temCMinTime",temMinC.getTemCMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                String temNMaxValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_n_max_value");
                BusTemHourDo temMaxN = JsonUtils.parseObject(temNMaxValue, BusTemHourDo.class);
                String temNMinValue = getMaxData(startTime, endTime, Arrays.asList(Id), index, "tem_n_min_value");
                BusTemHourDo temMinN = JsonUtils.parseObject(temNMinValue, BusTemHourDo.class);
                if(temMaxN != null){
                    result.put("temNMaxValue",temMaxN.getTemNMaxValue());
                    result.put("temNMaxTime",temMaxN.getTemNMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                if(temMinN != null) {
                    result.put("temNMinValue", temMinN.getTemNMinValue());
                    result.put("temNMinTime",temMinN.getTemNMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                result.put("lineRes",lineRes);
                return result;
            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public String getBusRedisByDevKey(String devKey) {
        if (StringUtils.isEmpty(devKey)){
            return null;
        }else {
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

        for (BusIndexDO busIndexDO : list) {
            BusIndexRes busIndexRes = new BusIndexRes();
            busIndexRes.setStatus(busIndexDO.getRunStatus());
            busIndexRes.setBusId(busIndexDO.getId());
            busIndexRes.setDevKey(busIndexDO.getDevKey());
            busIndexRes.setBusName(busIndexDO.getBusName());
            res.add(busIndexRes);
        }
        getPosition(res);
        return new PageResult<>(res,busIndexDOPageResult.getTotal());
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

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index,String order) throws IOException {
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
                String str = hit.getSourceAsString();
                return str;
            }
        }
        return null;
    }

    private String getMinData(String startTime, String endTime, List<Integer> ids, String index,String order) throws IOException {
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
                String str = hit.getSourceAsString();
                return str;
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


    private   Map<Integer, Map<Integer, MaxValueAndCreateTime>> getBusLineCurMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
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
                    maxValueAndCreateTime.setMaxTime(new DateTime(sourceAsMap.get("cur_max_time").toString(),"yyyy-MM-dd HH:mm:ss"));
                }

                // 将valueMap添加到lineIdMap中
                lineIdMap.put(lineId, maxValueAndCreateTime);
            }

            // 将lineIdMap添加到resultMap中
            resultMap.put(boxId, lineIdMap);
        }
        return resultMap;
    }

    private  Map<Integer, Map<Integer, MaxValueAndCreateTime>> getBusLinePowMaxData(String startTime, String endTime, List<Integer> ids, String index) throws IOException {
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

        List<String> list = new ArrayList<>();
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
                    maxValueAndCreateTime.setMaxTime(new DateTime(sourceAsMap.get("pow_active_max_time").toString(),"yyyy-MM-dd HH:mm:ss"));
                }

                // 将valueMap添加到lineIdMap中
                lineIdMap.put(lineId, maxValueAndCreateTime);
            }

            // 将lineIdMap添加到resultMap中
            resultMap.put(boxId, lineIdMap);
        }
        return resultMap;
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

    /**
     * 获取设备位置
     * @return
     */
    public void getPosition(List< ? extends BusResBase> res){
        if(CollectionUtils.isEmpty(res)){
            return;
        }
        ValueOperations ops = redisTemplate.opsForValue();
        List<String> devKeyList = res.stream().map(BusResBase::getDevKey).collect(Collectors.toList());
        //设备位置

        //柜列
        List<AisleBar> aisleBar  = aisleBarMapper.selectList(new LambdaQueryWrapper<AisleBar>()
                .in(!CollectionUtils.isEmpty(devKeyList),AisleBar::getBarKey,devKeyList));
        Map<String, String> aislePathMap = aisleBar.stream().collect(Collectors.toMap(AisleBar::getBarKey, AisleBar::getPath));
        Map<String, Integer> aisleBarKeyMap = aisleBar.stream().collect(Collectors.toMap(AisleBar::getBarKey,AisleBar::getAisleId));
        Map<Integer, String> positonMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(aisleBar)){
            Set<String> redisKeys = aisleBar.stream().map(aisle -> REDIS_KEY_AISLE + aisle.getAisleId()).collect(Collectors.toSet());
            List aisles = ops.multiGet(redisKeys);
            if (!CollectionUtils.isEmpty(aisleBar)){
                for (Object aisle : aisles) {
                    if (aisle == null) {
                        continue;
                    }
                    JSONObject json = JSON.parseObject(JSON.toJSONString(aisle));
                    String devPosition = new String();
                    devPosition = json.getString("room_name") + SPLIT_KEY
                            +  json.getString("aisle_name") + SPLIT_KEY ;
                    positonMap.put(json.getInteger("aisle_key"),devPosition);
                }
            }
        }
        res.forEach( bus ->{
            if (aisleBarKeyMap.get(bus.getDevKey()) != null){
                Integer aisleId = aisleBarKeyMap.get(bus.getDevKey());
                bus.setLocation(positonMap.get(aisleId) + aislePathMap.get(bus.getDevKey()) + "路");
            }
        });
        List<BusResBase> resNotInAisle = res.stream().filter(busRes -> StringUtils.isEmpty(busRes.getLocation())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(resNotInAisle)){
            return;
        }
        for (BusResBase busResBase : resNotInAisle) {
            List<CabinetBus> cabinetBusListA  = cabinetBusMapper.selectList(new LambdaQueryWrapper<CabinetBus>()
                    .like(CabinetBus::getDevKeyA,busResBase.getDevKey()));
            if (!CollectionUtils.isEmpty(cabinetBusListA)){
                CabinetBus cabinetBus = cabinetBusListA.get(0);
                CabinetIndex index = cabinetIndexMapper.selectById(cabinetBus.getCabinetId());
                String cabKey = index.getRoomId() + SPLIT_KEY + index.getId();
                String redisKey = REDIS_KEY_CABINET + cabKey;
                Object cabinet = ops.get(redisKey);
                if (Objects.nonNull(cabinet)){
                    JSONObject json = JSON.parseObject(JSON.toJSONString(cabinet));
                    String devPosition = new String();
                    devPosition = json.getString("room_name") ;
                    busResBase.setLocation(devPosition + SPLIT_KEY + busResBase.getBusName());
                }
            }
            if (busResBase.getLocation() != null){
                continue;
            }

            List<CabinetBus> cabinetBusListB = cabinetBusMapper.selectList(new LambdaQueryWrapper<CabinetBus>()
                    .like(CabinetBus::getDevKeyB,busResBase.getDevKey()));
            if (!CollectionUtils.isEmpty(cabinetBusListB)){
                CabinetBus cabinetBus = cabinetBusListB.get(0);
                CabinetIndex index = cabinetIndexMapper.selectById(cabinetBus.getCabinetId());
                String cabKey = index.getRoomId() + SPLIT_KEY + index.getId();
                String redisKey = REDIS_KEY_CABINET + cabKey;
                Object cabinet = ops.get(redisKey);
                if (Objects.nonNull(cabinet)){
                    JSONObject json = JSON.parseObject(JSON.toJSONString(cabinet));
                    String devPosition = new String();
                    devPosition = json.getString("room_name");
                    busResBase.setLocation(devPosition + SPLIT_KEY + busResBase.getBusName());
                }
            }
        }
    }

    private Map getESTotalAndIds(String index,String startTime,String endTime,Integer pageSize,Integer pageNo) throws IOException {
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
        if (totalSizeAggregation != null){
            totalRes = totalSizeAggregation.getValue();
        }

        result.put("total",totalRes);
        result.put("ids",sortValues);
        return result;
    }

    private Map getESTotalAndIds(String index,String startTime,String endTime,Integer pageSize,Integer pageNo,List<Integer> ids) throws IOException {
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
        if (totalSizeAggregation != null){
            totalRes = totalSizeAggregation.getValue();
        }

        result.put("total",totalRes);
        result.put("ids",sortValues);
        return result;
    }

    private List getMutiRedis(List<BusIndexDO> list){
        List<String> devKeys = list.stream().map(busIndexDo -> REDIS_KEY_BUS + busIndexDo.getDevKey()).collect(Collectors.toList());
        ValueOperations ops = redisTemplate.opsForValue();
        return ops.multiGet(devKeys);
    }
}