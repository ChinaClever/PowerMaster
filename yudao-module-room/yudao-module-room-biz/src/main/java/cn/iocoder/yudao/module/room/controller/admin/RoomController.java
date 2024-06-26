package cn.iocoder.yudao.module.room.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.room.dto.RoomDetailDTO;
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


    /**
     * 机房新增/编辑页面
     *
     * @param vo
     */
    @Operation(summary = "机房新增/编辑")
    @PostMapping("/room/save")
    public CommonResult<Integer> saveRoom(@RequestBody RoomSaveVo vo) throws Exception {
        return success(roomService.roomSave(vo));
    }


    /**
     * 机房删除
     *
     * @param id 机房id
     */
    @Operation(summary = "机房删除")
    @GetMapping("/room/delete")
    public CommonResult<Integer> deleteRoom(@Param("id") int id) throws Exception {
       roomService.deleteRoom(id);
        return success(id);
    }

}
