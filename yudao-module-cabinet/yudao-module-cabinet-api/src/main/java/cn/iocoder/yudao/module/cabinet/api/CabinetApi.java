package cn.iocoder.yudao.module.cabinet.api;

import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetDTO;
import cn.iocoder.yudao.framework.common.dto.cabinet.CabinetVo;

/**
 * @author luowei
 * @version 1.0
 * @description: 提供给其他模块调用
 * @date 2024/7/3 15:57
 */
public interface CabinetApi {

    /**
     * 机柜保存
     * @param vo
     */
    void saveCabinet(CabinetVo vo) throws Exception;


    CabinetDTO getDetail(int id);

    /**
     * 机柜删除
     */
    void delCabinet(int id) throws Exception;

}
