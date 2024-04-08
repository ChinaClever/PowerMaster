package cn.iocoder.yudao.module.statis.controller.admin.pdudevice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - PDU设备 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PDUDeviceRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7060")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "设备唯一识别码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("设备唯一识别码")
    private String devKey;

    @Schema(description = "IP地址")
    @ExcelProperty("IP地址")
    private String ipAddr;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "级联地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("级联地址")
    private Integer cascadeNum;

}