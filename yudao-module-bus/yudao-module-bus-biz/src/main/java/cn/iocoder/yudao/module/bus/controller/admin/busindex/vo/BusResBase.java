package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 始端箱基础返回")
@Data
@ExcelIgnoreUnannotated
public class BusResBase {

    /**
     * 设备识别码
     */
    @Schema(description = "设备识别码")
    private String devKey;

    @Schema(description = "始端箱Id")
    private Integer busId;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "始端箱名称")
    private String busName;
}
