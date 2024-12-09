package cn.iocoder.yudao.module.cabinet.service;

import cn.iocoder.yudao.module.cabinet.dto.CabinetActivePowDTO;
import cn.iocoder.yudao.module.cabinet.dto.CabinetPowDTO;
import cn.iocoder.yudao.module.cabinet.vo.CabinetPowVo;

import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 功率数据业务
 * @date 2024/4/30 11:21
 */
public interface CabinetPowService {

    /**
     * 实时曲线功率数据
     *
     * @param vo
     * @return
     */
    List<CabinetPowDTO> getPowList(CabinetPowVo vo);

    /**
     * 总有功功率数据
     *
     * @return
     */
    CabinetActivePowDTO getActivePow(CabinetPowVo vo);
}
