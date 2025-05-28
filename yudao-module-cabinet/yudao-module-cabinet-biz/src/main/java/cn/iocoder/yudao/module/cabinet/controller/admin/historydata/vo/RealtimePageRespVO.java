package cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.excel.core.util.DataSourceConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机柜电力（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class RealtimePageRespVO {

    @ExcelProperty("位置")
    private String location;


    @ExcelProperty(value="保存策略",converter = DataSourceConverter.class)
    private Integer data_source;
    @ExcelProperty("记录时间")
    private String create_time;
    @ExcelProperty("总有功功率(kW)")
    @NumberFormat("0.000")
    private Double active_total;

    @ExcelProperty("总视在功率(kVA)")
    @NumberFormat("0.000")
    private Double apparent_total;

    @ExcelProperty("总无功功率(kVar)")
    @NumberFormat("0.000")
    private Double reactive_total;

    @ExcelProperty("总功率因素")
    @NumberFormat("0.00")
    private Double factor_total;

    @ExcelProperty("总负载率(%)")
    @NumberFormat("0")
    private Double load_rate;

    @ExcelProperty("A路有功功率(kW)")
    @NumberFormat("0.000")
    private Double active_a;

    @ExcelProperty("A路视在功率(kVA)")
    @NumberFormat("0.000")
    private Double apparent_a;

    @ExcelProperty("A路无功功率(kVar)")
    @NumberFormat("0.000")
    private Double reactive_a;

    @ExcelProperty("A路功率因素")
    @NumberFormat("0.00")
    private Double factor_a;

    @ExcelProperty("B路有功功率(kW)")
    @NumberFormat("0.000")
    private Double active_b;

    @ExcelProperty("B路视在功率(kVA)")
    @NumberFormat("0.000")
    private Double apparent_b;

    @ExcelProperty("B路无功功率(kVar)")
    @NumberFormat("0.000")
    private Double reactive_b;

    @ExcelProperty("B路功率因素")
    @NumberFormat("0.00")
    private Double factor_b;

}