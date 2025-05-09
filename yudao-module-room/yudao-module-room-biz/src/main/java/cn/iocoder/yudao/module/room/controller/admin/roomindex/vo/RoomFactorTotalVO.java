package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomFactorTotalVO {
    /**
     * 总平均功率因素
     */
    @JsonProperty("factor_total_avg_value")
    private float factorTotalAvgValue;

    @JsonProperty("factor_total_max_time")
    private DateTime factorTotalMaxTime;

    @JsonProperty("factor_total_max_value")
    private float factorTotalMaxValue;

    @JsonProperty("factor_total_min_time")
    private DateTime factorTotalMinTime;

    @JsonProperty("factor_total_min_value")
    private float factorTotalMinValue;
}
