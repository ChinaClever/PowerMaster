package cn.iocoder.yudao.framework.common.entity.es.aisle.pow;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.aisle.AisleBaseDo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列历史基础数据
 * @date 2024/4/23 9:23
 */
@Data
public class AislePowBaseDo extends AisleBaseDo {

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
     * a路平均视在功率
     */
    @JsonProperty("apparent_a_avg_value")
    private float apparentAAvgValue;
    /**
     * a路最大视在功率
     */
    @JsonProperty("apparent_a_max_value")
    private float apparentAMaxValue;
    /**
     * a路最大视在功率时间
     */
    @JsonProperty("apparent_a_max_time")
    private DateTime apparentAMaxTime;
    /**
     * a路最小视在功率时间
     */
    @JsonProperty("apparent_a_min_time")
    private DateTime apparentAMinTime;
    /**
     * a路最小视在功率
     */
    @JsonProperty("apparent_a_min_value")
    private float apparentAMinValue;

    /**
     * b路平均视在功率
     */
    @JsonProperty("apparent_b_avg_value")
    private float apparentBAvgValue;
    /**
     * b路最大视在功率
     */
    @JsonProperty("apparent_b_max_value")
    private float apparentBMaxValue;
    /**
     * b路最大视在功率时间
     */
    @JsonProperty("apparent_b_max_time")
    private DateTime apparentBMaxTime;
    /**
     * b路最小视在功率时间
     */
    @JsonProperty("apparent_b_min_time")
    private DateTime apparentBMinTime;
    /**
     * b路最小视在功率
     */
    @JsonProperty("apparent_b_min_value")
    private float apparentBMinValue;

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
     * a路平均有功功率
     */
    @JsonProperty("active_a_avg_value")
    private float activeAAvgValue;
    /**
     * a路最大有功功率
     */
    @JsonProperty("active_a_max_value")
    private float activeAMaxValue;
    /**
     * a路最大有功功率时间
     */
    @JsonProperty("active_a_max_time")
    private DateTime activeAMaxTime;
    /**
     * a路最小有功功率时间
     */
    @JsonProperty("active_a_min_time")
    private DateTime activeAMinTime;
    /**
     * a路最小有功功率
     */
    @JsonProperty("active_a_min_value")
    private float activeAMinValue;

    /**
     * b路平均有功功率
     */
    @JsonProperty("active_b_avg_value")
    private float activeBAvgValue;
    /**
     * b路最大有功功率
     */
    @JsonProperty("active_b_max_value")
    private float activeBMaxValue;
    /**
     * b路最大有功功率时间
     */
    @JsonProperty("active_b_max_time")
    private DateTime activeBMaxTime;
    /**
     * b路最小有功功率时间
     */
    @JsonProperty("active_b_min_time")
    private DateTime activeBMinTime;
    /**
     * b路最小有功功率
     */
    @JsonProperty("active_b_min_value")
    private float activeBMinValue;

    /**
     * 总平均无功功率
     */
    @JsonProperty("reactive_total_avg_value")
    private float reactiveTotalAvgValue;
    /**
     * 总最大无功功率
     */
    @JsonProperty("reactive_total_max_value")
    private float reactiveTotalMaxValue;
    /**
     * 总最大无功功率时间
     */
    @JsonProperty("reactive_total_max_time")
    private DateTime reactiveTotalMaxTime;
    /**
     * 总最小无功功率时间
     */
    @JsonProperty("reactive_total_min_time")
    private DateTime reactiveTotalMinTime;
    /**
     * 总最小无功功率
     */
    @JsonProperty("reactive_total_min_value")
    private float reactiveTotalMinValue;

    /**
     * a路平均无功功率
     */
    @JsonProperty("reactive_a_avg_value")
    private float reactiveAAvgValue;

    /**
     * b路平均无功功率
     */
    @JsonProperty("reactive_b_avg_value")
    private float reactiveBAvgValue;

    /**
     * a路最大无功功率
     */
    @JsonProperty("reactive_a_max_value")
    private float reactiveAMaxValue;
    /**
     * a路最大无功功率时间
     */
    @JsonProperty("reactive_a_max_time")
    private DateTime reactiveAMaxTime;
    /**
     * a路最小无功功率时间
     */
    @JsonProperty("reactive_a_min_time")
    private DateTime reactiveAMinTime;
    /**
     * a路最小无功功率
     */
    @JsonProperty("reactive_a_min_value")
    private float reactiveAMinValue;

    /**
     * b路最大无功功率
     */
    @JsonProperty("reactive_b_max_value")
    private float reactiveBMaxValue;
    /**
     * b路最大无功功率时间
     */
    @JsonProperty("reactive_b_max_time")
    private DateTime reactiveBMaxTime;
    /**
     * b路最小无功功率时间
     */
    @JsonProperty("reactive_b_min_time")
    private DateTime reactiveBMinTime;
    /**
     * b路最小无功功率
     */
    @JsonProperty("reactive_b_min_value")
    private float reactiveBMinValue;

    /**
     * 总平均功率因素
     */
    @JsonProperty("factor_total_avg_value")
    private float factorTotalAvgValue;

    /**
     * a路平均功率因素
     */
    @JsonProperty("factor_a_avg_value")
    private float factorAAvgValue;

    /**
     * b路平均功率因素
     */
    @JsonProperty("factor_b_avg_value")
    private float factorBAvgValue;

    /**
     * 总最大功率因素
     */
    @JsonProperty("factor_total_max_value")
    private float factorTotalMaxValue;

    @JsonProperty("factor_total_max_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime factorTotalMaxTime;

    /**
     * a路最大功率因素
     */
    @JsonProperty("factor_a_max_value")
    private float factorAMaxValue;

    @JsonProperty("factor_a_max_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime factorAMaxTime;

    /**
     * b路最大功率因素
     */
    @JsonProperty("factor_b_max_value")
    private float factorBMaxValue;

    @JsonProperty("factor_b_max_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime factorBMaxTime;

    /**
     * 总最小功率因素
     */
    @JsonProperty("factor_total_min_value")
    private float factorTotalMinValue;

    @JsonProperty("factor_total_min_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime factorTotalMinTime;

    /**
     * a路最大功率因素
     */
    @JsonProperty("factor_a_min_value")
    private float factorAMinValue;

    @JsonProperty("factor_a_min_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime factorAMinTime;

    /**
     * b路最大功率因素
     */
    @JsonProperty("factor_b_min_value")
    private float factorBMinValue;

    @JsonProperty("factor_b_min_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime factorBMinTime;
}
