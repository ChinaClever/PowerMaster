package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BusIndexMaxEqResVO extends BusResBase {
    @Schema(description = "id", example = "1")
    private int id;

    private double maxEq;

}
