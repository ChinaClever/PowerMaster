package cn.iocoder.yudao.module.statis.schedule;

import cn.iocoder.yudao.module.statis.service.LoopService;
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

    /**
     * 每天凌晨六点执行
     */
    @Scheduled(cron = "${cron.day}")
    @Async()
    public void scheduledLoopTask() {
        log.info("定时任务1开始：" + System.currentTimeMillis());
        loopService.dayDeal();
        log.info("定时任务1结束：" + System.currentTimeMillis());
    }
}
