package cn.iocoder.yudao.module.bus.service;

import cn.iocoder.yudao.module.bus.dto.BusDataDTO;
import cn.iocoder.yudao.module.bus.vo.BusIndexVo;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2024/5/30 14:45
 */
public interface BusService {
    /**
     * 母线拓扑数据
     *
     * @param vo
     * @return
     */
    List<BusDataDTO> getBusData(BusIndexVo vo);
}
