package cn.iocoder.yudao.module.statis.entity.env;

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
    private float tem;

    /**
     * 湿度
     */
    private int hum;

}
