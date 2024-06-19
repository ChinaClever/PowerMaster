package cn.iocoder.yudao.module.bus.controller.admin.historydata.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 母线始端箱(实时数据) 导出数据")
@Data
@ExcelIgnoreUnannotated
public class BusRealtimePageRespVO {

    @ExcelProperty("母线名称")
    private String bus_name;

    @ExcelProperty("位置")
    private String location;

    @ExcelProperty("IP地址")
    private String ip_addr;

    @ExcelProperty("记录时间")
    private String create_time;

    @ExcelProperty("有功功率(kW)")
    private Double pow_active;

    @ExcelProperty("视在功率(kVA)")
    private Double pow_apparent;

    @ExcelProperty("无功功率(kW)")
    private Double pow_reactive;

    @ExcelProperty("功率因素")
    private Double power_factor;

    @ExcelProperty("剩余电流(A)")
    private Double cur_residual;

    @ExcelProperty("零线电流(A)")
    private Double cur_zero;

    @ExcelProperty("电压三相不平衡")
    private Double vol_unbalance;

    @ExcelProperty("电流三相不平衡")
    private Double cur_unbalance;

    //相
    @ExcelProperty("相")
    private Double line_id;

    @ExcelProperty("电压(V)")
    private Double vol_value;

    @ExcelProperty("电流(A)")
    private Double cur_value;

    @ExcelProperty("线电压(V)")
    private Double vol_line;

    @ExcelProperty("负载率(%)")
    private Double load_rate;

    @ExcelProperty("电流谐波含量")
    private String cur_thd;

    @ExcelProperty("电压谐波含量")
    private String vol_thd;

}