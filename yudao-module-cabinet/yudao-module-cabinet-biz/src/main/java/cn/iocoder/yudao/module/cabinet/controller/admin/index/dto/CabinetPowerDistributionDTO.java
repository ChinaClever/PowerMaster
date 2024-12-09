package cn.iocoder.yudao.module.cabinet.controller.admin.index.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CabinetPowerDistributionDTO {

    private Integer id;

    private Integer roomId;

    private String roomName;

    private String cabinetName;

    private Integer runStatus;
    /**
     * 有功功率
     */
    @JsonProperty("pow_active")
    private BigDecimal powActive;

    /**
     * 无功功率
     */
    @JsonProperty("pow_reactive")
    private BigDecimal powReactive;


    /**
     * 视在功率
     */
    @JsonProperty("pow_apparent")
    private BigDecimal powApparent;


    /**
     * 功率因素
     */
    @JsonProperty("power_factor")
    private BigDecimal powerFactor;
}
