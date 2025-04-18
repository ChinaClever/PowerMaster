package cn.iocoder.yudao.module.room.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.excel.core.util.DateStringConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机房（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RealtimeEQPageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty(converter = DateStringConverter.class, value ="记录日期")
    private String create_time;

    @NumberFormat("0.0")
    @ExcelProperty("A路电能 (kWh)")
    private Double ele_a;

    @NumberFormat("0.0")
    @ExcelProperty("B路电能 (kWh)")
    private Double ele_b;

    @NumberFormat("0.0")
    @ExcelProperty("总电能 (kWh)")
    private Double ele_total;

}