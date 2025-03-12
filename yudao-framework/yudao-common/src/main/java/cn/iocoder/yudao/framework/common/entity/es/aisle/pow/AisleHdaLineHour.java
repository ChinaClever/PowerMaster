package cn.iocoder.yudao.framework.common.entity.es.aisle.pow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class AisleHdaLineHour {

    @JsonProperty("aisle_id")
    private Integer aisleId;

    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("cur_a_avg_value")
    private Double curAAvgValue;

    @JsonProperty("cur_a_max_time")
    private String curAMaxTime;

    @JsonProperty("cur_a_max_value")
    private Double curAMaxValue;

    @JsonProperty("cur_a_min_time")
    private String curAMinTime;

    @JsonProperty("curA_min_value")
    private Double curAMinValue;

    @JsonProperty("cur_b_avg_value")
    private Double curBAvgValue;

    @JsonProperty("cur_b_max_time")
    private String curBMaxTime;

    @JsonProperty("cur_b_max_value")
    private Double curBMaxValue;

    @JsonProperty("cur_b_min_time")
    private String curBMinTime;

    @JsonProperty("cur_b_min_value")
    private Double curBMinValue;


    @JsonProperty("pow_a_avg_value")
    private Double powAAvgValue;

    @JsonProperty("pow_a_max_time")
    private String powAMaxTime;

    @JsonProperty("pow_a_max_value")
    private Double powAMaxValue;

    @JsonProperty("pow_a_min_time")
    private String powAMinTime;

    @JsonProperty("pow_a_min_value")
    private Double powAMinValue;

    @JsonProperty("pow_b_avg_value")
    private Double powBAvgValue;

    @JsonProperty("pow_b_max_time")
    private String powBMaxTime;

    @JsonProperty("pow_b_max_value")
    private Double powBMaxValue;

    @JsonProperty("pow_b_min_time")
    private String powBMinTime;

    @JsonProperty("pow_b_min_value")
    private Double powBMinValue;

    @JsonProperty("vol_a_avg_value")
    private Double volAAvgValue;

    @JsonProperty("vol_a_max_time")
    private String volABaxTime;

    @JsonProperty("vol_a_max_value")
    private Double volAMaxValue;

    @JsonProperty("vol_a_min_time")
    private String volAMinTime;

    @JsonProperty("vol_a_min_value")
    private Double volAMinValue;

    @JsonProperty("vol_b_avg_value")
    private Double volBAvgValue;

    @JsonProperty("vol_b_max_time")
    private String volBMaxTime;

    @JsonProperty("vol_b_max_value")
    private Double volBMaxValue;


    @JsonProperty("vol_b_min_time")
    private String volBMinTime;

    @JsonProperty("vol_b_min_value")
    private Double volBMinValue;
}
