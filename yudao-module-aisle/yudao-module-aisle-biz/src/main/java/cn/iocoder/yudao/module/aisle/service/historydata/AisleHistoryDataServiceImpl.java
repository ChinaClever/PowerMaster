package cn.iocoder.yudao.module.aisle.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.enums.DelEnums;
import cn.iocoder.yudao.framework.common.mapper.AisleIndexMapper;
import cn.iocoder.yudao.framework.common.mapper.RoomIndexMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.aisle.controller.admin.historydata.vo.AisleHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.aisle.controller.admin.historydata.vo.AisleHistoryDataPageReqVO;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
import cn.iocoder.yudao.module.aisle.service.energyconsumption.AisleEnergyConsumptionService;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @author linzhentian
 */
@Service
public class AisleHistoryDataServiceImpl implements AisleHistoryDataService {

    @Autowired
    private AisleEnergyConsumptionService aisleEnergyConsumptionService;
    @Resource
    private AisleIndexMapper aisleIndexMapper;

    @Autowired
    private RoomIndexMapper roomIndexMapper;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public List<Object> getLocationsByAisleIds(List<Map<String, Object>> mapList) {
        List<Object> resultList = new ArrayList<>();
            for (Map<String, Object> map : mapList){
                try{
                    Object aisleId = map.get("aisle_id");
                    if (aisleId instanceof Integer) {
                        AisleIndex aisleIndex = aisleIndexMapper.selectById((Serializable) aisleId);
                        String roomName = roomIndexMapper.selectById(aisleIndex.getRoomId()).getRoomName();
                        String localtion = roomName + "-"  + aisleIndex.getAisleName() ;

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
    public PageResult<Object> getHistoryDataPage(AisleHistoryDataPageReqVO pageReqVO) throws IOException {
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

        String[] aisleIds = pageReqVO.getAisleIds();
        if (aisleIds != null){
            searchSourceBuilder.query(QueryBuilders.termsQuery("aisle_id", aisleIds));
        }
        if ("realtime".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("aisle_hda_pow_realtime");
        } else if ("hour".equals(pageReqVO.getGranularity())) {
            searchRequest.indices("aisle_hda_pow_hour");
        } else {
            searchRequest.indices("aisle_hda_pow_day");
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
        mapList.forEach(map -> {
            if(((Map)map).get("active_a_avg_value")!=null){
                ((Map)map).put("active_a_avg_value", new BigDecimal((Double) ((Map)map).get("active_a_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_a_max_value")!=null){
                ((Map)map).put("active_a_max_value", new BigDecimal((Double) ((Map)map).get("active_a_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_a_min_value")!=null){
                ((Map)map).put("active_a_min_value", new BigDecimal((Double) ((Map)map).get("active_a_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("active_b_avg_value")!=null){
                ((Map)map).put("active_b_avg_value", new BigDecimal((Double) ((Map)map).get("active_b_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_b_max_value")!=null){
                ((Map)map).put("active_b_max_value", new BigDecimal((Double) ((Map)map).get("active_b_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_b_min_value")!=null){
                ((Map)map).put("active_b_min_value", new BigDecimal((Double) ((Map)map).get("active_b_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("active_total_avg_value")!=null){
                ((Map)map).put("active_total_avg_value", new BigDecimal((Double) ((Map)map).get("active_total_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_total_max_value")!=null){
                ((Map)map).put("active_total_max_value", new BigDecimal((Double) ((Map)map).get("active_total_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_total_min_value")!=null){
                ((Map)map).put("active_total_min_value", new BigDecimal((Double) ((Map)map).get("active_total_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }


            if(((Map)map).get("apparent_a_avg_value")!=null){
                ((Map)map).put("apparent_a_avg_value", new BigDecimal((Double) ((Map)map).get("apparent_a_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_a_max_value")!=null){
                ((Map)map).put("apparent_a_max_value", new BigDecimal((Double) ((Map)map).get("apparent_a_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_a_min_value")!=null){
                ((Map)map).put("apparent_a_min_value", new BigDecimal((Double) ((Map)map).get("apparent_a_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("apparent_b_avg_value")!=null){
                ((Map)map).put("apparent_b_avg_value", new BigDecimal((Double) ((Map)map).get("apparent_b_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_b_max_value")!=null){
                ((Map)map).put("apparent_b_max_value", new BigDecimal((Double) ((Map)map).get("apparent_b_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_b_min_value")!=null){
                ((Map)map).put("apparent_b_min_value", new BigDecimal((Double) ((Map)map).get("apparent_b_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("apparent_total_avg_value")!=null){
                ((Map)map).put("apparent_total_avg_value", new BigDecimal((Double) ((Map)map).get("apparent_total_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_total_max_value")!=null){
                ((Map)map).put("apparent_total_max_value", new BigDecimal((Double) ((Map)map).get("apparent_total_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_total_min_value")!=null){
                ((Map)map).put("apparent_total_min_value", new BigDecimal((Double) ((Map)map).get("apparent_total_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("reactive_a_avg_value")!=null){
                ((Map)map).put("reactive_a_avg_value", new BigDecimal((Double) ((Map)map).get("reactive_a_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("reactive_b_avg_value")!=null){
                ((Map)map).put("reactive_b_avg_value", new BigDecimal((Double) ((Map)map).get("reactive_b_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("reactive_total_avg_value")!=null){
                ((Map)map).put("reactive_total_avg_value", new BigDecimal((Double) ((Map)map).get("reactive_total_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("reactive_total_max_value")!=null){
                ((Map)map).put("reactive_total_max_value", new BigDecimal((Double) ((Map)map).get("reactive_total_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("reactive_total_min_value")!=null){
                ((Map)map).put("reactive_total_min_value", new BigDecimal((Double) ((Map)map).get("reactive_total_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("factor_a_avg_value")!=null){
                ((Map)map).put("factor_a_avg_value", new BigDecimal((Double) ((Map)map).get("factor_a_avg_value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("factor_b_avg_value")!=null){
                ((Map)map).put("factor_b_avg_value", new BigDecimal((Double) ((Map)map).get("factor_b_avg_value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("factor_total_avg_value")!=null){
                ((Map)map).put("factor_total_avg_value", new BigDecimal((Double) ((Map)map).get("factor_total_avg_value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("active_a")!=null){
                ((Map)map).put("active_a", new BigDecimal((Double) ((Map)map).get("active_a")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_b")!=null){
                ((Map)map).put("active_b", new BigDecimal((Double) ((Map)map).get("active_b")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_total")!=null){
                ((Map)map).put("active_total", new BigDecimal((Double) ((Map)map).get("active_total")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("apparent_a")!=null){
                ((Map)map).put("apparent_a", new BigDecimal((Double) ((Map)map).get("apparent_a")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_b")!=null){
                ((Map)map).put("apparent_b", new BigDecimal((Double) ((Map)map).get("apparent_b")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_total")!=null){
                ((Map)map).put("apparent_total", new BigDecimal((Double) ((Map)map).get("apparent_total")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("reactive_a")!=null){
                ((Map)map).put("reactive_a", new BigDecimal((Double) ((Map)map).get("reactive_a")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("reactive_b")!=null){
                ((Map)map).put("reactive_b", new BigDecimal((Double) ((Map)map).get("reactive_b")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("reactive_total")!=null){
                ((Map)map).put("reactive_total", new BigDecimal((Double) ((Map)map).get("reactive_total")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("factor_a")!=null){
                ((Map)map).put("factor_a", new BigDecimal((Double) ((Map)map).get("factor_a")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("factor_b")!=null){
                ((Map)map).put("factor_b", new BigDecimal((Double) ((Map)map).get("factor_b")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("factor_total")!=null){
                ((Map)map).put("factor_total", new BigDecimal((Double) ((Map)map).get("factor_total")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        });
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回的结果
        pageResult = new PageResult<>();
        pageResult.setList(getLocationsByAisleIds(mapList))
                .setTotal(totalHits);

        return pageResult;
    }

    @Override
    public PageResult<Object> getHistoryDataDetails(AisleHistoryDataDetailsReqVO reqVO) throws IOException{
        Integer aisleId = reqVO.getAisleId();
        if (Objects.equals(aisleId, null)){
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
            searchRequest.indices("aisle_hda_pow_realtime");
        }else if ("hour".equals(reqVO.getGranularity()) ){
            searchRequest.indices("aisle_hda_pow_hour");
        }else {
            searchRequest.indices("aisle_hda_pow_day");
        }
        if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
            searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                    .from(reqVO.getTimeRange()[0])
                    .to(reqVO.getTimeRange()[1]));
        }
        searchSourceBuilder.query(QueryBuilders.termQuery("aisle_id", aisleId));
        searchRequest.source(searchSourceBuilder);
        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        List<Object> resultList = new ArrayList<>();
        SearchHits hits = searchResponse.getHits();
        hits.forEach(searchHit -> resultList.add(searchHit.getSourceAsMap()));
        resultList.stream().forEach(map -> {
            if(((Map)map).get("active_a_avg_value")!=null){
                ((Map)map).put("active_a_avg_value", new BigDecimal((Double) ((Map)map).get("active_a_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_a_max_value")!=null){
                ((Map)map).put("active_a_max_value", new BigDecimal((Double) ((Map)map).get("active_a_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_a_min_value")!=null){
                ((Map)map).put("active_a_min_value", new BigDecimal((Double) ((Map)map).get("active_a_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("active_b_avg_value")!=null){
                ((Map)map).put("active_b_avg_value", new BigDecimal((Double) ((Map)map).get("active_b_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_b_max_value")!=null){
                ((Map)map).put("active_b_max_value", new BigDecimal((Double) ((Map)map).get("active_b_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_b_min_value")!=null){
                ((Map)map).put("active_b_min_value", new BigDecimal((Double) ((Map)map).get("active_b_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("active_total_avg_value")!=null){
                ((Map)map).put("active_total_avg_value", new BigDecimal((Double) ((Map)map).get("active_total_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_total_max_value")!=null){
                ((Map)map).put("active_total_max_value", new BigDecimal((Double) ((Map)map).get("active_total_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_total_min_value")!=null){
                ((Map)map).put("active_total_min_value", new BigDecimal((Double) ((Map)map).get("active_total_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }


            if(((Map)map).get("apparent_a_avg_value")!=null){
                ((Map)map).put("apparent_a_avg_value", new BigDecimal((Double) ((Map)map).get("apparent_a_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_a_max_value")!=null){
                ((Map)map).put("apparent_a_max_value", new BigDecimal((Double) ((Map)map).get("apparent_a_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_a_min_value")!=null){
                ((Map)map).put("apparent_a_min_value", new BigDecimal((Double) ((Map)map).get("apparent_a_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("apparent_b_avg_value")!=null){
                ((Map)map).put("apparent_b_avg_value", new BigDecimal((Double) ((Map)map).get("apparent_b_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_b_max_value")!=null){
                ((Map)map).put("apparent_b_max_value", new BigDecimal((Double) ((Map)map).get("apparent_b_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_b_min_value")!=null){
                ((Map)map).put("apparent_b_min_value", new BigDecimal((Double) ((Map)map).get("apparent_b_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("apparent_total_avg_value")!=null){
                ((Map)map).put("apparent_total_avg_value", new BigDecimal((Double) ((Map)map).get("apparent_total_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_total_max_value")!=null){
                ((Map)map).put("apparent_total_max_value", new BigDecimal((Double) ((Map)map).get("apparent_total_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_total_min_value")!=null){
                ((Map)map).put("apparent_total_min_value", new BigDecimal((Double) ((Map)map).get("apparent_total_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("reactive_a_avg_value")!=null){
                ((Map)map).put("reactive_a_avg_value", new BigDecimal((Double) ((Map)map).get("reactive_a_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("reactive_b_avg_value")!=null){
                ((Map)map).put("reactive_b_avg_value", new BigDecimal((Double) ((Map)map).get("reactive_b_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("reactive_total_avg_value")!=null){
                ((Map)map).put("reactive_total_avg_value", new BigDecimal((Double) ((Map)map).get("reactive_total_avg_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("reactive_total_max_value")!=null){
                ((Map)map).put("reactive_total_max_value", new BigDecimal((Double) ((Map)map).get("reactive_total_max_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("reactive_total_min_value")!=null){
                ((Map)map).put("reactive_total_min_value", new BigDecimal((Double) ((Map)map).get("reactive_total_min_value")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("factor_a_avg_value")!=null){
                ((Map)map).put("factor_a_avg_value", new BigDecimal((Double) ((Map)map).get("factor_a_avg_value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("factor_b_avg_value")!=null){
                ((Map)map).put("factor_b_avg_value", new BigDecimal((Double) ((Map)map).get("factor_b_avg_value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("factor_total_avg_value")!=null){
                ((Map)map).put("factor_total_avg_value", new BigDecimal((Double) ((Map)map).get("factor_total_avg_value")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("active_a")!=null){
                ((Map)map).put("active_a", new BigDecimal((Double) ((Map)map).get("active_a")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_b")!=null){
                ((Map)map).put("active_b", new BigDecimal((Double) ((Map)map).get("active_b")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("active_total")!=null){
                ((Map)map).put("active_total", new BigDecimal((Double) ((Map)map).get("active_total")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("apparent_a")!=null){
                ((Map)map).put("apparent_a", new BigDecimal((Double) ((Map)map).get("apparent_a")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_b")!=null){
                ((Map)map).put("apparent_b", new BigDecimal((Double) ((Map)map).get("apparent_b")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("apparent_total")!=null){
                ((Map)map).put("apparent_total", new BigDecimal((Double) ((Map)map).get("apparent_total")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("reactive_a")!=null){
                ((Map)map).put("reactive_a", new BigDecimal((Double) ((Map)map).get("reactive_a")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("reactive_b")!=null){
                ((Map)map).put("reactive_b", new BigDecimal((Double) ((Map)map).get("reactive_b")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("reactive_total")!=null){
                ((Map)map).put("reactive_total", new BigDecimal((Double) ((Map)map).get("reactive_total")).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
            }

            if(((Map)map).get("factor_a")!=null){
                ((Map)map).put("factor_a", new BigDecimal((Double) ((Map)map).get("factor_a")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("factor_b")!=null){
                ((Map)map).put("factor_b", new BigDecimal((Double) ((Map)map).get("factor_b")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            if(((Map)map).get("factor_total")!=null){
                ((Map)map).put("factor_total", new BigDecimal((Double) ((Map)map).get("factor_total")).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        });
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
        indices = new String[]{"aisle_hda_pow_realtime"};
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
        map = aisleEnergyConsumptionService.getSumData(indices, key, timeAgo);
        return map;
    }

    @Override
    public List<Object> getNewExcelList(List<Object> list) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof Map && ((Map<?, ?>) obj).keySet().stream().allMatch(key -> key instanceof String)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                mapList.add(map);
            }
        }
        for(int i=0;i<mapList.size();i++){
            mapList.get(i).put("create_time",mapList.get(i).get("create_time").toString().substring(0,16));
        }
        return list;
    }

    @Override
    public List<Object> getNewDetailsExcelList(List<Object> list) {
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
        LambdaQueryWrapper<RoomIndex> queryWrapper = new LambdaQueryWrapper<RoomIndex>().orderByDesc(RoomIndex::getId)
                .in(RoomIndex::getId,roomIds).eq(RoomIndex::getIsDelete, DelEnums.NO_DEL.getStatus());
        List<RoomIndex> roomIndexList = roomIndexMapper.selectList(queryWrapper);
        return roomIndexList.stream().filter(item -> ObjectUtils.isNotEmpty(item.getId()))
                .collect(Collectors.toMap(RoomIndex::getId, roomIndex -> roomIndex));
    }

}