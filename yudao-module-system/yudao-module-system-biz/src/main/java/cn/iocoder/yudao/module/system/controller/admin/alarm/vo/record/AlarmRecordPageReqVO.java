package cn.iocoder.yudao.module.system.controller.admin.alarm.vo.record;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 告警记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AlarmRecordPageReqVO extends PageParam {

    @Schema(description = "设备key", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String devKey;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String devName;

    /**
     *     ROOM(1, "机房"),
     *     AISLE(2, "通道"),
     *     CABINET(3, "机柜"),
     *     PDU(4, "pdu"),
     *     RACK(5, "机架"),
     *     BUS(6, "始端箱"),
     *     BOX(7, "插接箱"),
     */
    @Schema(description = "设备类型  1-机房,2-通道,3-机柜,4-pdu,5-机架,6-始端箱,7-插接箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer devType;

    @Schema(description = "告警类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "80")
    private Integer alarmType;

    @Schema(description = "告警级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer alarmLevel;

    @Schema(description = "记录状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Integer> status;

    @Schema(description = "设备位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String devPosition;

    private String likeName;

    private String a;

    @Schema(description = "开始时间")
    private String oldTime;

    @Schema(description = "结束时间")
    private String newTime;
}
