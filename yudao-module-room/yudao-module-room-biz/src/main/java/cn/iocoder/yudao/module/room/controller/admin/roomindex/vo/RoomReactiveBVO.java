package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomReactiveBVO {
    /**
     * b路平均无功功率
     */
    @JsonProperty("reactive_b_avg_value")
    private float reactiveBAvgValue;

    /**
     * b路最大有功功率
     */
    @JsonProperty("reactive_b_max_value")
    private float reactiveBMaxValue;
    /**
     * b路最大有功功率时间
     */
    @JsonProperty("reactive_b_max_time")
    private DateTime reactiveBMaxTime;
    /**
     * b路最小有功功率时间
     */
    @JsonProperty("reactive_b_min_time")
    private DateTime reactiveBMinTime;
    /**
     * b路最小有功功率
     */
    @JsonProperty("reactive_b_min_value")
    private float reactiveBMinValue;
}
