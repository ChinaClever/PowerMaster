package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - pdu电力（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RealtimePageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("网络地址")
    private String address;

    @ExcelProperty("相")
    private Integer line_id;

    @ExcelProperty("回路")
    private Integer loop_id;

    @ExcelProperty("输出位")
    private Integer outlet_id;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("有功功率(kW)")
    private Double pow_active;

    @ExcelProperty("视在功率(kVA)")
    private Double pow_apparent;

    @ExcelProperty("功率因素")
    private Double power_factor;

    @ExcelProperty("电压(V)")
    private Double vol_value;

    @ExcelProperty("电流(A)")
    private Double cur_value;

}