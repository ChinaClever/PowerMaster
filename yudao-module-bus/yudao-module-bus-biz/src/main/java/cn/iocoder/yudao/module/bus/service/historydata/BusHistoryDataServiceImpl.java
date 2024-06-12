package cn.iocoder.yudao.module.bus.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BusIndex;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataPageReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;
import cn.iocoder.yudao.module.bus.mapper.BoxIndexMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * @author linzhentian
 */
@Service
public class BusHistoryDataServiceImpl implements BusHistoryDataService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private BusIndexMapper busIndexMapper;
    @Autowired
    private BoxIndexMapper boxIndexMapper;
//
//    @Autowired
//    private CabinetPduMapper cabinetPduMapper;
//
//    @Autowired
//    private CabinetIndexMapper cabinetIndexMapper;
//
//    @Autowired
//    private AisleIndexMapper aisleIndexMapper;
//
//    @Autowired
//    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private RestHighLevelClient client;

//    @Autowired
//    private EnergyConsumptionService energyConsumptionService;
//
//    @Autowired
//    private CabinetEnvSensorMapper cabinetEnvSensorMapper;

    @Override
    public List<Object> getLocationsAndNameByBusIds(List<Map<String, Object>> mapList) {
        List<Object> resultList = new ArrayList<>();
        for (Map<String, Object> map : mapList){
            Object busId = map.get("bus_id");
            if (busId instanceof Integer) {
                // 查询位置
                BusIndexDO busIndex = busIndexMapper.selectById( (int)busId );
                if (busIndex != null){
                    String dev_key = busIndex.getDevKey();
                    String[] parts = dev_key.split("_");
                    map.put("bus_name", parts[1]);
                    map.put("ip_addr", parts[0]);
                }else{
                    map.put("bus_name", null);
                    map.put("ip_addr", null);
                }
            }else{
                map.put("bus_name", null);
                map.put("ip_addr", null);
            }
            resultList.add(map);
        }
        return resultList;
    }

    @Override
    public List<Object> getLocationsAndNameByBoxIds(List<Map<String, Object>> mapList) {
        List<Object> resultList = new ArrayList<>();
        for (Map<String, Object> map : mapList){
            Object boxId = map.get("box_id");
            if (boxId instanceof Integer) {
                // 查询位置
                BoxIndex boxIndex = boxIndexMapper.selectById( (int)boxId );
                if (boxIndex != null){
                    map.put("box_name", boxIndex.getBoxName());
                    map.put("bus_name", boxIndex.getBusName());
                    map.put("ip_addr", boxIndex.getIpAddr());
                }else{
                    map.put("box_name", null);
                    map.put("bus_name", null);
                    map.put("ip_addr", null);
                }
            }else{
                map.put("box_name", null);
                map.put("bus_name", null);
                map.put("ip_addr", null);
            }
            resultList.add(map);
        }
        return resultList;
    }
//
//    @Override
//    public List<Object> getSensorLocationsByPduIds(List<Map<String, Object>> mapList) {
//        List<Object> resultList = new ArrayList<>();
//        for (Map<String, Object> map : mapList){
//            Object pduId = map.get("pdu_id");
//            Integer sensorId = (Integer) map.get("sensor_id");
//            if (pduId instanceof Integer) {
//                // 查询位置
//                PduIndex pduIndex = pduIndexMapper.selectById( (int)pduId );
//                if (pduIndex != null){
//                    map.put("location", pduIndex.getDevKey());
//                    map.put("address", getSensorAddressByIpAddr(pduIndex.getDevKey(), sensorId));
//                }else{
//                    map.put("location", null);
//                    map.put("address", null);
//                }
//            }else{
//                map.put("location", null);
//                map.put("address", null);
//            }
//            resultList.add(map);
//        }
//        return resultList;
//    }
//
//    @Override
//    public String getAddressByIpAddr(String location) {
//        String[] ipParts = location.split("-");
//        String address = null;
//        CabinetPdu cabinetPduA = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
//                .eq(CabinetPdu::getPduIpA, ipParts[0])
//                .eq(CabinetPdu::getCasIdA, ipParts[1]));
//        CabinetPdu cabinetPduB = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
//                .eq(CabinetPdu::getPduIpB, ipParts[0])
//                .eq(CabinetPdu::getCasIdB, ipParts[1]));
//        if(cabinetPduA != null){
//            int cabinetId = cabinetPduA.getCabinetId();
//            CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
//            String cabinetName = cabinet.getName();
//            RoomIndex roomIndex = roomIndexMapper.selectById(cabinet.getRoomId());
//            String roomName = roomIndex.getName();
//            if(cabinet.getAisleId() != 0){
//                String aisleName = aisleIndexMapper.selectById(cabinet.getAisleId()).getName();
//                address = roomName + "-" + aisleName + "-" + cabinetName + "-" + "A路";
//            }else {
//                address = roomName + "-"  + cabinetName +  "-" + "A路";
//            }
//        }
//        if(cabinetPduB != null){
//            int cabinetId = cabinetPduB.getCabinetId();
//            CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
//            String cabinetName = cabinet.getName();
//            RoomIndex roomIndex = roomIndexMapper.selectById(cabinet.getRoomId());
//            String roomName = roomIndex.getName();
//            if(cabinet.getAisleId() != 0){
//                String aisleName = aisleIndexMapper.selectById(cabinet.getAisleId()).getName();
//                address = roomName + "-" + aisleName + "-" + cabinetName + "-" + "B路";
//            }else {
//                address = roomName + "-"  + cabinetName +  "-" + "B路";
//            }
//        }
//        return address;
//    }
//
//    @Override
//    public Map<String, Object> getSensorAddressByIpAddr(String location, Integer sensorId) {
//        Map<String, Object> map = new HashMap<>();
//        String[] ipParts = location.split("-");
//        String address = null;
//        CabinetPdu cabinetPduA = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
//                .eq(CabinetPdu::getPduIpA, ipParts[0])
//                .eq(CabinetPdu::getCasIdA, ipParts[1]));
//        CabinetPdu cabinetPduB = cabinetPduMapper.selectOne(new LambdaQueryWrapperX<CabinetPdu>()
//                .eq(CabinetPdu::getPduIpB, ipParts[0])
//                .eq(CabinetPdu::getCasIdB, ipParts[1]));
//        if(cabinetPduA != null){
//            int cabinetId = cabinetPduA.getCabinetId();
//            CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
//            String cabinetName = cabinet.getName();
//            RoomIndex roomIndex = roomIndexMapper.selectById(cabinet.getRoomId());
//            String roomName = roomIndex.getName();
//            if(cabinet.getAisleId() != 0){
//                String aisleName = aisleIndexMapper.selectById(cabinet.getAisleId()).getName();
////                address = roomName + "-" + aisleName + "-" + cabinetName + "-" + "A路";
//                address = roomName + "-" + aisleName + "-" + cabinetName;
//            }else {
////                address = roomName + "-"  + cabinetName +  "-" + "A路";
//                address = roomName + "-"  + cabinetName ;
//            }
//            // 查传感器在机柜的前后门上中下位置
//            CabinetEnvSensor cabinetEnvSensor = cabinetEnvSensorMapper.selectOne(new LambdaQueryWrapperX<CabinetEnvSensor>()
//                    .eq(CabinetEnvSensor::getCabinetId, cabinetId)
//                    .eq(CabinetEnvSensor::getPathPdu, 'A')
//                    .eq(CabinetEnvSensor::getSensorId, sensorId));
//            map.put("channel", cabinetEnvSensor.getChannel());
//            map.put("position", cabinetEnvSensor.getPosition());
//        }
//        if(cabinetPduB != null){
//            int cabinetId = cabinetPduB.getCabinetId();
//            CabinetIndex cabinet = cabinetIndexMapper.selectById(cabinetId);
//            String cabinetName = cabinet.getName();
//            RoomIndex roomIndex = roomIndexMapper.selectById(cabinet.getRoomId());
//            String roomName = roomIndex.getName();
//            if(cabinet.getAisleId() != 0){
//                String aisleName = aisleIndexMapper.selectById(cabinet.getAisleId()).getName();
////                address = roomName + "-" + aisleName + "-" + cabinetName + "-" + "B路";
//                address = roomName + "-" + aisleName + "-" + cabinetName ;
//            }else {
////                address = roomName + "-"  + cabinetName +  "-" + "B路";
//                address = roomName + "-"  + cabinetName ;
//            }
//            // 查传感器在机柜的前后门上中下位置
//            CabinetEnvSensor cabinetEnvSensor = cabinetEnvSensorMapper.selectOne(new LambdaQueryWrapperX<CabinetEnvSensor>()
//                    .eq(CabinetEnvSensor::getCabinetId, cabinetId)
//                    .eq(CabinetEnvSensor::getPathPdu, 'B')
//                    .eq(CabinetEnvSensor::getSensorId, sensorId));
//            map.put("channel", cabinetEnvSensor.getChannel());
//            map.put("position", cabinetEnvSensor.getPosition());
//        }
//        map.put("address", address);
//        return map;
//    }
//
//    @Override
//    public Integer getPduIdByAddr(String ipAddr, String cascadeAddr) {
//        String devKey = ipAddr+"-"+cascadeAddr;
//        QueryWrapper<PduIndex> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("dev_key", devKey); // 指定查询条件：name 字段等于给定的 name 值
//        PduIndex pduIndex = pduIndexMapper.selectOne(queryWrapper); // 执行查询，返回匹配的实体对象
//        if (pduIndex != null){
//            return Math.toIntExact(pduIndex.getId());
//        }
//        return null;
//    }
//
//    @Override
//    public List<String> getPduIdsByIps(String[] ips){
//        QueryWrapper<PduIndex> queryWrapper = new QueryWrapper<>();
//        queryWrapper.select("id");
//        queryWrapper.in("dev_key", ips);
//        return pduIndexMapper.selectObjs(queryWrapper);
//
//    }

    @Override
    public Map getHistoryDataTypeMaxValue(String[] boxIds) throws IOException {
        HashMap resultMap = new HashMap<>();
        String[] indexArr = new String[]{"box_hda_line_realtime", "box_hda_loop_realtime", "box_hda_outlet_realtime"};
        String[] fieldNameArr = new String[]{"line_id", "loop_id", "outlet_id"};
        for (int i = 0; i < indexArr.length; i++) {
            SearchRequest searchRequest = new SearchRequest(indexArr[i]);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            if (boxIds != null){
                searchSourceBuilder.query(QueryBuilders.termsQuery("box_id", boxIds));
            }
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

//    @Override
//    public Map getSensorIdMaxValue() throws IOException {
//        HashMap resultMap = new HashMap<>();
//        SearchRequest searchRequest = new SearchRequest("pdu_env_realtime");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        // 添加最大值聚合
//        searchSourceBuilder.aggregation(
//                AggregationBuilders.max("max_value").field("sensor_id")
//        );
//        searchRequest.source(searchSourceBuilder);
//        // 执行搜索请求
//        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//        // 从聚合结果中获取最大值
//        Max maxAggregation = searchResponse.getAggregations().get("max_value");
//        Integer maxValue = (int) maxAggregation.getValue();
//        resultMap.put("sensor_id_max_value", maxValue);
//        return resultMap;
//    }

    @Override
    public PageResult<Object> getBusHistoryDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException {
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
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        String[] busIds = pageReqVO.getBusIds();
        if (busIds != null){
            searchSourceBuilder.query(QueryBuilders.termsQuery("bus_id", busIds));
        }
        switch (pageReqVO.getType()) {
            case "total":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("bus_hda_total_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("bus_hda_total_hour");
                } else {
                    searchRequest.indices("bus_hda_total_day");
                }
                break;
            case "line":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("bus_hda_line_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("bus_hda_line_hour");
                } else {
                    searchRequest.indices("bus_hda_line_day");
                }
                if( pageReqVO.getLineId() != null){
                    Integer lineId = pageReqVO.getLineId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery ("line_id", lineId);
                    if (busIds != null) {
                        QueryBuilder termQuery1 = QueryBuilders.termQuery("bus_id", busIds[0]);
                        boolQuery.must(termQuery1);
                    }
                    boolQuery.must(termQuery);
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                break;

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
        pageResult.setList(getLocationsAndNameByBusIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getBoxHistoryDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException {
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
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        String[] boxIds = pageReqVO.getBoxIds();
        if (boxIds != null){
            searchSourceBuilder.query(QueryBuilders.termsQuery("box_id", boxIds));
        }
        switch (pageReqVO.getType()) {
            case "total":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("box_hda_total_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("box_hda_total_hour");
                } else {
                    searchRequest.indices("box_hda_total_day");
                }
                break;
            case "line":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("box_hda_line_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("box_hda_line_hour");
                } else {
                    searchRequest.indices("box_hda_line_day");
                }
                if( pageReqVO.getLineId() != null){
                    Integer lineId = pageReqVO.getLineId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery ("line_id", lineId);
                    if (boxIds != null) {
                        QueryBuilder termQuery1 = QueryBuilders.termQuery("box_id", boxIds[0]);
                        boolQuery.must(termQuery1);
                    }
                    boolQuery.must(termQuery);
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                break;

            case "loop":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("box_hda_loop_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("box_hda_loop_hour");
                } else {
                    searchRequest.indices("box_hda_loop_day");
                }
                if( pageReqVO.getLoopId() != null){
                    Integer loopId = pageReqVO.getLoopId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery ("loop_id", loopId);
                    if (boxIds != null) {
                        QueryBuilder termQuery1 = QueryBuilders.termQuery("box_id", boxIds[0]);
                        boolQuery.must(termQuery1);
                    }
                    boolQuery.must(termQuery);
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                break;

            case "outlet":
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("box_hda_outlet_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("box_hda_outlet_hour");
                } else {
                    searchRequest.indices("box_hda_outlet_day");
                }
                if( pageReqVO.getOutletId() != null){
                    Integer outletId = pageReqVO.getOutletId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery ("outlet_id", outletId);
                    if (boxIds != null) {
                        QueryBuilder termQuery1 = QueryBuilders.termQuery("box_id", boxIds[0]);
                        boolQuery.must(termQuery1);
                    }
                    boolQuery.must(termQuery);
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                break;

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
        pageResult.setList(getLocationsAndNameByBoxIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getBusHistoryDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException{
        Integer busId = reqVO.getBusId();
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
                    searchRequest.indices("bus_hda_total_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("bus_hda_total_hour");
                }else {
                    searchRequest.indices("bus_hda_total_day");
                }
                searchSourceBuilder.query(QueryBuilders.termQuery("bus_id", busId));
                break;

            case "line":
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("bus_hda_line_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("bus_hda_line_hour");
                }else {
                    searchRequest.indices("bus_hda_line_day");
                }
                Integer lineId = reqVO.getLineId();
                // 创建范围查询
                QueryBuilder termQuery = QueryBuilders.termQuery("bus_id", busId);
                // 创建匹配查询
                QueryBuilder termQuery1 = QueryBuilders.termQuery("line_id", lineId);
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
    public PageResult<Object> getBoxHistoryDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException{
        Integer boxId = reqVO.getBoxId();
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
                    searchRequest.indices("box_hda_total_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("box_hda_total_hour");
                }else {
                    searchRequest.indices("box_hda_total_day");
                }
                searchSourceBuilder.query(QueryBuilders.termQuery("box_id", boxId));
                break;

            case "line":
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("box_hda_line_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("box_hda_line_hour");
                }else {
                    searchRequest.indices("box_hda_line_day");
                }
                Integer lineId = reqVO.getLineId();
                // 创建范围查询
                QueryBuilder termQuery = QueryBuilders.termQuery("box_id", boxId);
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
                    searchRequest.indices("box_hda_loop_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("box_hda_loop_hour");
                }else {
                    searchRequest.indices("box_hda_loop_day");
                }
                Integer loopId = reqVO.getLoopId();
                // 创建范围查询
                QueryBuilder termQuery2 = QueryBuilders.termQuery("box_id", boxId);
                // 创建匹配查询
                QueryBuilder termQuery3 = QueryBuilders.termQuery("loop_id", loopId);
                // 将范围查询和匹配查询添加到布尔查询中
                boolQuery.must(termQuery2);
                boolQuery.must(termQuery3);
                // 将布尔查询设置到SearchSourceBuilder中
                searchSourceBuilder.query(boolQuery);
                break;

            case "outlet":
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("box_hda_outlet_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("box_hda_outlet_hour");
                }else {
                    searchRequest.indices("box_hda_outlet_day");
                }
                Integer outletId = reqVO.getOutletId();
                // 创建范围查询
                QueryBuilder termQuery4 = QueryBuilders.termQuery("box_id", boxId);
                // 创建匹配查询
                QueryBuilder termQuery5 = QueryBuilders.termQuery("outlet_id", outletId);
                // 将范围查询和匹配查询添加到布尔查询中
                boolQuery.must(termQuery4);
                boolQuery.must(termQuery5);
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
//    @Override
//    public PageResult<Object> getEnvDataPage(EnvDataPageReqVo pageReqVO) throws IOException {
//        PageResult<Object> pageResult = null;
//        // 搜索源构建对象
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        int pageNo = pageReqVO.getPageNo();
//        int pageSize = pageReqVO.getPageSize();
//        int index = (pageNo - 1) * pageSize;
//        searchSourceBuilder.from(index);
//        // 最后一页请求超过一万，pageSize设置成请求刚好一万条
//        if (index + pageSize > 10000){
//            searchSourceBuilder.size(10000 - index);
//        }else{
//            searchSourceBuilder.size(pageSize);
//        }
//        searchSourceBuilder.trackTotalHits(true);
//        searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
//
////        if (!Objects.equals(pageReqVO.getIpAddr(), "") && !Objects.equals(pageReqVO.getIpAddr(), null)){
////            Integer pduId = getPduIdByAddr(pageReqVO.getIpAddr(), pageReqVO.getCascadeAddr());
////            if(pduId != null){
////                if (!Objects.equals(sensorId, 0)){
////                    // 创建范围查询
////                    QueryBuilder termQuery = QueryBuilders.termQuery("pdu_id", pduId);
////                    // 创建匹配查询
////                    QueryBuilder termQuery1 = QueryBuilders.termQuery("sensor_id", sensorId);
////                    // 创建BoolQueryBuilder对象
////                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
////                    // 将范围查询和匹配查询添加到布尔查询中
////                    boolQuery.must(termQuery);
////                    boolQuery.must(termQuery1);
////                    // 将布尔查询设置到SearchSourceBuilder中
////                    searchSourceBuilder.query(boolQuery);
////                }else{
////                    searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
////                }
////            }else{
////                // 查不到pdu 直接返回空数据
////                pageResult = new PageResult<>();
////                pageResult.setList(null)
////                        .setTotal(0L);
////                return pageResult;
////            }
////        }else{
////            if (!Objects.equals(sensorId, 0)){
////                QueryBuilder termQuery = QueryBuilders.termQuery("sensor_id", sensorId);
////                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
////                boolQuery.must(termQuery);
////                searchSourceBuilder.query(boolQuery);
////            }else{
////                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
////            }
////        }
//
//        // 接收机柜id数组 查出ip数组
//        List<String> pduIds = new ArrayList<>();
//        String[] cabinetIds = pageReqVO.getCabinetIds();
//        Integer channel = pageReqVO.getChannel();
//        Integer position = pageReqVO.getPosition();
//
//        // 前端筛选机柜但没筛选探测点触发
//        if (cabinetIds != null && channel == null){
//            QueryWrapper<CabinetPdu> cabinetPduQueryWrapper = new QueryWrapper<>();
//            cabinetPduQueryWrapper.in("cabinet_id", cabinetIds);
//            List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(cabinetPduQueryWrapper);
//            for (CabinetPdu cabinetPdu1 : cabinetPduList){
//                Integer ipA = getPduIdByAddr(cabinetPdu1.getPduIpA(), String.valueOf(cabinetPdu1.getCasIdA()));
//                Integer ipB = getPduIdByAddr(cabinetPdu1.getPduIpB(), String.valueOf(cabinetPdu1.getCasIdB()));
//                if (ipA != null) pduIds.add(String.valueOf(ipA));
//                if (ipB != null) pduIds.add(String.valueOf(ipB));
//            }
//            if (!pduIds.isEmpty()) {
//                searchSourceBuilder.query(QueryBuilders.termsQuery("pdu_id", pduIds));
//            }else{
//                // 查不到pdu 直接返回空数据
//                pageResult = new PageResult<>();
//                pageResult.setList(null)
//                        .setTotal(0L);
//                return pageResult;
//            }
//        }else{
//            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//        }
//
//        // 前端筛选某个机柜和探测点后触发
//        if (cabinetIds!= null && channel != null && position != null){
//            QueryWrapper<CabinetEnvSensor> cabinetEnvSensorQueryWrapper = new QueryWrapper<>();
//            cabinetEnvSensorQueryWrapper.eq("cabinet_id", cabinetIds[0])
//                    .eq("channel", channel)
//                    .eq("position", position);
//            CabinetEnvSensor cabinetEnvSensor = cabinetEnvSensorMapper.selectOne(cabinetEnvSensorQueryWrapper);
//            // 表示此机柜此位置没有传感器 直接返回
//            if ( cabinetEnvSensor == null ){
//                pageResult = new PageResult<>();
//                pageResult.setList(null)
//                        .setTotal(0L);
//                return pageResult;
//            }
//            QueryWrapper<CabinetPdu> cabinetPduQueryWrapper = new QueryWrapper<>();
//            cabinetPduQueryWrapper.eq("cabinet_id", cabinetIds[0]);
//            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(cabinetPduQueryWrapper);
//            Integer pduId = null;
//            if (cabinetEnvSensor.getPathPdu() == 'A'){
//                pduId = getPduIdByAddr(cabinetPdu.getPduIpA(), String.valueOf(cabinetPdu.getCasIdA()));
//            }
//            if (cabinetEnvSensor.getPathPdu() == 'B'){
//                pduId = getPduIdByAddr(cabinetPdu.getPduIpB(), String.valueOf(cabinetPdu.getCasIdB()));
//            }
//            // 创建范围查询
//            if (pduId != null) {
//                QueryBuilder termQuery = QueryBuilders.termQuery("pdu_id", pduId);
//                // 创建匹配查询
//                QueryBuilder termQuery1 = QueryBuilders.termQuery("sensor_id", cabinetEnvSensor.getSensorId());
//                // 创建BoolQueryBuilder对象
//                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//                // 将范围查询和匹配查询添加到布尔查询中
//                boolQuery.must(termQuery);
//                boolQuery.must(termQuery1);
//                // 将布尔查询设置到SearchSourceBuilder中
//                searchSourceBuilder.query(boolQuery);
//            }
//
//        }
//
//        // 搜索请求对象
//        SearchRequest searchRequest = new SearchRequest();
//        if ("realtime".equals(pageReqVO.getGranularity())) {
//            searchRequest.indices("pdu_env_realtime");
//        } else if ("hour".equals(pageReqVO.getGranularity())) {
//            searchRequest.indices("pdu_env_hour");
//        } else {
//            searchRequest.indices("pdu_env_day");
//        }
//        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
//            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
//                    .from(pageReqVO.getTimeRange()[0])
//                    .to(pageReqVO.getTimeRange()[1]));
//        }
//        searchRequest.source(searchSourceBuilder);
//        // 执行搜索,向ES发起http请求
//        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//        // 搜索结果
//        List<Map<String, Object>> mapList = new ArrayList<>();
//        SearchHits hits = searchResponse.getHits();
//        hits.forEach(searchHit -> mapList.add(searchHit.getSourceAsMap()));
//        // 匹配到的总记录数
//        Long totalHits = hits.getTotalHits().value;
//        // 返回的结果
//        pageResult = new PageResult<>();
//        pageResult.setList(getSensorLocationsByPduIds(mapList))
//                .setTotal(totalHits);
//
//        return pageResult;
//    }
//
//    @Override
//    public Map<String, Object> getEnvDataDetails(EnvDataDetailsReqVO reqVO) throws IOException {
//        // 创建BoolQueryBuilder对象
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
//        searchSourceBuilder.size(10000);
//        searchSourceBuilder.trackTotalHits(true);
//        // 搜索请求对象
//        SearchRequest searchRequest = new SearchRequest();
//        if ("realtime".equals(reqVO.getGranularity()) ){
//            searchRequest.indices("pdu_env_realtime");
//        }else if ("hour".equals(reqVO.getGranularity()) ){
//            searchRequest.indices("pdu_env_hour");
//        }else {
//            searchRequest.indices("pdu_env_day");
//        }
//        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
//            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
//                    .from(reqVO.getTimeRange()[0])
//                    .to(reqVO.getTimeRange()[1]));
//        }
//        Map<String, Object> map = new HashMap<>();
//        Integer sensorId = reqVO.getSensorId();
//        Integer pduId = reqVO.getPduId();
//        Integer cabinetId = reqVO.getCabinetId();
//        Integer channel = reqVO.getChannel();
//        Integer position = reqVO.getPosition();
//        String ipAddr = null;
//        /**
//         * 不是跳转到环境分析的情况 此时没有pduId和sensorId 要用cabinetId、channel和position来查
//         */
//        if (pduId == null && sensorId == null){
//            QueryWrapper<CabinetEnvSensor> cabinetEnvSensorQueryWrapper = new QueryWrapper<>();
//            cabinetEnvSensorQueryWrapper.eq("cabinet_id", cabinetId)
//                    .eq("channel", channel)
//                    .eq("position", position);
//            CabinetEnvSensor cabinetEnvSensor = cabinetEnvSensorMapper.selectOne(cabinetEnvSensorQueryWrapper);
//            // 表示此机柜此位置没有传感器 直接返回
//            if ( cabinetEnvSensor == null ){
//                System.out.println("机柜此位置没有传感器");
//                map.put("list", null);
//                map.put("total", 0);
//                map.put("ipAddr", null);
//                return map;
//            }
//            sensorId = cabinetEnvSensor.getSensorId();
//            QueryWrapper<CabinetPdu> cabinetPduQueryWrapper = new QueryWrapper<>();
//            cabinetPduQueryWrapper.eq("cabinet_id", cabinetId);
//            CabinetPdu cabinetPdu = cabinetPduMapper.selectOne(cabinetPduQueryWrapper);
//            if (cabinetEnvSensor.getPathPdu() == 'A'){
//                pduId = getPduIdByAddr(cabinetPdu.getPduIpA(), String.valueOf(cabinetPdu.getCasIdA()));
//                ipAddr = cabinetPdu.getPduIpA()+'-'+ cabinetPdu.getCasIdA();
//            } else if (cabinetEnvSensor.getPathPdu() == 'B') {
//                pduId = getPduIdByAddr(cabinetPdu.getPduIpB(), String.valueOf(cabinetPdu.getCasIdB()));
//                ipAddr = cabinetPdu.getPduIpB()+'-'+ cabinetPdu.getCasIdB();
//            }
//        }
//
//        // 创建匹配查询
//        QueryBuilder termQuery = QueryBuilders.termQuery("pdu_id", pduId);
//        QueryBuilder termQuery1 = QueryBuilders.termQuery("sensor_id", sensorId);
//        // 将匹配查询添加到布尔查询中
//        boolQuery.must(termQuery);
//        boolQuery.must(termQuery1);
//        // 将布尔查询设置到SearchSourceBuilder中
//        searchSourceBuilder.query(boolQuery);
//        searchRequest.source(searchSourceBuilder);
//        // 执行搜索,向ES发起http请求
//        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//        // 搜索结果
//        List<Object> resultList = new ArrayList<>();
//        SearchHits hits = searchResponse.getHits();
//        hits.forEach(searchHit -> resultList.add(searchHit.getSourceAsMap()));
//        // 匹配到的总记录数
//        Long totalHits = hits.getTotalHits().value;
//        // 返回的结果
//        map.put("list", resultList);
//        map.put("total", totalHits);
//        map.put("ipAddr", ipAddr);
//
//        return map;
//    }

    @Override
    public Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo, String field) throws IOException {
        Map<String, Object> resultItem = new HashMap<>();
        // 添加范围查询
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < indices.length; i++) {
            SearchRequest searchRequest = new SearchRequest(indices[i]);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(timeAgo[i].format(formatter))
                    .to(now.format(formatter)));
            // 添加计数聚合
            searchSourceBuilder.aggregation(
                    AggregationBuilders.count("total_insertions").field(field)
            );
            searchRequest.source(searchSourceBuilder);
            // 执行搜索请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            // 从聚合结果中获取文档数量
            ValueCount totalInsertionsAggregation = searchResponse.getAggregations().get("total_insertions");
            long totalInsertions = totalInsertionsAggregation.getValue();
            resultItem.put(name[i], totalInsertions);
        }
        return resultItem;
    }

    @Override
    public Map<String, Object> getBusNavNewData(String granularity) throws IOException {
        String[] indices = new String[0];
        String[] key = new String[]{"total", "line"};
        LocalDateTime[] timeAgo = new LocalDateTime[0];
        Map<String, Object> map;
        switch (granularity){
            case "realtime":
                indices = new String[]{"bus_hda_total_realtime", "bus_hda_line_realtime"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(1)};
                break;
            case "hour":
                indices = new String[]{"bus_hda_total_hour", "bus_hda_line_hour"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1)};
                break;
            case "day":
                indices = new String[]{"bus_hda_total_day", "bus_hda_line_day"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)};
                break;
            default:
        }
        map = getSumData(indices, key, timeAgo,"bus_id");
        return map;
    }

    @Override
    public Map<String, Object> getBoxNavNewData(String granularity) throws IOException {
        String[] indices = new String[0];
        String[] key = new String[]{"total", "line", "loop", "outlet"};
        LocalDateTime[] timeAgo = new LocalDateTime[0];
        Map<String, Object> map;
        switch (granularity){
            case "realtime":
                indices = new String[]{"box_hda_total_realtime", "box_hda_line_realtime", "box_hda_loop_realtime", "box_hda_outlet_realtime"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(1)};
                break;
            case "hour":
                indices = new String[]{"box_hda_total_hour", "box_hda_line_hour", "box_hda_loop_hour", "box_hda_outlet_hour"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1)};
                break;
            case "day":
                indices = new String[]{"box_hda_total_day", "box_hda_line_day", "box_hda_loop_day", "box_hda_outlet_day"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)};
                break;
            default:
        }
        map = getSumData(indices, key, timeAgo,"box_id");
        return map;
    }


}