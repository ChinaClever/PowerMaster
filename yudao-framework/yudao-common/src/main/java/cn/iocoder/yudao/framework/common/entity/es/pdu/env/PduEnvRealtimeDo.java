package cn.iocoder.yudao.framework.common.entity.es.pdu.env;

import cn.iocoder.yudao.module.statis.entity.BaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Auther: chenwany
 * @Date: 2024/3/28 14:47
 * @Description: pdu环境表（实时）
 */
@Data
public class PduEnvRealtimeDo extends BaseDo {


    /**
     * 传感器
     */
    @JsonProperty("sensor_id")
    private int sensorId;
    /**
     * 温度
     */
    @JsonProperty("tem_value")
    private float tem;

    /**
     * 湿度
     */
    @JsonProperty("hum_value")
    private int hum;

}
