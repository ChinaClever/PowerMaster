package cn.iocoder.yudao.module.cabinet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CabinetLoadPageChartResVO {
//    	private BigDecimal eleActive;
    @Schema(description = "机柜id")
    private Integer cabinetId;

    @Schema(description = "负载率")
    private BigDecimal loadRateA;
    @Schema(description = "负载率")
    private BigDecimal loadRateB;
    @Schema(description = "负载率")
    private BigDecimal loadRateTotal;

    @Schema(description = "电流1")
    private Double curValue;
    @Schema(description = "电流2")
    private Double curValuell;
    @Schema(description = "电流3")
    private Double curValuelll;

    @Schema(description = "电压1")
    private Double volValue;
    @Schema(description = "电压2")
    private Double volValuell;
    @Schema(description = "电压3")
    private Double volValuelll;

    @Schema(description = "时间")
    private String createTime;

    @Schema(description = "有功功率A")
    private Double powActiveA;// 有功功率
    @Schema(description = "有功功率B")
    private Double powActiveB;
    @Schema(description = "有功功率总")
    private Double powActiveTotal;

    @Schema(description = "视在功率A")
    private Double powApparentA;	// 视在功率
    @Schema(description = "视在功率B")
    private Double powApparentB;
    @Schema(description = "视在功率总")
    private Double powApparentTotal;

    @Schema(description = "无功功率A")
    private Double powReactiveA;		// 无功功率
    @Schema(description = "无功功率B")
    private Double powReactiveB;
    @Schema(description = "无功功率总")
    private Double powReactiveTotal;

    @Schema(description = "功率因素A")
    private Double powerFactorA;		// 功率因素
    @Schema(description = "功率因素B")
    private Double powerFactorB;
    @Schema(description = "功率因素总")
    private Double powerFactorTotal;
}
