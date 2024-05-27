package cn.iocoder.yudao.framework.common.entity.es.box.ele.line;


import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEleTotalDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱相电能表(实时)
 * @date 2024/5/17 9:04
 */
@Data
public class BoxEleLineDo extends BoxEleTotalDo {

    @JsonProperty("line_id")
    private int lineId;
}
