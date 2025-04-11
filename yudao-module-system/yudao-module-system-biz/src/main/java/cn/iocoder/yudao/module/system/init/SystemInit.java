package cn.iocoder.yudao.module.system.init;

import cn.iocoder.yudao.framework.common.util.ListenerThreadPoolConfig;
import cn.iocoder.yudao.module.system.monitor.constants.BinLogConstants;
import cn.iocoder.yudao.module.system.monitor.MySQLTableMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class SystemInit {


    @Autowired
    private MySQLTableMonitor mySQLTableMonitor;

    @Autowired
    private BinLogConstants binLogConstants;


    @PostConstruct
    public void init(){
        // 开始监听pdu_index表结构变化
        ListenPduIndexTableChange();

    }

    public void ListenPduIndexTableChange(){
        ListenerThreadPoolConfig.getThreadPool().execute(() -> {
            binLogConstants.setTable("pdu_index");
            try {
                log.info("开始监听pdu_index表结构变化");
                mySQLTableMonitor.tableListener(binLogConstants);
            } catch (Exception e) {
                log.error("监听pdu异常",e);
            }
        });
    }
}
