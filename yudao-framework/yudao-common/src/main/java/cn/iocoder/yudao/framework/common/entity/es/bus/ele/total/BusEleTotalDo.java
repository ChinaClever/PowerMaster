package cn.iocoder.yudao.framework.common.entity.es.bus.ele.total;

import cn.iocoder.yudao.framework.common.entity.es.bus.BusBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱总电能表(实时)
 * @date 2024/5/17 9:04
 */
@Data
public class BusEleTotalDo extends BusBaseDo {

    /**
     * 总电能
     */
    @JsonProperty("ele_active")
    private double eleActive;
}
