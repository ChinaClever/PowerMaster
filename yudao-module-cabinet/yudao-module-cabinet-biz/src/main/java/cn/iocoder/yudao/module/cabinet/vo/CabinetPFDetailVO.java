package cn.iocoder.yudao.module.cabinet.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CabinetPFDetailVO {
    /**
     * 总平均功率因素
     */
    @JsonProperty("factor_total_avg_value")
    private float factorTotalAvgValue;

    /**
     * a路平均功率因素
     */
    @JsonProperty("factor_a_avg_value")
    private float factorAAvgValue;

    /**
     * b路平均功率因素
     */
    @JsonProperty("factor_b_avg_value")
    private float factorBAvgValue;

    @JsonProperty("create_time")
    private String createTime;
}
