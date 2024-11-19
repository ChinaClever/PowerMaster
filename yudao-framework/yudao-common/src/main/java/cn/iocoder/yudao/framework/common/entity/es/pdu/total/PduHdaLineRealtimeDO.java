package cn.iocoder.yudao.framework.common.entity.es.pdu.total;

import cn.iocoder.yudao.framework.common.entity.es.pdu.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PduHdaLineRealtimeDO extends PduBaseDo {
    /**
     *相
     */
    @JsonProperty("line_id")
    private Integer lineId;
    /**
     *电压
     */
    @JsonProperty("vol_value")
    private BigDecimal volValue;
    /**
     * 电流
     */
    @JsonProperty("cur_value")
    private BigDecimal curValue;

    /**
     * 有功功率
     */
    @JsonProperty("pow_active")
    private BigDecimal activePow;

    /**
     * 视在功率
     */
    @JsonProperty("pow_apparent")
    private BigDecimal apparentPow;

    /**
     * 功率因素
     */
    @JsonProperty("power_factor")
    private BigDecimal powerFactor;

    /**
     * 无功功率
     */
    @JsonProperty("pow_reactive")
    private BigDecimal powReactive;
}
