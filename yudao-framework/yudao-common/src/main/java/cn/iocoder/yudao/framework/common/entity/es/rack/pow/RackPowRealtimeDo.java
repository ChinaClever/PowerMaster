package cn.iocoder.yudao.framework.common.entity.es.rack.pow;

import cn.iocoder.yudao.framework.common.entity.es.rack.RackBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机架功率历史数据表（实时）
 * @date 2024/4/23 9:19
 */
@Data
public class RackPowRealtimeDo extends RackBaseDo {

    /**
     * 总视在功率
     */
    @JsonProperty("apparent_total")
    private float apparentTotal;

    /**
     * 总有功功率
     */
    @JsonProperty("active_total")
    private float activeTotal;


}
