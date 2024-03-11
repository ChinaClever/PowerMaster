package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - pdu历史数据分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HistoryDataPageReqVO extends PageParam {

    @Schema(description = "编号", example = "13535")
    private Long id;

    @Schema(description = "pdu编号", example = "15383")
    private Long pduId;

    @Schema(description = "数据类型	", example = "2")
    private String type;

    @Schema(description = "topic")
    private String topic;

    @Schema(description = "indexes")
    private Long indexes;

    @Schema(description = "值")
    private Double value;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}