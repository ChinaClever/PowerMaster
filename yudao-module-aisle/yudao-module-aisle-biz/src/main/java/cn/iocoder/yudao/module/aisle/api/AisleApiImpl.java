package cn.iocoder.yudao.module.aisle.api;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;
import cn.iocoder.yudao.module.aisle.service.AisleService;
import cn.iocoder.yudao.module.cabinet.api.CabinetApi;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜提供API
 * @date 2024/7/3 16:02
 */
@Service
public class AisleApiImpl implements AisleApi {

    @Resource
    AisleService aisleService;

    @Override
    public void deleteAisle(Integer aisleId) {
        aisleService.deleteAisle(aisleId);
    }
}
