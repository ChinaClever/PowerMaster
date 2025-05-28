package cn.iocoder.yudao.module.cabinet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 总有功功率数据
 * @date 2024/5/6 15:24
 */
@Schema(description = "管理后台 - 机柜有功功率趋势 Response VO")
@Data
public class CabinetMaxActivePowDTO {

    /**
     * 今日有功功率峰值
     */
    @Schema(description = "今日有功功率峰值", example = "1")
    private Float todayMax;

    /**
     * 今日有功功率峰值时间
     */
    @Schema(description = "今日有功功率峰值时间", example = "2024-05-07 01:00:00")
    private String todayMaxTime;
    /**
     * 昨日有功功率峰值
     */
    @Schema(description = "昨日有功功率峰值", example = "1")
    private Float yesterdayMax;

    /**
     * 昨日有功功率峰值时间
     */
    @Schema(description = "昨日有功功率峰值时间", example = "2024-05-07 01:00:00")
    private String yesterdayMaxTime;

    /**
     * 今日总有功功率趋势数据
     */
    @Schema(description = "今日总有功功率趋势数据", example = "[]")
    private List<CabinetMaxActivePowTrendDTO> todayList;

    /**
     * 昨日总有功功率趋势数据
     */
    @Schema(description = "昨日总有功功率趋势数据", example = "[]")
    private List<CabinetMaxActivePowTrendDTO> yesterdayList;

}
