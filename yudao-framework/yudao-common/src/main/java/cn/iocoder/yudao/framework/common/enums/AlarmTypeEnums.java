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
    PDU_OFF_LINE(1, "PDU离线"),
    PDU_ALARM(2, "PDU告警"),
    PDU_WARNING(3, "PDU预警"),
    BUS_ALARM(4, "母线告警"),
    BUS_OFF_LINE(5, "母线离线"),
    CABINET_CAPACITY(6, "机柜容量"),
    STATUS(7, "状态告警"),
    OFF_LINE(8, "离线告警"),
    ELE(9, "用能告警"),
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

    public static Integer getStatusByName(String name) {
        AlarmTypeEnums[] enums = AlarmTypeEnums.values();
        for (AlarmTypeEnums indexEnum : enums){
            if (indexEnum.getName().equals(name)){
                return indexEnum.getType();
            }
        }
        return null;
    }


}
