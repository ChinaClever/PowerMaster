package cn.iocoder.yudao.module.cabinet.service.historydata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataPageReqVO;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.module.cabinet.dal.mysql.index.CabIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.AisleIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.RoomIndexMapper;
import cn.iocoder.yudao.module.cabinet.service.energyconsumption.CabinetEnergyConsumptionService;
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


/**
 * @author linzhentian
 */
@Service
public class CabinetHistoryDataServiceImpl implements CabinetHistoryDataService {

    @Autowired
    private CabinetIndexMapper cabinetIndexMapper;
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
                        String roomName = roomIndexMapper.selectById(indexDO.getRoomId()).getName();
                        if(indexDO.getAisleId() != 0){
                            String aisleName = aisleIndexMapper.selectById(indexDO.getAisleId()).getName();
                            localtion = roomName + "-" + aisleName + "-" + indexDO.getName();
                        }else {
                            localtion = roomName + "-"  + indexDO.getName() ;
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
    public Map<String, Object> getOneHourSumData() throws IOException {
        String[] indices = new String[]{"cabinet_hda_pow_realtime"};
        String[] name = new String[]{"total"};
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Map<String, Object> map = cabinetEnergyConsumptionService.getSumData(indices, name, oneHourAgo);
        return map;
    }

//    @Override
//    public PageResult<Object> getEnvDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
//        PageResult<Object> pageResult = null;
//        Integer sensorId = pageReqVO.getSensorId();
//        // 搜索源构建对象
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        int pageNo = pageReqVO.getPageNo();
//        int pageSize = pageReqVO.getPageSize();
//        int index = (pageNo - 1) * pageSize;
//        searchSourceBuilder.from(index);
//        // 最后一页请求超过一万，pageSize设置成请求刚好一万条
//        if (index + pageSize > 10000){
//            searchSourceBuilder.size(10000 - index);
//        }else{
//            searchSourceBuilder.size(pageSize);
//        }
//        searchSourceBuilder.trackTotalHits(true);
//        searchSourceBuilder.sort("create_time.keyword", SortOrder.DESC);
//        if (!Objects.equals(pageReqVO.getIpAddr(), "") && !Objects.equals(pageReqVO.getIpAddr(), null)){
//            Integer pduId = getPduIdByAddr(pageReqVO.getIpAddr(), pageReqVO.getCascadeAddr());
//            if(pduId != null){
//                if (!Objects.equals(sensorId, 0)){
//                    // 创建范围查询
//                    QueryBuilder rangeQuery = QueryBuilders.rangeQuery("pdu_id").gte(pduId).lte(pduId);
//                    // 创建匹配查询
//                    QueryBuilder matchQuery = QueryBuilders.matchQuery("sensor_id", sensorId);
//                    // 创建BoolQueryBuilder对象
//                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//                    // 将范围查询和匹配查询添加到布尔查询中
//                    boolQuery.must(rangeQuery);
//                    boolQuery.must(matchQuery);
//                    // 将布尔查询设置到SearchSourceBuilder中
//                    searchSourceBuilder.query(boolQuery);
//                }else{
//                    // 这样构造的查询条件，将不进行score计算，从而提高查询效率
//                    searchSourceBuilder.query(QueryBuilders.constantScoreQuery(QueryBuilders.rangeQuery("pdu_id").gte(pduId).lte(pduId)));
//                }
//            }else{
//                // 查不到pdu 直接返回空数据
//                pageResult = new PageResult<>();
//                pageResult.setList(null)
//                        .setTotal(0L);
//                return pageResult;
//            }
//        }else{
//            if (!Objects.equals(sensorId, 0)){
//                QueryBuilder matchQuery = QueryBuilders.matchQuery("sensor_id", sensorId);
//                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//                boolQuery.must(matchQuery);
//                searchSourceBuilder.query(boolQuery);
//            }else{
//                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//            }
//        }
//
//        // 搜索请求对象
//        SearchRequest searchRequest = new SearchRequest();
//        if ("realtime".equals(pageReqVO.getGranularity())) {
//            searchRequest.indices("pdu_env_realtime");
//        } else if ("hour".equals(pageReqVO.getGranularity())) {
//            searchRequest.indices("pdu_env_hour");
//        } else {
//            searchRequest.indices("pdu_env_day");
//        }
//        if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
//            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
//                    .from(pageReqVO.getTimeRange()[0])
//                    .to(pageReqVO.getTimeRange()[1]));
//        }
//        searchRequest.source(searchSourceBuilder);
//        // 执行搜索,向ES发起http请求
//        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
//        // 搜索结果
//        List<Map<String, Object>> mapList = new ArrayList<>();
//        SearchHits hits = searchResponse.getHits();
//        hits.forEach(searchHit -> mapList.add(searchHit.getSourceAsMap()));
//        // 匹配到的总记录数
//        Long totalHits = hits.getTotalHits().value;
//        // 返回的结果
//        pageResult = new PageResult<>();
//        pageResult.setList(getLocationsByPduIds(mapList))
//                .setTotal(totalHits);
//
//        return pageResult;
//    }
//
//    @Override
//    public PageResult<Object> getEnvDataDetails(EnvDataDetailsReqVO reqVO) throws IOException {
//        Integer sensorId = reqVO.getSensorId();
//        Integer pduId = reqVO.getPduId();
//        if (Objects.equals(pduId, null)){
//            pduId = getPduIdByAddr(reqVO.getIpAddr(), reqVO.getCascadeAddr());
//            if (Objects.equals(pduId, null)){
//                return null;
//            }
//        }
//        // 创建BoolQueryBuilder对象
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//        // 搜索源构建对象
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
//        searchSourceBuilder.size(10000);
//        searchSourceBuilder.trackTotalHits(true);
//        PageResult<Object> pageResult = null;
//        // 搜索请求对象
//        SearchRequest searchRequest3 = new SearchRequest();
//        if ("realtime".equals(reqVO.getGranularity()) ){
//            searchRequest3.indices("pdu_env_realtime");
//        }else if ("hour".equals(reqVO.getGranularity()) ){
//            searchRequest3.indices("pdu_env_hour");
//        }else {
//            searchRequest3.indices("pdu_env_day");
//        }
//        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
//            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
//                    .from(reqVO.getTimeRange()[0])
//                    .to(reqVO.getTimeRange()[1]));
//        }
//        // 创建匹配查询
//        QueryBuilder matchQuery = QueryBuilders.matchQuery("pdu_id", pduId);
//        QueryBuilder matchQuery1 = QueryBuilders.matchQuery("sensor_id", sensorId);
//        // 将匹配查询添加到布尔查询中
//        boolQuery.must(matchQuery);
//        boolQuery.must(matchQuery1);
//        // 将布尔查询设置到SearchSourceBuilder中
//        searchSourceBuilder.query(boolQuery);
//        searchRequest3.source(searchSourceBuilder);
//        // 执行搜索,向ES发起http请求
//        SearchResponse searchResponse3 = client.search(searchRequest3, RequestOptions.DEFAULT);
//        // 搜索结果
//        List<Object> resultList3 = new ArrayList<>();
//        SearchHits hits3 = searchResponse3.getHits();
//        hits3.forEach(searchHit -> resultList3.add(searchHit.getSourceAsMap()));
//        // 匹配到的总记录数
//        Long totalHits3 = hits3.getTotalHits().value;
//        // 返回的结果
//        pageResult = new PageResult<>();
//        pageResult.setList(resultList3)
//                .setTotal(totalHits3);
//
//        return pageResult;
//    }

}