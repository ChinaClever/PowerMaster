package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - 机房索引新增/修改 Request VO")
@Data
public class RoomIndexSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23348")
    private Integer id;

    @Schema(description = "机房名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "机房名不能为空")
    private String name;

    @Schema(description = "是否删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否删除不能为空")
    private Integer isDelete;

    @Schema(description = "电力容量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "电力容量不能为空")
    private Double powerCapacity;

    @Schema(description = "日用电告警开关 0 关 1开", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日用电告警开关 0 关 1开不能为空")
    private Integer eleAlarmDay;

    @Schema(description = "日用能限制", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "日用能限制不能为空")
    private Double eleLimitDay;

    @Schema(description = "月用电告警开关 0关 1开", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "月用电告警开关 0关 1开不能为空")
    private Integer eleAlarmMonth;

    @Schema(description = "月用能限制", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "月用能限制不能为空")
    private Double eleLimitMonth;

}