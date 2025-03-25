package cn.iocoder.yudao.module.room.controller.admin.roomindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RoomMaxEqResVO {
    @Schema(description = "机房id", example = "1")
    private int id;

    private double maxEq;

    @Schema(description = "机房名称")
    private String roomName;

    @Schema(description = "0-天 1-周  2-月")
    private Integer type;

}
