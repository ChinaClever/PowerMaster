package cn.iocoder.yudao.module.pdu.service.historydata;

import cn.iocoder.yudao.framework.common.entity.es.pdu.line.PduHdaLineRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.loop.PduHdaLoopRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.outlet.PduHdaOutletRealtimeDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalDayDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalHourDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataPageReqVO;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class HistoryDataServiceImpl implements HistoryDataService {

//    @Autowired
//    private HdaRepository hdaRepository;
    @Autowired
    private RestHighLevelClient client;

//    private HdaRepository hdaRepository = null;
//
//    @Autowired
//    public HistoryDataServiceImpl(HdaRepository historyDataRepository) {
//        this.hdaRepository = hdaRepository;
//    }


    @Override
    public PageResult<Object> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int page = pageReqVO.getPageNo(); // 页码
        int size = pageReqVO.getPageSize(); // 每页显示的条数
        int index = (page - 1) * size;
        searchSourceBuilder.from(index);
        searchSourceBuilder.size(size);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        PageResult<Object> pageResult = null;
        switch (pageReqVO.getType()) {
            case "total":
                // 搜索请求对象
                SearchRequest searchRequest = new SearchRequest();
                if ("realtime".equals(pageReqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_total_realtime");
                }else if ("hour".equals(pageReqVO.getGranularity()) ){
                    searchRequest.indices("pdu_hda_total_hour");
                }else {
                    searchRequest.indices("pdu_hda_total_day");
                }
                searchRequest.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> resultList = new ArrayList<>();
                SearchHits hits = searchResponse.getHits();
                for (SearchHit hit : hits.getHits()) {
                    String str = hit.getSourceAsString();
                    if ("realtime".equals(pageReqVO.getGranularity()) ){
                        PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                        resultList.add(pduHdaTotalRealtimeDo);
                    }else if ("hour".equals(pageReqVO.getGranularity()) ){
                        PduHdaTotalHourDo pduHdaTotalHourDo = JsonUtils.parseObject(str, PduHdaTotalHourDo.class);
                        resultList.add(pduHdaTotalHourDo);
                    }else {
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
                searchRequest1.indices("pdu_hda_line_realtime");
                searchRequest1.types("_doc");
                searchRequest1.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse1 = client.search(searchRequest1, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> pduHdaLineRealtimeDOList = new ArrayList<>();
                SearchHits hits1 = searchResponse1.getHits();
                for (SearchHit hit : hits1.getHits()) {
                    String str = hit.getSourceAsString();
                    PduHdaLineRealtimeDo pduHdaLineRealtimeDO = JsonUtils.parseObject(str, PduHdaLineRealtimeDo.class);
                    pduHdaLineRealtimeDOList.add(pduHdaLineRealtimeDO);
                }
                // 匹配到的总记录数
                Long totalHits1 = hits1.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(pduHdaLineRealtimeDOList)
                        .setTotal(totalHits1);
                break;

            case "loop":
                // 搜索请求对象
                SearchRequest searchRequest2 = new SearchRequest();
                searchRequest2.indices("pdu_hda_loop_realtime");
                searchRequest2.types("_doc");
                searchRequest2.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse2 = client.search(searchRequest2, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> pduHdaLoopRealtimeDOList = new ArrayList<>();
                SearchHits hits2 = searchResponse2.getHits();
                for (SearchHit hit : hits2.getHits()) {
                    String str = hit.getSourceAsString();
                    PduHdaLoopRealtimeDo pduHdaLoopRealtimeDo = JsonUtils.parseObject(str, PduHdaLoopRealtimeDo.class);
                    pduHdaLoopRealtimeDOList.add(pduHdaLoopRealtimeDo);
                }
                // 匹配到的总记录数
                Long totalHits2 = hits2.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(pduHdaLoopRealtimeDOList)
                        .setTotal(totalHits2);
                break;

            case "outlet":
                // 搜索请求对象
                SearchRequest searchRequest3 = new SearchRequest();
                searchRequest3.indices("pdu_hda_outlet_realtime");
                searchRequest3.types("_doc");
                searchRequest3.source(searchSourceBuilder);
                // 执行搜索,向ES发起http请求
                SearchResponse searchResponse3 = client.search(searchRequest3, RequestOptions.DEFAULT);
                // 搜索结果
                List<Object> pduHdaOutletRealtimeDOList = new ArrayList<>();
                SearchHits hits3 = searchResponse3.getHits();
                for (SearchHit hit : hits3.getHits()) {
                    String str = hit.getSourceAsString();
                    PduHdaOutletRealtimeDo pduHdaOutletRealtimeDo = JsonUtils.parseObject(str, PduHdaOutletRealtimeDo.class);
                    pduHdaOutletRealtimeDOList.add(pduHdaOutletRealtimeDo);
                }
                // 匹配到的总记录数
                Long totalHits3 = hits3.getTotalHits().value;
                // 返回的结果
                pageResult = new PageResult<>();
                pageResult.setList(pduHdaOutletRealtimeDOList)
                        .setTotal(totalHits3);
                break;

        }

        return pageResult;
    }



}