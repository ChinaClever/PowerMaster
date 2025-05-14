package cn.iocoder.yudao.module.system.job;

import cn.iocoder.yudao.framework.quartz.core.handler.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DemoJob implements JobHandler {


    @Override
    public String execute(String param) throws Exception {
        log.info(Thread.currentThread().getName() + "[execute][定时执行测试]");
        return null;
    }
}
