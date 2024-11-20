package cn.iocoder.yudao.module.room.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机房电力（小时、天数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RoomHistoryDataExcelRespVO {

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("总有功功率")
    private Double active_total;

    @ExcelProperty("总视在功率")
    private Double apparent_total;

    @ExcelProperty("总无功功率")
    private Double factor_total;

    @ExcelProperty("总功率因素")
    private Double reactive_total;

    @ExcelProperty({"总视在功率最大值","数值"})
    private String apparent_total_max_value;

    @ExcelProperty({"总视在功率最大值","时间"})
    private String apparent_total_max_time;

    @ExcelProperty({"总视在功率最小值","数值"})
    private String apparent_total_min_value;

    @ExcelProperty({"总视在功率最小值","时间"})
    private String apparent_total_min_time;

    @ExcelProperty("总平均无功功率")
    private Double reactive_total_avg_value;

    @ExcelProperty({"总无功功率最大值","数值"})
    private Double reactive_total_max_value;

    @ExcelProperty({"总无功功率最大值","时间"})
    private Double reactive_total_max_time;

    @ExcelProperty({"总无功功率最小值","数值"})
    private Double reactive_total_min_value;

    @ExcelProperty({"总无功功率最小值","时间"})
    private Double reactive_total_min_time;

    @ExcelProperty("总平均功率因素")
    private Double factor_total_avg_value;

}