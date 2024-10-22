package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
    private List<Double> outletApparentPowColor;

    @Schema(description = "插接位的功率因数")
    private List<Double> outletPowFactor;

    @Schema(description = "A的电流")
    private Double aCur;

    @Schema(description = "A的电流告警状态")
    private Integer aCurStatus;

    @Schema(description = "A的电流背景颜色")
    private String aCurColor;

    @Schema(description = "B的电流")
    private Double bCur;

    @Schema(description = "B的电流告警状态")
    private Integer bCurStatus;

    @Schema(description = "B的电流背景颜色")
    private String bCurColor;

    @Schema(description = "C的电流")
    private Double cCur;

    @Schema(description = "C的电流告警状态")
    private Integer cCurStatus;

    @Schema(description = "C的电流背景颜色")
    private String cCurColor;

    @Schema(description = "A的电压")
    private Double aVol;

    @Schema(description = "A的电压告警状态")
    private Integer aVolStatus;

    @Schema(description = "A的电压背景颜色")
    private String aVolColor;

    @Schema(description = "B的电压")
    private Double bVol;

    @Schema(description = "B的电压告警状态")
    private Integer bVolStatus;

    @Schema(description = "B的电压背景颜色")
    private String bVolColor;

    @Schema(description = "C的电压")
    private Double cVol;

    @Schema(description = "C的电压告警状态")
    private Integer cVolStatus;

    @Schema(description = "C的电压背景颜色")
    private String cVolColor;

    @Schema(description = "A的有功功率")
    private Double aActivePow;

    @Schema(description = "A的有功功率告警状态")
    private Integer aActivePowStatus;

    @Schema(description = "A的有功功率背景颜色")
    private String aActivePowColor;

    @Schema(description = "B的有功功率")
    private Double bActivePow;

    @Schema(description = "B的有功功率告警状态")
    private Integer bActivePowStatus;

    @Schema(description = "B的有功功率背景颜色")
    private String bActivePowColor;

    @Schema(description = "C的有功功率")
    private Double cActivePow;

    @Schema(description = "C的有功功率告警状态")
    private Integer cActivePowStatus;

    @Schema(description = "C的有功功率背景颜色")
    private String cActivePowColor;

    @Schema(description = "A的无功功率")
    private Double aReactivePow;

    @Schema(description = "B的无功功率")
    private Double bReactivePow;

    @Schema(description = "B的无功功率")
    private Double cReactivePow;

    @Schema(description = "负载率所在范围")
    private Integer color;

    @Schema(description = "总视在功率")
    private Double powActive;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

}