package cn.iocoder.yudao.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 删除枚举
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum DelFlagEnums {
    DELETE(true, "删除"),
    NO_DEL(false, "未删除");

    /**
     * 状态值
     */
    private final Boolean flag;
    /**
     * 状态名
     */
    private final String name;

}
