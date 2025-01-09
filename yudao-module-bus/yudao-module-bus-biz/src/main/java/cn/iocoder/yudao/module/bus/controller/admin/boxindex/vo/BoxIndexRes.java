package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - 始端箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BoxIndexRes  extends BoxResBase{

    @Schema(description = "位置")
    private String location;

    @Schema(description = "A的负载率")
    private Integer aLoadRate;

    @Schema(description = "B的负载率")
    private Integer bLoadRate;

    @Schema(description = "C的负载率")
    private Integer cLoadRate;

    @Schema(description = "负载率所在范围")
    private Integer color;

    @Schema(description = "运行状态")
    private Integer status;

    private Integer loadRateStatus;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;

}