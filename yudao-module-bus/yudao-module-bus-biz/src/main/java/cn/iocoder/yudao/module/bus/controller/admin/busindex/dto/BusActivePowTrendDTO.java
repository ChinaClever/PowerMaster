package cn.iocoder.yudao.module.bus.controller.admin.busindex.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @author luowei
 * @version 1.0
 * @description: 有功功率趋势
 * @date 2024/4/30 11:23
 */
@Schema(description = "管理后台 - 始端箱总有功功率趋势 Response VO")
@Data
public class BusActivePowTrendDTO {

    /**
     * 时间
     */
    @Schema(description = "时间", example = "2024-05-06 00:00:00")
    private String dateTime;

    /**
     * 有功功率
     */
    @Schema(description = "有功功率")
    private String activePow;

    /**
     * 有功功率
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "有功功率最大时间")
    private Date activePowMaxTime;

    /**
     * 有功功率
     */    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "有功功率最小时间")
    private Date activePowMinTime;

    /**
     * 有功功率
     */
    @Schema(description = "有功功率最大")
    private String activePowMax;

    /**
     * 有功功率
     */
    @Schema(description = "有功功率最小")
    private String activePowMin;
}
