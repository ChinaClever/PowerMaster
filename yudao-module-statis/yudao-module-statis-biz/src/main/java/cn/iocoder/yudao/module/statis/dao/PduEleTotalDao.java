package cn.iocoder.yudao.module.statis.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.PduEqBaseDo;
import cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total.PduEleTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.enums.EsIndexEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
<<<<<<< Updated upstream
import cn.iocoder.yudao.module.statis.entity.ele.PduEleTotalRealtimeDo;
import cn.iocoder.yudao.module.statis.entity.ele.PduEqBaseDo;
=======
>>>>>>> Stashed changes
import cn.iocoder.yudao.module.statis.vo.EqBillConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static cn.iocoder.yudao.framework.common.constant.FieldConstant.*;

/**
 * @Author: chenwany
 * @Date: 2024/4/9 09:52
 * @Description:  总电量计算
 */
@Slf4j
@Component
public class PduEleTotalDao {

    @Autowired
    RestHighLevelClient client;

    /**
     *  电费计算 (按天统计)
     * @param billConfigVoList 电费计算配置
     */
    public  List<PduEqBaseDo> statisEleDay(List<EqBillConfigVo> billConfigVoList){
        List<PduEqBaseDo> list = new ArrayList<>();

        try {
            billConfigVoList.forEach(billConfigVo -> {
                //获取时间段内第一条和最后一条数据
                Map<Integer, PduEleTotalRealtimeDo> endMap = getEleData(billConfigVo,
                        SortOrder.DESC,
                        EsIndexEnum.PDU_ELE_TOTAL_REALTIME.getIndex());
                Map<Integer, PduEleTotalRealtimeDo> startMap = getEleData(billConfigVo,
                        SortOrder.ASC,
                        EsIndexEnum.PDU_ELE_TOTAL_REALTIME.getIndex());
                endMap.keySet().forEach(pduId ->{
                    PduEqBaseDo dayDo = new PduEqBaseDo();
                    //统计时间段
                    dayDo.setStartTime(DateUtil.parseDateTime(billConfigVo.getStartTime()));
                    dayDo.setEndTime(DateUtil.parseDateTime(billConfigVo.getEndTime()));
                    dayDo.setPduId(pduId);

                    PduEleTotalRealtimeDo endRealtimeDo = endMap.get(pduId);
                    PduEleTotalRealtimeDo startRealtimeDo = startMap.get(pduId);
                    //结束时间电量
                    double endEle = endRealtimeDo.getEle();
                    dayDo.setEndEle(endEle);
                    //开始时间电量
                    double startEle = startRealtimeDo.getEle();
                    dayDo.setStartEle(startEle);
                    //判断使用电量  开始电量大于结束电量，电量有清零操作，以结束电量为准
                    double eq ;
                    if (startEle>endEle){
                        eq = endEle;
                    }else {
                        eq = endEle - startEle;
                    }
                    dayDo.setEq(eq);
                    //电费计算 电量*该时间段计费
                    double bill = billConfigVo.getBill() * eq;
                    dayDo.setBill(bill);
                    dayDo.setCreateTime(DateTime.now());

                    list.add(dayDo);
                });

            });

            return list;
        }catch (Exception e){
            log.error("计算异常：",e);
        }
        return list;
    }
    

    /**
     * @description:  获取ES中数据
      * @param configVo 时间段配置
     * @param sortOrder 排序
     * @param index  索引名称
     * @return Map<Integer,PduEleTotalRealtimeDo>
     * @author luowei
     * @date: 2024/4/10 10:46
     */
    private Map<Integer, PduEleTotalRealtimeDo> getEleData(EqBillConfigVo configVo,SortOrder sortOrder,String index){
        Map<Integer, PduEleTotalRealtimeDo> dataMap = new HashMap<>();
        try {
            // 创建SearchRequest对象, 设置查询索引名
            SearchRequest searchRequest = new SearchRequest(index);
            // 通过QueryBuilders构建ES查询条件，
            SearchSourceBuilder builder = new SearchSourceBuilder();
            //获取需要处理的数据
            builder.query(QueryBuilders.rangeQuery(CREATE_TIME + ".keyword")
                    .gte(configVo.getStartTime())
                    .lt(configVo.getEndTime()));

            // 创建terms桶聚合，聚合名字=by_pdu, 字段=pdu_id，根据pdu_id分组
            TermsAggregationBuilder pduAggregationBuilder = AggregationBuilders.terms("by_pdu")
                    .field("pdu_id");
            // 嵌套聚合
            // 设置聚合查询
            String top = "top";
            AggregationBuilder topAgg = AggregationBuilders.topHits(top)
                    .size(1).sort(CREATE_TIME + ".keyword", sortOrder);

            builder.aggregation(pduAggregationBuilder.subAggregation(topAgg));

            // 设置搜索条件
            searchRequest.source(builder);
            // 如果只想返回聚合统计结果，不想返回查询结果可以将分页大小设置为0
//            builder.size(0);

            // 执行ES请求
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 处理聚合查询结果
            Aggregations aggregations = searchResponse.getAggregations();
            // 根据by_pdu名字查询terms聚合结果
            Terms byPduAggregation = aggregations.get("by_pdu");

            for (Terms.Bucket bucket : byPduAggregation.getBuckets()){
                TopHits tophits = bucket.getAggregations().get(top);
                SearchHits tophitsHits = tophits.getHits();
                SearchHit hit = tophitsHits.getHits()[0];
                PduEleTotalRealtimeDo realtimeDo = JsonUtils.parseObject(hit.getSourceAsString(), PduEleTotalRealtimeDo.class);
                dataMap.put(Integer.parseInt(String.valueOf(bucket.getKey())),realtimeDo);
            }
            return dataMap;
        }catch (Exception e){
            log.error("获取数据异常：",e);
        }
       return dataMap;
    }
}
