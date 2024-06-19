package cn.iocoder.yudao.module.rack.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机架电力（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class HourAndDayPageRespVO {

    @ExcelProperty("机架名")
    private String rack_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("平均有功功率(kW)")
    private Double active_total_avg_value;

    @ExcelProperty("最大有功功率时间")
    private Double active_total_max_time;

    @ExcelProperty("最大有功功率(kW)")
    private Double active_total_max_value;

    @ExcelProperty("最小有功功率时间")
    private Double active_total_min_time;

    @ExcelProperty("最小有功功率(kW)")
    private Double active_total_min_value;

    @ExcelProperty("平均视在功率(kVA)")
    private String apparent_total_avg_value;

    @ExcelProperty("最大视在功率时间")
    private String apparent_total_max_time;

    @ExcelProperty("最大视在功率(kVA)")
    private String apparent_total_max_value;

    @ExcelProperty("最小视在功率时间")
    private String apparent_total_min_time;

    @ExcelProperty("最小视在功率(kVA)")
    private String apparent_total_min_value;

}