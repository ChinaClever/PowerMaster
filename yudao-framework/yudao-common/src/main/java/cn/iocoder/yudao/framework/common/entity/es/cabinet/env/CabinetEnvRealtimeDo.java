package cn.iocoder.yudao.framework.common.entity.es.cabinet.env;

import cn.iocoder.entity.es.cabinet.CabinetBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜环境表（实时）
 * @date 2024/4/23 9:49
 */
@Data
public class CabinetEnvRealtimeDo extends CabinetBaseDo {

    /**
     * 传感器id
     */
    @JsonProperty("sensor_id")
    private int sensorId;

    /**
     * 温度
     */
    @JsonProperty("tem_value")
    private float temValue;
    /**
     * 湿度
     */
    @JsonProperty("hum_value")
    private float humValue;
}
