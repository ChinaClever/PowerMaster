package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Schema(description = "管理后台 - 插接箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BoxRedisDataRes extends BoxResBase {

    @Schema(description = "位置")
    private String location;

    @Schema(description = "回路的电流")
    private List<Double> loopCur;

    @Schema(description = "回路电流的告警状态")
    private List<Integer> loopCurStatus;

    @Schema(description = "回路电流的背景颜色")
    private List<String> loopCurColor;

    @Schema(description = "回路的电压")
    private List<Double> loopVol;

    @Schema(description = "回路电压的告警状态")
    private List<Integer> loopVolStatus;

    @Schema(description = "回路电压的背景颜色")
    private List<String> loopVolColor;

    @Schema(description = "回路的有功功率")
    private List<Double> loopActivePow;

    @Schema(description = "回路有功功率的告警状态")
    private List<Integer> loopActivePowStatus;

    @Schema(description = "回路有功功率的背景颜色")
    private List<String> loopActivePowColor;

    @Schema(description = "回路的无功功率")
    private List<Double> loopReactivePow;


    @Schema(description = "插接位有功功率")
    private List<Double> outletActivePow;

    @Schema(description = "插接位的无功功率")
    private List<Double> outletReactivePow;

    @Schema(description = "插接位的视在功率")
    private List<Double> outletApparentPow;

    @Schema(description = "插接位的功率因数")
    private List<Double> outletPowFactor;

    @Schema(description = "相的电流")
    private List<Double> phaseCur;

    @Schema(description = "相的电压")
    private List<Double> phaseVol;

    @Schema(description = "相的有功功率")
    private List<Double> phaseActivePow;

    @Schema(description = "相的无功功率")
    private List<Double> phaseReactivePow;

    @Schema(description = "相的视在功率")
    private List<Double> phaseApparentPow;

    @Schema(description = "相的功率因数")
    private List<Double> phasePowFactor;

    @Schema(description = "负载率所在范围")
    private Integer color;

//    @Schema(description = "总视在功率")
//    private Double powActive;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

    @Schema(description = "总有功功率")
    private BigDecimal powActive; //总有功功率

    @Schema(description = "总视在功率")
    private BigDecimal  powApparent; //总视在功率

    @Schema(description = "总无功功率")
    private BigDecimal   powReactive; //总无功功率

}