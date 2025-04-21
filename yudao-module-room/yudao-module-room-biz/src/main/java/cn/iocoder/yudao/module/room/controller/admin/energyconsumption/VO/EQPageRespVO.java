package cn.iocoder.yudao.module.room.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.excel.core.util.DateStringConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机房能耗趋势（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class EQPageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty(converter = DateStringConverter.class, value = "记录日期")
    private String create_time;

    @ExcelProperty(converter = DateStringConverter.class, value = {"开始","开始日期"})
    private String start_time;

    @NumberFormat("0.0")
    @ExcelProperty({"开始","开始电能(kWh)"})
    private Double start_ele;

    @ExcelProperty(converter = DateStringConverter.class, value = {"结束","结束日期"})
    private String end_time;

    @NumberFormat("0.0")
    @ExcelProperty({"结束","结束电能(kWh)"})
    private Double end_ele;

    @NumberFormat("0.0")
    @ExcelProperty("耗电量(kWh)")
    private Double eq_value;

}