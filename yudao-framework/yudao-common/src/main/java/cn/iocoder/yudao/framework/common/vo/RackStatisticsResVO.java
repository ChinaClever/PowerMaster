package cn.iocoder.yudao.framework.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RackStatisticsResVO {
    @Schema(description = "未绑定", example = "1")
    private Integer unbound;
    @Schema(description = "开机", example = "1")
    private Integer powerOn;
    @Schema(description = "关机", example = "1")
    private Integer powerOff;
    @Schema(description = "总数", example = "1")
    private Integer total;
}
