package cn.iocoder.yudao.module.rack.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.excel.core.util.DateStringConverter;
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

@Schema(description = "管理后台 - pdu历史数据详情 导出原始数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)public class HistoryDataDetailsExportDetailsVO {
    @ExcelProperty("位置")
    private String location;
    @ExcelProperty(value = "记录时间", converter = DateStringConverter.class)
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
    private Double apparent_total_avg_value;

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


}
