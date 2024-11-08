package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import cn.iocoder.yudao.framework.common.entity.es.pdu.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
@Schema(description = "pdu相历史电压/电流")
@Data
public class PduHdaLineRealtimeResVO extends PduBaseDo {
    /**
     *相
     */
    @Schema(description = "相")
    @JsonProperty("line_id")
    private Integer lineId;
    /**
     *电压
     */
    @Schema(description = "电压")
    @JsonProperty("vol_value")
    private BigDecimal volValue;
    /**
     * 电流
     */
    @Schema(description = "电流")
    @JsonProperty("cur_value")
    private BigDecimal curValue;

}
