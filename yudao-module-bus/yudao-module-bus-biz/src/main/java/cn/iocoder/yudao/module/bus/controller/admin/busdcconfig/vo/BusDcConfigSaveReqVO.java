package cn.iocoder.yudao.module.bus.controller.admin.busdcconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 母线数据采集配置新增/修改 Request VO")
@Data
public class BusDcConfigSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1083")
    private Short id;

    @Schema(description = "数据接收端口", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据接收端口不能为空")
    private Integer receivePort;

    @Schema(description = "定时存储定时任务配置")
    private String fixStoreCron;

    @Schema(description = "变化存储定时任务配置")
    private String changeStoreCron;

    @Schema(description = "电能存储定时任务配置")
    private String eleStoreCron;

    @Schema(description = "总视在功率变化比", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "总视在功率变化比不能为空")
    private Integer powLimitRate;

    @Schema(description = "rediskey过期时间（秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "rediskey过期时间（秒）不能为空")
    private Integer redisExpire;

    @Schema(description = "离线告警时长（分钟）")
    private Integer offLineDuration;

    @Schema(description = "离线告警开关（1开启 0关闭）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "离线告警开关（1开启 0关闭）不能为空")
    private Integer offLineAlarm;

    @Schema(description = "状态告警开关 1开启 0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态告警开关 1开启 0关闭不能为空")
    private Integer statusAlarm;

    @Schema(description = "定时推送任务配置")
    private String timingPushCron;

    @Schema(description = "变化推送任务配置")
    private String changePushCron;

    @Schema(description = "告警推送任务配置")
    private String alarmPushCron;

    @Schema(description = "定时推送开关 1开启 0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "定时推送开关 1开启 0关闭不能为空")
    private Integer timingPush;

    @Schema(description = "变化推送开关 1开启 0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变化推送开关 1开启 0关闭不能为空")
    private Integer changePush;

    @Schema(description = "告警推送开关 1开启 0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "告警推送开关 1开启 0关闭不能为空")
    private Integer alarmPush;

    @Schema(description = "配置推送的mq")
    private String pushMqs;

    @Schema(description = "定时任务开关 默认开 1   关0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "定时任务开关 默认开 1   关0不能为空")
    private Integer fixStore;

    @Schema(description = "变化存储开关 默认开1 关0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "变化存储开关 默认开1 关0不能为空")
    private Integer changeStore;

    @Schema(description = "电能存储开关 默认开1  关0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "电能存储开关 默认开1  关0不能为空")
    private Integer eleStore;

    @Schema(description = "redis保存开关  开1 关0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "redis保存开关  开1 关0不能为空")
    private Integer redisSwitch;

}