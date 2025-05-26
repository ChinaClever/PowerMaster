package cn.iocoder.yudao.framework.common.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName: ThreadPoolConfig
 * 多线程连接池
 * @Author: jiangjinchi
 * @Date：2024/12/2415:02
 **/
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    /**
     * 每秒需要多少个线程处理?
     * tasks/(1/taskcost)
     */
    private static int corePoolSize = 3;

    /**
     * 线程池维护线程的最大数量
     * (max(tasks)- queueCapacity)/(1/taskcost)
     */
    private static int maxPoolSize = 3;

    /**
     * 缓存队列
     * (coreSizePool/taskcost)*responsetime
     */
    private static int queueCapacity = 10;

    /**
     * 允许的空闲时间
     * 默认为60
     */
    private static int keepAlive = 100;

    private static ThreadPoolTaskExecutor threadPool;

    @Bean(name = "getTHreadPool")
    public static ThreadPoolTaskExecutor getTHreadPool() {
        if (null == threadPool) {
            threadPool = new ThreadPoolTaskExecutor();
            threadPool.initialize();
            // 设置核心线程数
            threadPool.setCorePoolSize(corePoolSize);
            // 设置最大线程数
            threadPool.setMaxPoolSize(maxPoolSize);
            // 设置队列容量
            threadPool.setQueueCapacity(queueCapacity);
            // 设置允许的空闲时间（秒）
            //executor.setKeepAliveSeconds(keepAlive);
            // 设置默认线程名称
            threadPool.setThreadNamePrefix("thread-");
            // 设置拒绝策略rejection-policy：当pool已经达到max size的时候，如何处理新任务
            // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
            threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            // 等待所有任务结束后再关闭线程池
            threadPool.setWaitForTasksToCompleteOnShutdown(true);
            return threadPool;
        }
        return threadPool;
    }


}
