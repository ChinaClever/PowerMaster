package cn.iocoder.yudao.module.rack.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.excel.core.util.DateStringConverter;
import cn.iocoder.yudao.framework.excel.core.annotations.EnumFiledConvert;
import cn.iocoder.yudao.framework.excel.core.util.TypeOptionConverter;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机架电费统计（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BillPageRespVO {

    @ExcelProperty("机架名")
    private String rack_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty(converter = DateStringConverter.class, value ="日期")
    private String start_time;

    @ExcelProperty("结束日期")
    @ExcelIgnore
    private String end_time;

    @NumberFormat("0.0")
    @ExcelProperty("耗电量(kWh)")
    private Double eq_value;

    @NumberFormat("0.00")
    @ExcelProperty("电费(元)")
    private Double bill_value;

    @ExcelProperty(value = "计费方式",converter = TypeOptionConverter.class)
    @EnumFiledConvert(enumMap = "0|固定计费,1|分段计费")
    private Integer bill_mode;
}