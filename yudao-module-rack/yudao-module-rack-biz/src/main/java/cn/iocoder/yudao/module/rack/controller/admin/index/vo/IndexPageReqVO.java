package cn.iocoder.yudao.module.rack.controller.admin.index.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 机架索引分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IndexPageReqVO extends PageParam {

    @Schema(description = "机柜id", example = "10322")
    private Integer cabinetId;

    @Schema(description = "机房id", example = "19026")
    private Integer roomId;

    @Schema(description = "U位名称", example = "芋艿")
    private String rackName;

    @Schema(description = "是否删除 0未删除 1已删除")
    private Integer isDelete;

    @Schema(description = "A路PDU输出位")
    private String outletIdA;

    @Schema(description = "B路PDU输出位")
    private String outletIdB;

    @Schema(description = "所属公司")
    private String company;

    @Schema(description = "U位位置")
    private Integer uAddress;

    @Schema(description = "U位高度")
    private Integer uHeight;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "设备类型", example = "1")
    private String type;

}