package cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.util.*;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - pdu电量计费方式配置 Response VO")
@Data
@ExcelIgnoreUnannotated
public class EqBillConfigRespVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "27418")
    @ExcelProperty("主键id")
    private Integer id;

    @Schema(description = "电费单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("电费单价")
    private Double bill;

    @Schema(description = "计费方式 1固定计费 2 分段计费", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计费方式 1固定计费 2 分段计费")
    private Integer billMode;

    @Schema(description = "计费时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计费时间段")
    private String billPeriod;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private String createTime;

}