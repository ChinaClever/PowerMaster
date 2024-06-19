package cn.iocoder.yudao.module.bus.controller.admin.busstatisconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 母线计算服务配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BusStatisConfigPageReqVO extends PageParam {

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

}