package cn.iocoder.yudao.module.cabinet.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.excel.core.util.DateStringConverter;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author: jiangjinchi
 * @time: 2024/11/8 12:04
 */
@Schema(description = "管理后台 - 机房-实时电能出参")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class CabinetEleTotalRealtimeResVO {
    @ExcelProperty("RoomId")
    @ExcelIgnore
    private Integer id;

    @ExcelProperty("机房名称")
    private String location;

    @NumberFormat("0.0")
    @ExcelProperty("开始电能 (kWh)")
    private Double eleActiveStart;
    @ExcelProperty(converter = DateStringConverter.class, value ="开始时间")
    @JsonFormat
    private String createTimeMin;

    @NumberFormat("0.0")
    @ExcelProperty("结束电能 (kWh)")
    private Double eleActiveEnd;

    @ExcelProperty(converter = DateStringConverter.class, value = "结束时间")
    private String createTimeMax;

    @NumberFormat("0.0")
    @ExcelProperty("电能 (kWh)")
    private Double eleActive;
}
