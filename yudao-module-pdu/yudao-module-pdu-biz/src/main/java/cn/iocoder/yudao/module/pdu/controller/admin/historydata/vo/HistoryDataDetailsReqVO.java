package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HistoryDataDetailsReqVO extends PageParam {
    private Integer pduId;

    private Integer lineId;

    private Integer loopId;

    private Integer outletId;

    private String type;

    private String granularity;

    private String ipAddr;

    private String cascadeAddr;

    private String[] timeRange;

    private String nowAddress;

    private String pduKey;
}