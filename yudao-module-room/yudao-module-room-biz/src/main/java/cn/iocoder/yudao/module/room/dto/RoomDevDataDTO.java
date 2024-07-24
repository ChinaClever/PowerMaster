package cn.iocoder.yudao.module.room.dto;

import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 主页面设备数据
 * @date 2024/6/21 15:49
 */
@Data
public class RoomDevDataDTO {

    /**
     * 机房iD
     */
    private Integer id;

     /**
     * 告警数量
     */
    private int alarmNum;

    /**
     * 正常数量
     */
    private int normalNum;

    /**
     * 离线数量
     */
    private int offLineNum;

    /**
     * 设备数量
     */
    private int deviceNum;



}
