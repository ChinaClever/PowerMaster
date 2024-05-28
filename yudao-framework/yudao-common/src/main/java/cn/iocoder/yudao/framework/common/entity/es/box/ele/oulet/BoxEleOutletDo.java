package cn.iocoder.yudao.framework.common.entity.es.box.ele.oulet;


import cn.iocoder.yudao.framework.common.entity.es.box.ele.total.BoxEleTotalDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱输出位电能表(实时)
 * @date 2024/5/17 9:04
 */
@Data
public class BoxEleOutletDo extends BoxEleTotalDo {

    @JsonProperty("outlet_id")
    private int outletId;
}
