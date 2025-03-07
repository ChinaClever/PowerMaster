package cn.iocoder.yudao.module.cabinet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author luowei
 * @version 1.0
 * @description: 能耗趋势返回
 * @date 2024/5/6 15:19
 */
@Schema(description = "管理后台 - 机柜pdu电流趋势 Response VO")
@Data
public class CabinetPduCurTrendDTO {


    /**
     * 时间
     */
    @Schema(description = "时间", example = "01:00")
    private String dateTime;

    @Schema(description = "A路 pdu key", example = "")
    private String aKey;

    @Schema(description = "B路 pdu key", example = "")
    private String bKey;

    /**
     * A路电流
     */
    @Schema(description = "A路电流", example = "[]")
    private List<Map<String, Object>> curA;

    /**
     * B路电流
     */
    @Schema(description = "B路电流", example = "[]")
    private List<Map<String, Object>> curB;


}
