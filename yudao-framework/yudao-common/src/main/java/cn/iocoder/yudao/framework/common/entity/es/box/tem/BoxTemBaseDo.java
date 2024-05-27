package cn.iocoder.yudao.framework.common.entity.es.box.tem;

import cn.hutool.core.date.DateTime;
import cn.iocoder.yudao.framework.common.entity.es.box.BoxBaseDo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱温度数据表
 * @date 2024/5/21 14:52
 */
@Data
public class BoxTemBaseDo extends BoxBaseDo {

    /**
     * 平均A路温度
     */
    @JsonProperty("tem_a_avg_value")
    private float temAAvgValue;


    /**
     * 最大A路温度
     */
    @JsonProperty("tem_a_max_value")
    private float temAMaxValue;


    /**
     * 最高A路温度时间
     */
    @JsonProperty("tem_a_max_time")
    private DateTime temAMaxTime;


    /**
     * 最低A路温度
     */
    @JsonProperty("tem_a_min_value")
    private float temAMinValue;


    /**
     * 最低A路温度时间
     */
    @JsonProperty("tem_a_min_time")
    private DateTime temAMinTime;


    /**
     * 平均B路温度
     */
    @JsonProperty("tem_b_avg_value")
    private float temBAvgValue;


    /**
     * 最大B路温度
     */
    @JsonProperty("tem_b_max_value")
    private float temBMaxValue;


    /**
     * 最高B路温度时间
     */
    @JsonProperty("tem_b_max_time")
    private DateTime temBMaxTime;


    /**
     * 最低B路温度
     */
    @JsonProperty("tem_b_min_value")
    private float temBMinValue;


    /**
     * 最低B路温度时间
     */
    @JsonProperty("tem_b_min_time")
    private DateTime temBMinTime;

    /**
     * 平均C路温度
     */
    @JsonProperty("tem_c_avg_value")
    private float temCAvgValue;


    /**
     * 最大C路温度
     */
    @JsonProperty("tem_c_max_value")
    private float temCMaxValue;


    /**
     * 最高C路温度时间
     */
    @JsonProperty("tem_c_max_time")
    private DateTime temCMaxTime;


    /**
     * 最低C路温度
     */
    @JsonProperty("tem_c_min_value")
    private float temCMinValue;


    /**
     * 最低C路温度时间
     */
    @JsonProperty("tem_c_min_time")
    private DateTime temCMinTime;

    /**
     * 平均中线温度
     */
    @JsonProperty("tem_n_avg_value")
    private float temNAvgValue;


    /**
     * 最大中线温度
     */
    @JsonProperty("tem_n_max_value")
    private float temNMaxValue;


    /**
     * 最高中线温度时间
     */
    @JsonProperty("tem_n_max_time")
    private DateTime temNMaxTime;


    /**
     * 最低中线温度
     */
    @JsonProperty("tem_n_min_value")
    private float temNMinValue;


    /**
     * 最低中线温度时间
     */
    @JsonProperty("tem_n_min_time")
    private DateTime temNMinTime;

}
