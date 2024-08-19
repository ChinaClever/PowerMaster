package cn.iocoder.yudao.module.aisle.api;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;

/**
 * @author luowei
 * @version 1.0
 * @description: 提供给其他模块调用
 * @date 2024/7/3 15:57
 */
public interface AisleApi {


    /**
     * 删除柜列
     * @param aisleId 柜列id
     */
    void deleteAisle(Integer aisleId);

}
