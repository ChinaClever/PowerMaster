package cn.iocoder.yudao.framework.common.entity.es.box.outlet;

import cn.iocoder.yudao.framework.common.entity.es.box.BoxBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱箱输出位历史数据表(实时 ）
 * @date 2024/5/17 9:10
 */
@Data
public class BoxOutletRealtimeDo extends BoxBaseDo {

    @JsonProperty("outlet_id")
    private int outletId;

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
}
