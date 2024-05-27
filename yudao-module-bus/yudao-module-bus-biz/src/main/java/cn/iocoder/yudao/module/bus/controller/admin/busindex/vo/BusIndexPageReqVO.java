package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 始端箱索引分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BusIndexPageReqVO extends PageParam {

    @Schema(description = "设备识别码")
    private String devKey;

    @Schema(description = "ip地址")
    private String ipAddr;

    @Schema(description = "母线地址")
    private String devAddr;

    @Schema(description = "母线编号", example = "16770")
    private Integer barId;

    @Schema(description = "运行状态 0：正常 1：预警 2：告警 3: 升级 4：故障 5：离线", example = "2")
    private Integer runStatus;

    @Schema(description = "节点IP")
    private String nodeIp;

    @Schema(description = "颜色")
    private List<Integer> color;

    @Schema(description = "逻辑删除")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}