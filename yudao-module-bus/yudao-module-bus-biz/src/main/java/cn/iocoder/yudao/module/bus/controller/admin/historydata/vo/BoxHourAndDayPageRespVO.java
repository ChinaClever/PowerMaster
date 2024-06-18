package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 母线插接箱 导出数据")
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
    private String ip_addr;

    @ExcelProperty("平均有功功率")
    private Double pow_active_avg_value;

    @ExcelProperty("最大有功功率时间")
    private Double pow_active_max_time;

    @ExcelProperty("最大有功功率")
    private Double pow_active_max_value;

    @ExcelProperty("最小有功功率时间")
    private Double pow_active_min_time;

    @ExcelProperty("最小有功功率")
    private Double pow_active_min_value;

    @ExcelProperty("平均无功功率")
    private Double pow_reactive_avg_value;

    @ExcelProperty("最大无功功率时间")
    private Double pow_reactive_max_time;

    @ExcelProperty("最大无功功率")
    private Double pow_reactive_max_value;

    @ExcelProperty("最小无功功率时间")
    private String pow_reactive_min_time;

    @ExcelProperty("最小无功功率")
    private String pow_reactive_min_value;

    @ExcelProperty("平均视在功率")
    private String pow_apparent_avg_value;

    @ExcelProperty("最大视在功率时间")
    private String pow_apparent_max_time;

    @ExcelProperty("最大视在功率")
    private String pow_apparent_max_value;

    @ExcelProperty("最小视在功率时间")
    private String pow_apparent_min_time;

    @ExcelProperty("最小视在功率")
    private String pow_apparent_min_value;

    @ExcelProperty("记录时间")
    private String create_time;


}