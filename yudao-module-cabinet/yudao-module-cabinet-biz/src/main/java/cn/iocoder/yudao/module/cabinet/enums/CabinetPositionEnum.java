package cn.iocoder.yudao.module.cabinet.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜位置枚举
 * @date 2024/5/10 13:56
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CabinetPositionEnum {

    EMPTY(0, "无"),
    TOP(1, "上"),
    MIDDLE(2, "中"),
    BOTTOM(3, "下");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 描述
     */
    private final String desc;

    /**
     * 根据值获取枚举类
     */
    public static CabinetPositionEnum getEnumByValue(int value) {
        CabinetPositionEnum[] enums = CabinetPositionEnum.values();
        for (CabinetPositionEnum indexEnum : enums) {
            if (indexEnum.value.equals(value)) {
                return indexEnum;
            }
        }
        return null;
    }

}
