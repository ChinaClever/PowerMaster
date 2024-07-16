package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AislePfRes extends AisleIndexRespVO{
    @Schema(description = "A路功率因素")
    private Double pfA;

    @Schema(description = "B路功率因素")
    private Double pfB;

    @Schema(description = "总功率因素")
    private Double pfTotal;

}
