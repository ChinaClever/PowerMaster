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
public class BoxHourAndDayLoopExportDetailVO {
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
    @ExcelProperty({"无功功率(kW)","平均值"})
    private Double pow_reactive_avg_value;
    @NumberFormat("0.000")
    @ExcelProperty({"无功功率(kW)","最大值"})
    private Double pow_reactive_max_value;
    @ExcelProperty({"无功功率(kW)","发生时间"})
    private String pow_reactive_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"无功功率(kW)","最小值"})
    private Double pow_reactive_min_value;
    @ExcelProperty({"无功功率(kW)","发生时间"})
    private String pow_reactive_min_time;
    @NumberFormat("0.000")
    @ExcelProperty({"视在功率(kW)","平均值"})
    private Double pow_apparent_avg_value;
    @NumberFormat("0.000")
    @ExcelProperty({"视在功率(kW)","最大值"})
    private Double pow_apparent_max_value;
    @ExcelProperty({"视在功率(kW)","发生时间"})
    private String pow_apparent_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"视在功率(kW)","最小值"})
    private Double pow_apparent_min_value;
    @ExcelProperty({"视在功率(kW)","发生时间"})
    private String pow_apparent_min_time;
    @NumberFormat("0.00")
    @ExcelProperty({"功率因素","平均值"})
    private Double power_factor_avg_value;
    @NumberFormat("0.00")
    @ExcelProperty({"功率因素","最大值"})
    private Double power_factor_max_value;
    @ExcelProperty({"功率因素","发生时间"})
    private String power_factor_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"功率因素","最小值"})
    private Double power_factor_min_value;
    @ExcelProperty({"功率因素","发生时间"})
    private String power_factor_min_time;


    @NumberFormat("0.00")
    @ExcelProperty({"电流(A)","平均值"})
    private Double cur_avg_value;
    @NumberFormat("0.00")
    @ExcelProperty({"电流(A)","最大值"})
    private Double cur_max_value;
    @ExcelProperty({"电流(A)","发生时间"})
    private String cur_max_time;
    @NumberFormat("0.00")
    @ExcelProperty({"电流(A)","最小值"})
    private Double cur_min_value;
    @ExcelProperty({"电流(A)","发生时间"})
    private String cur_min_time;

    @NumberFormat("0.0")
    @ExcelProperty({"电压(V)","平均值"})
    private Double vol_avg_value;
    @NumberFormat("0.0")
    @ExcelProperty({"电压(V)","最大值"})
    private Double vol_max_value;
    @ExcelProperty({"电压(V)","发生时间"})
    private String vol_max_time;
    @NumberFormat("0.0")
    @ExcelProperty({"电压(V)","最小值"})
    private Double vol_min_value;
    @ExcelProperty({"电压(V)","发生时间"})
    private String vol_min_time;

}