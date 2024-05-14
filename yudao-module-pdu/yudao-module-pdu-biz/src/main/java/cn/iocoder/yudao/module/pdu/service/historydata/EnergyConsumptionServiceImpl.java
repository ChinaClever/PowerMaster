package cn.iocoder.yudao.module.pdu.service.historydata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EnergyConsumptionPageReqVO;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        switch (pageReqVO.getType()) {
            case "total":
                // 搜索请求对象
                SearchRequest searchRequest = new SearchRequest();
                if ("day".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_total_day");
                } else if ("week".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_eq_total_week");
                } else {
                    searchRequest.indices("pdu_eq_total_month");
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
                pageResult.setList(historyDataService.getLocationsByPduIds(mapList))
                        .setTotal(totalHits);
                break;

            case "outlet":
                // 搜索请求对象
                SearchRequest searchRequest1 = new SearchRequest();
                if ("day".equals(pageReqVO.getGranularity())) {
                    searchRequest1.indices("pdu_eq_outlet_day");
                } else if ("week".equals(pageReqVO.getGranularity())) {
                    searchRequest1.indices("pdu_eq_outlet_week");
                } else {
                    searchRequest1.indices("pdu_eq_outlet_outlet");
                }
                if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
                    searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(pageReqVO.getTimeRange()[0])
                            .to(pageReqVO.getTimeRange()[1]));
                }
                if( pageReqVO.getOutletId() != null){
                    String outletId = String.valueOf(pageReqVO.getOutletId());
                    // 创建匹配查询
                    QueryBuilder matchQuery1 = QueryBuilders.matchQuery("outlet_id", outletId);
                    boolQuery.must(matchQuery1);
                    // 将布尔查询设置到SearchSourceBuilder中
                    searchSourceBuilder.query(boolQuery);
                }
                searchRequest1.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse3 = client.search(searchRequest1, RequestOptions.DEFAULT);
                // 搜索结果
                List<Map<String, Object>> mapList3 = new ArrayList<>();
                SearchHits hits1 = searchResponse3.getHits();
                hits1.forEach(searchHit -> mapList3.add(searchHit.getSourceAsMap()));
                // 匹配到的总记录数
                Long totalHits1 = hits1.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(historyDataService.getLocationsByPduIds(mapList3))
                        .setTotal(totalHits1);
                break;

            default:

        }

        return pageResult;
    }
}
