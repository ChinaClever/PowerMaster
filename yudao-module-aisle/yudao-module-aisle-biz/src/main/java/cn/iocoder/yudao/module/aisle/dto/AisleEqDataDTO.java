package cn.iocoder.yudao.module.aisle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列主页面用能数据
 * @date 2024/6/21 15:49
 */
@Data
public class AisleEqDataDTO {


    /**
     * 上周电量
     */
    @Schema(description = "上周电量", example = "1")
    private Double lastWeekEq;

    /**
     * 上月电量
     */
    @Schema(description = "上月电量", example = "1")
    private Double lastMonthEq;

    /**
     * 今日电量
     */
    @Schema(description = "今日电量", example = "1")
    private Double todayEq;

    /**
     * 本周电量
     */
    @Schema(description = "本周电量", example = "1")
    private Double thisWeekEq;

    /**
     * 本月电量
     */
    @Schema(description = "本月电量", example = "1")
    private Double thisMonthEq;

    /**
     * 昨日用能
     */
    @Schema(description = "昨日用能", example = "1")
    private Double yesterdayEq;

    /**
     * 柜列iD
     */
    private Integer id;

}
