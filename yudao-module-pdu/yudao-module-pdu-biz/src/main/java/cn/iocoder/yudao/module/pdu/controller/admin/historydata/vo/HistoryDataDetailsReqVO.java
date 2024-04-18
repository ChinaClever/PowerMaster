package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HistoryDataDetailsReqVO extends PageParam {
    private Integer id;

    private String type;

    private String granularity;

    private String ipAddr;

    private String[] timeRange;

}