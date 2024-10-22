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

@Schema(description = "管理后台 - pdu能耗趋势（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class EQOutletPageRespVO {

    @ExcelProperty("所在位置")
    private String address;

    @ExcelProperty("输出位")
    private Integer outlet_id;

    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("记录日期")
    private String create_time;

    @NumberFormat("0.0")
    @ExcelProperty({"开始电能","开始电能(kWh)"})
    private Double start_ele;

    @ExcelProperty({"开始电能","开始时间"})
    private String start_time;

    @ExcelProperty({"结束电能","结束电能(kWh)"})
    @NumberFormat("0.0")
    private Double end_ele;

    @ExcelProperty({"结束电能","结束时间"})
    private String end_time;

    @NumberFormat("0.0")
    @ExcelProperty("耗电量(kWh)")
    private Double eq_value;

}