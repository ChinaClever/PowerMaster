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
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.module.statis.constant.Constants.STATIS_CONFIG;
import static cn.iocoder.yudao.module.statis.init.DataInit.STATIS_CONFIG_MAP;

/**
 * @Author: chenwany
 * @Date: 2024/4/2 16:53
 * @Description: 定时任务（天）
 */
@Data
@Slf4j
@EnableAsync
@Component
public class ScheduleDayTask implements SchedulingConfigurer {

    @Autowired
    LoopService loopService;
    @Autowired
    LineService lineService;
    @Autowired
    TotalService totalService;
    @Autowired
    OutletService outletService;

    //按天执行 凌晨六点
    private static final String DAY_CRON = "0 0 6 * * ?";

    private String dayCron = DAY_CRON;


    /**
     * 每天凌晨六点执行
     */
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
    @Async()
    public void scheduledTotalTask() {
        long start = System.currentTimeMillis();
        totalService.dayDeal();
        long end = System.currentTimeMillis();
        log.info("总历史数据按天统计结束：" + (end-start));
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("cron: " + dayCron);
        String cron = STATIS_CONFIG_MAP.get(STATIS_CONFIG).getDayCron();
        dayCron = StringUtils.isNotEmpty(cron)?cron:DAY_CRON;
        log.info("cron: " + dayCron);
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
            CronTrigger trigger = new CronTrigger(dayCron);
            return trigger.nextExecutionTime(triggerContext);
        });
    }
}
