package cn.iocoder.yudao.framework.common.entity.es.pdu.outlet;

import cn.iocoder.yudao.framework.common.entity.es.pdu.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Auther: chenwany
 * @Date: 2024/3/28 14:47
 * @Description: pdu输出位历史数据表（实时）
 */
@Data
public class PduHdaOutletRealtimeDo extends PduBaseDo {


    /**
     * 输出位
     */
    @JsonProperty("outlet_id")
    private int outletId;


    /**
     * 电流
     */
    @JsonProperty("cur_value")
    private float cur;

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
    private float powerFactor;

    /**
     * 无功功率
     */
    @JsonProperty("pow_reactive")
    private float powReactive;
}
