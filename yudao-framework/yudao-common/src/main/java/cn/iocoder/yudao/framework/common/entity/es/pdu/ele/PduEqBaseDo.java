package cn.iocoder.yudao.framework.common.entity.es.pdu.ele;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.pdu.PduBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: chenwany
 * @Date: 2024/4/9 09:39
 * @Description: 电量计算基础数据
 */
@Data
public class PduEqBaseDo extends PduBaseDo {

    /**
     * 开始时间
     */
    @JsonProperty("start_time")
    private DateTime startTime;

    /**
     * 结束时间
     */
    @JsonProperty("end_time")
    private DateTime endTime;

    /**
     * 开始电能
     */
    @JsonProperty("start_ele")
    private double startEle;

    /**
     * 结束电能
     */
    @JsonProperty("end_ele")
    private double endEle;

    /**
     * 电量
     */
    @JsonProperty("eq_value")
    private double eq;

    /**
     * 电费
     */
    @JsonProperty("bill_value")
    private double bill;


    /**
     * 计费方式
     */
    @JsonProperty("bill_mode")
    private int billMode;

    /**
     * 计费时间段
     */
    @JsonProperty("bill_period")
    private String billPeriod;
    /**
     * 计费方式(实际计费方式)
     */
    @JsonProperty("bill_mode_real")
    private Integer billModeReal;
}
