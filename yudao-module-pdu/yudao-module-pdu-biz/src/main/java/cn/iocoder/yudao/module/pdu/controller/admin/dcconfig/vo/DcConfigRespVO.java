package cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - pdu数据采集配置 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DcConfigRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "24868")
    @ExcelProperty("主键ID")
    private Short id;

    @Schema(description = "数据接收端口", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数据接收端口")
    private Integer receivePort;

    @Schema(description = "定时存储定时任务配置")
    @ExcelProperty("定时存储定时任务配置")
    private String fixStoreCron;

    @Schema(description = "变化存储定时任务配置")
    @ExcelProperty("变化存储定时任务配置")
    private String changeStoreCron;

    @Schema(description = "电能存储定时任务配置")
    @ExcelProperty("电能存储定时任务配置")
    private String eleStoreCron;

    @Schema(description = "总视在功率变化比", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("总视在功率变化比")
    private Integer powLimitRate;

    @Schema(description = "rediskey过期时间（分钟）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("rediskey过期时间（分钟）")
    private Integer redisExpire;

    @Schema(description = "离线告警时长（分钟）")
    @ExcelProperty("离线告警时长（分钟）")
    private Integer offLineDuration;

    @Schema(description = "离线告警开关（1开启 0关闭）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("离线告警开关（1开启 0关闭）")
    private Integer offLineAlarm;

    @Schema(description = "状态告警开关 1开启 0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("状态告警开关 1开启 0关闭")
    private Integer statusAlarm;

    @Schema(description = "定时推送任务配置")
    @ExcelProperty("定时推送任务配置")
    private String timingPushCron;

    @Schema(description = "变化推送任务配置")
    @ExcelProperty("变化推送任务配置")
    private String changePushCron;

    @Schema(description = "告警推送任务配置")
    @ExcelProperty("告警推送任务配置")
    private String alarmPushCron;

    @Schema(description = "定时推送开关 1开启 0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("定时推送开关 1开启 0关闭")
    private Integer timingPush;

    @Schema(description = "变化推送开关 1开启 0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("变化推送开关 1开启 0关闭")
    private Integer changePush;

    @Schema(description = "告警推送开关 1开启 0关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("告警推送开关 1开启 0关闭")
    private Integer alarmPush;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private String createTime;

    @Schema(description = "配置推送的mq")
    @ExcelProperty("配置推送的mq")
    private String pushMqs;

    @Schema(description = "定时任务开关 默认开 1   关0", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("定时任务开关 默认开 1   关0")
    private Integer fixStore;

    @Schema(description = "变化存储开关 默认开1 关0", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("变化存储开关 默认开1 关0")
    private Integer changeStore;

    @Schema(description = "电能存储开关 默认开1  关0", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("电能存储开关 默认开1  关0")
    private Integer eleStore;

    @Schema(description = "redis保存开关  开1 关0", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("redis保存开关  开1 关0")
    private Integer redisSwitch;

}