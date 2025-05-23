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
    CABINET_CAPACITY_WARNING(6, "机柜电力容量预警"),
    CABINET_CAPACITY_ALARM(7, "机柜电力容量告警"),
    CABINET_DAY_POWER_ALARM(8, "机柜每日电量限额告警"),
    CABINET_MONTH_POWER_ALARM(9, "机柜每月电量限额告警"),
    AISLE_CAPACITY_WARNING(10, "柜列电力容量预警"),
    AISLE_CAPACITY_ALARM(11, "柜列电力容量告警"),
    AISLE_DAY_POWER_ALARM(12, "柜列每日电量限额告警"),
    AISLE_MONTH_POWER_ALARM(13, "柜列每月电量限额告警"),
    ROOM_CAPACITY_WARNING(14, "机房电力容量预警"),
    ROOM_CAPACITY_ALARM(15, "机房电力容量告警"),
    ROOM_DAY_POWER_ALARM(16, "机房每日电量限额告警"),
    ROOM_MONTH_POWER_ALARM(17, "机房每月电量限额告警"),
    STATUS(18, "状态告警"),
    OFF_LINE(19, "离线告警"),
    ELE(20, "用能告警"),
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
