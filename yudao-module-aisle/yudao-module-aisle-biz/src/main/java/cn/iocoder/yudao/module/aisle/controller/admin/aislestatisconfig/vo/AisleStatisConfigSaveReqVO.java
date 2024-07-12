package cn.iocoder.yudao.module.aisle.controller.admin.aislestatisconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 柜列计算服务配置新增/修改 Request VO")
@Data
public class AisleStatisConfigSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24861")
    private Integer id;

    @Schema(description = "计费方式 1固定计费 2分段计费", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计费方式 1固定计费 2分段计费不能为空")
    private Integer billMode;

    @Schema(description = "按天统计历史数据任务")
    private String dayCron;

    @Schema(description = "按小时统计历史数据任务")
    private String hourCron;

    @Schema(description = "电量按天统计任务")
    private String eqDayCron;

    @Schema(description = "电量按周执行任务")
    private String eqWeekCron;

    @Schema(description = "按月统计电量任务")
    private String eqMonthCron;

    @Schema(description = "存储任务")
    private String storeCron;

    @Schema(description = "redis key过期时间")
    private Integer redisExpire;

    @Schema(description = "电能存储任务")
    private String eleStoreCron;

    @Schema(description = "redis保存定时")
    private String redisCron;

    @Schema(description = "日用电告警开关 0 关 1开", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日用电告警开关 0 关 1开不能为空")
    private Integer eleAlarmDay;

    @Schema(description = "日用能限制", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日用能限制不能为空")
    private Double eleLimitDay;

    @Schema(description = "月用电告警开关 0关 1开", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "月用电告警开关 0关 1开不能为空")
    private Integer eleAlarmMonth;

    @Schema(description = "月用能限制", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "月用能限制不能为空")
    private Double eleLimitMonth;

}