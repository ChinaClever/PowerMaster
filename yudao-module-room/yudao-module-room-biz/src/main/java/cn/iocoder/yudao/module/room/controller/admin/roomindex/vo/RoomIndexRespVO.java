package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 机房索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RoomIndexRespVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23348")
    @ExcelProperty("主键id")
    private Integer id;

    @Schema(description = "机房名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("机房名")
    private String name;

    @Schema(description = "位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String location;

    @Schema(description = "是否删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否删除")
    private Integer isDelete;

    @Schema(description = "电力容量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("电力容量")
    private Double powerCapacity;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "日用电告警开关 0 关 1开", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("日用电告警开关 0 关 1开")
    private Integer eleAlarmDay;

    @Schema(description = "日用能限制", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("日用能限制")
    private Double eleLimitDay;

    @Schema(description = "月用电告警开关 0关 1开", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("月用电告警开关 0关 1开")
    private Integer eleAlarmMonth;

    @Schema(description = "月用能限制", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("月用能限制")
    private Double eleLimitMonth;

}