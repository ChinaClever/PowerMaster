package cn.iocoder.yudao.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 告警登记状态枚举
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum AlarmLevelEnums {
    ONE(1, "一级告警"),
    TWO(2, "二级告警"),
    THREE(3, "三级告警"),
    ;

    /**
     * 类型
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

    public static String getNameByStatus(Integer status) {
        AlarmLevelEnums[] enums = AlarmLevelEnums.values();
        for (AlarmLevelEnums indexEnum : enums){
            if (indexEnum.status.equals(status)){
                return indexEnum.getName();
            }
        }
        return null;
    }

    public static Integer getStatusByName(String name) {
        AlarmLevelEnums[] enums = AlarmLevelEnums.values();
        for (AlarmLevelEnums indexEnum : enums){
            if (indexEnum.getName().equals(name)){
                return indexEnum.getStatus();
            }
        }
        return null;
    }

}
