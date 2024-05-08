package cn.iocoder.yudao.module.pdu.enums;

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
