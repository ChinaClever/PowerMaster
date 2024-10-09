package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 始端箱环境历史数据（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
@ColumnWidth(20)
@HeadRowHeight(20)
public class BusHourAndDayTemHistoryDataVO {
    @ExcelProperty("记录时间")
    private String create_time;
    @NumberFormat("0.0")
    @ExcelProperty("A路平均温度(℃)")
    private Double tem_a_avg_value;
    @NumberFormat("0.0")
    @ExcelProperty({"A路温度最高值","数值"})
    private Double tem_a_max_value;
    @ExcelProperty({"A路温度最高值","发生时间"})
    private String tem_a_max_time;
    @NumberFormat("0.0")
    @ExcelProperty({"A路温度最低值","数值"})
    private Double tem_a_min_value;
    @ExcelProperty({"A路温度最低值","发生时间"})
    private String tem_a_min_time;
    @NumberFormat("0.0")
    @ExcelProperty("B路平均温度(℃)")
    private Double tem_b_avg_value;
    @NumberFormat("0.0")
    @ExcelProperty({"B路温度最高值","数值"})
    private Double tem_b_max_value;
    @ExcelProperty({"B路温度最高值","发生时间"})
    private String tem_b_max_time;
    @NumberFormat("0.0")
    @ExcelProperty({"B路温度最低值","数值"})
    private Double tem_b_min_value;
    @ExcelProperty({"B路温度最低值","发生时间"})
    private String tem_b_min_time;
    @NumberFormat("0.0")
    @ExcelProperty("C路平均温度(℃)")
    private Double tem_c_avg_value;
    @NumberFormat("0.0")
    @ExcelProperty({"C路温度最高值","数值"})
    private Double tem_c_max_value;
    @ExcelProperty({"C路温度最高值","发生时间"})
    private String tem_c_max_time;
    @NumberFormat("0.0")
    @ExcelProperty({"C路温度最低值","数值"})
    private Double tem_c_min_value;
    @ExcelProperty({"C路温度最低值","发生时间"})
    private String tem_c_min_time;
    @NumberFormat("0.0")
    @ExcelProperty("中线平均温度(℃)")
    private Double tem_n_avg_value;
    @NumberFormat("0.0")
    @ExcelProperty({"中线温度最高值","数值"})
    private Double tem_n_max_value;
    @ExcelProperty({"中线温度最高值","发生时间"})
    private String tem_n_max_time;
    @NumberFormat("0.0")
    @ExcelProperty({"中线温度最低值","数值"})
    private Double tem_n_min_value;
    @ExcelProperty({"中线温度最低值","发生时间"})
    private String tem_n_min_time;

}