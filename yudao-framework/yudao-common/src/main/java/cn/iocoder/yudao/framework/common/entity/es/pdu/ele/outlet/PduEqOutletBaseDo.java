package cn.iocoder.yudao.framework.common.entity.es.pdu.ele.outlet;

import cn.iocoder.yudao.module.statis.entity.ele.PduEqBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/9 09:49
 * @Description: 输出位电量
 */
@Data
public class PduEqOutletBaseDo extends PduEqBaseDo {

    /**
     * 输出位
     */
    @JsonProperty("outlet_id")
    private int outletId;
}
