package cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 机柜计算服务配置新增/修改 Request VO")
@Data
public class StatisConfigSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "13793")
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

    @Schema(description = "负载限制", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "负载限制不能为空")
    private Integer loadLimit;

    @Schema(description = "状态告警开关 0关 1开", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态告警开关 0关 1开不能为空")
    private Integer statusAlarm;

    @Schema(description = "存储任务")
    private String storeCron;

    @Schema(description = "告警任务")
    private String alarmCron;

    @Schema(description = "告警推送开关 0 关 1开", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "告警推送开关 0 关 1开不能为空")
    private Integer alarmPush;

    @Schema(description = "告警推送任务")
    private String alarmPushCron;

    @Schema(description = "推送mq配置")
    private String pushMqs;

    @Schema(description = "redis key过期时间")
    private Integer redisExpire;

    @Schema(description = "电能存储任务")
    private String eleStoreCron;

    @Schema(description = "定时推送任务")
    private String timingPushCron;

    @Schema(description = "定时推送开关 1开启 0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "定时推送开关 1开启 0关闭不能为空")
    private Integer timingPush;

    @Schema(description = "redis缓存任务")
    private String redisCron;

}