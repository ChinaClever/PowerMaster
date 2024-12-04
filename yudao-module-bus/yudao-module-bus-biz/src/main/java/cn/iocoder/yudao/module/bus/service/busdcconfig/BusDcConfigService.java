package cn.iocoder.yudao.module.bus.service.busdcconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.busdcconfig.vo.BusDcConfigPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.busdcconfig.vo.BusDcConfigSaveReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busdcconfig.BusDcConfigDO;

import javax.validation.Valid;

/**
 * 母线数据采集配置 Service 接口
 *
 * @author clever
 */
public interface BusDcConfigService {

    /**
     * 创建母线数据采集配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Short createDcConfig(@Valid BusDcConfigSaveReqVO createReqVO);

    /**
     * 更新母线数据采集配置
     *
     * @param updateReqVO 更新信息
     */
    void updateDcConfig(@Valid BusDcConfigSaveReqVO updateReqVO);

    /**
     * 删除母线数据采集配置
     *
     * @param id 编号
     */
    void deleteDcConfig(Short id);

    /**
     * 获得母线数据采集配置
     *
     * @param id 编号
     * @return 母线数据采集配置
     */
    BusDcConfigDO getDcConfig(Short id);

    /**
     * 获得母线数据采集配置分页
     *
     * @param pageReqVO 分页查询
     * @return 母线数据采集配置分页
     */
    PageResult<BusDcConfigDO> getDcConfigPage(BusDcConfigPageReqVO pageReqVO);

}