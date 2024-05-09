package cn.iocoder.yudao.module.cabinet.service.statisconfig;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.statisconfig.StatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.cabinet.dal.mysql.statisconfig.StatisConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.cabinet.enums.ErrorCodeConstants.*;

/**
 * 机柜计算服务配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class StatisConfigServiceImpl implements StatisConfigService {

    @Resource
    private StatisConfigMapper statisConfigMapper;

    @Override
    public Integer createStatisConfig(StatisConfigSaveReqVO createReqVO) {
        // 插入
        StatisConfigDO statisConfig = BeanUtils.toBean(createReqVO, StatisConfigDO.class);
        statisConfigMapper.insert(statisConfig);
        // 返回
        return statisConfig.getId();
    }

    @Override
    public void updateStatisConfig(StatisConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateStatisConfigExists(updateReqVO.getId());
        // 更新
        StatisConfigDO updateObj = BeanUtils.toBean(updateReqVO, StatisConfigDO.class);
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
    public StatisConfigDO getStatisConfig(Integer id) {
        return statisConfigMapper.selectById(id);
    }

    @Override
    public PageResult<StatisConfigDO> getStatisConfigPage(StatisConfigPageReqVO pageReqVO) {
        return statisConfigMapper.selectPage(pageReqVO);
    }

}