package cn.iocoder.yudao.module.cabinet.service;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.module.cabinet.dto.RoomMenuDTO;
import cn.iocoder.yudao.module.cabinet.dto.RoomPduMenuDTO;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房操作
 * @date 2024/4/30 15:02
 */
public interface RoomService {

    /**
     * 获取机房列表
     *
     * @param name 机房名称
     */
    List<RoomIndex> roomList(String name);


    /**
     * 获取机房菜单
     *
     * @param id 机房id
     */
    List<RoomMenuDTO> roomMenuList(Integer id);

    /**
     * 柜列列表
     *
     * @param roomId
     */
    List<AisleIndex> aisleList(Integer roomId);


    /**
     * 获取机房菜单(全部)
     */
    List<RoomMenuDTO> roomMenuListAll();

    /**
     * 获取机房菜单
     *
     * @param id 机房id
     */
    List<RoomPduMenuDTO> roomPduMenuList(Integer id);
}
