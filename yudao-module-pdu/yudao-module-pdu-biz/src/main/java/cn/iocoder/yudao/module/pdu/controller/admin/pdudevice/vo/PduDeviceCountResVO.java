package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import lombok.Data;

/**
 * @author: jiangjinchi
 * @time: 2024/12/3 10:16
 */
@Data
public class PduDeviceCountResVO {
    /**
     * 正常
     */
    private Integer normal;

    /**
     * 离线
     */
    private Integer offline;

    /**
     * 告警
     */
    private Integer alarm;

    /**
     * 预警
     */
    private Integer warn;
}
