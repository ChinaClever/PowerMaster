package cn.iocoder.yudao.framework.common.entity.es.rack.ele;

import cn.iocoder.yudao.framework.common.entity.es.rack.RackBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架总电能表(实时
 * @date 2024/4/23 10:21
 */
@Data
public class RackEleTotalRealtimeDo extends RackBaseDo {

    /**
     * 总电能
     */
    @JsonProperty("ele_total")
    private double eleTotal;

}
