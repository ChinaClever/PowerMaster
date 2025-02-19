package cn.iocoder.yudao.module.rack.service.index;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletHourDo;
import cn.iocoder.yudao.framework.common.entity.es.rack.ele.RackEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.rack.ele.RackEqTotalDayDo;

import cn.iocoder.yudao.framework.common.entity.es.rack.pow.RackPowHourDo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.entity.mysql.pdu.PduIndexDo;
import cn.iocoder.yudao.framework.common.entity.mysql.rack.RackIndex;
import cn.iocoder.yudao.framework.common.mapper.CabinetPduMapper;
import cn.iocoder.yudao.framework.common.mapper.PduIndexDoMapper;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;

import cn.iocoder.yudao.module.cabinet.dal.mysql.index.CabIndexMapper;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
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
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.rack.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.rack.dal.dataobject.index.RackDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.rack.dal.mysql.index.RackMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rack.enums.ErrorCodeConstants.*;

/**
 * 机架索引 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
@Slf4j
public class RackServiceImpl implements RackService {

    @Resource
    private RackMapper rackMapper;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CabinetPduMapper cabinetPduMapper;

    @Autowired
    private PduIndexDoMapper pduIndexDoMapper;


    @Override
    public Integer createIndex(IndexSaveReqVO createReqVO) {
        // 插入
        RackDO index = BeanUtils.toBean(createReqVO, RackDO.class);
        rackMapper.insert(index);
        // 返回
        return index.getId();
    }

    @Override
    public void updateIndex(IndexSaveReqVO updateReqVO) {
        // 校验存在
        validateIndexExists(updateReqVO.getId());
        // 更新
        RackDO updateObj = BeanUtils.toBean(updateReqVO, RackDO.class);
        rackMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndex(Integer id) {
        // 校验存在
        validateIndexExists(id);
        // 删除
        rackMapper.deleteById(id);
    }

    private void validateIndexExists(Integer id) {
        if (rackMapper.selectById(id) == null) {
            throw exception(INDEX_NOT_EXISTS);
        }
    }

    @Override
    public RackDO getIndex(Integer id) {
        return rackMapper.selectById(id);
    }

    @Override
    public PageResult<RackDO> getIndexPage(IndexPageReqVO pageReqVO) {
        return rackMapper.selectPage(pageReqVO);
    }

    @Override
    public Map getReportConsumeDataById(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase barRes = new LineResBase();
        BarSeries barSeries = new BarSeries();
        try {
            if(id != null){
                String index = null;
                boolean isSameDay = false;
                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "rack_ele_total_realtime";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }
                    isSameDay = true;
                } else {
                    index = "rack_eq_total_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                    isSameDay = false;
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> rackData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                Double firstEq = null;
                Double lastEq = null;
                Double totalEq = 0D;
                Double maxEle = null;
                String maxEleTime = null;
                int nowTimes = 0;
                if (isSameDay){
                    for (String str : rackData) {
                        nowTimes++;
                        RackEleTotalRealtimeDo rackEleTotalRealtimeDo = JsonUtils.parseObject(str, RackEleTotalRealtimeDo.class);
                        if (nowTimes == 1) {
                            firstEq = rackEleTotalRealtimeDo.getEleTotal();
                        }
                        if (nowTimes > 1){
                            barSeries.getData().add((float)(rackEleTotalRealtimeDo.getEleTotal() -lastEq));
                            barRes.getTime().add(rackEleTotalRealtimeDo.getCreateTime().toString("HH:mm:ss"));
                        }
                        lastEq = rackEleTotalRealtimeDo.getEleTotal();
                    }
                    String eleMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "ele_total");
                    RackEleTotalRealtimeDo eleMaxValue = JsonUtils.parseObject(eleMax, RackEleTotalRealtimeDo.class);
                    if(eleMaxValue != null){
                        maxEle = eleMaxValue.getEleTotal();
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
                    for (String str : rackData) {
                        nowTimes++;
                        RackEqTotalDayDo totalDayDo = JsonUtils.parseObject(str, RackEqTotalDayDo.class);
                        totalEq += totalDayDo.getEqValue();
                        barSeries.getData().add((float)totalDayDo.getEqValue());
                        barRes.getTime().add(totalDayDo.getStartTime().toString("yyyy-MM-dd"));
                    }
                    String eqMax = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "eq_value");
                    RackEqTotalDayDo eqMaxValue = JsonUtils.parseObject(eqMax, RackEqTotalDayDo.class);
                    if(eqMaxValue != null){
                        maxEle = eqMaxValue.getEqValue();
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
    public Map getReportPowDataById(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase totalLineRes = new LineResBase();
        
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
            if(id != null) {
                String index = null;

                if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                    index = "rack_hda_pow_hour";
                    if (oldTime.equals(newTime)) {
                        newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                    }

                } else {
                    index = "rack_hda_pow_day";
                    oldTime = oldTime.plusDays(1);
                    newTime = newTime.plusDays(1);
                }
                String startTime = localDateTimeToString(oldTime);
                String endTime = localDateTimeToString(newTime);
                List<String> cabinetData = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
                List<RackPowHourDo> rackPowHourDos = cabinetData.stream().map(str -> JsonUtils.parseObject(str, RackPowHourDo.class)).collect(Collectors.toList());


                LineSeries totalApparentPow = new LineSeries();
                totalApparentPow.setName("总平均视在功率");
                LineSeries totalActivePow = new LineSeries();
                totalActivePow.setName("总平均有功功率");
                totalLineRes.getSeries().add(totalApparentPow);
                totalLineRes.getSeries().add(totalActivePow);
                

                if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                    rackPowHourDos.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("HH:mm:ss"));

                    });
                }else{
                    rackPowHourDos.forEach(hourdo -> {
                        totalApparentPow.getData().add(hourdo.getApparentTotalAvgValue());
                        totalActivePow.getData().add(hourdo.getActiveTotalAvgValue());
                        totalLineRes.getTime().add(hourdo.getCreateTime().toString("yyyy-MM-dd"));
                        
                    });
                }

                String apparentTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_total_max_value");
                RackPowHourDo totalMaxApparent = JsonUtils.parseObject(apparentTotalMaxValue, RackPowHourDo.class);
                String apparentTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "apparent_total_min_value");
                RackPowHourDo totalMinApparent = JsonUtils.parseObject(apparentTotalMinValue, RackPowHourDo.class);

                String activeTotalMaxValue = getMaxData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_total_max_value");
                RackPowHourDo totalMaxActive = JsonUtils.parseObject(activeTotalMaxValue, RackPowHourDo.class);
                String activeTotalMinValue = getMinData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index, "active_total_min_value");
                RackPowHourDo totalMinActive = JsonUtils.parseObject(activeTotalMinValue, RackPowHourDo.class);

                

                result.put("totalLineRes",totalLineRes);

                result.put("apparentPowMaxValue",totalMaxApparent.getApparentTotalMaxValue());
                result.put("apparentPowMaxTime",totalMaxApparent.getApparentTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("apparentPowMinValue",totalMinApparent.getApparentTotalMinValue());
                result.put("apparentPowMinTime",totalMinApparent.getApparentTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMaxValue",totalMaxActive.getActiveTotalMaxValue());
                result.put("activePowMaxTime",totalMaxActive.getActiveTotalMaxTime().toString("yyyy-MM-dd HH:mm:ss"));
                result.put("activePowMinValue",totalMinActive.getActiveTotalMinValue());
                result.put("activePowMinTime",totalMinActive.getActiveTotalMinTime().toString("yyyy-MM-dd HH:mm:ss"));

            }
        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public Map getRackRedisById(Integer id) {
        Map result = new HashMap<>();
        if (id == null){
            return null;
        }else {
            RackDO rackDO = rackMapper.selectOne(new LambdaQueryWrapper<RackDO>().eq(RackDO::getId, id));
            if (rackDO != null) {
                String rackType = rackDO.getRackType();
                result.put("rackType", rackType);
                ValueOperations ops = redisTemplate.opsForValue();
                JSONObject jsonObject = (JSONObject) ops.get("packet:rack:" + id);
                result.put("rackRedis", jsonObject);
                return result;
            }
        }
        return null;
    }

    @Override
    public Map getRackPFLine(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase totalLineRes = new LineResBase();
        result.put("pfLineRes",totalLineRes);
        try {

            String index = null;

            if (timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())) {
                index = "rack_hda_pow_hour";
                if (oldTime.equals(newTime)) {
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }

            } else {
                index = "rack_hda_pow_day";
                oldTime = oldTime.plusDays(1);
                newTime = newTime.plusDays(1);
            }

            String startTime = localDateTimeToString(oldTime);
            String endTime = localDateTimeToString(newTime);
            List<String> data = getData(startTime, endTime, Arrays.asList(Integer.valueOf(id)), index);
            List<RackPowHourDo> powList = data.stream().map(str -> JsonUtils.parseObject(str, RackPowHourDo.class)).collect(Collectors.toList());

            LineSeries totalPFLine = new LineSeries();
            totalPFLine.setName("总平均功率因素");

            totalLineRes.getSeries().add(totalPFLine);

            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());
                    DateTime dateTime = new DateTime(hourdo.getCreateTime());
                    totalLineRes.getTime().add(dateTime.toString("HH:mm"));

                });
            }else{
                powList.forEach(hourdo -> {
                    totalPFLine.getData().add(hourdo.getPowerFactorAvgValue());
                    DateTime dateTime = new DateTime(hourdo.getCreateTime());
                    totalLineRes.getTime().add(dateTime.toString("yyyy-MM-dd"));
                });
            }
            result.put("pfLineRes",totalLineRes);

        }catch (Exception e){
            log.error("获取数据失败",e);
        }
        return result;
    }

    @Override
    public List<Integer> idList() {
        return rackMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(RackDO::getId).collect(Collectors.toList());
    }

    @Override
    public Map<String,Double> getEleByRack(String id,  LocalDateTime oldTime, LocalDateTime newTime) throws IOException {
        Map<String, Double> result = new HashMap<>();
        String index = "rack_ele_total_realtime";
        List<Integer> list = Arrays.asList(Integer.valueOf(id));

        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件
        SearchSourceBuilder builder = new SearchSourceBuilder();

        // 获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(oldTime).lte(newTime))
                .must(QueryBuilders.termsQuery("rack_id", list))));
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

    @Override
    public Map getOutletCur(String id, String type, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        LineResBase lineResBaseA = new LineResBase();
        LineResBase lineResBaseB = new LineResBase();
        Map AList = new HashMap();
        Map BList = new HashMap();
        String index = null;
        if (type.equals("0")){
            index = "pdu_hda_outlet_hour";
        }else {
            index = "pdu_hda_outlet_day";
        }
        SearchRequest searchRequest = new SearchRequest(index);
        RackDO rackDO = rackMapper.selectOne(new LambdaQueryWrapper<RackDO>().eq(RackDO::getId, id));
        if (rackDO == null){
            return null;
        }
        String outletIdA = rackDO.getOutletIdA();
        String outletIdB = rackDO.getOutletIdB();
        List<Integer> resultA = new ArrayList<>();
        List<Integer> resultB = new ArrayList<>();
        outletIdA = outletIdA.substring(1,outletIdA.length()-1);
        String[] partsA = outletIdA.split(",");
        Pattern pattern = Pattern.compile("\\d+");
        for (String part : partsA){
            Matcher matcher = pattern.matcher(part.trim());
            if (matcher.find()){
                resultA.add(Integer.parseInt(matcher.group()));
            }
        }
        outletIdB = outletIdB.substring(1,outletIdB.length()-1);
        String[] partsB = outletIdB.split(",");
        for (String part : partsB){
            Matcher matcher = pattern.matcher(part.trim());
            if (matcher.find()){
                resultB.add(Integer.parseInt(matcher.group()));
            }
        }
        CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(new LambdaQueryWrapper<CabinetPdu>().eq(CabinetPdu::getCabinetId, rackDO.getCabinetId()));
        if (cabinetPdu == null){
            return null;
        }
        String pduKeyA = cabinetPdu.getPduKeyA();
        String pduKeyB = cabinetPdu.getPduKeyB();
        PduIndexDo pduIndexA = pduIndexDoMapper.selectOne(new LambdaQueryWrapper<PduIndexDo>().eq(PduIndexDo::getPduKey, pduKeyA));
        if (pduIndexA != null){
            for (Integer i : resultA){
                List<Float> list = new ArrayList<>();
                List<String> list1 = new ArrayList<>();
                // 构建查询请求
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("outlet_id", i))
                        .must(QueryBuilders.termQuery("pdu_id", pduIndexA.getId()));
                searchSourceBuilder.query(boolQuery);
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                        .from(formatter.format(oldTime))
                        .to(formatter.format(newTime)));
                searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                searchSourceBuilder.size(1000); // 设置返回的最大结果数
                searchRequest.source(searchSourceBuilder);
                try{
                    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                    if (searchResponse != null) {
                        SearchHits hits = searchResponse.getHits();
                        for (SearchHit hit : hits) {
                            String str = hit.getSourceAsString();
                            if (type.equals("0")){
                                PduHdaOutletHourDo outletHourDo = JsonUtils.parseObject(str, PduHdaOutletHourDo.class);
                                list.add(outletHourDo.getCurAvgValue());
                                DateTime dateTime = new DateTime(outletHourDo.getCreateTime());
                                list1.add(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
                            }else {
                                PduHdaOutletDayDo outletDayDo = JsonUtils.parseObject(str, PduHdaOutletDayDo.class);
                                list.add(outletDayDo.getCurAvgValue());
                                DateTime dateTime = new DateTime(outletDayDo.getCreateTime());
                                list1.add(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
                            }
                        }
                            }
                    LineSeries lineSeries = new LineSeries();
                    lineSeries.setName("A路输出位"+i+"电流");
                    lineSeries.setData(list);
                    lineResBaseA.getSeries().add(lineSeries);
                    lineResBaseA.setTime(list1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        PduIndexDo pduIndexB = pduIndexDoMapper.selectOne(new LambdaQueryWrapper<PduIndexDo>().eq(PduIndexDo::getPduKey, pduKeyB));
        if (pduIndexB != null){
            for (Integer i : resultB){
                List<Float> list = new ArrayList<>();
                List<String> list1 = new ArrayList<>();
                // 构建查询请求
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("outlet_id", i))
                        .must(QueryBuilders.termQuery("pdu_id", pduIndexB.getId()));
                searchSourceBuilder.query(boolQuery);
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                        .from(formatter.format(oldTime))
                        .to(formatter.format(newTime)));
                searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                searchSourceBuilder.size(1000);
                searchRequest.source(searchSourceBuilder);
                try{
                    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                    if (searchResponse != null) {
                        SearchHits hits = searchResponse.getHits();
                        for (SearchHit hit : hits) {
                            String str = hit.getSourceAsString();
                            if (type.equals("0")){
                                PduHdaOutletHourDo outletHourDo = JsonUtils.parseObject(str, PduHdaOutletHourDo.class);
                                list.add(outletHourDo.getCurAvgValue());
                                DateTime dateTime = new DateTime(outletHourDo.getCreateTime());
                                list1.add(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
                            }else {
                                PduHdaOutletDayDo outletDayDo = JsonUtils.parseObject(str, PduHdaOutletDayDo.class);
                                list.add(outletDayDo.getCurAvgValue());
                                DateTime dateTime = new DateTime(outletDayDo.getCreateTime());
                                list1.add(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
                            }
                        }
                    }
                    LineSeries lineSeries = new LineSeries();
                    lineSeries.setName("B路输出位"+i+"电流");
                    lineSeries.setData(list);
                    lineResBaseB.getSeries().add(lineSeries);
                    lineResBaseB.setTime(list1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            }
        result.put("A",lineResBaseA);
        result.put("B",lineResBaseB);
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
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("rack_id", ids))));
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

    private String getMaxData(String startTime, String endTime, List<Integer> ids, String index,String order) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword").gte(startTime).lte(endTime))
                .must(QueryBuilders.termsQuery("rack_id", ids))));
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
                .must(QueryBuilders.termsQuery("rack_id", ids))));
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

    private String localDateTimeToString(LocalDateTime time){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
    }

}