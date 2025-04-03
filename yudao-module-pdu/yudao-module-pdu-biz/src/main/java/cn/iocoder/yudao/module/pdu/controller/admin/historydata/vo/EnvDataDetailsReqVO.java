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

    /**
     * 机柜前后通道 1前2后
     */
    private Integer channel;
    /**
     * 位置 1 上 2 中 3下
     */
    private Integer position;

    private Integer cabinetId;

    private String[] timeRange;

    private String nowAddress;

    private String pduKey;

}