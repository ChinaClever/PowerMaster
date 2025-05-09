package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomActiveAVO {
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
}
