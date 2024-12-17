package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

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
    private Integer LoadRateOne;

    @Schema(description = "30~60")
    private Integer LoadRateTwo;

    @Schema(description = "60~90")
    private Integer LoadRateThree;

    @Schema(description = ">90%")
    private Integer LoadRateFour;
}
