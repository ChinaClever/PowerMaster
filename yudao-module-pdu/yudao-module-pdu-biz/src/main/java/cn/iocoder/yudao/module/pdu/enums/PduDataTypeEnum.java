package cn.iocoder.yudao.module.pdu.enums;

import lombok.Getter;

@Getter
public enum PduDataTypeEnum {

    //处理pow接口数据
    APPARENT_TOTAL_MAX("总最大视在功率"),
    ACTIVE_TOTAL_MAX("总最大有功功率"),
    REACTIVE_TOTAL_MAX("总最大无功功率"),
    APPARENT_TOTAL_AVG("总平均视在功率"),
    ACTIVE_TOTAL_AVG("总平均有功功率"),
    REACTIVE_TOTAL_AVG("总平均无功功率"),
    APPARENT_TOTAL_MIN("总最小视在功率"),
    ACTIVE_TOTAL_MIN("总最小有功功率"),
    REACTIVE_TOTAL_MIN("总最小无功功率"),

    FACTOR_TOTAL_MAX("总最大功率因素"),
    FACTOR_A_TOTAL_MAX("A路最大功率因素"),
    FACTOR_B_TOTAL_MAX("B路最大功率因素"),
    FACTOR_TOTAL_AVG("总平均功率因素"),
    FACTOR_A_TOTAL_AVG("A路平均功率因素"),
    FACTOR_B_TOTAL_AVG("B路平均功率因素"),
    FACTOR_TOTAL_MIN("总最小功率因素"),
    FACTOR_A_TOTAL_MIN("A路最小功率因素"),
    FACTOR_B_TOTAL_MIN("B路最小功率因素"),

    CURRENT_MAX("最大电流"),
    CURRENT_AVG("平均电流"),
    CURRENT_MIN("最小电流"),

    FACTOR_LINE_A_MAX("A相最大功率因素"),
    FACTOR_LINE_B_MAX("B相最大功率因素"),
    FACTOR_LINE_C_MAX("C相最大功率因素"),
    FACTOR_LINE_A_MIN("A相最小功率因素"),
    FACTOR_LINE_B_MIN("B相最小功率因素"),
    FACTOR_LINE_C_MIN("C相最小功率因素"),
    FACTOR_LINE_A_AVG("A相平均功率因素"),
    FACTOR_LINE_B_AVG("B相平均功率因素"),
    FACTOR_LINE_C_AVG("C相平均功率因素");



    private  String dataType;

    PduDataTypeEnum( String dataType) {
        this.dataType = dataType;
    }

}
