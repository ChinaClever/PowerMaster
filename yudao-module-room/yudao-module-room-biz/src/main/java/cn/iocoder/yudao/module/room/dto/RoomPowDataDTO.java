package cn.iocoder.yudao.module.room.dto;

import lombok.Data;

import java.util.List;


/**
 * @author luowei
 * @version 1.0
 * @description: 机房详情
 * @date 2024/6/21 15:49
 */
@Data
public class RoomPowDataDTO {

    /**
     * 机房iD
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
