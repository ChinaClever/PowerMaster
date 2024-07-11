package cn.iocoder.yudao.module.aisle.dto;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列列表数据
 * @date 2024/6/21 15:49
 */
@Data
public class AisleListDTO {



    private int roomId;


    /**
     * 机房名
     */
    private String roomName;
    /**
     * 柜列数据
     */
    private List<AisleIndex>  aisleList;


}
