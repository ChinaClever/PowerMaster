package cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO;

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
public class BillOutletWeekPageRespVO {

    @ExcelProperty("所在位置")
    private String address;

    @ExcelProperty("输出位")
    private Integer outlet_id;

    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("开始时间")
    private String start_time;

    @ExcelProperty("结束时间")
    private String end_time;
    @NumberFormat("0.0")
    @ExcelProperty("耗电量(kWh)")
    private Double eq_value;

    @NumberFormat("0.00")
    @ExcelProperty("电费(元)")
    private Double bill_value;

}