package cn.iocoder.yudao.module.aisle.dto;

import lombok.Data;
import org.elasticsearch.threadpool.Scheduler;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 始端箱数据详情
 * @date 2024/6/21 15:49
 */
@Data
public class BusDetailDataDTO {

    private int id;


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
     * 温度
     */
    private float[] temData;

    /**
     * 插接箱数据
     */
    private List<BoxDetailDataDTO>  boxList;


}
