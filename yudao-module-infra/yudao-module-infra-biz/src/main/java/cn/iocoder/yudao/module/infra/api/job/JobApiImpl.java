package cn.iocoder.yudao.module.infra.api.job;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.JobSaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.JobDO;
import cn.iocoder.yudao.module.infra.dal.mysql.job.JobMapper;
import cn.iocoder.yudao.module.infra.service.job.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class JobApiImpl implements JobApi{

    @Autowired
    private JobService jobService;
    @Autowired
    private JobMapper jobMapper;

    @Override
    public boolean existJobByHandler(String handlerName) {
        JobDO jobDO = jobMapper.selectByHandlerName(handlerName);
        return jobDO == null;
    }

    @Override
    public void createJob(Map<String, Object> map) {
        try {
            JobSaveReqVO job = BeanUtils.toBean(map, JobSaveReqVO.class);
            jobService.createJob(job);
        } catch (Exception e) {
            log.error("创建定时任务异常：" + e.getMessage());
        }
    }

    @Override
    public void updateJobCronByHandler(String handlerName, String cron) {
        try {
            jobService.updateJobCronByHandler(handlerName,cron);
        } catch (Exception e) {
            log.error("更新定时任务异常：" + e.getMessage());
        }
    }
}
