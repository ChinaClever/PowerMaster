package cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AisleBalanceChartVolCurVO {

    private Double curAAvgValue;

    private Double curBAvgValue;

    private Double volAAvgValue;

    private Double volBAvgValue;

    private Integer aisleId;

    private String createTime;
}
