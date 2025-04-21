package cn.iocoder.yudao.module.alarm.init;

import cn.iocoder.yudao.module.alarm.constants.DBTable;
import cn.iocoder.yudao.framework.common.util.ListenerThreadPoolConfig;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgprompt.AlarmCfgPromptDO;
import cn.iocoder.yudao.module.alarm.monitor.MySQLTableMonitor;
import cn.iocoder.yudao.module.alarm.constants.BinLogConstants;
import cn.iocoder.yudao.module.alarm.service.cfgprompt.AlarmCfgPromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class AlarmInit {


    @Autowired
    private MySQLTableMonitor mySQLTableMonitor;

    @Autowired
    private BinLogConstants binLogConstants;

    @Autowired
    private AlarmCfgPromptService alarmCfgPromptService;

    @PostConstruct
    public void init() throws InterruptedException {
        // 初始化告警配置
        initAlarmPrompt();

        // 开始监听pdu_index表结构变化
        ListenPduIndexTableChange();
    }

    private void initAlarmPrompt() {
        List<AlarmCfgPromptDO> cfgPromptList = alarmCfgPromptService.getCfgPromptList();
        if (cfgPromptList.isEmpty()) {
            alarmCfgPromptService.initAlarmPrompt();
        }
    }

    public void ListenPduIndexTableChange(){
        ListenerThreadPoolConfig.getThreadPool().execute(() -> {
            binLogConstants.setTable(DBTable.PDU_INDEX);
            try {
                log.info("开始监听pdu_index表结构变化");
                mySQLTableMonitor.tableListener(binLogConstants);
            } catch (Exception e) {
                log.error("监听pdu异常",e);
            }
        });
    }


}
