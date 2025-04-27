package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

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

import java.util.List;

@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class AislePFTableRes {

    @ExcelProperty("总功率因素")
    @NumberFormat("0.0")
    private Float factorTotalAvgValue;

    @ExcelProperty("A路功率因素")
    @NumberFormat("0.0")
    private Float factorAAvgValue;

    @ExcelProperty("B路功率因素")
    @NumberFormat("0.0")
    private Float factorBAvgValue;

    @ExcelProperty("时间")
    private String time;

    @ExcelProperty("位置")
    @Schema(description = "位置")
    private String location;
}