package cn.iocoder.yudao.module.pdu.service.pdudevice;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEqTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.env.PduEnvRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.module.cabinet.mapper.AisleIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetPduMapper;
import cn.iocoder.yudao.module.cabinet.mapper.RoomIndexMapper;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDULineRes;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.CurbalanceColorDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.curbalancecolor.CurbalanceColorMapper;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Min;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;

import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndex;


import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

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

import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.framework.common.pojo.PageResult;


import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndexMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * PDU设备 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PDUDeviceServiceImpl implements PDUDeviceService {

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
    private CurbalanceColorMapper curbalanceColorMapper;

    @Override
    public PageResult<PDUDeviceDO> getPDUDevicePage(PDUDevicePageReqVO pageReqVO) {

        PageResult<PduIndex> pduIndexPageResult = null;
        List<PDUDeviceDO> result = new ArrayList<>();
        CurbalanceColorDO curbalanceColorDO = curbalanceColorMapper.selectOne(new LambdaQueryWrapperX<>(), false);
        if(pageReqVO.getCabinetIds() != null && !pageReqVO.getCabinetIds().isEmpty()) {
            List<String> devKeyList = new ArrayList<>();

            List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>().inIfPresent(CabinetPdu::getCabinetId, pageReqVO.getCabinetIds()));
            if(cabinetPduList != null && cabinetPduList.size() > 0){
                for (CabinetPdu cabinetPdu : cabinetPduList) {
                    if (!StringUtils.isEmpty(cabinetPdu.getPduIpA()) && cabinetPdu.getCasIdA() >= 0){
                        devKeyList.add(cabinetPdu.getPduIpA() + '-' +cabinetPdu.getCasIdA());
                    }
                    if (!StringUtils.isEmpty(cabinetPdu.getPduIpB()) && cabinetPdu.getCasIdB() >= 0){
                        devKeyList.add(cabinetPdu.getPduIpB() + '-' +cabinetPdu.getCasIdB());
                    }
                }
            }else{
                return new PageResult<PDUDeviceDO>(result,0L);
            }
            pduIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PduIndex>()
                    .likeIfPresent(PduIndex::getDevKey,pageReqVO.getDevKey()).inIfPresent(PduIndex::getDevKey,devKeyList));
        }else{
            pduIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PduIndex>()
                    .likeIfPresent(PduIndex::getDevKey,pageReqVO.getDevKey()));
        }


        List<PduIndex> pduIndices = pduIndexPageResult.getList();
        ValueOperations ops = redisTemplate.opsForValue();

        long i = 0;
        for (PduIndex pduIndex : pduIndices) {
            i++;
            String localtion = null;
            String ipAddr = pduIndex.getIpAddr();
            String cascadeAddr = pduIndex.getCascadeAddr();
            PDUDeviceDO pduDeviceDO = new PDUDeviceDO();
            result.add(pduDeviceDO);
            CabinetPdu cabinetPduA = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
                            .eq(CabinetPdu::getPduIpA, ipAddr)
                            .eq(CabinetPdu::getCasIdA,cascadeAddr));
            CabinetPdu cabinetPduB = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
                    .eq(CabinetPdu::getPduIpB, ipAddr)
                    .eq(CabinetPdu::getCasIdB, cascadeAddr));
            if(cabinetPduA != null){
                int cabinetId = cabinetPduA.getCabinetId();
                CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
                JSONObject cabinetObject = (JSONObject) ops.get("packet:cabinet:" + cabinet.getRoomId() + '-' + cabinet.getId());
                String cabinetName = cabinetObject.getString("cabinet_name");;
                String roomName = cabinetObject.getString("room_name");;
                if(cabinet.getAisleId() != 0){
                    String aisleName = cabinetObject.getString("aisle_name");;
                    localtion = roomName + "-" + aisleName + "-" + cabinetName + "-" + "A路";
                }else {
                    localtion = roomName + "-"  + cabinetName +  "-" + "A路";
                }
            }
            if(cabinetPduB != null){
                int cabinetId = cabinetPduB.getCabinetId();
                CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
                JSONObject cabinetObject = (JSONObject) ops.get("packet:cabinet:" + cabinet.getRoomId() + '-' + cabinet.getId());
                String cabinetName = cabinetObject.getString("cabinet_name");;
                String roomName = cabinetObject.getString("room_name");;
                if(cabinet.getAisleId() != 0){
                    String aisleName = cabinetObject.getString("aisle_name");;
                    localtion = roomName + "-" + aisleName + "-" + cabinetName + "-" + "B路";
                }else {
                    localtion = roomName + "-"  + cabinetName +  "-" + "B路";
                }
            }
            pduDeviceDO.setLocation(localtion);
            pduDeviceDO.setDevKey(pduIndex.getDevKey());

            JSONObject jsonObject = (JSONObject) ops.get("packet:pdu:" + pduIndex.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject pduTgData = jsonObject.getJSONObject("pdu_data").getJSONObject("pdu_total_data");
            JSONArray curArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("cur_value");
            JSONArray volArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("vol_value");
            JSONArray curAlarmArr = jsonObject.getJSONObject("pdu_data").getJSONObject("line_item_list").getJSONArray("cur_alarm_max");
            curAlarmArr.sort(Collections.reverseOrder());
            double maxVal = curAlarmArr.getDouble(0);
            List<Double> temp = curArr.toList(Double.class);
            Double curUnbalance = null;
            Double bcur = null;
            Double ccur = null;
            temp.sort(Collections.reverseOrder());
            int color = 0;
            if(temp.size() >= 2){
                double a = temp.get(0) - temp.get(2);

                curUnbalance = pduTgData.getDoubleValue("cur_unbalance");
                bcur = curArr.getDoubleValue(1);
                ccur = curArr.getDoubleValue(2);
                if (curbalanceColorDO == null) {
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
                        if (curUnbalance < curbalanceColorDO.getRangeOne()) {
                            color = 2;
                        } else if (curUnbalance < curbalanceColorDO.getRangeFour()) {
                            color = 3;
                        } else {
                            color = 4;
                        }
                    } else {
                        color = 1;
                    }
                }
            }
            Integer status1 = jsonObject.getInteger("status");
            if(pageReqVO.getStatus() != null){
                if(!pageReqVO.getStatus().contains(status1)){
                    continue;
                }
            }
            if(pageReqVO.getColor() != null){
                if(!pageReqVO.getColor().contains(color)){
                    continue;
                }
            }

            if(status1 != null){
                pduDeviceDO.setStatus(status1);
            }
            pduDeviceDO.setId(pduIndex.getId());

            pduDeviceDO.setPf(pduTgData.getDoubleValue("power_factor"));

            pduDeviceDO.setEle(pduTgData.getDoubleValue("ele_active"));
            pduDeviceDO.setPow(pduTgData.getDoubleValue("pow_active"));
            pduDeviceDO.setApparentPow(pduTgData.getDoubleValue("pow_apparent"));
            pduDeviceDO.setReactivePow(pduTgData.getDoubleValue("pow_reactive"));
            pduDeviceDO.setDataUpdateTime(jsonObject.getString("sys_time"));
            pduDeviceDO.setPduAlarm(jsonObject.getString("pdu_alarm"));
            pduDeviceDO.setAcur(curArr.getDoubleValue(0));
            pduDeviceDO.setBcur(bcur);
            pduDeviceDO.setCcur(ccur);
            pduDeviceDO.setCurUnbalance(curUnbalance);
            pduDeviceDO.setAvol(volArr.getDoubleValue(0));
            pduDeviceDO.setBvol(volArr.size() > 1 ? volArr.getDoubleValue(1) : null);
            pduDeviceDO.setCvol(volArr.size() > 2 ? volArr.getDoubleValue(2) : null);
            pduDeviceDO.setVolUnbalance(pduTgData.getDouble("vol_unbalance") != null ? pduTgData.getDouble("vol_unbalance") : null);
            pduDeviceDO.setColor(color);

        }

        return new PageResult<PDUDeviceDO>(result,pduIndexPageResult.getTotal());
    }

    @Override
    public PageResult<PDULineRes> getPDULineDevicePage(PDUDevicePageReqVO pageReqVO) {

        PageResult<PduIndex> pduIndexPageResult = null;
        List<PDULineRes> result = new ArrayList<>();
        ValueOperations ops = redisTemplate.opsForValue();
        if(pageReqVO.getCabinetIds() != null && !pageReqVO.getCabinetIds().isEmpty()) {
            List<String> ipAddrList = new ArrayList<>();
            List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(new LambdaQueryWrapperX<CabinetPdu>().inIfPresent(CabinetPdu::getCabinetId, pageReqVO.getCabinetIds()));
            if(cabinetPduList != null && cabinetPduList.size() > 0){
                for (CabinetPdu cabinetPdu : cabinetPduList) {
                    if (!StringUtils.isEmpty(cabinetPdu.getPduIpA())){
                        ipAddrList.add(cabinetPdu.getPduIpA());
                    }
                    if (!StringUtils.isEmpty(cabinetPdu.getPduIpB())){
                        ipAddrList.add(cabinetPdu.getPduIpB());
                    }
                }
            }else{
                return new PageResult<PDULineRes>(result,0L);
            }
            pduIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PduIndex>()
                    .likeIfPresent(PduIndex::getDevKey,pageReqVO.getDevKey()).inIfPresent(PduIndex::getIpAddr,ipAddrList));
        }else{
            pduIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<PduIndex>()
                    .likeIfPresent(PduIndex::getDevKey,pageReqVO.getDevKey()));
        }
        List<PduIndex> pduIndices = pduIndexPageResult.getList();

        for (PduIndex pduIndex : pduIndices) {
            PDULineRes pduLineRes = new PDULineRes();
            String ipAddr = pduIndex.getIpAddr();
            String cascadeAddr = pduIndex.getCascadeAddr();
            String localtion = null;
            CabinetPdu cabinetPduA = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
                    .eq(CabinetPdu::getPduIpA, ipAddr)
                    .eq(CabinetPdu::getCasIdA,cascadeAddr));
            CabinetPdu cabinetPduB = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
                    .eq(CabinetPdu::getPduIpB, ipAddr)
                    .eq(CabinetPdu::getCasIdB, cascadeAddr));
            if(cabinetPduA != null){
                int cabinetId = cabinetPduA.getCabinetId();
                CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
                JSONObject cabinetObject = (JSONObject) ops.get("packet:cabinet:" + cabinet.getRoomId() + '-' + cabinet.getId());
                String cabinetName = cabinetObject.getString("cabinet_name");;
                String roomName = cabinetObject.getString("room_name");;
                if(cabinet.getAisleId() != 0){
                    String aisleName = cabinetObject.getString("aisle_name");;
                    localtion = roomName + "-" + aisleName + "-" + cabinetName + "-" + "A路";
                }else {
                    localtion = roomName + "-"  + cabinetName +  "-" + "A路";
                }
            }
            if(cabinetPduB != null){
                int cabinetId = cabinetPduB.getCabinetId();
                CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
                JSONObject cabinetObject = (JSONObject) ops.get("packet:cabinet:" + cabinet.getRoomId() + '-' + cabinet.getId());
                String cabinetName = cabinetObject.getString("cabinet_name");;
                String roomName = cabinetObject.getString("room_name");;
                if(cabinet.getAisleId() != 0){
                    String aisleName = cabinetObject.getString("aisle_name");;
                    localtion = roomName + "-" + aisleName + "-" + cabinetName + "-" + "B路";
                }else {
                    localtion = roomName + "-"  + cabinetName +  "-" + "B路";
                }
            }
            pduLineRes.setLocation(localtion);
            pduLineRes.setPduId(pduIndex.getId());
            pduLineRes.setDevKey(pduIndex.getDevKey());
            if(pageReqVO.getTimeType() == 0  || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                pageReqVO.setNewTime(LocalDateTime.now());
                pageReqVO.setOldTime(LocalDateTime.now().minusHours(24));
            } else {
                pageReqVO.setNewTime(pageReqVO.getNewTime().plusDays(1));
                pageReqVO.setOldTime(pageReqVO.getOldTime().plusDays(1));
            }

            MultiSearchRequest request = new MultiSearchRequest();

            for (int lineId = 1; lineId <= 3; lineId++) {
                SearchRequest searchRequest = null;

                if(pageReqVO.getTimeType() == 0 || pageReqVO.getOldTime().toLocalDate().equals(pageReqVO.getNewTime().toLocalDate())) {
                    searchRequest = new SearchRequest("pdu_hda_line_hour");
                } else {
                    searchRequest = new SearchRequest("pdu_hda_line_day");
                }

                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.size(0);

                sourceBuilder.query(QueryBuilders.boolQuery()
                        .filter(QueryBuilders.termQuery("pdu_id", pduIndex.getId()))
                        .filter(QueryBuilders.termQuery("line_id", lineId))
                        .filter(QueryBuilders.rangeQuery("create_time.keyword").gte(pageReqVO.getOldTime()).lte(pageReqVO.getNewTime())));

                sourceBuilder.aggregation(AggregationBuilders.max("vol_max").field("vol_max_value"));
                sourceBuilder.aggregation(AggregationBuilders.max("cur_max").field("cur_max_value"));
                sourceBuilder.aggregation(AggregationBuilders.max("pow_max").field("pow_active_max_value"));

                searchRequest.source(sourceBuilder);
                request.add(searchRequest);
            }
            try {
                MultiSearchResponse response = client.msearch(request, RequestOptions.DEFAULT);

                if(response.getResponses().length > 2){
                    pduLineRes.setL1MaxCur(((Max)(response.getResponses()[0].getResponse().getAggregations().get("cur_max"))).getValue());
                    if(((Max)(response.getResponses()[0].getResponse().getAggregations().get("cur_max"))).getValue() < 0){
                        continue;
                    }
                    pduLineRes.setL1MaxVol(((Max)(response.getResponses()[0].getResponse().getAggregations().get("vol_max"))).getValue());
                    pduLineRes.setL1MaxPow(((Max)(response.getResponses()[0].getResponse().getAggregations().get("pow_max"))).getValue());
                    pduLineRes.setL2MaxCur(((Max)(response.getResponses()[1].getResponse().getAggregations().get("cur_max"))).getValue());
                    pduLineRes.setL2MaxVol(((Max)(response.getResponses()[1].getResponse().getAggregations().get("vol_max"))).getValue());
                    pduLineRes.setL2MaxPow(((Max)(response.getResponses()[1].getResponse().getAggregations().get("pow_max"))).getValue());
                    pduLineRes.setL3MaxCur(((Max)(response.getResponses()[2].getResponse().getAggregations().get("cur_max"))).getValue());
                    pduLineRes.setL3MaxVol(((Max)(response.getResponses()[2].getResponse().getAggregations().get("vol_max"))).getValue());
                    pduLineRes.setL3MaxPow(((Max)(response.getResponses()[2].getResponse().getAggregations().get("pow_max"))).getValue());
                }else {
                    pduLineRes.setL1MaxCur(((Max)(response.getResponses()[0].getResponse().getAggregations().get("cur_max"))).getValue());
                    pduLineRes.setL1MaxVol(((Max)(response.getResponses()[0].getResponse().getAggregations().get("vol_max"))).getValue());
                    pduLineRes.setL1MaxPow(((Max)(response.getResponses()[0].getResponse().getAggregations().get("pow_max"))).getValue());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            result.add(pduLineRes);
        }

        return new PageResult<PDULineRes>(result,pduIndexPageResult.getTotal());
    }

    @Override
    public String getDisplayDataByDevKey(String devKey) {
        if (StringUtils.isEmpty(devKey)){
            return null;
        }else {
            ValueOperations ops = redisTemplate.opsForValue();
            JSONObject jsonObject = (JSONObject) ops.get("packet:pdu:" + devKey);
            return jsonObject != null ? jsonObject.toJSONString() : null;
        }
    }

    @Override
    public Map getHistoryDataByDevKey(String devKey,String type) {
        HashMap result = new HashMap<>();

        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        if(pduIndex != null){
            Long id = pduIndex.getId();
            // 构建查询请求
            SearchRequest searchRequest = null;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime pastTime = null;
            if("oneHour".equals(type)){
                pastTime = now.minusHours(1);
                pastTime = pastTime.minusMinutes(1);
                searchRequest = new SearchRequest("pdu_hda_total_realtime");
            } else if("twentyfourHour".equals(type)){
                pastTime = now.minusHours(25);
                searchRequest = new SearchRequest("pdu_hda_total_hour");
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
            List<Double> apparentList = new ArrayList<>();
            List<Double> activeList = new ArrayList<>();
            List<String> dateTimes = new ArrayList<>();
            // 执行查询请求
            try {
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                if(searchResponse != null){
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit hit : hits) {
                        String str = hit.getSourceAsString();
                        if("oneHour".equals(type)){
                            PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                            apparentList.add(Double.valueOf(pduHdaTotalRealtimeDo.getApparentPow()));
                            activeList.add(Double.valueOf(pduHdaTotalRealtimeDo.getActivePow()));
                            dateTimes.add(pduHdaTotalRealtimeDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
                        } else if("twentyfourHour".equals(type)){
                            PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaTotalHourDo.class);
                            apparentList.add(Double.valueOf(pduHdaTotalHourDo.getApparentPowAvgValue()));
                            activeList.add(Double.valueOf(pduHdaTotalHourDo.getActivePowAvgValue()));
                            dateTimes.add(pduHdaTotalHourDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            result.put("apparentList",apparentList);
            result.put("activeList",activeList);
            result.put("dateTimes",dateTimes);

            return result;
        } else{
            return result;
        }
    }

    @Override
    public Map getChartNewDataByPduDevKey(String devKey,LocalDateTime oldTime,String type) {
        HashMap result = new HashMap<>();

        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        if(pduIndex != null){
            Long id = pduIndex.getId();
            // 构建查询请求
            SearchRequest searchRequest = null;
            LocalDateTime newTime = null;
            if("oneHour".equals(type)){
                newTime = oldTime.plusMinutes(1);
                newTime = newTime.plusSeconds(20);
                // 构建查询请求
                searchRequest = new SearchRequest("pdu_hda_total_realtime");
            } else if("twentyfourHour".equals(type)){
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
                if(searchResponse != null){
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit hit : hits) {
                        String str = hit.getSourceAsString();
                        if("oneHour".equals(type)){
                            PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                            apparent = pduHdaTotalRealtimeDo.getApparentPow();
                            active = pduHdaTotalRealtimeDo.getActivePow();
                            dateTime = pduHdaTotalRealtimeDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss");
                        } else if("twentyfourHour".equals(type)){
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

            result.put("apparent",apparent);
            result.put("active",active);
            result.put("dateTime",dateTime);

            return result;
        } else {
            return result;
        }

    }

    @Override
    public Map getReportConsumeDataByDevKey(String devKey,Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        if(pduIndex != null){
            Long pduId = pduIndex.getId();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                if(oldTime.equals(newTime)){
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest pduRealTimeDisRequest = new SearchRequest("pdu_ele_total_realtime");
                SearchSourceBuilder pduRealTimeDisSourceBuilder = new SearchSourceBuilder();

                pduRealTimeDisSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));

                pduRealTimeDisSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                pduRealTimeDisSourceBuilder.size(24); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduRealTimeDisRequest.source(pduRealTimeDisSourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduRealTimeDisRequest);
                double todayEle = 0;
                List<Double> eq = new ArrayList<>();
                List<String> time = new ArrayList<>();
                double maxEle = -1;
                DateTime maxEleTime = new DateTime();
                double lastEq = 0;
                double firstEq = 0;
                int index = 0;
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduEleTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(pduEleTotalRealDisResponse != null && pduEleTotalRealDisResponse.getHits().getTotalHits().value > 1 ){
                        for (SearchHit hit : pduEleTotalRealDisResponse.getHits()) {
                            index++;
                            PduEleTotalRealtimeDo pduEleTotalRealtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEleTotalRealtimeDo.class);
                            if(index == 1){
                                firstEq = pduEleTotalRealtimeDo.getEle();
                            }
                            double eleValue  = pduEleTotalRealtimeDo.getEle() - lastEq;
                            DateTime createTime = pduEleTotalRealtimeDo.getCreateTime();
                            if(eleValue > maxEle){
                                maxEle = eleValue;
                                maxEleTime = createTime;
                            }
                            lastEq = pduEleTotalRealtimeDo.getEle();
                            if (index > 1){
                                eq.add(eleValue);
                                time.add(createTime.toString("yyyy-MM-dd HH:mm"));
                            }
                        }
                    }
                    result.put("eq",eq);
                    result.put("time",time);
                    result.put("totalEle",todayEle);
                    result.put("maxEle",maxEle != -1 ? maxEle : null);
                    result.put("maxEleTime",maxEle != -1 ? maxEleTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("firstEq",firstEq);
                    result.put("lastEq",lastEq);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建聚合搜索请求
                SearchRequest pduEqTotalDayRequest = new SearchRequest("pdu_eq_total_day");
                SearchSourceBuilder pduEqTotalDaySourceBuilder = new SearchSourceBuilder();
                // 设置时间范围查询条件

                pduEqTotalDaySourceBuilder.query(QueryBuilders.boolQuery()
                        //今天的数据 pdu_ele_total_realtime的时间范围查询必须使用字符串
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("pdu_id", pduId))); // 添加pdu_id条件
                // 设置聚合条件
                pduEqTotalDaySourceBuilder.aggregation(AggregationBuilders.sum("total_eq")
                        .field("eq_value"));
                // 将搜索条件添加到请求中
                pduEqTotalDayRequest.source(pduEqTotalDaySourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduEqTotalDayRequest);

                // 创建时间分布搜索请求
                SearchRequest pduRealTimeDisRequest = new SearchRequest("pdu_eq_total_day");
                SearchSourceBuilder pduRealTimeDisSourceBuilder = new SearchSourceBuilder();

                pduRealTimeDisSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));

                pduRealTimeDisSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                pduRealTimeDisSourceBuilder.size(31); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduRealTimeDisRequest.source(pduRealTimeDisSourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduRealTimeDisRequest);
                double totalEq = 0;
                List<Double> eq = new ArrayList<>();
                List<String> time = new ArrayList<>();
                double maxEq = -1;
                DateTime maxEleTime = new DateTime();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析第一个搜索请求的聚合结果
                    SearchResponse pduEleTotalRealResponse = multiSearchResponse.getResponses()[0].getResponse();
                    Sum sumAggregation1 = pduEleTotalRealResponse.getAggregations().get("total_eq");
                    totalEq = sumAggregation1.getValue();

                    // 解析第二个搜索请求
                    SearchResponse pduEqTotalDayDisResponse = multiSearchResponse.getResponses()[1].getResponse();
                    if(pduEqTotalDayDisResponse != null){
                        for (SearchHit hit : pduEqTotalDayDisResponse.getHits()) {
                            PduEqTotalDayDo pduEqTotalDayDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEqTotalDayDo.class);
                            double eqValue  = pduEqTotalDayDo.getEq();
                            DateTime startTime = pduEqTotalDayDo.getStartTime();
                            if(eqValue > maxEq){
                                maxEq = eqValue;
                                maxEleTime = startTime;
                            }
                            eq.add(eqValue);
                            time.add(startTime.toString("yyyy-MM-dd "));
                        }
                    }
                    result.put("eq",eq);
                    result.put("time",time);
                    result.put("totalEle",totalEq);
                    result.put("maxEle",maxEq != -1 ? maxEq : null);
                    result.put("maxEleTime",maxEq != -1 ? maxEleTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        } else{
            return result;
        }
    }

    @Override
    public Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        if(pduIndex != null){
            Long pduId = pduIndex.getId();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                if(oldTime.equals(newTime)){
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest pduPowTotalRealRequest = new SearchRequest("pdu_hda_total_hour");
                SearchSourceBuilder pduPowTotalRealSourceBuilder = new SearchSourceBuilder();

                pduPowTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));

                pduPowTotalRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                pduPowTotalRealSourceBuilder.size(24); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduPowTotalRealRequest.source(pduPowTotalRealSourceBuilder);
                // 将第二个搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduPowTotalRealRequest);

                List<Double> activePowAvgValue = new ArrayList<>();
                List<Double> apparentPowAvgValue = new ArrayList<>();
                List<String> time = new ArrayList<>();
                double apparentPowMaxValue = -1;
                DateTime apparentPowMaxTime = new DateTime();
                double apparentPowMinValue = Double.MAX_VALUE;
                DateTime apparentPowMinTime = new DateTime();
                double activePowMaxValue = -1;
                DateTime activePowMaxTime = new DateTime();
                double activePowMinValue = Double.MAX_VALUE;
                DateTime activePowMinTime = new DateTime();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(pduPowTotalRealDisResponse != null){
                        for (SearchHit hit : pduPowTotalRealDisResponse.getHits()) {
                            PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduHdaTotalHourDo.class);
                            double activePowAvg  = pduHdaTotalHourDo.getActivePowAvgValue();
                            double apparentPowAvg = pduHdaTotalHourDo.getApparentPowAvgValue();
                            DateTime createTime = pduHdaTotalHourDo.getCreateTime();
                            if(pduHdaTotalHourDo.getApparentPowMaxValue() > apparentPowMaxValue){
                                apparentPowMaxValue = pduHdaTotalHourDo.getApparentPowMaxValue();
                                apparentPowMaxTime = pduHdaTotalHourDo.getApparentPowMaxTime();
                            }
                            if(pduHdaTotalHourDo.getApparentPowMinValue() < apparentPowMinValue ){
                                apparentPowMinValue = pduHdaTotalHourDo.getApparentPowMinValue();
                                apparentPowMinTime = pduHdaTotalHourDo.getApparentPowMinTime();
                            }
                            if(pduHdaTotalHourDo.getActivePowMaxValue() > activePowMaxValue){
                                activePowMaxValue = pduHdaTotalHourDo.getActivePowMaxValue();
                                activePowMaxTime = pduHdaTotalHourDo.getActivePowMaxTime();
                            }
                            if(pduHdaTotalHourDo.getActivePowMinValue() < activePowMinValue ){
                                activePowMinValue = pduHdaTotalHourDo.getActivePowMinValue();
                                activePowMinTime = pduHdaTotalHourDo.getActivePowMinTime();
                            }
                            activePowAvgValue.add(activePowAvg);
                            apparentPowAvgValue.add(apparentPowAvg);
                            time.add(createTime.toString("yyyy-MM-dd HH:mm"));
                        }
                    }
                    result.put("activePowAvgValue",activePowAvgValue);
                    result.put("apparentPowAvgValue",apparentPowAvgValue);
                    result.put("time",time);
                    result.put("apparentPowMaxValue",apparentPowMaxValue != -1 ? apparentPowMaxValue : null);
                    result.put("apparentPowMaxTime",apparentPowMaxValue != -1 ? apparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("apparentPowMinValue",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinValue : null);
                    result.put("apparentPowMinTime",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMaxValue",activePowMaxValue != -1 ? activePowMaxValue : null);
                    result.put("activePowMaxTime",activePowMaxValue != -1 ? activePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMinValue",activePowMinValue != Double.MAX_VALUE ? activePowMinValue : null);
                    result.put("activePowMinTime",activePowMinValue != Double.MAX_VALUE ? activePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest pduPowTotalRealRequest = new SearchRequest("pdu_hda_total_day");
                SearchSourceBuilder pduPowTotalRealSourceBuilder = new SearchSourceBuilder();

                pduPowTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));

                pduPowTotalRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                pduPowTotalRealSourceBuilder.size(31); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduPowTotalRealRequest.source(pduPowTotalRealSourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduPowTotalRealRequest);

                List<Double> activePowAvgValue = new ArrayList<>();
                List<Double> apparentPowAvgValue = new ArrayList<>();
                List<String> time = new ArrayList<>();
                double apparentPowMaxValue = -1;
                DateTime apparentPowMaxTime = new DateTime();
                double apparentPowMinValue = Double.MAX_VALUE;
                DateTime apparentPowMinTime = new DateTime();
                double activePowMaxValue = -1;
                DateTime activePowMaxTime = new DateTime();
                double activePowMinValue = Double.MAX_VALUE;
                DateTime activePowMinTime = new DateTime();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduPowTotalDayDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(pduPowTotalDayDisResponse != null){
                        for (SearchHit hit : pduPowTotalDayDisResponse.getHits()) {
                            PduHdaTotalDayDo pduHdaTotalHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduHdaTotalDayDo.class);
                            double activePowAvg  = pduHdaTotalHourDo.getActivePowAvgValue();
                            double apparentPowAvg = pduHdaTotalHourDo.getApparentPowAvgValue();
                            DateTime createTime = new DateTime(pduHdaTotalHourDo.getCreateTime().toLocalDateTime().minusDays(1));
                            if(pduHdaTotalHourDo.getApparentPowMaxValue() > apparentPowMaxValue){
                                apparentPowMaxValue = pduHdaTotalHourDo.getApparentPowMaxValue();
                                apparentPowMaxTime = pduHdaTotalHourDo.getApparentPowMaxTime();
                            }
                            if(pduHdaTotalHourDo.getApparentPowMinValue() < apparentPowMinValue ){
                                apparentPowMinValue = pduHdaTotalHourDo.getApparentPowMinValue();
                                apparentPowMinTime = pduHdaTotalHourDo.getApparentPowMinTime();
                            }
                            if(pduHdaTotalHourDo.getActivePowMaxValue() > activePowMaxValue){
                                activePowMaxValue = pduHdaTotalHourDo.getActivePowMaxValue();
                                activePowMaxTime = pduHdaTotalHourDo.getActivePowMaxTime();
                            }
                            if(pduHdaTotalHourDo.getActivePowMinValue() < activePowMinValue ){
                                activePowMinValue = pduHdaTotalHourDo.getActivePowMinValue();
                                activePowMinTime = pduHdaTotalHourDo.getActivePowMinTime();
                            }
                            activePowAvgValue.add(activePowAvg);
                            apparentPowAvgValue.add(apparentPowAvg);
                            time.add(createTime.toString("yyyy-MM-dd"));
                        }
                    }
                    result.put("activePowAvgValue",activePowAvgValue);
                    result.put("apparentPowAvgValue",apparentPowAvgValue);
                    result.put("time",time);
                    result.put("apparentPowMaxValue",apparentPowMaxValue != -1 ? apparentPowMaxValue : null);
                    result.put("apparentPowMaxTime",apparentPowMaxValue != -1 ? apparentPowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("apparentPowMinValue",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinValue : null);
                    result.put("apparentPowMinTime",apparentPowMinValue != Double.MAX_VALUE ? apparentPowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMaxValue",activePowMaxValue != -1 ? activePowMaxValue : null);
                    result.put("activePowMaxTime",activePowMaxValue != -1 ? activePowMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("activePowMinValue",activePowMinValue != Double.MAX_VALUE ? activePowMinValue : null);
                    result.put("activePowMinTime",activePowMinValue != Double.MAX_VALUE ? activePowMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }else {
            return result;
        }
    }

    @Override
    public Map getReportOutLetDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        if(pduIndex != null){
            Long pduId = pduIndex.getId();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                if(oldTime.equals(newTime)){
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest pduOutLetTotalRealRequest = new SearchRequest("pdu_ele_outlet");
                SearchSourceBuilder pduOutLetTotalRealSourceBuilder = new SearchSourceBuilder();

                pduOutLetTotalRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));
                //映射sum的sum_ele_active为sumEle

                pduOutLetTotalRealSourceBuilder.aggregation(
                        AggregationBuilders.terms("group_by_outlet").field("outlet_id").size(10)
                                .subAggregation(AggregationBuilders.min("min_ele_active").field("ele_active"))
                                .subAggregation(AggregationBuilders.max("max_ele_active").field("ele_active")));

//
//                Map<String, String> map = new HashMap<>();
//                map.put("sumEle", "sum_ele_active");
//                pduOutLetTotalRealSourceBuilder.aggregation(AggregationBuilders
//                        .terms("by_outlet_id").field("outlet_id").order(BucketOrder.aggregation("sum_ele_active",true)).size(24)
//                        .subAggregation(AggregationBuilders.sum("sum_ele_active").field("ele_active"))
//                        //筛选sumEle > 0的
//                        .subAggregation(PipelineAggregatorBuilders.bucketSelector("positive_sum_ele",map, new Script("params.sumEle > 0"))));


                pduOutLetTotalRealSourceBuilder.size(0); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduOutLetTotalRealRequest.source(pduOutLetTotalRealSourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduOutLetTotalRealRequest);

                List<Double> eleValue = new ArrayList<>();
                List<String> outLetId = new ArrayList<>();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(pduPowTotalRealDisResponse != null){
                        for ( Terms.Bucket bucket : ((ParsedLongTerms) pduPowTotalRealDisResponse.getAggregations().get("group_by_outlet")).getBuckets()) {
                            String outlet = bucket.getKeyAsString();
                            Min minEleActive = bucket.getAggregations().get("min_ele_active");
                            Max maxEleActive = bucket.getAggregations().get("max_ele_active");

                            double minEleActiveValue = minEleActive.getValue();
                            double maxEleActiveValue = maxEleActive.getValue();
                            double sumEleActive = maxEleActiveValue - minEleActiveValue;

                            eleValue.add(sumEleActive);
                            outLetId.add(outlet);
                        }
                    }

                    // 创建一个自定义的Comparator，用于eleValue的降序排序
                    Comparator<Integer> comparator = (a, b) -> Double.compare(eleValue.get(a), eleValue.get(b));

                    // 创建一个索引列表，用于保存eleValue的原始索引位置
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < eleValue.size(); i++) {
                        indexes.add(i);
                    }

                    // 使用Collections.sort()方法对indexes进行排序，并根据indexes的排序结果更新eleValue和outLetId列表
                    Collections.sort(indexes, comparator);
                    List<Double> sortedEleValue = new ArrayList<>();
                    List<String> sortedOutLetId = new ArrayList<>();
                    for (int i = 0; i < indexes.size(); i++) {
                        int index = indexes.get(i);
                        sortedEleValue.add(eleValue.get(index));
                        sortedOutLetId.add(outLetId.get(index));
                    }

                    result.put("eleValue",sortedEleValue);
                    result.put("outLetId",sortedOutLetId);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
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
                        .terms("by_outlet_id").field("outlet_id").order(BucketOrder.aggregation("sum_eq",true)).size(24)
                        .subAggregation(AggregationBuilders.sum("sum_eq").field("eq_value"))
                        //筛选sumEle > 0的
                        .subAggregation(PipelineAggregatorBuilders.bucketSelector("positive_sum_eq",map, new Script("params.sumEq > 0"))));

                pduOutLetTotalDaySourceBuilder.size(0); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduOutLetTotalDayRequest.source(pduOutLetTotalDaySourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduOutLetTotalDayRequest);

                List<Double> eqValue = new ArrayList<>();
                List<String> outLetId = new ArrayList<>();
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduPowTotalRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(pduPowTotalRealDisResponse != null){
                        for ( Terms.Bucket bucket : ((ParsedLongTerms) pduPowTotalRealDisResponse.getAggregations().get("by_outlet_id")).getBuckets()) {
                            String outlet = bucket.getKeyAsString();
                            Sum sum = bucket.getAggregations().get("sum_eq");
                            double sumEleActive = sum.getValue();
                            eqValue.add(sumEleActive);
                            outLetId.add(outlet);
                        }
                    }
                    result.put("eleValue",eqValue);
                    result.put("outLetId",outLetId);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return result;
        } else {
            return result;
        }

    }

    @Override
    public Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime) {
        Map result = new HashMap<>();
        PduIndex pduIndex = pDUDeviceMapper.selectOne(new LambdaQueryWrapperX<PduIndex>().eq(PduIndex::getDevKey, devKey));
        if(pduIndex != null){
            Long pduId = pduIndex.getId();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if(timeType.equals(0) || oldTime.toLocalDate().equals(newTime.toLocalDate())){
                if(oldTime.equals(newTime)){
                    newTime = newTime.withHour(23).withMinute(59).withSecond(59);
                }
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest pduTemRealRequest = new SearchRequest("pdu_env_hour");
                SearchSourceBuilder pduTemRealSourceBuilder = new SearchSourceBuilder();

                pduTemRealSourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime)).lte(formatter.format(newTime)))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));


                pduTemRealSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                pduTemRealSourceBuilder.size(24 * 4); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduTemRealRequest.source(pduTemRealSourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduTemRealRequest);

                List<List<Double>> temAvgValue = new ArrayList<>();
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                List<List<String>> time = new ArrayList<>();
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                double temMaxValue = -255;
                DateTime temMaxTime = null;
                int temMaxSensorId = -1;
                double temMinValue = Double.MAX_VALUE;
                DateTime temMinTime = null;
                int temMinSensorId = -1;
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduTemRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(pduTemRealDisResponse != null) {
                        for (SearchHit hit : pduTemRealDisResponse.getHits()) {
                            PduEnvHourDo pduEnvHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEnvHourDo.class);
                            double temAvg = pduEnvHourDo.getTemAvgValue();
                            DateTime createTime = pduEnvHourDo.getCreateTime();
                            if (pduEnvHourDo.getTemMaxValue() > temMaxValue) {
                                temMaxValue = pduEnvHourDo.getTemMaxValue();
                                temMaxTime = pduEnvHourDo.getTemMaxTime();
                                temMaxSensorId = pduEnvHourDo.getSensorId();
                            }
                            if (pduEnvHourDo.getTemMinValue() > 0 && pduEnvHourDo.getTemMinValue() < temMinValue) {
                                temMinValue = pduEnvHourDo.getTemMinValue();
                                temMinTime = pduEnvHourDo.getTemMinTime();
                                temMinSensorId = pduEnvHourDo.getSensorId();
                            }
                            temAvgValue.get(pduEnvHourDo.getSensorId()).add(temAvg);
                            time.get(pduEnvHourDo.getSensorId()).add(createTime.toString("yyyy-MM-dd HH:mm"));
                        }
                    }
                    result.put("temAvgValue",temAvgValue);
                    result.put("time",time);
                    result.put("temMaxValue",temMaxValue != -255 ? temMaxValue : null);
                    result.put("temMaxTime",temMaxValue != -255 ? temMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("temMaxSensorId",temMaxValue != -255 ? temMaxSensorId : null);
                    result.put("temMinValue",temMinValue != Double.MAX_VALUE ? temMinValue : null);
                    result.put("temMinTime",temMinValue != Double.MAX_VALUE ? temMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("temMinSensorId",temMinValue != Double.MAX_VALUE ? temMinSensorId : null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                MultiSearchRequest multiSearchRequest = new MultiSearchRequest();

                // 创建时间分布搜索请求
                SearchRequest pduTemDayRequest = new SearchRequest("pdu_env_day");
                SearchSourceBuilder pduTemDaySourceBuilder = new SearchSourceBuilder();

                pduTemDaySourceBuilder.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("create_time.keyword").gte(formatter.format(oldTime.plusDays(1))).lte(formatter.format(newTime.plusDays(1))))
                        .must(QueryBuilders.termQuery("pdu_id", pduId)));


                pduTemDaySourceBuilder.sort("create_time.keyword", SortOrder.ASC);
                pduTemDaySourceBuilder.size(31 * 4); // 设置返回的最大结果数
                // 将搜索条件添加到请求中
                pduTemDayRequest.source(pduTemDaySourceBuilder);
                // 将第搜索请求添加到多索引搜索请求中
                multiSearchRequest.add(pduTemDayRequest);

                List<List<Double>> temAvgValue = new ArrayList<>();
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                temAvgValue.add(new ArrayList<>());
                List<List<String>> time = new ArrayList<>();
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                time.add(new ArrayList<>());
                double temMaxValue = -255;
                DateTime temMaxTime = null;
                int temMaxSensorId = -1;
                double temMinValue = Double.MAX_VALUE;
                DateTime temMinTime = null;
                int temMinSensorId = -1;
                try {
                    // 执行多索引搜索请求
                    MultiSearchResponse multiSearchResponse = client.msearch(multiSearchRequest, RequestOptions.DEFAULT);

                    // 解析搜索请求
                    SearchResponse pduTemRealDisResponse = multiSearchResponse.getResponses()[0].getResponse();
                    if(pduTemRealDisResponse != null) {
                        for (SearchHit hit : pduTemRealDisResponse.getHits()) {
                            PduEnvDayDo pduEnvHourDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEnvDayDo.class);
                            double temAvg = pduEnvHourDo.getTemAvgValue();
                            DateTime createTime = new DateTime(pduEnvHourDo.getCreateTime().toLocalDateTime().minusDays(1));
                            if (pduEnvHourDo.getTemMaxValue() > temMaxValue) {
                                temMaxValue = pduEnvHourDo.getTemMaxValue();
                                temMaxTime = pduEnvHourDo.getTemMaxTime();
                                temMaxSensorId = pduEnvHourDo.getSensorId();
                            }
                            if (pduEnvHourDo.getTemMinValue() > 0 && pduEnvHourDo.getTemMinValue() < temMinValue) {
                                temMinValue = pduEnvHourDo.getTemMinValue();
                                temMinTime = pduEnvHourDo.getTemMinTime();
                                temMinSensorId = pduEnvHourDo.getSensorId();
                            }
                            temAvgValue.get(pduEnvHourDo.getSensorId()).add(temAvg);
                            time.get(pduEnvHourDo.getSensorId()).add(createTime.toString("yyyy-MM-dd"));
                        }
                    }
                    result.put("temAvgValue",temAvgValue);
                    result.put("time",time);
                    result.put("temMaxValue",temMaxValue != -255 ? temMaxValue : null);
                    result.put("temMaxTime",temMaxValue != -255 ? temMaxTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("temMaxSensorId",temMaxValue != -255 ? temMaxSensorId : null);
                    result.put("temMinValue",temMinValue != Double.MAX_VALUE ? temMinValue : null);
                    result.put("temMinTime",temMinValue != Double.MAX_VALUE ? temMinTime.toString("yyyy-MM-dd HH:mm:ss") : null);
                    result.put("temMinSensorId",temMinValue != Double.MAX_VALUE ? temMinSensorId : null);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }else{
            return result;
        }
    }



}