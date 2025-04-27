package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class AislePowerFactorMaxResVO {

    /**
     * 总功率因素
     */
    @Schema(description = "总功率因素")
    @JsonProperty("factor_total")
    private float factorTotal;

    @Schema(description = "最大视在功率")
    private Double apparentMax;

    @Schema(description = "柜列id")
    @JsonProperty("aisle_id")
    private int aisleId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonProperty("create_time")
    private String createTime;

    /**
     * 机房id
     */
    @Schema(description = "机房id")
    private Integer roomId;

    /**
     * 柜列名称
     */
    @Schema(description = "柜列名称")
    private String aisleName;

    @Schema(description = "机房名称")
    private String roomName;
}
