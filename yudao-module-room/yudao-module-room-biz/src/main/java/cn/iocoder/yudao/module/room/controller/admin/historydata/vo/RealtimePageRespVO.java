package cn.iocoder.yudao.module.room.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.text.DecimalFormat;

@Schema(description = "管理后台 - 机房电力（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RealtimePageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("保存策略")
    private Integer data_source;

    @ExcelProperty("记录时间")
    private String create_time;

    @NumberFormat("0.000")
    @ExcelProperty(value = "总有功功率(kW)")
    private Double active_total;

    @NumberFormat("0.000")
    @ExcelProperty(value = "总视在功率(kVA)")
    private Double apparent_total;

    @NumberFormat("0.000")
    @ExcelProperty(value = "总无功功率(kVar)")
    private Double reactive_total;

    @NumberFormat("0.00")
    @ExcelProperty(value = "总功率因素")
    private Double factor_total;

    @NumberFormat("0.000")
    @ExcelProperty(value = "A路有功功率(kW)")
    private Double active_a;

    @NumberFormat("0.000")
    @ExcelProperty(value = "A路视在功率(kVA)")
    private Double apparent_a;

    @NumberFormat("0.000")
    @ExcelProperty(value = "A路无功功率(kVar)")
    private Double reactive_a;

    @NumberFormat("0.00")
    @ExcelProperty(value = "A路功率因素")
    private Double factor_a;

    @NumberFormat("0.000")
    @ExcelProperty(value = "B路有功功率(kW)")
    private Double active_b;

    @NumberFormat("0.000")
    @ExcelProperty(value = "B路视在功率(kVA)")
    private Double apparent_b;

    @NumberFormat("0.000")
    @ExcelProperty(value = "B路无功功率(kVar)")
    private Double reactive_b;

    @NumberFormat("0.00")
    @ExcelProperty(value = "B路功率因素")
    private Double factor_b;
}