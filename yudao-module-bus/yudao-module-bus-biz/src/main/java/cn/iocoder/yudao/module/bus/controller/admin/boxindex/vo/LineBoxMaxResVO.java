package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: jiangjinchi
 * @time: 2024/12/3 15:08
 */
@Data
public class LineBoxMaxResVO {

    @Schema(description = "位置")
    private String location;

    @Schema(description = "网络地址")
    private String devKey;

    @Schema(description = "名称")
    private String busName;

    @JsonProperty("box_id")
    @Schema(description = "busid")
    private Integer busId;

    @JsonProperty("line_id")
    private Integer lineId;

    @Schema(description = "相位")
    private String lineName;

    @JsonProperty("create_time")
    @Schema(description = "时间")
    private String createTime;

    @JsonProperty("cur_max_value")
    @Schema(description = "最大电流")
    private String curMaxValue;

    @JsonProperty("pow_active_max_value")
    @Schema(description = "最大电流")
    private String powActiveMaxValue;
}
