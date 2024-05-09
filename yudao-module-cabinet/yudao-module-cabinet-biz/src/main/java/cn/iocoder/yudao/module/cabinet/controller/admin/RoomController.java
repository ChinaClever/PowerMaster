package cn.iocoder.yudao.module.cabinet.controller.admin;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.cabinet.dto.RoomMenuDTO;
import cn.iocoder.yudao.module.cabinet.dto.RoomPduMenuDTO;
import cn.iocoder.yudao.module.cabinet.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房操作
 * @date 2024/5/6 9:09
 */
@RestController
public class RoomController {

    @Autowired
    RoomService roomService;

    /**
     * 机房列表
     *
     * @param name 机房名称
     */
    @GetMapping("/room/list")
    public CommonResult<List<RoomIndex>> getRoomList(@RequestParam(value = "name", required = false) String name) {
        List<RoomIndex> dto = roomService.roomList(name);
        return success(dto);
    }

    /**
     * 机房菜单
     *
     * @param id 机房id
     */
    @GetMapping("/room/menu")
    public CommonResult<List<RoomMenuDTO>> getRoomMenu(@RequestParam(value = "id") Integer id) {
        List<RoomMenuDTO> dto = roomService.roomMenuList(id);
        return success(dto);
    }

    /**
     * 机房菜单全部
     */
    @GetMapping("/room/menuAll")
    public CommonResult<List<RoomMenuDTO>> getRoomMenuAll() {
        List<RoomMenuDTO> dto = roomService.roomMenuListAll();
        return success(dto);
    }

    /**
     * 机房下柜列列表
     *
     * @param roomId 机房id
     */
    @GetMapping("/room/aisleList")
    public CommonResult<List<AisleIndex>> aisleList(@RequestParam(value = "roomId") Integer roomId) {
        List<AisleIndex> dto = roomService.aisleList(roomId);
        return success(dto);
    }

    /**
     * 机房菜单
     *
     * @param id 机房id
     */
    @GetMapping("/room/pdu/menu")
    public CommonResult<List<RoomPduMenuDTO>> getRoomPduMenu(@RequestParam(value = "id") Integer id) {
        List<RoomPduMenuDTO> dto = roomService.roomPduMenuList(id);
        return success(dto);
    }
}
