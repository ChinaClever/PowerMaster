package cn.iocoder.yudao.module.statis.entity.es;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Auther: chenwany
 * @Date: 2024/3/28 14:47
 * @Description: pdu总历史数据表（实时）
 */
@Data
public class PduHdaTotalRealtimeDo extends BaseDo {


    /**
     * 有功功率
     */
    @JsonProperty("active_pow")
    private float activePow;

    /**
     * 视在功率
     */
    @JsonProperty("apparent_pow")
    private float apparentPow;

    /**
     * 功率因素
     */
    @JsonProperty("power_factor")
    private int powerFactor;
}
