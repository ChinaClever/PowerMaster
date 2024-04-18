package cn.iocoder.yudao.module.statis.schedule;

import cn.iocoder.yudao.module.statis.service.LineService;
import cn.iocoder.yudao.module.statis.service.LoopService;
import cn.iocoder.yudao.module.statis.service.OutletService;
import cn.iocoder.yudao.module.statis.service.TotalService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.statis.constant.Constants.STATIS_CONFIG;
import static cn.iocoder.yudao.module.statis.init.DataInit.STATIS_CONFIG_MAP;

/**
 * @Author: chenwany
 * @Date: 2024/4/2 16:52
 * @Description: 定时任务（小时）
 */
@Data
@Slf4j
@EnableAsync
@Component
public class ScheduleHourTask implements SchedulingConfigurer {

    @Autowired
    LoopService loopService;
    @Autowired
    LineService lineService;
    @Autowired
    TotalService totalService;
    @Autowired
    OutletService outletService;

    //按小时执行
    private static final String HOUR_CRON = "0 0 * * * ?";

    private String hourCron = HOUR_CRON;

    /**
     * 每小时执行一次
     */
    @Async()
    public void scheduledLoopTask() {
        long start = System.currentTimeMillis();
        loopService.hourDeal();
        long end = System.currentTimeMillis();
        log.info("回路按小时统计结束：" + (end-start));
    }


    @Async()
    public void scheduledLineTask() {
        long start = System.currentTimeMillis();
        lineService.hourDeal();
        long end = System.currentTimeMillis();
        log.info("相按小时统计结束：" + (end-start));
    }


    @Async()
    public void scheduledOutletTask() {
        long start = System.currentTimeMillis();
        outletService.hourDeal();
        long end = System.currentTimeMillis();
        log.info("输出位按小时统计结束：" + (end-start));
    }

    @Async()
    public void scheduledTotalTask() {
        long start = System.currentTimeMillis();
        totalService.hourDeal();
        long end = System.currentTimeMillis();
        log.info("总历史数据按小时统计结束：" + (end-start));
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("cron: " + hourCron);
        String cron = STATIS_CONFIG_MAP.get(STATIS_CONFIG).getHourCron();
        hourCron = StringUtils.isNotEmpty(cron)?cron:HOUR_CRON;
        log.info("cron: " + hourCron);
        taskRegistrar.addTriggerTask(() -> {
            //回路按小时统计
            scheduledLoopTask();
            //相按小时统计
            scheduledLineTask();
            //输出位按小时统计
            scheduledOutletTask();
            //总历史数据按小时统计
            scheduledTotalTask();

        }, triggerContext -> {
            CronTrigger trigger = new CronTrigger(hourCron);
            return trigger.nextExecutionTime(triggerContext);
        });
    }
}
