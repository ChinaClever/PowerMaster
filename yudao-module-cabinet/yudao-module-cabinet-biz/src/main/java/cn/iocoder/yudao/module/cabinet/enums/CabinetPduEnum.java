package cn.iocoder.yudao.module.cabinet.enums;

import com.alibaba.druid.sql.visitor.functions.Char;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜PDU枚举
 * @date 2024/5/10 13:56
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CabinetPduEnum {

    A("A", "A路"),
    B("B", "B路"),
    ;

    /**
     * 值
     */
    private final String value;
    /**
     * 描述
     */
    private final String desc;

    /**
     * 根据值获取枚举类
     */
    public static CabinetPduEnum getEnumByValue(String value) {
        CabinetPduEnum[] enums = CabinetPduEnum.values();
        for (CabinetPduEnum indexEnum : enums) {
            if (indexEnum.value.equals(value)) {
                return indexEnum;
            }
        }
        return null;
    }

}
