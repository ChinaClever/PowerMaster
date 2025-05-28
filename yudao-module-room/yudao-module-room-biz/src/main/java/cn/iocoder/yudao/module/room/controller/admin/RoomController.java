package cn.iocoder.yudao.module.room.controller.admin;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.aisle.RoomAisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetSaveVo;
import cn.iocoder.yudao.framework.common.dto.room.RoomIndexVo;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomSavesVo;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.HttpUtil;
import cn.iocoder.yudao.framework.common.util.ThreadPoolConfig;
import cn.iocoder.yudao.module.room.dto.*;
import cn.iocoder.yudao.module.room.service.RoomService;
import cn.iocoder.yudao.module.room.vo.RoomIndexAddrResVO;
import cn.iocoder.yudao.module.room.vo.RoomMainResVO;
import cn.iocoder.yudao.module.room.vo.RoomSaveVo;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房主页面
 * @date 2024/4/28 11:12
 */
@Tag(name = "管理后台 - 机房页面")
@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    RoomService roomService;

    @Value("${room-refresh-url}")
    public String adder;

    @Value("${aisle-refresh-url}")
    public String adderAisle;

    @Value("${cabinet-refresh-url}")
    public String adderCabinet;

    /**
     * 机房详情
     *
     * @param id 机房id
     */
    @Operation(summary = "机房详情")
    @GetMapping("/detail")
    public CommonResult<RoomDetailDTO> getRoomDetail(@Param("id") int id) {
        RoomDetailDTO dto = roomService.getDetail(id);
        return success(dto);
    }

    @Operation(summary = "新-机房详情")
    @GetMapping("/newDetail")
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
    @PostMapping("/save")
    public CommonResult<Integer> saveRoom(@RequestBody RoomSaveVo vo) {
        CommonResult<Integer> success = success(roomService.roomSave(vo));
        ThreadPoolConfig.getTHreadPool().execute(() -> {
            HttpUtil.get(adder);
            HttpUtil.get(adderAisle);
            HttpUtil.get(adderCabinet);
        });
        return success;
    }


    @Operation(summary = "新-机房新增/编辑")
    @PostMapping("/newSave")
    public CommonResult<Integer> newSaveRoom(@RequestBody RoomSavesVo vo) {
        Integer i = roomService.newSaveRoom(vo);
        if (i > 0) {
            ThreadPoolConfig.getTHreadPool().execute(() -> {
                HttpUtil.get(adder);
                HttpUtil.get(adderAisle);
                HttpUtil.get(adderCabinet);
            });
        }
        return success(i);
    }

    @Operation(summary = "机房编辑x与y")
    @GetMapping("/findAreaById")
    public CommonResult<Boolean> findAreaById(@RequestParam(value = "xLength") @Parameter(description = "x") Integer xLength,
                                              @RequestParam(value = "yLength") @Parameter(description = "y") Integer yLength,
                                              @RequestParam(value = "id") @Parameter(description = "机房id") Integer id) {
        Boolean i = roomService.findAreaById(xLength, yLength, id);
        return success(i);
    }

    @Operation(summary = "机房新增根据名称异步查询")
    @GetMapping("/newSelectRoomByName")
    public CommonResult<Integer> newSelectRoomByName(@RequestParam(value = "roomName")  String name,
                                                     @RequestParam(value = "id",  required = false) Integer id) {
        Integer i = roomService.newSelectRoomByName(name,id);
        return success(i);
    }


    /**
     * 机房删除
     *
     * @param id 机房id
     */
    @Operation(summary = "机房删除")
    @GetMapping("/delete")
    public CommonResult<Integer> deleteRoom(@Param("id") int id) {
        roomService.deleteRoom(id);
        return success(id);
    }

    @Operation(summary = "新-机房删除")
    @GetMapping("/newDelete")
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
    @GetMapping("/data/detail")
    public CommonResult<RoomDataDTO> getDataDetail(@Param("id") int id) {
        RoomDataDTO dto = roomService.getDataDetail(id);
        return success(dto);
    }

    @Operation(summary = "新的机房数据详情")
    @GetMapping("/data/newDetail")
    public CommonResult<RoomMainResVO> getDataNewDetail(@Param("id") int id) throws ExecutionException, InterruptedException {
        RoomMainResVO dto = roomService.getDataNewDetail(id);
        return success(dto);
    }


    @Operation(summary = "主页面用能数据")
    @GetMapping("/main/eq")
    public CommonResult<RoomEqDataDTO> getMainEq(@Param("id") int id) {
        RoomEqDataDTO dto = roomService.getMainEq(id);
        return success(dto);
    }

    /**
     * 主页面数据
     *
     * @param id 柜列id
     */
    @Operation(summary = "主页面告警设备数据")
    @GetMapping("/main/dev/data")
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
    @GetMapping("/main/pow/data")
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
    @GetMapping("/main/curve/data")
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
    @GetMapping("/main/env/data")
    public CommonResult<RoomEnvDataDTO> getMainEnvData(@Param("id") int id) throws IOException {
        RoomEnvDataDTO dto = roomService.getMainEnvData(id);
        return success(dto);
    }


    @Operation(summary = "机房柜列新增/编辑")
    @PostMapping("/roomAisleSave")
    public CommonResult<Integer> roomAisleSave(@RequestBody RoomAisleSaveVo vo) {
        Integer result = roomService.roomAisleSave(vo);
        if (result > 0) {
            ThreadPoolConfig.getTHreadPool().execute(() -> {
                HttpUtil.get(adder);
                HttpUtil.get(adderAisle);
                HttpUtil.get(adderCabinet);
            });
        }
        return success(result);
    }

    @Operation(summary = "柜列添加验证")
    @PostMapping("/findAddAisleVerify")
    public CommonResult<Boolean> findAddAisleVerify(@RequestBody AisleSaveVo vo) {
        Boolean i = roomService.findAddAisleVerify(vo);
        return success(i);
    }

    @Operation(summary = "机房机柜新增/编辑")
    @PostMapping("/roomCabinetSave")
    public CommonResult<Integer> roomCabinetSave(@RequestBody CabinetSaveVo vo) {
        Integer result = roomService.roomCabinetSave(vo);
        if (result > 0) {
            ThreadPoolConfig.getTHreadPool().execute(() -> {
                HttpUtil.get(adder);
                HttpUtil.get(adderAisle);
                HttpUtil.get(adderCabinet);
            });
        }
        return success(result);
    }


    @Operation(summary = "获得已删除机房分页")
    @PostMapping("/deletedRoomPage")
    public CommonResult<PageResult<JSONObject>> getDeletedRoomPage(@RequestBody RoomIndexVo pageReqVO) {
        PageResult<JSONObject> pageResult = roomService.getDeletedRoomPage(pageReqVO);
        return success(pageResult);
    }

    @Operation(summary = "恢复已删除机房")
    @GetMapping("/restoreRoomInfo")
    public CommonResult<Integer> getRestoreRoom(@Param("id") int id) {
        roomService.getRestoreRoom(id);
        return success(id);
    }


    @Operation(summary = "获取机房楼层列表")
    @GetMapping("/getRoomAddrList")
    public CommonResult<List<String>> getRoomAddrList() {
        return success(roomService.getRoomAddrList());
    }

    @Operation(summary = "机房监测")
    @PostMapping("/roomList")
    public CommonResult<List<RoomIndexAddrResVO>> getRoomList(@RequestParam(value = "addr", required = false) @Parameter(description = "地址（楼层）") String addr,
                                                              @RequestParam(value = "roomName", required = false) @Parameter(description = "机房名称") String roomName) {
        List<RoomIndexAddrResVO> roomIndexAddrResVO = roomService.getRoomList(addr, roomName);
        return success(roomIndexAddrResVO);
    }

    @Operation(summary = "机房监测All")
    @GetMapping("/getRoomAddrListAll")
    public CommonResult<Map<String, List<RoomIndexAddrResVO>>> getRoomAddrListAll(@RequestParam(value = "addr", required = false) @Parameter(description = "地址（楼层）") String addr,
                                                                                  @RequestParam(value = "roomName", required = false) @Parameter(description = "机房名称") String roomName) {
        return success(roomService.getRoomAddrListAll(addr, roomName));
    }

    @Operation(summary = "机房编辑导出")
    @PostMapping("/editAisleExport")
    public void editAisleExport(Integer roomId, Integer aisleId, HttpServletResponse response) throws IOException {
        roomService.editAisleExport(roomId, aisleId,response);
    }

    @PostMapping("/editAisleExcel")
    @Operation(summary = "柜列导入")
    public CommonResult<Boolean> editAisleExcel(@RequestParam(value = "file") MultipartFile file, HttpServletRequest request) throws Exception{
        Boolean flag = roomService.editAisleExcel(file, request);
        return CommonResult.success(flag);
    }
}
