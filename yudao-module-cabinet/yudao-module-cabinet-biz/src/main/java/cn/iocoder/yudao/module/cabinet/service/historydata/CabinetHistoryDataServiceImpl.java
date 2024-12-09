package cn.iocoder.yudao.module.cabinet.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataPageReqVO;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.module.cabinet.dal.mysql.index.CabIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.module.cabinet.service.energyconsumption.CabinetEnergyConsumptionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.ObjectUtils;
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
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author linzhentian
 */
@Service
public class CabinetHistoryDataServiceImpl implements CabinetHistoryDataService {

    @Autowired
    private CabinetEnergyConsumptionService cabinetEnergyConsumptionService;
    @Resource
    private CabIndexMapper cabIndexMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private AisleIndexMapper aisleIndexMapper;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public List<Object> getLocationsByCabinetIds(List<Map<String, Object>> mapList) {
        List<Object> resultList = new ArrayList<>();
            for (Map<String, Object> map : mapList){
                try{
                    Object cabinetId = map.get("cabinet_id");
                    if (cabinetId instanceof Integer) {
                        String localtion = null;
                        IndexDO indexDO = cabIndexMapper.selectById((Serializable) cabinetId);
                        String roomName = roomIndexMapper.selectById(indexDO.getRoomId()).getRoomName();
                        if(indexDO.getAisleId() != 0){
                            String aisleName = aisleIndexMapper.selectById(indexDO.getAisleId()).getName();
                            localtion = roomName + "-" + aisleName + "-" + indexDO.getCabinetName();
                        }else {
                            localtion = roomName + "-"  + indexDO.getCabinetName() ;
                        }
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
    public PageResult<Object> getHistoryDataPage(CabinetHistoryDataPageReqVO pageReqVO) throws IOException {
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

        String[] cabinetIds = pageReqVO.getCabinetIds();
        if (cabinetIds != null){
            searchSourceBuilder.query(QueryBuilders.termsQuery("cabinet_id", cabinetIds));
        }
        if ("realtime".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("cabinet_hda_pow_realtime");
        } else if ("hour".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("cabinet_hda_pow_hour");
        } else {
            searchRequest.indices("cabinet_hda_pow_day");
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
        pageResult.setList(getLocationsByCabinetIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getHistoryDataDetails(CabinetHistoryDataDetailsReqVO reqVO) throws IOException{
        Integer cabinetId = reqVO.getCabinetId();
        if (Objects.equals(cabinetId, null)){
            return null;
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
        SearchRequest searchRequest = new SearchRequest();
        if ("realtime".equals(reqVO.getGranularity()) ){
            searchRequest.indices("cabinet_hda_pow_realtime");
        }else if ("hour".equals(reqVO.getGranularity()) ){
            searchRequest.indices("cabinet_hda_pow_hour");
        }else {
            searchRequest.indices("cabinet_hda_pow_day");
        }
        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqVO.getTimeRange()[0])
                    .to(reqVO.getTimeRange()[1]));
        }
        searchSourceBuilder.query(QueryBuilders.termQuery("cabinet_id", cabinetId));
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
    public Map<String, Object> getNavNewData(String granularity) throws IOException {
        String[] indices = new String[0];
        String[] key = new String[]{"total"};
        LocalDateTime[] timeAgo = new LocalDateTime[0];
        Map<String, Object> map;
        switch (granularity){
            case "realtime":
                indices = new String[]{"cabinet_hda_pow_realtime"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusMinutes(1)};
                break;
            case "hour":
                indices = new String[]{"cabinet_hda_pow_hour"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1)};
                break;
            case "day":
                indices = new String[]{"cabinet_hda_pow_day"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1)};
                break;
            default:
        }
        map = cabinetEnergyConsumptionService.getSumData(indices, key, timeAgo);
        return map;
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
        for(int i=0;i<mapList.size();i++) {
            mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
        }
        if(mapList.get(0).containsKey("active_total_max_time")){
            for(int i=0;i<mapList.size();i++) {
                mapList.get(i).put("active_total_max_time", mapList.get(i).get("active_total_max_time").toString().substring(0, 16));
                mapList.get(i).put("active_total_min_time", mapList.get(i).get("active_total_min_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_total_max_time", mapList.get(i).get("apparent_total_max_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_total_min_time", mapList.get(i).get("apparent_total_min_time").toString().substring(0, 16));
                mapList.get(i).put("active_a_max_time", mapList.get(i).get("active_a_max_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_a_max_time", mapList.get(i).get("apparent_a_max_time").toString().substring(0, 16));
                mapList.get(i).put("active_a_min_time", mapList.get(i).get("active_a_min_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_a_min_time", mapList.get(i).get("apparent_a_min_time").toString().substring(0, 16));
                mapList.get(i).put("active_b_max_time", mapList.get(i).get("active_b_max_time").toString().substring(0, 16));
                mapList.get(i).put("active_b_min_time", mapList.get(i).get("active_b_min_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_b_max_time", mapList.get(i).get("apparent_b_max_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_b_min_time", mapList.get(i).get("apparent_b_min_time").toString().substring(0, 16));
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
        for(int i=0;i<mapList.size();i++) {
            mapList.get(i).put("create_time", mapList.get(i).get("create_time").toString().substring(0, 16));
        }
        if(mapList.get(0).containsKey("active_total_max_time")){
            for(int i=0;i<mapList.size();i++) {
                mapList.get(i).put("active_total_max_time", mapList.get(i).get("active_total_max_time").toString().substring(0, 16));
                mapList.get(i).put("active_total_min_time", mapList.get(i).get("active_total_min_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_total_max_time", mapList.get(i).get("active_total_max_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_total_min_time", mapList.get(i).get("active_total_min_time").toString().substring(0, 16));
            }
        }
        if(mapList.get(0).containsKey("active_b_max_time")){
            for(int i=0;i<mapList.size();i++) {
                mapList.get(i).put("active_b_max_time", mapList.get(i).get("active_b_max_time").toString().substring(0, 16));
                mapList.get(i).put("active_b_min_time", mapList.get(i).get("active_b_min_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_b_max_time", mapList.get(i).get("active_b_max_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_b_min_time", mapList.get(i).get("active_b_min_time").toString().substring(0, 16));
            }
        }
        if(mapList.get(0).containsKey("active_a_max_time")){
            for(int i=0;i<mapList.size();i++) {
                mapList.get(i).put("active_a_max_time", mapList.get(i).get("active_a_max_time").toString().substring(0, 16));
                mapList.get(i).put("active_a_min_time", mapList.get(i).get("active_a_min_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_a_max_time", mapList.get(i).get("active_a_max_time").toString().substring(0, 16));
                mapList.get(i).put("apparent_a_min_time", mapList.get(i).get("active_a_min_time").toString().substring(0, 16));
            }
        }


        return list;
    }

    @Override
    public Map<Integer, RoomIndex> getRoomById(List<Integer> roomIds) {
        LambdaQueryWrapper<RoomIndex> queryWrapper = new LambdaQueryWrapper<RoomIndex>().orderByDesc(RoomIndex::getCreateTime)
                .in(RoomIndex::getId,roomIds).eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus());
        List<RoomIndex> roomIndexList = roomIndexMapper.selectList(queryWrapper);
        return roomIndexList.stream().filter(item -> ObjectUtils.isNotEmpty(item.getId()))
                .collect(Collectors.toMap(RoomIndex::getId, roomIndex -> roomIndex));
    }

    @Override
    public Map<Integer, AisleIndex> getAisleByIds(List<Integer> aisleIds) {
        LambdaQueryWrapper<AisleIndex> queryWrapper = new LambdaQueryWrapper<AisleIndex>().orderByDesc(AisleIndex::getCreateTime)
                .in(AisleIndex::getId,aisleIds).eq(AisleIndex::getIsDelete, DelEnums.NO_DEL.getStatus());
        List<AisleIndex> roomIndexList = aisleIndexMapper.selectList(queryWrapper);
        return roomIndexList.stream().filter(item -> ObjectUtils.isNotEmpty(item.getId()))
                .collect(Collectors.toMap(AisleIndex::getId, roomIndex -> roomIndex));
    }
//        Map<String, DataMenu> collect = list1.stream().filter(item -> ObjectUtils.isNotEmpty(item.getDictionaryValue()))
//                .collect(Collectors.toMap(DataMenu::getDictionaryValue, x -> x, (oldVal, newVal) -> newVal));
}