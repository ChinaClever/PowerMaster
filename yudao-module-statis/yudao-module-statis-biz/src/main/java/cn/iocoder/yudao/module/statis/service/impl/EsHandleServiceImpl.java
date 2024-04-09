package cn.iocoder.yudao.module.statis.service.impl;

import cn.iocoder.yudao.module.statis.service.EsHandleService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: chenwany
 * @Date: 2024/3/29 13:58
 * @Description: ES操作类
 */
@Slf4j
@Service
public class EsHandleServiceImpl implements EsHandleService {


    @Value("${max-deal-count}")
    private int perMaxDealCount;

    @Autowired
    private RestHighLevelClient client;

    @Override
    public <T> void batchInsert(List<T> list, String indexName) {
        //开始时间
        long startTime = System.currentTimeMillis();
        if (CollectionUtils.isEmpty(list)) {
            log.info("数据为空........");
            return;
        }
        try {
            //分批插入数据大小
            //计数
            int index = 1;
            //分页
            int wholePage = list.size() / perMaxDealCount;
            //需要插入的次数
            int times = list.size() % perMaxDealCount == 0 ? (wholePage) : (wholePage + 1);

            do {
                //分批插入的数据
                List<T> pageDealDataList;
                //数据开始坐标
                int startIndex = (index - 1) * perMaxDealCount;
                //此处需要注意 list.subList(int fromIndex, int toIndex)  返回list中指定下标的元素组成的list集合,左闭右开
                int endIndex = index * perMaxDealCount;

                BulkRequest request = new BulkRequest();
                if (index == times) {
                    pageDealDataList = list.subList(startIndex, list.size());
                    log.info("general: " + pageDealDataList.size());
                    pageDealDataList.forEach(t -> {
                        IndexRequest indexRequest = new IndexRequest(indexName);

                        indexRequest.source(t, XContentType.JSON);
                        request.add(indexRequest);
                    });
                } else {
                    pageDealDataList = list.subList(startIndex, endIndex);
                    log.info("general: " + pageDealDataList.size());
                    pageDealDataList.forEach(t -> {
                        IndexRequest indexRequest = new IndexRequest(indexName);
                        indexRequest.source(t,XContentType.JSON);
                        request.add(indexRequest);
                    });
                }
                index++;

                // 执行批量插入
                BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);

                // 检查批量响应的结果
                if (bulkResponse.hasFailures()) {
                    // 处理失败的请求
                    log.error("Batch insert failed..." + bulkResponse.buildFailureMessage());
                } else {
                    log.info("Batch insert success...");
                }
            }
            while (index <= times);
        } catch (Exception e) {
            log.error("插入失败：", e);
        }
        long endTime = System.currentTimeMillis();
        log.info("插入时长：{}", endTime - startTime);
    }
}
