package cn.iocoder.yudao.module.room.service;

import cn.iocoder.yudao.framework.common.dto.aisle.AisleSaveVo;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetSaveVo;
import cn.iocoder.yudao.framework.common.dto.room.RoomIndexVo;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomSavesVo;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.room.dto.*;
import cn.iocoder.yudao.module.room.vo.RoomIndexAddrResVO;
import cn.iocoder.yudao.module.room.vo.RoomMainResVO;
import cn.iocoder.yudao.module.room.vo.RoomSaveVo;
import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author luowei
 * @version 1.0
 * @description: 机房操作
 * @date 2024/6/21 10:31
 */
public interface RoomService {


    /**
     * 机房保存
     */
    Integer  roomSave(RoomSaveVo roomSaveVo);


    /**
     * 删除机房
     * @param roomId 机房iD
     */
    void deleteRoom(Integer roomId);


    /**
     * 获取机房详情
     * @param roomId 机房id
     */
    RoomDetailDTO getDetail(Integer roomId);

    /**
     * 获取机柜数据
     * @param id
     * @return
     */
    RoomDataDTO getDataDetail(int id);

    /**
     * 主页面用能数据
     * @param id
     * @return
     */
    RoomEqDataDTO getMainEq(int id);

    /**
     * 主页面数据
     * @param id
     * @return
     */
    RoomDevDataDTO getMainDevData(int id);

    /**
     * 主页面数据
     * @param id
     * @return
     */
    RoomPowDataDTO getMainPowData(int id);

    /**
     * 主页面数据
     * @param id
     * @return
     */
    RoomCurveDataDTO getMainCurveData(int id);

    /**
     * 主页面数据
     * @param id
     * @return
     */
    RoomEnvDataDTO getMainEnvData(int id) throws IOException;

    Integer newSaveRoom(RoomSavesVo vo);

    /**
     * 新-机房删除
     * @param id
     */
    void newDeleteRoom(int id);

    /**
     * 机房机列新增/编辑
     * @param vo
     * @return
     */
    Integer roomAisleSave(AisleSaveVo vo);

    /**
     * 新-roomAisleDelete
     * @param id
     * @return
     */
    RoomAndRoomCfgDTO getNewRoomDetail(int id);

    /**
     * 机房柜列新增/编辑
     * @param
     * @return
     */
    Integer roomCabinetSave(CabinetSaveVo vo);


    PageResult<JSONObject> getDeletedRoomPage(RoomIndexVo pageReqVO);

    //恢复已删除机房
    void getRestoreRoom(int id);

    List<RoomIndexAddrResVO> getRoomList(String addr, String roomName);

    List<String> getRoomAddrList();

    RoomMainResVO getDatanewDetail(int id) throws ExecutionException, InterruptedException;

    //柜列编辑删除
    Integer roomAisleDeleteById(int id);

    //机房新增根据名称异步查询
    Integer newSelectRoomByName(String name);

    void getRoomListRedis(List<RoomIndexAddrResVO> bean);
}
