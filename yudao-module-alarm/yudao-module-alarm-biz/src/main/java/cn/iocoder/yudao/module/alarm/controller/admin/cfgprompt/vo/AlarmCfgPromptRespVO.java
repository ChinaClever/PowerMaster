package cn.iocoder.yudao.module.alarm.controller.admin.cfgprompt.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 系统告警配置 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AlarmCfgPromptRespVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24543")
    @ExcelProperty("主键id")
    private Integer id;

    @Schema(description = " 0 未启用 1启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(" 0 未启用 1启用")
    private Boolean isEnable;

    @Schema(description = "告警提醒方式：1声音 2邮件 3短信 4MQ", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("告警提醒方式：1声音 2邮件 3短信 4MQ")
    private Integer promptType;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}