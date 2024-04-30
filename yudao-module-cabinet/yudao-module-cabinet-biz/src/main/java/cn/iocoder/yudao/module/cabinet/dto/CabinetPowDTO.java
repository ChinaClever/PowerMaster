package cn.iocoder.yudao.module.cabinet.dto;

import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 实时曲线数据
 * @date 2024/4/30 11:23
 */
@Data
public class CabinetPowDTO {

    /**
     * 时间
     */
    private String dateTime;

    /**
     * 有功功率
     */
    private float activePow;

    /**
     * 视在功率
     */
    private float apparentPow;
}
