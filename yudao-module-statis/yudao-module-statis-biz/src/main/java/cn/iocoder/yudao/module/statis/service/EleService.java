package cn.iocoder.yudao.module.statis.service;

/**
 * @Author: chenwany
 * @Date: 2024/4/9 10:10
 * @Description: 电量数据处理
 */
public interface EleService {

    /**
     * 总电量按天统计
     */
    void eleTotalDayDeal();

    /**
     * 总电量按周统计
     */
    void eleTotalWeekDeal();

    /**
     * 总电量按月统计
     */
    void eleTotalMonthDeal();

    /**
     * 输出位电量按天统计
     */
    void eleOutletDayDeal();

    /**
     * 输出位电量按周统计
     */
    void eleOutletWeekDeal();

    /**
     * 输出位电量按月统计
     */
    void eleOutletMonthDeal();
}
