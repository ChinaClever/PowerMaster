package cn.iocoder.yudao.framework.common.entity.es.room.ele;

import cn.iocoder.yudao.framework.common.entity.es.room.RoomBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房总电能表(实时
 * @date 2024/4/23 10:21
 */
@Data
public class RoomEleTotalRealtimeDo extends RoomBaseDo {

    /**
     * 总电能
     */
    @JsonProperty("ele_total")
    private double eleTotal;
    /**
     * a路电能
     */
    @JsonProperty("ele_a")
    private double eleA;

    /**
     * b路电能
     */
    @JsonProperty("ele_b")
    private double eleB;

}
