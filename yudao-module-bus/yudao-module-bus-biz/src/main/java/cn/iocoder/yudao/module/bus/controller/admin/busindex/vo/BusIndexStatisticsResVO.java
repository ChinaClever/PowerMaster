package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BusIndexStatisticsResVO {
    @Schema(description = "正常")
  private Integer  normal;

  @Schema(description = "预警")
    private Integer   warn;

    @Schema(description = "告警")
    private Integer   alarm;
    @Schema(description = "离线")
    private Integer   offline;

    @Schema(description = "总数")
    private Integer   total;
}
