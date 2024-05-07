package cn.iocoder.yudao.module.cabinet.dto;

import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 能耗趋势返回
 * @date 2024/5/6 15:19
 */
@Data
public class CabinetEqTrendDTO {


    /**
     * 时间
     */
    private String dateTime;

    /**
     * 当前能耗
     */
    private double eq;

    /**
     * 上一次能耗
     */
    private double lastEq;

}
