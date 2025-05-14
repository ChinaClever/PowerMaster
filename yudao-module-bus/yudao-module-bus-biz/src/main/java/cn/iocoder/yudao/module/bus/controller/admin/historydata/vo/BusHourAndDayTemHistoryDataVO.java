package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

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

@Schema(description = "管理后台 - 始端箱环境历史数据（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class BusHourAndDayTemHistoryDataVO {
    @ExcelProperty("位置")
    private String location;
    @ExcelProperty("记录时间")
    private String create_time;
    @ExcelProperty({"A路温度(℃)","平均值"})
    private Integer tem_a_avg_value;
    @ExcelProperty({"A路温度(℃)","最高值"})
    private Integer tem_a_max_value;
    @ExcelProperty({"A路温度(℃)","发生时间"})
    private String tem_a_max_time;
    @ExcelProperty({"A路温度(℃)","最小值"})
    private Integer tem_a_min_value;
    @ExcelProperty({"A路温度(℃)","发生时间"})
    private String tem_a_min_time;
    @ExcelProperty({"B路温度(℃)","平均值"})
    private Integer tem_b_avg_value;
    @ExcelProperty({"B路温度(℃)","最大值"})
    private Integer tem_b_max_value;
    @ExcelProperty({"B路温度(℃)","发生时间"})
    private String tem_b_max_time;
    @ExcelProperty({"B路温度(℃)","最小值"})
    private Integer tem_b_min_value;
    @ExcelProperty({"B路温度(℃)","发生时间"})
    private String tem_b_min_time;
    @ExcelProperty({"C路温度(℃)","平均值"})
    private Integer tem_c_avg_value;
    @ExcelProperty({"C路温度(℃)","最大值"})
    private Integer tem_c_max_value;
    @ExcelProperty({"C路温度(℃)","发生时间"})
    private String tem_c_max_time;
    @ExcelProperty({"C路温度(℃)","最小值"})
    private Integer tem_c_min_value;
    @ExcelProperty({"C路温度(℃)","发生时间"})
    private String tem_c_min_time;
    @ExcelProperty({"中线温度(℃)","平均值"})
    private Integer tem_n_avg_value;
    @ExcelProperty({"中线温度(℃)","最大值"})
    private Integer tem_n_max_value;
    @ExcelProperty({"中线温度(℃)","发生时间"})
    private String tem_n_max_time;
    @ExcelProperty({"中线温度(℃)","最小值"})
    private Integer tem_n_min_value;
    @ExcelProperty({"中线温度(℃)","发生时间"})
    private String tem_n_min_time;

}