package cn.iocoder.yudao.framework.common.entity.es.cabinet.env;

import cn.iocoder.yudao.framework.common.entity.es.cabinet.CabinetBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜环境表（实时）
 * @date 2024/4/23 9:49
 */
@Data
public class CabinetEnvRealtimeSensorList extends CabinetBaseDo {

    /**
     * 传感器id
     */
    @JsonProperty("sensorId")
    private int sensorId;

    /**
     * 温度
     */
    @JsonProperty("temValue")
    private float temValue;
    /**
     * 湿度
     */
    @JsonProperty("humValue")
    private float humValue;
}
