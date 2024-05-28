package cn.iocoder.yudao.module.bus.controller.admin.buscurbalancecolor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 母线不平衡度颜色新增/修改 Request VO")
@Data
public class BusCurbalanceColorSaveReqVO {

    @Schema(description = "自增id", requiredMode = Schema.RequiredMode.REQUIRED, example = "7534")
    private Long id;

    @Schema(description = "第一个小于的范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "第一个小于的范围不能为空")
    private Integer rangeOne;

    @Schema(description = "第二个范围的最小值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "第二个范围的最小值不能为空")
    private Integer rangeTwo;

    @Schema(description = "第二个范围的最大值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "第二个范围的最大值不能为空")
    private Integer rangeThree;

    @Schema(description = "第三个大于的范围", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "第三个大于的范围不能为空")
    private Integer rangeFour;

}