package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BusHistoryDataDetailsReqVO extends PageParam {
    private Integer busId;

    private Integer boxId;

    private Integer lineId;

    private Integer loopId;

    private Integer outletId;

    private String type;

    private String granularity;

    private String[] timeRange;

}