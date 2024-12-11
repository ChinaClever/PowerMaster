package cn.iocoder.yudao.module.bus.service.busstatisconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bus.controller.admin.busstatisconfig.vo.BusStatisConfigPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.busstatisconfig.vo.BusStatisConfigSaveReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busstatisconfig.BusStatisConfigDO;
import cn.iocoder.yudao.module.bus.dal.mysql.busstatisconfig.BusStatisConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.bus.enums.ErrorCodeConstants.STATIS_CONFIG_NOT_EXISTS;

/**
 * 母线计算服务配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class BusStatisConfigServiceImpl implements BusStatisConfigService {

    @Resource
    private BusStatisConfigMapper statisConfigMapper;

    @Override
    public Integer createStatisConfig(BusStatisConfigSaveReqVO createReqVO) {
        // 插入
        BusStatisConfigDO statisConfig = BeanUtils.toBean(createReqVO, BusStatisConfigDO.class);
        statisConfigMapper.insert(statisConfig);
        // 返回
        return statisConfig.getId();
    }

    @Override
    public void updateStatisConfig(BusStatisConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateStatisConfigExists(updateReqVO.getId());
        // 更新
        BusStatisConfigDO updateObj = BeanUtils.toBean(updateReqVO, BusStatisConfigDO.class);
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
    public BusStatisConfigDO getStatisConfig(Integer id) {
        return statisConfigMapper.selectById(id);
    }

    @Override
    public PageResult<BusStatisConfigDO> getStatisConfigPage(BusStatisConfigPageReqVO pageReqVO) {
        return statisConfigMapper.selectPage(pageReqVO);
    }

}