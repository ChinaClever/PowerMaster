package cn.iocoder.yudao.module.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 能耗趋势返回
 * @date 2024/5/6 15:19
 */
@Schema(description = "管理后台 - 用能趋势 Response VO")
@Data
public class RoomEqDTO {


    /**
     * 时间
     */
    @Schema(description = "时间", example = "01:00")
    private String dateTime;

    /**
     * 能耗
     */
    @Schema(description = "电量", example = "0")
    private double eq;


}
