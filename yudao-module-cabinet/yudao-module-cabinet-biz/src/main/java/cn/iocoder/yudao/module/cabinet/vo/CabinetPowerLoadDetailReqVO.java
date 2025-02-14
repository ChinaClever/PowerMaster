package cn.iocoder.yudao.module.cabinet.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 母线电力负荷详情（电力负荷、功率数据） Request VO")
@Data
public class CabinetPowerLoadDetailReqVO {
    @Schema(description = "机柜id", required = true, example = "1")
    @NotNull(message = "机柜不能为空")
    private Integer id;

    @Schema(description = "机房id", required = true, example = "1")
    @NotNull(message = "机房不能为空")
    private Integer roomId;

    @Schema(description = "电流 电压 = 1 ，其他为0")
    private Integer type;

    @Schema(description = "时间粒度")
    private String granularity;

}
