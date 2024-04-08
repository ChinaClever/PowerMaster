package cn.iocoder.yudao.module.statis.controller.admin.historydata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.*;

@Schema(description = "管理后台 - pdu历史数据新增/修改 Request VO")
@Data
public class HistoryDataSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "pdu编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "pdu编号不能为空")
    private Long pduId;

    @Schema(description = "数据类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "数据类型不能为空")
    private String type;

    @Schema(description = "topic", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "topic不能为空")
    private String topic;

    @Schema(description = "indexes", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "indexes不能为空")
    private Long indexes;

    @Schema(description = "值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "值不能为空")
    private Double value;

}