package cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 告警记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AlarmRecordPageReqVO extends PageParam {

    @Schema(description = "设备key", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String devKey;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String devName;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer devType;

    @Schema(description = "告警类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Integer alarmType;

    @Schema(description = "告警级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer alarmLevel;

    @Schema(description = "记录状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Integer> status;

    @Schema(description = "设备位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String devPosition;
}
