package cn.iocoder.yudao.framework.common.entity.es.pdu.ele.outlet;

import cn.iocoder.yudao.module.statis.entity.ele.total.PduEleTotalRealtimeDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/8 15:04
 * @Description: pdu输出位电能表
 */
@Data
public class PduEleOutletDo extends PduEleTotalRealtimeDo {

    /**
     * 输出位
     */
    @JsonProperty("outlet_id")
    private int outletId;
}
