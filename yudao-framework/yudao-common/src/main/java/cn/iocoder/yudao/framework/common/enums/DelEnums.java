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
public enum DelEnums {
    DELETE(1, "删除"),
    NO_DEL(0, "未删除");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
