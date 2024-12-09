package cn.iocoder.yudao.module.rack.controller.admin.index.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 机架索引新增/修改 Request VO")
@Data
public class IndexSaveReqVO {

    @Schema(description = "机架id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30159")
    private Integer id;

    @Schema(description = "机柜id", requiredMode = Schema.RequiredMode.REQUIRED, example = "10322")
    @NotNull(message = "机柜id不能为空")
    private Integer cabinetId;

    @Schema(description = "机房id", requiredMode = Schema.RequiredMode.REQUIRED, example = "19026")
    @NotNull(message = "机房id不能为空")
    private Integer roomId;

    @Schema(description = "U位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "U位名称不能为空")
    private String rackName;

    @Schema(description = "是否删除 0未删除 1已删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否删除 0未删除 1已删除不能为空")
    private Integer isDelete;

    @Schema(description = "A路PDU输出位")
    private String outletIdA;

    @Schema(description = "B路PDU输出位")
    private String outletIdB;

    @Schema(description = "所属公司")
    private String company;

    @Schema(description = "U位位置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "U位位置不能为空")
    private Integer uAddress;

    @Schema(description = "U位高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "U位高度不能为空")
    private Integer uHeight;

    @Schema(description = "设备类型", example = "1")
    private String type;

}