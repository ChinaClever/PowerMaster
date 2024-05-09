package cn.iocoder.yudao.module.cabinet.dto;

import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房-pdu菜单
 * @date 2024/5/6 9:36
 */
@Data
public class RoomPduMenuDTO extends RoomMenuDTO{


    /**
     * IP地址
     */
    private String ip;

    /**
     * 级联地址
     */
    private Integer cas;

}
