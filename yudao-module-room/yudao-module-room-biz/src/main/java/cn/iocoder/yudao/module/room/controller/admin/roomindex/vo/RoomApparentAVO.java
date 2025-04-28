package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomApparentAVO {
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
    private DateTime apparentAMaxTime;
    /**
     * a路最小视在功率时间
     */
    @JsonProperty("apparent_a_min_time")
    private DateTime apparentAMinTime;
    /**
     * a路最小视在功率
     */
    @JsonProperty("apparent_a_min_value")
    private float apparentAMinValue;
}
