package cn.iocoder.yudao.framework.common.entity.es.pdu;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * @Author: chenwany
 * @Date: 2024/3/29 09:20
 * @Description: 基础实体类
 */
@Data
public class PduBaseDo {
    @Schema(description = "id")
    private int id;

    @Schema(description = "PDU_ID")
    @JsonProperty("pdu_id")
    private int pduId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime createTime;
}
