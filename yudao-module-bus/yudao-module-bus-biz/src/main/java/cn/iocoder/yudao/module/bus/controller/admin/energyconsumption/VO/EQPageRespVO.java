package cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.excel.core.util.DateStringConverter;
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

@Schema(description = "管理后台 - 始端箱能耗趋势（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class EQPageRespVO {

    @ExcelProperty("所在位置")
    private String location;

    @ExcelProperty("设备地址")
    private String dev_key;
    @ExcelProperty("设备名称")
    private String bus_name;

    @ExcelProperty(converter = DateStringConverter.class, value ="记录日期")
    private String create_time;


    @NumberFormat("0.0")
    @ExcelProperty({"开始电能","开始电能(kWh)"})
    private Double start_ele;
    @ExcelProperty(converter = DateStringConverter.class, value ={"开始电能","开始时间"})
    private String start_time;


    @ExcelProperty({"结束电能","结束电能(kWh)"})
    @NumberFormat("0.0")
    private Double end_ele;

    @ExcelProperty(converter = DateStringConverter.class, value ={"结束电能","结束时间"})
    private String end_time;


    @NumberFormat("0.0")
    @ExcelProperty("耗电量(kWh)")
    private Double eq_value;

}