package cn.iocoder.yudao.framework.common.entity.es.cabinet.pow;

import cn.iocoder.entity.es.cabinet.CabinetBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜功率历史数据表（实时）
 * @date 2024/4/23 9:19
 */
@Data
public class CabinetPowRealtimeDo extends CabinetBaseDo {

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


}
