package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import lombok.Data;

@Data
public class BalancedDistributionStatisticsVO {

    private int smallCurrent;

    private int lessFifteen;

    private int greaterFifteen;

    private int greaterThirty;
}
