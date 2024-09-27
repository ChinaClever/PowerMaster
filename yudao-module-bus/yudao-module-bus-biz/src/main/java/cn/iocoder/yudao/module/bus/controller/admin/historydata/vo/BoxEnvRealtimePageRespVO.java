package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 插接箱环境历史数据（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BoxEnvRealtimePageRespVO {

    @ExcelProperty("母线名称")
    private String bus_name;

    @ExcelProperty("插接箱名称")
    private String box_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("IP地址")
    private String dev_key;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("A路温度(℃)")
    private Double tem_a;

    @ExcelProperty("B路温度(℃)")
    private Double tem_b;

    @ExcelProperty("C路温度(℃)")
    private Double tem_c;

    @ExcelProperty("中线温度(℃)")
    private Double tem_n;
}