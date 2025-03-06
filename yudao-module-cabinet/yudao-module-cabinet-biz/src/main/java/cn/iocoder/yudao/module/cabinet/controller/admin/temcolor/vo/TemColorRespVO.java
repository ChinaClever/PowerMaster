package cn.iocoder.yudao.module.cabinet.controller.admin.temcolor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 机柜温度颜色 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TemColorRespVO {

    @Schema(description = "自增id", requiredMode = Schema.RequiredMode.REQUIRED, example = "22403")
    @ExcelProperty("自增id")
    private Long id;

    @Schema(description = "温度范围最小值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("温度范围最小值")
    private Integer min;

    @Schema(description = "温度范围最大值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("温度范围最大值")
    private Integer max;

    @Schema(description = "温度范围对应的颜色", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("温度范围对应的颜色")
    private String color;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "热温度范围最小值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("热温度范围最小值")
    private Integer hotMin;

    @Schema(description = "热温度范围最大值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("热温度范围最大值")
    private Integer hotMax;

    @Schema(description = "热温度范围对应的颜色", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("热温度范围对应的颜色")
    private String hotColor;

}