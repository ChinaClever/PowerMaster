package cn.iocoder.yudao.module.rack.service.rackstatisconfig;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.rack.controller.admin.rackstatisconfig.vo.*;
import cn.iocoder.yudao.module.rack.dal.dataobject.rackstatisconfig.RackStatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 机架计算服务配置 Service 接口
 *
 * @author clever
 */
public interface RackStatisConfigService {

    /**
     * 创建机架计算服务配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createStatisConfig(@Valid RackStatisConfigSaveReqVO createReqVO);

    /**
     * 更新机架计算服务配置
     *
     * @param updateReqVO 更新信息
     */
    void updateStatisConfig(@Valid RackStatisConfigSaveReqVO updateReqVO);

    /**
     * 删除机架计算服务配置
     *
     * @param id 编号
     */
    void deleteStatisConfig(Integer id);

    /**
     * 获得机架计算服务配置
     *
     * @param id 编号
     * @return 机架计算服务配置
     */
    RackStatisConfigDO getStatisConfig(Integer id);

    /**
     * 获得机架计算服务配置分页
     *
     * @param pageReqVO 分页查询
     * @return 机架计算服务配置分页
     */
    PageResult<RackStatisConfigDO> getStatisConfigPage(RackStatisConfigPageReqVO pageReqVO);

}