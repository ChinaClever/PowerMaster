package cn.iocoder.yudao.module.pdu.service.dcconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo.DcConfigPageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo.DcConfigSaveReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.dcconfig.DcConfigDO;

import javax.validation.Valid;

/**
 * pdu数据采集配置 Service 接口
 *
 * @author clever
 */
public interface PDUDcConfigService {

    /**
     * 创建pdu数据采集配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Short createDcConfig(@Valid DcConfigSaveReqVO createReqVO);

    /**
     * 更新pdu数据采集配置
     *
     * @param updateReqVO 更新信息
     */
    void updateDcConfig(@Valid DcConfigSaveReqVO updateReqVO);

    /**
     * 删除pdu数据采集配置
     *
     * @param id 编号
     */
    void deleteDcConfig(Short id);

    /**
     * 获得pdu数据采集配置
     *
     * @param id 编号
     * @return pdu数据采集配置
     */
    DcConfigDO getDcConfig(Short id);

    /**
     * 获得pdu数据采集配置分页
     *
     * @param pageReqVO 分页查询
     * @return pdu数据采集配置分页
     */
    PageResult<DcConfigDO> getDcConfigPage(DcConfigPageReqVO pageReqVO);

}