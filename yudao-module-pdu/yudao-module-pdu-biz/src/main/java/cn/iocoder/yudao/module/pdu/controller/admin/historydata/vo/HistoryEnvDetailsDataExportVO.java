package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

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

@Schema(description = "管理后台 - pdu环境分析 导出原始数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class HistoryEnvDetailsDataExportVO {
    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;
    @NumberFormat("0.0")
    @ExcelProperty("平均温度℃")
    private Double tem_avg_value;
    @ExcelProperty({"温度最高值", "温度最高值℃"})
    @NumberFormat("0.0")
    private Double tem_max_value;
    @ExcelProperty({"温度最高值", "发生时间"})
    private String tem_max_time;
    @ExcelProperty({"温度最低值", "温度最低值℃"})
    @NumberFormat("0.0")
    private Double tem_min_value;
    @ExcelProperty({"温度最低值", "发生时间"})
    private String tem_min_time;
    @ExcelProperty("平均湿度(%RH)")
    @NumberFormat("0")
    private Double hum_avg_value;
    @ExcelProperty({"湿度最大值", "湿度最大值(%RH)"})
    @NumberFormat("0")
    private Double hum_max_value;
    @ExcelProperty({"湿度最大值", "发生时间"})
    private String hum_max_time;
    @NumberFormat("0")
    @ExcelProperty({"湿度最小值", "湿度最小值(%RH)"})
    private Double hum_min_value;
    @ExcelProperty({"湿度最小值", "发生时间"})
    private String hum_min_time;

}
