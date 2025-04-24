package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoomApparentBVO {
    /**
     * b路平均视在功率
     */
    @JsonProperty("apparent_b_avg_value")
    private float apparentBAvgValue;
    /**
     * b路最大视在功率
     */
    @JsonProperty("apparent_b_max_value")
    private float apparentBMaxValue;
    /**
     * b路最大视在功率时间
     */
    @JsonProperty("apparent_b_max_time")
    private DateTime apparentBMaxTime;
    /**
     * b路最小视在功率时间
     */
    @JsonProperty("apparent_b_min_time")
    private DateTime apparentBMinTime;
    /**
     * b路最小视在功率
     */
    @JsonProperty("apparent_b_min_value")
    private float apparentBMinValue;
}
