package cn.iocoder.yudao.framework.common.enums;

import lombok.Getter;

@Getter
public enum RoomStatusEnum {
    /**
     * 安全
     */
    SAFE(1, "安全"),
    /**
     * 正常
     */
    NORMAL(2, "正常"),
    /**
     * 预警
     */
    EARLY_WARNING(3, "预警"),
    /**
     * 危险
     */
    ALARM(4, "危险");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 描述
     */
    private final String desc;

    RoomStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

}
