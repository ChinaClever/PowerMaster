package cn.iocoder.yudao.framework.common.entity.es.bus.ele.line;

import cn.iocoder.yudao.framework.common.entity.es.bus.ele.total.BusEleTotalDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱相电能表
 * @date 2024/5/17 9:06
 */
@Data
public class BusEleLineDo extends BusEleTotalDo {

    @JsonProperty("line_id")
    private int lineId;
}
