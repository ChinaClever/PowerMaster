package cn.iocoder.yudao.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luowei
 * @version 1.0
 * @description: 数据来源枚举
 * @date 2024/4/25 14:11
 */
@Getter
@AllArgsConstructor
public enum PduBoxEnums {


    PDU(0, "PDU"),
    BUS(1, "母线");

    /**
     * 值
     */
    private final Integer value;
    /**
     * 名称
     */
    private final String name;

}
