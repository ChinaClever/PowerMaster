package cn.iocoder.yudao.module.aisle.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AisleHistoryDataDetailsReqVO extends PageParam {
    private Integer aisleId;

    private String granularity;

    private String[] timeRange;

}