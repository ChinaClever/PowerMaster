package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - pdu环境（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class EnvHourAndDayPageRespVO {

    @ExcelProperty("位置")
    private String address;

    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("监测点")
    private Integer a;

    @ExcelProperty("传感器")
    private Integer sensor_id;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("平均湿度(%RH)")
    @NumberFormat("0.0")
    private Double hum_avg_value;

    @ExcelProperty("最大湿度时间")
    private String hum_max_time;

    @ExcelProperty("最大湿度(%RH)")
    private Double hum_max_value;

    @ExcelProperty("最小湿度时间")
    private String hum_min_time;

    @ExcelProperty("最小湿度(%RH)")
    private Double hum_min_value;

    @NumberFormat("0.0")
    @ExcelProperty("平均温度(℃)")
    private Double tem_avg_value;

    @ExcelProperty("最高温度时间")
    private String tem_max_time;

    @ExcelProperty("最高温度(℃)")
    private String tem_max_value;

    @ExcelProperty("最低温度时间")
    private String tem_min_time;

    @ExcelProperty("最低温度(℃)")
    private String tem_min_value;



}