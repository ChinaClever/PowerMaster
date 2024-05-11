package cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CabinetHistoryDataDetailsReqVO extends PageParam {
    private Integer cabinetId;

    private String granularity;

    private String[] timeRange;

}