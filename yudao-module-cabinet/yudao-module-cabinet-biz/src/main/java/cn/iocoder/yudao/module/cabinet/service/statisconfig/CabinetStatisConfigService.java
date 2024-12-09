package cn.iocoder.yudao.module.cabinet.service.statisconfig;

import javax.validation.*;
import cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.statisconfig.CabinetStatisConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 机柜计算服务配置 Service 接口
 *
 * @author clever
 */
public interface CabinetStatisConfigService {

    /**
     * 创建机柜计算服务配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createStatisConfig(@Valid CabinetStatisConfigSaveReqVO createReqVO);

    /**
     * 更新机柜计算服务配置
     *
     * @param updateReqVO 更新信息
     */
    void updateStatisConfig(@Valid CabinetStatisConfigSaveReqVO updateReqVO);

    /**
     * 删除机柜计算服务配置
     *
     * @param id 编号
     */
    void deleteStatisConfig(Integer id);

    /**
     * 获得机柜计算服务配置
     *
     * @param id 编号
     * @return 机柜计算服务配置
     */
    CabinetStatisConfigDO getStatisConfig(Integer id);

    /**
     * 获得机柜计算服务配置分页
     *
     * @param pageReqVO 分页查询
     * @return 机柜计算服务配置分页
     */
    PageResult<CabinetStatisConfigDO> getStatisConfigPage(CabinetStatisConfigPageReqVO pageReqVO);

}