package cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO;

import cn.iocoder.yudao.framework.excel.core.annotations.EnumFiledConvert;
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

@Schema(description = "管理后台 - pdu电费统计（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class BillDayPageRespVO {

    @ExcelProperty("所在位置")
    private String address;

    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("发生时间")
    private String start_time;

    @NumberFormat("0.0")
    @ExcelProperty("耗电量(kWh)")
    private Double eq_value;

    @NumberFormat("0.00")
    @ExcelProperty("电费(元)")
    private Double bill_value;

    @ExcelProperty(value = "计费方式",converter = TypeOptionConverter.class)
    @EnumFiledConvert(enumMap = "1|固定计费,2|分段计费")
    private Integer bill_mode;
}