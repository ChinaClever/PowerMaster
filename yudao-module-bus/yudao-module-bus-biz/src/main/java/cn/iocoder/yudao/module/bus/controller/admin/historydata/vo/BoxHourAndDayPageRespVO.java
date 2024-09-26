package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 母线插接箱（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BoxHourAndDayPageRespVO {

    @ExcelProperty("母线名称")
    private String bus_name;

    @ExcelProperty("插接箱名称")
    private String box_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("IP地址")
    private String dev_key;


    @ExcelProperty("输出位")
    private Integer outlet_id;

    @ExcelProperty("记录时间")
    private String create_time;
    @NumberFormat("0.000")
    @ExcelProperty("平均有功功率(kW)")
    private Double pow_active_avg_value;

    @ExcelProperty("最大有功功率时间")
    private String pow_active_max_time;
    @NumberFormat("0.000")
    @ExcelProperty("最大有功功率(kW)")
    private Double pow_active_max_value;

    @ExcelProperty("最小有功功率时间")
    private String pow_active_min_time;
    @NumberFormat("0.000")
    @ExcelProperty("最小有功功率(kW)")
    private Double pow_active_min_value;
    @NumberFormat("0.000")
    @ExcelProperty("平均无功功率(kW)")
    private Double pow_reactive_avg_value;

    @ExcelProperty("最大无功功率时间")
    private String pow_reactive_max_time;
    @NumberFormat("0.000")
    @ExcelProperty("最大无功功率(kW)")
    private Double pow_reactive_max_value;

    @ExcelProperty("最小无功功率时间")
    private String pow_reactive_min_time;
    @NumberFormat("0.000")
    @ExcelProperty("最小无功功率(kW)")
    private Double pow_reactive_min_value;
    @NumberFormat("0.000")
    @ExcelProperty("平均视在功率(kVA)")
    private Double pow_apparent_avg_value;

    @ExcelProperty("最大视在功率时间")
    private String pow_apparent_max_time;
    @NumberFormat("0.000")
    @ExcelProperty("最大视在功率(kVA)")
    private Double pow_apparent_max_value;

    @ExcelProperty("最小视在功率时间")
    private String pow_apparent_min_time;
    @NumberFormat("0.000")
    @ExcelProperty("最小视在功率(kVA)")
    private Double pow_apparent_min_value;

    //回路
    @ExcelProperty("回路")
    private Integer loop_id;
    @NumberFormat("0.0")
    @ExcelProperty("平均电压(V)")
    private Double vol_avg_value;

    @ExcelProperty("最大电压时间")
    private String vol_max_time;
    @NumberFormat("0.0")
    @ExcelProperty("最大电压(V)")
    private Double vol_max_value;

    @ExcelProperty("最小电压时间")
    private String vol_min_time;
    @NumberFormat("0.0")
    @ExcelProperty("最小电压(V)")
    private Double vol_min_value;
    @NumberFormat("0.00")

    @ExcelProperty("平均电流(A)")
    private Double cur_avg_value;

    @ExcelProperty("最大电流时间")
    private String cur_max_time;
    @NumberFormat("0.00")

    @ExcelProperty("最大电流(A)")
    private Double cur_max_value;

    @ExcelProperty("最小电流时间")
    private String cur_min_time;
    @NumberFormat("0.00")

    @ExcelProperty("最小电流(A)")
    private Double cur_min_value;

    // 相
    @ExcelProperty("相")
    private Integer line_id;

    @ExcelProperty("平均电流谐波含量")
    private String cur_thd_avg_value;

    @ExcelProperty("最大电流谐波含量时间")
    private String cur_thd_max_time;

    @ExcelProperty("最大电流谐波含量")
    private String cur_thd_max_value;

    @ExcelProperty("最小电流谐波含量时间")
    private String cur_thd_min_time;

    @ExcelProperty("最小电流谐波含量")
    private String cur_thd_min_value;


}