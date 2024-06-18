package cn.iocoder.yudao.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 告警记录状态枚举
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum AlarmStatusEnums {
    UNTREATED(0, "未处理"),
    HUNG(1, "挂起"),
    CONFIRM(2, "确认"),
    FINISH(3, "结束"),
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
        AlarmStatusEnums[] enums = AlarmStatusEnums.values();
        for (AlarmStatusEnums indexEnum : enums){
            if (indexEnum.status.equals(status)){
                return indexEnum.getName();
            }
        }
        return null;
    }

}
