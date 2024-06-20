package cn.iocoder.yudao.module.bus.service.busstatisconfig;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.bus.controller.admin.busstatisconfig.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busstatisconfig.BusStatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 母线计算服务配置 Service 接口
 *
 * @author clever
 */
public interface BusStatisConfigService {

    /**
     * 创建母线计算服务配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createStatisConfig(@Valid BusStatisConfigSaveReqVO createReqVO);

    /**
     * 更新母线计算服务配置
     *
     * @param updateReqVO 更新信息
     */
    void updateStatisConfig(@Valid BusStatisConfigSaveReqVO updateReqVO);

    /**
     * 删除母线计算服务配置
     *
     * @param id 编号
     */
    void deleteStatisConfig(Integer id);

    /**
     * 获得母线计算服务配置
     *
     * @param id 编号
     * @return 母线计算服务配置
     */
    BusStatisConfigDO getStatisConfig(Integer id);

    /**
     * 获得母线计算服务配置分页
     *
     * @param pageReqVO 分页查询
     * @return 母线计算服务配置分页
     */
    PageResult<BusStatisConfigDO> getStatisConfigPage(BusStatisConfigPageReqVO pageReqVO);

}