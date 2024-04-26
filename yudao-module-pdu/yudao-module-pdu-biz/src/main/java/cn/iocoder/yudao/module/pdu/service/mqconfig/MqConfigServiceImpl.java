package cn.iocoder.yudao.module.pdu.service.mqconfig;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.pdu.controller.admin.mqconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.mqconfig.MqConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.pdu.dal.mysql.mqconfig.MqConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;


/**
 * 消息队列系统配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MqConfigServiceImpl implements MqConfigService {

    @Resource
    private MqConfigMapper mqConfigMapper;

    @Override
    public Integer createMqConfig(MqConfigSaveReqVO createReqVO) {
        // 插入
        MqConfigDO mqConfig = BeanUtils.toBean(createReqVO, MqConfigDO.class);
        mqConfigMapper.insert(mqConfig);
        // 返回
        return mqConfig.getId();
    }

    @Override
    public void updateMqConfig(MqConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateMqConfigExists(updateReqVO.getId());
        // 更新
        MqConfigDO updateObj = BeanUtils.toBean(updateReqVO, MqConfigDO.class);
        mqConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteMqConfig(Integer id) {
        // 校验存在
        validateMqConfigExists(id);
        // 删除
        mqConfigMapper.deleteById(id);
    }

    private void validateMqConfigExists(Integer id) {
        if (mqConfigMapper.selectById(id) == null) {
            throw new RuntimeException("该记录不存在");
        }
    }

    @Override
    public MqConfigDO getMqConfig(Integer id) {
        return mqConfigMapper.selectById(id);
    }

    @Override
    public PageResult<MqConfigDO> getMqConfigPage(MqConfigPageReqVO pageReqVO) {
        return mqConfigMapper.selectPage(pageReqVO);
    }

}