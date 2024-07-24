package cn.iocoder.yudao.module.room.controller.admin.energyconsumption.VO;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 机房电费统计（天周月） 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BillPageRespVO {

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("日期")
    private String end_time;

    @ExcelProperty("耗电量(kWh)")
    private Double eq_value;

    @ExcelProperty("电费(元)")
    private Double bill_value;

}