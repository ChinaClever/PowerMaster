package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import cn.hutool.core.date.DateTime;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BusPFTableRes {
    @ExcelProperty("A相功率因素")
    private Float powerFactorAvgValueA;
    @ExcelProperty("B相功率因素")
    private Float powerFactorAvgValueB;
    @ExcelProperty("C相功率因素")
    private Float powerFactorAvgValueC;

    @ExcelProperty("A相最大功率因素")
    private float powerFactorMaxValueA;
    @ExcelProperty("A相最大功率因素时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String powerFactorMaxTimeA;

    @ExcelProperty("B相最大功率因素")
    private float powerFactorMaxValueB;
    @ExcelProperty("B相最大功率因素时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String powerFactorMaxTimeB;

    @ExcelProperty("C相最大功率因素")
    private float powerFactorMaxValueC;
    @ExcelProperty("C相最大功率因素时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String powerFactorMaxTimeC;

    @ExcelProperty("A相最小功率因素")
    private float powerFactorMinValueA;
    @ExcelProperty("A相最小功率因素时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String powerFactorMinTimeA;

    @ExcelProperty("B相最小功率因素")
    private float powerFactorMinValueB;
    @ExcelProperty("B相最小功率因素时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String powerFactorMinTimeB;

    @ExcelProperty("C相最小功率因素")
    private float powerFactorMinValueC;
    @ExcelProperty("C相最小功率因素时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String powerFactorMinTimeC;

    @ExcelProperty("时间")
    private String time;
}
