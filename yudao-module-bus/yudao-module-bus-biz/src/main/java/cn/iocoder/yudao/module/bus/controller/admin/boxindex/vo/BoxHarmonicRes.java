package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - 始端箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BoxHarmonicRes extends BoxResBase{

    @Schema(description = "位置")
    private String location;

    @Schema(description = "A的电流谐波")
    private Double acurThd;

    @Schema(description = "B的电流谐波")
    private Double bcurThd;

    @Schema(description = "C的电流谐波")
    private Double ccurThd;

    @Schema(description = "A的电压谐波")
    private Double avolThd;

    @Schema(description = "B的电压谐波")
    private Double bvolThd;

    @Schema(description = "C的电压谐波")
    private Double cvolThd;

    @Schema(description = "负载率所在范围")
    private Integer color;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

}