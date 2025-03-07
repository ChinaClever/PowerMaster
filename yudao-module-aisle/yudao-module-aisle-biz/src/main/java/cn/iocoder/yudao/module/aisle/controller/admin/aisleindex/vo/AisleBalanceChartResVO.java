package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AisleBalanceChartResVO extends AisleIndexRespVO{

    @Schema(description = "A路的视在功率占比")
    private Double rateA;

    @Schema(description = "B路的视在功率占比")
    private Double rateB;

    @Schema(description = "A路的有功功率占比")
    private Double powActiveARate;

    @Schema(description = "B路的有功功率占比")
    private Double powActiveBRate;

    @Schema(description = "a电流")
    private List<Double> curLista;

    @Schema(description = "a电压")
    private List<Double> volLista;

    @Schema(description = "b电流")
    private List<Double> curListb;

    @Schema(description = "b电压")
    private List<Double> volListb;



}
