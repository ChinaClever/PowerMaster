package cn.iocoder.yudao.module.infra.api.job;

import java.util.Map;

public interface JobApi {

    /**
     * 根据处理器，判断是否存在定时任务
     * @param handlerName 处理器名称
     *
     */
    public boolean existJobByHandler (String handlerName);

    /**
     * 创建定时任务
     * @param map 任务属性
     *
     */
    void createJob(Map<String, Object> map);

    /**
     * 更新定时任务
     * @param handlerName 处理器
     * @param cron 定时任务表达式
     */
    void updateCabinetJobCron(String handlerName, String cron);
}
