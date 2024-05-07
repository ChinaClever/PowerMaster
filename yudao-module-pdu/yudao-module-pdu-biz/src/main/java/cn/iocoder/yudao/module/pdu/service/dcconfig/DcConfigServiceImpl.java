package cn.iocoder.yudao.module.pdu.service.dcconfig;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.dcconfig.DcConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.pdu.dal.mysql.dcconfig.DcConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.statis.enums.ErrorCodeConstants.*;

/**
 * pdu数据采集配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class DcConfigServiceImpl implements DcConfigService {

    @Resource
    private DcConfigMapper dcConfigMapper;

    @Override
    public Short createDcConfig(DcConfigSaveReqVO createReqVO) {
        // 插入
        DcConfigDO dcConfig = BeanUtils.toBean(createReqVO, DcConfigDO.class);
        dcConfigMapper.insert(dcConfig);
        // 返回
        return dcConfig.getId();
    }

    @Override
    public void updateDcConfig(DcConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateDcConfigExists(updateReqVO.getId());
        // 更新
        DcConfigDO updateObj = BeanUtils.toBean(updateReqVO, DcConfigDO.class);
        dcConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteDcConfig(Short id) {
        // 校验存在
        validateDcConfigExists(id);
        // 删除
        dcConfigMapper.deleteById(id);
    }

    private void validateDcConfigExists(Short id) {
        if (dcConfigMapper.selectById(id) == null) {
            throw exception(DC_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public DcConfigDO getDcConfig(Short id) {
        return dcConfigMapper.selectById(id);
    }

    @Override
    public PageResult<DcConfigDO> getDcConfigPage(DcConfigPageReqVO pageReqVO) {
        return dcConfigMapper.selectPage(pageReqVO);
    }

}