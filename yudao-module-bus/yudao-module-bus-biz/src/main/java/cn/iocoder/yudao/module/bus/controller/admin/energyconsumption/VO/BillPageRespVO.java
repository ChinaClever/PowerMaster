package cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 始端箱电费统计（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BillPageRespVO {

    @ExcelProperty("母线名称")
    private String bus_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("网络地址")
    private String ip_addr;

    @ExcelProperty("日期")
    private String end_time;

    @ExcelProperty("耗电量(kWh)")
    private Double eq_value;

    @ExcelProperty("电费(元)")
    private Double bill_value;

}