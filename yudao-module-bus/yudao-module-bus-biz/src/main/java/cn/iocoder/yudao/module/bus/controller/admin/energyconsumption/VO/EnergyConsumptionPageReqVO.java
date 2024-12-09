package cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnergyConsumptionPageReqVO extends PageParam {
    private Integer busId;

    private Integer boxId;

    // 分段收费传来的参数
    private String startTime;

    private String granularity;

    private String[] timeRange;

    private String[] devkeys;

    private String devkey;

    private String nowAddress;

}
