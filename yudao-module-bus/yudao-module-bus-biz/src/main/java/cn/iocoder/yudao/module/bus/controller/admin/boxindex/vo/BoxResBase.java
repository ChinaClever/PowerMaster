package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BoxResBase {

    /**
     * 设备识别码
     */
    @Schema(description = "设备识别码")
    private String devKey;

    @Schema(description = "始端箱Id")
    private Integer boxId;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "插接箱名称")
    private String boxName;

    @Schema(description = "始端箱名称")
    private String busName;
}
