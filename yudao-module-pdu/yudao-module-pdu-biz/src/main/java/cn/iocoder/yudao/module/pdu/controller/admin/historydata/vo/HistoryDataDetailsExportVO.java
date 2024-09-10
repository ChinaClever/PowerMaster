package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - pdu历史数据详情 导出原始数据")
@Data
@ExcelIgnoreUnannotated
public class HistoryDataDetailsExportVO {
    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("总有功功率")
    @NumberFormat("0.000")
    private Double pow_active;
    @NumberFormat("0.000")
    @ExcelProperty("总视在功率")
    private Double pow_apparent;
    @NumberFormat("0.00")
    @ExcelProperty("功率因素")
    private Double power_factor;
}
