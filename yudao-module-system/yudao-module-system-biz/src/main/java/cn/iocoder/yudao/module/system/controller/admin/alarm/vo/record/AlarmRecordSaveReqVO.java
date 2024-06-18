package cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Schema(description = "管理后台 - 告警记录修改 Request VO")
@Data
public class AlarmRecordSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Integer id;

    @Schema(description = "记录状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "确认原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String confirmReason;

}
