package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.pdu.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "BUS报表/电流(小时/天)")
@Data
public class BusHdaLineAvgResVO {

    @Schema(description = "相")
    @JsonProperty("line_id")
    private Integer lineId;
    /**
     *平均电压
     */
    @Schema(description = "平均电压")
    @JsonProperty("vol_avg_value")
    private BigDecimal volAvgValue;
    /**
     * 平均电流
     */
    @Schema(description = "平均电流")
    @JsonProperty("cur_avg_value")
    private BigDecimal curAvgValue;

    /**
     *最大电压
     */
    @Schema(description = "最大电压")
    @JsonProperty("vol_max_value")
    private BigDecimal volMaxValue;

    /**
     * 最大电压时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("vol_max_time")
    private Date volMaxTime;

    /**
     * 最大电流
     */
    @Schema(description = "最大电流")
    @JsonProperty("cur_max_value")
    private BigDecimal curMaxValue;

    /**
     * 最大电流时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("cur_max_time")
    private Date curMaxTime;

    /**
     *最小电压
     */
    @Schema(description = "最小电压")
    @JsonProperty("vol_min_value")
    private BigDecimal volMinValue;

    /**
     * 最小电压时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("vol_min_time")
    private Date volMinTime;

    /**
     * 最小电流
     */
    @Schema(description = "最小电流")
    @JsonProperty("cur_min_value")
    private BigDecimal curMinValue;

    /**
     * 最小电流时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("cur_min_time")
    private Date curMinTime;


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
