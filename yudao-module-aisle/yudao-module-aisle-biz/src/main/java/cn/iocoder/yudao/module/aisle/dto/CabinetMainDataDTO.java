package cn.iocoder.yudao.module.aisle.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜主页面详情
 * @date 2024/6/21 15:49
 */
@Data
public class CabinetMainDataDTO {

    /**
     * 机柜id
     */
    private int id;

    /**
     * 机柜名称
     */
    private String cabinetName;


    /**
     * A功率因素
     */
    private float powerFactorA;

    /**
     * b功率因素
     */
    private float powerFactorB;


    /**
     * a有功功率
     */
    private  float powActiveA;

    /**
     * b有功功率
     */
    private  float powActiveB;


    /**
     * A无功功率
     */
    private  float powReactiveA;

    /**
     * B无功功率
     */
    private  float powReactiveB;

    /**
     * a视在功率
     */
    private  float powApparentA;


    /**
     * b视在功率
     */
    private  float powApparentB;


}
