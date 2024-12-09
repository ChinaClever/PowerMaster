package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 机柜索引新增/修改 Request VO")
@Data
public class IndexSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "16822")
    private Integer id;

    @Schema(description = "机房id", requiredMode = Schema.RequiredMode.REQUIRED, example = "8955")
    @NotNull(message = "机房id不能为空")
    private Integer roomId;

    @Schema(description = "机柜名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "机柜名称不能为空")
    private String name;

    @Schema(description = "通道编号", example = "23731")
    private Integer aisleId;

    @Schema(description = "电力容量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "电力容量不能为空")
    private Double powCapacity;

    @Schema(description = "数据来源 0：PDU 1：母线", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据来源 0：PDU 1：母线不能为空")
    private Integer pduBox;

    @Schema(description = "禁用 0：启用 1：禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "禁用 0：启用 1：禁用不能为空")
    private Integer isDisabled;

    @Schema(description = "是否删除 0未删除 1已删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否删除 0未删除 1已删除不能为空")
    private Integer isDeleted;

    @Schema(description = "运行状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "运行状态不能为空")
    private Integer runStatus;

}