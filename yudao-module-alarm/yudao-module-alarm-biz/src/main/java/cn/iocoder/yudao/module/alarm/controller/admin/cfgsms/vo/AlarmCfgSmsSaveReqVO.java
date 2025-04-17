package cn.iocoder.yudao.module.alarm.controller.admin.cfgsms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 告警短信接收人配置新增/修改 Request VO")
@Data
public class AlarmCfgSmsSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "14218")
    private Integer id;

    @Schema(description = "是否可用 0 可用 1 不可用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否可用 0 可用 1 不可用不能为空")
    private Integer isEnable;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "手机号不能为空")
    private String phoneNumber;

    @Schema(description = "描述")
    private String smsDesc;

}