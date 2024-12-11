package cn.iocoder.yudao.module.rack.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RackEnergyConsumptionPageReqVO extends PageParam {
    private Integer rackId;

    private String granularity;

    private String startTime;

    private String[] timeRange;

    private String[] rackIds;

    private String nowAddress;

}

