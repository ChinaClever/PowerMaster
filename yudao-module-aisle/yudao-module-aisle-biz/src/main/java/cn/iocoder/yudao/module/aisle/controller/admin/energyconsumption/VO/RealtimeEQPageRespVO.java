package cn.iocoder.yudao.module.aisle.controller.admin.energyconsumption.VO;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 柜列（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RealtimeEQPageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录日期")
    private String create_time;

    @ExcelProperty("A路电能 (kWh)")
    private Double ele_a;

    @ExcelProperty("B路电能 (kWh)")
    private Double ele_b;

    @ExcelProperty("电能 (kWh)")
    private Double ele_total;

}