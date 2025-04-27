package cn.iocoder.yudao.module.aisle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "柜列首页电力折线图入参")
@Data
public class AislePowerLoadDetailReqDTO {
    @Schema(description = "柜列id", required = true, example = "1")
    private Integer id;

    @Schema(description = "时间粒度 realtime  hour  SeventyHours  day")
    private String granularity;

    @Schema(description = "状态：0 -电流/电压，1-电能 2 -其他")
    private Integer flag;

}
