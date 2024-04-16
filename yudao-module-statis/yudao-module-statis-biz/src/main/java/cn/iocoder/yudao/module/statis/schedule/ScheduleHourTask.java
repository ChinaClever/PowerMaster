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
 * @Date: 2024/4/2 16:52
 * @Description: 定时任务（小时）
 */
@Slf4j
@EnableAsync
@Component
public class ScheduleHourTask {

    @Autowired
    LoopService loopService;
    @Autowired
    LineService lineService;
    @Autowired
    TotalService totalService;
    @Autowired
    OutletService outletService;

    /**
     * 每小时执行一次
     */
//    @Scheduled(cron = "${cron.day}")
    //测试。每五分钟执行一次
//    @Scheduled(cron = "0 0/2 * * * ?")
    @Scheduled(cron = "${cron.hour}")
    @Async()
    public void scheduledLoopTask() {
        log.info("定时任务1开始：" + System.currentTimeMillis());
        loopService.hourDeal();
        log.info("定时任务1结束：" + System.currentTimeMillis());
    }



    //测试。每五分钟执行一次
//    @Scheduled(cron = "0 0/5 * * * ?")
    @Scheduled(cron = "${cron.hour}")
    @Async()
    public void scheduledLineTask() {
        log.info("定时任务2开始：" + System.currentTimeMillis());
        lineService.hourDeal();
        log.info("定时任务2结束：" + System.currentTimeMillis());
    }

    // @Scheduled(cron = "${cron.day}")
    //测试。每五分钟执行一次
//    @Scheduled(cron = "0 0/5 * * * ?")
    @Scheduled(cron = "${cron.hour}")
    @Async()
    public void scheduledOutletTask() {
        log.info("定时任务3开始：" + System.currentTimeMillis());
        outletService.hourDeal();
        log.info("定时任务3结束：" + System.currentTimeMillis());
    }

    // @Scheduled(cron = "${cron.day}")
    //测试。每五分钟执行一次
//    @Scheduled(cron = "0 0/5 * * * ?")
    @Scheduled(cron = "${cron.hour}")
    @Async()
    public void scheduledTotalTask() {
        log.info("定时任务4开始：" + System.currentTimeMillis());
        totalService.hourDeal();
        log.info("定时任务4结束：" + System.currentTimeMillis());
    }

}
