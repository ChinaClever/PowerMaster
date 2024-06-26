package cn.iocoder.yudao.module.bus.service.boxindex;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.entity.es.box.line.BoxLineHourDo;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.*;

import cn.iocoder.yudao.module.bus.dal.dataobject.boxcurbalancecolor.BoxCurbalanceColorDO;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;

import cn.iocoder.yudao.module.bus.dal.mysql.boxcurbalancecolor.BoxCurbalanceColorMapper;
import cn.iocoder.yudao.module.bus.dal.mysql.boxindex.BoxIndexCopyMapper;
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
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.INDEX_NOT_EXISTS;

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

    @Resource
    private BoxCurbalanceColorMapper boxCurbalanceColorMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient client;
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
        boxIndexCopyMapper.deleteById(id);
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
        ValueOperations ops = redisTemplate.opsForValue();
        for (BoxIndex boxIndexDO : list) {
            BoxIndexRes boxIndexRes = new BoxIndexRes();
            boxIndexRes.setStatus(boxIndexDO.getRunStatus());
            res.add(boxIndexRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + boxIndexDO.getDevKey());
            if (jsonObject == null){

                continue;
            }
            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
            JSONArray loadRate = lineItemList.getJSONArray("load_rate");
            List<Double> rateList = loadRate.toList(Double.class);
            if(rateList.size() > 1) {
                boxIndexRes.setALoadRate(loadRate.getDouble(0));
                boxIndexRes.setBLoadRate(loadRate.getDouble(1));
                boxIndexRes.setCLoadRate(loadRate.getDouble(2));
            } else{
                boxIndexRes.setALoadRate(loadRate.getDouble(0));
            }
            rateList.sort(Collections.reverseOrder());
            Double biggest = rateList.get(0) * 100;
            if (biggest == 0){
                boxIndexRes.setColor(0);
            } else if (biggest < 30){
                boxIndexRes.setColor(1);
            } else if (biggest < 60){
                boxIndexRes.setColor(2);
            } else if (biggest < 90){
                boxIndexRes.setColor(3);
            } else if (biggest >= 90){
                boxIndexRes.setColor(4);
            }
            if(pageReqVO.getColor() != null){
                if(!pageReqVO.getColor().contains(boxIndexRes.getColor())){
                    continue;
                }
            }

        }

        return new PageResult<>(res,boxIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BoxRedisDataRes> getBoxRedisPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxRedisDataRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BoxIndex boxIndexDO : list) {
            BoxRedisDataRes boxRedisDataRes = new BoxRedisDataRes();
            boxRedisDataRes.setStatus(boxIndexDO.getRunStatus());
            res.add(boxRedisDataRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + boxIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");
            JSONArray volValue = loopItemList.getJSONArray("vol_value");
            JSONArray volStatus = loopItemList.getJSONArray("vol_status");
            JSONArray curValue = loopItemList.getJSONArray("cur_value");
            JSONArray curStatus = loopItemList.getJSONArray("cur_status");
            JSONArray powValue = loopItemList.getJSONArray("pow_value");
            JSONArray powStatus = loopItemList.getJSONArray("pow_status");
            JSONArray powReactive = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list").getJSONArray("pow_reactive");
            for (int i = 0; i < 3; i++) {
                double vol = volValue.getDoubleValue(i);
                Integer volSta = volStatus.getInteger(i);
                double cur = curValue.getDoubleValue(i);
                Integer curSta =curStatus.getInteger(i);
                double activePow = powValue.getDoubleValue(i);
                Integer activePowSta =powStatus.getInteger(i);
                double reactivePow = powReactive.getDoubleValue(i);
                if (i == 0){
                    boxRedisDataRes.setACur(cur);
                    boxRedisDataRes.setACurStatus(curSta);
                    if(curSta != 0){
                        boxRedisDataRes.setACurColor("red");
                    }
                    boxRedisDataRes.setAVol(vol);
                    boxRedisDataRes.setAVolStatus(volSta);
                    if(volSta != 0){
                        boxRedisDataRes.setAVolColor("red");
                    }
                    boxRedisDataRes.setAActivePow(activePow);
                    boxRedisDataRes.setAActivePowStatus(activePowSta);
                    if(activePowSta != 0){
                        boxRedisDataRes.setAActivePowColor("red");
                    }
                    boxRedisDataRes.setAReactivePow(reactivePow);
                }else if(i == 1){
                    boxRedisDataRes.setBCur(cur);
                    boxRedisDataRes.setBCurStatus(curSta);
                    if(curSta != 0){
                        boxRedisDataRes.setBCurColor("red");
                    }
                    boxRedisDataRes.setBVol(vol);
                    boxRedisDataRes.setBVolStatus(volSta);
                    if(volSta != 0){
                        boxRedisDataRes.setBVolColor("red");
                    }
                    boxRedisDataRes.setBActivePow(activePow);
                    boxRedisDataRes.setBActivePowStatus(activePowSta);
                    if(activePowSta != 0){
                        boxRedisDataRes.setBActivePowColor("red");
                    }
                    boxRedisDataRes.setBReactivePow(reactivePow);
                }else if(i == 2){
                    boxRedisDataRes.setCCur(cur);
                    boxRedisDataRes.setCCurStatus(curSta);
                    if(curSta != 0){
                        boxRedisDataRes.setCCurColor("red");
                    }
                    boxRedisDataRes.setCVol(vol);
                    boxRedisDataRes.setCVolStatus(volSta);
                    if(volSta != 0){
                        boxRedisDataRes.setCVolColor("red");
                    }
                    boxRedisDataRes.setCActivePow(activePow);
                    boxRedisDataRes.setCActivePowStatus(activePowSta);
                    if(activePowSta != 0){
                        boxRedisDataRes.setCActivePowColor("red");
                    }
                    boxRedisDataRes.setCReactivePow(reactivePow);
                }
            }

        }
        return new PageResult<>(res,boxIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BoxIndexDTO> getEqPage(BoxIndexPageReqVO pageReqVO) {
        try {
            PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
            List<BoxIndex> boxIndexDOList = boxIndexDOPageResult.getList();
            List<BoxIndexDTO> result = new ArrayList<>();
            List<Integer> ids = boxIndexDOList.stream().map(BoxIndex::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)){
                return new PageResult<>(result, boxIndexDOPageResult.getTotal());
            }
            //昨日
            boxIndexDOList.forEach(boxIndex -> {
                result.add(new BoxIndexDTO().setId(boxIndex.getId()).setRunStatus(boxIndex.getRunStatus()));
            });
            String startTime = DateUtil.formatDateTime(DateUtil.beginOfDay(DateTime.now()));
            String endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  yesterdayList = getData(startTime,endTime, ids,"box_eq_total_day");
            Map<Integer,Double> yesterdayMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(yesterdayList)){
                yesterdayList.forEach(str -> {
                    BoxEqTotalDayDo dayDo = JsonUtils.parseObject(str, BoxEqTotalDayDo.class);
                    yesterdayMap.put(dayDo.getBoxId(),dayDo.getEq());
                });
            }

            //上周
            startTime = DateUtil.formatDateTime(DateUtil.beginOfWeek(DateTime.now()));
            endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  weekList = getData(startTime,endTime, ids,"box_eq_total_week");
            Map<Integer,Double> weekMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(weekList)){
                weekList.forEach(str -> {
                    BoxEqTotalWeekDo weekDo = JsonUtils.parseObject(str, BoxEqTotalWeekDo.class);
                    weekMap.put(weekDo.getBoxId(),weekDo.getEq());
                });
            }

            //上月
            startTime = DateUtil.formatDateTime(DateUtil.beginOfMonth(DateTime.now()));
            endTime =DateUtil.formatDateTime(DateTime.now());
            List<String>  monthList = getData(startTime,endTime, ids,"box_eq_total_month");
            Map<Integer,Double> monthMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(monthList)){
                monthList.forEach(str -> {
                    BoxEqTotalMonthDo monthDo = JsonUtils.parseObject(str, BoxEqTotalMonthDo.class);
                    monthMap.put(monthDo.getBoxId(),monthDo.getEq());
                });
            }

            result.forEach(dto -> {
                dto.setYesterdayEq(yesterdayMap.get(dto.getId()));
                dto.setLastWeekEq(weekMap.get(dto.getId()));
                dto.setLastMonthEq(monthMap.get(dto.getId()));
            });
            return new PageResult<>(result, boxIndexDOPageResult.getTotal());
        }catch (Exception e) {
            log.error("获取数据失败：", e);
        }
        return new PageResult<>(new ArrayList<>(), 0L);
    }

    @Override
    public PageResult<BoxBalanceDataRes> getBoxBalancePage(BoxIndexPageReqVO pageReqVO) {

        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        BoxCurbalanceColorDO boxCurbalanceColorDO = boxCurbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxBalanceDataRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BoxIndex boxIndexDO : list) {
            BoxBalanceDataRes boxBalanceDataRes = new BoxBalanceDataRes();
            boxBalanceDataRes.setStatus(boxBalanceDataRes.getStatus());
            res.add(boxBalanceDataRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + boxIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");
            JSONArray volValue = loopItemList.getJSONArray("vol_value");
            JSONArray curValue = loopItemList.getJSONArray("cur_value");
            JSONObject boxTotalData = jsonObject.getJSONObject("box_data").getJSONObject("box_total_data");
            JSONArray curAlarmArr = loopItemList.getJSONArray("cur_max");
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
                    boxBalanceDataRes.setACur(cur);
                    boxBalanceDataRes.setAVol(vol);
                }else if(i == 1){
                    boxBalanceDataRes.setBCur(cur);
                    boxBalanceDataRes.setBVol(vol);
                }else if(i == 2){
                    boxBalanceDataRes.setCCur(cur);
                    boxBalanceDataRes.setCVol(vol);
                }
            }
            if (boxCurbalanceColorDO == null) {
                if (a >= maxVal * 0.2) {
                    if (boxTotalData.getDouble("cur_unbalance") < 15) {
                        color = 2;
                    } else if (boxTotalData.getDouble("cur_unbalance") < 30) {
                        color = 3;
                    } else {
                        color = 4;
                    }
                } else {
                    color = 1;
                }
            } else {
                if (a >= maxVal * 0.2) {
                    if (boxTotalData.getDouble("cur_unbalance") < boxCurbalanceColorDO.getRangeOne()) {
                        color = 2;
                    } else if (boxTotalData.getDouble("cur_unbalance") < boxCurbalanceColorDO.getRangeFour()) {
                        color = 3;
                    } else {
                        color = 4;
                    }
                } else {
                    color = 1;
                }
            }
            boxBalanceDataRes.setCurUnbalance(boxTotalData.getDouble("cur_unbalance"));
            boxBalanceDataRes.setVolUnbalance(boxTotalData.getDouble("vol_unbalance"));
            boxBalanceDataRes.setColor(color);
        }
        return new PageResult<>(res,boxIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BoxTemRes> getBoxTemPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxTemRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BoxIndex boxIndexDO : list) {
            BoxTemRes boxTemRes = new BoxTemRes();
            boxTemRes.setStatus(boxIndexDO.getRunStatus());
            res.add(boxTemRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + boxIndexDO.getDevKey());
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
                    boxTemRes.setATem(tem);
                    boxTemRes.setATemStatus(temSta);
                    if(temSta != 0){
                        boxTemRes.setATemColor("red");
                    }
                }else if(i == 1){
                    boxTemRes.setBTem(tem);
                    boxTemRes.setBTemStatus(temSta);
                    if(temSta != 0){
                        boxTemRes.setBTemColor("red");
                    }
                }else if(i == 2){
                    boxTemRes.setCTem(tem);
                    boxTemRes.setCTemStatus(temSta);
                    if(temSta != 0){
                        boxTemRes.setCTemColor("red");
                    }
                }else if(i == 3){
                    boxTemRes.setNTem(tem);
                    boxTemRes.setNTemStatus(temSta);
                    if(temSta != 0){
                        boxTemRes.setNTemColor("red");
                    }
                }
            }

        }
        return new PageResult<>(res,boxIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BoxPFRes> getBoxPFPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxPFRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BoxIndex boxIndexDO : list) {
            BoxPFRes boxPFRes = new BoxPFRes();
            boxPFRes.setStatus(boxIndexDO.getRunStatus());
            res.add(boxPFRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + boxIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject loopItemList = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list");
            JSONArray pfValue = loopItemList.getJSONArray("power_factor");

            for (int i = 0; i < 3; i++) {
                double pf = pfValue.getDoubleValue(i);
                if (i == 0){
                    boxPFRes.setApf(pf);
                }else if(i == 1){
                    boxPFRes.setBpf(pf);
                }else if(i == 2){
                    boxPFRes.setCpf(pf);
                }
            }
            boxPFRes.setTotalPf(jsonObject.getJSONObject("box_data").getJSONObject("box_total_data").getDoubleValue("power_factor"));
        }
        return new PageResult<>(res,boxIndexDOPageResult.getTotal());
    }

    @Override
    public PageResult<BoxHarmonicRes> getBoxHarmonicPage(BoxIndexPageReqVO pageReqVO) {
        PageResult<BoxIndex> boxIndexDOPageResult = boxIndexCopyMapper.selectPage(pageReqVO);
        List<BoxIndex> list = boxIndexDOPageResult.getList();
        List<BoxHarmonicRes> res = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        for (BoxIndex boxIndexDO : list) {
            BoxHarmonicRes boxHarmonicRes = new BoxHarmonicRes();
            boxHarmonicRes.setStatus(boxIndexDO.getRunStatus());
            res.add(boxHarmonicRes);
            JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + boxIndexDO.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
            JSONArray curThd = lineItemList.getJSONArray("cur_thd");
            for (int i = 0; i < 3; i++) {
                double curThdValue = curThd.getDoubleValue(i);
                if (i == 0){
                    boxHarmonicRes.setAcurThd(curThdValue);
                }else if(i == 1){
                    boxHarmonicRes.setBcurThd(curThdValue);
                }else if(i == 2){
                    boxHarmonicRes.setCcurThd(curThdValue);
                }
            }

        }
        return new PageResult<>(res,boxIndexDOPageResult.getTotal());
    }

    @Override
    public List<String> getDevKeyList() {
        List<String> result = boxIndexCopyMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(BoxIndex::getDevKey).collect(Collectors.toList());
        return result;
    }

    @Override
    public PageResult<BoxLineRes> getBoxLineDevicePage(BoxIndexPageReqVO pageReqVO) {
        try {
            PageResult<BoxIndex> boxIndexPageResult = null;
            List<BoxLineRes> result = new ArrayList<>();
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
//                    return new PageResult<BoxLineRes>(result,0L);
//                }
//                boxIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<BoxIndex>()
//                        .likeIfPresent(BoxIndex::getDevKey,pageReqVO.getDevKey()).inIfPresent(BoxIndex::getIpAddr,ipAddrList));
//            }else{
//                boxIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<BoxIndex>()
//                        .likeIfPresent(BoxIndex::getDevKey,pageReqVO.getDevKey()));
//            }

            boxIndexPageResult = boxIndexCopyMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<BoxIndex>()
                    .likeIfPresent(BoxIndex::getDevKey,pageReqVO.getDevKey()));

            List<BoxIndex> boxIndices = boxIndexPageResult.getList();

//            Set<String> ipAddrSet = new HashSet<>();
//            Set<Integer> cascadeAddrSet = new HashSet<>();
//            for (BoxIndex busIndex : boxIndices) {
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
            List<Integer> ids = boxIndices.stream().map(BoxIndex::getId).collect(Collectors.toList());
            List<String> esCurMaxResult = null;
            List<String> esPowMaxResult = null;
            Map<Integer,Map<Integer, BoxLineHourDo>> curMap = new HashMap<>();
            Map<Integer,Map<Integer,BoxLineHourDo>> powMap = new HashMap<>();
            String index = null;
            if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                index = "box_hda_line_hour";
            } else {
                index = "box_hda_line_day";
            }
            String startTime = localDateTimeToString(pageReqVO.getOldTime());
            String endTime = localDateTimeToString(pageReqVO.getNewTime());
            esCurMaxResult = getBoxLineData(startTime,endTime,ids,index,"cur_max_value");
            esPowMaxResult = getBoxLineData(startTime,endTime,ids,index,"pow_active_max_value");
            if (!CollectionUtils.isEmpty(esCurMaxResult)){
                esCurMaxResult.forEach(str -> {
                    BoxLineHourDo hourDo = JsonUtils.parseObject(str, BoxLineHourDo.class);
                    curMap.computeIfAbsent(hourDo.getBoxId(), k -> new HashMap<>()).put(hourDo.getLineId(), hourDo);
                });
            }

            if (!CollectionUtils.isEmpty(esPowMaxResult)){
                esPowMaxResult.forEach(str -> {
                    BoxLineHourDo hourDo = JsonUtils.parseObject(str, BoxLineHourDo.class);
                    powMap.computeIfAbsent(hourDo.getBoxId(), k -> new HashMap<>()).put(hourDo.getLineId(), hourDo);
                });
            }

            for (BoxIndex boxIndex : boxIndices) {
                Integer id = boxIndex.getId();
                if (curMap.get(id) == null){
                    continue;
                }
                BoxLineRes boxLineRes = new BoxLineRes();
                boxLineRes.setStatus(boxIndex.getRunStatus());

                boxLineRes.setBoxId(boxIndex.getId());
                boxLineRes.setDevKey(boxIndex.getDevKey());

                BoxLineHourDo curl1 = curMap.get(id).get(1);
                boxLineRes.setL1MaxCur(curl1.getCurMaxValue());
                boxLineRes.setL1MaxCurTime(curl1.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                BoxLineHourDo curl2 = curMap.get(id).get(2);
                if(curl2 != null){
                    boxLineRes.setL2MaxCur(curl2.getCurMaxValue());
                    boxLineRes.setL2MaxCurTime(curl2.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                BoxLineHourDo curl3 = curMap.get(id).get(3);
                if(curl3 != null){
                    boxLineRes.setL3MaxCur(curl3.getCurMaxValue());
                    boxLineRes.setL3MaxCurTime(curl3.getCurMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                BoxLineHourDo powl1 = powMap.get(id).get(1);
                boxLineRes.setL1MaxPow(powl1.getPowActiveMaxValue());
                boxLineRes.setL1MaxPowTime(powl1.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                BoxLineHourDo powl2 = powMap.get(id).get(2);
                if(powl2 != null) {
                    boxLineRes.setL2MaxPow(powl2.getPowActiveMaxValue());
                    boxLineRes.setL2MaxPowTime(powl2.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }
                BoxLineHourDo powl3 = powMap.get(id).get(3);
                if(powl3 != null) {
                    boxLineRes.setL3MaxPow(powl3.getPowActiveMaxValue());
                    boxLineRes.setL3MaxPowTime(powl3.getPowActiveMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                }

                result.add(boxLineRes);
            }
            return new PageResult<BoxLineRes>(result,boxIndexPageResult.getTotal());

        }catch (Exception e){
            System.out.println(e);
        }


        return new PageResult<>(new ArrayList<>(), 0L);
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
                .must(QueryBuilders.termsQuery("box_id", ids))));
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

    private List<String> getBoxLineData(String startTime, String endTime, List<Integer> ids, String index, String sort) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lt(endTime))
                .must(QueryBuilders.termsQuery("box_id", ids))));
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