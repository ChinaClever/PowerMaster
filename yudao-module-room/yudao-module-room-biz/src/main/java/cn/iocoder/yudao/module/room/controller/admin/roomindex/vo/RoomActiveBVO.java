package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomActiveBVO {
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
}
