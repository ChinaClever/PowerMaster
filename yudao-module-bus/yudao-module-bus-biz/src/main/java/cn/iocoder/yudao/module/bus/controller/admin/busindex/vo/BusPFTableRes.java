package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BusPFTableRes {
    @ExcelProperty("A相功率因素")
    private Float powerFactorAvgValueA;
    @ExcelProperty("B相功率因素")
    private Float powerFactorAvgValueB;
    @ExcelProperty("C相功率因素")
    private Float powerFactorAvgValueC;
    @ExcelProperty("时间")
    private String time;
}
