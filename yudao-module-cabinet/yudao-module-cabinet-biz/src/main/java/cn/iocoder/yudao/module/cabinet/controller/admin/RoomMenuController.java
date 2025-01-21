package cn.iocoder.yudao.module.cabinet.controller.admin;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.dto.room.RoomMenuDTO;
import cn.iocoder.yudao.module.cabinet.dto.RoomPduMenuDTO;
import cn.iocoder.yudao.module.cabinet.service.RoomMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房菜单操作
 * @date 2024/5/6 9:09
 */
@Tag(name = "管理后台 - 机房数据")
@RestController
public class RoomMenuController {

    @Autowired
    RoomMenuService roomService;

    /**
     * 机房列表
     *
     * @param name 机房名称
     */
    @Operation(summary = "机房列表")
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
    @Operation(summary = "机房菜单（不包含机房）")
    @GetMapping("/room/menu")
    public CommonResult<List<RoomMenuDTO>> getRoomMenu(@RequestParam(value = "id") Integer id) {
        List<RoomMenuDTO> dto = roomService.roomMenuList(id);
        return success(dto);
    }

    /**
     * 机房菜单全部
     */
    @Operation(summary = "机房菜单")
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
    @Operation(summary = "机房下柜列列表")
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
    @Operation(summary = "机房-pdu菜单")
    @GetMapping("/room/pdu/menu")
    public CommonResult<List<RoomPduMenuDTO>> getRoomPduMenu() {
        List<RoomPduMenuDTO> dto = roomService.roomPduMenuList();
        return success(dto);
    }

    /**
     * 机房菜单
     *
     * @param id 机房id
     */
    @Operation(summary = "机房-机架菜单")
    @GetMapping("/room/rack/menu")
    public CommonResult<List<RoomMenuDTO>> getRoomRackMenu(@RequestParam(value = "id") Integer id) {
        List<RoomMenuDTO> dto = roomService.roomRackMenuList(id);
        return success(dto);
    }


    /**
     * 机房菜单
     *
     */
    @Operation(summary = "机房-始端箱菜单")
    @GetMapping("/room/bus/menu")
    public CommonResult<List<RoomMenuDTO>> getRoomBusMenu() {
        List<RoomMenuDTO> dto = roomService.roomBusMenuList();
        return success(dto);
    }

    /**
     * 机房菜单
     *
     */
    @Operation(summary = "机房-插接箱菜单")
    @GetMapping("/room/box/menu")
    public CommonResult<List<RoomMenuDTO>> getRoomBoxMenu() {
        List<RoomMenuDTO> dto = roomService.roomBoxMenuList();
        return success(dto);
    }


    /**
     * 机房菜单
     *
     */
    @Operation(summary = "机房-柜列菜单")
    @GetMapping("/room/aisle/menu")
    public CommonResult<List<RoomMenuDTO>> getRoomAislexMenu() {
        List<RoomMenuDTO> dto = roomService.roomAisleMenuList();
        return success(dto);
    }
}
