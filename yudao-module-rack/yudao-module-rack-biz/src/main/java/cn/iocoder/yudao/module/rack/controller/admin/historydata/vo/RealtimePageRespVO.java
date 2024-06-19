package cn.iocoder.yudao.module.rack.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机架电力（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RealtimePageRespVO {

    @ExcelProperty("机架名")
    private String rack_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("有功功率(kW)")
    private Double active_total;

    @ExcelProperty("视在功率(kVA)")
    private Double apparent_total;

}