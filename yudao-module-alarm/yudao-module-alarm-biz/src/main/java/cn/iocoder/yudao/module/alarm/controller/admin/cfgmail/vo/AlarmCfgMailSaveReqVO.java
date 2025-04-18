package cn.iocoder.yudao.module.alarm.controller.admin.cfgmail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 告警邮件接收人配置新增/修改 Request VO")
@Data
public class AlarmCfgMailSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "17164")
    private Integer id;

    @Schema(description = "是否可用 0 可用 1 不可用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否可用 0 可用 1 不可用不能为空")
    private Integer isEnable;

    @Schema(description = "邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "邮箱地址不能为空")
    private String mailAddr;

    @Schema(description = "描述")
    private String mailDesc;

}