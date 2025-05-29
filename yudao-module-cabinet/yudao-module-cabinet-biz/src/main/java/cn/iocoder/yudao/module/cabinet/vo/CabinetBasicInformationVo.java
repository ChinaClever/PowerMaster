package cn.iocoder.yudao.module.cabinet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CabinetBasicInformationVo {

    @Schema(description = "机柜Id")
    private Integer cabinetId;

    @Schema(description = "运行状态")
    private Integer status;

    @Schema(description = "编号")
    private String devKey;

    @Schema(description = "机柜名")
    private String cabinetName;

    @Schema(description = "总有功功率")
    private Double powActive;

    @Schema(description = "总无功功率")
    private Double powReactive;

    @Schema(description = "总视在功率")
    private Double powApparent;

    @Schema(description = "总功率因数")
    private Double powerFactor;

    @Schema(description = "AB路占比")
    private Double proportion;

}
