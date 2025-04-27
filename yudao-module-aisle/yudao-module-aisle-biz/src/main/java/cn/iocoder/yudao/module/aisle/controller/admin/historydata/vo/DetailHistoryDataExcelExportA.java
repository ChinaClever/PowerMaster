package cn.iocoder.yudao.module.aisle.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机柜电力分析 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class DetailHistoryDataExcelExportA {
    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;
    @NumberFormat("0.000")
    @ExcelProperty("A路有功功率(kW)")
    private Double active_a;
    @NumberFormat("0.000")
    @ExcelProperty("A路视在功率(kW)")
    private Double apparent_a;
    @NumberFormat("0.000")
    @ExcelProperty("A路无功功率(kW)")
    private Double reactive_a;
    @NumberFormat("0.00")
    @ExcelProperty("A路功率因素")
    private Double factor_a;
}