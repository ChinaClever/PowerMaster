package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import cn.hutool.core.date.DateTime;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BoxPFDetailTotalResVO {
    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTime;

    /**
     * 平均功率因素
     */
    @ExcelProperty("输出位1功率因素")
    private BigDecimal powerFactorAvgValue;

    /**
     * 平均功率因素
     */
    @ExcelProperty("输出位1最大功率因素")
    private BigDecimal powerFactorMaxValue;

    @ExcelProperty("输出位1最大功率因素发生时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime powerFactorMaxTime;

    /**
     * 平均功率因素
     */
    @ExcelProperty("输出位1最小功率因素")
    private BigDecimal powerFactorMinValue;

    @ExcelProperty("输出位1最小功率因素发生时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime powerFactorMinTime;

    /**
     * 平均功率因素
     */
    @ExcelProperty("输出位2功率因素")
    private BigDecimal powerFactorAvgValueb;

    /**
     * 平均功率因素
     */
    @ExcelProperty("输出位2最大功率因素")
    private BigDecimal powerFactorMaxValueb;

    @ExcelProperty("输出位2最大功率因素发生时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime powerFactorMaxTimeb;

    /**
     * 平均功率因素
     */
    @ExcelProperty("输出位2最小功率因素")
    private BigDecimal powerFactorMinValueb;

    @ExcelProperty("输出位2最小功率因素发生时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime powerFactorMinTimeb;

    /**
     * 平均功率因素
     */
    @ExcelProperty("输出位3功率因素")
    private BigDecimal powerFactorAvgValuec;

    /**
     * 平均功率因素
     */
    @ExcelProperty("输出位3最大功率因素")
    private BigDecimal powerFactorMaxValuec;

    @ExcelProperty("输出位3最大功率因素发生时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime powerFactorMaxTimec;

    /**
     * 平均功率因素
     */
    @ExcelProperty("输出位3最小功率因素")
    private BigDecimal powerFactorMinValuec;

    @ExcelProperty("输出位3最小功率因素发生时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime powerFactorMinTimec;

}
