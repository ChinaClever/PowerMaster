package cn.iocoder.yudao.module.aisle.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import netscape.javascript.JSObject;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜数据详情
 * @date 2024/6/21 15:49
 */
@Data
public class CabinetDetailDataDTO {

    /**
     * 机柜id
     */
    private int id;

    /**
     * 负载率
     */
    private float loadRate;
    /**
     * A路相电流
     */
    private  float[] lineCurA;

    /**
     * b路相电流
     */
    private  float[] lineCurB;

    /**
     * A路相电压
     */
    private  float[] lineVolA;

    /**
     * B路相电压
     */
    private  float[] lineVolB;

    /**
     * 总功率因素
     */
    private float powerFactor;

    /**
     * A功率因素
     */
    private float powerFactorA;

    /**
     * b功率因素
     */
    private float powerFactorB;

    /**
     * 有功功率
     */
    private  float powActive;

    /**
     * a有功功率
     */
    private  float powActiveA;

    /**
     * b有功功率
     */
    private  float powActiveB;

    /**
     * 无功功率
     */
    private  float powReactive;

    /**
     * A无功功率
     */
    private  float powReactiveA;

    /**
     * B无功功率
     */
    private  float powReactiveB;
    /**
     * 视在功率
     */
    private  float powApparent;

    /**
     * a视在功率
     */
    private  float powApparentA;


    /**
     * b视在功率
     */
    private  float powApparentB;


    /**
     * 温度
     */
    private JSONObject temData;
}
