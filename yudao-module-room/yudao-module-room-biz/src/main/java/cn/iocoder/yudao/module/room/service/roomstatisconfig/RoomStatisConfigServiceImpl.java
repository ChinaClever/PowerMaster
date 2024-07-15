package cn.iocoder.yudao.module.room.service.roomstatisconfig;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import cn.iocoder.yudao.module.room.controller.admin.roomstatisconfig.vo.*;
import cn.iocoder.yudao.module.room.dal.dataobject.roomstatisconfig.RoomStatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.room.dal.mysql.roomstatisconfig.RoomStatisConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.room.enums.ErrorCodeConstants.*;

/**
 * 机房计算服务配置 Service 实现类
 *
 * @author clever
 */
@Service
@Validated
public class RoomStatisConfigServiceImpl implements RoomStatisConfigService {

    @Resource
    private RoomStatisConfigMapper statisConfigMapper;

    @Override
    public Integer createStatisConfig(RoomStatisConfigSaveReqVO createReqVO) {
        // 插入
        RoomStatisConfigDO statisConfig = BeanUtils.toBean(createReqVO, RoomStatisConfigDO.class);
        statisConfigMapper.insert(statisConfig);
        // 返回
        return statisConfig.getId();
    }

    @Override
    public void updateStatisConfig(RoomStatisConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateStatisConfigExists(updateReqVO.getId());
        // 更新
        RoomStatisConfigDO updateObj = BeanUtils.toBean(updateReqVO, RoomStatisConfigDO.class);
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
    public RoomStatisConfigDO getStatisConfig(Integer id) {
        return statisConfigMapper.selectById(id);
    }

    @Override
    public PageResult<RoomStatisConfigDO> getStatisConfigPage(RoomStatisConfigPageReqVO pageReqVO) {
        return statisConfigMapper.selectPage(pageReqVO);
    }

}