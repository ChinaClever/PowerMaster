package cn.iocoder.yudao.module.pdu.service.eqbillconfig;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.eqbillconfig.EqBillConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * pdu电量计费方式配置 Service 接口
 *
 * @author clever
 */
public interface EqBillConfigService {

    /**
     * 创建pdu电量计费方式配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createEqBillConfig(@Valid EqBillConfigSaveReqVO createReqVO);

    /**
     * 更新pdu电量计费方式配置
     *
     * @param updateReqVO 更新信息
     */
    void updateEqBillConfig(@Valid EqBillConfigSaveReqVO updateReqVO);

    /**
     * 删除pdu电量计费方式配置
     *
     * @param id 编号
     */
    void deleteEqBillConfig(Integer id);

    /**
     * 获得pdu电量计费方式配置
     *
     * @param id 编号
     * @return pdu电量计费方式配置
     */
    EqBillConfigDO getEqBillConfig(Integer id);

    /**
     * 获得pdu电量计费方式配置分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量计费方式配置分页
     */
    PageResult<EqBillConfigDO> getEqBillConfigPage(EqBillConfigPageReqVO pageReqVO);

}