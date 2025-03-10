package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AisleLineMaxRes extends AisleIndexRespVO{
        //总
    private Float maxPowTotal;
    private String maxPowTotalTime;

    //有功功率a
    @Schema(description = "有功功率a")
    private Float maxPowA;
    @Schema(description = "有功功率a")
    private String maxPowATime;

    //有功功率b
    @Schema(description = "有功功率b")
    private Float maxPowB;
    @Schema(description = "有功功率b")
    private String maxPowBTime;

    //视在功率a
    @Schema(description = "视在功率a")
    private Float maxApparentA;
    @Schema(description = "视在功率a")
    private String maxApparentATime;

    //视在功率b
    @Schema(description = "视在功率b")
    private Float maxApparentB;
    @Schema(description = "视在功率b")
    private String maxApparentBTime;

}
