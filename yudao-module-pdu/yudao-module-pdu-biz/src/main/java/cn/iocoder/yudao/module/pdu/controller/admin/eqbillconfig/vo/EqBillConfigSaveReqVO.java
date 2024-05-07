package cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import javax.validation.constraints.*;

@Schema(description = "管理后台 - pdu电量计费方式配置新增/修改 Request VO")
@Data
public class EqBillConfigSaveReqVO {

    @Schema(description = "主键id", requiredMode = Schema.RequiredMode.REQUIRED, example = "27418")
    private Integer id;

    @Schema(description = "电费单价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "电费单价不能为空")
    private Double bill;

    @Schema(description = "计费方式 1固定计费 2 分段计费", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计费方式 1固定计费 2 分段计费不能为空")
    private Integer billMode;

    @Schema(description = "计费时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "计费时间段不能为空")
    private String billPeriod;

    private LocalDateTime createTime;

}