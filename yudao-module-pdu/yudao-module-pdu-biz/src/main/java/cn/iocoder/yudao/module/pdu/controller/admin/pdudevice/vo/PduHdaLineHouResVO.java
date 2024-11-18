package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo;

import cn.iocoder.yudao.framework.common.entity.es.pdu.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
@Schema(description = "pdu相历史电压/电流(小时/天)")
@Data
public class PduHdaLineHouResVO  extends PduBaseDo {

    @Schema(description = "相")
    @JsonProperty("line_id")
    private Integer lineId;
    /**
     *平均电压
     */
    @Schema(description = "电压")
    @JsonProperty("vol_avg_value")
    private BigDecimal volValue;
    /**
     * 平均电流
     */
    @Schema(description = "电流")
    @JsonProperty("cur_avg_value")
    private BigDecimal curValue;
}
