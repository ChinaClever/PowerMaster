package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - pdu历史数据 Response VO")
@Data
@ExcelIgnoreUnannotated
public class HistoryDataRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "pdu编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("pdu编号")
    private Long pduId;

    @Schema(description = "数据类型	", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数据类型	")
    private String type;

    @Schema(description = "topic", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("topic")
    private String topic;

    @Schema(description = "indexes", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("indexes")
    private Long indexes;

    @Schema(description = "值", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("值")
    private Double value;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}