package cn.iocoder.yudao.module.room.service;

import cn.iocoder.yudao.module.room.dto.*;
import cn.iocoder.yudao.module.room.vo.RoomSaveVo;

import java.io.IOException;

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
}
