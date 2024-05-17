package cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyConsumptionPageReqVO extends PageParam {
    private Integer pduId;

    private Integer lineId;

    private Integer loopId;

    private Integer outletId;

    private String type;

    private String granularity;

    private String[] timeRange;
}
