package cn.iocoder.yudao.module.bus.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoadRateStatus {
/*0：空载
1：<30%
2: 30~60
3：60~90
4：>90%*/
    @Schema(description = "空载")
    private Integer LoadRateZero;

    @Schema(description = "<30%")
    private Integer lessThirty;

    @Schema(description = "30~60")
    private Integer greaterThirty;

    @Schema(description = "60~90")
    private Integer greaterSixty;

    @Schema(description = ">90%")
    private Integer greaterNinety;

}
