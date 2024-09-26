package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - pdu环境分析 导出原始数据")
@Data
@ExcelIgnoreUnannotated
@ColumnWidth(30)
public class HistoryEnvDataExportVO {
    @ExcelProperty("记录时间")
    private String create_time;
    @ExcelProperty("温度")
    @NumberFormat("0.0")
    private Double tem_value;
    @ExcelProperty("湿度")
    @NumberFormat("0")
    private Double hum_value;
}
