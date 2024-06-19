package cn.iocoder.yudao.module.system.controller.admin.alarm.vo.sms;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 手机号分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SmsConfigPageReqVO extends PageParam {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15684560021")
    private String phone;

    @Schema(description = "是否启用" , requiredMode = Schema.RequiredMode.REQUIRED , example = "1")
    private Integer isEnable;

}
