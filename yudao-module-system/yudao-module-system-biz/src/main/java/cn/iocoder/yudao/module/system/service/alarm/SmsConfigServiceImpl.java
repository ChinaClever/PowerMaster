package cn.iocoder.yudao.module.system.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.sms.SmsConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.sms.SmsConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemSmsAlarmConfig;
import cn.iocoder.yudao.module.system.dal.mysql.alarm.SysAlarmSmsConfigMapper;
import cn.iocoder.yudao.module.system.service.sms.SmsSendService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 邮箱配置 Service 实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
@Slf4j
public class SmsConfigServiceImpl implements SmsConfigService {

    @Resource
    private SysAlarmSmsConfigMapper alarmSmsConfigMapper;

    @Override
    public Integer saveSmsConfig(SmsConfigSaveReqVO saveReqVO) {
        SystemSmsAlarmConfig config = BeanUtils.toBean(saveReqVO, SystemSmsAlarmConfig.class);
        if (Objects.nonNull(saveReqVO.getId())){
            //修改
            alarmSmsConfigMapper.updateById(config);
        }else {
            //新增
            alarmSmsConfigMapper.insert(config);
        }
        return config.getId();

    }

    @Override
    public List<SystemSmsAlarmConfig> getSmsConfig() {
        return alarmSmsConfigMapper.selectList(new LambdaUpdateWrapper<>());
    }

    @Override
    public void deleteSmsAccount(Integer id) {
        alarmSmsConfigMapper.deleteById(id);
    }

    @Override
    public PageResult<SystemSmsAlarmConfig> getSmsAccountPage(SmsConfigPageReqVO pageReqVO) {
        return  alarmSmsConfigMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<SystemSmsAlarmConfig>()
                .likeIfPresent(SystemSmsAlarmConfig::getPhone, pageReqVO.getPhone())
                .eqIfPresent(SystemSmsAlarmConfig::getIsEnable , pageReqVO.getIsEnable()));
    }
}
