package cn.iocoder.yudao.framework.common.entity.es.bus.total;

import cn.iocoder.yudao.framework.common.entity.es.bus.BusBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱总历史数据表
 * @date 2024/5/17 8:57
 */
@Data
public class BusTotalRealtimeDo extends BusBaseDo {


    /**
     * 有功功率
     */
    @JsonProperty("pow_active")
    private float powActive;

    /**
     * 无功功率
     */
    @JsonProperty("pow_reactive")
    private float powReactive;


    /**
     * 视在功率
     */
    @JsonProperty("pow_apparent")
    private float powApparent;


    /**
     * 功率因素
     */
    @JsonProperty("power_factor")
    private float powerFactor;

    /**
     * 剩余电流
     */
    @JsonProperty("cur_residual")
    private float curResidual;

    /**
     * 零线电流
     */
    @JsonProperty("cur_zero")
    private float curZero;

    /**
     * 电压三相不平衡
     */
    @JsonProperty("vol_unbalance")
    private float volUnbalance;

    /**
     * 电流三相不平衡
     */
    @JsonProperty("cur_unbalance")
    private float curUnbalance;
}
