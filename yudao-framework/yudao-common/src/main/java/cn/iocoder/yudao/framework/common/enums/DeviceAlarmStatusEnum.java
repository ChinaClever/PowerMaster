package cn.iocoder.yudao.framework.common.enums;

import lombok.Getter;

/**
 * @Author: chenwany
 * @Date: 2024/4/1 13:53
 * @Description: 设备告警状态
 */
@Getter
public enum DeviceAlarmStatusEnum {

    /**
     * 设备正常
     */
    NORMAL(0, "正常"),
    /**
     * 设备预警
     */
    EARLY_WARNING(1, "预警"),
    /**
     * 设备告警
     */
    ALARM(2, "告警"),
    /**
     * 设备升级
     */
    UPGRADE(3, "升级"),
    /**
     * 设备故障
     */
    FAULT(4, "故障"),
    /**
     * 设备离线
     */
    OFF_LINE(5, "离线");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 描述
     */
    private final String desc;

    DeviceAlarmStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

}
