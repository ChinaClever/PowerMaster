package cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 系统告警记录新增 Request VO")
@Data
public class AlarmLogRecordSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "6203")
    private Integer id;

    private List<Integer> ids;

    @Schema(description = "设备唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设备唯一标识不能为空")
    private String alarmKey;

    @Schema(description = "状态 0 未处理  1 挂起 2 确认 3 结束", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态 0 未处理  1 挂起 2 确认 3 结束不能为空")
    private Integer alarmStatus;

    @Schema(description = "类型类型：1PDU离线 2PDU告警 3PDU预警 4母线告警报 5母线离线 6机柜容量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "类型类型：1PDU离线 2PDU告警 3PDU预警 4母线告警报 5母线离线 6机柜容量不能为空")
    private Integer alarmType;

    @Schema(description = "所在位置")
    private String alarmPosition;

    @Schema(description = "告警级别 1 一级  2 二级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "告警级别 1 一级  2 二级不能为空")
    private Integer alarmLevel;

    @Schema(description = "告警信息")
    private String alarmDesc;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime finishTime;

    @Schema(description = "结束原因 1自动恢复 2手动结束", example = "不香")
    private String finishReason;

    @Schema(description = "确认原因", example = "不喜欢")
    private String confirmReason;

}