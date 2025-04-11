package cn.iocoder.yudao.framework.common.entity.es.bus.line;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.bus.BusBaseDo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱相历史数据
 * @date 2024/5/21 13:51
 */
@Data
public class BusLineBaseDo extends BusBaseDo {


    /**
     * 相
     */
    @JsonProperty("line_id")
    private int lineId;

    /**
     * 平均有功功率
     */
    @JsonProperty("pow_active_avg_value")
    private float powActiveAvgValue;

    /**
     * 最大有功功率时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("pow_reactive_min_time")
    private DateTime powReactiveMinTime;

    /**
     * 最小无功功率
     */
    @JsonProperty("pow_reactive_min_value")
    private float powReactiveMinValue;

    /**
     * 平均电压
     */
    @JsonProperty("vol_avg_value")
    private float volAvgValue;

    /**
     * 最大电压时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("vol_max_time")
    private DateTime volMaxTime;

    /**
     * 最大电压
     */
    @JsonProperty("vol_max_value")
    private float volMaxValue;

    /**
     * 最小电压时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("vol_min_time")
    private DateTime volMinTime;

    /**
     * 最小电压
     */
    @JsonProperty("vol_min_value")
    private float volMinValue;

    /**
     * 平均电流
     */
    @JsonProperty("cur_avg_value")
    private float curAvgValue;

    /**
     * 最大电流时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("cur_max_time")
    private DateTime curMaxTime;

    /**
     * 最大电流
     */
    @JsonProperty("cur_max_value")
    private float curMaxValue;


    /**
     * 最小电流时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("cur_min_time")
    private DateTime curMinTime;

    /**
     * 最小电流
     */
    @JsonProperty("cur_min_value")
    private float curMinValue;


    /**
     * 平均线电压
     */
    @JsonProperty("vol_line_avg_value")
    private float volLineAvgValue;

    /**
     * 最大线电压时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("vol_line_max_time")
    private DateTime volLineMaxTime;

    /**
     * 最大线电压
     */
    @JsonProperty("vol_line_max_value")
    private float volLineMaxValue;


    /**
     * 最小线电压时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("vol_line_min_time")
    private DateTime volLineMinTime;

    /**
     * 最小线电压
     */
    @JsonProperty("vol_line_min_value")
    private float volLineMinValue;


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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("power_factor_max_time")
    private DateTime powerFactorMaxTime;

    /**
     * 平均功率因素
     */
    @JsonProperty("power_factor_min_value")
    private float powerFactorMinValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("power_factor_min_time")
    private DateTime powerFactorMinTime;


    /**
     * 平均功率因素
     */
    @JsonProperty("load_rate_avg_value")
    private float loadRateAvgValue;

    /**
     * 平均功率因素
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("load_rate_max_time")
    private DateTime loadRateMaxTime;

    /**
     * 平均功率因素
     */
    @JsonProperty("load_rate_max_value")
    private float loadRateMaxValue;

    /**
     * 平均功率因素
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("load_rate_min_time")
    private DateTime loadRateMinTime;

    /**
     * 平均功率因素
     */
    @JsonProperty("load_rate_min_value")
    private float loadRateMinValue;

}
