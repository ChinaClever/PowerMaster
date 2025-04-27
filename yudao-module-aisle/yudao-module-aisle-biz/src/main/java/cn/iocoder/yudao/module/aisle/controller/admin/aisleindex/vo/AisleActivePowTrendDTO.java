package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 始端箱总有功功率趋势 Response VO")
@Data
public class AisleActivePowTrendDTO {

    /**
     * 时间
     */
    @Schema(description = "时间", example = "2024-05-06 00:00:00")
    private String dateTime;

    /**
     * 有功功率
     */
    @Schema(description = "有功功率", example = "0.11")
    private String activePow;
}