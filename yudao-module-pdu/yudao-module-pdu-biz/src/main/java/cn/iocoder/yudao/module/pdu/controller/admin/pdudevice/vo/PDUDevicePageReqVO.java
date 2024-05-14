package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - PDU设备分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PDUDevicePageReqVO extends PageParam {

    @Schema(description = "设备唯一识别码")
    private String devKey;

    @Schema(description = "IP地址")
    private String ipAddr;

    @Schema(description = "状态")
    private List<Integer> status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "级联地址")
    private Integer cascadeNum;

    @Schema(description = "颜色")
    private List<Integer> color;

    @Schema(description = "机柜ID列表")
    private List<Integer> cabinetIds;

    @Schema(description = "时间类型")
    private Integer timeType;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime oldTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime newTime;

    public String getDevKey(){
        return devKey;
    }

}