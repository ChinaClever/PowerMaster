package cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor.vo;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 插接箱谐波颜色 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BoxHarmonicColorRespVO {
    @Schema(description = "自增id", requiredMode = Schema.RequiredMode.REQUIRED, example = "21173")
    @ExcelProperty("自增id")
    private Long id;

    @Schema(description = "第一个小于的范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("第一个小于的范围")
    private Integer rangeOne;

    @Schema(description = "第二个范围的最小值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("第二个范围的最小值")
    private Integer rangeTwo;

    @Schema(description = "第二个范围的最大值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("第二个范围的最大值")
    private Integer rangeThree;

    @Schema(description = "第三个大于的范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("第三个大于的范围")
    private Integer rangeFour;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
