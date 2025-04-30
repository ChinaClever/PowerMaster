package cn.iocoder.yudao.module.cabinet.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetEnvResVO;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataPageReqVO;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.module.cabinet.dal.mysql.index.CabIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.module.cabinet.service.energyconsumption.CabinetEnergyConsumptionService;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.CABINET_ID;
import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.KEYWORD;


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

    private DateTimeFormatter dateTimeFormat= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
                            String aisleName = aisleIndexMapper.selectById(indexDO.getAisleId()).getAisleName();
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
        indices = new String[]{"cabinet_hda_pow_realtime"};
        switch (granularity){
            case "realtime":
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusSeconds(100)};
                break;
            case "hour":
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1)};
                break;
            case "day":
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

    @Override
    public PageResult<CabinetEnvResVO> getHistoryDataPageEnv(CabinetHistoryDataPageReqVO pageReqVO,boolean isPage) {
        String index = null;
        switch (pageReqVO.getGranularity()){
            case "realtime":index="cabinet_env_realtime";
                break;
            case "hour":index ="cabinet_env_hour";
                break;
            case "day":index ="cabinet_env_day";
                break;
            default:
        }
        String startTime = pageReqVO.getTimeRange()[0];
        String endTime = pageReqVO.getTimeRange()[1];
        List<CabinetEnvResVO> list = new ArrayList<>();
        SearchResponse searchResponse = getDataEs(startTime, endTime, pageReqVO.getCabinetIds(), index,pageReqVO.getPageNo(),!isPage?10000:pageReqVO.getPageSize());
        if (searchResponse != null){
            searchResponse.getHits().forEach(hit -> {
                Map<String, Object> map = hit.getSourceAsMap();
                list.add(mapToCabinetEnvResVO(map));
            });
            return new PageResult<>(list,searchResponse.getHits().getTotalHits().value);
        }else {
            return new PageResult<>(new ArrayList(),0L);
        }
    }

    public CabinetEnvResVO mapToCabinetEnvResVO(Map<String, Object> map) {
        CabinetEnvResVO resVO = new CabinetEnvResVO();
        resVO.setId((Integer) map.get("cabinet_id"));
        resVO.setCreateTime((String) map.get("create_time"));
        Map<String, Object> sensorList = (Map<String, Object>) map.get("sensor_list");
        List black = (List) sensorList.get("black");
        List front = (List) sensorList.get("front");
        resVO.setFront(front);
        resVO.setBlack(black);
        resVO.setAddress("");
        IndexDO cab = cabIndexMapper.selectOne("id", resVO.getId());
        if (cab != null){
            resVO.setRoomId(cab.getRoomId());
            RoomIndex room = roomIndexMapper.selectById(cab.getRoomId());
            if(room!=null){
                resVO.setRoomName(room.getRoomName());
                if(room.getRoomName()!=null&&!"".equals(room.getRoomName())){
                    resVO.setAddress(room.getRoomName());
                }
            }
        }
        AisleIndex aisle = aisleIndexMapper.selectById(cab.getAisleId());
        if(aisle!=null&&aisle.getAisleName()!=null&&!"".equals(aisle.getAisleName())){
            if(resVO.getAddress().length()>0){
                resVO.setAddress(resVO.getAddress()+"-"+aisle.getAisleName());
            }else{
                resVO.setAddress(aisle.getAisleName());
            }
        }
        if(cab.getCabinetName()!=null&&!"".equals(cab.getCabinetName())){
            if(resVO.getAddress().length()>0){
                resVO.setAddress(resVO.getAddress()+"-"+cab.getCabinetName());
            }else{
                resVO.setAddress(cab.getCabinetName());
            }
            resVO.setCabinetName(cab.getCabinetName());
        }
        return resVO;
    }

    @Override
    public Map<String, Object> getEnvNewData() {
        String[] indices = new String[]{"cabinet_env_realtime", "cabinet_env_realtime", "cabinet_env_realtime"};
        String[] name = new String[]{"min", "hour", "day"};
        LocalDateTime[] timeAgo = new LocalDateTime[]{LocalDateTime.now().minusSeconds(100),LocalDateTime.now().minusHours(1), LocalDateTime.now().minusDays(1)};
        Map<String, Object> map = new HashMap<>(3);
        for (int i=0;i<3;i++){
            SearchRequest searchRequest = new SearchRequest();
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.rangeQuery("create_time.keyword").from(timeAgo[i].format(dateTimeFormat)).to(LocalDateTime.now().format(dateTimeFormat)));
            searchSourceBuilder.trackTotalHits(true);
            searchRequest.indices(indices[i]);
            searchRequest.source(searchSourceBuilder);
            try {
                SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
                if (response != null) {
                    map.put(name[i],response.getHits().getTotalHits().value);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return map;
    }

    @Override
    public PageResult<CabinetEnvResVO> getHistoryEnvDataDetails(Integer cabinetId, String granularity, String[] timeRange) {
        String index=null;
        switch (granularity){
            case "realtime":
                index="cabinet_env_realtime";
                break;
            case "hour":
                index ="cabinet_env_hour";
                break;
            case "day":
                index ="cabinet_env_day";
                break;
            default:
        }
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        if(timeRange!=null&&timeRange.length==2){
            searchSourceBuilder.query(QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery(CREATE_TIME+KEYWORD)
                    .from(timeRange[0]).to(timeRange[1]))
                    .must(QueryBuilders.termQuery(CABINET_ID, cabinetId)));
        }else{
            searchSourceBuilder.query(QueryBuilders.termQuery(CABINET_ID, cabinetId));
        }
        searchSourceBuilder.sort(CREATE_TIME+KEYWORD,SortOrder.ASC);
        try {
            searchRequest.indices(index);
            searchRequest.source(searchSourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            if (response != null) {
                List<Map<String,Object>> ans=new ArrayList<>();
                SearchHits hits = response.getHits();
                hits.forEach((hit)->{
                    ans.add(hit.getSourceAsMap());
                });
                List<CabinetEnvResVO> collect = ans.stream().map((map) -> mapToCabinetEnvResVO(map))
                        .collect(Collectors.toList());
                return new PageResult<>(collect,hits.getTotalHits().value);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new PageResult<CabinetEnvResVO>(new ArrayList(),0L);
    }


    private SearchResponse getDataEs(String startTime, String endTime, String[] ids, String index,Integer pageNo, Integer pageSize) {
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            if (Objects.nonNull(pageNo) && Objects.nonNull(pageSize)) {
                int pageIndex = (pageNo - 1) * pageSize;
                builder.from(pageIndex);
                // 最后一页请求超过一万，pageSize设置成请求刚好一万条
                if (pageIndex + pageSize > 10000) {
                    builder.size(10000 - pageIndex);
                } else {
                    builder.size(pageSize);
                }
                builder.trackTotalHits(true);
            }
            //获取需要处理的数据
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime));
            if (ObjectUtils.isNotEmpty(ids)) {
                boolQueryBuilder.must(QueryBuilders.termsQuery(CABINET_ID, ids));
            }
            builder.query(QueryBuilders.constantScoreQuery(boolQueryBuilder));
            builder.sort(CREATE_TIME+KEYWORD, SortOrder.DESC);
            // 设置搜索条件
            searchRequest.source(builder);


            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            return searchResponse;
        } catch (Exception e) {
            return null;
        }
    }
}