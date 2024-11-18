package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import cn.iocoder.yudao.framework.common.entity.es.pdu.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "PDU需量详情数据/电流(小时/天)")
@Data
public class PduHdaLineMaxResVO extends PduBaseDo {

    @Schema(description = "相")
    @JsonProperty("line_id")
    private Integer lineId;
    /**
     *平均电压
     */
    @Schema(description = "电流")
    @JsonProperty("cur_max_value")
    private BigDecimal curValue;
    /**
     * 平均电流
     */
    @Schema(description = "功率")
    @JsonProperty("pow_active_max_value")
    private BigDecimal powActiveValue;
}
