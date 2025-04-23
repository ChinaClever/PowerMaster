package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomReactiveTotalVO {
    /**
     * 总平均无功功率
     */
    @JsonProperty("reactive_total_avg_value")
    private float reactiveTotalAvgValue;
    /**
     * 总最大无功功率
     */
    @JsonProperty("reactive_total_max_value")
    private float reactiveTotalMaxValue;
    /**
     * 总最大无功功率时间
     */
    @JsonProperty("reactive_total_max_time")
    private DateTime reactiveTotalMaxTime;
    /**
     * 总最小无功功率时间
     */
    @JsonProperty("reactive_total_min_time")
    private DateTime reactiveTotalMinTime;
    /**
     * 总最小无功功率
     */
    @JsonProperty("reactive_total_min_value")
    private float reactiveTotalMinValue;
}
