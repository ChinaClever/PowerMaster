package cn.iocoder.yudao.framework.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenwany
 * @Date: 2024/4/3 15:39
 * @Description: ES数据字段枚举
 */
@Getter
public enum EsStatisFieldEnum {

    /**
     * 平均电压
     */
    VOL_AVG_VALUE("vol_avg_value","平均电压"),
    /**
     * 最大电压
     */
    VOL_MAX_VALUE("vol_max_value","最大电压"),
    /**
     * 最小电压
     */
    VOL_MIN_VALUE("vol_min_value","最小电压"),
    /**
     * 平均电流
     */
    CUR_AVG_VALUE("cur_avg_value","平均电流"),
    /**
     * 最大电流
     */
    CUR_MAX_VALUE("cur_max_value","最大电流"),
    /**
     * 最小电流
     */
    CUR_MIN_VALUE("cur_min_value","最小电流"),
    /**
     * 平均有功功率
     */
    ACTIVE_POW_AVG_VALUE("pow_active_avg_value","平均有功功率"),
    /**
     * 最大有功功率
     */
    ACTIVE_POW_MAX_VALUE("pow_active_max_value","最大有功功率"),
    /**
     * 最小有功功率
     */
    ACTIVE_POW_MIN_VALUE("pow_active_min_value","最小有功功率"),
    /**
     * 平均视在功率
     */
    APPARENT_POW_AVG_VALUE("pow_apparent_avg_value","平均视在功率"),
    /**
     * 最大视在功率
     */
    APPARENT_POW_MAX_VALUE("pow_apparent_max_value","最大视在功率"),
    /**
     * 最小视在功率
     */
    APPARENT_POW_MIN_VALUE("pow_apparent_min_value","最小视在功率"),

    ;
    /**
     * es索引
     */
    private final String field;

    /**
     * 索引描述
     */
    private final String desc;

    EsStatisFieldEnum(String field, String desc) {
        this.field = field;
        this.desc = desc;
    }

    /**
     * 获取所有字段值
     */
    public static List<String> fields() {
        return Arrays.stream(EsStatisFieldEnum.values()).map(EsStatisFieldEnum::getField).collect(Collectors.toList());
    }

}
