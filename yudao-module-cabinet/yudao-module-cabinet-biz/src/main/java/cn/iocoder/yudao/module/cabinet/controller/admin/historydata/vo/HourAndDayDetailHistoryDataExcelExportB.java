package cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo;

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
public class HourAndDayDetailHistoryDataExcelExportB {
    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;
    @NumberFormat("0.000")
    @ExcelProperty({"B路有功功率(kW)","平均值"})
    private Double active_b_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"B路有功功率(kW)","最大值"})
    private Double active_b_max_value;

    @ExcelProperty({"B路有功功率(kW)","发生时间"})
    private String active_b_max_time;

    @NumberFormat("0.000")
    @ExcelProperty({"B路有功功率(kW)","最小值"})
    private Double active_b_min_value;

    @ExcelProperty({"B路有功功率(kW)","发生时间"})
    private String active_b_min_time;

    @NumberFormat("0.000")
    @ExcelProperty({"B路视在功率(kVA)","平均值"})
    private Double apparent_b_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"B路视在功率(kVA)","最大值"})
    private Double apparent_b_max_value;

    @ExcelProperty({"B路视在功率(kVA)","发生时间"})
    private String apparent_b_max_time;

    @NumberFormat("0.000")
    @ExcelProperty({"B路视在功率(kVA)","最小值"})
    private Double apparent_b_min_value;

    @ExcelProperty({"B路视在功率(kVA)","发生时间"})
    private String apparent_b_min_time;

    @NumberFormat("0.000")
    @ExcelProperty("B路平均无功功率(kVar)")
    private Double reactive_b_avg_value;

    @NumberFormat("0.00")
    @ExcelProperty("B路平均功率因素")
    private Double factor_b_avg_value;

}