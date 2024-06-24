package cn.iocoder.yudao.module.bus.controller.admin.busindex.vo;

import lombok.Data;

@Data
public class BusPFTableRes {

    private Float powerFactorAvgValueA;

    private Float powerFactorAvgValueB;

    private Float powerFactorAvgValueC;

    private String time;
}
