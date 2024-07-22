package cn.iocoder.yudao.module.aisle.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 柜列电力（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class HourAndDayPageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("总平均有功功率(kW)")
    private Double active_total_avg_value;

    @ExcelProperty("总最大有功功率时间")
    private Double active_total_max_time;

    @ExcelProperty("总最大有功功率(kW)")
    private Double active_total_max_value;

    @ExcelProperty("总最小有功功率时间")
    private Double active_total_min_time;

    @ExcelProperty("总最小有功功率(kW)")
    private Double active_total_min_value;

    @ExcelProperty("总平均视在功率(kVA)")
    private String apparent_total_avg_value;

    @ExcelProperty("总最大视在功率时间")
    private String apparent_total_max_time;

    @ExcelProperty("总最大视在功率(kVA)")
    private String apparent_total_max_value;

    @ExcelProperty("总最小视在功率时间")
    private String apparent_total_min_time;

    @ExcelProperty("总最小视在功率(kVA)")
    private String apparent_total_min_value;

    @ExcelProperty("总平均无功功率(kW)")
    private Double reactive_total_avg_value;

    @ExcelProperty("总最大无功功率时间")
    private Double reactive_total_max_time;

    @ExcelProperty("总最大无功功率(kW)")
    private Double reactive_total_max_value;

    @ExcelProperty("总最小无功功率时间")
    private Double reactive_total_min_time;

    @ExcelProperty("总最小无功功率(kW)")
    private Double reactive_total_min_value;

    @ExcelProperty("总平均功率因素")
    private Double factor_total_avg_value;


    @ExcelProperty("A路平均有功功率(kW)")
    private Double active_a_avg_value;

    @ExcelProperty("A路最大有功功率时间")
    private Double active_a_max_time;

    @ExcelProperty("A路最大有功功率(kW)")
    private Double active_a_max_value;

    @ExcelProperty("A路最小有功功率时间")
    private Double active_a_min_time;

    @ExcelProperty("A路最小有功功率(kW)")
    private Double active_a_min_value;

    @ExcelProperty("A路平均视在功率(kVA)")
    private Double apparent_a_avg_value;

    @ExcelProperty("A路最大视在功率时间")
    private Double apparent_a_max_time;

    @ExcelProperty("A路最大视在功率(kVA)")
    private Double apparent_a_max_value;

    @ExcelProperty("A路最小视在功率时间")
    private Double apparent_a_min_time;

    @ExcelProperty("A路最小视在功率(kVA)")
    private Double apparent_a_min_value;

    @ExcelProperty("A路平均无功功率(kW)")
    private Double reactive_a_avg_value;

    @ExcelProperty("A路平均功率因素")
    private Double factor_a_avg_value;


    @ExcelProperty("B路平均有功功率(kW)")
    private Double active_b_avg_value;

    @ExcelProperty("B路最大有功功率时间")
    private Double active_b_max_time;

    @ExcelProperty("B路最大有功功率(kW)")
    private Double active_b_max_value;

    @ExcelProperty("B路最小有功功率时间")
    private Double active_b_min_time;

    @ExcelProperty("B路最小有功功率(kW)")
    private Double active_b_min_value;

    @ExcelProperty("B路平均视在功率(kVA)")
    private Double apparent_b_avg_value;

    @ExcelProperty("B路最大视在功率时间")
    private Double apparent_b_max_time;

    @ExcelProperty("B路最大视在功率(kVA)")
    private Double apparent_b_max_value;

    @ExcelProperty("B路最小视在功率时间")
    private Double apparent_b_min_time;

    @ExcelProperty("B路最小视在功率(kVA)")
    private Double apparent_b_min_value;

    @ExcelProperty("B路平均无功功率(kW)")
    private Double reactive_b_avg_value;

    @ExcelProperty("B路平均功率因素")
    private Double factor_b_avg_value;


}