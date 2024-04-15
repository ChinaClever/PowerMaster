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
   // @Scheduled(cron = "${cron.day}")
    //测试。每五分钟执行一次
    @Scheduled(cron = "0 0/2 * * * ?")
    @Async()
    public void scheduledLoopTask() {
        long start = System.currentTimeMillis();
        loopService.hourDeal();
        long end = System.currentTimeMillis();
        log.info("回路按小时统计结束：" + (end-start));
    }


    // @Scheduled(cron = "${cron.day}")
    //测试。每五分钟执行一次
    @Scheduled(cron = "0 0/5 * * * ?")
    @Async()
    public void scheduledLineTask() {
        long start = System.currentTimeMillis();
        lineService.hourDeal();
        long end = System.currentTimeMillis();
        log.info("相按小时统计结束：" + (end-start));
    }

    // @Scheduled(cron = "${cron.day}")
    //测试。每五分钟执行一次
    @Scheduled(cron = "0 0/5 * * * ?")
    @Async()
    public void scheduledOutletTask() {
        long start = System.currentTimeMillis();
        outletService.hourDeal();
        long end = System.currentTimeMillis();
        log.info("输出位按小时统计结束：" + (end-start));
    }

    // @Scheduled(cron = "${cron.day}")
    //测试。每五分钟执行一次
    @Scheduled(cron = "0 0/5 * * * ?")
    @Async()
    public void scheduledTotalTask() {
        long start = System.currentTimeMillis();
        totalService.hourDeal();
        long end = System.currentTimeMillis();
        log.info("总历史数据按小时统计结束：" + (end-start));
    }

}
