package cn.iocoder.yudao.module.cabinet.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 传感器类型
 * @date 2024/5/13 13:01
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SensorTypeEnum {

    HUM(1, "温湿度传感器"),
    ACCESS(2, "门禁传感器"),
    WATER(3, "水浸传感器"),
    SMOKE(4, "烟雾传感器");

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
    public static SensorTypeEnum getEnumByValue(int value) {
        SensorTypeEnum[] enums = SensorTypeEnum.values();
        for (SensorTypeEnum indexEnum : enums) {
            if (indexEnum.value.equals(value)) {
                return indexEnum;
            }
        }
        return null;
    }
}
