package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - pdu电力（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class HourAndDayPageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("网络地址")
    private String address;

    @ExcelProperty("相")
    private Integer line_id;

    @ExcelProperty("回路")
    private Integer loop_id;

    @ExcelProperty("输出位")
    private Integer outlet_id;

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

}