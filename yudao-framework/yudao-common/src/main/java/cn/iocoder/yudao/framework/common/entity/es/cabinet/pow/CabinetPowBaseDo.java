package cn.iocoder.yudao.framework.common.entity.es.cabinet.pow;

import cn.iocoder.entity.es.cabinet.CabinetBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜历史基础数据
 * @date 2024/4/23 9:23
 */
@Data
public class CabinetPowBaseDo extends CabinetBaseDo {

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
    private String apparentTotalMaxTime;
    /**
     * 最小总视在功率时间
     */
    @JsonProperty("apparent_total_min_time")
    private String apparentTotalMinTime;
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
    private String apparentAMaxTime;
    /**
     * a路最小视在功率时间
     */
    @JsonProperty("apparent_a_min_time")
    private String apparentAMinTime;
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
    private String apparentBMaxTime;
    /**
     * b路最小视在功率时间
     */
    @JsonProperty("apparent_b_min_time")
    private String apparentBMinTime;
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
    private String activeTotalMaxTime;
    /**
     * 总最小有功功率时间
     */
    @JsonProperty("active_total_min_time")
    private String activeTotalMinTime;
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
    private String activeAMaxTime;
    /**
     * a路最小有功功率时间
     */
    @JsonProperty("active_a_min_time")
    private String activeAMinTime;
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
    private String activeBMaxTime;
    /**
     * b路最小有功功率时间
     */
    @JsonProperty("active_b_min_time")
    private String activeBMinTime;
    /**
     * b路最小有功功率
     */
    @JsonProperty("active_b_min_value")
    private float activeBMinValue;

}
