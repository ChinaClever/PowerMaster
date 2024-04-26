package cn.iocoder.yudao.framework.common.entity.es.cabinet.env;

import cn.iocoder.yudao.framework.common.entity.es.cabinet.CabinetBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜环境基础表
 * @date 2024/4/23 10:19
 */
@Data
public class CabinetEnvBaseDo extends CabinetBaseDo {

    /**
     * 传感器id
     */
    @JsonProperty("sensor_id")
    private int sensorId;

    /**
     * 平均温度
     */
    @JsonProperty("tem_avg_value")
    private float temAvgValue;
    /**
     * 最大温度
     */
    @JsonProperty("tem_max_value")
    private float temMaxValue;
    /**
     * 最高温度时间
     */
    @JsonProperty("tem_max_time")
    private String temMaxTime;
    /**
     * 最低温度
     */
    @JsonProperty("tem_min_value")
    private float temMinValue;
    /**
     * 最低温度时间
     */
    @JsonProperty("tem_min_time")
    private String temMinTime;

    /**
     * 平均湿度
     */
    @JsonProperty("hum_avg_value")
    private float humAvgValue;
    /**
     * 最高湿度
     */
    @JsonProperty("hum_max_value")
    private float humMaxValue;
    /**
     * 最高湿度时间
     */
    @JsonProperty("hum_max_time")
    private String humMaxTime;
    /**
     * 最低湿度
     */
    @JsonProperty("hum_min_value")
    private float humMinValue;
    /**
     * 最低湿度时间
     */
    @JsonProperty("hum_min_time")
    private String humMinTime;
}
