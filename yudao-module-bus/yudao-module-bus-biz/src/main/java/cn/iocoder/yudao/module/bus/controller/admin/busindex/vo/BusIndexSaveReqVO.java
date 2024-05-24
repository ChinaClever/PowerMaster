package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 始端箱索引新增/修改 Request VO")
@Data
public class BusIndexSaveReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "16387")
    private Long id;

    @Schema(description = "设备识别码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设备识别码不能为空")
    private String devKey;

    @Schema(description = "ip地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "ip地址不能为空")
    private String ipAddr;

    @Schema(description = "母线地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "母线地址不能为空")
    private String devAddr;

    @Schema(description = "母线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "16770")
    @NotNull(message = "母线编号不能为空")
    private Integer barId;

    @Schema(description = "运行状态 0：正常 1：预警 2：告警 3: 升级 4：故障 5：离线", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "运行状态 0：正常 1：预警 2：告警 3: 升级 4：故障 5：离线不能为空")
    private Integer runStatus;

    @Schema(description = "节点IP", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "节点IP不能为空")
    private String nodeIp;

    @Schema(description = "逻辑删除")
    private Integer isDeleted;

}