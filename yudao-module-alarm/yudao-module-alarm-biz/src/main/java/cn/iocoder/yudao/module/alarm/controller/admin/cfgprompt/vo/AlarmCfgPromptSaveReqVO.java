package cn.iocoder.yudao.module.alarm.controller.admin.cfgprompt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 系统告警配置新增/修改 Request VO")
@Data
public class AlarmCfgPromptSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24543")
    private Integer id;

    @Schema(description = " 0 未启用 1启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = " 0 未启用 1启用不能为空")
    private Boolean isEnable;

    @Schema(description = "告警提醒方式：1声音 2邮件 3短信 4MQ", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "告警提醒方式：1声音 2邮件 3短信 4MQ不能为空")
    private Integer promptType;


    @Schema(description = "发送邮箱开关", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer mailAlarm;

    @Schema(description = "告警声音开关", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer voiceAlarm;

    @Schema(description = "短信告警开关", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer smsAlarm;

}