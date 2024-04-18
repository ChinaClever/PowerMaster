package cn.iocoder.yudao.module.pdu.service.historydata;

import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.loop.PduHdaLoopDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.loop.PduHdaLoopHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.loop.PduHdaLoopRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataPageReqVO;
import com.alibaba.excel.util.StringUtils;
import org.apache.catalina.connector.ClientAbortException;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author linzhentian
 */
@Service
public class HistoryDataServiceImpl implements HistoryDataService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult<Object> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int pageNo = pageReqVO.getPageNo();
        int pageSize = pageReqVO.getPageSize();
        int index = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(index);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.trackTotalHits(true);
        PageResult<Object> pageResult = null;
        switch (pageReqVO.getType()) {
            case "total":
                // 搜索请求对象
                SearchRequest searchRequest = new SearchRequest();
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_total_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest.indices("pdu_hda_total_hour");
                } else {
                    searchRequest.indices("pdu_hda_total_day");
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
                List<Object> resultList = new ArrayList<>();
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits.getHits()) {
                    String str = hit.getSourceAsString();
                    if ("realtime".equals(pageReqVO.getGranularity())) {
                        PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                        resultList.add(pduHdaTotalRealtimeDo);
                    } else if ("hour".equals(pageReqVO.getGranularity())) {
                        PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaTotalHourDo.class);
                        resultList.add(pduHdaTotalHourDo);
                    } else {
                        PduHdaTotalDayDo pduHdaTotalDayDo = JsonUtils.parseObject(str, PduHdaTotalDayDo.class);
                        resultList.add(pduHdaTotalDayDo);
                    }
                }
                // 匹配到的总记录数
                Long totalHits = hits.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(resultList)
                        .setTotal(totalHits);
                break;

            case "line":
                // 搜索请求对象
                SearchRequest searchRequest1 = new SearchRequest();
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest1.indices("pdu_hda_line_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest1.indices("pdu_hda_line_hour");
                } else {
                    searchRequest1.indices("pdu_hda_line_day");
                }
                if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
                    searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(pageReqVO.getTimeRange()[0])
                            .to(pageReqVO.getTimeRange()[1]));
                }
                searchRequest1.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> resultList1 = new ArrayList<>();
                SearchHits hits1 = searchResponse1.getHits();
                for (SearchHit hit : hits1.getHits()) {
                    String str = hit.getSourceAsString();
                    if ("realtime".equals(pageReqVO.getGranularity())) {
                        PduHdaLineRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaLineRealtimeDo.class);
                        resultList1.add(pduHdaTotalRealtimeDo);
                    } else if ("hour".equals(pageReqVO.getGranularity())) {
                        PduHdaLineHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaLineHourDo.class);
                        resultList1.add(pduHdaTotalHourDo);
                    } else {
                        PduHdaLineDayDo pduHdaTotalDayDo = JsonUtils.parseObject(str, PduHdaLineDayDo.class);
                        resultList1.add(pduHdaTotalDayDo);
                    }
                }
                // 匹配到的总记录数
                Long totalHits1 = hits1.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(resultList1)
                        .setTotal(totalHits1);
                break;

            case "loop":
                // 搜索请求对象
                SearchRequest searchRequest2 = new SearchRequest();
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest2.indices("pdu_hda_loop_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest2.indices("pdu_hda_loop_hour");
                } else {
                    searchRequest2.indices("pdu_hda_loop_day");
                }
                if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
                    searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(pageReqVO.getTimeRange()[0])
                            .to(pageReqVO.getTimeRange()[1]));
                }
                searchRequest2.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse2 = client.search(searchRequest2, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> resultList2 = new ArrayList<>();
                SearchHits hits2 = searchResponse2.getHits();
                for (SearchHit hit : hits2.getHits()) {
                    String str = hit.getSourceAsString();
                    if ("realtime".equals(pageReqVO.getGranularity())) {
                        PduHdaLoopRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaLoopRealtimeDo.class);
                        resultList2.add(pduHdaTotalRealtimeDo);
                    } else if ("hour".equals(pageReqVO.getGranularity())) {
                        PduHdaLoopHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaLoopHourDo.class);
                        resultList2.add(pduHdaTotalHourDo);
                    } else {
                        PduHdaLoopDayDo pduHdaTotalDayDo = JsonUtils.parseObject(str, PduHdaLoopDayDo.class);
                        resultList2.add(pduHdaTotalDayDo);
                    }
                }
                // 匹配到的总记录数
                Long totalHits2 = hits2.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(resultList2)
                        .setTotal(totalHits2);
                break;

            case "outlet":
                // 搜索请求对象
                SearchRequest searchRequest3 = new SearchRequest();
                if ("realtime".equals(pageReqVO.getGranularity())) {
                    searchRequest3.indices("pdu_hda_outlet_realtime");
                } else if ("hour".equals(pageReqVO.getGranularity())) {
                    searchRequest3.indices("pdu_hda_outlet_hour");
                } else {
                    searchRequest3.indices("pdu_hda_outlet_day");
                }
                if (pageReqVO.getTimeRange() != null && pageReqVO.getTimeRange().length != 0) {
                    searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(pageReqVO.getTimeRange()[0])
                            .to(pageReqVO.getTimeRange()[1]));
                }
                searchRequest3.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse3 = client.search(searchRequest3, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> resultList3 = new ArrayList<>();
                SearchHits hits3 = searchResponse3.getHits();
                for (SearchHit hit : hits3.getHits()) {
                    String str = hit.getSourceAsString();
                    if ("realtime".equals(pageReqVO.getGranularity())) {
                        PduHdaOutletRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaOutletRealtimeDo.class);
                        resultList3.add(pduHdaTotalRealtimeDo);
                    } else if ("hour".equals(pageReqVO.getGranularity())) {
                        PduHdaOutletHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaOutletHourDo.class);
                        resultList3.add(pduHdaTotalHourDo);
                    } else {
                        PduHdaOutletDayDo pduHdaTotalDayDo = JsonUtils.parseObject(str, PduHdaOutletDayDo.class);
                        resultList3.add(pduHdaTotalDayDo);
                    }
                }
                // 匹配到的总记录数
                Long totalHits3 = hits3.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(resultList3)
                        .setTotal(totalHits3);
                break;

            default:

        }

        return pageResult;
    }

    @Override
    public PageResult<Object> getHistoryDataDetails(HistoryDataDetailsReqVO reqVO) throws IOException{
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String id = String.valueOf(reqVO.getId());
        SimpleQueryStringBuilder simpleQueryStringBuilder = QueryBuilders.simpleQueryStringQuery(id);
        simpleQueryStringBuilder.field("pdu_id");
        simpleQueryStringBuilder.defaultOperator(Operator.OR);
        searchSourceBuilder.query(simpleQueryStringBuilder);
        searchSourceBuilder.trackTotalHits(true);
        PageResult<Object> pageResult = null;
        switch (reqVO.getType()) {
            case "total":
                // 搜索请求对象
                SearchRequest searchRequest = new SearchRequest();
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_total_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_total_hour");
                }else {
                    searchRequest.indices("pdu_hda_total_day");
                }
                if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
                    searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(reqVO.getTimeRange()[0])
                            .to(reqVO.getTimeRange()[1]));
                }
                searchRequest.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> resultList = new ArrayList<>();
                SearchHits hits = searchResponse.getHits();
                System.out.println(hits.getTotalHits());
                for (SearchHit hit : hits.getHits()) {
                    String str = hit.getSourceAsString();
                    if ("realtime".equals(reqVO.getGranularity()) ){
                        PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                        resultList.add(pduHdaTotalRealtimeDo);
                        System.out.println(pduHdaTotalRealtimeDo);
                    }else if ("hour".equals(reqVO.getGranularity()) ){
                        PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaTotalHourDo.class);
                        resultList.add(pduHdaTotalHourDo);
                        resultList.add(pduHdaTotalHourDo);
                    }else {
                        PduHdaTotalDayDo pduHdaTotalDayDo = JsonUtils.parseObject(str, PduHdaTotalDayDo.class);
                        resultList.add(pduHdaTotalDayDo);
                        resultList.add(pduHdaTotalDayDo);
                    }
                }
                // 匹配到的总记录数
                Long totalHits = hits.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(resultList)
                        .setTotal(totalHits);
                break;

            case "line":
                // 搜索请求对象
                SearchRequest searchRequest1 = new SearchRequest();
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest1.indices("pdu_hda_line_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest1.indices("pdu_hda_line_hour");
                }else {
                    searchRequest1.indices("pdu_hda_line_day");
                }
                if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
                    searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(reqVO.getTimeRange()[0])
                            .to(reqVO.getTimeRange()[1]));
                }
                searchRequest1.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> resultList1 = new ArrayList<>();
                SearchHits hits1 = searchResponse1.getHits();
                for (SearchHit hit : hits1.getHits()) {
                    String str = hit.getSourceAsString();
                    if ("realtime".equals(reqVO.getGranularity()) ){
                        PduHdaLineRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaLineRealtimeDo.class);
                        resultList1.add(pduHdaTotalRealtimeDo);
                    }else if ("hour".equals(reqVO.getGranularity()) ){
                        PduHdaLineHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaLineHourDo.class);
                        resultList1.add(pduHdaTotalHourDo);
                    }else {
                        PduHdaLineDayDo pduHdaTotalDayDo = JsonUtils.parseObject(str, PduHdaLineDayDo.class);
                        resultList1.add(pduHdaTotalDayDo);
                    }
                }
                // 匹配到的总记录数
                Long totalHits1 = hits1.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(resultList1)
                        .setTotal(totalHits1);
                break;

            case "loop":
                // 搜索请求对象
                SearchRequest searchRequest2 = new SearchRequest();
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest2.indices("pdu_hda_loop_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest2.indices("pdu_hda_loop_hour");
                }else {
                    searchRequest2.indices("pdu_hda_loop_day");
                }
                if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
                    searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(reqVO.getTimeRange()[0])
                            .to(reqVO.getTimeRange()[1]));
                }
                searchRequest2.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse2 = client.search(searchRequest2, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> resultList2 = new ArrayList<>();
                SearchHits hits2 = searchResponse2.getHits();
                for (SearchHit hit : hits2.getHits()) {
                    String str = hit.getSourceAsString();
                    if ("realtime".equals(reqVO.getGranularity()) ){
                        PduHdaLoopRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaLoopRealtimeDo.class);
                        resultList2.add(pduHdaTotalRealtimeDo);
                    }else if ("hour".equals(reqVO.getGranularity()) ){
                        PduHdaLoopHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaLoopHourDo.class);
                        resultList2.add(pduHdaTotalHourDo);
                    }else {
                        PduHdaLoopDayDo pduHdaTotalDayDo = JsonUtils.parseObject(str, PduHdaLoopDayDo.class);
                        resultList2.add(pduHdaTotalDayDo);
                    }
                }
                // 匹配到的总记录数
                Long totalHits2 = hits2.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(resultList2)
                        .setTotal(totalHits2);
                break;

            case "outlet":
                // 搜索请求对象
                SearchRequest searchRequest3 = new SearchRequest();
                if ("realtime".equals(reqVO.getGranularity()) ){
                    searchRequest3.indices("pdu_hda_outlet_realtime");
                }else if ("hour".equals(reqVO.getGranularity()) ){
                    searchRequest3.indices("pdu_hda_outlet_hour");
                }else {
                    searchRequest3.indices("pdu_hda_outlet_day");
                }
                if (reqVO.getTimeRange() != null && reqVO.getTimeRange().length != 0){
                    searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                            .from(reqVO.getTimeRange()[0])
                            .to(reqVO.getTimeRange()[1]));
                }
                searchRequest3.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse3 = client.search(searchRequest3, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> resultList3 = new ArrayList<>();
                SearchHits hits3 = searchResponse3.getHits();
                for (SearchHit hit : hits3.getHits()) {
                    String str = hit.getSourceAsString();
                    if ("realtime".equals(reqVO.getGranularity()) ){
                        PduHdaOutletRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaOutletRealtimeDo.class);
                        resultList3.add(pduHdaTotalRealtimeDo);
                    }else if ("hour".equals(reqVO.getGranularity()) ){
                        PduHdaOutletHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaOutletHourDo.class);
                        resultList3.add(pduHdaTotalHourDo);
                    }else {
                        PduHdaOutletDayDo pduHdaTotalDayDo = JsonUtils.parseObject(str, PduHdaOutletDayDo.class);
                        resultList3.add(pduHdaTotalDayDo);
                    }
                }
                // 匹配到的总记录数
                Long totalHits3 = hits3.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(resultList3)
                        .setTotal(totalHits3);
                break;

            default:

        }

        return pageResult;
    }

}