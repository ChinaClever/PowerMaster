package cn.iocoder.yudao.module.system.controller.admin.alarm.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 邮箱配置创建/修改 Request VO")
@Data
public class ConfigSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Integer id;

    @Schema(description = "发送邮箱开关", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer mailAlarm;


    @Schema(description = "告警声音开关", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer voiceAlarm;

}
