package cn.iocoder.yudao.module.cabinet.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜负载状态
 * @date 2024/4/23 11:10
 */
@Getter
@AllArgsConstructor
public enum CabinetLoadEnums {
    GREY(0, "0%"),
    GREEN(1, "<30%"),
    BLUE(2, "30%-60%"),
    YELLOW(3, "60%-90%"),
    RED(4, ">90%");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;

}
