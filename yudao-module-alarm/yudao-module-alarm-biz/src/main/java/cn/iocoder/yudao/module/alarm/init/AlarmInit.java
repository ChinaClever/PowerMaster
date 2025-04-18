package cn.iocoder.yudao.module.alarm.init;

import cn.iocoder.yudao.module.alarm.monitor.constants.DBTable;
import cn.iocoder.yudao.framework.common.util.ListenerThreadPoolConfig;
import cn.iocoder.yudao.module.alarm.monitor.MySQLTableMonitor;
import cn.iocoder.yudao.module.alarm.monitor.constants.BinLogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class AlarmInit {


    @Autowired
    private MySQLTableMonitor mySQLTableMonitor;

    @Autowired
    private BinLogConstants binLogConstants;

    @PostConstruct
    public void init() throws InterruptedException {
        // 开始监听pdu_index表结构变化
        ListenPduIndexTableChange();
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
