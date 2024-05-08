package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 机柜索引 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IndexRespVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "16822")
    @ExcelProperty("主键id")
    private Integer id;

    @Schema(description = "机房id", requiredMode = Schema.RequiredMode.REQUIRED, example = "8955")
    @ExcelProperty("机房id")
    private Integer roomId;

    @Schema(description = "机柜名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("机柜名称")
    private String name;

    @Schema(description = "通道编号", example = "23731")
    @ExcelProperty("通道编号")
    private Integer aisleId;

    @Schema(description = "电力容量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("电力容量")
    private Double powCapacity;

    @Schema(description = "数据来源 0：PDU 1：母线", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数据来源 0：PDU 1：母线")
    private Integer pduBox;

    @Schema(description = "禁用 0：启用 1：禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("禁用 0：启用 1：禁用")
    private Integer isDisabled;

    @Schema(description = "是否删除 0未删除 1已删除", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否删除 0未删除 1已删除")
    private Integer isDeleted;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "运行状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("运行状态")
    private Integer runStatus;

}