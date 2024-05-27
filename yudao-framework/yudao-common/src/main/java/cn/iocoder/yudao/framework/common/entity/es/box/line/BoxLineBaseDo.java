package cn.iocoder.yudao.framework.common.entity.es.box.line;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.box.BoxBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 相历史数据表
 * @date 2024/5/21 14:48
 */
@Data
public class BoxLineBaseDo extends BoxBaseDo {

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
     * 平均电压
     */
    @JsonProperty("vol_avg_value")
    private float volAvgValue;

    /**
     * 最大电压时间
     */
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
    @JsonProperty("cur_min_time")
    private DateTime curMinTime;

    /**
     * 最小电流
     */
    @JsonProperty("cur_min_value")
    private float curMinValue;


    /**
     * 平均电流谐波含量
     */
    @JsonProperty("cur_thd_avg_value")
    private float curThdAvgValue;

    /**
     * 最大电流谐波含量时间
     */
    @JsonProperty("cur_thd_max_time")
    private DateTime curThdMaxTime;

    /**
     * 最大电流谐波含量
     */
    @JsonProperty("cur_thd_max_value")
    private float curThdMaxValue;


    /**
     * 最小电流谐波含量时间
     */
    @JsonProperty("cur_thd_min_time")
    private DateTime curThdMinTime;

    /**
     * 最小电流谐波含量
     */
    @JsonProperty("cur_thd_min_value")
    private float curThdMinValue;

}
