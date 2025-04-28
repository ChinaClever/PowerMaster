package cn.iocoder.yudao.module.room.enums;

import lombok.Getter;

@Getter
public enum DataTypeEnum {

    FACTOR_TOTAL_MAX("总最大功率因素"),
    FACTOR_A_TOTAL_MAX("A路最大功率因素"),
    FACTOR_B_TOTAL_MAX("B路最大功率因素"),
    FACTOR_TOTAL_AVG("总平均功率因素"),
    FACTOR_A_TOTAL_AVG("A路平均功率因素"),
    FACTOR_B_TOTAL_AVG("B路平均功率因素"),
    FACTOR_TOTAL_MIN("总最小功率因素"),
    FACTOR_A_TOTAL_MIN("A路最小功率因素"),
    FACTOR_B_TOTAL_MIN("B路最小功率因素"),

    APPARENT_TOTAL_MAX("总最大视在功率"),
    ACTIVE_TOTAL_MAX("总最大有功功率"),
    REACTIVE_TOTAL_MAX("总最大无功功率"),
    APPARENT_TOTAL_AVG("总平均视在功率"),
    ACTIVE_TOTAL_AVG("总平均有功功率"),
    REACTIVE_TOTAL_AVG("总平均无功功率"),
    APPARENT_TOTAL_MIN("总最小视在功率"),
    ACTIVE_TOTAL_MIN("总最小有功功率"),
    REACTIVE_TOTAL_MIN("总最小无功功率"),

    A_APPARENT_TOTAL_MAX("A路最大视在功率"),
    A_ACTIVE_TOTAL_MAX("A路最大有功功率"),
    A_REACTIVE_TOTAL_MAX("A路最大无功功率"),
    A_APPARENT_TOTAL_AVG("A路平均视在功率"),
    A_ACTIVE_TOTAL_AVG("A路平均有功功率"),
    A_REACTIVE_TOTAL_AVG("A路平均无功功率"),
    A_APPARENT_TOTAL_MIN("A路最小视在功率"),
    A_ACTIVE_TOTAL_MIN("A路最小有功功率"),
    A_REACTIVE_TOTAL_MIN("A路最小无功功率"),

    B_APPARENT_TOTAL_MAX("B路最大视在功率"),
    B_ACTIVE_TOTAL_MAX("B路最大有功功率"),
    B_REACTIVE_TOTAL_MAX("B路最大无功功率"),
    B_APPARENT_TOTAL_AVG("B路平均视在功率"),
    B_ACTIVE_TOTAL_AVG("B路平均有功功率"),
    B_REACTIVE_TOTAL_AVG("B路平均无功功率"),
    B_APPARENT_TOTAL_MIN("B路最小视在功率"),
    B_ACTIVE_TOTAL_MIN("B路最小有功功率"),
    B_REACTIVE_TOTAL_MIN("B路最小无功功率");




    private  String dataType;

    DataTypeEnum( String dataType) {
        this.dataType = dataType;
    }
}
