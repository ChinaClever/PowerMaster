package cn.iocoder.yudao.module.statis.entity.env;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/8 11:37
 * @Description:  环境基础数据
 */
@Data
public class PduEnvBaseDo {

    private int id;

    /**
     * 设备标识
     */

    @JsonProperty("pdu_id")
    private int pduId;


    /**
     * 传感器ID
     */

    @JsonProperty("sensor_id")
    private int sensorId;

    /**
     * 平均温度
     */
    @JsonProperty("tem_avg_value")
    private float temAvgValue;


    /**
     *  最大温度
     */
    @JsonProperty("tem_max_value")
    private float temMaxValue;



    /**
     *  最高温度时间
     */
    @JsonProperty("tem_max_time")
    private DateTime temMaxTime;



    /**
     * 最低温度
     */
    @JsonProperty("tem_min_value")
    private float temMinValue;



    /**
     * 最低温度时间
     */
    @JsonProperty("tem_min_time")
    private DateTime temMinTime;


    /**
     *  平均湿度
     */
    @JsonProperty("hum_avg_value")
    private int humAvgValue;

    /**
     * 最高湿度
     */
    @JsonProperty("hum_max_value")
    private int humMaxValue;



    /**
     * 最高湿度时间
     */
    @JsonProperty("hum_max_time")
    private DateTime humMaxTime;


    /**
     * 最低湿度
     */
    @JsonProperty("hum_min_value")
    private int humMinValue;


    /**
     * 最低湿度时间
     */
    @JsonProperty("hum_min_time")
    private DateTime humMinTime;


    /**
     *  创建时间
     */
    @JsonProperty("create_time")
    private DateTime createTime;




}
