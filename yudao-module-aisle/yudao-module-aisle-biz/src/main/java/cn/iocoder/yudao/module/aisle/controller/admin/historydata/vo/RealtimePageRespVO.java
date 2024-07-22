package cn.iocoder.yudao.module.aisle.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 柜列电力（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RealtimePageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("总有功功率(kW)")
    private Double active_total;

    @ExcelProperty("总视在功率(kVA)")
    private Double apparent_total;

    @ExcelProperty("总无功功率(kW)")
    private Double reactive_total;

    @ExcelProperty("总功率因素")
    private Double factor_total;

    @ExcelProperty("A路有功功率(kW)")
    private Double active_a;

    @ExcelProperty("A路视在功率(kVA)")
    private Double apparent_a;

    @ExcelProperty("A路无功功率(kW)")
    private Double reactive_a;

    @ExcelProperty("A路功率因素")
    private Double factor_a;

    @ExcelProperty("B路有功功率(kW)")
    private Double active_b;

    @ExcelProperty("B路视在功率(kVA)")
    private Double apparent_b;

    @ExcelProperty("B路无功功率(kW)")
    private Double reactive_b;

    @ExcelProperty("B路功率因素")
    private Double factor_b;

}