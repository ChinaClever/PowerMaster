package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理后台 - 通道列电力 Response VO")
public class AislePowerRes extends  AisleIndexRespVO{

    @Schema(description = "总有功电能")
    private Double eleActiveTotal;

    @Schema(description = "总视在功率")
    private Double powApparentTotal;

    @Schema(description = "总有功功率")
    private Double powActiveTotal;

    @Schema(description = "总无功功率")
    private Double powReactiveTotal;

    @Schema(description = "A路有功电能")
    private Double eleActiveA;

    @Schema(description = "A路视在功率")
    private Double powApparentA;

    @Schema(description = "A路有功功率")
    private Double powActiveA;

    @Schema(description = "A路无功功率")
    private Double powReactiveA;

    @Schema(description = "B路有功电能")
    private Double eleActiveB;

    @Schema(description = "B路视在功率")
    private Double powApparentB;

    @Schema(description = "B路有功功率")
    private Double powActiveB;

    @Schema(description = "B路无功功率")
    private Double powReactiveB;
}
