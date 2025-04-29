package cn.iocoder.yudao.framework.common.enums;

import lombok.Getter;

@Getter
public enum CabinetStatusEnum {
    /**
     * 未绑定设备
     */
    UNBOUND(0, "未绑定"),
    /**
     * 设备正常
     */
    NORMAL(1, "正常"),
    /**
     * 设备预警
     */
    EARLY_WARNING(2, "预警"),
    /**
     * 设备告警
     */
    ALARM(3, "告警"),
    /**
     * 设备离线
     */
    OFF_LINE(4, "离线");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 描述
     */
    private final String desc;

    CabinetStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

}
