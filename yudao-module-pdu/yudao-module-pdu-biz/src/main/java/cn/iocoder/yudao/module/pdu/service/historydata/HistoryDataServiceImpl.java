package cn.iocoder.yudao.module.pdu.service.historydata;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.HistoryDataPageReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.HistoryDataDO;
import cn.iocoder.yudao.module.pdu.repository.HdaRepository;
import org.apache.lucene.search.TotalHits;
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
    public PageResult<HistoryDataDO> getHistoryDataPage(HistoryDataPageReqVO pageReqVO) throws IOException {
        // 搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int page = pageReqVO.getPageNo(); // 页码
        int size = pageReqVO.getPageSize(); // 每页显示的条数
        int index = (page - 1) * size;
        searchSourceBuilder.from(index);
        searchSourceBuilder.size(size);
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        // 搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("pdu_hda_total_realtime");
        searchRequest.types("_doc");
        searchRequest.source(searchSourceBuilder);

        // 执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 搜索结果
        SearchHits hits = searchResponse.getHits();
        List<HistoryDataDO> historyDataDOList = new ArrayList<>();
        for (SearchHit hit : hits.getHits()) {
            String str = hit.getSourceAsString();
            System.out.println(str);
            HistoryDataDO historyDataDO = JsonUtils.parseObject(str, HistoryDataDO.class);
            System.out.println(historyDataDO);
            historyDataDOList.add(historyDataDO);
        }
        // 匹配到的总记录数
        Long totalHits = hits.getTotalHits().value;
        // 返回结果
        PageResult<HistoryDataDO> pageResult = new PageResult<>();
        pageResult.setList(historyDataDOList).setTotal(totalHits);
        return pageResult;
    }



}