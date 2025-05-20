package cn.iocoder.yudao.framework.common.entity.es.box.outlet;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.box.BoxBaseDo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱输出位历史数据表（
 * @date 2024/5/21 14:40
 */
@Data
public class BoxOutletBaseDo extends BoxBaseDo {

    /**
     * 输出位
     */
    @JsonProperty("outlet_id")
    private int outletId;


    /**
     * 平均有功功率
     */
    @JsonProperty("pow_active_avg_value")
    private float powActiveAvgValue;

    /**
     * 最大有功功率时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("pow_active_max_time")
    private DateTime powActiveMaxTime;

    /**
     * 最大有功功率
     */
    @JsonProperty("pow_active_max_value")
    private float powActiveMaxValue;

    /**
     * 最小有功功率时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("pow_active_min_time")
    private DateTime powActiveMinTime;

    /**
     * 最小有功功率
     */
    @JsonProperty("pow_active_min_value")
    private float powActiveMinValue;


    /**
     * 平均视在功率
     */
    @JsonProperty("pow_apparent_avg_value")
    private float powApparentAvgValue;

    /**
     * 最大视在功率时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("pow_apparent_max_time")
    private DateTime powApparentMaxTime;

    /**
     * 最大视在功率
     */
    @JsonProperty("pow_apparent_max_value")
    private float powApparentMaxValue;


    /**
     * 最小视在功率时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("pow_apparent_min_time")
    private DateTime powApparentMinTime;

    /**
     * 最小视在功率
     */
    @JsonProperty("pow_apparent_min_value")
    private float powApparentMinValue;


    /**
     * 平均无功功率
     */
    @JsonProperty("pow_reactive_avg_value")
    private float powReactiveAvgValue;

    /**
     * 最大无功功率时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("pow_reactive_max_time")
    private DateTime powReactiveMaxTime;

    /**
     * 最大无功功率
     */
    @JsonProperty("pow_reactive_max_value")
    private float powReactiveMaxValue;


    /**
     * 最小无功功率时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("pow_reactive_min_time")
    private DateTime powReactiveMinTime;

    /**
     * 最小无功功率
     */
    @JsonProperty("pow_reactive_min_value")
    private float powReactiveMinValue;

    /**
     * 平均功率因素
     */
    @JsonProperty("power_factor_avg_value")
    private float powerFactorAvgValue;

    /**
     * 最大功率因素
     */
    @JsonProperty("power_factor_max_value")
    private float powerFactorMaxValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("power_factor_max_time")
    private DateTime powerFactorMaxTime;

    /**
     * 最小功率因素
     */
    @JsonProperty("power_factor_min_value")
    private float powerFactorMinValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("power_factor_min_time")
    private DateTime powerFactorMinTime;
}
