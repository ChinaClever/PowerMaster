package cn.iocoder.yudao.module.bus.service.buspowerloaddetail;

import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.BoxResBase;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusResBase;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.buspowerloaddetail.VO.BusPowerLoadDetailRespVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.module.bus.dal.mysql.boxindex.BoxIndexCopyMapper;
import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;
import cn.iocoder.yudao.module.bus.service.boxindex.BoxIndexServiceImpl;
import cn.iocoder.yudao.module.bus.service.busindex.BusIndexServiceImpl;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
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
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.module.bus.constant.BusConstants.KEYWORD;

@Log4j2
@Service
public class BusPowerLoadDetailServiceImpl implements BusPowerLoadDetailService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private BusIndexMapper busIndexMapper;
    @Autowired
    private BoxIndexCopyMapper boxIndexCopyMapper;

    @Autowired
    private BusIndexServiceImpl busIndexService;

    @Autowired
    private BoxIndexServiceImpl boxIndexService;


    @Override
    public BusPowerLoadDetailRespVO getDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException {
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get("packet:bus:" + reqVO.getDevKey());
        if (jsonObject == null) {
            return null;
        }
        Double runLoad = jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDouble("pow_apparent");
        List<Double> curMax = jsonObject.getJSONObject("bus_data").getJSONObject("line_item_list").getJSONArray("cur_max").toList(Double.class);
        curMax.sort(Collections.reverseOrder());
        Double ratedCapacity = (curMax.get(0) * 220 + curMax.get(1) * 220 + curMax.get(2) * 220) / 1000;
        Double reserveMargin = ratedCapacity - runLoad;
        Double powActive = jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDouble("pow_value");
        Double powReactive = jsonObject.getJSONObject("bus_data").getJSONObject("bus_total_data").getDouble("pow_reactive");

        // 异步执行 Elasticsearch 查询bus_hda_total_hour近24小时总视在功率最大值
        CompletableFuture<Double> peakDemandFuture = CompletableFuture.supplyAsync(() -> {
            try {
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.indices("bus_hda_total_hour");
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


                String startTime = localDateTimeToString(LocalDateTime.now().minusDays(1));
                String endTime = localDateTimeToString(LocalDateTime.now());
                searchSourceBuilder.query(QueryBuilders.constantScoreQuery(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                        .must(QueryBuilders.termQuery("bus_id", reqVO.getId()))));
                searchSourceBuilder.aggregation(
                        AggregationBuilders.max("pow_apparent_max").field("pow_apparent_max_value")
                );
                searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
                searchRequest.source(searchSourceBuilder);

                // 执行搜索
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                // 从聚合结果中获取最大值
                Max maxAggregation = searchResponse.getAggregations().get("pow_apparent_max");
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

    private String localDateTimeToString(LocalDateTime time) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return time.format(fmt);
    }

    @Override
    public Map<String, Object> getLineChartDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException {
        Long busId = reqVO.getId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        if (busId == null) {
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
        if (Objects.equals(reqVO.getGranularity(), "realtime")) {
            searchRequest.indices("bus_hda_line_realtime");
            searchSourceBuilder.fetchSource(new String[]{"bus_id", "line_id", "pow_active", "pow_reactive", "pow_apparent", "power_factor", "vol_value", "cur_value", "load_rate", "vol_line", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(oneHourAgo.format(formatter))
                    .to(now.format(formatter)));
        } else if (Objects.equals(reqVO.getGranularity(), "hour")) {
            searchRequest.indices("bus_hda_line_hour");
            searchSourceBuilder.fetchSource(new String[]{"bus_id", "line_id", "pow_active_avg_value", "pow_reactive_avg_value", "power_factor_avg_value", "pow_apparent_avg_value", "vol_avg_value", "cur_avg_value", "vol_line_avg_value", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(oneDayAgo.format(formatter))
                    .to(now.format(formatter)));
        } else if (Objects.equals(reqVO.getGranularity(), "SeventyHours")) {
            searchRequest.indices("bus_hda_line_hour");
            searchSourceBuilder.fetchSource(new String[]{"bus_id", "line_id", "pow_active_avg_value", "pow_reactive_avg_value", "power_factor_avg_value", "pow_apparent_avg_value", "vol_avg_value", "cur_avg_value", "vol_line_avg_value", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(threeDaysAgo.format(formatter))
                    .to(now.format(formatter)));
        } else {
            searchRequest.indices("bus_hda_line_day");
            searchSourceBuilder.fetchSource(new String[]{"bus_id", "line_id", "pow_active_avg_value", "pow_reactive_avg_value", "power_factor_avg_value", "pow_apparent_avg_value", "vol_avg_value", "cur_avg_value", "vol_line_avg_value", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(oneMonthAgo.format(formatter))
                    .to(now.format(formatter)));
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        if (totalHits == 0) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<Object> resultLine1 = new ArrayList<>();
        List<Object> resultLine2 = new ArrayList<>();
        List<Object> resultLine3 = new ArrayList<>();
        hits.forEach(searchHit -> {
            // 获取文档内容，假设它以 Map 的形式存储
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            // 从文档内容中获取 line_id
            Integer lineId = (Integer) sourceAsMap.get("line_id");
            switch (lineId) {
                case 1:
                    resultLine1.add(sourceAsMap);
                    break;
                case 2:
                    resultLine2.add(sourceAsMap);
                    break;
                case 3:
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

    @Override
    public BusPowerLoadDetailRespVO getBoxDetailData(BusPowerLoadDetailReqVO reqVO) {
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + reqVO.getDevKey());
        if (jsonObject == null) {
            return null;
        }
        Double runLoad = jsonObject.getJSONObject("box_data").getJSONObject("box_total_data").getDouble("pow_apparent");
        List<Double> curMax = jsonObject.getJSONObject("box_data").getJSONObject("loop_item_list").getJSONArray("cur_max").toList(Double.class);
        curMax.sort(Collections.reverseOrder());
        Double ratedCapacity = (curMax.get(0) * 220 + curMax.get(1) * 220 + curMax.get(2) * 220) / 1000;
        Double reserveMargin = ratedCapacity - runLoad;
        Double powActive = jsonObject.getJSONObject("box_data").getJSONObject("box_total_data").getDouble("pow_active");
        Double powReactive = jsonObject.getJSONObject("box_data").getJSONObject("box_total_data").getDouble("pow_reactive");

        // 异步执行 Elasticsearch 查询bus_hda_total_hour近24小时总视在功率最大值
        CompletableFuture<Double> peakDemandFuture = CompletableFuture.supplyAsync(() -> {
            try {
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.indices("box_hda_total_hour");
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.termQuery("box_id", reqVO.getId()));
                searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                        .from("now-24h")
                        .to("now"));
                searchSourceBuilder.aggregation(
                        AggregationBuilders.max("pow_apparent_max").field("pow_apparent_max_value")
                );
                searchRequest.source(searchSourceBuilder);

                // 执行搜索
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                // 从聚合结果中获取最大值
                Max maxAggregation = searchResponse.getAggregations().get("pow_apparent_max");
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
    public Map<String, Object> getBoxLineChartDetailData(BusPowerLoadDetailReqVO reqVO) throws IOException {
        Long boxId = reqVO.getId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        if (boxId == null) {
            return null;
        }
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        searchSourceBuilder.query(QueryBuilders.termQuery("box_id", boxId));
        if (Objects.equals(reqVO.getGranularity(), "realtime")) {
            searchRequest.indices("box_hda_line_realtime");
            searchSourceBuilder.fetchSource(new String[]{"box_id", "line_id", "pow_active", "pow_reactive", "pow_apparent", "power_factor", "vol_value", "cur_value", "load_rate", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(oneHourAgo.format(formatter))
                    .to(now.format(formatter)));
        } else if (Objects.equals(reqVO.getGranularity(), "hour")) {
            searchRequest.indices("box_hda_line_hour");
            searchSourceBuilder.fetchSource(new String[]{"box_id", "line_id", "pow_active_avg_value", "pow_reactive_avg_value", "power_factor_avg_value", "pow_apparent_avg_value", "vol_avg_value", "cur_avg_value", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(oneDayAgo.format(formatter))
                    .to(now.format(formatter)));
        } else if (Objects.equals(reqVO.getGranularity(), "SeventyHours")) {
            searchRequest.indices("box_hda_line_hour");
            searchSourceBuilder.fetchSource(new String[]{"box_id", "line_id", "pow_active_avg_value", "pow_reactive_avg_value", "power_factor_avg_value", "pow_apparent_avg_value", "vol_avg_value", "cur_avg_value", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(threeDaysAgo.format(formatter))
                    .to(now.format(formatter)));
        } else {
            searchRequest.indices("box_hda_line_day");
            searchSourceBuilder.fetchSource(new String[]{"box_id", "line_id", "pow_active_avg_value", "pow_reactive_avg_value", "power_factor_avg_value", "pow_apparent_avg_value", "vol_avg_value", "cur_avg_value", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(oneMonthAgo.format(formatter))
                    .to(now.format(formatter)));
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        if (totalHits == 0) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<Object> resultLine1 = new ArrayList<>();
        List<Object> resultLine2 = new ArrayList<>();
        List<Object> resultLine3 = new ArrayList<>();
        hits.forEach(searchHit -> {
            // 获取文档内容，假设它以 Map 的形式存储
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            // 从文档内容中获取 line_id
            Integer lineId = (Integer) sourceAsMap.get("line_id");
            switch (lineId) {
                case 1:
                    resultLine1.add(sourceAsMap);
                    break;
                case 2:
                    resultLine2.add(sourceAsMap);
                    break;
                case 3:
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

    @Override
    public Map<String, Object> getBoxEqData(BusPowerLoadDetailReqVO reqVO) throws IOException {
        Long boxId = reqVO.getId();
        DecimalFormat df = new DecimalFormat("#.000");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        if (boxId == null) {
            return null;
        }
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        searchSourceBuilder.query(QueryBuilders.termQuery("box_id", boxId));
        if (Objects.equals(reqVO.getGranularity(), "realtime")) {
            searchRequest.indices("box_eq_total_day");
            searchSourceBuilder.fetchSource(new String[]{"box_id", "eq_value", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(oneMonthAgo.format(formatter))
                    .to(now.format(formatter)));
        } else if (Objects.equals(reqVO.getGranularity(), "hour")) {
            searchRequest.indices("box_ele_total_realtime");
            searchSourceBuilder.fetchSource(new String[]{"box_id", "ele_active", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(oneDayAgo.format(formatter))
                    .to(now.format(formatter)));
        } else if (Objects.equals(reqVO.getGranularity(), "SeventyHours")) {
            searchRequest.indices("box_ele_total_realtime");
            searchSourceBuilder.fetchSource(new String[]{"box_id", "ele_active", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(threeDaysAgo.format(formatter))
                    .to(now.format(formatter)));
        } else {
            searchRequest.indices("box_eq_total_day");
            searchSourceBuilder.fetchSource(new String[]{"box_id", "eq_value", "create_time"}, null);
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(oneMonthAgo.format(formatter))
                    .to(now.format(formatter)));
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        if (totalHits == 0) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<Map> result = new ArrayList<>();
        if (Objects.equals(reqVO.getGranularity(), "hour")) {
            hits.forEach(searchHit -> {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                result.add(sourceAsMap);
            });
            for (int i = 0; i < result.size() - 1; i++) {
                result.get(i).put("create_time", result.get(i + 1).get("create_time"));
                result.get(i).put("ele_active", Double.valueOf(df.format((double) result.get(i + 1).get("ele_active") - (double) result.get(i).get("ele_active"))));
            }
            result.remove(result.size() - 1);
        } else if (Objects.equals(reqVO.getGranularity(), "SeventyHours")) {
            hits.forEach(searchHit -> {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                result.add(sourceAsMap);
            });
            for (int i = 0; i < result.size() - 1; i++) {
                result.get(i).put("create_time", result.get(i + 1).get("create_time"));
                result.get(i).put("ele_active", Double.valueOf(df.format((double) result.get(i + 1).get("ele_active") - (double) result.get(i).get("ele_active"))));
            }
            result.remove(result.size() - 1);
        } else {
            hits.forEach(searchHit -> {
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                result.add(sourceAsMap);
            });
        }

        resultMap.put("L1", result);
        return resultMap;
    }

    @Override
    public Map<String, Object> getBusEqData(BusPowerLoadDetailReqVO reqVO) throws IOException {
        try {
            Long busId = reqVO.getId();
            DecimalFormat df = new DecimalFormat("#.000");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            if (busId == null) {
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
            if (Objects.equals(reqVO.getGranularity(), "realtime")) {
                searchRequest.indices("bus_eq_total_day");
                searchSourceBuilder.fetchSource(new String[]{"bus_id", "eq_value", "create_time"}, null);
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                        .from(oneMonthAgo.format(formatter))
                        .to(now.format(formatter)));
            } else if (Objects.equals(reqVO.getGranularity(), "hour")) {
                searchRequest.indices("bus_ele_total_realtime");
                searchSourceBuilder.fetchSource(new String[]{"bus_id", "ele_active", "create_time"}, null);
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                        .from(oneDayAgo.format(formatter))
                        .to(now.format(formatter)));
            } else if (Objects.equals(reqVO.getGranularity(), "SeventyHours")) {
                searchRequest.indices("bus_ele_total_realtime");
                searchSourceBuilder.fetchSource(new String[]{"bus_id", "ele_active", "create_time"}, null);
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                        .from(threeDaysAgo.format(formatter))
                        .to(now.format(formatter)));
            } else {
                searchRequest.indices("bus_eq_total_day");
                searchSourceBuilder.fetchSource(new String[]{"bus_id", "eq_value", "create_time"}, null);
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                        .from(oneMonthAgo.format(formatter))
                        .to(now.format(formatter)));
            }
            searchRequest.source(searchSourceBuilder);
            // 执行搜索,向ES发起http请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            // 搜索结果
            SearchHits hits = searchResponse.getHits();
            // 匹配到的总记录数
            Long totalHits = hits.getTotalHits().value;
            if (totalHits == 0) {
                return null;
            }
            Map<String, Object> resultMap = new HashMap<>();
            List<Map> result = new ArrayList<>();
            if (Objects.equals(reqVO.getGranularity(), "hour")) {
                hits.forEach(searchHit -> {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    result.add(sourceAsMap);
                });
                for (int i = 0; i < result.size() - 1; i++) {
                    result.get(i).put("create_time", result.get(i + 1).get("create_time"));
                    result.get(i).put("ele_active", Double.valueOf(df.format((double) result.get(i + 1).get("ele_active") - (double) result.get(i).get("ele_active"))));
                }
                result.remove(result.size() - 1);
            } else if (Objects.equals(reqVO.getGranularity(), "SeventyHours")) {
                hits.forEach(searchHit -> {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    result.add(sourceAsMap);
                });
                for (int i = 0; i < result.size() - 1; i++) {
                    result.get(i).put("create_time", result.get(i + 1).get("create_time"));
                    result.get(i).put("ele_active", Double.valueOf(df.format((double) result.get(i + 1).get("ele_active") - (double) result.get(i).get("ele_active"))));
                }
                result.remove(result.size() - 1);
            } else {
                hits.forEach(searchHit -> {
                    Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                    result.add(sourceAsMap);
                });
            }

            resultMap.put("L1", result);
            return resultMap;
        }catch (Exception e){
            log.error("母线用能详情错误："+e);
            return null;
        }
    }


    @Override
    public List<String> getBusDevKeyList() {
        List<String> result = busIndexMapper.selectList().stream().limit(10).collect(Collectors.toList())
                .stream().map(BusIndexDO::getBusKey).collect(Collectors.toList());
        return result;
    }

    @Override
    public List<String> getBoxDevKeyList() {
        List<String> result = boxIndexCopyMapper.selectList().stream()
                .limit(10)
                .filter(boxIndex -> boxIndex.getBoxType() == 0)
                .map(BoxIndex::getBoxKey)
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public BusResBase getBusIdAndLocationByDevKey(BusPowerLoadDetailReqVO reqVO) {
        BusIndexDO busIndexDO = busIndexMapper.selectOne(BusIndexDO::getBusKey, reqVO.getDevKey());
        if (busIndexDO != null) {
            List<BusResBase> busResBaseList = new ArrayList<>();
            BusResBase busResBase = new BusResBase();// 创建 BusResBase 对象
            busResBase.setBusId(busIndexDO.getId());
            busResBase.setBusName(busIndexDO.getBusName());
            busResBase.setDevKey(reqVO.getDevKey());
            busResBaseList.add(busResBase);// 将对象添加到列表中
            try {
                busIndexService.getPosition(busResBaseList);
                return busResBaseList.get(0);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public BoxResBase getBoxIdAndLocationByDevKey(BusPowerLoadDetailReqVO reqVO) {
        BoxIndex boxIndex = boxIndexCopyMapper.selectOne(BoxIndex::getBoxKey, reqVO.getDevKey());
        ValueOperations ops = redisTemplate.opsForValue();
        JSONObject jsonObject = (JSONObject) ops.get("packet:box:" + reqVO.getDevKey());
        if (jsonObject == null) {
            return null;
        }

        if (boxIndex != null) {
            List<BoxResBase> boxResBaseList = new ArrayList<>();
            BoxResBase boxResBase = new BoxResBase();// 创建 BoxResBase 对象
            boxResBase.setBusName(jsonObject.getString("bus_name"));
            boxResBase.setBoxId(boxIndex.getId());
            boxResBase.setBoxName(boxIndex.getBoxName());
            boxResBase.setDevKey(reqVO.getDevKey());
            boxResBaseList.add(boxResBase);// 将对象添加到列表中
            try {
                boxIndexService.getPosition(boxResBaseList);
                return boxResBaseList.get(0);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }

    }


}
