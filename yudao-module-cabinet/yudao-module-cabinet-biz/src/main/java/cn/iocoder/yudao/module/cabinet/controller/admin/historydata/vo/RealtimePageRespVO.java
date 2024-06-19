package cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机柜电力（实时数据） 导出数据")
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

    @ExcelProperty("A路有功功率(kW)")
    private Double active_a;

    @ExcelProperty("A路视在功率(kVA)")
    private Double apparent_a;

    @ExcelProperty("B路有功功率(kW)")
    private Double active_b;

    @ExcelProperty("B路视在功率(kVA)")
    private Double apparent_b;

}