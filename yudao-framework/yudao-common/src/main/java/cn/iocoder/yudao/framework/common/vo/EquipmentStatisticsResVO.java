package cn.iocoder.yudao.framework.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EquipmentStatisticsResVO {

    @Schema(description = "总数")
    private Integer total;
    @Schema(description = "在线")
    private Integer online;
    @Schema(description = "告警")
    private Integer offline;

    @Schema(description = "离线")
    private Integer alarm;
}
