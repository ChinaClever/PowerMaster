package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import lombok.Data;

@Data
public class BalancedDistributionStatisticsVO {
//小电流
    private int smallCurrent;
//<20%
    private int lessFifteen;
//21-35
    private int greaterFifteen;
//>36
    private int greaterThirty;
}
