package cn.iocoder.yudao.module.aisle.service.aislestatisconfig;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.aisle.controller.admin.aislestatisconfig.vo.*;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aislestatisconfig.AisleStatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.aisle.dal.mysql.aislestatisconfig.AisleStatisConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.aisle.enums.ErrorCodeConstants.*;

/**
 * 柜列计算服务配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class AisleStatisConfigServiceImpl implements AisleStatisConfigService {

    @Resource
    private AisleStatisConfigMapper statisConfigMapper;

    @Override
    public Integer createStatisConfig(AisleStatisConfigSaveReqVO createReqVO) {
        // 插入
        AisleStatisConfigDO statisConfig = BeanUtils.toBean(createReqVO, AisleStatisConfigDO.class);
        statisConfigMapper.insert(statisConfig);
        // 返回
        return statisConfig.getId();
    }

    @Override
    public void updateStatisConfig(AisleStatisConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateStatisConfigExists(updateReqVO.getId());
        // 更新
        AisleStatisConfigDO updateObj = BeanUtils.toBean(updateReqVO, AisleStatisConfigDO.class);
        statisConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteStatisConfig(Integer id) {
        // 校验存在
        validateStatisConfigExists(id);
        // 删除
        statisConfigMapper.deleteById(id);
    }

    private void validateStatisConfigExists(Integer id) {
        if (statisConfigMapper.selectById(id) == null) {
            throw exception(STATIS_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public AisleStatisConfigDO getStatisConfig(Integer id) {
        return statisConfigMapper.selectById(id);
    }

    @Override
    public PageResult<AisleStatisConfigDO> getStatisConfigPage(AisleStatisConfigPageReqVO pageReqVO) {
        return statisConfigMapper.selectPage(pageReqVO);
    }

}