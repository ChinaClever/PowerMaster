package cn.iocoder.yudao.module.system.controller.admin.alarm.vo.sms;

import cn.iocoder.yudao.framework.common.validation.Telephone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 手机号配置创建/修改 Request VO")
@Data
public class SmsConfigSaveReqVO {

    @Schema(description = "编号", example = "1024")
    private Integer id;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15584562215")
    @NotNull(message = "手机号不能为空")
    @Telephone(message = "必须是 手机号 格式")
    private String phone;


    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "是否启用")
    private Integer isEnable;

    /**
     * 描述
     */
    @Schema(description = "描述",  example = "0")
    private String smsDesc;

}
