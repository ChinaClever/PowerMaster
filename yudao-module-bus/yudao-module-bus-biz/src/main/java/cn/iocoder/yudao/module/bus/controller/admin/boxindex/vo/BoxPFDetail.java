package cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo;

import cn.hutool.core.date.DateTime;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BoxPFDetail {
    /**
     * 输出位
     */
    @ExcelProperty("输出位")
    @JsonProperty("outlet_id")
    private int outletId;

    /**
     * 平均功率因素
     */
    @ExcelProperty("功率因素")
    @JsonProperty("power_factor_avg_value")
    private BigDecimal powerFactorAvgValue;

//    @JsonProperty("box_id")
//    private int boxId;

    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTime;
}
