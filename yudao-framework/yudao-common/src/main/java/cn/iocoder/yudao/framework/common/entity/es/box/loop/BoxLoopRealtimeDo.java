package cn.iocoder.yudao.framework.common.entity.es.box.loop;

import cn.iocoder.yudao.framework.common.entity.es.box.BoxBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱箱回路历史数据表(实时 ）
 * @date 2024/5/17 9:10
 */
@Data
public class BoxLoopRealtimeDo extends BoxBaseDo {

    @JsonProperty("loop_id")
    private int loopId;

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
     * 电压
     */
    @JsonProperty("vol_value")
    private float volValue;

    /**
     * 电流
     */
    @JsonProperty("cur_value")
    private float curValue;
}
