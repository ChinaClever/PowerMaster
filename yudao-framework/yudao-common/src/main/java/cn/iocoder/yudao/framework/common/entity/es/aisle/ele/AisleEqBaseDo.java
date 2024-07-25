package cn.iocoder.yudao.framework.common.entity.es.aisle.ele;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.aisle.AisleBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜电量基础表
 * @date 2024/4/23 10:24
 */
@Data
public class AisleEqBaseDo extends AisleBaseDo {

    /**
     * 开始时间
     */
    @JsonProperty("start_time")
    private DateTime startTime;

    /**
     * 开始电能
     */
    @JsonProperty("start_ele")
    private double startEle;

    /**
     * 结束电能
     */
    @JsonProperty("end_ele")
    private double endEle;

    /**
     * 结束时间
     */
    @JsonProperty("end_time")
    private DateTime endTime;

    /**
     * 电量
     */
    @JsonProperty("eq_value")
    private double eqValue;

    /**
     * 电费
     */
    @JsonProperty("bill_value")
    private double billValue;

    /**
     * 计费方式
     */
    @JsonProperty("bill_mode")
    private int billMode;

    /**
     * 计费时间段
     */
    @JsonProperty("bill_period")
    private String billPeriod;

    /**
     * 计费方式(实际计费方式)
     */
    @JsonProperty("bill_mode_real")
    private Integer billModeReal;
}
