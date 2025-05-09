package cn.iocoder.yudao.framework.common.enums;

import lombok.Getter;

/**
 * @author: jiangjinchi
 * @time: 2024/11/26 16:04
 */
@Getter
public enum BusStatusEnum {

    /**
     * 设备离线
     */
    OFF_LINE(0, "离线"),

    /**
     * 设备正常
     */
    NORMAL(1, "正常"),

    /**
     * 设备告警
     */
    ALARM(2, "告警");

    /**
     * 状态值
     */
    private  Integer status;
    /**
     * 描述
     */
    private  String desc;

    BusStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }


}
