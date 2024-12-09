package cn.iocoder.yudao.framework.common.entity.es.pdu.ele.total;

import cn.iocoder.yudao.framework.common.entity.es.pdu.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author chenwany
 * @Date: 2024/3/28 14:47
 * @Description: pdu总电能表(实时)
 */
@Data
public class PduEleTotalRealtimeDo extends PduBaseDo {


    /**
     * 电能
     */
    @JsonProperty("ele_active")
    private double ele;


}
