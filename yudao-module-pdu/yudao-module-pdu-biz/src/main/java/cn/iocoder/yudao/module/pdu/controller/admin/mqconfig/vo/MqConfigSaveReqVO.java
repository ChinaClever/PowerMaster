package cn.iocoder.yudao.module.pdu.controller.admin.mqconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 消息队列系统配置新增/修改 Request VO")
@Data
public class MqConfigSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11740")
    private Integer id;

    @Schema(description = "ip端口", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "ip端口不能为空")
    private String ipAndPorts;

    @Schema(description = "用户名", example = "王五")
    private String userName;

    @Schema(description = "密码")
    private String passWord;

    @Schema(description = "主题/分组")
    private String topic;

    @Schema(description = "mq名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "mq名称不能为空")
    private String mq;

}