package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: jiangjinchi
 * @time: 2024/11/28 10:34
 */
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class BusCurLinePageResVO {


    @Schema(description = "始端箱Id")
    @JsonProperty("bus_id")
    private Integer busId;

    @Schema(description = "位置")
    private String location;

    @ExcelProperty("设备识别码")
    @Schema(description = "设备识别码")
    private String devKey;

    @JsonProperty("create_time")
    @ExcelProperty("时间")
    private String createTime;

    /**
     * 相
     */
    @JsonProperty("line_id")
    private int lineId;

    @ExcelProperty("相")
    @Schema(description = "line名")
    private String line;

    /**
     * 最大电流
     */
    @ExcelProperty("最大电流")
    @JsonProperty("cur_max_value")
    @Schema(description = "最大电流")
    private BigDecimal curMaxValue;

    /**
     * 最大有功功率
     */
    @ExcelProperty("最大有功功率")
    @JsonProperty("pow_active_max_value")
    @Schema(description = "最大有功功率")
    private BigDecimal powActiveMaxValue;
}
