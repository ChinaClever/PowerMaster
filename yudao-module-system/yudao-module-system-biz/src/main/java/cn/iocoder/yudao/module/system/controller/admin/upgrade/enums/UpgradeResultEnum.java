package cn.iocoder.yudao.module.system.controller.admin.upgrade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 设备升级状态
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum UpgradeResultEnum {
    FAIL(2, "失败"),
    SUCCESS(1, "成功"),
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
        UpgradeResultEnum[] enums = UpgradeResultEnum.values();
        for (UpgradeResultEnum indexEnum : enums){
            if (indexEnum.type.equals(type)){
                return indexEnum.getDesc();
            }
        }
        return null;
    }

}
