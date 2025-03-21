package cn.iocoder.yudao.module.room.dto;

import cn.iocoder.yudao.framework.common.dto.room.AisleDataDTO;
import cn.iocoder.yudao.framework.common.dto.room.RoomCabinetDTO;
import lombok.Data;

import java.util.List;


/**
 * @author luowei
 * @version 1.0
 * @description: 机房详情
 * @date 2024/6/21 15:49
 */
@Data
public class RoomDataDTO {

    /**
     * 机房iD
     */
    private Integer id;




    //柜列数据
    List<AisleDataDTO> aisleList;


    /**
     * 机柜数据
     */
    private List<RoomCabinetDTO> cabinetList;


}
