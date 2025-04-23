package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomReactiveAVO {
    /**
     * a路平均无功功率
     */
    @JsonProperty("rerctive_a_avg_value")
    private float reactiveAAvgValue;

    /**
     * a路最大有功功率
     */
    @JsonProperty("reactive_a_max_value")
    private float reactiveAMaxValue;
    /**
     * a路最大有功功率时间
     */
    @JsonProperty("reactive_a_max_time")
    private DateTime reactiveAMaxTime;
    /**
     * a路最小有功功率时间
     */
    @JsonProperty("reactive_a_min_time")
    private DateTime reactiveAMinTime;
    /**
     * a路最小有功功率
     */
    @JsonProperty("reactive_a_min_value")
    private float reactiveAMinValue;
}
