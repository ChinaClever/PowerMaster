package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 通道列新增/修改 Request VO")
@Data
public class AisleIndexSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "4246")
    private Integer id;

    @Schema(description = "机房id", requiredMode = Schema.RequiredMode.REQUIRED, example = "757")
    @NotNull(message = "机房id不能为空")
    private Integer roomId;

    @Schema(description = "通道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "通道名称不能为空")
    private String name;

    @Schema(description = "数据来源")
    private Integer pduBar;

    @Schema(description = "是否删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否删除不能为空")
    private Integer isDelete;

    @Schema(description = "长度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "长度不能为空")
    private Integer length;

    @Schema(description = "柜列类型", example = "1")
    private String type;

}