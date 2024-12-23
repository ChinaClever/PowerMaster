package cn.iocoder.yudao.module.pdu.service.mqconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.mqconfig.vo.MqConfigPageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.mqconfig.vo.MqConfigSaveReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.mqconfig.MqConfigDO;

import javax.validation.Valid;

/**
 * 消息队列系统配置 Service 接口
 *
 * @author 芋道源码
 */
public interface MqConfigService {

    /**
     * 创建消息队列系统配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createMqConfig(@Valid MqConfigSaveReqVO createReqVO);

    /**
     * 更新消息队列系统配置
     *
     * @param updateReqVO 更新信息
     */
    void updateMqConfig(@Valid MqConfigSaveReqVO updateReqVO);

    /**
     * 删除消息队列系统配置
     *
     * @param id 编号
     */
    void deleteMqConfig(Integer id);

    /**
     * 获得消息队列系统配置
     *
     * @param id 编号
     * @return 消息队列系统配置
     */
    MqConfigDO getMqConfig(Integer id);

    /**
     * 获得消息队列系统配置分页
     *
     * @param pageReqVO 分页查询
     * @return 消息队列系统配置分页
     */
    PageResult<MqConfigDO> getMqConfigPage(MqConfigPageReqVO pageReqVO);

}