package cn.iocoder.yudao.module.aisle.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.excel.core.util.DateStringConverter;
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

@Schema(description = "管理后台 - 柜列电费统计（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class DetailsExportPageRespVO {

    @ExcelProperty({"开始电能","数值"})
    @NumberFormat("0.0")
    private Double start_ele;
    @ExcelProperty(converter = DateStringConverter.class, value ={"开始电能","发生时间"})
    private String start_time;
    @ExcelProperty({"结束电能","数值"})
    @NumberFormat("0.0")
    private Double end_ele;
    @ExcelProperty(converter = DateStringConverter.class, value ={"结束电能","发生时间"})
    private String end_time;
    @ExcelProperty({"耗电量","数值"})
    @NumberFormat("0.0")
    private Double eq_value;
    @ExcelProperty(converter = DateStringConverter.class, value ={"耗电量","记录时间"})
    private String create_time;


}