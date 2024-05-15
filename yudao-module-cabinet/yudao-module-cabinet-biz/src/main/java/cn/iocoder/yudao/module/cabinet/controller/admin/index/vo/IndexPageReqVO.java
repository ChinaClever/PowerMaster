package cn.iocoder.yudao.module.cabinet.controller.admin.index.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 机柜索引分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IndexPageReqVO extends PageParam {

    @Schema(description = "机房id", example = "8955")
    private Integer roomId;

    @Schema(description = "机柜名称", example = "李四")
    private String name;

    @Schema(description = "通道编号", example = "23731")
    private Integer aisleId;

    @Schema(description = "电力容量")
    private Double powCapacity;

    @Schema(description = "机柜ID列表")
    private List<Integer> cabinetIds;

    @Schema(description = "数据来源 0：PDU 1：母线")
    private Integer pduBox;

    @Schema(description = "禁用 0：启用 1：禁用")
    private Integer isDisabled;

    @Schema(description = "是否删除 0未删除 1已删除")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "运行状态", example = "1")
    private Integer runStatus;

}