package cn.iocoder.yudao.module.bus.controller.admin.busindex.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 能耗环比
 * @date 2024/5/6 15:22
 */
@Schema(description = "管理后台 - 始端箱能耗环比 Response VO")
@Data
public class BusEleChainDTO {


    /**
     * 今日用能
     */
    @Schema(description = "今日用能", example = "1")
    private Double todayEq;

    /**
     * 昨日用能
     */
    @Schema(description = "昨日用能", example = "1")
    private Double yesterdayEq;

    /**
     * 日环比
     */
    @Schema(description = "日环比", example = "1%")
    private String dayRate;

    /**
     * 当周用能
     */
    @Schema(description = "当周用能", example = "1")
    private Double thisWeekEq;

    /**
     * 上周用能
     */
    @Schema(description = "上周用能", example = "1")
    private Double lastWeekEq;

    /**
     * 周环比
     */
    @Schema(description = "周环比", example = "1%")
    private String weekRate;


    /**
     * 本月用能
     */
    @Schema(description = "本月用能", example = "1")
    private Double thisMonthEq;

    /**
     * 上月用能
     */
    @Schema(description = "上月用能", example = "1")
    private Double lastMonthEq;

    /**
     * 月环比
     */
    @Schema(description = "月环比", example = "1%")
    private String monthRate;

}
