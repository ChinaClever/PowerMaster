package cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 系统告警记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AlarmLogRecordPageReqVO extends PageParam {

    @Schema(description = "设备唯一标识")
    private String alarmKey;

    @Schema(description = "状态 0 未处理  1 挂起 2 确认 3 结束", example = "1")
    private List<Integer> alarmStatus;

    @Schema(description = "类型类型：1PDU离线 2PDU告警 3PDU预警 4母线告警报 5母线离线 6机柜容量", example = "2")
    private Integer alarmType;

    @Schema(description = "所在位置")
    private String alarmPosition;

    @Schema(description = "告警级别 1 一级  2 二级")
    private Integer alarmLevel;

    @Schema(description = "告警信息")
    private String alarmDesc;

    @Schema(description = "开始时间")
    private LocalDateTime[] startTime;

    @Schema(description = "结束时间")
    private LocalDateTime[] finishTime;

    @Schema(description = "结束原因 1自动恢复 2手动结束", example = "不香")
    private String finishReason;

    @Schema(description = "确认原因", example = "不喜欢")
    private String confirmReason;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

    private String likeName;


}