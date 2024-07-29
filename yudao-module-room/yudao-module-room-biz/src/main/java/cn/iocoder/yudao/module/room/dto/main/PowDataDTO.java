package cn.iocoder.yudao.module.room.dto.main;

import cn.iocoder.yudao.module.room.dto.RoomPowDataDTO;
import lombok.Data;

import java.util.List;


/**
 * @author luowei
 * @version 1.0
 * @description: 功率数据详情
 * @date 2024/6/21 15:49
 */
@Data
public class PowDataDTO {

    /**
     * 机房数据
     */
    List<RoomPowDataDTO> roomDataList;


    /**
     * 有功功率
     */
    private  float  totalPowActive;

    /**
     * 无功功率
     */
    private  float  totalPowReactive;

    /**
     * 视在功率
     */
    private  float  totalPowApparent;

    /**
     * 视在功率
     */
    private  float  totalPowerFactor;


}
