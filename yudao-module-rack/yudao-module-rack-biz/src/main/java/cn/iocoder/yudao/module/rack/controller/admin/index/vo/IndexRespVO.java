package cn.iocoder.yudao.module.rack.controller.admin.index.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 机架索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IndexRespVO {

    @Schema(description = "机架id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30159")
    @ExcelProperty("机架id")
    private Integer id;

    @Schema(description = "机柜id", requiredMode = Schema.RequiredMode.REQUIRED, example = "10322")
    @ExcelProperty("机柜id")
    private Integer cabinetId;

    @Schema(description = "机房id", requiredMode = Schema.RequiredMode.REQUIRED, example = "19026")
    @ExcelProperty("机房id")
    private Integer roomId;

    @Schema(description = "U位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("U位名称")
    private String rackName;

    @Schema(description = "是否删除 0未删除 1已删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否删除 0未删除 1已删除")
    private Integer isDelete;

    @Schema(description = "A路PDU输出位")
    @ExcelProperty("A路PDU输出位")
    private String outletIdA;

    @Schema(description = "B路PDU输出位")
    @ExcelProperty("B路PDU输出位")
    private String outletIdB;

    @Schema(description = "所属公司")
    @ExcelProperty("所属公司")
    private String company;

    @Schema(description = "U位位置", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("U位位置")
    private Integer uAddress;

    @Schema(description = "U位高度", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("U位高度")
    private Integer uHeight;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "设备类型", example = "1")
    @ExcelProperty("设备类型")
    private String type;

}