package cn.iocoder.yudao.framework.common.entity.es.pdu.total;

import cn.iocoder.yudao.module.statis.entity.BaseDo;
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
    @JsonProperty("pow_active")
    private float activePow;

    /**
     * 视在功率
     */
    @JsonProperty("pow_apparent")
    private float apparentPow;

    /**
     * 功率因素
     */
    @JsonProperty("power_factor")
    private int powerFactor;
}
