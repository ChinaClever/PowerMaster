package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomActiveTotalVO {
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
}
