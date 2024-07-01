package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 插接箱环境（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BoxEnvHourAndDayPageRespVO {

    @ExcelProperty("母线名称")
    private String bus_name;

    @ExcelProperty("插接箱名称")
    private String box_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("IP地址")
    private String ip_addr;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("A路平均温度(℃)")
    private String tem_a_avg_value;

    @ExcelProperty("A路最高温度时间")
    private String tem_a_max_time;

    @ExcelProperty("A路最高温度(℃)")
    private String tem_a_max_value;

    @ExcelProperty("A路最低温度时间")
    private String tem_a_min_time;

    @ExcelProperty("A路最低温度(℃)")
    private String tem_a_min_value;

    @ExcelProperty("B路平均温度(℃)")
    private String tem_b_avg_value;

    @ExcelProperty("B路最高温度时间")
    private String tem_b_max_time;

    @ExcelProperty("B路最高温度(℃)")
    private String tem_b_max_value;

    @ExcelProperty("B路最低温度时间")
    private String tem_b_min_time;

    @ExcelProperty("B路最低温度(℃)")
    private String tem_b_min_value;

    @ExcelProperty("C路平均温度(℃)")
    private String tem_c_avg_value;

    @ExcelProperty("C路最高温度时间")
    private String tem_c_max_time;

    @ExcelProperty("C路最高温度(℃)")
    private String tem_c_max_value;

    @ExcelProperty("C路最低温度时间")
    private String tem_c_min_time;

    @ExcelProperty("C路最低温度(℃)")
    private String tem_c_min_value;

    @ExcelProperty("中线平均温度(℃)")
    private String tem_n_avg_value;

    @ExcelProperty("中线最高温度时间")
    private String tem_n_max_time;

    @ExcelProperty("中线最高温度(℃)")
    private String tem_n_max_value;

    @ExcelProperty("中线最低温度时间")
    private String tem_n_min_time;

    @ExcelProperty("中线最低温度(℃)")
    private String tem_n_min_value;

}