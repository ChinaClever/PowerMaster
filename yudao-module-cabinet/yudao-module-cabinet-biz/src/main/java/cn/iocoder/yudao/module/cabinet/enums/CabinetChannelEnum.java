package cn.iocoder.yudao.module.cabinet.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜通道枚举
 * @date 2024/5/10 13:56
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CabinetChannelEnum {

    FRONT(1, "前端"),
    BACK(2, "后端");

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
    public static CabinetChannelEnum getEnumByValue(int value) {
        CabinetChannelEnum[] enums = CabinetChannelEnum.values();
        for (CabinetChannelEnum indexEnum : enums) {
            if (indexEnum.value.equals(value)) {
                return indexEnum;
            }
        }
        return null;
    }

}
