package cn.iocoder.yudao.module.pdu.service.pdudevice;

import cn.iocoder.yudao.framework.common.entity.es.pdu.total.PduHdaTotalRealtimeDo;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;

import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndex;

import cn.hutool.core.date.DateTime;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSONObject;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndexMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.statis.enums.ErrorCodeConstants.*;

/**
 * PDU设备 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PDUDeviceServiceImpl implements PDUDeviceService {

    @Resource
    private PduIndexMapper pDUDeviceMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient client;


    @Override
    public PageResult<PDUDeviceDO> getPDUDevicePage(PDUDevicePageReqVO pageReqVO) {

        PageResult<PduIndex> pduIndexPageResult = pDUDeviceMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<>());

        List<PduIndex> pduIndices = pduIndexPageResult.getList();;
        ValueOperations ops = redisTemplate.opsForValue();
        List<PDUDeviceDO> result = new ArrayList<>();
        long i = 0;
        for (PduIndex pduIndex : pduIndices) {
            i++;
            JSONObject jsonObject = (JSONObject) ops.get("packet:pdu:" + pduIndex.getDevKey());
            if (jsonObject == null){
                continue;
            }
            JSONObject pduTgData = jsonObject.getJSONObject("pdu_data").getJSONObject("pdu_tg_data");
            PDUDeviceDO pduDeviceDO = new PDUDeviceDO();
            pduDeviceDO.setId(pduIndex.getId());
            pduDeviceDO.setPf(pduTgData.getDoubleValue("pf"));
            pduDeviceDO.setDevKey(pduIndex.getDevKey());
            pduDeviceDO.setEle(pduTgData.getDoubleValue("ele"));
            pduDeviceDO.setPow(pduTgData.getDoubleValue("pow"));
            pduDeviceDO.setApparentPow(pduTgData.getDoubleValue("apparent_pow"));
            pduDeviceDO.setReactivePow(pduTgData.getDoubleValue("reactive_pow"));
            pduDeviceDO.setDataUpdateTime(jsonObject.getString("sys_time"));
            result.add(pduDeviceDO);
        }
        return new PageResult<PDUDeviceDO>(result,i);
    }

    @Override
    public String getDisplayDataByDevKey(String devKey) {
        if (StringUtils.isEmpty(devKey)){
            return null;
        }else {
            ValueOperations ops = redisTemplate.opsForValue();
            JSONObject jsonObject = (JSONObject) ops.get("packet:pdu:" + devKey);
            return jsonObject.toJSONString();
        }
    }

    @Override
    public Map getHistoryDataByPduId(Long id) {
        HashMap result = new HashMap<>();


        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        // 构建查询请求
        SearchRequest searchRequest = new SearchRequest("pdu_hda_total_realtime");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        searchSourceBuilder.query(QueryBuilders.termQuery("pdu_id", id));
        searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("create_time.keyword")
                .from(formatter.format(oneHourAgo))
                .to(formatter.format(now)));
        searchSourceBuilder.sort("create_time.keyword", SortOrder.ASC);
        searchSourceBuilder.size(1000); // 设置返回的最大结果数



        searchRequest.source(searchSourceBuilder);
        List<Double> apparentList = new ArrayList<>();
        List<Double> activeList = new ArrayList<>();
        List<String> dateTimes = new ArrayList<>();
        // 执行查询请求
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            for (SearchHit hit : hits) {
                String str = hit.getSourceAsString();
                PduHdaTotalRealtimeDo pduHdaTotalRealtimeDo = JsonUtils.parseObject(str, PduHdaTotalRealtimeDo.class);
                apparentList.add(Double.valueOf(pduHdaTotalRealtimeDo.getApparentPow()));
                activeList.add(Double.valueOf(pduHdaTotalRealtimeDo.getActivePow()));
                dateTimes.add(pduHdaTotalRealtimeDo.getCreateTime().toString("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.put("apparentList",apparentList);
        result.put("activeList",activeList);
        result.put("dateTimes",dateTimes);

        return result;
    }

}