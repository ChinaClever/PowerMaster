package cn.iocoder.yudao.module.pdu.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.cabinet.mapper.AisleIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetPduMapper;
import cn.iocoder.yudao.module.cabinet.mapper.RoomIndexMapper;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.EnvDataDetailsReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataPageReqVO;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndex;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndexMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.*;


/**
 * @author linzhentian
 */
@Service
public class HistoryDataServiceImpl implements HistoryDataService {

    @Autowired
    private PduIndexMapper pduIndexMapper;

    @Autowired
    private CabinetPduMapper cabinetPduMapper;

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;

    @Autowired
    private AisleIndexMapper aisleIndexMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public List<Object> getLocationsByPduIds(List<Map<String, Object>> mapList) {
        List<Object> resultList = new ArrayList<>();
        for (Map<String, Object> map : mapList){
            Object pduId = map.get("pdu_id");
            if (pduId instanceof Integer) {
                // 查询位置
                PduIndex pduIndex = pduIndexMapper.selectById( (int)pduId );
                if (pduIndex != null){
                    map.put("location", pduIndex.getDevKey());
                    map.put("address", getAddressByLocation(pduIndex.getDevKey()));
                }else{
                    map.put("location", null);
                    map.put("address", null);
                }
            }else{
                map.put("location", null);
                map.put("address", null);
            }
            resultList.add(map);
        }
        return resultList;
    }

    @Override
    public String getAddressByLocation(String location) {
        String[] ipParts = location.split("-");
        String address = null;
        CabinetPdu cabinetPduA = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
                .eq(CabinetPdu::getPduIpA, ipParts[0])
                .eq(CabinetPdu::getCasIdA, ipParts[1]));
        CabinetPdu cabinetPduB = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
                .eq(CabinetPdu::getPduIpB, ipParts[0])
                .eq(CabinetPdu::getCasIdB, ipParts[1]));
        if(cabinetPduA != null){
            int cabinetId = cabinetPduA.getCabinetId();
            CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
            String cabinetName = cabinet.getName();
            String roomName = roomIndexMapper.selectById(cabinet.getRoomId()).getName();
            if(cabinet.getAisleId() != 0){
                String aisleName = aisleIndexMapper.selectById(cabinet.getAisleId()).getName();
                address = roomName + "-" + aisleName + "-" + cabinetName + "-" + "A路";
            }else {
                address = roomName + "-"  + cabinetName +  "-" + "A路";
            }
        }
        if(cabinetPduB != null){
            int cabinetId = cabinetPduB.getCabinetId();
            CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
            String cabinetName = cabinet.getName();
            String roomName = roomIndexMapper.selectById(cabinet.getRoomId()).getName();
            if(cabinet.getAisleId() != 0){
                String aisleName = aisleIndexMapper.selectById(cabinet.getAisleId()).getName();
                address = roomName + "-" + aisleName + "-" + cabinetName + "-" + "B路";
            }else {
                address = roomName + "-"  + cabinetName +  "-" + "B路";
            }
        }
        return address;
    }

    @Override
    public Integer getPduIdByAddr(String ipAddr, String cascadeAddr) {
        String devKey = ipAddr+"-"+cascadeAddr;
        QueryWrapper<PduIndex> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dev_key", devKey); // 指定查询条件：name 字段等于给定的 name 值
        PduIndex pduIndex = pduIndexMapper.selectOne(queryWrapper); // 执行查询，返回匹配的实体对象
        if (pduIndex != null){
            return Math.toIntExact(pduIndex.getId());
        }
        return null;
    }

    @Override
    public Map getHistoryDataTypeMaxValue() throws IOException {
        HashMap resultMap = new HashMap<>();
        String[] indexArr = new String[]{"pdu_hda_line_realtime", "pdu_hda_loop_realtime", "pdu_hda_outlet_realtime"};
        String[] fieldNameArr = new String[]{"line_id", "loop_id", "outlet_id"};
        for (int i = 0; i < indexArr.length; i++) {
            SearchRequest searchRequest = new SearchRequest(indexArr[i]);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // 添加最大值聚合
            searchSourceBuilder.aggregation(
                    AggregationBuilders.max("max_value").field(fieldNameArr[i])
            );
            searchRequest.source(searchSourceBuilder);
            // 执行搜索请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            // 从聚合结果中获取最大值
            Max maxAggregation = searchResponse.getAggregations().get("max_value");
            Integer maxValue = (int) maxAggregation.getValue();
            resultMap.put(fieldNameArr[i]+"_max_value", maxValue);
        }

        return resultMap;
    }

    @Override
    public Map getSensorIdMaxValue() throws IOException {
        HashMap resultMap = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest("pdu_env_realtime");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 添加最大值聚合
        searchSourceBuilder.aggregation(
                AggregationBuilders.max("max_value").field("sensor_id")
        );
        searchRequest.source(searchSourceBuilder);
        // 执行搜索请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 从聚合结果中获取最大值
        Max maxAggregation = searchResponse.getAggregations().get("max_value");
        Integer maxValue = (int) maxAggregation.getValue();
        resultMap.put("sensor_id_max_value", maxValue);
        return resultMap;
    }

    @Override
    public PageResult<Object> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = null;
        // 创建BoolQueryBuilder对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int pageNo = pageReqVO.getPageNo();
        int pageSize = pageReqVO.getPageSize();
        int index = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(index);
        // 最后一页请求超过一万，pageSize设置成请求刚好一万条
        if (index + pageSize > 10000){
            searchSourceBuilder.size(10000 - index);
        }else{
            searchSourceBuilder.size(pageSize);
        }
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
        Integer pduId = null;
        if (!Objects.equals(pageReqVO.getIpAddr(), "") && !Objects.equals(pageReqVO.getIpAddr(), null)){
            pduId = getPduIdByAddr(pageReqVO.getIpAddr(), pageReqVO.getCascadeAddr());
            if(pduId != null){
                searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
            }else{
                // 查不到pdu 直接返回空数据
                pageResult = new PageResult<>();
                pageResult.setList(null)
                        .setTotal(0L);
                return pageResult;
            }
        }else{
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        }
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        switch (pageReqVO.getType()) {
            case "total":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_total_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_total_hour");
                } else {
                    searchRequest.indices("pdu_hda_total_day");
                }
                break;
            case "line":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_line_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_line_hour");
                } else {
                    searchRequest.indices("pdu_hda_line_day");
                }
                if( pageReqVO.getLineId() != null){
                    Integer lineId = pageReqVO.getLineId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery ("line_id", lineId);
                    if (pduId != null) {
                        QueryBuilder termQuery1 = QueryBuilders.termQuery("pdu_id", pduId);
                        boolQuery.must(termQuery1);
                    }
                    boolQuery.must(termQuery);
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                break;

            case "loop":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_loop_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_loop_hour");
                } else {
                    searchRequest.indices("pdu_hda_loop_day");
                }
                if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
                    searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(pageReqVO.getTimeRange()[0])
                            .to(pageReqVO.getTimeRange()[1]));
                }
                if( pageReqVO.getLoopId() != null){
                    Integer loopId = pageReqVO.getLoopId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("loop_id", loopId);
                    boolQuery.must(termQuery);
                    if (pduId != null) {
                        QueryBuilder termQuery1 = QueryBuilders.termQuery("pdu_id", pduId);
                        boolQuery.must(termQuery1);
                    }
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                break;

            case "outlet":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_outlet_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_outlet_hour");
                } else {
                    searchRequest.indices("pdu_hda_outlet_day");
                }
                if( pageReqVO.getOutletId() != null){
                    Integer outletId = pageReqVO.getOutletId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("outlet_id", outletId);
                    boolQuery.must(termQuery);
                    if (pduId != null) {
                        QueryBuilder termQuery1 = QueryBuilders.termQuery("pdu_id", pduId);
                        boolQuery.must(termQuery1);
                    }
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                break;
            default:
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(searchHit -> mapList.add(searchHit.getSourceAsMap()));
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回的结果
        pageResult = new PageResult<>();
        pageResult.setList(getLocationsByPduIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getHistoryDataDetails(HistoryDataDetailsReqVO reqVO) throws IOException{
        Integer pduId = reqVO.getPduId();
        if (Objects.equals(pduId, null)){
            pduId = getPduIdByAddr(reqVO.getIpAddr(), reqVO.getCascadeAddr());
            if (Objects.equals(pduId, null)){
                return null;
            }
        }
        // 创建BoolQueryBuilder对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqVO.getTimeRange()[0])
                    .to(reqVO.getTimeRange()[1]));
        }
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        PageResult<Object> pageResult = null;
        switch (reqVO.getType()) {
            case "total":
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_total_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_total_hour");
                }else {
                    searchRequest.indices("pdu_hda_total_day");
                }
                searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
                break;

            case "line":
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_line_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_line_hour");
                }else {
                    searchRequest.indices("pdu_hda_line_day");
                }
                Integer lineId = reqVO.getLineId();
                // 创建范围查询
                QueryBuilder termQuery = QueryBuilders.termQuery("pdu_id", pduId);
                // 创建匹配查询
                QueryBuilder termQuery1 = QueryBuilders.termQuery("line_id", lineId);
                // 将范围查询和匹配查询添加到布尔查询中
                boolQuery.must(termQuery);
                boolQuery.must(termQuery1);
                // 将布尔查询设置到SearchSourceBuilder中
                searchSourceBuilder.query(boolQuery);
                break;

            case "loop":
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_loop_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_loop_hour");
                }else {
                    searchRequest.indices("pdu_hda_loop_day");
                }
                Integer loopId = reqVO.getLoopId();
                // 创建范围查询
                termQuery = QueryBuilders.termQuery("pdu_id", pduId);
                // 创建匹配查询
                termQuery1 = QueryBuilders.termQuery("loop_id", loopId);
                // 将范围查询和匹配查询添加到布尔查询中
                boolQuery.must(termQuery);
                boolQuery.must(termQuery1);
                // 将布尔查询设置到SearchSourceBuilder中
                searchSourceBuilder.query(boolQuery);
                break;

            case "outlet":
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_outlet_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_outlet_hour");
                }else {
                    searchRequest.indices("pdu_hda_outlet_day");
                }
                Integer outletId = reqVO.getOutletId();
                // 创建范围查询
                termQuery = QueryBuilders.termQuery("pdu_id", pduId);
                // 创建匹配查询
                termQuery1 = QueryBuilders.termQuery("outlet_id", outletId);
                // 将范围查询和匹配查询添加到布尔查询中
                boolQuery.must(termQuery);
                boolQuery.must(termQuery1);
                // 将布尔查询设置到SearchSourceBuilder中
                searchSourceBuilder.query(boolQuery);
                break;
            default:
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        List<Object> resultList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(searchHit -> resultList.add(searchHit.getSourceAsMap()));
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回的结果
        pageResult = new PageResult<>();
        pageResult.setList(resultList)
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getEnvDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = null;
        Integer sensorId = pageReqVO.getSensorId();
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int pageNo = pageReqVO.getPageNo();
        int pageSize = pageReqVO.getPageSize();
        int index = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(index);
        // 最后一页请求超过一万，pageSize设置成请求刚好一万条
        if (index + pageSize > 10000){
            searchSourceBuilder.size(10000 - index);
        }else{
            searchSourceBuilder.size(pageSize);
        }
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
        if (!Objects.equals(pageReqVO.getIpAddr(), "") && !Objects.equals(pageReqVO.getIpAddr(), null)){
            Integer pduId = getPduIdByAddr(pageReqVO.getIpAddr(), pageReqVO.getCascadeAddr());
            if(pduId != null){
                if (!Objects.equals(sensorId, 0)){
                    // 创建范围查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("pdu_id", pduId);
                    // 创建匹配查询
                    QueryBuilder termQuery1 = QueryBuilders.termQuery("sensor_id", sensorId);
                    // 创建BoolQueryBuilder对象
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    // 将范围查询和匹配查询添加到布尔查询中
                    boolQuery.must(termQuery);
                    boolQuery.must(termQuery1);
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }else{
                    searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
                }
            }else{
                // 查不到pdu 直接返回空数据
                pageResult = new PageResult<>();
                pageResult.setList(null)
                        .setTotal(0L);
                return pageResult;
            }
        }else{
            if (!Objects.equals(sensorId, 0)){
                QueryBuilder termQuery = QueryBuilders.termQuery("sensor_id", sensorId);
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                boolQuery.must(termQuery);
                searchSourceBuilder.query(boolQuery);
            }else{
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            }
        }

        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if ("realtime".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("pdu_env_realtime");
        } else if ("hour".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("pdu_env_hour");
        } else {
            searchRequest.indices("pdu_env_day");
        }
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(searchHit -> mapList.add(searchHit.getSourceAsMap()));
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回的结果
        pageResult = new PageResult<>();
        pageResult.setList(getLocationsByPduIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getEnvDataDetails(EnvDataDetailsReqVO reqVO) throws IOException {
        Integer sensorId = reqVO.getSensorId();
        Integer pduId = reqVO.getPduId();
        if (Objects.equals(pduId, null)){
            pduId = getPduIdByAddr(reqVO.getIpAddr(), reqVO.getCascadeAddr());
            if (Objects.equals(pduId, null)){
                return null;
            }
        }
        // 创建BoolQueryBuilder对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        PageResult<Object> pageResult = null;
        // 搜索请求对象
        SearchRequest searchRequest3 = new SearchRequest();
        if ("realtime".equals(reqVO.getGranularity()) ){
            searchRequest3.indices("pdu_env_realtime");
        }else if ("hour".equals(reqVO.getGranularity()) ){
            searchRequest3.indices("pdu_env_hour");
        }else {
            searchRequest3.indices("pdu_env_day");
        }
        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqVO.getTimeRange()[0])
                    .to(reqVO.getTimeRange()[1]));
        }
        // 创建匹配查询
        QueryBuilder termQuery = QueryBuilders.termQuery("pdu_id", pduId);
        QueryBuilder termQuery1 = QueryBuilders.termQuery("sensor_id", sensorId);
        // 将匹配查询添加到布尔查询中
        boolQuery.must(termQuery);
        boolQuery.must(termQuery1);
        // 将布尔查询设置到SearchSourceBuilder中
        searchSourceBuilder.query(boolQuery);
        searchRequest3.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse3 = client.search(searchRequest3, RequestOptions.DEFAULT);
        // 搜索结果
        List<Object> resultList3 = new ArrayList<>();
        SearchHits hits3 = searchResponse3.getHits();
        hits3.forEach(searchHit -> resultList3.add(searchHit.getSourceAsMap()));
        // 匹配到的总记录数
        Long totalHits3 = hits3.getTotalHits().value;
        // 返回的结果
        pageResult = new PageResult<>();
        pageResult.setList(resultList3)
                .setTotal(totalHits3);

        return pageResult;
    }

}