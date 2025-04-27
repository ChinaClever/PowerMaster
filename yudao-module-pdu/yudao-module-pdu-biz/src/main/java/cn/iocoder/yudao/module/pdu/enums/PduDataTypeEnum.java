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
    REACTIVE_TOTAL_MIN("总最小无功功率");



    private  String dataType;

    PduDataTypeEnum( String dataType) {
        this.dataType = dataType;
    }

}
