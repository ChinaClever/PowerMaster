package cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - pdu电量计费方式配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EqBillConfigPageReqVO extends PageParam {

    @Schema(description = "电费单价")
    private Double bill;

    @Schema(description = "计费方式 1固定计费 2 分段计费")
    private Integer billMode;

    @Schema(description = "计费时间段")
    private String billPeriod;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private String[] createTime;

}