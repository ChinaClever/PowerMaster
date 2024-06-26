package cn.iocoder.yudao.framework.common.entity.es.aisle.pow;

import cn.iocoder.yudao.framework.common.entity.es.aisle.AisleBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列功率历史数据表（实时）
 * @date 2024/4/23 9:19
 */
@Data
public class AislePowRealtimeDo extends AisleBaseDo {

    /**
     * 总视在功率
     */
    @JsonProperty("apparent_total")
    private float apparentTotal;
    /**
     * a路视在功率
     */
    @JsonProperty("apparent_a")
    private float apparentA;
    /**
     * b路视在功率
     */
    @JsonProperty("apparent_b")
    private float apparentB;
    /**
     * 总有功功率
     */
    @JsonProperty("active_total")
    private float activeTotal;
    /**
     * a路有功功率
     */
    @JsonProperty("active_a")
    private float activeA;
    /**
     * b路有功功率
     */
    @JsonProperty("active_b")
    private float activeB;

    /**
     * 总无功功率
     */
    @JsonProperty("reactive_total")
    private float reactiveTotal;
    /**
     * a路无功功率
     */
    @JsonProperty("reactive_a")
    private float reactiveA;
    /**
     * b路无功功率
     */
    @JsonProperty("reactive_b")
    private float reactiveB;


    /**
     * 总功率因素
     */
    @JsonProperty("factor_total")
    private float factorTotal;
    /**
     * a路功率因素
     */
    @JsonProperty("factor_a")
    private float factorA;
    /**
     * b路功率因素
     */
    @JsonProperty("factor_b")
    private float factorB;


}
