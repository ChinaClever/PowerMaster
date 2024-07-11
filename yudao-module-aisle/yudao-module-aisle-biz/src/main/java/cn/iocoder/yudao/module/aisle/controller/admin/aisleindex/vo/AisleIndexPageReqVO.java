package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 通道列分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AisleIndexPageReqVO extends PageParam {

    @Schema(description = "id", example = "757")
    private Integer id;

    @Schema(description = "机房id", example = "757")
    private Integer roomId;

    @Schema(description = "通道名称", example = "芋艿")
    private String name;

    @Schema(description = "数据来源")
    private Integer pduBar;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "长度")
    private Integer length;

    @Schema(description = "柜列类型", example = "1")
    private String type;

}