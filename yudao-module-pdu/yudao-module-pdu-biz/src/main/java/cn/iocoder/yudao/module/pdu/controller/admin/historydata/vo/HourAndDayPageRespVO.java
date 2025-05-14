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

@Schema(description = "管理后台 - pdu电力（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class HourAndDayPageRespVO {

    @ExcelProperty("所在位置")
    private String address;

    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;
//    @ExcelProperty("相")
//    private Integer line_id;
//
//    @ExcelProperty("回路")
//    private Integer loop_id;
//
//    @ExcelProperty("输出位")
//    private Integer outlet_id;


    @ExcelProperty("平均有功功率(kW)")
    @NumberFormat("0.000")
    private Double pow_active_avg_value;

    @ExcelProperty("最大有功功率(kW)")
    @NumberFormat("0.000")
    private Double pow_active_max_value;

    @ExcelProperty("最大有功功率时间")
    private String pow_active_max_time;

    @ExcelProperty("最小有功功率(kW)")
    @NumberFormat("0.000")
    private Double pow_active_min_value;

    @ExcelProperty("最小有功功率时间")
    private String pow_active_min_time;

    @ExcelProperty("平均视在功率(kVA)")
    @NumberFormat("0.000")
    private Double pow_apparent_avg_value;

    @ExcelProperty("最大视在功率(kVA)")
    @NumberFormat("0.000")
    private Double pow_apparent_max_value;

    @ExcelProperty("最大视在功率时间")
    private String pow_apparent_max_time;

    @ExcelProperty("最小视在功率(kVA)")
    @NumberFormat("0.000")
    private Double pow_apparent_min_value;

    @ExcelProperty("最小视在功率时间")
    private String pow_apparent_min_time;

    @ExcelProperty("平均无功功率(kVar)")
    @NumberFormat("0.000")
    private Double pow_reactive_avg_value;

    @ExcelProperty("最大无功功率(kVar)")
    @NumberFormat("0.000")
    private Double pow_reactive_max_value;

    @ExcelProperty("最大无功功率时间")
    private String pow_reactive_max_time;

    @ExcelProperty("最小无功功率(kVar)")
    @NumberFormat("0.000")
    private Double pow_reactive_min_value;

    @ExcelProperty("最小无功功率时间")
    private String pow_reactive_min_time;

    @ExcelProperty("平均功率因素")
    @NumberFormat("0.00")
    private Double power_factor_avg_value;

    @ExcelProperty("最大功率因素")
    @NumberFormat("0.00")
    private Double power_factor_max_value;

    @ExcelProperty("最大功率因素时间")
    private String power_factor_max_time;

    @ExcelProperty("最小功率因素")
    @NumberFormat("0.00")
    private Double power_factor_min_value;

    @ExcelProperty("最小功率因素时间")
    private String power_factor_min_time;
//
//    @ExcelProperty("平均电压(V)")
//    @NumberFormat("0.0")
//    private Double vol_avg_value;
//
//    @ExcelProperty("最大电压时间")
//    private String vol_max_time;
//
//    @ExcelProperty("最大电压(V)")
//    @NumberFormat("0.0")
//    private Double vol_max_value;
//
//    @ExcelProperty("最小电压时间")
//    private String vol_min_time;
//
//    @ExcelProperty("最小电压(V)")
//    private Double vol_min_value;
//
//    @ExcelProperty("平均电流(A)")
//    @NumberFormat("0.00")
//    private Double cur_avg_value;
//
//    @ExcelProperty("最大电流时间")
//    private String cur_max_time;
//
//    @ExcelProperty("最大电流(A)")
//    @NumberFormat("0.00")
//    private Double cur_max_value;
//
//    @ExcelProperty("最小电流时间")
//    private String cur_min_time;
//
//    @ExcelProperty("最小电流(A)")
//    @NumberFormat("0.00")
//    private Double cur_min_value;

}