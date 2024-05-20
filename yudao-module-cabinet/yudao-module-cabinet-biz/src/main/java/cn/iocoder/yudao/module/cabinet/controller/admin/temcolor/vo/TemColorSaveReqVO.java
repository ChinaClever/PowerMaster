package cn.iocoder.yudao.module.cabinet.controller.admin.temcolor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 机柜温度颜色新增/修改 Request VO")
@Data
public class TemColorSaveReqVO {

    @Schema(description = "自增id", requiredMode = Schema.RequiredMode.REQUIRED, example = "22403")
    private Long id;

    @Schema(description = "温度范围最小值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "温度范围最小值不能为空")
    private Integer min;

    @Schema(description = "温度范围最大值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "温度范围最大值不能为空")
    private Integer max;

    @Schema(description = "温度范围对应的颜色", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "温度范围对应的颜色不能为空")
    private String color;

}