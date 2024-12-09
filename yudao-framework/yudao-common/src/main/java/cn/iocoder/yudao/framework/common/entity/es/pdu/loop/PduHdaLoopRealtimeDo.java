package cn.iocoder.yudao.framework.common.entity.es.pdu.loop;

import cn.iocoder.yudao.framework.common.entity.es.pdu.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Auther: chenwany
 * @Date: 2024/3/28 14:47
 * @Description: pdu回路历史数据表（实时)
 */
@Data
public class PduHdaLoopRealtimeDo extends PduBaseDo {


    /**
     * 回路
     */
    @JsonProperty("loop_id")
    private int loopId;

    /**
     * 电压
     */
    @JsonProperty("vol_value")
    private float vol;

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
    private int powerFactor;
    /**
     * 无功功率
     */
    @JsonProperty("pow_reactive")
    private float powReactive;

}
