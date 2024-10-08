package cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 始端箱（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RealtimeEQPageRespVO {

    @ExcelProperty("所在位置")
    private String location;

    @ExcelProperty("设备地址")
    private String dev_key;

    @ExcelProperty("记录日期")
    private String create_time;

    @ExcelProperty("电能 (kWh)")
    @NumberFormat("0.0")
    private Double ele_active;

}