package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BoxIndexMaxEqResVO extends BoxResBase{
    @Schema(description = "id", example = "1")
    private int id;

    private double maxEq;

}
