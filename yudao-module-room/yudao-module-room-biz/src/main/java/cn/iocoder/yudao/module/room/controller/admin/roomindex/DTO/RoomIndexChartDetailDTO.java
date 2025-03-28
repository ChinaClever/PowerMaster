package cn.iocoder.yudao.module.room.controller.admin.roomindex.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RoomIndexChartDetailDTO {

    private Integer roomId;

    @Schema(description = "时间粒度 realtime  hour  SeventyHours  day")
    private String granularity;

    @Schema(description = "状态：1-pue/负载率 2 -功率曲线  0- 有功电能")
    private Integer flag;
}
