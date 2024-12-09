package cn.iocoder.yudao.module.rack.service.rackstatisconfig;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.rack.controller.admin.rackstatisconfig.vo.*;
import cn.iocoder.yudao.module.rack.dal.dataobject.rackstatisconfig.RackStatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.rack.dal.mysql.rackstatisconfig.RackStatisConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.rack.enums.ErrorCodeConstants.*;

/**
 * 机架计算服务配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class RackStatisConfigServiceImpl implements RackStatisConfigService {

    @Resource
    private RackStatisConfigMapper statisConfigMapper;

    @Override
    public Integer createStatisConfig(RackStatisConfigSaveReqVO createReqVO) {
        // 插入
        RackStatisConfigDO statisConfig = BeanUtils.toBean(createReqVO, RackStatisConfigDO.class);
        statisConfigMapper.insert(statisConfig);
        // 返回
        return statisConfig.getId();
    }

    @Override
    public void updateStatisConfig(RackStatisConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateStatisConfigExists(updateReqVO.getId());
        // 更新
        RackStatisConfigDO updateObj = BeanUtils.toBean(updateReqVO, RackStatisConfigDO.class);
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
    public RackStatisConfigDO getStatisConfig(Integer id) {
        return statisConfigMapper.selectById(id);
    }

    @Override
    public PageResult<RackStatisConfigDO> getStatisConfigPage(RackStatisConfigPageReqVO pageReqVO) {
        return statisConfigMapper.selectPage(pageReqVO);
    }

}