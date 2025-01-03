package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BoxPowerDetailRedisResVO {
    @Schema(description = "温度")
    private List<Double> temValue;

    @Schema(description = "负载率")
    private Double loadFactor;

    @Schema(description = "负载率最大时间")
    private String loadFactorTime;

    @Schema(description = "电压均衡")
    private Double vub;

    @Schema(description = "电流均衡")
    private Double cub;

    @Schema(description = "当前时间")
    private String updateTime;

    @Schema(description = "设备编号")
    private String devKey;

    @Schema(description = "回路")
    private List<BoxLoopItemResVO> boxLoopItemResVO;

    @Schema(description = "总-功率因素")
    private Double totalPowerFactor;
    @Schema(description = "总-视在功率")
    private Double totalPowApparent;
    @Schema(description = "总-功率")
    private Double totalPowActive;
    @Schema(description = "总-无功功率")
    private Double totalPowReactive;
    @Schema(description = "总-总电能")
    private Double totalEleActive;

    @Schema(description = "相-电压")
    private List<Double> lineVolValue;
    @Schema(description = "相-电流")
    private List<Double> lineCurValue;
    @Schema(description = "相-功率")
    private List<Double> linePowActive;
    @Schema(description = "相-视在功率")
    private List<Double> linePowApparent;

    @Schema(description = "相-电流谐波含量")
    private List<Double> lineCurThd;

    @Schema(description = "相-无功功率")
    private List<Double> linePowReactive;
    @Schema(description = "相-负载率")
    private List<Double> lineLoadRate;
    @Schema(description = "相-电能")
    private List<Double> lineEleActive;
    @Schema(description = "相-无功电能")
    private List<Double> lineEleReactive;
    @Schema(description = "相-功率因素")
    private List<Double> linePowerFactor;

    @Schema(description = "输出-有功功率")
    private List<Double> outletPowActive;

    @Schema(description = "输出-视在功率")
    private List<Double> outletPowApparent;

    @Schema(description = "输出-无功功率")
    private List<Double> outletPowReactive;

    @Schema(description = "输出-电能")
    private List<Double> outletEleActive;

    @Schema(description = "输出-视在电能")
    private List<Double> outletEleApparent;

    @Schema(description = "输出-无功电能")
    private List<Double> outletEleReactive;

    @Schema(description = "输出-功率因素")
    private List<Double> outletPowerFactor;


    @Schema(description = "负载率曲线 - 时间")
    private List<String> day;

    @Schema(description = "负载率曲线 - a")
    private List<BigDecimal> factorA;

    @Schema(description = "负载率曲线 - b")
    private List<BigDecimal> factorB;

    @Schema(description = "负载率曲线 - C")
    private List<BigDecimal> factorC;

//    @Schema(description = "负载率曲线 - 总")
//    private List<BigDecimal> factorTotal;
}
