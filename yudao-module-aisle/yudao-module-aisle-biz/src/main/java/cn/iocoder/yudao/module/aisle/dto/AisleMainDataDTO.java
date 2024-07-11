package cn.iocoder.yudao.module.aisle.dto;

import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列主页面数据
 * @date 2024/6/21 15:49
 */
@Data
public class AisleMainDataDTO {


     /**
     * 始端箱相电流
     */
    private float[] barLineCurA;

    private float[] barLineCurB;

    /**
     * 始端箱相电压
     */
    private float[] barLineVolA;

    private float[] barLineVolB;

    //机柜
    /**
     * 机柜数据
     */
    private List<CabinetMainDataDTO> cabinetList;


    /**
     * 柜列iD
     */
    private Integer id;

    /**
     * 总功率因素
     */
    private float powerFactor;


    /**
     * 有功功率
     */
    private  float powActive;

    /**
     * 无功功率
     */
    private  float powReactive;

    /**
     * 视在功率
     */
    private  float powApparent;

}
