package cn.iocoder.yudao.module.rack.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RackHistoryDataDetailsReqVO extends PageParam {
    private Integer rackId;

    private String granularity;

    private String[] timeRange;

}