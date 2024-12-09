package cn.iocoder.yudao.module.aisle.dto;

import lombok.Data;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列详情数据
 * @date 2024/6/21 15:49
 */
@Data
public class AisleDataDTO {


     /**
     * A路母线
     */
    private BusDetailDataDTO barA;
    /**
     * B路母线
     */
    private BusDetailDataDTO barB;

    //机柜
    /**
     * 机柜数据
     */
    private List<CabinetDetailDataDTO> cabinetList;

    /**
     * 柜列iD
     */
    private Integer id;

}
