package cn.iocoder.yudao.module.pdu.service.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EleTotalRealtimeReqDTO;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EnergyConsumptionPageReqVO;
import cn.iocoder.yudao.module.pdu.service.historydata.HistoryDataService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.aggregations.pipeline.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EnergyConsumptionServiceImpl implements EnergyConsumptionService{

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private HistoryDataService historyDataService;

    @Override
    public PageResult<Object> getEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = null;
        // 创建BoolQueryBuilder对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置要排除的字段
        searchSourceBuilder.fetchSource(null, new String[]{"id", "bill_value", "bill_mode", "bill_period"});
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
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        List<String> pduIds = null;
        String[] ipArray = pageReqVO.getIpArray();
        if (ipArray != null){
            pduIds = historyDataService.getPduIdsByIps(ipArray);
            searchSourceBuilder.query(QueryBuilders.termsQuery("pdu_id", pduIds));
        }
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        switch (pageReqVO.getType()) {
            case "total":
                if ("day".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_total_day");
                } else if ("week".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_total_week");
                } else {
                    searchRequest.indices("pdu_eq_total_month");
                }
                break;

            case "outlet":
                // 搜索请求对象
                searchRequest = new SearchRequest();
                if ("day".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_outlet_day");
                } else if ("week".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_outlet_week");
                } else {
                    searchRequest.indices("pdu_eq_outlet_month");
                }
                if( pageReqVO.getOutletId() != null){
                    Integer outletId = pageReqVO.getOutletId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("outlet_id", outletId);
                    boolQuery.must(termQuery);
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
        pageResult.setList(historyDataService.getLocationsByPduIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getBillDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException {
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
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        List<String> pduIds = null;
        String[] ipArray = pageReqVO.getIpArray();
        if (ipArray != null){
            pduIds = historyDataService.getPduIdsByIps(ipArray);
            searchSourceBuilder.query(QueryBuilders.termsQuery("pdu_id", pduIds));
        }
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        switch (pageReqVO.getType()) {
            case "total":
                if ("day".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_total_day");
                } else if ("week".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_total_week");
                } else {
                    searchRequest.indices("pdu_eq_total_month");
                }
                break;

            case "outlet":
                // 搜索请求对象
                searchRequest = new SearchRequest();
                if ("day".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_outlet_day");
                } else if ("week".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_outlet_week");
                } else {
                    searchRequest.indices("pdu_eq_outlet_month");
                }
                if( pageReqVO.getOutletId() != null){
                    Integer outletId = pageReqVO.getOutletId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("outlet_id", outletId);
                    boolQuery.must(termQuery);
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
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(historyDataService.getLocationsByPduIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getEQDataDetails(EnergyConsumptionPageReqVO reqVO) throws IOException {
        Integer pduId = reqVO.getPduId();
        if (Objects.equals(pduId, null)){
            pduId = historyDataService.getPduIdByAddr(reqVO.getIpAddr(), reqVO.getCascadeAddr());
            if (Objects.equals(pduId, null)){
                PageResult<Object> pageResult=new PageResult<>();
                pageResult.setList(new ArrayList<>())
                        .setTotal(new Long(0));
                return pageResult;
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
        PageResult<Object> pageResult = null;
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        switch (reqVO.getType()) {
            case "total":
                if ("day".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_eq_total_day");
                }else if ("week".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_eq_total_week");
                }else {
                    searchRequest.indices("pdu_eq_total_month");
                }
                searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
                break;

            case "outlet":
                if ("day".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_eq_outlet_day");
                }else if ("week".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_eq_outlet_week");
                }else {
                    searchRequest.indices("pdu_eq_outlet_month");
                }
                Integer outletId = reqVO.getOutletId();
                // 创建范围查询
                QueryBuilder termQuery = QueryBuilders.termQuery("pdu_id", pduId);
                // 创建匹配查询
                QueryBuilder termQuery1 = QueryBuilders.termQuery("outlet_id", outletId);
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
    public List<Object> getOutletsEQData(EnergyConsumptionPageReqVO reqVO) throws IOException {
        Integer pduId = reqVO.getPduId();
        if (Objects.equals(pduId, null)){
            pduId = historyDataService.getPduIdByAddr(reqVO.getIpAddr(), reqVO.getCascadeAddr());
            if (Objects.equals(pduId, null)){
                return null;
            }
        }
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if ("day".equals(reqVO.getGranularity()) ){
            searchRequest.indices("pdu_eq_outlet_day");
        }else if ("week".equals(reqVO.getGranularity()) ){
            searchRequest.indices("pdu_eq_outlet_week");
        }else {
            searchRequest.indices("pdu_eq_outlet_month");
        }
        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqVO.getTimeRange()[0])
                    .to(reqVO.getTimeRange()[1]));
        }
        searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        SearchHits hits = searchResponse.getHits();
        Map<Integer, Double> outletIdToSumEqValueMap = new HashMap<>();
        hits.forEach(searchHit -> {
            Map<String, Object> sourceMap = searchHit.getSourceAsMap();
            Integer outletId = (Integer) sourceMap.get("outlet_id");
            Double eqValue = (Double) sourceMap.get("eq_value");
            // 如果 outletId 已经在 Map 中存在，则累加对应的 eqValue
            if (outletIdToSumEqValueMap.containsKey(outletId)) {
                double sumEqValue = outletIdToSumEqValueMap.get(outletId) + eqValue;
                outletIdToSumEqValueMap.put(outletId, sumEqValue);
            } else {
                // 如果 outletId 不在 Map 中，则添加新的记录
                outletIdToSumEqValueMap.put(outletId, eqValue);
            }
        });
        // 将结果转换为列表形式
        List<Object> resultList = new ArrayList<>();
        outletIdToSumEqValueMap.forEach((outletId, sumEqValue) -> {
            Map<String, Object> resultItem = new HashMap<>();
            resultItem.put("outlet_id", outletId);
            resultItem.put("sum_eq_value", sumEqValue);
            resultList.add(resultItem);
        });
        // 返回的结果
        return resultList;
    }

    @Override
    public PageResult<Object> getRealtimeEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException {
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
        SearchRequest searchRequest = new SearchRequest();
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        String[] ipArray = pageReqVO.getIpArray();
        if (ipArray != null){
            List<String> pduIds = historyDataService.getPduIdsByIps(ipArray);
            searchSourceBuilder.query(QueryBuilders.termsQuery("pdu_id", pduIds));
        }
        switch (pageReqVO.getType()) {
            case "total":
                // 搜索请求对象
                searchRequest.indices("pdu_ele_total_realtime");
                break;
            case "line":
                searchRequest.indices("pdu_ele_line");
                if( pageReqVO.getLineId() != null){
                    Integer lineId = pageReqVO.getLineId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("line_id", lineId);
                    boolQuery.must(termQuery);
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                break;
            case "loop":
                searchRequest.indices("pdu_ele_loop");
                if( pageReqVO.getLoopId() != null) {
                    Integer loopId = pageReqVO.getLoopId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("loop_id", loopId);
                    boolQuery.must(termQuery);
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                break;
            case "outlet":
                searchRequest.indices("pdu_ele_outlet");
                if( pageReqVO.getOutletId() != null) {
                    Integer outletId = pageReqVO.getOutletId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("outlet_id", outletId);
                    boolQuery.must(termQuery);
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
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(historyDataService.getLocationsByPduIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo) throws IOException {
        Map<String, Object> resultItem = new HashMap<>();
        // 添加范围查询 最近24小时
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < (name.length==4?4:3); i++) {
            SearchRequest searchRequest;
            if(name.length==4){
                searchRequest = new SearchRequest(indices[i]);
            }
            else{
                searchRequest = new SearchRequest(indices[0],indices[1]);
            }
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(timeAgo[i].format(formatter))
                    .to(now.format(formatter)));
            // 添加计数聚合
            searchSourceBuilder.aggregation(
                    AggregationBuilders.count("total_insertions").field("pdu_id")
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
    public Map<String, Object> getNewData() throws IOException {
        String[] indices = new String[]{"pdu_eq_total_day", "pdu_eq_outlet_day"};
        String[] name = new String[]{"day", "week", "month"};
        LocalDateTime[] timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1), LocalDateTime.now().minusWeeks(1), LocalDateTime.now().minusMonths(1)};
        Map<String, Object> map = getSumData(indices, name, timeAgo);
        return map;
    }

    @Override
    public Map<String, Object> getOneDaySumData() throws IOException {
        String[] indices = new String[]{"pdu_ele_total_realtime", "pdu_ele_line", "pdu_ele_loop", "pdu_ele_outlet"};
        String[] name = new String[]{"total", "line", "loop", "outlet"};
        LocalDateTime[] timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)};
        Map<String, Object> map = getSumData(indices, name, timeAgo);
        return map;
    }

    @Override
    public PageResult<Object> getSubBillDetails(EnergyConsumptionPageReqVO reqVO) throws IOException {
        Integer pduId = reqVO.getPduId();
        if (Objects.equals(pduId, null)){
            return null;
        }
        // 把传来的开始时间(例如"2024-07-25 10:00:00") 的年月日加一天变成 '2024-07-26 00:00:00'和'2024-07-27 00:00:00'
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 解析原始时间字符串为LocalDateTime对象
        LocalDateTime originalTime = LocalDateTime.parse(reqVO.getStartTime(), formatter);
        // 获取当天的开始时间
        LocalDateTime startTime = originalTime.withHour(0).withMinute(0).withSecond(0).withNano(0).plusDays(1);
        // 获取下一天的开始时间（即当天的结束时间的下一天）
        LocalDateTime endTime = startTime.plusDays(1);
        // 将LocalDateTime对象转换回字符串
        String startTimeString = startTime.format(formatter);
        String endTimeString = endTime.format(formatter);
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("start_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                .from(startTimeString)
                .to(endTimeString));
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if ("total".equals(reqVO.getGranularity())){
            searchRequest.indices("pdu_eq_sub_total_day");
            searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
        }else{
            searchRequest.indices("pdu_eq_sub_outlet_day");
            searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            Integer outletId = reqVO.getOutletId();
            QueryBuilder termQuery = QueryBuilders.termQuery("outlet_id", outletId);
            boolQuery.must(termQuery);
            // 将布尔查询设置到SearchSourceBuilder中
            searchSourceBuilder.query(boolQuery);
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
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(resultList)
                .setTotal(totalHits);
        return pageResult;
    }

    @Override
    public List<Object> getNewList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();

        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }

        for(int i=0;i<mapList.size();i++){
            mapList.get(i).put("create_time",mapList.get(i).get("create_time").toString().substring(0,16));
            mapList.get(i).put("start_time",mapList.get(i).get("start_time").toString().substring(0,16));
            mapList.get(i).put("end_time",mapList.get(i).get("end_time").toString().substring(0,16));
        }
        return list;
    }

    @Override
    public List<Object> getNewBillList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();

        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }

        for(int i=0;i<mapList.size();i++){
            mapList.get(i).put("start_time",mapList.get(i).get("start_time").toString().substring(0,10));
            mapList.get(i).put("end_time",mapList.get(i).get("end_time").toString().substring(0,10));
        }
        return list;
    }

    @Override
    public List<Object> getNewEQList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();

        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }

        for(int i=0;i<mapList.size();i++){
            mapList.get(i).put("create_time",mapList.get(i).get("create_time").toString().substring(0,16));
        }
        return list;
    }

    @Override
    public List<Object> getNewOutLetsList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();

        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }

        for(int i=0;i<mapList.size();i++){
            mapList.get(i).put("start_time",mapList.get(i).get("start_time").toString().substring(0,10));
            mapList.get(i).put("end_time",mapList.get(i).get("end_time").toString().substring(0,10));
            mapList.get(i).put("create_time",mapList.get(i).get("create_time").toString().substring(0,10));
        }
        return list;
    }

    @Override
    public PageResult<Object> getEleTotalRealtime(EleTotalRealtimeReqDTO reqDTO) throws IOException {
        PageResult<Object> pageResult = null;
        // 创建BoolQueryBuilder对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置要排除的字段
        //searchSourceBuilder.fetchSource(null, new String[]{"id", "bill_value", "bill_mode", "bill_period"});
        int pageNo = reqDTO.getPageNo();
        int pageSize = reqDTO.getPageSize();
        int index = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(index);



        //分页指标--用于统计分页total总数
        CardinalityAggregationBuilder cardinalityAggregation = AggregationBuilders.cardinality("custCard").field("customer_no.keyword");


        //返回字段取最新一条记录
        TopHitsAggregationBuilder topHitsAggregation = AggregationBuilders.topHits("latestCust")
                .fetchSource(new String[]{"pdu_id", "ele_active", "create_time"} , null).size(1)
                .sort("create_time", SortOrder.DESC);


        //以会员编码分组
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("topCustomer").field("customer_no.keyword")
                .size(pageNo* pageSize)
                .subAggregation(topHitsAggregation);

        //hit查询返回0条数据
        searchSourceBuilder.size(0);
        searchSourceBuilder.from(0);
        //排序
        searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        if (reqDTO.getTimeRange() != null && reqDTO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqDTO.getTimeRange()[0])
                    .to(reqDTO.getTimeRange()[1]));
        }
        List<String> pduIds = null;
        String[] ipArray = reqDTO.getIpArray();
        if (ipArray != null) {
            pduIds = historyDataService.getPduIdsByIps(ipArray);
            searchSourceBuilder.query(QueryBuilders.termsQuery("pdu_id", pduIds));
        }
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bus_ele_total_realtime");

        //query条件--正常查询条件
        searchRequest.source(searchSourceBuilder);

        //聚合条件 分组+分页指标    searchRequest.source(searchSourceBuilder.aggregation(termsAggregationBuilder));
        searchRequest.source(searchSourceBuilder.aggregation(cardinalityAggregation));
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
        pageResult.setList(historyDataService.getLocationsByPduIds(mapList))
                .setTotal(totalHits);
        return pageResult;
    }



}
