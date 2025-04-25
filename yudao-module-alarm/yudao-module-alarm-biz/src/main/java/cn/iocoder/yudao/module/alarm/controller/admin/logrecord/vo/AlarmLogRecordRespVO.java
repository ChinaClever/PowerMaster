package cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 系统告警记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AlarmLogRecordRespVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "6203")
    @ExcelProperty("主键id")
    private Integer id;

    @Schema(description = "设备唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("设备唯一标识")
    private String alarmKey;

    @Schema(description = "状态 0 未处理  1 挂起 2 确认 3 结束", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态 0 未处理  1 挂起 2 确认 3 结束")
    private Integer alarmStatus;

    private String alarmStatusDesc;

    @Schema(description = "类型类型：1PDU离线 2PDU告警 3PDU预警 4母线告警报 5母线离线 6机柜容量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("类型类型：1PDU离线 2PDU告警 3PDU预警 4母线告警报 5母线离线 6机柜容量")
    private Integer alarmType;

    private String alarmTypeDesc;

    @Schema(description = "所在位置")
    @ExcelProperty("所在位置")
    private String alarmPosition;

    @Schema(description = "告警级别 1 一级  2 二级", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("告警级别 1 一级  2 二级")
    private Integer alarmLevel;

    private String alarmLevelDesc;

    @Schema(description = "告警信息")
    @ExcelProperty("告警信息")
    private String alarmDesc;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @ExcelProperty("结束时间")
    private LocalDateTime finishTime;

    @Schema(description = "结束原因 1自动恢复 2手动结束", example = "不香")
    @ExcelProperty("结束原因 1自动恢复 2手动结束")
    private String finishReason;

    @Schema(description = "确认原因", example = "不喜欢")
    @ExcelProperty("确认原因")
    private String confirmReason;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "机房id")
    private Integer roomId;

}