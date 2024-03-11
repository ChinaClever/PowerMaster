package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - PDU设备新增/修改 Request VO")
@Data
public class PDUDeviceSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7060")
    private Long id;

    @Schema(description = "设备唯一识别码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设备唯一识别码不能为空")
    private String devKey;

    @Schema(description = "IP地址")
    private String ipAddr;

    @Schema(description = "级联地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "级联地址不能为空")
    private Integer cascadeNum;

}