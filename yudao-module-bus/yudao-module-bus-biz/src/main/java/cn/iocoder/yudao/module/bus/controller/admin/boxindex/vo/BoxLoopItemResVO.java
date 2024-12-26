package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BoxLoopItemResVO {
    @Schema(description = "回路编号")
    private Integer loopId;

    @Schema(description = "回路-电流")
    private Double loopCurValue;

    @Schema(description = "回路-电压")
    private Double loopVolValue;

    @Schema(description = "回路-无功功率")
    private Double loopPowReactive;

    @Schema(description = "回路-视在功率")
    private Double loopPowApparent;

    @Schema(description = "回路-有功功率值")
    private Double loopPowValue;

    @Schema(description = "回路-功率因素")
    private Double loopPowerFactor;

    @Schema(description = "回路-电能")
    private Double loopEleActive;
}
