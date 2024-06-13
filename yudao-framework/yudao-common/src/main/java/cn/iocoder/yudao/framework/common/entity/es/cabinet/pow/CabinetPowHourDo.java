package cn.iocoder.yudao.framework.common.entity.es.cabinet.pow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜功率历史数据表（按小时算）
 * @date 2024/4/23 9:41
 */
@Data
public class CabinetPowHourDo extends CabinetPowBaseDo {

    /**
     * 总平均无功功率
     */
    @JsonProperty("reactive_total_avg_value")
    private float reactiveTotalAvgValue;


    /**
     * 总平均功率因素
     */
    @JsonProperty("factor_total_avg_value")
    private float factorTotalAvgValue;


    /**
     * 平均负载率
     */
    @JsonProperty("load_rate_total_avg_value")
    private float loadRateTotalAvgValue;
}
