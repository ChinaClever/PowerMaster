package cn.iocoder.yudao.framework.common.entity.es.pdu.total;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/2 16:59
 * @Description: 公共属性
 */
@Data
public class PduTotalBaseDo {


    private int id;

    /**
     * 设备标识
     */

    @JsonProperty("pdu_id")
    private int pduId;


    /**
     * 平均有功功率
     */
    @JsonProperty("pow_active_avg_value")
    private float activePowAvgValue;

    /**
     * 最大有功功率时间
     */
    @JsonProperty("pow_active_max_time")
    private DateTime activePowMaxTime;

    /**
     * 最大有功功率
     */
    @JsonProperty("pow_active_max_value")
    private float activePowMaxValue;

    /**
     * 最小有功功率时间
     */
    @JsonProperty("pow_active_min_time")
    private DateTime activePowMinTime;

    /**
     * 最小有功功率
     */
    @JsonProperty("pow_active_min_value")
    private float activePowMinValue;


    /**
     * 平均视在功率
     */
    @JsonProperty("pow_apparent_avg_value")
    private float apparentPowAvgValue;

    /**
     * 最大视在功率时间
     */
    @JsonProperty("pow_apparent_max_time")
    private DateTime apparentPowMaxTime;

    /**
     * 最大视在功率
     */
    @JsonProperty("pow_apparent_max_value")
    private float apparentPowMaxValue;


    /**
     * 最小视在功率时间
     */
    @JsonProperty("pow_apparent_min_time")
    private DateTime apparentPowMinTime;

    /**
     * 最小视在功率
     */
    @JsonProperty("pow_apparent_min_value")
    private float apparentPowMinValue;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private DateTime createTime;

    /**
     * 平均无功功率
     */
    @JsonProperty("pow_reactive_avg_value")
    private float powReactiveAvgValue;

    /**
     * 最大无功功率时间
     */
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
     * 平均功率因素
     */
    @JsonProperty("power_factor_max_value")
    private float powerFactorMaxValue;

    /**
     * 平均功率因素
     */
    @JsonProperty("power_factor_min_value")
    private float powerFactorMinValue;

}
