package cn.iocoder.yudao.module.room.controller.admin;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomSavesVo;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.room.dto.*;
import cn.iocoder.yudao.module.room.service.RoomService;
import cn.iocoder.yudao.module.room.vo.RoomSaveVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房主页面
 * @date 2024/4/28 11:12
 */
@Tag(name = "管理后台 - 机房页面")
@RestController
public class RoomController {

    @Autowired
    RoomService roomService;



    /**
     * 机房详情
     *
     * @param id 机房id
     */
    @Operation(summary = "机房详情")
    @GetMapping("/room/detail")
    public CommonResult<RoomDetailDTO> getRoomDetail(@Param("id") int id) {
        RoomDetailDTO dto = roomService.getDetail(id);
        return success(dto);
    }

    @Operation(summary = "新-机房详情")
    @GetMapping("/room/newDetail")
    public CommonResult<RoomAndRoomCfgDTO> getNewRoomDetail(@Param("id") int id) {
        RoomAndRoomCfgDTO dto = roomService.getNewRoomDetail(id);
        return success(dto);
    }


    /**
     * 机房新增/编辑页面
     *
     * @param vo
     */
    @Operation(summary = "机房新增/编辑")
    @PostMapping("/room/save")
    public CommonResult<Integer> saveRoom(@RequestBody RoomSaveVo vo) {
        return success(roomService.roomSave(vo));
    }


    @Operation(summary = "新-机房新增/编辑")
    @PostMapping("/room/newSave")
    public CommonResult<Integer> newSaveRoom(@RequestBody RoomSavesVo vo) {
        return success(roomService.newSaveRoom(vo));
    }

    /**
     * 机房删除
     *
     * @param id 机房id
     */
    @Operation(summary = "机房删除")
    @GetMapping("/room/delete")
    public CommonResult<Integer> deleteRoom(@Param("id") int id) {
        roomService.deleteRoom(id);
        return success(id);
    }

    @Operation(summary = "新-机房删除")
    @GetMapping("/room/newDelete")
    public CommonResult<Integer> newDeleteRoom(@Param("id") int id) {
        roomService.newDeleteRoom(id);
        return success(id);
    }



    /**
     * 机房数据详情
     *
     * @param id 机房id
     */
    @Operation(summary = "机房数据详情")
    @GetMapping("/room/data/detail")
    public CommonResult<RoomDataDTO> getDataDetail(@Param("id") int id) throws IOException {
        RoomDataDTO dto = roomService.getDataDetail(id);
        return success(dto);
    }


    @Operation(summary = "主页面用能数据")
    @GetMapping("/room/main/eq")
    public CommonResult<RoomEqDataDTO> getMainEq(@Param("id") int id) {
        RoomEqDataDTO dto = roomService.getMainEq(id);
        return success(dto);
    }

    /**
     * 主页面数据
     *
     * @param id 柜列id
     */
    @Operation(summary = "主页面设备数据")
    @GetMapping("/room/main/dev/data")
    public CommonResult<RoomDevDataDTO> getMainDevData(@Param("id") int id) {
        RoomDevDataDTO dto = roomService.getMainDevData(id);
        return success(dto);
    }

    /**
     * 主页面数据
     *
     * @param id 柜列id
     */
    @Operation(summary = "主页面功率数据")
    @GetMapping("/room/main/pow/data")
    public CommonResult<RoomPowDataDTO> getMainPowData(@Param("id") int id) {
        RoomPowDataDTO dto = roomService.getMainPowData(id);
        return success(dto);
    }

    /**
     * 主页面数据
     *
     * @param id 柜列id
     */
    @Operation(summary = "主页面曲线数据")
    @GetMapping("/room/main/curve/data")
    public CommonResult<RoomCurveDataDTO> getMainCurveData(@Param("id") int id) {
        RoomCurveDataDTO dto = roomService.getMainCurveData(id);
        return success(dto);
    }

    /**
     * 主页面数据
     *
     * @param id 柜列id
     */
    @Operation(summary = "主页面环境数据")
    @GetMapping("/room/main/env/data")
    public CommonResult<RoomEnvDataDTO> getMainEnvData(@Param("id") int id) throws IOException {
        RoomEnvDataDTO dto = roomService.getMainEnvData(id);
        return success(dto);
    }


    @Operation(summary = "机房机柜新增/编辑")
    @PostMapping("/room/roomAisleSave")
    public CommonResult<Integer> roomAisleSave(@RequestBody AisleSaveVo vo) {
        return success(roomService.roomAisleSave(vo));
    }

    @Operation(summary = "机房柜列新增/编辑")
    @PostMapping("/room/roomCabinetSave")
    public CommonResult<Integer> roomCabinetSave(@RequestBody CabinetSaveVo vo) {
        return success(roomService.roomCabinetSave(vo));
    }


}
