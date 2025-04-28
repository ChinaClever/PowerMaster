package cn.iocoder.yudao.module.room.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机房电力（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class HourAndDayPageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;

    @NumberFormat("0.000")
    @ExcelProperty({"总有功功率(kW)","平均值"})
    private Double active_total_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"总有功功率(kW)","最大值"})
    private Double active_total_max_value;

    @ExcelProperty({"总有功功率(kW)","发生时间"})
    private String active_total_max_time;

    @NumberFormat("0.000")
    @ExcelProperty({"总有功功率(kW)","最小值"})
    private Double active_total_min_value;

    @ExcelProperty({"总有功功率(kW)","发生时间"})
    private String active_total_min_time;

    @NumberFormat("0.000")
    @ExcelProperty({"总视在功率(kVA)","平均值"})
    private Double apparent_total_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"总视在功率(kVA)","最大值"})
    private Double apparent_total_max_value;

    @ExcelProperty({"总视在功率(kVA)","发生时间"})
    private String apparent_total_max_time;

    @NumberFormat("0.000")
    @ExcelProperty({"总视在功率(kVA)","最小值"})
    private Double apparent_total_min_value;

    @ExcelProperty({"总视在功率(kVA)","发生时间"})
    private String apparent_total_min_time;

    @NumberFormat("0.000")
    @ExcelProperty({"总无功功率(kVar)","平均值"})
    private Double reactive_total_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"总无功功率(kVar)","最大值"})
    private Double reactive_total_max_value;

    @ExcelProperty({"总无功功率(kVar)","发生时间"})
    private String reactive_total_max_time;

    @NumberFormat("0.000")
    @ExcelProperty({"总无功功率(kVar)","最小值"})
    private Double reactive_total_min_value;

    @ExcelProperty({"总无功功率(kVar)","发生时间"})
    private String reactive_total_min_time;

    @NumberFormat("0.00")
    @ExcelProperty("总平均功率因素")
    private Double factor_total_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"A路有功功率(kW)","平均值"})
    private Double active_a_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"A路有功功率(kW)","最大值"})
    private Double active_a_max_value;

    @ExcelProperty({"A路有功功率(kW)","发生时间"})
    private String active_a_max_time;

    @NumberFormat("0.000")
    @ExcelProperty({"A路有功功率(kW)","最小值"})
    private Double active_a_min_value;

    @ExcelProperty({"A路有功功率(kW)","发生时间"})
    private String active_a_min_time;

    @NumberFormat("0.000")
    @ExcelProperty({"A路视在功率(kVA)","平均值"})
    private Double apparent_a_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"A路视在功率(kVA)","最大值"})
    private Double apparent_a_max_value;

    @ExcelProperty({"A路视在功率(kVA)","发生时间"})
    private String apparent_a_max_time;

    @NumberFormat("0.000")
    @ExcelProperty({"A路视在功率(kVA)","最小值"})
    private Double apparent_a_min_value;

    @ExcelProperty({"A路视在功率(kVA)","发生时间"})
    private String apparent_a_min_time;

    @NumberFormat("0.000")
    @ExcelProperty({"A路平均无功功率(kVar)"})
    private Double reactive_a_avg_value;

    @NumberFormat("0.00")
    @ExcelProperty("A路平均功率因素")
    private Double factor_a_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"B路有功功率(kW)","平均值"})
    private Double active_b_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"B路有功功率(kW)","最大值"})
    private Double active_b_max_value;

    @ExcelProperty({"B路有功功率(kW)","发生时间"})
    private String active_b_max_time;

    @NumberFormat("0.000")
    @ExcelProperty({"B路有功功率(kW)","最小值"})
    private Double active_b_min_value;

    @ExcelProperty({"B路有功功率(kW)","发生时间"})
    private String active_b_min_time;

    @NumberFormat("0.000")
    @ExcelProperty({"B路视在功率(kVA)","平均值"})
    private Double apparent_b_avg_value;

    @NumberFormat("0.000")
    @ExcelProperty({"B路视在功率(kVA)","最大值"})
    private Double apparent_b_max_value;

    @ExcelProperty({"B路视在功率(kVA)","发生时间"})
    private String apparent_b_max_time;

    @NumberFormat("0.000")
    @ExcelProperty({"B路视在功率(kVA)","最小值"})
    private Double apparent_b_min_value;

    @ExcelProperty({"B路视在功率(kVA)","发生时间"})
    private String apparent_b_min_time;

    @NumberFormat("0.000")
    @ExcelProperty("B路平均无功功率(kVar)")
    private Double reactive_b_avg_value;

    @NumberFormat("0.00")
    @ExcelProperty("B路平均功率因素")
    private Double factor_b_avg_value;
}