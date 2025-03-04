package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MaxEqResVO {
    @Schema(description = "昨日电量ip")
    private String yesterdayBusKey;
    @Schema(description = "昨日电量位置")
    private String yesterdayLocal;
    /**
     * 上周电量
     */
    @Schema(description = "上周电量ip")
    private String lastWeekBusKey;
    @Schema(description = "上周电量位置")
    private String lastWeekLocal;
    /**
     * 上月电量
     */
    @Schema(description = "上月电量ip")
    private String lastMonthBusKey;

    @Schema(description = "上月电量位置")
    private String lastMonthLocal;
}
