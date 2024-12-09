package cn.iocoder.yudao.module.cabinet.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 菜单类型枚举
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnums {
    ROOM(1, "机房"),
    AISLE(2, "通道"),
    CABINET(3, "机柜"),
    PDU(4, "pdu"),
    RACK(5, "机架"),
    BUS(6, "始端箱"),
    BOX(7, "插接箱")
    ;

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

}
