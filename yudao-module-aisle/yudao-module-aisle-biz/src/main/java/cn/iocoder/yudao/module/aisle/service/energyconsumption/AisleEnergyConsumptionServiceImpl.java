package cn.iocoder.yudao.module.aisle.service.energyconsumption;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.module.aisle.controller.admin.energyconsumption.DTO.AisleEleTotalRealtimeReqDTO;
import cn.iocoder.yudao.module.aisle.controller.admin.energyconsumption.VO.AisleEleTotalRealtimeResVO;
import cn.iocoder.yudao.module.aisle.controller.admin.energyconsumption.VO.AisleEnergyConsumptionPageReqVO;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
import cn.iocoder.yudao.module.aisle.service.historydata.AisleHistoryDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AisleEnergyConsumptionServiceImpl implements AisleEnergyConsumptionService {

    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private AisleIndexMapper aisleIndexMapper;
    @Autowired
    private AisleHistoryDataService aisleHistoryDataService;

    @Override
    public PageResult<Object> getEQDataPage(AisleEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置要排除的字段
        searchSourceBuilder.fetchSource(null, new String[]{"id", "bill_value", "bill_mode", "bill_period"});
        int pageNo = pageReqVO.getPageNo();
        int pageSize = pageReqVO.getPageSize();
        int index = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(index);
        // 最后一页请求超过一万，pageSize设置成请求刚好一万条
        if (index + pageSize > 10000) {
            searchSourceBuilder.size(10000 - index);
        } else {
            searchSourceBuilder.size(pageSize);
        }
        searchSourceBuilder.trackTotalHits(true);
        searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("start_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        String[] aisleIds = pageReqVO.getAisleIds();
        if (ObjectUtils.isNotEmpty(aisleIds)) {
            searchSourceBuilder.query(QueryBuilders.termsQuery("aisle_id", aisleIds));
        }
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if ("day".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("aisle_eq_total_day");
        } else if ("week".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("aisle_eq_total_week");
        } else {
            searchRequest.indices("aisle_eq_total_month");
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(searchHit -> mapList.add(searchHit.getSourceAsMap()));
        mapList.forEach(map->{
            if(map.get("start_ele")!=null){
                map.put("start_ele", new BigDecimal((Double) map.get("start_ele")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(map.get("end_ele")!=null){
                map.put("end_ele", new BigDecimal((Double) map.get("end_ele")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(map.get("start_ele")!=null&&map.get("end_ele")!=null){
                BigDecimal subtract = new BigDecimal((Double) map.get("end_ele")).subtract(new BigDecimal((Double) map.get("start_ele")));
                if(subtract.compareTo(BigDecimal.ZERO)==-1){
                    map.put("eq_value", new BigDecimal((Double) map.get("end_ele")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
        });
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回的结果
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(aisleHistoryDataService.getLocationsByAisleIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getBillDataPage(AisleEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置要排除的字段
//        searchSourceBuilder.fetchSource(new String[]{"aisle_id", "start_time", "end_time", "eq_value",  "bill_value", }, null);
        int pageNo = pageReqVO.getPageNo();
        int pageSize = pageReqVO.getPageSize();
        int index = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(index);
        // 最后一页请求超过一万，pageSize设置成请求刚好一万条
        if (index + pageSize > 10000) {
            searchSourceBuilder.size(10000 - index);
        } else {
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
        String[] aisleIds = pageReqVO.getAisleIds();
        if (ObjectUtils.isNotEmpty(aisleIds)) {
            searchSourceBuilder.query(QueryBuilders.termsQuery("aisle_id", aisleIds));
        }
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if ("day".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("aisle_eq_total_day");
        } else if ("week".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("aisle_eq_total_week");
        } else {
            searchRequest.indices("aisle_eq_total_month");
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(searchHit -> mapList.add(searchHit.getSourceAsMap()));
        mapList.forEach(map->{
            if(map.get("start_ele")!=null){
                map.put("start_ele", new BigDecimal((Double) map.get("start_ele")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(map.get("end_ele")!=null){
                map.put("end_ele", new BigDecimal((Double) map.get("end_ele")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(map.get("start_ele")!=null&&map.get("end_ele")!=null){
                BigDecimal subtract = new BigDecimal((Double) map.get("end_ele")).subtract(new BigDecimal((Double) map.get("start_ele")));
                if(subtract.compareTo(BigDecimal.ZERO)==1){
                    map.put("eq_value", subtract.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
        });
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回的结果
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(aisleHistoryDataService.getLocationsByAisleIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getEQDataDetails(AisleEnergyConsumptionPageReqVO reqVO) throws IOException {
        Integer aisleId = reqVO.getAisleId();
        if (Objects.equals(aisleId, null)) {
            return null;
        }
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqVO.getTimeRange()[0])
                    .to(reqVO.getTimeRange()[1]));
        }
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if ("day".equals(reqVO.getGranularity())) {
            searchRequest.indices("aisle_eq_total_day");
        } else if ("week".equals(reqVO.getGranularity())) {
            searchRequest.indices("aisle_eq_total_week");
        } else {
            searchRequest.indices("aisle_eq_total_month");
        }
        searchSourceBuilder.query(QueryBuilders.termQuery("aisle_id", aisleId));
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        List<Map> resultList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(searchHit -> resultList.add(searchHit.getSourceAsMap()));
        resultList.forEach(map -> {
            if(map.get("start_ele")!=null){
                map.put("start_ele", new BigDecimal((Double) map.get("start_ele")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(map.get("end_ele")!=null){
                map.put("end_ele", new BigDecimal((Double) map.get("end_ele")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(map.get("start_ele")!=null&&map.get("end_ele")!=null){
                BigDecimal subtract = new BigDecimal((Double) map.get("end_ele")).subtract(new BigDecimal((Double) map.get("start_ele")));
                if(subtract.compareTo(BigDecimal.ZERO)==1){
                    map.put("eq_value", subtract.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue());
                }
            }
        });
        List<Object> list = new ArrayList<>(resultList.size());
        resultList.forEach(map -> list.add(map));
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回的结果
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(list)
                .setTotal(totalHits);
        return pageResult;
    }

    @Override
    public PageResult<Object> getRealtimeEQDataPage(AisleEnergyConsumptionPageReqVO pageReqVO) throws IOException {
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int pageNo = pageReqVO.getPageNo();
        int pageSize = pageReqVO.getPageSize();
        int index = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(index);
        // 最后一页请求超过一万，pageSize设置成请求刚好一万条
        if (index + pageSize > 10000) {
            searchSourceBuilder.size(10000 - index);
        } else {
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
        String[] aisleIds = pageReqVO.getAisleIds();
        if (ObjectUtils.isNotEmpty(aisleIds)) {
            searchSourceBuilder.query(QueryBuilders.termsQuery("aisle_id", aisleIds));
        }
        searchRequest.indices("aisle_ele_total_realtime");
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(searchHit -> mapList.add(searchHit.getSourceAsMap()));
        mapList.forEach(map -> {
            if(map.get("ele_a")!=null){
                map.put("ele_a", new BigDecimal((Double) map.get("ele_a")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(map.get("ele_b")!=null){
                map.put("ele_b", new BigDecimal((Double) map.get("ele_b")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(map.get("ele_a")!=null&&map.get("ele_b")!=null){
                BigDecimal subtract = new BigDecimal((Double) map.get("ele_a")).add(new BigDecimal((Double) map.get("ele_b")));
                map.put("ele_total", subtract.setScale(1,BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        });
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回的结果
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(aisleHistoryDataService.getLocationsByAisleIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo) throws IOException {
        Map<String, Object> resultItem = new HashMap<>();
        // 添加范围查询 最近24小时
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
                    AggregationBuilders.count("total_insertions").field("aisle_id")
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
//        String[] indices = new String[]{"aisle_eq_total_day", "aisle_eq_total_week", "aisle_eq_total_month"};
        String indices = "aisle_ele_total_realtime";
        String[] name = new String[]{"day", "week", "month"};
        LocalDateTime[] timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1), LocalDateTime.now().minusWeeks(1), LocalDateTime.now().minusMonths(1)};
//        Map<String, Object> map = getSumData(indices, name, timeAgo);

        Map<String, Object> resultItem = new HashMap<>();
        // 添加范围查询 最近24小时
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < timeAgo.length; i++) {
            SearchRequest searchRequest = new SearchRequest(indices);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("create_time.keyword")
                    .gte(timeAgo[i].format(formatter))
                    .lte(now.format(formatter)));
            // 添加计数聚合
            searchSourceBuilder.aggregation(
                    AggregationBuilders.count("total_insertions").field("aisle_id")
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
    public Map<String, Object> getOneDaySumData() throws IOException {
        String[] indices = new String[]{"aisle_ele_total_realtime"};
        String[] name = new String[]{"total"};
        LocalDateTime[] timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1)};
        Map<String, Object> map = getSumData(indices, name, timeAgo);
        return map;
    }

    @Override
    public PageResult<Object> getSubBillDetails(AisleEnergyConsumptionPageReqVO reqVO) throws IOException {
        Integer aisleId = reqVO.getAisleId();
        if (Objects.equals(aisleId, null)) {
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
        searchRequest.indices("aisle_eq_sub_total_day");
        searchSourceBuilder.query(QueryBuilders.termQuery("aisle_id", aisleId));
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
    public List<Object> getEqExcelList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        for (int i = 0; i < mapList.size(); i++) {
            mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
            mapList.get(i).put("start_time", mapList.get(i).get("start_time").toString().substring(0, 16));
            mapList.get(i).put("end_time", mapList.get(i).get("end_time").toString().substring(0, 16));
        }
        return list;
    }

    @Override
    public List<Object> getDetailsExcelList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        for (int i = 0; i < mapList.size(); i++) {
            mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
            mapList.get(i).put("start_time", mapList.get(i).get("start_time").toString().substring(0, 16));
            mapList.get(i).put("end_time", mapList.get(i).get("end_time").toString().substring(0, 16));
        }
        return list;
    }

    @Override
    public List<Object> getBillExcelList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        for (int i = 0; i < mapList.size(); i++) {
            mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
            mapList.get(i).put("start_time", mapList.get(i).get("start_time").toString().substring(0, 16));
            mapList.get(i).put("end_time", mapList.get(i).get("end_time").toString().substring(0, 16));
        }
        return list;
    }

    @Override
    public List<Object> getRealtimeExcelList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        for (int i = 0; i < mapList.size(); i++) {
            mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
        }
        return list;
    }

    @Override
    public PageResult<AisleEleTotalRealtimeResVO> getAisleEleTotalRealtime(AisleEleTotalRealtimeReqDTO reqDTO, boolean flag) throws IOException {
        PageResult<AisleEleTotalRealtimeResVO> pageResult = new PageResult<>();
        List<AisleEleTotalRealtimeResVO> list = new ArrayList<>();
        List<AisleIndex> records = null;
        Long total = 0L;
        LambdaQueryWrapper<AisleIndex> queryWrapper = new LambdaQueryWrapper<AisleIndex>().eq(AisleIndex::getIsDelete, 0)
                .orderByDesc(AisleIndex::getCreateTime);
        if (reqDTO.getAisleIds() != null && reqDTO.getAisleIds().length != 0) {
            queryWrapper.in(AisleIndex::getId, reqDTO.getAisleIds());
        }
        if (flag) {
            IPage<AisleIndex> iPage = aisleIndexMapper.selectPage(new Page<>(reqDTO.getPageNo(), reqDTO.getPageSize()), queryWrapper);
            records = iPage.getRecords();
            total = iPage.getTotal();
        } else {
            records = aisleIndexMapper.selectList(queryWrapper);
        }
        List<Integer> roomIds = records.stream().map(AisleIndex::getRoomId).distinct().collect(Collectors.toList());
        Map<Integer , RoomIndex> map = aisleHistoryDataService.getRoomById(roomIds);
        for (AisleIndex record : records) {
            RoomIndex roomIndex = map.get(record.getRoomId());
            String location = null;
            if (roomIndex != null) {
                location =  roomIndex.getRoomName() + "-"  + record.getAisleName() ;
            }else {
                location =  record.getAisleName() ;
            }
            AisleEleTotalRealtimeResVO resVO = new AisleEleTotalRealtimeResVO();
            resVO.setId(record.getId()).setLocation(location);
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.must(QueryBuilders.rangeQuery("create_time.keyword")
                    .gte(reqDTO.getTimeRange()[0])
                    .lte(reqDTO.getTimeRange()[1]));
            boolQuery.must(QueryBuilders.termsQuery("aisle_id", String.valueOf(record.getId())));
            // 搜索源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(boolQuery);
            searchSourceBuilder.size(1);
            searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
            SearchRequest searchRequest1 = new SearchRequest();
            searchRequest1.indices("aisle_ele_total_realtime");
            //query条件--正常查询条件
            searchRequest1.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse1.getHits();
            for (SearchHit hit : hits) {
                resVO.setCreateTimeMax((String) hit.getSourceAsMap().get("create_time"));
                if (Objects.nonNull(resVO.getCreateTimeMax())) {
                    resVO.setEleActiveEnd(new BigDecimal((Double) Optional.ofNullable(hit.getSourceAsMap().get("ele_total")).orElseGet(() -> 0.0)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue() );
                }
            }
            SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();
            searchSourceBuilder2.query(boolQuery);
            searchSourceBuilder2.size(1);
            searchSourceBuilder2.sort("create_time.keyword", SortOrder.ASC);
            SearchRequest searchRequest2 = new SearchRequest();
            searchRequest2.indices("aisle_ele_total_realtime");
            //query条件--正常查询条件
            searchRequest2.source(searchSourceBuilder2);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse2 = client.search(searchRequest2, RequestOptions.DEFAULT);
            SearchHits hits2 = searchResponse2.getHits();

            for (SearchHit hit : hits2) {
                resVO.setCreateTimeMin((String) hit.getSourceAsMap().get("create_time"));
                if (Objects.nonNull(resVO.getCreateTimeMin())) {
                    resVO.setEleActiveStart(new BigDecimal((Double) Optional.ofNullable(hit.getSourceAsMap().get("ele_total")).orElseGet(() -> 0.0)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    double sub = BigDemicalUtil.sub(resVO.getEleActiveEnd(), resVO.getEleActiveStart(), 1);
                    resVO.setEleActive(sub);
                    if (sub < 0) {
                        resVO.setEleActive(resVO.getEleActiveEnd());
                    }
                }
            }
            list.add(resVO);
        }
        pageResult.setTotal(total).setList(list);
        return pageResult;
    }

}
