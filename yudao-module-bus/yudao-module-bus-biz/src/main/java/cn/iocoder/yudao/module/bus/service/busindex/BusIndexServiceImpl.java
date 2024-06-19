package cn.iocoder.yudao.module.bus.service.busindex;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.ele.CabinetEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
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
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
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
}