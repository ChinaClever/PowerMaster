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
public class HourAndDayDetailHistoryDataExcelExport {

    @ExcelProperty("记录时间")
    private String create_time;
    @NumberFormat("0.000")
    @ExcelProperty("总平均有功功率(kW)")
    private Double active_total_avg_value;
    @NumberFormat("0.000")
    @ExcelProperty({"总有功功率最大值(kW)","数值"})
    private Double active_total_max_value;
    @ExcelProperty({"总有功功率最大值(kW)","发生时间"})
    private String active_total_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"总有功功率最小值(kW)","数值"})
    private Double active_total_min_value;
    @ExcelProperty({"总有功功率最小值(kW)","发生时间"})
    private String active_total_min_time;
    @NumberFormat("0.000")
    @ExcelProperty("总平均视在功率(kVA)")
    private Double apparent_total_avg_value;;
    @NumberFormat("0.000")
    @ExcelProperty({"总视在功率最大值(kVA)","数值"})
    private Double apparent_total_max_value;
    @ExcelProperty({"总视在功率最大值(kVA)","发生时间"})
    private String apparent_total_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"总视在功率最小值(kVA)","数值"})
    private Double apparent_total_min_value;
    @ExcelProperty({"总视在功率最小值(kVA)","发生时间"})
    private String apparent_total_min_time;
    @NumberFormat("0.000")
    @ExcelProperty("总平均无功功率(kW)")
    private Double reactive_total_avg_value;
    @NumberFormat("0.000")
    @ExcelProperty({"总无功功率最大值(kW)","数值"})
    private Double reactive_total_max_value;
    @ExcelProperty({"总无功功率最大值(kW)","发生时间"})
    private String reactive_total_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"总无功功率最小值(kW)","数值"})
    private Double reactive_total_min_value;
    @ExcelProperty({"总无功功率最小值(kW)","发生时间"})
    private String reactive_total_min_time;
    @NumberFormat("0.00")
    @ExcelProperty("总功平均功率因素")
    private Double factor_total_avg_value;

}