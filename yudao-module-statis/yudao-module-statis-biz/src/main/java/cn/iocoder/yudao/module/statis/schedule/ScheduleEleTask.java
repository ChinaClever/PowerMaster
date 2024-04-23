package cn.iocoder.yudao.module.statis.schedule;

import cn.iocoder.yudao.module.statis.service.*;
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
 * @Description: 定时任务(电量计算)
 */
@Data
@Slf4j
@EnableAsync
@Component
public class ScheduleEleTask implements SchedulingConfigurer {

    @Autowired
    EleService eleService;

    //按周执行 每周一早上六点
    private static final String WEEK_CRON = "0 0 6 ? * SUN";

    //按月执行 每月一号六点
    private static final String MONTH_CRON = "0 0 6 1 * ?";

    //按天执行 每天六点
    private static final String DAY_CRON = "0 0 6 * * ?";

    private String weekCron = WEEK_CRON;

    private String monthCron = MONTH_CRON;

    private String dayCron = DAY_CRON;


    @Async()
    public void scheduledTotalDayTask() {
        long start = System.currentTimeMillis();
       eleService.eleTotalDayDeal();
        long end = System.currentTimeMillis();
        log.info("总电量按天统计结束：" + (end-start));
    }


    @Async()
    public void scheduledOutletDayTask() {
        long start = System.currentTimeMillis();
        eleService.eleOutletDayDeal();
        long end = System.currentTimeMillis();
        log.info("输出位电量按天统计结束：" + (end-start));
    }


    @Async()
    public void scheduledTotalWeekTask() {
        long start = System.currentTimeMillis();
        eleService.eleTotalWeekDeal();
        long end = System.currentTimeMillis();
        log.info("总电量按周统计结束：" + (end-start));
    }

    @Async()
    public void scheduledOutletWeekTask() {
        long start = System.currentTimeMillis();
        eleService.eleOutletWeekDeal();
        long end = System.currentTimeMillis();
        log.info("输出位电量按周统计结束：" + (end-start));
    }


    @Async()
    public void scheduledOutletMonthTask() {
        long start = System.currentTimeMillis();
        eleService.eleOutletMonthDeal();
        long end = System.currentTimeMillis();
        log.info("输出位电量按月统计结束：" + (end-start));
    }


    @Async()
    public void scheduledTotalMonthTask() {
        long start = System.currentTimeMillis();
        eleService.eleTotalMonthDeal();
        long end = System.currentTimeMillis();
        log.info("总电量数据按天统计结束：" + (end-start));
    }


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        log.info("monthCron: " + monthCron + "dayCron: "+ dayCron + "weekCron: " + weekCron);
        dayCron = StringUtils.isNotEmpty(STATIS_CONFIG_MAP.get(STATIS_CONFIG).getEqDayCron())
                ?STATIS_CONFIG_MAP.get(STATIS_CONFIG).getEqDayCron():DAY_CRON;
        weekCron = StringUtils.isNotEmpty(STATIS_CONFIG_MAP.get(STATIS_CONFIG).getEqWeekCron())
                ?STATIS_CONFIG_MAP.get(STATIS_CONFIG).getEqWeekCron():WEEK_CRON;
        monthCron = StringUtils.isNotEmpty(STATIS_CONFIG_MAP.get(STATIS_CONFIG).getEqMonthCron())
                ?STATIS_CONFIG_MAP.get(STATIS_CONFIG).getEqMonthCron():MONTH_CRON;
        log.info("monthCron: " + monthCron + "dayCron: "+ dayCron + "weekCron: " + weekCron);
        //按月统计
        taskRegistrar.addTriggerTask(() -> {
            scheduledOutletMonthTask();

            scheduledTotalMonthTask();

        }, triggerContext -> {
            CronTrigger trigger = new CronTrigger(monthCron);
            return trigger.nextExecutionTime(triggerContext);
        });

        //按天统计
        taskRegistrar.addTriggerTask(() -> {
            scheduledTotalDayTask();

            scheduledOutletDayTask();

        }, triggerContext -> {
            CronTrigger trigger = new CronTrigger(dayCron);
            return trigger.nextExecutionTime(triggerContext);
        });


        //按周统计
        taskRegistrar.addTriggerTask(() -> {
            scheduledTotalWeekTask();

            scheduledOutletWeekTask();

        }, triggerContext -> {
            CronTrigger trigger = new CronTrigger(weekCron);
            return trigger.nextExecutionTime(triggerContext);
        });
    }
}
