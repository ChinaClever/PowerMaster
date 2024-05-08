package cn.iocoder.yudao.module.cabinet.service;

import cn.iocoder.yudao.module.cabinet.dto.CabinetEleChainDTO;
import cn.iocoder.yudao.module.cabinet.dto.CabinetEqTrendDTO;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜能耗数据
 * @date 2024/5/6 17:06
 */
public interface CabinetEleService {

    /**
     * 获取机柜用能环比
     *
     * @param id 机柜id
     * @return
     */
    CabinetEleChainDTO getEleChain(int id);

    /**
     * 获取机柜能耗趋势
     *
     * @param id   机柜id
     * @param type 数据时间类型
     * @return
     */
    List<CabinetEqTrendDTO> eqTrend(int id, String type);
}
