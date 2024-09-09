package cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - pdu（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class RealtimeEQOutletIdPageRespVO {

    @ExcelProperty("位置")
    private String address;


    @ExcelProperty("网络地址")
    private String location;

    @ExcelProperty("记录日期")
    private String create_time;

//    @ExcelProperty("相")
//    private Integer line_id;
//
//    @ExcelProperty("回路")
//    private Integer loop_id;

    @ExcelProperty("输出位")
    private Integer outlet_id;

    @ExcelProperty("电能 (kWh)")
    private Double ele_active;
//    private Double eq_value;

}