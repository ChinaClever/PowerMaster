package cn.iocoder.yudao.framework.common.entity.es.box.ele.loop;


import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEleTotalDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱回路电能表(实时)
 * @date 2024/5/17 9:04
 */
@Data
public class BoxEleLoopDo extends BoxEleTotalDo {

    @JsonProperty("loop_id")
    private int loopId;
}
