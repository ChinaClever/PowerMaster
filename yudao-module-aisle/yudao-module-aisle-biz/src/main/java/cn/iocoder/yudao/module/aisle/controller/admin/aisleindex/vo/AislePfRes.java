package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AislePfRes extends AisleIndexRespVO{
    @Schema(description = "A路的A相功率因素")
    private Double apfA;

    @Schema(description = "A路的B相功率因素")
    private Double bpfA;

    @Schema(description = "A路的C相功率因素")
    private Double cpfA;

    @Schema(description = "A路的总功率因素")
    private Double totalPfA;

    @Schema(description = "B路的A相功率因素")
    private Double apfB;

    @Schema(description = "B路的B相功率因素")
    private Double bpfB;

    @Schema(description = "B路的C相功率因素")
    private Double cpfB;

    @Schema(description = "B路的总功率因素")
    private Double totalPfB;
}
