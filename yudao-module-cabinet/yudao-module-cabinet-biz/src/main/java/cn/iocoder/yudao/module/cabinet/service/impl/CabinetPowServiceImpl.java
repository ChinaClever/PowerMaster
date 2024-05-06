package cn.iocoder.yudao.module.cabinet.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowHourDo;
import cn.iocoder.yudao.framework.common.entity.es.cabinet.pow.CabinetPowRealtimeDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.cabinet.dto.CabinetPowDTO;
import cn.iocoder.yudao.module.cabinet.service.CabinetPowService;
import cn.iocoder.yudao.module.cabinet.vo.CabinetPowVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.CREATE_TIME;
import static cn.iocoder.yudao.module.cabinet.constant.CabConstants.*;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/4/30 11:21
 */
@Slf4j
@Service
public class CabinetPowServiceImpl implements CabinetPowService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public List<CabinetPowDTO> getPowList(CabinetPowVo vo) {
        List<CabinetPowDTO> list = new ArrayList<>();
        try {
            //近一个小时
            if (vo.getType().equals(HOUR)){
                DateTime end = DateTime.now();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, -1);
                DateTime start = DateTime.of(calendar.getTime());

                String startTime = DateUtil.formatDateTime(start);
                String endTime = DateUtil.formatDateTime(end);

                List<String> data = getData(startTime,endTime,vo,CABINET_HDA_POW_REALTIME);
                data.forEach(str -> {
                    CabinetPowRealtimeDo cabinetPowRealtimeDo = JsonUtils.parseObject(str, CabinetPowRealtimeDo.class);
                    CabinetPowDTO dto = new CabinetPowDTO();
                    dto.setActivePow(cabinetPowRealtimeDo.getActiveTotal());
                    dto.setApparentPow(cabinetPowRealtimeDo.getApparentTotal());
                    dto.setDateTime(cabinetPowRealtimeDo.getCreateTime());
                    list.add(dto);
                });
            //近24小时
            }else if(vo.getType().equals(DAY)){
                DateTime end = DateTime.now();
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR_OF_DAY, -24);
                DateTime start = DateTime.of(calendar.getTime());

                String startTime = DateUtil.formatDateTime(start);
                String endTime = DateUtil.formatDateTime(end);

                List<String> data = getData(startTime,endTime,vo,CABINET_HDA_POW_HOUR);
                data.forEach(str -> {
                    CabinetPowHourDo hourDo = JsonUtils.parseObject(str, CabinetPowHourDo.class);
                    CabinetPowDTO dto = new CabinetPowDTO();
                    dto.setActivePow(hourDo.getActiveTotalAvgValue());
                    dto.setApparentPow(hourDo.getApparentTotalAvgValue());
                    dto.setDateTime(hourDo.getCreateTime());
                    list.add(dto);
                });

            }

            return list;
        }catch (Exception e){
            log.error("获取数据异常： ",e);
        }

        return list;

    }


    /**
     * 获取es数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param powVo  请求参数
     * @param index  索引表
     * @return
     * @throws IOException
     */
    private List<String> getData(String startTime, String endTime,CabinetPowVo powVo, String index) throws IOException {
        // 创建SearchRequest对象, 设置查询索引名
        SearchRequest searchRequest = new SearchRequest(index);
        // 通过QueryBuilders构建ES查询条件，
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //获取需要处理的数据
        builder.query(QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery(CREATE_TIME + KEYWORD).gte(startTime).lt(endTime))
                .must(QueryBuilders.termQuery(CABINET_ID,powVo.getId())));
        builder.sort(CREATE_TIME+ KEYWORD,SortOrder.ASC);
        // 设置搜索条件
        searchRequest.source(builder);
        builder.size(1000);

        List<String> list = new ArrayList<>();
        // 执行ES请求
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        if(searchResponse != null){
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                list.add(str);
            }
        }
        return list;

    }
}
