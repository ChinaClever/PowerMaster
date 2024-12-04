package cn.iocoder.yudao.module.pdu.service.eqbillconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig.vo.EqBillConfigPageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig.vo.EqBillConfigSaveReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.eqbillconfig.EqBillConfigDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.eqbillconfig.EqBillConfigMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pdu.enums.ErrorCodeConstants.EQ_BILL_CONFIG_NOT_EXISTS;

/**
 * pdu电量计费方式配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class EqBillConfigServiceImpl implements EqBillConfigService {


    @Value("${pdu-cal-refresh-url}")
    public String adder;

    @Resource
    private EqBillConfigMapper eqBillConfigMapper;

    @Override
    public Integer createEqBillConfig(EqBillConfigSaveReqVO createReqVO) {
        try {
            // 插入
            EqBillConfigDO eqBillConfig = BeanUtils.toBean(createReqVO, EqBillConfigDO.class);
            eqBillConfig.setCreateTime(LocalDateTime.now());
            eqBillConfigMapper.insert(eqBillConfig);
            // 返回
            return eqBillConfig.getId();
        } finally {
            HttpUtil.get(adder);
        }

    }

    @Override
    public void updateEqBillConfig(EqBillConfigSaveReqVO updateReqVO) {
        try {
            // 校验存在
            validateEqBillConfigExists(updateReqVO.getId());
            // 更新
            EqBillConfigDO updateObj = BeanUtils.toBean(updateReqVO, EqBillConfigDO.class);
            eqBillConfigMapper.updateById(updateObj);
        } finally {
            HttpUtil.get(adder);
        }

    }

    @Override
    public void deleteEqBillConfig(Integer id) {
        try {
            // 校验存在
            validateEqBillConfigExists(id);
            // 删除
            eqBillConfigMapper.deleteById(id);
        } finally {
            HttpUtil.get(adder);
        }

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