package cn.iocoder.yudao.module.rack.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.excel.core.util.DateStringConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.Data;

/**
 * @author: jiangjinchi
 * @time: 2024/11/21 14:02
 */
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class RackDetailsExcelVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty(value = "记录时间", converter = DateStringConverter.class)
    private String create_time;
    @NumberFormat("0.000")
    @ExcelProperty("有功功率(kW)")
    private Double active_total;
    @NumberFormat("0.000")
    @ExcelProperty("视在功率(kVA)")
    private Double apparent_total;
}
