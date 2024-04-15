package cn.iocoder.yudao.module.pdu.service.pdudevice;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;

import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndex;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndexMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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


    @Override
    public PageResult<PDUDeviceDO> getPDUDevicePage(PDUDevicePageReqVO pageReqVO) {

        List<PduIndex> pduIndices = pDUDeviceMapper.selectList();
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

}