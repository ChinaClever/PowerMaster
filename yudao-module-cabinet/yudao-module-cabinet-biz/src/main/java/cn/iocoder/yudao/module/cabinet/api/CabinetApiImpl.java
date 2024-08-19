package cn.iocoder.yudao.module.cabinet.api;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.module.cabinet.service.CabinetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜提供API
 * @date 2024/7/3 16:02
 */
@Service
public class CabinetApiImpl implements CabinetApi{

    @Resource
    CabinetService cabinetService;

    @Override
    public void saveCabinet(CabinetVo vo) throws Exception {
        cabinetService.saveCabinet(vo);
    }

    @Override
    public CabinetDTO getDetail(int id) {
        return cabinetService.getCabinetDetailV2(id);
    }

    @Override
    public void delCabinet(int id) throws Exception {
        cabinetService.delCabinet(id);
    }
}
