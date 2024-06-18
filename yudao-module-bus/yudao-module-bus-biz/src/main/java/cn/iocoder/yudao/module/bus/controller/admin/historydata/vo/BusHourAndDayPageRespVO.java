package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 母线始端箱（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BusHourAndDayPageRespVO {

    @ExcelProperty("母线名称")
    private String bus_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("IP地址")
    private String ip_addr;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("平均有功功率(kW)")
    private Double pow_active_avg_value;

    @ExcelProperty("最大有功功率时间")
    private Double pow_active_max_time;

    @ExcelProperty("最大有功功率(kW)")
    private Double pow_active_max_value;

    @ExcelProperty("最小有功功率时间")
    private Double pow_active_min_time;

    @ExcelProperty("最小有功功率(kW)")
    private Double pow_active_min_value;

    @ExcelProperty("平均无功功率(kW)")
    private Double pow_reactive_avg_value;

    @ExcelProperty("最大无功功率时间")
    private Double pow_reactive_max_time;

    @ExcelProperty("最大无功功率(kW)")
    private Double pow_reactive_max_value;

    @ExcelProperty("最小无功功率时间")
    private String pow_reactive_min_time;

    @ExcelProperty("最小无功功率(kW)")
    private String pow_reactive_min_value;

    @ExcelProperty("平均视在功率(kVA)")
    private String pow_apparent_avg_value;

    @ExcelProperty("最大视在功率时间")
    private String pow_apparent_max_time;

    @ExcelProperty("最大视在功率(kVA)")
    private String pow_apparent_max_value;

    @ExcelProperty("最小视在功率时间")
    private String pow_apparent_min_time;

    @ExcelProperty("最小视在功率(kVA)")
    private String pow_apparent_min_value;

    @ExcelProperty("平均剩余电流(A)")
    private String cur_residual_avg_value;

    @ExcelProperty("最大剩余电流时间")
    private String cur_residual_max_time;

    @ExcelProperty("最大剩余电流(A)")
    private String cur_residual_max_value;

    @ExcelProperty("最小剩余电流时间")
    private String cur_residual_min_time;

    @ExcelProperty("最小剩余电流(A)")
    private String cur_residual_min_value;

    @ExcelProperty("平均零线电流(A)")
    private String cur_zero_avg_value;

    @ExcelProperty("最大零线电流时间")
    private String cur_zero_max_time;

    @ExcelProperty("最大零线电流(A)")
    private String cur_zero_max_value;

    @ExcelProperty("最小零线电流时间")
    private String cur_zero_min_time;

    @ExcelProperty("最小零线电流(A)")
    private String cur_zero_min_value;

    // 相
    @ExcelProperty("相")
    private String line_id;

    @ExcelProperty("平均电压(V)")
    private String vol_avg_value;

    @ExcelProperty("最大电压时间")
    private String vol_max_time;

    @ExcelProperty("最大电压(V)")
    private String vol_max_value;

    @ExcelProperty("最小电压时间")
    private String vol_min_time;

    @ExcelProperty("最小电压(V)")
    private String vol_min_value;

    @ExcelProperty("平均电流(A)")
    private String cur_avg_value;

    @ExcelProperty("最大电流时间")
    private String cur_max_time;

    @ExcelProperty("最大电流(A)")
    private String cur_max_value;

    @ExcelProperty("最小电流时间")
    private String cur_min_time;

    @ExcelProperty("最小电流(A)")
    private String cur_min_value;

    @ExcelProperty("平均线电压(V)")
    private String vol_line_avg_value;

    @ExcelProperty("最大线电压时间")
    private String vol_line_max_time;

    @ExcelProperty("最大线电压(V)")
    private String vol_line_max_value;

    @ExcelProperty("最小线电压时间")
    private String vol_line_min_time;

    @ExcelProperty("最小线电压(V)")
    private String vol_line_min_value;

}