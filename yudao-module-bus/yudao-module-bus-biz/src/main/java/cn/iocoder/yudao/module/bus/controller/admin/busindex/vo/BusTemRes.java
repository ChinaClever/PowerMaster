package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - 始端箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusTemRes  extends BusResBase {


    @Schema(description = "位置")
    private String location;

    @Schema(description = "A的温度")
    private Double aTem;

    @Schema(description = "A的温度告警状态")
    private Integer aTemStatus;

    @Schema(description = "A的温度背景颜色")
    private String aTemColor;

    @Schema(description = "B的温度")
    private Double bTem;

    @Schema(description = "B的温度告警状态")
    private Integer bTemStatus;

    @Schema(description = "B的温度背景颜色")
    private String bTemColor;

    @Schema(description = "C的温度")
    private Double cTem;

    @Schema(description = "C的温度告警状态")
    private Integer cTemStatus;

    @Schema(description = "C的温度背景颜色")
    private String cTemColor;

    @Schema(description = "N的温度")
    private Double nTem;

    @Schema(description = "N的温度告警状态")
    private Integer nTemStatus;

    @Schema(description = "N的温度背景颜色")
    private String nTemColor;
    
    @Schema(description = "负载率所在范围")
    private Integer color;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

}