package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.excel.core.util.DateTimeConverter;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(30)
@HeadRowHeight(20)
public class BusTemTableRes {
    /**
     * 平均A路温度
     */
    @ExcelProperty("平均A路温度")
    @JsonProperty("tem_a_avg_value")
    private float temAAvgValue;


    /**
     * 最大A路温度
     */
    @ExcelProperty("最大A路温度")
    @JsonProperty("tem_a_max_value")
    private float temAMaxValue;


    /**
     * 最高A路温度时间
     */
    @ExcelProperty(value = "最高A路温度时间", converter = DateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("tem_a_max_time")
    private DateTime temAMaxTime;


    /**
     * 最低A路温度
     */
    @ExcelProperty("最低A路温度")
    @JsonProperty("tem_a_min_value")
    private float temAMinValue;


    /**
     * 最低A路温度时间
     */
    @ExcelProperty(value = "最低A路温度时间", converter = DateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("tem_a_min_time")
    private DateTime temAMinTime;


    /**
     * 平均B路温度
     */
    @ExcelProperty("平均B路温度")
    @JsonProperty("tem_b_avg_value")
    private float temBAvgValue;


    /**
     * 最大B路温度
     */
    @ExcelProperty("最大B路温度")
    @JsonProperty("tem_b_max_value")
    private float temBMaxValue;


    /**
     * 最高B路温度时间
     */
    @ExcelProperty(value = "最高B路温度时间", converter = DateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("tem_b_max_time")
    private DateTime temBMaxTime;


    /**
     * 最低B路温度
     */
    @ExcelProperty("最低B路温度")
    @JsonProperty("tem_b_min_value")
    private float temBMinValue;


    /**
     * 最低B路温度时间
     */
    @ExcelProperty(value = "最低B路温度时间", converter = DateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("tem_b_min_time")
    private DateTime temBMinTime;

    /**
     * 平均C路温度
     */
    @ExcelProperty("平均C路温度")
    @JsonProperty("tem_c_avg_value")
    private float temCAvgValue;


    /**
     * 最大C路温度
     */
    @ExcelProperty("最大C路温度")
    @JsonProperty("tem_c_max_value")
    private float temCMaxValue;


    /**
     * 最高C路温度时间
     */
    @ExcelProperty(value = "最高C路温度时间", converter = DateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("tem_c_max_time")
    private DateTime temCMaxTime;


    /**
     * 最低C路温度
     */
    @ExcelProperty("最低C路温度")
    @JsonProperty("tem_c_min_value")
    private float temCMinValue;


    /**
     * 最低C路温度时间
     */
    @ExcelProperty(value = "最低C路温度时间", converter = DateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("tem_c_min_time")
    private DateTime temCMinTime;

    /**
     * 平均中线温度
     */
    @ExcelProperty("平均中线温度")
    @JsonProperty("tem_n_avg_value")
    private float temNAvgValue;


    /**
     * 最大中线温度
     */
    @ExcelProperty("最大中线温度")
    @JsonProperty("tem_n_max_value")
    private float temNMaxValue;


    /**
     * 最高中线温度时间
     */
    @ExcelProperty(value = "最高中线温度时间", converter = DateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("tem_n_max_time")
    private DateTime temNMaxTime;


    /**
     * 最低中线温度
     */
    @ExcelProperty("最低中线温度")
    @JsonProperty("tem_n_min_value")
    private float temNMinValue;


    /**
     * 最低中线温度时间
     */
    @ExcelProperty(value = "最低中线温度时间", converter = DateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("tem_n_min_time")
    private DateTime temNMinTime;

    @ExcelProperty(value = "创建时间", converter = DateTimeConverter.class)
    private DateTime createTime;

}
