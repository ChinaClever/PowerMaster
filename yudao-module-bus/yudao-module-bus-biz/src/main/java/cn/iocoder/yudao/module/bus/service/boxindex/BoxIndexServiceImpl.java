package cn.iocoder.yudao.module.bus.service.boxindex;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalMonthDo;
import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEqTotalWeekDo;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.dto.BusIndexDTO;
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
            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
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
            boxIndexDOList.forEach(busIndexDO -> {
                result.add(new BoxIndexDTO().setId(busIndexDO.getId()).setRunStatus(busIndexDO.getRunStatus()));
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
            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
            JSONArray volValue = lineItemList.getJSONArray("vol_value");
            JSONArray curValue = lineItemList.getJSONArray("cur_value");
            JSONObject boxTotalData = jsonObject.getJSONObject("box_data").getJSONObject("box_total_data");
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
            JSONObject lineItemList = jsonObject.getJSONObject("box_data").getJSONObject("line_item_list");
            JSONArray pfValue = lineItemList.getJSONArray("power_factor");

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
            JSONArray volThd = lineItemList.getJSONArray("vol_thd");
            for (int i = 0; i < 3; i++) {
                double curThdValue = curThd.getDoubleValue(i);
                double volThdValue = volThd.getDoubleValue(i);
                if (i == 0){
                    boxHarmonicRes.setAcurThd(curThdValue);
                    boxHarmonicRes.setAvolThd(volThdValue);
                }else if(i == 1){
                    boxHarmonicRes.setBcurThd(curThdValue);
                    boxHarmonicRes.setBvolThd(volThdValue);
                }else if(i == 2){
                    boxHarmonicRes.setCcurThd(curThdValue);
                    boxHarmonicRes.setCvolThd(volThdValue);
                }
            }

        }
        return new PageResult<>(res,boxIndexDOPageResult.getTotal());
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
}