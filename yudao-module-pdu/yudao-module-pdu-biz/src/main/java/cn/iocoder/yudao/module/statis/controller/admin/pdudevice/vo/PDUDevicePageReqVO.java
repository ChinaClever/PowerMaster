package cn.iocoder.yudao.module.statis.controller.admin.pdudevice.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

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

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "级联地址")
    private Integer cascadeNum;

    public String getDevKey(){
        return devKey;
    }

}