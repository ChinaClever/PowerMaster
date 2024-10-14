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

@Schema(description = "管理后台 - pdu（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class RealtimeEQLoopIdPageRespVO {

    @ExcelProperty("所在位置")
    private String address;


    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("发生时间")
    private String create_time;

//    @ExcelProperty("相")
//    private Integer line_id;

    @ExcelProperty("回路")
    private Integer loop_id;

//    @ExcelProperty("输出位")
//    private Integer outlet_id;
    @NumberFormat("0.0")
    @ExcelProperty("电能 (kWh)")
    private Double ele_active;
//    private Double eq_value;

}