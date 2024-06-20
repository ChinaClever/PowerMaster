package cn.iocoder.yudao.framework.common.entity.es.rack.pow;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.rack.RackBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架历史基础数据
 * @date 2024/4/23 9:23
 */
@Data
public class RackPowBaseDo extends RackBaseDo {

    /**
     * 总平均视在功率
     */
    @JsonProperty("apparent_total_avg_value")
    private float apparentTotalAvgValue;
    /**
     * 最大 总视在功率
     */
    @JsonProperty("apparent_total_max_value")
    private float apparentTotalMaxValue;
    /**
     * 最大总视在功率时间
     */
    @JsonProperty("apparent_total_max_time")
    private DateTime apparentTotalMaxTime;
    /**
     * 最小总视在功率时间
     */
    @JsonProperty("apparent_total_min_time")
    private DateTime apparentTotalMinTime;
    /**
     * 最小总视在功率
     */
    @JsonProperty("apparent_total_min_value")
    private float apparentTotalMinValue;

    /**
     * 总平均有功功率
     */
    @JsonProperty("active_total_avg_value")
    private float activeTotalAvgValue;
    /**
     * 总最大有功功率
     */
    @JsonProperty("active_total_max_value")
    private float activeTotalMaxValue;
    /**
     * 总最大有功功率时间
     */
    @JsonProperty("active_total_max_time")
    private DateTime activeTotalMaxTime;
    /**
     * 总最小有功功率时间
     */
    @JsonProperty("active_total_min_time")
    private DateTime activeTotalMinTime;
    /**
     * 总最小有功功率
     */
    @JsonProperty("active_total_min_value")
    private float activeTotalMinValue;


    /**
     * 平均无功功率
     */
    @JsonProperty("reactive_total_avg_value")
    private float reactiveTotalAvgValue;

    /**
     * 最大无功功率时间
     */
    @JsonProperty("reactive_total_max_time")
    private DateTime reactiveTotalMaxTime;

    /**
     * 最大无功功率
     */
    @JsonProperty("reactive_total_max_value")
    private float reactiveTotalMaxValue;


    /**
     * 最小无功功率时间
     */
    @JsonProperty("reactive_total_min_time")
    private DateTime reactiveTotalMinTime;

    /**
     * 最小无功功率
     */
    @JsonProperty("reactive_total_min_value")
    private float reactiveTotalMinValue;

    /**
     * 平均功率因素
     */
    @JsonProperty("power_factor_avg_value")
    private float powerFactorAvgValue;

}
