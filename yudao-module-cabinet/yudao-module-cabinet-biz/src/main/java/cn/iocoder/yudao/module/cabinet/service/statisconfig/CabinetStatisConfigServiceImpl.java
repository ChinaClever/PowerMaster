package cn.iocoder.yudao.module.cabinet.service.statisconfig;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.statisconfig.CabinetStatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.cabinet.dal.mysql.statisconfig.CabinetStatisConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.cabinet.enums.ErrorCodeConstants.*;

/**
 * 机柜计算服务配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class CabinetStatisConfigServiceImpl implements CabinetStatisConfigService {

    @Resource
    private CabinetStatisConfigMapper cabinetStatisConfigMapper;

    @Override
    public Integer createStatisConfig(CabinetStatisConfigSaveReqVO createReqVO) {
        // 插入
        CabinetStatisConfigDO statisConfig = BeanUtils.toBean(createReqVO, CabinetStatisConfigDO.class);
        cabinetStatisConfigMapper.insert(statisConfig);
        // 返回
        return statisConfig.getId();
    }

    @Override
    public void updateStatisConfig(CabinetStatisConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateStatisConfigExists(updateReqVO.getId());
        // 更新
        CabinetStatisConfigDO updateObj = BeanUtils.toBean(updateReqVO, CabinetStatisConfigDO.class);
        cabinetStatisConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteStatisConfig(Integer id) {
        // 校验存在
        validateStatisConfigExists(id);
        // 删除
        cabinetStatisConfigMapper.deleteById(id);
    }

    private void validateStatisConfigExists(Integer id) {
        if (cabinetStatisConfigMapper.selectById(id) == null) {
            throw exception(STATIS_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public CabinetStatisConfigDO getStatisConfig(Integer id) {
        return cabinetStatisConfigMapper.selectById(id);
    }

    @Override
    public PageResult<CabinetStatisConfigDO> getStatisConfigPage(CabinetStatisConfigPageReqVO pageReqVO) {
        return cabinetStatisConfigMapper.selectPage(pageReqVO);
    }

}