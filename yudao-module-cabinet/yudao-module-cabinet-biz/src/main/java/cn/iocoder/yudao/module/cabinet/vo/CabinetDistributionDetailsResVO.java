package cn.iocoder.yudao.module.cabinet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CabinetDistributionDetailsResVO {
    @Schema(description = "机柜名称")
    private String cabinetName;
    @Schema(description = "机房名称")
    private String roomName;

    @Schema(description = "a电流")
    private List<BigDecimal> curA;

    @Schema(description = "b电流")
    private List<BigDecimal> curB;

    @Schema(description = "a电压")
    private List<BigDecimal> volA;

    @Schema(description = "b电压")
    private List<BigDecimal> volB;

    @Schema(description = "机柜负载率")
    private BigDecimal loadFactor;

    @Schema(description = "A有功功率")
    private BigDecimal  powActiveA;

    @Schema(description = "A视在功率")
    private BigDecimal  powApparentA;

    @Schema(description = "A无功功率")
    private BigDecimal  powReactiveA;

    @Schema(description = "B有功功率")
    private BigDecimal  powActiveB;

    @Schema(description = "B视在功率")
    private BigDecimal  powApparentB;

    @Schema(description = "B无功功率")
    private BigDecimal  powReactiveB;

    @Schema(description = "总有功功率")
    private BigDecimal powActiveTotal;

    @Schema(description = "总视在功率")
    private BigDecimal powApparentTotal;

    @Schema(description = "总无功功率")
    private BigDecimal powReactiveTotal;

    @Schema(description = "功率因素")
    private BigDecimal powerFactor;

    @Schema(description = "A不平衡比例", example = "1.00")
    private BigDecimal aPow;

    @Schema(description = "B不平衡比例", example = "1.00")
    private BigDecimal bPow;

    @Schema(description = "时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

//    @Schema(description = "负载率曲线 - 时间")
//    private List<String> day;
//
//    @Schema(description = "负载率曲线 - a")
//    private List<BigDecimal> factorA;
//
//    @Schema(description = "负载率曲线 - b")
//    private List<BigDecimal> factorB;
//
//    @Schema(description = "负载率曲线 - 总")
//    private List<BigDecimal> factorTotal;

}
