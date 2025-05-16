package cn.iocoder.yudao.module.alarm.service.cfgprompt;

import cn.iocoder.yudao.framework.common.enums.AlarmPromptType;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.module.alarm.controller.admin.cfgprompt.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgprompt.AlarmCfgPromptDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.alarm.dal.mysql.cfgprompt.AlarmCfgPromptMapper;

import java.util.Arrays;
import java.util.List;


/**
 * 系统告警配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AlarmCfgPromptServiceImpl implements AlarmCfgPromptService {

    @Resource
    private AlarmCfgPromptMapper cfgPromptMapper;

    @Override
    public Integer saveCfgPrompt(AlarmCfgPromptSaveReqVO saveReqVO) {
        AlarmCfgPromptDO alarmCfgPromptDO = BeanUtils.toBean(saveReqVO, AlarmCfgPromptDO.class);
        int id = cfgPromptMapper.insert(alarmCfgPromptDO);
        return id;
    }

    @Override
    public void updateCfgPrompt(AlarmCfgPromptSaveReqVO updateReqVO) {
        List<AlarmCfgPromptDO> cfgPromptList = getCfgPromptList();
        for (AlarmCfgPromptDO alarmCfgPromptDO : cfgPromptList) {
            if (AlarmPromptType.VOICE_ALARM.getCode().equals(alarmCfgPromptDO.getPromptType())) {
                alarmCfgPromptDO.setIsEnable(updateReqVO.getVoiceAlarm());
            } else if (AlarmPromptType.EMAIL_ALARM.getCode().equals(alarmCfgPromptDO.getPromptType())) {
                alarmCfgPromptDO.setIsEnable(updateReqVO.getMailAlarm());
            } else if (AlarmPromptType.SMS_ALARM.getCode().equals(alarmCfgPromptDO.getPromptType())) {
                alarmCfgPromptDO.setIsEnable(updateReqVO.getSmsAlarm());
            }
        }
        cfgPromptMapper.updateBatch(cfgPromptList);
    }

    @Override
    public void deleteCfgPrompt(Integer id) {
        // 校验存在
        validateCfgPromptExists(id);
        // 删除
        cfgPromptMapper.deleteById(id);
    }

    private void validateCfgPromptExists(Integer id) {
        if (cfgPromptMapper.selectById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
    }

    @Override
    public AlarmCfgPromptDO getCfgPrompt(Integer id) {
        return cfgPromptMapper.selectById(id);
    }

    @Override
    public List<AlarmCfgPromptDO> getCfgPromptList() {
        return cfgPromptMapper.selectList(new LambdaQueryWrapperX<AlarmCfgPromptDO>());
    }

    @Override
    public PageResult<AlarmCfgPromptDO> getCfgPromptPage(AlarmCfgPromptPageReqVO pageReqVO) {
        return cfgPromptMapper.selectPage(pageReqVO);
    }

    @Override
    public void initAlarmPrompt() {
        AlarmCfgPromptDO voice = new AlarmCfgPromptDO(1, 0, 1);
        AlarmCfgPromptDO mail = new AlarmCfgPromptDO(2, 0, 2);
        AlarmCfgPromptDO sms = new AlarmCfgPromptDO(3, 0, 3);
        AlarmCfgPromptDO mq = new AlarmCfgPromptDO(4, 0, 4);
        cfgPromptMapper.insertBatch(Arrays.asList(voice, mail, sms, mq));
    }

    @Override
    public Integer getCfgPromptByType(Integer code) {
        return cfgPromptMapper.getCfgPromptByType(code);
    }

}