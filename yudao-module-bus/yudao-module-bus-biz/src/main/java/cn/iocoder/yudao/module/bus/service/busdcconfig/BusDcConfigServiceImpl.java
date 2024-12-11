package cn.iocoder.yudao.module.bus.service.busdcconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bus.controller.admin.busdcconfig.vo.BusDcConfigPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.busdcconfig.vo.BusDcConfigSaveReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busdcconfig.BusDcConfigDO;
import cn.iocoder.yudao.module.bus.dal.mysql.busdcconfig.BusDcConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.DC_CONFIG_NOT_EXISTS;

/**
 * 母线数据采集配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class BusDcConfigServiceImpl implements BusDcConfigService {

    @Resource
    private BusDcConfigMapper busDcConfigMapper;

    @Override
    public Short createDcConfig(BusDcConfigSaveReqVO createReqVO) {
        // 插入
        BusDcConfigDO dcConfig = BeanUtils.toBean(createReqVO, BusDcConfigDO.class);
        busDcConfigMapper.insert(dcConfig);
        // 返回
        return dcConfig.getId();
    }

    @Override
    public void updateDcConfig(BusDcConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateDcConfigExists(updateReqVO.getId());
        // 更新
        BusDcConfigDO updateObj = BeanUtils.toBean(updateReqVO, BusDcConfigDO.class);
        busDcConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteDcConfig(Short id) {
        // 校验存在
        validateDcConfigExists(id);
        // 删除
        busDcConfigMapper.deleteById(id);
    }

    private void validateDcConfigExists(Short id) {
        if (busDcConfigMapper.selectById(id) == null) {
            throw exception(DC_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public BusDcConfigDO getDcConfig(Short id) {
        return busDcConfigMapper.selectById(id);
    }

    @Override
    public PageResult<BusDcConfigDO> getDcConfigPage(BusDcConfigPageReqVO pageReqVO) {
        return busDcConfigMapper.selectPage(pageReqVO);
    }

}