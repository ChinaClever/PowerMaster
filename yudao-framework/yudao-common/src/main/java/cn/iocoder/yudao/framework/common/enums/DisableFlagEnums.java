package cn.iocoder.yudao.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 可用枚举
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum DisableFlagEnums {
    ENABLE(false, "可用"),
    DISABLE(true, "不可用");

    /**
     * 状态值
     */
    private final Boolean status;
    /**
     * 状态名
     */
    private final String name;

}
