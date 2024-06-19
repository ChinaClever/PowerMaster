package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - pdu环境历史数据（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class EnvRealtimePageRespVO {

    @ExcelProperty("位置")
    private String address;

    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("传感器")
    private Integer sensor_id;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("湿度(%RH)")
    private Double hum_value;

    @ExcelProperty("温度(℃)")
    private Double tem_value;

}