package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 始端箱索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BusIndexRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "16387")
    @ExcelProperty("id")
    private Long id;

    @Schema(description = "设备识别码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("设备识别码")
    private String devKey;

    @Schema(description = "ip地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("ip地址")
    private String ipAddr;

    @Schema(description = "母线地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("母线地址")
    private String devAddr;

    @Schema(description = "母线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "16770")
    @ExcelProperty("母线编号")
    private Integer barId;

    @Schema(description = "运行状态 0：正常 1：预警 2：告警 3: 升级 4：故障 5：离线", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("运行状态 0：正常 1：预警 2：告警 3: 升级 4：故障 5：离线")
    private Integer runStatus;

    @Schema(description = "节点IP", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("节点IP")
    private String nodeIp;

    @Schema(description = "逻辑删除")
    @ExcelProperty("逻辑删除")
    private Integer isDeleted;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;



}