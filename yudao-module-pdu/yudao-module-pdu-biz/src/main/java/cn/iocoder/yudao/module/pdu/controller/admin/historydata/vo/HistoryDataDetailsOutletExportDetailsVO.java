package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

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
import org.apache.poi.ss.usermodel.HorizontalAlignment;

@Schema(description = "管理后台 - pdu历史数据详情 导出原始数据")
@Data
@ExcelIgnoreUnannotated
@HeadRowHeight(20)
@ColumnWidth(20)
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
public class HistoryDataDetailsOutletExportDetailsVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;
    @ExcelProperty({"电流(A)","平均值"})
    @NumberFormat("0.00")
    private Double cur_avg_value;
    @ExcelProperty({"电流(A)", "最大值"})
    @NumberFormat("0.00")
    private Double cur_max_value;
    @ExcelProperty({"电流(A)", "发生时间"})
    private String cur_max_time;
    @NumberFormat("0.00")
    @ExcelProperty({"电流(A)", "最小值"})
    private Double cur_min_value;
    @ExcelProperty({"电流(A)", "发生时间"})
    private String cur_min_time;
    @ExcelProperty({"有功功率(kW)","平均值"})
    private Double pow_active_avg_value;
    @ExcelProperty({"有功功率(kW)", "最大值"})
    @NumberFormat("0.000")
    private Double pow_active_max_value;
    @ExcelProperty({"有功功率(kW)", "发生时间"})
    private String pow_active_max_time;
    @ExcelProperty({"有功功率(kW)", "最小值"})
    @NumberFormat("0.000")
    private Double pow_active_min_value;
    @ExcelProperty({"有功功率(kW)", "发生时间"})
    private String pow_active_min_time;
    @ExcelProperty({"视在功率(kVA)","平均值"})
    @NumberFormat("0.000")
    private Double pow_apparent_avg_value;
    @ExcelProperty({"视在功率(kVA)", "最大值"})
    @NumberFormat("0.000")
    private Double pow_apparent_max_value;
    @ExcelProperty({"视在功率(kVA)", "发生时间"})
    private String pow_apparent_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"视在功率(kVA)", "最小值"})
    private Double pow_apparent_min_value;
    @ExcelProperty({"视在功率(kVA)", "发生时间"})
    private String pow_apparent_min_time;

    @ExcelProperty({"无功功率(kVar)","平均值"})
    @NumberFormat("0.000")
    private Double pow_reactive_avg_value;
    @ExcelProperty({"无功功率(kVar)", "最大值"})
    @NumberFormat("0.000")
    private Double pow_reactive_max_value;
    @ExcelProperty({"无功功率(kVar)", "发生时间"})
    private String pow_reactive_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"无功功率(kVar)", "最小值"})
    private Double pow_reactive_min_value;
    @ExcelProperty({"无功功率(kVar)", "发生时间"})
    private String pow_reactive_min_time;

    @ExcelProperty({"功率因素","平均值"})
    @NumberFormat("0.00")
    private Double power_factor_avg_value;
    @ExcelProperty({"功率因素", "最大值"})
    @NumberFormat("0.00")
    private Double power_factor_max_value;
    @ExcelProperty({"功率因素", "发生时间"})
    private String power_factor_max_time;
    @NumberFormat("0.00")
    @ExcelProperty({"功率因素", "最小值"})
    private Double power_factor_min_value;
    @ExcelProperty({"功率因素", "发生时间"})
    private String power_factor_min_time;
}
