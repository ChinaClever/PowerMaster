package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoadRate {

    @Schema(description = "A的负载率")
    private Double aLoadRate;

    @Schema(description = "B的负载率")
    private Double bLoadRate;

    @Schema(description = "C的负载率")
    private Double cLoadRate;

    @Schema(description = "负载的类型")
    private Integer color;
}
