package cn.iocoder.yudao.framework.common.entity.es.bus.total;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.bus.BusBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱总历史数据
 * @date 2024/5/21 13:35
 */
@Data
public class BusTotalBaseDo extends BusBaseDo {


    /**
     * 平均有功功率
     */
    @JsonProperty("pow_active_avg_value")
    private float powActiveAvgValue;

    /**
     * 最大有功功率时间
     */
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
     * 平均剩余电流
     */
    @JsonProperty("cur_residual_avg_value")
    private float curResidualAvgValue;

    /**
     * 最大剩余电流时间
     */
    @JsonProperty("cur_residual_max_time")
    private DateTime curResidualMaxTime;

    /**
     * 最大剩余电流
     */
    @JsonProperty("cur_residual_max_value")
    private float curResidualMaxValue;


    /**
     * 最小剩余电流时间
     */
    @JsonProperty("cur_residual_min_time")
    private DateTime curResidualMinTime;

    /**
     * 最小剩余电流
     */
    @JsonProperty("cur_residual_min_value")
    private float curResidualMinValue;


    /**
     * 平均零线电流
     */
    @JsonProperty("cur_zero_avg_value")
    private float curZeroAvgValue;

    /**
     * 最大零线电流时间
     */
    @JsonProperty("cur_zero_max_time")
    private DateTime curZeroMaxTime;

    /**
     * 最大零线电流
     */
    @JsonProperty("cur_zero_max_value")
    private float curZeroMaxValue;


    /**
     * 最小零线电流时间
     */
    @JsonProperty("cur_zero_min_time")
    private DateTime curZeroMinTime;

    /**
     * 最小零线电流
     */
    @JsonProperty("cur_zero_min_value")
    private float curZeroMinValue;

}
