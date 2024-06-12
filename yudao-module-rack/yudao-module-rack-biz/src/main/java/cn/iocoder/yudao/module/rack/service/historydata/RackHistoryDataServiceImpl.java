package cn.iocoder.yudao.module.rack.service.historydata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.module.cabinet.dal.mysql.index.CabIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.AisleIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.CabinetIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.RackIndexMapper;
import cn.iocoder.yudao.module.cabinet.mapper.RoomIndexMapper;
import cn.iocoder.yudao.module.cabinet.service.energyconsumption.CabinetEnergyConsumptionService;
import cn.iocoder.yudao.module.rack.controller.admin.historydata.vo.RackHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.rack.controller.admin.historydata.vo.RackHistoryDataPageReqVO;
import cn.iocoder.yudao.module.rack.service.energyconsumption.RackEnergyConsumptionService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * @author linzhentian
 */
@Service
public class RackHistoryDataServiceImpl implements RackHistoryDataService {

    @Autowired
    private RackEnergyConsumptionService rackEnergyConsumptionService;
    @Resource
    private CabIndexMapper cabIndexMapper;
    @Autowired
    private RoomIndexMapper roomIndexMapper;
    @Autowired
    private AisleIndexMapper aisleIndexMapper;
    @Autowired
    private RackIndexMapper rackIndexMapper;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public List<Object> getRackNameAndLocationsByCabinetIds(List<Map<String, Object>> mapList) {
        List<Object> resultList = new ArrayList<>();
            for (Map<String, Object> map : mapList){
                try{
                    Object cabinetId = map.get("cabinet_id");
                    if (cabinetId instanceof Integer) {
                        String localtion;
                        IndexDO indexDO = cabIndexMapper.selectById((Serializable) cabinetId);
                        String roomName = roomIndexMapper.selectById(indexDO.getRoomId()).getName();
                        if(indexDO.getAisleId() != 0){
                            String aisleName = aisleIndexMapper.selectById(indexDO.getAisleId()).getName();
                            localtion = roomName + "-" + aisleName + "-" + indexDO.getName();
                        }else {
                            localtion = roomName + "-"  + indexDO.getName() ;
                        }
                        Object rackId = map.get("rack_id");
                        String rackName = rackIndexMapper.selectById((Serializable) rackId).getRackName();
                        map.put("location", localtion);
                        map.put("rack_name", rackName);
                    }else{
                        map.put("location", null);
                        map.put("rack_name", null);
                    }
                } catch (Exception  e) {
                    map.put("location", null);
                    map.put("rack_name", null);
                }
                resultList.add(map);
            }
        return resultList;
    }

    @Override
    public PageResult<Object> getHistoryDataPage(RackHistoryDataPageReqVO pageReqVO) throws IOException {
        PageResult<Object> pageResult = null;
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

        String[] rackIds = pageReqVO.getRackIds();
        if (rackIds != null){
            searchSourceBuilder.query(QueryBuilders.termsQuery("rack_id", rackIds));
        }
        if ("realtime".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("rack_hda_pow_realtime");
        } else if ("hour".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("rack_hda_pow_hour");
        } else {
            searchRequest.indices("rack_hda_pow_day");
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

        pageResult.setList(getRackNameAndLocationsByCabinetIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getHistoryDataDetails(RackHistoryDataDetailsReqVO reqVO) throws IOException{
        Integer rackId = reqVO.getRackId();
        if (Objects.equals(rackId, null)){
            return null;
        }
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackTotalHits(true);
        PageResult<Object> pageResult = null;
        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        if ("realtime".equals(reqVO.getGranularity()) ){
            searchRequest.indices("rack_hda_pow_realtime");
        }else if ("hour".equals(reqVO.getGranularity()) ){
            searchRequest.indices("rack_hda_pow_hour");
        }else {
            searchRequest.indices("rack_hda_pow_day");
        }
        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqVO.getTimeRange()[0])
                    .to(reqVO.getTimeRange()[1]));
        }
        searchSourceBuilder.query(QueryBuilders.termQuery("rack_id", rackId));
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
                indices = new String[]{"rack_hda_pow_realtime"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusMinutes(1)};
                break;
            case "hour":
                indices = new String[]{"rack_hda_pow_hour"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusHours(1)};
                break;
            case "day":
                indices = new String[]{"rack_hda_pow_day"};
                timeAgo = new LocalDateTime[]{LocalDateTime.now().minusDays(1)};
                break;
            default:
        }
        map = rackEnergyConsumptionService.getSumData(indices, key, timeAgo);
        return map;
    }

}