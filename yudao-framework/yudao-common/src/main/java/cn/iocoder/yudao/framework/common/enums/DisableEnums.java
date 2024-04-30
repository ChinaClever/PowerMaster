package cn.iocoder.yudao.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum DisableEnums {
    ENABLE(0, "可用"),
    DISABLE(1, "不可用");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
