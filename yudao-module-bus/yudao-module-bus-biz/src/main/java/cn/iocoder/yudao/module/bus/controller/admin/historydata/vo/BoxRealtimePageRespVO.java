package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 母线插接箱（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BoxRealtimePageRespVO {

    @ExcelProperty("母线名称")
    private String bus_name;

    @ExcelProperty("插接箱名称")
    private String box_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("IP地址")
    private String dev_key;


    @ExcelProperty("相")
    private Integer line_id;

    @ExcelProperty("回路")
    private Integer loop_id;

    @ExcelProperty("输出位")
    private Integer outlet_id;

    @ExcelProperty("记录时间")
    private String create_time;

    @NumberFormat("0.000")
    @ExcelProperty("有功功率(kW)")
    private Double pow_active;
    @NumberFormat("0.000")
    @ExcelProperty("视在功率(kVA)")
    private Double pow_apparent;
    @NumberFormat("0.000")

    @ExcelProperty("无功功率(kW)")
    private Double pow_reactive;
    @NumberFormat("0.00")

    @ExcelProperty("功率因素")
    private Double power_factor;
    @NumberFormat("0.0")

    @ExcelProperty("电压(V)")
    private Double vol_value;
    @NumberFormat("0.00")

    @ExcelProperty("电流(A)")
    private Double cur_value;

    @ExcelProperty("电流谐波含量")
    private Double cur_thd;

    @ExcelProperty("负载率(%)")
    private Double load_rate;

}