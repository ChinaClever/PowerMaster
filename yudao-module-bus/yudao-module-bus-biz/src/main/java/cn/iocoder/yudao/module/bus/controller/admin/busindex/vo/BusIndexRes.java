package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - 始端箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusIndexRes extends BusResBase {

    @Schema(description = "位置")
    private String location;

    @Schema(description = "A的负载率")
    private Double aLoadRate;

    @Schema(description = "B的负载率")
    private Double bLoadRate;

    @Schema(description = "C的负载率")
    private Double cLoadRate;

    @Schema(description = "负载率所在范围")
    private Integer color;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

}