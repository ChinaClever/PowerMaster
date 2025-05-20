package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

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

@Schema(description = "管理后台 - 母线始端箱(实时数据) 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class BoxHourAndDayExportDetailVO {
    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;
    @NumberFormat("0.000")
    @ExcelProperty({"有功功率(kW)","平均值"})
    private Double pow_active_avg_value;
    @NumberFormat("0.000")
    @ExcelProperty({"有功功率(kW)","最大值"})
    private Double pow_active_max_value;
    @ExcelProperty({"有功功率(kW)","发生时间"})
    private String pow_active_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"有功功率(kW)","最小值"})
    private Double pow_active_min_value;
    @ExcelProperty({"有功功率(kW)","发生时间"})
    private String pow_active_min_time;
    @NumberFormat("0.000")
    @ExcelProperty({"无功功率(kVar)","平均值"})
    private Double pow_reactive_avg_value;
    @NumberFormat("0.000")
    @ExcelProperty({"无功功率(kVar)","率最大值"})
    private Double pow_reactive_max_value;
    @ExcelProperty({"无功功率(kVar)","发生时间"})
    private String pow_reactive_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"无功功率(kVar)","最小值"})
    private Double pow_reactive_min_value;
    @ExcelProperty({"无功功率(kVar)","发生时间"})
    private String pow_reactive_min_time;
    @NumberFormat("0.000")
    @ExcelProperty({"平均视在功率(kVA)"})
    private Double pow_apparent_avg_value;
//    @NumberFormat("0.000")
//    @ExcelProperty({"视在功率(kVA)","平均值"})
//    private Double pow_apparent_avg_value;
//    @NumberFormat("0.000")
//    @ExcelProperty({"视在功率(kVA)","最大值"})
//    private Double pow_apparent_max_value;
//    @ExcelProperty({"视在功率(kVA)","发生时间"})
//    private String pow_apparent_max_time;
//    @NumberFormat("0.000")
//    @ExcelProperty({"视在功率(kVA)","最小值"})
//    private Double pow_apparent_min_value;
//    @ExcelProperty({"视在功率(kVA)","发生时间"})
//    private String pow_apparent_min_time;
}