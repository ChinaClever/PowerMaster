package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EnvDataDetailsReqVO extends PageParam {
    private Integer pduId;

    private Integer sensorId;

    private String granularity;

    private String ipAddr;

    private String cascadeAddr;

    private String[] timeRange;

}