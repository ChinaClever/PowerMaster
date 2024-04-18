package cn.iocoder.yudao.module.statis.vo;

import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/9 11:26
 * @Description: 电量计费配置
 */
@Data
public class EqBillConfigVo {

    /**
     * 电费
     */
    private double bill;

    /**
     * 计费开始时间 00:00:00
     */
    private String startTime;

    /**
     * 计费结束时间 23:59:59
     */
    private String endTime;


    /**
     * 计费方式
     */
    private int billMode;

    /**
     * 计费时间段
     */
    private String billPeriod;
}
