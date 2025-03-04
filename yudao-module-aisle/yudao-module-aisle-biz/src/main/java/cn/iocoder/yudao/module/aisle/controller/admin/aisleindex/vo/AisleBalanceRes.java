package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AisleBalanceRes extends AisleIndexRespVO{


    @Schema(description = "总共的视在功率")
    private Double powApparentTotal;

    @Schema(description = "总共的有功功率")
    private Double powActiveTotal;

    @Schema(description = "总共的无功功率")
    private Double powReactiveTotal;

    @Schema(description = "A路的视在功率")
    private Double powApparentA;

    @Schema(description = "A路的有功功率")
    private Double powActiveA;

    @Schema(description = "A路的无功功率")
    private Double powReactiveA;

    @Schema(description = "B路的视在功率")
    private Double powApparentB;

    @Schema(description = "B路的有功功率")
    private Double powActiveB;

    @Schema(description = "B路的无功功率")
    private Double powReactiveB;

    @Schema(description = "A路占比")
    private Double rateA;

    @Schema(description = "B路占比")
    private Double rateB;

    @Schema(description = "数据更新时间")
    private String dataUpdateTime;
}
