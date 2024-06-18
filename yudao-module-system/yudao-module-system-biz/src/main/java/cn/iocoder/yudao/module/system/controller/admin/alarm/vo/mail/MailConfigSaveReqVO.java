package cn.iocoder.yudao.module.system.controller.admin.alarm.vo.mail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 邮箱配置创建/修改 Request VO")
@Data
public class MailConfigSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Integer id;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudaoyuanma@123.com")
    @NotNull(message = "邮箱不能为空")
    @Email(message = "必须是 Email 格式")
    private String mail;


    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "是否启用")
    private Integer isEnable;

}
