package cn.iocoder.yudao.framework.common.entity.es.box.ele.oulet;

import cn.iocoder.yudao.framework.common.entity.es.box.ele.BoxEqBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱输出位电能表（
 * @date 2024/5/21 14:55
 */
@Data
public class BoxEqOutletBaseDo extends BoxEqBaseDo {


    @JsonProperty("outlet_id")
    private int outletId;

}
