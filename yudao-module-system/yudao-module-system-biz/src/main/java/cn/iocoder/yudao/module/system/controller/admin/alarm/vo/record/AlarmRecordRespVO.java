package cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Schema(description = "管理后台 - 告警记录 Response VO")
@Data
public class AlarmRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer id;

    @Schema(description = "设备位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "机房-柜列")
    private String devPosition;

    @Schema(description = "设备key", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String devKey;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String devName;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer devType;

    @Schema(description = "设备类型描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String devTypeDesc;

    @Schema(description = "告警类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Integer alarmType;

    @Schema(description = "告警类型描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private String alarmTypeDesc;

    @Schema(description = "告警描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxxx")
    private String alarmDesc;

    @Schema(description = "告警级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer alarmLevel;

    @Schema(description = "告警级别描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String alarmLevelDesc;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Date endTime;

    @Schema(description = "记录状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "记录状态描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private String statusDesc;

    @Schema(description = "结束原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxxx")
    private String finishReason;

    @Schema(description = "确认原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxxx")
    private String confirmReason;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @Schema(description = "修改时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
