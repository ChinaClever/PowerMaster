package cn.iocoder.yudao.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 设备类型枚举
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum AlarmTypeEnums {
    STATUS(1, "状态告警"),
    OFF_LINE(2, "离线告警"),
    ELE(3, "用能告警"),
    ;

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

    public static String getNameByStatus(Integer type) {
        AlarmTypeEnums[] enums = AlarmTypeEnums.values();
        for (AlarmTypeEnums indexEnum : enums){
            if (indexEnum.type.equals(type)){
                return indexEnum.getName();
            }
        }
        return null;
    }


}
