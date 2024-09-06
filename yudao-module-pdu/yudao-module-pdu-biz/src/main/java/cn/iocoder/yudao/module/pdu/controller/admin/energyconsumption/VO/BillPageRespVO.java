package cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - pdu电费统计（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BillPageRespVO {

    @ExcelProperty("位置")
    private String address;

    @ExcelProperty("输出位")
    private Integer outlet_id;

    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("开始日期")
    private String start_time;

    @ExcelProperty("结束日期")
    private String end_time;
    @NumberFormat("0.0")
    @ExcelProperty("耗电量(kWh)")
    private Double eq_value;
    @NumberFormat("0.0")
    @ExcelProperty("电费(元)")
    private Double bill_value;

}