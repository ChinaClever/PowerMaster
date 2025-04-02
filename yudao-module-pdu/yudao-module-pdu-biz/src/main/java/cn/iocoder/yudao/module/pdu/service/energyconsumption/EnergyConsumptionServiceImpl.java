package cn.iocoder.yudao.module.pdu.service.energyconsumption;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetPdu;
import cn.iocoder.yudao.framework.common.exception.BusinessAssert;
import cn.iocoder.yudao.framework.common.mapper.CabinetPduMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.BigDemicalUtil;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EleTotalRealtimeReqDTO;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EleTotalRealtimeRespVO;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EnergyConsumptionPageReqVO;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndex;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndexMapper;
import cn.iocoder.yudao.module.pdu.service.historydata.HistoryDataService;
import cn.iocoder.yudao.module.pdu.service.pdudevice.PDUDeviceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class EnergyConsumptionServiceImpl implements EnergyConsumptionService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private HistoryDataService historyDataService;

    @Autowired
    private PDUDeviceService pduDeviceService;

    @Autowired
    private CabinetPduMapper cabinetPduMapper;

    @Autowired
    private PduIndexMapper pduIndexMapper;

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
        // 假设相关对象已经正确导入和初始化
        List<String> pduIds = null;
        String[] ipArray = pageReqVO.getIpArray();

        if (ObjectUtils.isNotEmpty(ipArray)) {
            pduIds = historyDataService.getPduIdsByIps(ipArray);
            boolQuery.must(QueryBuilders.termsQuery("pdu_id", pduIds)); // 将 termsQuery 加入 boolQuery 的 must 子句
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
                if (pageReqVO.getOutletId() != null) {
                    Integer outletId = pageReqVO.getOutletId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("outlet_id", outletId);
                    boolQuery.must(termQuery); // 将 termQuery 加入 boolQuery 的 must 子句
                }
                break;
            default:
        }
        // 将最终的 boolQuery 作为查询添加到 searchSourceBuilder 中
        searchSourceBuilder.query(boolQuery);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            // 执行搜索,向ES发起http请求
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("EnergyConsumptionServiceImpl.getEQDataPage error:{}", e.getMessage());
        }
        // 搜索结果
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        if (totalHits == 0) {
            BusinessAssert.error(50010, "暂无数据");
        }
        hits.forEach(searchHit -> mapList.add(searchHit.getSourceAsMap()));
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
        List<String> pduIds;
        String[] ipArray = pageReqVO.getIpArray();
        if (ObjectUtils.isNotEmpty(ipArray)) {
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
                if (pageReqVO.getOutletId() != null) {
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
        if (Objects.equals(pduId, null)) {
            pduId = historyDataService.getPduIdByAddr(reqVO.getIpAddr(), null);
            if (Objects.equals(pduId, null)) {
                PageResult<Object> pageResult = new PageResult<>();
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
        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqVO.getTimeRange()[0])
                    .to(reqVO.getTimeRange()[1]));
        }
        PageResult<Object> pageResult = null;
        try {
            // 搜索请求对象
            SearchRequest searchRequest = new SearchRequest();
            switch (reqVO.getType()) {
                case "total":
                    if ("day".equals(reqVO.getGranularity())) {
                        searchRequest.indices("pdu_eq_total_day");
                    } else if ("week".equals(reqVO.getGranularity())) {
                        searchRequest.indices("pdu_eq_total_week");
                    } else {
                        searchRequest.indices("pdu_eq_total_month");
                    }
                    searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
                    break;

                case "outlet":
                    if ("day".equals(reqVO.getGranularity())) {
                        searchRequest.indices("pdu_eq_outlet_day");
                    } else if ("week".equals(reqVO.getGranularity())) {
                        searchRequest.indices("pdu_eq_outlet_week");
                    } else {
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
        } catch (Exception e) {
            log.error("getEQDataDetails error", e);
        }
        return pageResult;
    }

    @Override
    public List<Object> getOutletsEQData(EnergyConsumptionPageReqVO reqVO) throws IOException {

        List<Object> resultList = new ArrayList<>();
        try {
            Integer pduId = reqVO.getPduId();
            if (Objects.equals(pduId, null)) {
                pduId = historyDataService.getPduIdByAddr(reqVO.getIpAddr(), null);
                if (Objects.equals(pduId, null)) {
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
            if ("day".equals(reqVO.getGranularity())) {
                searchRequest.indices("pdu_eq_outlet_day");
            } else if ("week".equals(reqVO.getGranularity())) {
                searchRequest.indices("pdu_eq_outlet_week");
            } else {
                searchRequest.indices("pdu_eq_outlet_month");
            }
            if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0) {
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

            outletIdToSumEqValueMap.forEach((outletId, sumEqValue) -> {
                Map<String, Object> resultItem = new HashMap<>();
                resultItem.put("outlet_id", outletId);
                resultItem.put("sum_eq_value", sumEqValue);
                resultList.add(resultItem);
            });
            // 返回的结果
            return resultList;
        } catch (Exception e) {
            log.error("获取实时eq数据失败", e);
        }
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
        String[] ipArray = pageReqVO.getIpArray();
        if (ObjectUtils.isNotEmpty(ipArray)) {
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
                if (pageReqVO.getLineId() != null) {
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
                if (pageReqVO.getLoopId() != null) {
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
                if (pageReqVO.getOutletId() != null) {
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
        try {
            resultItem = new HashMap<>();
            // 添加范围查询 最近24小时
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < name.length; i++) {
                SearchRequest searchRequest;
                searchRequest = new SearchRequest(indices[i]);
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
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public Integer getPduIdByAddr(String ipAddr, String cascadeAddr) {
        String devKey = ipAddr;//+"-"+cascadeAddr;
        QueryWrapper<PduIndex> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pdu_key", devKey); // 指定查询条件：name 字段等于给定的 name 值
        PduIndex pduIndex = pduIndexMapper.selectOne(queryWrapper); // 执行查询，返回匹配的实体对象
        if (pduIndex != null) {
            return Math.toIntExact(pduIndex.getId());
        }
        return null;
    }

    public Map<String, Object> getSumDataByCabinet(String[] indices, String[] name, LocalDateTime[] timeAgo) throws IOException {
        Map<String, Object> resultItem = new HashMap<>();
        QueryWrapper<CabinetPdu> wrapper = new QueryWrapper<>();
        wrapper.select("pdu_key_a", "pdu_key_b");
        List<CabinetPdu> cabinetPduList = cabinetPduMapper.selectList(wrapper);

        // 收集所有需要查询的 pdu_key
        Set<String> allPduKeys = new HashSet<>();
        for (CabinetPdu cabinetPdu : cabinetPduList) {
            if (cabinetPdu.getPduKeyA() != null) {
                allPduKeys.add(cabinetPdu.getPduKeyA());
            }
            if (cabinetPdu.getPduKeyB() != null) {
                allPduKeys.add(cabinetPdu.getPduKeyB());
            }
        }

        // 批量查询所有 pdu_key 对应的 PduIndex 记录
        QueryWrapper<PduIndex> pduIndexQueryWrapper = new QueryWrapper<>();
        pduIndexQueryWrapper.in("pdu_key", allPduKeys);
        List<PduIndex> pduIndexList = pduIndexMapper.selectList(pduIndexQueryWrapper);

        // 将查询结果存入 Map，方便快速查找
        Map<String, Integer> pduKeyToIdMap = new HashMap<>();
        for (PduIndex pduIndex : pduIndexList) {
            pduKeyToIdMap.put(pduIndex.getPduKey(), Math.toIntExact(pduIndex.getId()));
        }

        // 从 Map 中获取对应的 pduId
        List<String> pduIds = new ArrayList<>();
        for (CabinetPdu cabinetPdu : cabinetPduList) {
            Integer ipA = pduKeyToIdMap.get(cabinetPdu.getPduKeyA());
            Integer ipB = pduKeyToIdMap.get(cabinetPdu.getPduKeyB());
            if (ipA != null) {
                pduIds.add(String.valueOf(ipA));
            }
            if (ipB != null) {
                pduIds.add(String.valueOf(ipB));
            }
        }

            try {
                resultItem = new HashMap<>();
                // 添加范围查询 最近24小时
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (int i = 0; i < name.length; i++) {
                    SearchRequest searchRequest = new SearchRequest(indices[i]);
                    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                    // 使用 bool 查询来组合多个查询条件
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(timeAgo[i].format(formatter))
                            .to(now.format(formatter)));
                    boolQuery.must(QueryBuilders.termsQuery("pdu_id", pduIds));
                    searchSourceBuilder.query(boolQuery);
                    // 添加计数聚合
                    searchSourceBuilder.aggregation(
                            AggregationBuilders.count("total_insertions").field("pdu_id")
                    );
                    searchRequest.source(searchSourceBuilder);
                    // 执行搜索请求
                    try {
                        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                        // 从聚合结果中获取文档数量
                        ValueCount totalInsertionsAggregation = searchResponse.getAggregations().get("total_insertions");
                        if (totalInsertionsAggregation!= null) {
                            long totalInsertions = totalInsertionsAggregation.getValue();
                            resultItem.put(name[i], totalInsertions);
                        } else {
                            resultItem.put(name[i], 0L);
                        }
                    } catch (Exception e) {
                        // 处理异常，例如打印异常信息
                        e.printStackTrace();
                    }
                }
                return resultItem;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            return null;
        }

    @Override
    public Map<String, Object> getNewData() throws IOException {
        String[] indices = new String[]{"pdu_ele_total_realtime","pdu_ele_total_realtime","pdu_ele_total_realtime"};
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
        if (Objects.equals(pduId, null)) {
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
        if ("total".equals(reqVO.getGranularity())) {
            searchRequest.indices("pdu_eq_sub_total_day");
            searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", pduId));
        } else {
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

        for (int i = 0; i < mapList.size(); i++) {
            mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
            mapList.get(i).put("start_time", mapList.get(i).get("start_time").toString().substring(0, 16));
            mapList.get(i).put("end_time", mapList.get(i).get("end_time").toString().substring(0, 16));
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

        for (int i = 0; i < mapList.size(); i++) {
            mapList.get(i).put("start_time", mapList.get(i).get("start_time").toString().substring(0, 10));
            mapList.get(i).put("end_time", mapList.get(i).get("end_time").toString().substring(0, 10));
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

        for (int i = 0; i < mapList.size(); i++) {
            mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
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

        for (int i = 0; i < mapList.size(); i++) {
            mapList.get(i).put("start_time", mapList.get(i).get("start_time").toString().substring(0, 10));
            mapList.get(i).put("end_time", mapList.get(i).get("end_time").toString().substring(0, 10));
            mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 10));
        }
        return list;
    }

    @Override
    public PageResult<EleTotalRealtimeRespVO> getEleTotalRealtime(EleTotalRealtimeReqDTO reqDTO, boolean flag) throws IOException {

        PageResult<EleTotalRealtimeRespVO> pageResult = null;
        List<EleTotalRealtimeRespVO> mapList = new ArrayList<>();
        List collect = new ArrayList();
        List<PduIndex> records = null;
        long total = 0;
        if (flag) {
            IPage<PduIndex> page = historyDataService.findPduIndexAll(reqDTO.getPageNo(), reqDTO.getPageSize(), reqDTO.getIpArray());
            total = page.getTotal();
            records = page.getRecords();
            collect = records.stream().map(PduIndex::getId).collect(Collectors.toList());
            collect.stream().map(String::valueOf).collect(Collectors.joining(","));
        } else {
            records = historyDataService.findPduIndexAllToList(reqDTO.getIpArray());
            collect = records.stream().map(PduIndex::getId).collect(Collectors.toList());
            collect.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        List<String> collect1 = records.stream().map(PduIndex::getPduKey).collect(Collectors.toList());
        Map<String,String> locaMap = pduDeviceService.setLocation(collect1);
        for (PduIndex record : records) {
            EleTotalRealtimeRespVO respVO = new EleTotalRealtimeRespVO();
            respVO.setPduId(record.getId()).setLocation(record.getPduKey()).setAddress(historyDataService.getAddressByIpAddr(record.getPduKey()));
            BoolQueryBuilder boolQuery1 = QueryBuilders.boolQuery();
            boolQuery1.must(QueryBuilders.rangeQuery("create_time.keyword")
                    .gte(reqDTO.getTimeRange()[0])
                    .lte(reqDTO.getTimeRange()[1]));
            boolQuery1.must(QueryBuilders.termsQuery("pdu_id", String.valueOf(record.getId())));
            // 搜索源构建对象
            SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder();
            searchSourceBuilder1.query(boolQuery1);
            searchSourceBuilder1.size(1);
            searchSourceBuilder1.sort("create_time.keyword", SortOrder.DESC);
            SearchRequest searchRequest1 = new SearchRequest();
            searchRequest1.indices("pdu_ele_total_realtime");
            //query条件--正常查询条件
            searchRequest1.source(searchSourceBuilder1);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse1.getHits();
            for (SearchHit hit : hits) {
                respVO.setCreateTimeMax((String) hit.getSourceAsMap().get("create_time"));
                if (Objects.nonNull(respVO.getCreateTimeMax())) {
                    respVO.setEleActiveEnd((Double) Optional.ofNullable(hit.getSourceAsMap().get("ele_active")).orElseGet(() -> 0.0));
                }
            }
            SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();
            searchSourceBuilder2.query(boolQuery1);
            searchSourceBuilder2.size(1);
            searchSourceBuilder2.sort("create_time.keyword", SortOrder.ASC);
            SearchRequest searchRequest2 = new SearchRequest();
            searchRequest2.indices("pdu_ele_total_realtime");
            //query条件--正常查询条件
            searchRequest2.source(searchSourceBuilder2);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse2 = client.search(searchRequest2, RequestOptions.DEFAULT);
            SearchHits hits2 = searchResponse2.getHits();

            for (SearchHit hit : hits2) {
                respVO.setCreateTimeMin((String) hit.getSourceAsMap().get("create_time"));
                if (Objects.nonNull(respVO.getCreateTimeMin())) {
                    respVO.setEleActiveStart((Double) Optional.ofNullable(hit.getSourceAsMap().get("ele_active")).orElseGet(() -> 0.0));
                    double sub = BigDemicalUtil.sub(respVO.getEleActiveEnd(), respVO.getEleActiveStart());
                    respVO.setEleActive(sub);
                    if (sub < 0) {
                        respVO.setEleActive(respVO.getEleActiveEnd());
                    }
                }
            }
            String s = locaMap.get(record.getPduKey());
            if (Objects.nonNull(s)){
                respVO.setAddress(s);
            }
            mapList.add(respVO);
        }
        pageResult = new PageResult<>();
        pageResult.setList(mapList)
                .setTotal(total);

        return pageResult;
    }

}