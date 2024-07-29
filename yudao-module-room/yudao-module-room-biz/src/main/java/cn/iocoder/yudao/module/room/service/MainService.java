package cn.iocoder.yudao.module.room.service;

import cn.iocoder.yudao.module.room.dto.main.DevDataDTO;
import cn.iocoder.yudao.module.room.dto.main.EqDataDTO;
import cn.iocoder.yudao.module.room.dto.main.PowDataDTO;

/**
 * @author luowei
 * @version 1.0
 * @description:
 * @date 2024/6/21 10:31
 */
public interface MainService {


    /**
     * 主页面用能数据
     * @return
     */
    EqDataDTO getMainEq();

    /**
     * 主页面数据
     * @return
     */
    DevDataDTO getMainDevData();

    /**
     * 主页面数据
     * @return
     */
    PowDataDTO getMainPowData();

}
