package cn.iocoder.yudao.module.system.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.config.ConfigSaveReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.mail.MailConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.mail.MailConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmConfig;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemMailAlarmConfig;
import cn.iocoder.yudao.module.system.dal.mysql.alarm.SysAlarmConfigMapper;
import cn.iocoder.yudao.module.system.dal.mysql.alarm.SysAlarmMailConfigMapper;
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
public class AlarmConfigServiceImpl implements AlarmConfigService {

    @Resource
    private SysAlarmConfigMapper alarmConfigMapper;


    @Override
    public Integer saveConfig(ConfigSaveReqVO saveReqVO) {
        SystemAlarmConfig config = BeanUtils.toBean(saveReqVO, SystemAlarmConfig.class);
        if (Objects.nonNull(saveReqVO.getId())){
            //修改
            alarmConfigMapper.updateById(config);
        }else {
            //新增
            alarmConfigMapper.insert(config);
        }
        return config.getId();
    }

    @Override
    public SystemAlarmConfig getConfig() {
        return  alarmConfigMapper.selectOne(new LambdaUpdateWrapper<SystemAlarmConfig>()
                .orderByDesc(SystemAlarmConfig::getCreateTime)
                .last("limit 1"));
    }
}
