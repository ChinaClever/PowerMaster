package cn.iocoder.yudao.module.cabinet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CabinetLoadPageChartAllResVO {
//    	private BigDecimal eleActive;
    @Schema(description = "机柜id")
    private Integer cabinetId;

    @Schema(description = "负载率")
    private Double loadRateTotal;

    @Schema(description = "平均电流1")
    private Double curValue;
    @Schema(description = "最大电流1")
    private Double maxCurValue;
    @Schema(description = "最大电流1时间")
    private String maxCurValueTime;
    @Schema(description = "最小电流1")
    private Double minCurValue;
    @Schema(description = "最小电流1时间")
    private String minCurValueTime;

    @Schema(description = "平均电流2")
    private Double curValuell;
    @Schema(description = "最大电流2")
    private Double maxCurValuell;
    @Schema(description = "最大电流2时间")
    private String maxCurValuellTime;
    @Schema(description = "最小电流2")
    private Double minCurValuell;
    @Schema(description = "最小电流2时间")
    private String minCurValuellTime;

    @Schema(description = "电流3")
    private Double curValuelll;
    @Schema(description = "最大电流3")
    private Double maxCurValuelll;
    @Schema(description = "最大电流3时间")
    private String maxCurValuelllTime;
    @Schema(description = "最小电流3")
    private Double minCurValuelll;
    @Schema(description = "最小电流3时间")
    private String minCurValuelllTime;

    @Schema(description = "平均电压1")
    private Double volValue;
    @Schema(description = "最大电压1")
    private Double maxVolValue;
    @Schema(description = "最大电压1时间")
    private String maxVolValueTime;
    @Schema(description = "最小电压1")
    private Double minVolValue;
    @Schema(description = "最小电压1时间")
    private String minVolValueTime;
    @Schema(description = "平均电压2")
    private Double volValuell;
    @Schema(description = "最大电压2")
    private Double maxVolValuell;
    @Schema(description = "最大电压2时间")
    private String maxVolValuellTime;
    @Schema(description = "最小电压2")
    private Double minVolValuell;
    @Schema(description = "最小电压2时间")
    private String minVolValuellTime;
    @Schema(description = "电压3")
    private Double volValuelll;
    @Schema(description = "最大电压3")
    private Double maxVolValuelll;
    @Schema(description = "最大电压3时间")
    private String maxVolValuelllTime;
    @Schema(description = "最小电压3")
    private Double minVolValuelll;
    @Schema(description = "最小电压3时间")
    private String minVolValuelllTime;

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
