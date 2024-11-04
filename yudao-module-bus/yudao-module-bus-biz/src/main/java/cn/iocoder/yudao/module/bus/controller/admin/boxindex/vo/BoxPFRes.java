package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;


@Schema(description = "管理后台 - 始端箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BoxPFRes extends BoxResBase{

    @Schema(description = "位置")
    private String location;

    @Schema(description = "回路电压的背景颜色")
    private List<Double> phasePowFactor;

    @Schema(description = "回路电压的背景颜色")
    private List<Double> loopPowFactor;

    @Schema(description = "回路电压的背景颜色")
    private List<Double> outletPowFactor;

    @Schema(description = "总的功率因素")
    private Double totalPowFactor;
    
    @Schema(description = "负载率所在范围")
    private Integer color;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

}