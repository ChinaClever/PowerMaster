package cn.iocoder.yudao.module.statis.entity.ele.line;

import cn.iocoder.yudao.module.statis.entity.ele.total.PduEleTotalRealtimeDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/8 15:04
 * @Description: pdu相电能表
 */
@Data
public class PduEleLineDo extends PduEleTotalRealtimeDo {

    /**
     * 相
     */
    @JsonProperty("line_id")
    private int lineId;
}
