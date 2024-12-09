package cn.iocoder.yudao.module.aisle.dto;

import lombok.Data;
import netscape.javascript.JSObject;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱数据详情
 * @date 2024/6/21 15:49
 */
@Data
public class BoxDetailDataDTO {

    private int id;

    /**
     * 位置
     */
    private int boxIndex;

    private String devKey;
    /**
     * 相负载率
     */
    private float[] lineLoadRate;
    /**
     * 相电流
     */
    private  float[] lineCur;

    /**
     * 相电压
     */
    private  float[] lineVol;


    /**
     * 输出位功率因素
     */
    private float[] powerFactor;

    /**
     * 输出位有功功率
     */
    private  float[] powActive;


    /**
     * 输出位无功功率
     */
    private  float[] powReactive;

    /**
     * 输出位视在功率
     */
    private  float[] powApparent;


    /**
     * 温度
     */
    private float[] temData;


}
