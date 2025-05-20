package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import cn.iocoder.yudao.framework.excel.core.util.DataSourceConverter;
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

@Schema(description = "管理后台 - 始端箱环境历史数据（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class BusEnvRealtimePageRespVO {

    @ExcelProperty("母线名称")
    private String bus_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("设备地址")
    private String dev_key;

    @ExcelProperty(value = "保存策略",converter = DataSourceConverter.class)
    private Integer data_source;

    @ExcelProperty("记录时间")
    private String create_time;
    @ExcelProperty("A路温度(℃)")
    private Integer tem_a;
    @ExcelProperty("B路温度(℃)")
    private Integer tem_b;
    @ExcelProperty("C路温度(℃)")
    private Integer tem_c;
    @ExcelProperty("中线温度(℃)")
    private Integer tem_n;
}