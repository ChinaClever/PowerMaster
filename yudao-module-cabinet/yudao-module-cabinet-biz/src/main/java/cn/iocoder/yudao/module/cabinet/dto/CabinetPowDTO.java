package cn.iocoder.yudao.module.cabinet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 实时曲线数据
 * @date 2024/4/30 11:23
 */
@Schema(description = "管理后台 - 机柜功率实时曲线 Response VO")
@Data
public class CabinetPowDTO {

    /**
     * 时间
     */
    @Schema(description = "时间", example = "2024-04-29 14:00:05")
    private String dateTime;

    /**
     * 有功功率
     */
    @Schema(description = "有功功率", example = "0.12")
    private float activePow;

    /**
     * 视在功率
     */
    @Schema(description = "视在功率", example = "2")
    private float apparentPow;
}
