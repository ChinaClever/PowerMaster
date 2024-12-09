package cn.iocoder.yudao.module.cabinet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 能耗趋势返回
 * @date 2024/5/6 15:19
 */
@Schema(description = "管理后台 - 机柜用能趋势 Response VO")
@Data
public class CabinetEqTrendDTO {


    /**
     * 时间
     */
    @Schema(description = "时间", example = "01:00")
    private String dateTime;

    /**
     * 当前能耗
     */
    @Schema(description = "电量", example = "0")
    private double eq;

    /**
     * 同一时间上次电量
     */
    @Schema(description = "同一时间上次电量", example = "0")
    private double lastEq;

}
