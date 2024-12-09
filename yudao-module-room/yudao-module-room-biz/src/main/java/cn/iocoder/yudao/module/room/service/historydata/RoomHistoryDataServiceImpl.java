package cn.iocoder.yudao.module.room.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.room.controller.admin.historydata.vo.RoomHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.room.controller.admin.historydata.vo.RoomHistoryDataPageReqVO;
import cn.iocoder.yudao.module.room.service.energyconsumption.RoomEnergyConsumptionService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @author linzhentian
 */
@Service
public class RoomHistoryDataServiceImpl implements RoomHistoryDataService {

    @Autowired
    private RoomEnergyConsumptionService roomEnergyConsumptionService;
    @Resource
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public List<Object> getLocationsByRoomIds(List<Map<String, Object>> mapList) {
        List<Object> resultList = new ArrayList<>();
            for (Map<String, Object> map : mapList){
                try{
                    Object roomId = map.get("room_id");
                    if (roomId instanceof Integer) {
                        RoomIndex roomIndex = roomIndexMapper.selectById((Serializable) roomId);
                        String localtion = roomIndex.getRoomName() ;

                        map.put("location", localtion);
                    }else{
                        map.put("location", null);
                    }
                } catch (Exception  e) {
                    map.put("location", null);
                }
                resultList.add(map);
            }
        return resultList;
    }

    @Override
    public PageResult<Object> getHistoryDataPage(RoomHistoryDataPageReqVO pageReqVO) throws IOException {
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

        String[] roomIds = pageReqVO.getRoomIds();
        if (roomIds != null){
            searchSourceBuilder.query(QueryBuilders.termsQuery("room_id", roomIds));
        }
        if ("realtime".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("room_hda_pow_realtime");
        } else if ("hour".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("room_hda_pow_hour");
        } else {
            searchRequest.indices("room_hda_pow_day");
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
        PageResult<Object> pageResult = new PageResult<>();
        pageResult.setList(getLocationsByRoomIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getHistoryDataDetails(RoomHistoryDataDetailsReqVO reqVO) throws IOException{
        Integer roomId = reqVO.getRoomId();
        if (Objects.equals(roomId, null)){
            return null;
        }
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if ("realtime".equals(reqVO.getGranularity()) ){
            searchRequest.indices("room_hda_pow_realtime");
        }else if ("hour".equals(reqVO.getGranularity()) ){
            searchRequest.indices("room_hda_pow_hour");
        }else {
            searchRequest.indices("room_hda_pow_day");
        }
        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqVO.getTimeRange()[0])
                    .to(reqVO.getTimeRange()[1]));
        }
        searchSourceBuilder.query(QueryBuilders.termQuery("room_id", roomId));
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
    public Map<String, Object> getNavNewData(String granularity) throws IOException {
        String[] indices = new String[0];
        String[] key = new String[]{"total"};
        LocalDateTime[] timeAgo = new LocalDateTime[0];
        Map<String, Object> map;
        switch (granularity){
            case "realtime":
                indices = new String[]{"room_hda_pow_realtime"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusMinutes(1)};
                break;
            case "hour":
                indices = new String[]{"room_hda_pow_hour"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1)};
                break;
            case "day":
                indices = new String[]{"room_hda_pow_day"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1)};
                break;
            default:
        }
        map = roomEnergyConsumptionService.getSumData(indices, key, timeAgo);
        return map;
    }

}