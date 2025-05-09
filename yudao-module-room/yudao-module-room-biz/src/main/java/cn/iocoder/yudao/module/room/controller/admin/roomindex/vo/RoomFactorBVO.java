package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomFactorBVO {
    /**
     * b路平均功率因素
     */
    @JsonProperty("factor_b_avg_value")
    private float factorBAvgValue;

    @JsonProperty("factor_b_max_time")
    private DateTime factorBMaxTime;

    @JsonProperty("factor_b_max_value")
    private float factorBMaxValue;

    @JsonProperty("factor_b_min_time")
    private DateTime factorBMinTime;

    @JsonProperty("factor_b_min_value")
    private float factorBMinValue;
}
