package cn.iocoder.yudao.module.aisle.controller.admin.aislestatisconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 柜列计算服务配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AisleStatisConfigPageReqVO extends PageParam {

    @Schema(description = "计费方式 1固定计费 2分段计费")
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

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "存储任务")
    private String storeCron;

    @Schema(description = "redis key过期时间")
    private Integer redisExpire;

    @Schema(description = "电能存储任务")
    private String eleStoreCron;

    @Schema(description = "redis保存定时")
    private String redisCron;

    @Schema(description = "日用电告警开关 0 关 1开")
    private Integer eleAlarmDay;

    @Schema(description = "日用能限制")
    private Double eleLimitDay;

    @Schema(description = "月用电告警开关 0关 1开")
    private Integer eleAlarmMonth;

    @Schema(description = "月用能限制")
    private Double eleLimitMonth;

}