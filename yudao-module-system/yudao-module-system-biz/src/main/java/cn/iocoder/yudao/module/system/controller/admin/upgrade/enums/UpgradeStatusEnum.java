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
public enum UpgradeStatusEnum {
    START(0, "未开始"),
    NOTICE(1, "已通知"),
    DOWNLOAD_ING(2, "下载中"),
    DOWNLOAD_END(3, "下载完成"),
    TIMEOUT(4, "超时未下载"),
    DOWNLOAD_FAIL(5, "下载失败"),
    UN_UPGRADE(6, "无需升级"),
    UPGRADE_END(7, "升级完成"),
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
        UpgradeStatusEnum[] enums = UpgradeStatusEnum.values();
        for (UpgradeStatusEnum indexEnum : enums){
            if (indexEnum.type.equals(type)){
                return indexEnum.getDesc();
            }
        }
        return null;
    }

}
