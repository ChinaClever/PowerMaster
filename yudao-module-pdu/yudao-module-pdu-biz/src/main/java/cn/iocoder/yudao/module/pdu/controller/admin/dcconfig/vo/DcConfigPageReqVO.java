package cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - pdu数据采集配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DcConfigPageReqVO extends PageParam {

    @Schema(description = "数据接收端口")
    private Integer receivePort;

    @Schema(description = "定时存储定时任务配置")
    private String fixStoreCron;

    @Schema(description = "变化存储定时任务配置")
    private String changeStoreCron;

    @Schema(description = "电能存储定时任务配置")
    private String eleStoreCron;

    @Schema(description = "总视在功率变化比")
    private Integer powLimitRate;

    @Schema(description = "rediskey过期时间（分钟）")
    private Integer redisExpire;

    @Schema(description = "离线告警时长（分钟）")
    private Integer offLineDuration;

    @Schema(description = "离线告警开关（1开启 0关闭）")
    private Integer offLineAlarm;

    @Schema(description = "状态告警开关 1开启 0关闭")
    private Integer statusAlarm;

    @Schema(description = "定时推送任务配置")
    private String timingPushCron;

    @Schema(description = "变化推送任务配置")
    private String changePushCron;

    @Schema(description = "告警推送任务配置")
    private String alarmPushCron;

    @Schema(description = "定时推送开关 1开启 0关闭")
    private Integer timingPush;

    @Schema(description = "变化推送开关 1开启 0关闭")
    private Integer changePush;

    @Schema(description = "告警推送开关 1开启 0关闭")
    private Integer alarmPush;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] createTime;

    @Schema(description = "配置推送的mq")
    private String pushMqs;

    @Schema(description = "定时任务开关 默认开 1   关0")
    private Integer fixStore;

    @Schema(description = "变化存储开关 默认开1 关0")
    private Integer changeStore;

    @Schema(description = "电能存储开关 默认开1  关0")
    private Integer eleStore;

    @Schema(description = "redis保存开关  开1 关0")
    private Integer redisSwitch;

}