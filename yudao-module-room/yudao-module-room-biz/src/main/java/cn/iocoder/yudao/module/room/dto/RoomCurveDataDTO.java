package cn.iocoder.yudao.module.room.dto;

import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 主页面曲线数据
 * @date 2024/6/21 15:49
 */
@Data
public class RoomCurveDataDTO {

    /**
     * 机房iD
     */
    private Integer id;

    /**
     *  功率曲线
     */
    private List<RoomPowDTO> powList;

    /**
     * 能耗曲线
     */
    private List<RoomEqDTO>  eqList;



}
