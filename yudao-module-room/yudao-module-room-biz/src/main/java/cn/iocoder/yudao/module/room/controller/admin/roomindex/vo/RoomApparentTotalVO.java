package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomApparentTotalVO {

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
    private DateTime apparentTotalMaxTime;
    /**
     * 最小总视在功率时间
     */
    @JsonProperty("apparent_total_min_time")
    private DateTime apparentTotalMinTime;
    /**
     * 最小总视在功率
     */
    @JsonProperty("apparent_total_min_value")
    private float apparentTotalMinValue;
}
