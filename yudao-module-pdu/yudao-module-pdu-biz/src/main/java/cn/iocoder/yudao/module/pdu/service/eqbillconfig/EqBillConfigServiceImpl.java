package cn.iocoder.yudao.module.pdu.service.eqbillconfig;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.eqbillconfig.EqBillConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.pdu.dal.mysql.eqbillconfig.EqBillConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pdu.enums.ErrorCodeConstants.*;

/**
 * pdu电量计费方式配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class EqBillConfigServiceImpl implements EqBillConfigService {

    @Resource
    private EqBillConfigMapper eqBillConfigMapper;

    @Override
    public Integer createEqBillConfig(EqBillConfigSaveReqVO createReqVO) {
        // 插入
        EqBillConfigDO eqBillConfig = BeanUtils.toBean(createReqVO, EqBillConfigDO.class);
        eqBillConfig.setCreateTime(LocalDateTime.now());
        eqBillConfigMapper.insert(eqBillConfig);
        // 返回
        return eqBillConfig.getId();
    }

    @Override
    public void updateEqBillConfig(EqBillConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateEqBillConfigExists(updateReqVO.getId());
        // 更新
        EqBillConfigDO updateObj = BeanUtils.toBean(updateReqVO, EqBillConfigDO.class);
        eqBillConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteEqBillConfig(Integer id) {
        // 校验存在
        validateEqBillConfigExists(id);
        // 删除
        eqBillConfigMapper.deleteById(id);
    }

    private void validateEqBillConfigExists(Integer id) {
        if (eqBillConfigMapper.selectById(id) == null) {
            throw exception(EQ_BILL_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public EqBillConfigDO getEqBillConfig(Integer id) {
        return eqBillConfigMapper.selectById(id);
    }

    @Override
    public PageResult<EqBillConfigDO> getEqBillConfigPage(EqBillConfigPageReqVO pageReqVO) {
        return eqBillConfigMapper.selectPage(pageReqVO);
    }

}