package cn.iocoder.yudao.module.cabinet.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CabinetEnergyConsumptionPageReqVO extends PageParam {
    private Integer cabinetId;

    private String startTime;

    private String granularity;

    private String[] timeRange;

    private String[] cabinetIds;

}
