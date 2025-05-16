package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "BUS报表/电流(小时/天)")
@Data
public class BusHdaLoopAvgResVO {

    @Schema(description = "相")
    @JsonProperty("loop_id")
    private Integer loopId;
    /**
     *平均电压
     */
    @Schema(description = "平均电压")
    @JsonProperty("vol_avg_value")
    private Float volAvgValue;

    /**
     * 最大电压时间
     */
    @JsonProperty("vol_max_time")
    private DateTime volMaxTime;


    /**
     * 最大电压
     */
    @JsonProperty("vol_max_value")
    private Float volMaxValue;

    /**
     * 最小电压时间
     */
    @JsonProperty("vol_min_time")
    private DateTime volMinTime;


    /**
     * 最小电压
     */
    @JsonProperty("vol_min_value")
    private Float volMinValue;

    /**
     * 平均电流
     */
    @Schema(description = "平均电流")
    @JsonProperty("cur_avg_value")
    private Float curAvgValue;

    /**
     * 最大电流时间
     */
    @JsonProperty("cur_max_time")
    private DateTime curMaxTime;

    /**
     * 最大电流
     */
    @JsonProperty("cur_max_value")
    private Float curMaxValue;

    /**
     * 最小电流时间
     */
    @JsonProperty("cur_min_time")
    private DateTime curMinTime;


    /**
     * 最小电流
     */
    @JsonProperty("cur_min_value")
    private Float curMinValue;


    @Schema(description = "busId")
    @JsonProperty("bus_id")
    private int busId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime createTime;



}
