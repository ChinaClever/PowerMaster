package cn.iocoder.yudao.module.cabinet.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CabinetEqTotalDay {
    
    @JsonProperty("bill_mode")
    private Integer billMode;

    @JsonProperty("bill_mode_real")
    private Integer billModeReal;

    @JsonProperty("bill_period")
    private String billPeriod;

    @JsonProperty("bill_value")
    private BigDecimal billValue;

    @JsonProperty("cabinet_id")
    private Integer cabinetId;

    @JsonProperty("create_time")
    private LocalDateTime createTime;

    @JsonProperty("end_ele")
    private BigDecimal endEle;

    @JsonProperty("end_time")
    private LocalDateTime endTime;

    @JsonProperty("eq_value")
    private BigDecimal eqValue;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("start_ele")
    private BigDecimal startEle;

    @JsonProperty("start_time")
    private LocalDateTime startTime;
}
