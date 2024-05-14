package cn.iocoder.yudao.module.pdu.service.historydata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EnergyConsumptionPageReqVO;

import java.io.IOException;

public interface EnergyConsumptionService {
    /**
     * 获得pdu电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;
}
