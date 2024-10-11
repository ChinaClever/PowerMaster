package cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO;

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

@Schema(description = "管理后台 - 插接箱能耗排名 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class DetailsPageRespVO {

    @ExcelProperty({"开始电能","开始电能(kWh)"})
    @NumberFormat("0.0")
    private Double start_ele;
    @ExcelProperty({"开始电能","发生时间"})
    private String start_time;
    @ExcelProperty({"结束电能","结束电能(kWh)"})
    @NumberFormat("0.0")
    private Double end_ele;
    @ExcelProperty({"结束电能","发生时间"})
    private String end_time;
    @ExcelProperty({"耗电量","耗电量(kWh)"})
    @NumberFormat("0.0")
    private Double eq_value;
    @ExcelProperty({"耗电量","记录时间"})
    private String create_time;

}