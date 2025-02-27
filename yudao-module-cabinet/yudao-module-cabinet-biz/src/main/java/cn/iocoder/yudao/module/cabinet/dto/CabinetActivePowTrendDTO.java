package cn.iocoder.yudao.module.cabinet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 有功功率趋势
 * @date 2024/4/30 11:23
 */
@Schema(description = "管理后台 - 机柜总有功功率趋势 Response VO")
@Data
public class CabinetActivePowTrendDTO {

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
