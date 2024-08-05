package cn.iocoder.yudao.module.system.controller.admin.upgrade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 升级设备枚举
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum UpgradeDevEnum {
    ALL(0, "全部设备"),
    ROOM(1, "指定机房"),
    DEV_IP(2, "指定ip"),
    ;

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String desc;

    public static String getDescByType(Integer type) {
        UpgradeDevEnum[] enums = UpgradeDevEnum.values();
        for (UpgradeDevEnum indexEnum : enums){
            if (indexEnum.type.equals(type)){
                return indexEnum.getDesc();
            }
        }
        return null;
    }

}
