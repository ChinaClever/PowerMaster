package cn.iocoder.yudao.framework.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CabinetCapacityStatisticsResVO {

    @Schema(description = "0-30%")
    private Integer thirty;
    @Schema(description = "30-50%")
    private Integer fifty;
    @Schema(description = "50-99%")
    private Integer ninetyNine;
    @Schema(description = "100%")
    private Integer oneHundred;

    @Schema(description = "总数")
    private Integer total;
}
