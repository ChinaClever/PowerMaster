package cn.iocoder.yudao.framework.common.entity.es.pdu.outlet;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.module.statis.entity.total.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/3 09:16
 * @Description: 输出位历史基础数据
 */
@Data
public class PduHdaOutletBaseDo extends PduBaseDo {

    /**
     * 输出位
     */
    @JsonProperty("outlet_id")
    private int outletId;

    /**
     * 平均电流
     */
    @JsonProperty("cur_avg_value")
    private float curAvgValue;

    /**
     * 最大电流时间
     */
    @JsonProperty("cur_max_time")
    private DateTime curMaxTime;

    /**
     * 最大电流
     */
    @JsonProperty("cur_max_value")
    private float curMaxValue;


    /**
     * 最小电流时间
     */
    @JsonProperty("cur_min_time")
    private DateTime curMinTime;

    /**
     * 最小电流
     */
    @JsonProperty("cur_min_value")
    private float curMinValue;
}
