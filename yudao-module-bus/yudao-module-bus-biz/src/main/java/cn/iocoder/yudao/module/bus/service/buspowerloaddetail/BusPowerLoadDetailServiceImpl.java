package cn.iocoder.yudao.module.bus.service.buspowerloaddetail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailRespVO;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataDetailsReqVO;
import com.alibaba.fastjson2.JSONObject;
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
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class BusPowerLoadDetailServiceImpl implements BusPowerLoadDetailService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestHighLevelClient client;

    @Override
    public BusPowerLoadDetailRespVO getDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException {
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + reqVO.getDevKey());
        if (jsonObject == null) {
            return null;
        }
        Double runLoad = jsonObject.getJSONObject("bus_total_data").getDouble("pow_apparent");
        Double ratedCapacity = jsonObject.getJSONObject("bus_cfg").getDouble("cur_specs") * 220 * 3;
        Double reserveMargin = ratedCapacity - runLoad;
        Double powActive = jsonObject.getJSONObject("bus_total_data").getDouble("pow_value");
        Double powReactive = jsonObject.getJSONObject("bus_total_data").getDouble("pow_reactive");

        // 异步执行 Elasticsearch 查询bus_hda_total_hour近24小时有功功率最大值
        CompletableFuture<Double> peakDemandFuture = CompletableFuture.supplyAsync(() -> {
            try {
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.indices("bus_hda_total_hour");
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.termQuery("bus_id", reqVO.getId()));
                searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                        .from("now-24h")
                        .to("now"));
                searchSourceBuilder.aggregation(
                        AggregationBuilders.max("pow_active_max").field("pow_active_max_value")
                );
                searchRequest.source(searchSourceBuilder);

                // 执行搜索
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                // 从聚合结果中获取最大值
                Max maxAggregation = searchResponse.getAggregations().get("pow_active_max");
                return maxAggregation.getValue();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
        // 等待异步操作完成并获取结果
        Double peakDemand = peakDemandFuture.join();

        // 返回数据
        BusPowerLoadDetailRespVO respVO = new BusPowerLoadDetailRespVO();
        respVO.setRunLoad(runLoad);
        respVO.setRatedCapacity(ratedCapacity);
        respVO.setReserveMargin(reserveMargin);
        respVO.setPowActive(powActive);
        respVO.setPowReactive(powReactive);
        respVO.setPeakDemand(peakDemand);

        return respVO;
    }

    @Override
    public Map<String, Object> getLineChartDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException {
        Long busId = reqVO.getId();
        if (busId == null){
            return null;
        }
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        searchSourceBuilder.query(QueryBuilders.termQuery("bus_id", busId));
        System.out.println(reqVO);
        if (Objects.equals(reqVO.getGranularity(), "realtime")){
            searchRequest.indices("bus_hda_line_realtime");
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from("now-1h")
                    .to("now"));
        } else if (Objects.equals(reqVO.getGranularity(), "hour")) {
            searchRequest.indices("bus_hda_line_hour");
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from("now-24h")
                    .to("now"));
        }else{
            searchRequest.indices("bus_hda_line_day");
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from("now-1M")
                    .to("now"));
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        Map<String, Object> resultMap = new HashMap<>();
        List<Object> resultLine1 = new ArrayList<>();
        List<Object> resultLine2 = new ArrayList<>();
        List<Object> resultLine3 = new ArrayList<>();
        hits.forEach(searchHit -> {
            // 获取文档内容，假设它以 Map 的形式存储
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            // 从文档内容中获取 line_id
            String lineId = (String) sourceAsMap.get("line_id");
            switch (lineId) {
                case "1":
                    resultLine1.add(sourceAsMap);
                    break;
                case "2":
                    resultLine2.add(sourceAsMap);
                    break;
                case "3":
                    resultLine3.add(sourceAsMap);
                    break;
                default:
            }
        });

        resultMap.put("L1", resultLine1);
        resultMap.put("L2", resultLine2);
        resultMap.put("L3", resultLine3);
        return resultMap;
    }

}
