package cn.iocoder.yudao.framework.common.entity.es.bus.tem;

import cn.iocoder.yudao.framework.common.entity.es.bus.BusBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱温度数据表
 * @date 2024/5/17 9:07
 */
@Data
public class BusTemRealtimeDo extends BusBaseDo {


    /**
     * A路温度
     */
    @JsonProperty("tem_a")
    private float temA;

    /**
     * B路温度
     */
    @JsonProperty("tem_b")
    private float temB;


    /**
     * C路温度
     */
    @JsonProperty("tem_c")
    private float temC;


    /**
     * 中线温度
     */
    @JsonProperty("tem_n")
    private float temN;
}
