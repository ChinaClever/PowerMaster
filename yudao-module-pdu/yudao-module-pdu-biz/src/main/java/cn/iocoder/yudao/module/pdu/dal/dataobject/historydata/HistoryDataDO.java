package cn.iocoder.yudao.module.pdu.dal.dataobject.historydata;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * pdu历史数据 DO
 *
 * @author 芋道源码
 */
@Data
public class HistoryDataDO{

    private Long id;

    @JsonProperty("pdu_id")
    private Long pduId;

    @JsonProperty("active_pow")
    private float activePow;

    @JsonProperty("apparent_pow")
    private float apparentPow;

    @JsonProperty("power_factor")
    private Integer powerFactor;

    @JsonProperty("create_time")
    private DateTime createTime;


}