package cn.iocoder.yudao.module.room.vo;

import lombok.Data;


/**
 * @author luowei
 * @version 1.0
 * @description: 机房详情
 * @date 2024/6/21 15:49
 */
@Data
public class RoomIndexAddrResVO {

    /**
     * 机房iD
     */
    private Integer id;


    /**
     * 机房名
     */
    private String roomName;

    private String addr;

    /**
     * 总功率因素
     */
    private Double powerFactor;


    /**
     * 有功功率
     */
    private  Double powActive;

    /**
     * 无功功率
     */
    private  Double powReactive;

    /**
     * 视在功率
     */
    private  Double powApparent;

    /**
     * 电能
     */
    private  Double eleActive;

    private Double pue;


}
