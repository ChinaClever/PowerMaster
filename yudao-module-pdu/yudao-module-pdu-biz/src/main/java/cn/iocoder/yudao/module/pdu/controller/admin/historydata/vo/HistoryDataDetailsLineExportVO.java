package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.With;

@Schema(description = "管理后台 - pdu历史数据详情 导出原始相数据")
@Data
@ExcelIgnoreUnannotated
@HeadRowHeight(20)
@ColumnWidth(20)
public class HistoryDataDetailsLineExportVO {
    @ExcelProperty("记录时间")
    private String create_time;
    @ExcelProperty("电压(V)")
    @NumberFormat("0.000")
    private Double vol_value;
    @NumberFormat("0.00(A)")
    @ExcelProperty("电流")
    private Double cur_value;
    @ExcelProperty("有功功率(kW)")
    @NumberFormat("0.000")
    private Double pow_active;
    @NumberFormat("0.000(kVA)")
    @ExcelProperty("视在功率")
    private Double pow_apparent;
    @NumberFormat("0.00")
    @ExcelProperty("功率因素")
    private Double power_factor;
}
