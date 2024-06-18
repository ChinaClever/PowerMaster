package cn.iocoder.yudao.module.bus.controller.admin.boxcurbalancecolor.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 插接箱不平衡度颜色分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BoxCurbalanceColorPageReqVO extends PageParam {

    @Schema(description = "第一个小于的范围")
    private Integer rangeOne;

    @Schema(description = "第二个范围的最小值")
    private Integer rangeTwo;

    @Schema(description = "第二个范围的最大值")
    private Integer rangeThree;

    @Schema(description = "第三个大于的范围")
    private Integer rangeFour;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}