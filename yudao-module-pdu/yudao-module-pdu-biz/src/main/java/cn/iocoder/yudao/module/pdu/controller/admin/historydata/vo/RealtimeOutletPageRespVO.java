package cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo;

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

@Schema(description = "管理后台 - pdu电力（实时数据） 导出数据")
@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class RealtimeOutletPageRespVO {

    @ExcelProperty("所在位置")
    private String address;

    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("发生时间")
    private String create_time;

    @ExcelProperty("输出位")
    private Integer outlet_id;

    @ExcelProperty("电流(A)")
    @NumberFormat("0.00")
    private Double cur_value;

    @ExcelProperty("有功功率(kW)")
    @NumberFormat("0.000")
    private Double pow_active;

    @ExcelProperty("视在功率(kVA)")
    @NumberFormat("0.000")
    private Double pow_apparent;

    @ExcelProperty("无功功率(kVar)")
    @NumberFormat("0.000")
    private Double pow_reactive;

    @ExcelProperty("功率因素")
    @NumberFormat("0.00")
    private Double power_factor;



}