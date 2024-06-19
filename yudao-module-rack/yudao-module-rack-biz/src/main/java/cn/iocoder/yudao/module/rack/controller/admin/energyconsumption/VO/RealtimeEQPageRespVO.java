package cn.iocoder.yudao.module.rack.controller.admin.energyconsumption.VO;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机架（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RealtimeEQPageRespVO {

    @ExcelProperty("机架名")
    private String rack_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("记录日期")
    private String create_time;

    @ExcelProperty("电能 (kWh)")
    private Double ele_total;

}