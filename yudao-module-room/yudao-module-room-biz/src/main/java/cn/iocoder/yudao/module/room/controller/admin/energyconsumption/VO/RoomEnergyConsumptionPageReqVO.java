package cn.iocoder.yudao.module.room.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RoomEnergyConsumptionPageReqVO extends PageParam {
    private Integer roomId;

    private String startTime;

    private String granularity;

    private String[] timeRange;

    private String[] roomIds;

}
