package cn.iocoder.yudao.module.room.controller.admin.historydata.vo;

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
 * @time: 2024/11/20 8:47
 */
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class RoomPowerAnalysisResBVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty(value ="记录时间")
    private String create_time;


    @NumberFormat("0.000")
    @ExcelProperty("B路有功功率(kW)")
    private Double active_b;

    @NumberFormat("0.000")
    @ExcelProperty("B路视在功率(kVA)")
    private Double apparent_b;

    @NumberFormat("0.000")
    @ExcelProperty("B路无功功率(kVar)")
    private Double reactive_b;

    @NumberFormat("0.00")
    @ExcelProperty("B路功率因素")
    private Double factor_b;

}
