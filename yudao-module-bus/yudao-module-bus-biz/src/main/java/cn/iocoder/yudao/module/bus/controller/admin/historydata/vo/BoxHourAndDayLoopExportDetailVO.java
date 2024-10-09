package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 母线始端箱(实时数据) 导出数据")
@Data
@ExcelIgnoreUnannotated
@ColumnWidth(20)
@HeadRowHeight(20)
public class BoxHourAndDayLoopExportDetailVO {

    @ExcelProperty("记录时间")
    private String create_time;
    @NumberFormat("0.00")
    @ExcelProperty("平均电流(A)")
    private Double cur_avg_value;
    @NumberFormat("0.00")
    @ExcelProperty({"电流最大值","数值"})
    private Double cur_max_value;
    @ExcelProperty({"电流最大值","发生时间"})
    private String cur_max_time;
    @NumberFormat("0.00")
    @ExcelProperty({"电流最小值","数值"})
    private Double cur_min_value;
    @ExcelProperty({"电流最小值","发生时间"})
    private String cur_min_time;

    @NumberFormat("0.0")
    @ExcelProperty("平均电压(V)")
    private Double vol_avg_value;
    @NumberFormat("0.0")
    @ExcelProperty({"电压最大值","数值"})
    private Double vol_max_value;
    @ExcelProperty({"电压最大值","发生时间"})
    private String vol_max_time;
    @NumberFormat("0.0")
    @ExcelProperty({"电压最小值","数值"})
    private Double vol_min_value;
    @ExcelProperty({"电压最小值","发生时间"})
    private String vol_min_time;

    @NumberFormat("0.000")
    @ExcelProperty("平均有功功率(kW)")
    private Double pow_active_avg_value;
    @NumberFormat("0.000")
    @ExcelProperty({"有功功率最大值","数值"})
    private Double pow_active_max_value;
    @ExcelProperty({"有功功率最大值","发生时间"})
    private String pow_active_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"有功功率最小值","数值"})
    private Double pow_active_min_value;
    @ExcelProperty({"有功功率最小值","发生时间"})
    private String pow_active_min_time;
    @NumberFormat("0.000")
    @ExcelProperty("平均无功功率(kW)")
    private Double pow_reactive_avg_value;
    @NumberFormat("0.000")
    @ExcelProperty({"无功功率最大值","数值"})
    private Double pow_reactive_max_value;
    @ExcelProperty({"无功功率最大值","发生时间"})
    private String pow_reactive_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"无功功率最小值","数值"})
    private Double pow_reactive_min_value;
    @ExcelProperty({"无功功率最小值","发生时间"})
    private String pow_reactive_min_time;
    @NumberFormat("0.000")
    @ExcelProperty("平均视在功率(kW)")
    private Double pow_apparent_avg_value;
    @NumberFormat("0.000")
    @ExcelProperty({"视在功率最大值","数值"})
    private Double pow_apparent_max_value;
    @ExcelProperty({"视在功率最大值","发生时间"})
    private String pow_apparent_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"视在功率最小值","数值"})
    private Double pow_apparent_min_value;
    @ExcelProperty({"视在功率最小值","发生时间"})
    private String pow_apparent_min_time;


}