package cn.iocoder.yudao.module.statis.service;

/**
 * @Author: chenwany
 * @Date: 2024/4/3 14:40
 * @Description: 回路历史数据统计
 */
public interface LoopService {

    /**
     * 按小时统计
     */
    void hourDeal();


    /**
     * 按天统计
     */
    void dayDeal();

}
