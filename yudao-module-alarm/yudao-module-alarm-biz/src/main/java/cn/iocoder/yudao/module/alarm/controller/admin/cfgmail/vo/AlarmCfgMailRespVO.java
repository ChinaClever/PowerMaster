package cn.iocoder.yudao.module.alarm.controller.admin.cfgmail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 告警邮件接收人配置 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AlarmCfgMailRespVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "17164")
    @ExcelProperty("主键id")
    private Integer id;

    @Schema(description = "是否可用 0 可用 1 不可用", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否可用 0 可用 1 不可用")
    private Integer isEnable;

    @Schema(description = "邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("邮箱地址")
    private String mailAddr;

    @Schema(description = "描述")
    @ExcelProperty("描述")
    private String mailDesc;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}