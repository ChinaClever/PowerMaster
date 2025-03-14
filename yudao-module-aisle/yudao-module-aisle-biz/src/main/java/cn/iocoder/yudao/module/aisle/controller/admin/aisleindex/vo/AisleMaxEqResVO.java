package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AisleMaxEqResVO {
    @Schema(description = "id", example = "1")
    private int id;

    private double maxEq;

    /**
     * 机房id
     */
    @Schema(description = "机房id", example = "1")
    private Integer roomId;

    /**
     * 柜列名称
     */
    @Schema(description = "柜列名称", example = "柜列1")
    private String aisleName;

    @Schema(description = "机房名称")
    private String roomName;

    @Schema(description = "0-天 1-周  2-月")
    private Integer type;

}
