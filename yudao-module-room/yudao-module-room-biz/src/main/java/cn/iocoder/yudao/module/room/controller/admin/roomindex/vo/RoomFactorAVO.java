package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomFactorAVO {
    /**
     * a路平均功率因素
     */
    @JsonProperty("factor_a_avg_value")
    private float factorAAvgValue;

    @JsonProperty("factor_a_max_time")
    private DateTime factorAMaxTime;

    @JsonProperty( "factor_a_max_value")
    private float factorAMaxValue;

    @JsonProperty("factor_a_min_time")
    private DateTime factorAMinTime;

    @JsonProperty("factor_a_min_value")
    private float factorAMinValue;
}
