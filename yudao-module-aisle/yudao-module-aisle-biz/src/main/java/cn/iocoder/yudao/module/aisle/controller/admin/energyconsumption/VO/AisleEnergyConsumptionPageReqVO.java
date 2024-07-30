package cn.iocoder.yudao.module.aisle.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AisleEnergyConsumptionPageReqVO extends PageParam {
    private Integer aisleId;

    private String granularity;

    // 查电费分段收费用到的时间参数
    private String startTime;

    private String[] timeRange;

    private String[] aisleIds;

}
