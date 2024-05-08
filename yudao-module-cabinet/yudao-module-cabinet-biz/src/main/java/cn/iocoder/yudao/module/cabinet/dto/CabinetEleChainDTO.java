package cn.iocoder.yudao.module.cabinet.dto;

import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 能耗环比
 * @date 2024/5/6 15:22
 */
@Data
public class CabinetEleChainDTO {


    /**
     * 今日用能
     */
    private Double todayEq;

    /**
     * 昨日用能
     */
    private Double yesterdayEq;

    /**
     * 日环比
     */
    private String dayRate;

    /**
     * 当周用能
     */
    private Double thisWeekEq;

    /**
     * 上周用能
     */
    private Double lastWeekEq;

    /**
     * 日环比
     */
    private String weekRate;


    /**
     * 本月用能
     */
    private Double thisMonthEq;

    /**
     * 上月用能
     */
    private Double lastMonthEq;

    /**
     * 月环比
     */
    private String monthRate;

}
