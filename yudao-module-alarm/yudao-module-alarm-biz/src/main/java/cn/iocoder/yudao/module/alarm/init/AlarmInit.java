package cn.iocoder.yudao.module.alarm.init;

import cn.iocoder.yudao.framework.common.constant.JobHandlerConstants;
import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetCronConfig;
import cn.iocoder.yudao.framework.common.mapper.CabinetCronCfgMapper;
import cn.iocoder.yudao.module.alarm.constants.DBTable;
import cn.iocoder.yudao.framework.common.util.ListenerThreadPoolConfig;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgprompt.AlarmCfgPromptDO;
import cn.iocoder.yudao.module.alarm.monitor.MySQLTableMonitor;
import cn.iocoder.yudao.module.alarm.constants.BinLogConstants;
import cn.iocoder.yudao.module.alarm.service.cfgprompt.AlarmCfgPromptService;
import cn.iocoder.yudao.module.infra.api.job.JobApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Component
public class AlarmInit {


    @Autowired
    private MySQLTableMonitor mySQLTableMonitor;

    @Autowired
    private BinLogConstants binLogConstants;

    @Autowired
    private AlarmCfgPromptService alarmCfgPromptService;

    @Autowired
    private JobApi jobApi;

    @Autowired
    private CabinetCronCfgMapper cabinetCronCfgMapper;

    @PostConstruct
    public void init() throws InterruptedException {
        // 初始化告警配置
        initAlarmPrompt();

        // 监听表结构变化
        ListenTableChange();

        // 初始化机柜电量限额告警定时任务
        initCabinetAlarmJob();

    }

    private void initAlarmPrompt() {
        List<AlarmCfgPromptDO> cfgPromptList = alarmCfgPromptService.getCfgPromptList();
        if (cfgPromptList.isEmpty()) {
            alarmCfgPromptService.initAlarmPrompt();
        }
    }

    public void ListenTableChange() {
        ListenerThreadPoolConfig.getThreadPool().execute(() -> {
            List<String> tableList = new ArrayList<>(Arrays.asList(DBTable.PDU_INDEX, DBTable.BUS_INDEX, DBTable.CABINET_INDEX, DBTable.ALARM_LOG_RECORD, DBTable.CABINET_CRON_CONFIG));
            binLogConstants.setTableList(tableList);
            try {
                log.info("开始监听数据库表结构变化");
                mySQLTableMonitor.tableListener(binLogConstants);
            } catch (Exception e) {
                log.error("监听数据库表结构异常", e);
            }
        });
    }

    public void initCabinetAlarmJob(){
        try {
            // 定时任务共同属性
            Map<String, Object> map = new HashMap<>();
            map.put("retryCount", 3);
            map.put("retryInterval", 1000);
            map.put("monitorTimeout", 1000);

            CabinetCronConfig cabinetCronConfig = cabinetCronCfgMapper.selectOne(null);

            // 创建每日定时job
            if (jobApi.existJobByHandler(JobHandlerConstants.CABINET_DAY_ALARM_JOB)) {
                map.put("name", "机柜电量每天限额告警Job");
                map.put("handlerName", JobHandlerConstants.CABINET_DAY_ALARM_JOB);
                map.put("cronExpression", cabinetCronConfig.getEqDayCron());
                jobApi.createJob(map);
            }

            // 创建每月定时job
            if (jobApi.existJobByHandler(JobHandlerConstants.CABINET_MONTH_ALARM_JOB)) {
                map.put("name", "机柜电量每月限额告警Job");
                map.put("handlerName", JobHandlerConstants.CABINET_MONTH_ALARM_JOB);
                map.put("cronExpression", cabinetCronConfig.getEqMonthCron());
                jobApi.createJob(map);
            }
        } catch (Exception e) {
            log.error("初始化机柜电量限额告警定时任务异常", e);
        }
    }



}
