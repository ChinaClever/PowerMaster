package cn.iocoder.yudao.module.aisle.service.aislestatisconfig;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.aisle.controller.admin.aislestatisconfig.vo.*;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aislestatisconfig.AisleStatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 柜列计算服务配置 Service 接口
 *
 * @author clever
 */
public interface AisleStatisConfigService {

    /**
     * 创建柜列计算服务配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createStatisConfig(@Valid AisleStatisConfigSaveReqVO createReqVO);

    /**
     * 更新柜列计算服务配置
     *
     * @param updateReqVO 更新信息
     */
    void updateStatisConfig(@Valid AisleStatisConfigSaveReqVO updateReqVO);

    /**
     * 删除柜列计算服务配置
     *
     * @param id 编号
     */
    void deleteStatisConfig(Integer id);

    /**
     * 获得柜列计算服务配置
     *
     * @param id 编号
     * @return 柜列计算服务配置
     */
    AisleStatisConfigDO getStatisConfig(Integer id);

    /**
     * 获得柜列计算服务配置分页
     *
     * @param pageReqVO 分页查询
     * @return 柜列计算服务配置分页
     */
    PageResult<AisleStatisConfigDO> getStatisConfigPage(AisleStatisConfigPageReqVO pageReqVO);

}