package cn.iocoder.yudao.module.alarm.controller.admin.cfgsms.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 告警短信接收人配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AlarmCfgSmsPageReqVO extends PageParam {

    @Schema(description = "是否可用 0 可用 1 不可用")
    private Integer isEnable;

    @Schema(description = "手机号")
    private String phoneNumber;

    @Schema(description = "描述")
    private String smsDesc;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}