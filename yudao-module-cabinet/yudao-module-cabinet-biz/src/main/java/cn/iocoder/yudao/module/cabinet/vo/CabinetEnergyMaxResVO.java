package cn.iocoder.yudao.module.cabinet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CabinetEnergyMaxResVO {

    @Schema(description = "机房名称")
    private String roomName;

    @Schema(description = "id")
    private Integer id;


    @Schema(description = "机柜名称")
    private String cabinetName;

    @Schema(description = "用能")
    private Double eqValue;

    private String location;

    private Integer type;
}
