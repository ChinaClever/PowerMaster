package cn.iocoder.yudao.module.bus.controller.admin.busindex.dto;

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
public class BusTrendDTO {


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
    @Schema(description = "电流", example = "[]")
    private List<Map<String,Object>> cur;

    /**
     * B路电流
     */
    @Schema(description = "电压", example = "[]")
    private List<Map<String,Object>> vol;


}
