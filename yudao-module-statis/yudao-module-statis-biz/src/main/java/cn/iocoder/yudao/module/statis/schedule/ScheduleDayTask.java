package cn.iocoder.yudao.module.statis.schedule;

import cn.iocoder.yudao.module.statis.service.LineService;
import cn.iocoder.yudao.module.statis.service.LoopService;
import cn.iocoder.yudao.module.statis.service.OutletService;
import cn.iocoder.yudao.module.statis.service.TotalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author: chenwany
 * @Date: 2024/4/2 16:53
 * @Description: 定时任务（天）
 */
@Slf4j
@EnableAsync
@Component
public class ScheduleDayTask {

    @Autowired
    LoopService loopService;
    @Autowired
    LineService lineService;
    @Autowired
    TotalService totalService;
    @Autowired
    OutletService outletService;
    /**
     * 每天凌晨六点执行
     */
    @Scheduled(cron = "${cron.day}")
    @Async()
    public void scheduledLoopTask() {
        long start = System.currentTimeMillis();
        loopService.dayDeal();
        long end = System.currentTimeMillis();
        log.info("回路按天统计结束：" + (end-start));
    }

    /**
     * 每天凌晨六点执行
     */
    @Scheduled(cron = "${cron.day}")
    @Async()
    public void scheduledLineTask() {
        long start = System.currentTimeMillis();
        lineService.dayDeal();
        long end = System.currentTimeMillis();
        log.info("相按天统计结束：" + (end-start));
    }


    /**
     * 每天凌晨六点执行
     */
    @Scheduled(cron = "${cron.day}")
    @Async()
    public void scheduledOutletTask() {
        long start = System.currentTimeMillis();
        outletService.dayDeal();
        long end = System.currentTimeMillis();
        log.info("输出位按天统计结束：" + (end-start));
    }


    /**
     * 每天凌晨六点执行
     */
    @Scheduled(cron = "${cron.day}")
    @Async()
    public void scheduledTotalTask() {
        long start = System.currentTimeMillis();
        totalService.dayDeal();
        long end = System.currentTimeMillis();
        log.info("总历史数据按天统计结束：" + (end-start));
    }

}
