package cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.excel.core.annotations.EnumFiledConvert;
import cn.iocoder.yudao.framework.excel.core.util.DateStringConverter;
import cn.iocoder.yudao.framework.excel.core.util.TypeOptionConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 始端箱电费统计（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class BillPageRespVO {

    @ExcelProperty("所在位置")
    private String location;

    @ExcelProperty("设备地址")
    private String dev_key;
    @ExcelProperty("设备名称")
    private String bus_name;
    @ExcelProperty(converter = DateStringConverter.class, value ="开始日期")
    private String start_time;

    @ExcelProperty(converter = DateStringConverter.class, value ="结束日期")
    private String end_time;

    @ExcelProperty("耗电量(kWh)")
    @NumberFormat("0.0")
    private Double eq_value;

    @ExcelProperty("电费(元)")
    @NumberFormat("0.0")
    private Double bill_value;

    @ExcelProperty(value = "计费方式",converter = TypeOptionConverter.class)
    @EnumFiledConvert(enumMap = "1|固定计费,2|分段计费")
    private Integer bill_mode;

}