package cn.iocoder.yudao.module.bus.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.mapper.BoxIndexMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.BoxResBase;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusResBase;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataPageReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.module.bus.dal.mysql.busindex.BusIndexMapper;
import cn.iocoder.yudao.module.bus.service.boxindex.BoxIndexServiceImpl;
import cn.iocoder.yudao.module.bus.service.busindex.BusIndexServiceImpl;
import cn.iocoder.yudao.module.bus.vo.BoxNameVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import java.util.function.Function;
import java.util.stream.Collectors;


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
    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private BusIndexServiceImpl busIndexService;

    @Autowired
    private BoxIndexServiceImpl boxIndexService;


    @Override
    public String[] getBusIdsbyBusDevkeys(String[] devkeys) {
        LambdaQueryWrapper<BusIndexDO> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(ObjectUtils.isNotEmpty(devkeys),BusIndexDO::getBusKey, devkeys);
        List<BusIndexDO> list = busIndexMapper.selectList(queryWrapper);
        String[] busIds = new String[list.size()]; // 使用 list 的大小来初始化数组
        for (int i = 0; i < list.size(); i++) {
            busIds[i] = String.valueOf(list.get(i).getId());
            System.out.println(busIds[i]);
        }
        return busIds;
    }

    @Override
    public String[] getBoxIdsbyBoxDevkeys(String[] devkeys) {
        LambdaQueryWrapper<BoxIndex> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(ObjectUtils.isNotEmpty(devkeys),BoxIndex::getBoxKey, devkeys);
        List<BoxIndex> list = boxIndexMapper.selectList(queryWrapper);
        String[] boxIds = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            boxIds[i] = String.valueOf(list.get(i).getId());
            System.out.println(boxIds[i]);
        }
        return boxIds;
    }

    @Override
    public List<Object> getLocationsAndNameByBusIds(List<Map<String, Object>> mapList) {
        List<Object> resultList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            Object busId = map.get("bus_id");
            if (busId instanceof Integer) {
                // 查询位置
                BusIndexDO busIndex = busIndexMapper.selectById((int) busId);
                if (busIndex != null) {
                    String dev_key = busIndex.getBusKey();


//                    String[] parts = dev_key.split("_");
                    map.put("dev_key", dev_key);
                    map.put("bus_name", busIndex.getBusName());
//                    map.put("ip_addr", parts[0]);
                    // 创建一个列表来存放要传递的对象 用于获取位置信息
                    List<BusResBase> busResBaseList = new ArrayList<>();
                    BusResBase busResBase = new BusResBase();// 创建 BusResBase 对象
                    busResBase.setBusId((Integer) busId);
                    busResBase.setBusName(busIndex.getBusName());
                    busResBase.setDevKey(busIndex.getBusKey());
                    busResBaseList.add(busResBase);// 将对象添加到列表中
                    try {
                        busIndexService.getPosition(busResBaseList);
                        map.put("location", busResBaseList.get(0).getLocation());
                    } catch (Exception e) {
                        map.put("location", null);
                    }

                } else {
                    map.put("dev_key", null);
                    map.put("location", null);
                }
            } else {
                map.put("dev_key", null);
                map.put("location", null);
            }
            resultList.add(map);
        }
        return resultList;
    }

    @Override
    public List<Object> getLocationsAndNameByBoxIds(List<Map<String, Object>> mapList) {
        List<Object> resultList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(mapList)) {
            List<Integer> boxIds = mapList.stream().map(i -> Integer.valueOf(i.get("box_id").toString())).collect(Collectors.toList());
            List<BoxIndex> boxIndices = boxIndexMapper.selectList(new LambdaQueryWrapper<BoxIndex>().in(BoxIndex::getId, boxIds));
            if (!CollectionUtils.isEmpty(boxIndices)) {
                Map<Integer, BoxIndex> boxIndexMap = boxIndices.stream().collect(Collectors.toMap(BoxIndex::getId, Function.identity()));
                List<String> devKeys = boxIndices.stream().map(BoxIndex::getBoxKey).distinct().collect(Collectors.toList());

                Map<String, BoxNameVO> roomByKeys = boxIndexService.getRoomByKeys(devKeys);
                for (Map<String, Object> map : mapList) {
                    Object boxId = map.get("box_id");
                    if (boxId instanceof Integer) {
                        // 查询位置
                        BoxIndex boxIndex = boxIndexMap.get((int) boxId);
                        if (boxIndex != null) {
                            map.put("dev_key", boxIndex.getBoxKey());
                            map.put("bus_name", boxIndex.getBoxName());
                            BoxNameVO boxNameVO = roomByKeys.get(boxIndex.getBoxKey());
                            if (Objects.nonNull(boxNameVO)) {
                                map.put("location", boxNameVO.getLocaltion());
                            }
                        } else {
                            map.put("dev_key", null);
                            map.put("location", null);
                        }
                    } else {
                        map.put("dev_key", null);
                        map.put("location", null);
                    }
                    resultList.add(map);
                }
            }
        }

        return resultList;
    }

    @Override
    public Map getHistoryDataTypeMaxValue(String[] boxIds) throws IOException {
        HashMap resultMap = new HashMap<>();
        String[] indexArr = new String[]{"box_hda_loop_hour", "box_hda_outlet_hour"};
        String[] fieldNameArr = new String[]{"loop_id", "outlet_id"};
        for (int i = 0; i < indexArr.length; i++) {
            SearchRequest searchRequest = new SearchRequest(indexArr[i]);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//            if (boxIds != null) {
//                // 这里boxIds实际上是devkey 要先查到真正的boxIds
//                boxIds = getBoxIdsbyBoxDevkeys(boxIds);
//                searchSourceBuilder.query(QueryBuilders.termsQuery("box_id", boxIds));
//            }
            // 添加最大值聚合
            searchSourceBuilder.aggregation(
                    AggregationBuilders.max("max_value").field(fieldNameArr[i])
            );

            searchSourceBuilder.size(0);
            searchRequest.source(searchSourceBuilder);
            // 执行搜索请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            // 从聚合结果中获取最大值
            Max maxAggregation = searchResponse.getAggregations().get("max_value");
            Integer maxValue = (int) maxAggregation.getValue();
            resultMap.put(fieldNameArr[i] + "_max_value", maxValue);
        }
        resultMap.put("line_id_max_value",3);
        return resultMap;
    }


    @Override
    public PageResult<Object> getBusHistoryDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException {
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
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        String[] devkeys = pageReqVO.getDevkeys();
        String[] busIds = new String[0];
        if (devkeys != null) {
            busIds = getBusIdsbyBusDevkeys(devkeys);
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
                if (pageReqVO.getLineId() != null) {
                    Integer lineId = pageReqVO.getLineId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("line_id", lineId);
                    if (busIds != null && busIds.length != 0) {
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
        PageResult<Object> pageResult = new PageResult<>();
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
        if (index + pageSize > 10000) {
            searchSourceBuilder.size(10000 - index);
        } else {
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
        String[] devkeys = pageReqVO.getDevkeys();
        String[] boxIds = new String[0];
        if (devkeys != null) {
            boxIds = getBoxIdsbyBoxDevkeys(devkeys);
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
                if (pageReqVO.getLineId() != null) {
                    Integer lineId = pageReqVO.getLineId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("line_id", lineId);
                    if (boxIds != null && boxIds.length > 0) {
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
                if (pageReqVO.getLoopId() != null) {
                    Integer loopId = pageReqVO.getLoopId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("loop_id", loopId);
                    if (boxIds != null && boxIds.length > 0) {
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
                if (pageReqVO.getOutletId() != null) {
                    Integer outletId = pageReqVO.getOutletId();
                    // 创建匹配查询
                    QueryBuilder termQuery = QueryBuilders.termQuery("outlet_id", outletId);
                    if (boxIds != null && boxIds.length > 0) {
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
    public PageResult<Object> getBusHistoryDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException {
        Integer busId = reqVO.getBusId();
        if (busId == null) {
            String devkey = reqVO.getDevkey();
            String[] devkeys = new String[1];
            devkeys[0] = devkey;
            String[] busIds = getBusIdsbyBusDevkeys(devkeys);
            if (busIds.length == 0) {
                return null;
            }
            busId = Integer.valueOf(busIds[0]);
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
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        PageResult<Object> pageResult = null;
        switch (reqVO.getType()) {
            case "total":
                if ("realtime".equals(reqVO.getGranularity())) {
                    searchRequest.indices("bus_hda_total_realtime");
                } else if ("hour".equals(reqVO.getGranularity())) {
                    searchRequest.indices("bus_hda_total_hour");
                } else {
                    searchRequest.indices("bus_hda_total_day");
                }
                searchSourceBuilder.query(QueryBuilders.termQuery("bus_id", busId));
                break;

            case "line":
                if ("realtime".equals(reqVO.getGranularity())) {
                    searchRequest.indices("bus_hda_line_realtime");
                } else if ("hour".equals(reqVO.getGranularity())) {
                    searchRequest.indices("bus_hda_line_hour");
                } else {
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
    public PageResult<Object> getBoxHistoryDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException {
        Integer boxId = reqVO.getBoxId();
        if (boxId == null) {
            String devkey = reqVO.getDevkey();
            String[] devkeys = new String[1];
            devkeys[0] = devkey;
            String[] boxIds = getBoxIdsbyBoxDevkeys(devkeys);
            if (boxIds.length == 0) {
                return null;
            }
            boxId = Integer.valueOf(boxIds[0]);
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
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        PageResult<Object> pageResult = null;
        switch (reqVO.getType()) {
            case "total":
                if ("realtime".equals(reqVO.getGranularity())) {
                    searchRequest.indices("box_hda_total_realtime");
                } else if ("hour".equals(reqVO.getGranularity())) {
                    searchRequest.indices("box_hda_total_hour");
                } else {
                    searchRequest.indices("box_hda_total_day");
                }
                searchSourceBuilder.query(QueryBuilders.termQuery("box_id", boxId));
                break;

            case "line":
                if ("realtime".equals(reqVO.getGranularity())) {
                    searchRequest.indices("box_hda_line_realtime");
                } else if ("hour".equals(reqVO.getGranularity())) {
                    searchRequest.indices("box_hda_line_hour");
                } else {
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
                if ("realtime".equals(reqVO.getGranularity())) {
                    searchRequest.indices("box_hda_loop_realtime");
                } else if ("hour".equals(reqVO.getGranularity())) {
                    searchRequest.indices("box_hda_loop_hour");
                } else {
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
                if ("realtime".equals(reqVO.getGranularity())) {
                    searchRequest.indices("box_hda_outlet_realtime");
                } else if ("hour".equals(reqVO.getGranularity())) {
                    searchRequest.indices("box_hda_outlet_hour");
                } else {
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
        String[] key = new String[]{};
        LocalDateTime[] timeAgo = new LocalDateTime[0];
        Map<String, Object> map;
        switch (granularity) {
            case "realtime":
                key = new String[]{"total", "line"};
                indices = new String[]{"bus_hda_total_realtime", "bus_hda_line_realtime"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(1)};
                break;
            case "hour":
                key = new String[]{"total", "total1", "line", "line1"};
                indices = new String[]{"bus_hda_total_realtime", "bus_hda_total_hour", "bus_hda_line_realtime", "bus_hda_line_hour"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1)};
                break;
            case "day":
                key = new String[]{"total", "total1", "total2", "line", "line1", "line2"};
                indices = new String[]{"bus_hda_total_realtime", "bus_hda_total_hour", "bus_hda_total_day", "bus_hda_line_realtime", "bus_hda_line_hour", "bus_hda_line_day"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)};
                break;
            default:
        }
        map = getSumData(indices, key, timeAgo, "bus_id");
        if (map.containsKey("total1")) {
            map.put("total", (Long) map.get("total1") + (Long) map.get("total"));
        }
        if (map.containsKey("total2")) {
            map.put("total", (Long) map.get("total2") + (Long) map.get("total"));
        }
        if (map.containsKey("line1")) {
            map.put("line", (Long) map.get("line1") + (Long) map.get("line"));
        }
        if (map.containsKey("line2")) {
            map.put("line", (Long) map.get("line2") + (Long) map.get("line"));
        }
        return map;
    }

    @Override
    public Map<String, Object> getBoxNavNewData(String granularity) throws IOException {
        String[] indices = new String[0];
        String[] key = new String[]{};
        LocalDateTime[] timeAgo = new LocalDateTime[0];
        Map<String, Object> map;
        switch (granularity) {
            case "realtime":
                key = new String[]{"total", "line", "loop", "outlet"};
                indices = new String[]{"box_hda_total_realtime", "box_hda_line_realtime", "box_hda_loop_realtime", "box_hda_outlet_realtime"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(1), LocalDateTime.now().minusMinutes(1)};
                break;
            case "hour":
                key = new String[]{"total", "total1", "line", "line1", "loop", "loop1", "outlet", "outlet1"};
                indices = new String[]{"box_hda_total_realtime", "box_hda_total_hour", "box_hda_line_realtime", "box_hda_line_hour", "box_hda_loop_realtime", "box_hda_loop_hour", "box_hda_outlet_realtime", "box_hda_outlet_hour"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1)};
                break;
            case "day":
                key = new String[]{"total", "total1", "total2", "line", "line1", "line2", "loop", "loop1", "loop2", "outlet", "outlet1", "outlet2"};
                indices = new String[]{"box_hda_total_realtime", "box_hda_total_hour", "box_hda_total_day", "box_hda_line_realtime", "box_hda_line_hour", "box_hda_line_day", "box_hda_loop_realtime", "box_hda_loop_hour", "box_hda_loop_day", "box_hda_outlet_realtime", "box_hda_outlet_hour", "box_hda_outlet_day"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1)};
                break;
            default:
        }
        map = getSumData(indices, key, timeAgo, "box_id");
        if (map.containsKey("total1")) {
            map.put("total", (Long) map.get("total1") + (Long) map.get("total"));
        }
        if (map.containsKey("total2")) {
            map.put("total", (Long) map.get("total2") + (Long) map.get("total"));
        }
        if (map.containsKey("line1")) {
            map.put("line", (Long) map.get("line1") + (Long) map.get("line"));
        }
        if (map.containsKey("line2")) {
            map.put("line", (Long) map.get("line2") + (Long) map.get("line"));
        }
        if (map.containsKey("loop1")) {
            map.put("loop", (Long) map.get("loop1") + (Long) map.get("loop"));
        }
        if (map.containsKey("loop2")) {
            map.put("loop", (Long) map.get("loop2") + (Long) map.get("loop"));
        }
        if (map.containsKey("outlet1")) {
            map.put("outlet", (Long) map.get("outlet1") + (Long) map.get("outlet"));
        }
        if (map.containsKey("outlet2")) {
            map.put("outlet", (Long) map.get("outlet2") + (Long) map.get("outlet"));
        }

        return map;
    }

    @Override
    public PageResult<Object> getBusEnvDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException {
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
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        String[] devkeys = pageReqVO.getDevkeys();
        if (devkeys != null) {
            String[] busIds = getBusIdsbyBusDevkeys(devkeys);
            searchSourceBuilder.query(QueryBuilders.termsQuery("bus_id", busIds));
        }

        if ("realtime".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("bus_tem_realtime");
        } else if ("hour".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("bus_tem_hour");
        } else {
            searchRequest.indices("bus_tem_day");
        }
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(searchHit -> mapList.add(searchHit.getSourceAsMap()));
        //获得设备名称
        mapList.forEach(map -> map.put("bus_name", busIndexMapper.selectById((Integer) map.get("bus_id")) != null ? busIndexMapper.selectById(Integer.valueOf(map.get("bus_id").toString())).getBusName() : ""));
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回的结果
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(getLocationsAndNameByBusIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getBusEnvDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException {
        Integer busId = reqVO.getBusId();
        if (busId == null) {
            String devkey = reqVO.getDevkey();
            String[] devkeys = new String[1];
            devkeys[0] = devkey;
            String[] busIds = getBusIdsbyBusDevkeys(devkeys);
            if (busIds.length == 0) {
                return null;
            }
            busId = Integer.valueOf(busIds[0]);
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
        if ("realtime".equals(reqVO.getGranularity())) {
            searchRequest.indices("bus_tem_realtime");
        } else if ("hour".equals(reqVO.getGranularity())) {
            searchRequest.indices("bus_tem_hour");
        } else {
            searchRequest.indices("bus_tem_day");
        }
        searchSourceBuilder.query(QueryBuilders.termQuery("bus_id", busId));
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
    public Map<String, Object> getBusEnvNavNewData() throws IOException {
        String[] name = new String[]{"hour", "day", "week"};
        String[] indices = new String[]{"bus_tem_realtime", "bus_tem_hour", "bus_tem_day"};
        LocalDateTime[] timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusWeeks(1)};
        Map<String, Object> map = getSumData(indices, name, timeAgo, "bus_id");
        return map;
    }

    @Override
    public PageResult<Object> getBoxEnvDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException {
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
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(pageReqVO.getTimeRange()[0])
                    .to(pageReqVO.getTimeRange()[1]));
        }
        String[] devkeys = pageReqVO.getDevkeys();
        if (devkeys != null) {
            String[] boxIds = getBoxIdsbyBoxDevkeys(devkeys);
            searchSourceBuilder.query(QueryBuilders.termsQuery("box_id", boxIds));
        }

        if ("realtime".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("box_tem_realtime");
        } else if ("hour".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("box_tem_hour");
        } else {
            searchRequest.indices("box_tem_day");
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
        pageResult.setList(getLocationsAndNameByBoxIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getBoxEnvDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException {
        Integer boxId = reqVO.getBoxId();
        if (boxId == null) {
            String devkey = reqVO.getDevkey();
            String[] devkeys = new String[1];
            devkeys[0] = devkey;
            String[] boxIds = getBoxIdsbyBoxDevkeys(devkeys);
            if (boxIds.length == 0) {
                return null;
            }
            boxId = Integer.valueOf(boxIds[0]);
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
        if ("realtime".equals(reqVO.getGranularity())) {
            searchRequest.indices("box_tem_realtime");
        } else if ("hour".equals(reqVO.getGranularity())) {
            searchRequest.indices("box_tem_hour");
        } else {
            searchRequest.indices("box_tem_day");
        }
        searchSourceBuilder.query(QueryBuilders.termQuery("box_id", boxId));
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
    public Map<String, Object> getBoxEnvNavNewData() throws IOException {
        String[] name = new String[]{"hour", "day", "week"};
        String indices = "box_tem_realtime";
        LocalDateTime[] timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusWeeks(1)};
        //Map<String, Object> map = getSumData(indices, name, timeAgo, "box_id");

        Map<String, Object> resultItem = getStringObjectMap(timeAgo, indices, name);
        return resultItem;
    }

    private Map<String, Object> getStringObjectMap(LocalDateTime[] timeAgo, String indices, String[] name) throws IOException {
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
                    AggregationBuilders.count("total_insertions").field("box_id")
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
    public List<Object> getNewHistoryList(List<Object> list) {
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
    public List<Object> getNewHistoryList1(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        for (int i = 0; i < mapList.size(); i++) {
            if (mapList.get(i).containsKey("cur_zero_max_time")) {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_max_time", mapList.get(i).get("pow_active_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_min_time", mapList.get(i).get("pow_active_min_time").toString().substring(0, 16));
                mapList.get(i).put("cur_zero_max_time", mapList.get(i).get("cur_zero_max_time").toString().substring(0, 16));
                mapList.get(i).put("cur_zero_min_time", mapList.get(i).get("cur_zero_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_max_time", mapList.get(i).get("pow_apparent_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_min_time", mapList.get(i).get("pow_apparent_min_time").toString().substring(0, 16));
                mapList.get(i).put("cur_residual_max_time", mapList.get(i).get("cur_residual_max_time").toString().substring(0, 16));
                mapList.get(i).put("cur_residual_min_time", mapList.get(i).get("cur_residual_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_max_time", mapList.get(i).get("pow_reactive_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_min_time", mapList.get(i).get("pow_reactive_min_time").toString().substring(0, 16));
                if (mapList.get(i).containsKey("vol_max_time")) {
                    mapList.get(i).put("vol_max_time", mapList.get(i).get("vol_max_time").toString().substring(0, 16));
                    mapList.get(i).put("vol_min_time", mapList.get(i).get("vol_min_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_max_time", mapList.get(i).get("cur_max_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_min_time", mapList.get(i).get("cur_min_time").toString().substring(0, 16));
                    mapList.get(i).put("vol_line_max_time", mapList.get(i).get("vol_line_max_time").toString().substring(0, 16));
                    mapList.get(i).put("vol_line_min_time", mapList.get(i).get("vol_line_min_time").toString().substring(0, 16));
                }
            } else {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                mapList.get(i).put("cur_max_time", mapList.get(i).get("cur_max_time").toString().substring(0, 16));
                mapList.get(i).put("cur_min_time", mapList.get(i).get("cur_min_time").toString().substring(0, 16));
                mapList.get(i).put("vol_line_max_time", mapList.get(i).get("vol_line_max_time").toString().substring(0, 16));
                mapList.get(i).put("vol_line_min_time", mapList.get(i).get("vol_line_min_time").toString().substring(0, 16));
                mapList.get(i).put("vol_max_time", mapList.get(i).get("vol_max_time").toString().substring(0, 16));
                mapList.get(i).put("vol_min_time", mapList.get(i).get("vol_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_max_time", mapList.get(i).get("pow_active_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_min_time", mapList.get(i).get("pow_active_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_max_time", mapList.get(i).get("pow_apparent_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_min_time", mapList.get(i).get("pow_apparent_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_max_time", mapList.get(i).get("pow_reactive_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_min_time", mapList.get(i).get("pow_reactive_min_time").toString().substring(0, 16));
            }
        }
        return list;
    }

    @Override
    public List<Object> getNewBoxHistoryList(List<Object> list) {
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
    public List<Object> getNewBoxHistoryList1(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        for (int i = 0; i < mapList.size(); i++) {

            if (mapList.get(i).containsKey("line_id")) {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_max_time", mapList.get(i).get("pow_active_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_min_time", mapList.get(i).get("pow_active_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_max_time", mapList.get(i).get("pow_reactive_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_min_time", mapList.get(i).get("pow_reactive_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_max_time", mapList.get(i).get("pow_apparent_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_min_time", mapList.get(i).get("pow_apparent_min_time").toString().substring(0, 16));
                mapList.get(i).put("cur_thd_max_time", mapList.get(i).get("cur_thd_max_time").toString().substring(0, 16));
                mapList.get(i).put("cur_thd_min_time", mapList.get(i).get("cur_thd_min_time").toString().substring(0, 16));
                mapList.get(i).put("vol_max_time", mapList.get(i).get("vol_max_time").toString().substring(0, 16));
                mapList.get(i).put("vol_min_time", mapList.get(i).get("vol_min_time").toString().substring(0, 16));
                mapList.get(i).put("cur_max_time", mapList.get(i).get("cur_max_time").toString().substring(0, 16));
                mapList.get(i).put("cur_min_time", mapList.get(i).get("cur_min_time").toString().substring(0, 16));
            } else if (mapList.get(i).containsKey("loop_id")) {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                mapList.get(i).put("cur_max_time", mapList.get(i).get("cur_max_time").toString().substring(0, 16));
                mapList.get(i).put("cur_min_time", mapList.get(i).get("cur_min_time").toString().substring(0, 16));
                mapList.get(i).put("vol_max_time", mapList.get(i).get("vol_max_time").toString().substring(0, 16));
                mapList.get(i).put("vol_min_time", mapList.get(i).get("vol_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_max_time", mapList.get(i).get("pow_apparent_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_min_time", mapList.get(i).get("pow_apparent_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_max_time", mapList.get(i).get("pow_active_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_min_time", mapList.get(i).get("pow_active_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_max_time", mapList.get(i).get("pow_reactive_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_min_time", mapList.get(i).get("pow_reactive_min_time").toString().substring(0, 16));
            } else if (mapList.get(i).containsKey("outlet_id")) {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_max_time", mapList.get(i).get("pow_active_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_min_time", mapList.get(i).get("pow_active_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_max_time", mapList.get(i).get("pow_reactive_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_min_time", mapList.get(i).get("pow_reactive_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_max_time", mapList.get(i).get("pow_apparent_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_min_time", mapList.get(i).get("pow_apparent_min_time").toString().substring(0, 16));
            } else {
                mapList.get(i).put("pow_apparent_max_time", mapList.get(i).get("pow_apparent_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_min_time", mapList.get(i).get("pow_apparent_min_time").toString().substring(0, 16));
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_max_time", mapList.get(i).get("pow_active_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_min_time", mapList.get(i).get("pow_active_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_max_time", mapList.get(i).get("pow_reactive_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_min_time", mapList.get(i).get("pow_reactive_min_time").toString().substring(0, 16));
            }

        }
        return list;
    }

    @Override
    public List<Object> getNewDetailHistoryList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        if (mapList.get(0).containsKey("pow_active_avg_value")) {
            if (mapList.get(0).containsKey("line_id")) {
                for (int i = 0; i < mapList.size(); i++) {
                    mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_max_time", mapList.get(i).get("cur_max_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_min_time", mapList.get(i).get("cur_min_time").toString().substring(0, 16));
                    mapList.get(i).put("vol_max_time", mapList.get(i).get("vol_max_time").toString().substring(0, 16));
                    mapList.get(i).put("vol_min_time", mapList.get(i).get("vol_min_time").toString().substring(0, 16));
                    mapList.get(i).put("vol_line_max_time", mapList.get(i).get("vol_line_max_time").toString().substring(0, 16));
                    mapList.get(i).put("vol_line_min_time", mapList.get(i).get("vol_line_min_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_active_max_time", mapList.get(i).get("pow_active_max_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_active_min_time", mapList.get(i).get("pow_active_min_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_reactive_max_time", mapList.get(i).get("pow_reactive_max_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_reactive_min_time", mapList.get(i).get("pow_reactive_min_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_apparent_max_time", mapList.get(i).get("pow_apparent_max_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_apparent_min_time", mapList.get(i).get("pow_apparent_min_time").toString().substring(0, 16));
                }
            } else {
                for (int i = 0; i < mapList.size(); i++) {
                    mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_active_max_time", mapList.get(i).get("pow_active_max_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_active_min_time", mapList.get(i).get("pow_active_min_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_reactive_max_time", mapList.get(i).get("pow_reactive_max_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_reactive_min_time", mapList.get(i).get("pow_reactive_min_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_apparent_max_time", mapList.get(i).get("pow_apparent_max_time").toString().substring(0, 16));
                    mapList.get(i).put("pow_apparent_min_time", mapList.get(i).get("pow_apparent_min_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_residual_max_time", mapList.get(i).get("cur_residual_max_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_residual_min_time", mapList.get(i).get("cur_residual_min_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_zero_max_time", mapList.get(i).get("cur_zero_max_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_zero_min_time", mapList.get(i).get("cur_zero_min_time").toString().substring(0, 16));
                }
            }
        } else {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
            }
        }
        return list;
    }

    @Override
    public List<Object> getNewBoxDetailHistoryList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        if (mapList.get(0).containsKey("pow_active_avg_value")) {
            if (mapList.get(0).containsKey("cur_thd_avg_value")) {
                for (int i = 0; i < mapList.size(); i++) {
                    mapList.get(i).put("cur_thd_max_time", mapList.get(i).get("cur_thd_max_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_thd_min_time", mapList.get(i).get("cur_thd_min_time").toString().substring(0, 16));
                }
            }

            if (mapList.get(0).containsKey("vol_avg_value")) {
                for (int i = 0; i < mapList.size(); i++) {
                    mapList.get(i).put("vol_max_time", mapList.get(i).get("vol_max_time").toString().substring(0, 16));
                    mapList.get(i).put("vol_min_time", mapList.get(i).get("vol_min_time").toString().substring(0, 16));
                }
            }
            if (mapList.get(0).containsKey("cur_avg_value")) {
                for (int i = 0; i < mapList.size(); i++) {
                    mapList.get(i).put("cur_max_time", mapList.get(i).get("cur_max_time").toString().substring(0, 16));
                    mapList.get(i).put("cur_min_time", mapList.get(i).get("cur_min_time").toString().substring(0, 16));
                }
            }

            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_max_time", mapList.get(i).get("pow_active_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_active_min_time", mapList.get(i).get("pow_active_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_max_time", mapList.get(i).get("pow_reactive_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_reactive_min_time", mapList.get(i).get("pow_reactive_min_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_max_time", mapList.get(i).get("pow_apparent_max_time").toString().substring(0, 16));
                mapList.get(i).put("pow_apparent_min_time", mapList.get(i).get("pow_apparent_min_time").toString().substring(0, 16));
            }
        } else {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
            }
        }

        return list;
    }

    @Override
    public List<Object> getNewEnvHistoryList(List<Object> list) {
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
        if (mapList.get(0).containsKey("tem_b_max_time")) {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("tem_b_max_time", mapList.get(i).get("tem_b_max_time").toString().substring(0, 16));
            }
        }
        if (mapList.get(0).containsKey("tem_b_min_time")) {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("tem_b_min_time", mapList.get(i).get("tem_b_min_time").toString().substring(0, 16));
            }
        }
        if (mapList.get(0).containsKey("tem_a_max_time")) {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("tem_a_max_time", mapList.get(i).get("tem_b_max_time").toString().substring(0, 16));
            }
        }
        if (mapList.get(0).containsKey("tem_a_min_time")) {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("tem_a_min_time", mapList.get(i).get("tem_a_min_time").toString().substring(0, 16));
            }
        }
        if (mapList.get(0).containsKey("tem_c_max_time")) {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("tem_c_max_time", mapList.get(i).get("tem_c_max_time").toString().substring(0, 16));
            }
        }
        if (mapList.get(0).containsKey("tem_c_min_time")) {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("tem_c_min_time", mapList.get(i).get("tem_c_min_time").toString().substring(0, 16));
            }
        }
        if (mapList.get(0).containsKey("tem_n_max_time")) {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("tem_n_max_time", mapList.get(i).get("tem_n_max_time").toString().substring(0, 16));
            }
        }
        if (mapList.get(0).containsKey("tem_n_min_time")) {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("tem_n_min_time", mapList.get(i).get("tem_n_min_time").toString().substring(0, 16));
            }
        }


        return list;
    }

    @Override
    public List<Object> getNewTemHistoryList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        if (mapList.get(0).containsKey("tem_n_min_time")) {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
                mapList.get(i).put("tem_a_min_time", mapList.get(i).get("tem_a_min_time").toString().substring(0, 16));
                mapList.get(i).put("tem_a_max_time", mapList.get(i).get("tem_a_max_time").toString().substring(0, 16));
                mapList.get(i).put("tem_b_max_time", mapList.get(i).get("tem_b_max_time").toString().substring(0, 16));
                mapList.get(i).put("tem_b_min_time", mapList.get(i).get("tem_b_min_time").toString().substring(0, 16));
                mapList.get(i).put("tem_c_max_time", mapList.get(i).get("tem_c_max_time").toString().substring(0, 16));
                mapList.get(i).put("tem_c_min_time", mapList.get(i).get("tem_c_min_time").toString().substring(0, 16));
                mapList.get(i).put("tem_n_max_time", mapList.get(i).get("tem_n_max_time").toString().substring(0, 16));
                mapList.get(i).put("tem_n_min_time", mapList.get(i).get("tem_n_min_time").toString().substring(0, 16));
            }
        } else {
            for (int i = 0; i < mapList.size(); i++) {
                mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
            }

        }
        return list;
    }

}