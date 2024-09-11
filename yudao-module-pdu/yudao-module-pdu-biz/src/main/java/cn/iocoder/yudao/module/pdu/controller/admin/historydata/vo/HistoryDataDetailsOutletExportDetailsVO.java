package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - pdu历史数据详情 导出原始数据")
@Data
@ExcelIgnoreUnannotated
@HeadRowHeight(20)
@ColumnWidth(20)

public class HistoryDataDetailsOutletExportDetailsVO {
    @ExcelProperty("记录时间")
    private String create_time;
    @ExcelProperty("平均电流(A)")
    @NumberFormat("0.00")
    private Double cur_avg_value;
    @ExcelProperty({"电流最大值(A)", "数值"})
    @NumberFormat("0.00")
    private Double cur_max_value;
    @ExcelProperty({"电流最大值(A)", "发生时间"})
    private String cur_max_time;
    @NumberFormat("0.00")
    @ExcelProperty({"电流最小值(A)", "数值"})
    private Double cur_min_value;
    @ExcelProperty({"电流最小值(A)", "发生时间"})
    private String cur_min_time;
    @NumberFormat("0.000")
    @ExcelProperty("平均有功功率(kW)")
    private Double pow_active_avg_value;
    @ExcelProperty({"有功功率最大值(kW)", "数值"})
    @NumberFormat("0.000")
    private Double pow_active_max_value;
    @ExcelProperty({"有功功率最大值(kW)", "发生时间"})
    private String pow_active_max_time;
    @ExcelProperty({"有功功率最小值(kW)", "数值"})
    @NumberFormat("0.000")
    private Double pow_active_min_value;
    @ExcelProperty({"有功功率最小值(kW)", "发生时间"})
    private String pow_active_min_time;
    @ExcelProperty("平均视在功率(kVA)")
    @NumberFormat("0.000")
    private Double pow_apparent_avg_value;
    @ExcelProperty({"视在功率最大值(kVA)", "数值"})
    @NumberFormat("0.000")
    private Double pow_apparent_max_value;
    @ExcelProperty({"视在功率最大值(kVA)", "发生时间"})
    private String pow_apparent_max_time;
    @NumberFormat("0.000")
    @ExcelProperty({"视在功率最小值(kVA)", "数值"})
    private Double pow_apparent_min_value;
    @ExcelProperty({"视在功率最小值(kVA)", "发生时间"})
    private String pow_apparent_min_time;

}
