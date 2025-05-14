package cn.iocoder.yudao.module.alarm.service.cfgmail;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.constant.MailTemplateConstants;
import cn.iocoder.yudao.framework.common.enums.*;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgprompt.AlarmCfgPromptDO;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.module.alarm.rabbitmq.producer.AlarmRabbitMQProducer;
import cn.iocoder.yudao.module.alarm.service.cfgprompt.AlarmCfgPromptService;
import cn.iocoder.yudao.module.alarm.service.logrecord.AlarmLogRecordService;
import cn.iocoder.yudao.module.alarm.utils.audioplayer.AudioPlayer;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import cn.iocoder.yudao.module.system.service.mail.MailAccountService;
import cn.iocoder.yudao.module.system.service.mail.MailLogService;
import cn.iocoder.yudao.module.system.service.mail.MailSendService;
import cn.iocoder.yudao.module.system.service.mail.MailTemplateService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.module.alarm.controller.admin.cfgmail.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgmail.AlarmCfgMailDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.alarm.dal.mysql.cfgmail.AlarmCfgMailMapper;
import java.time.LocalDateTime;
import java.util.*;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.MAIL_SEND_TEMPLATE_PARAM_MISS;


/**
 * 告警邮件接收人配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AlarmCfgMailServiceImpl implements AlarmCfgMailService {

    @Resource
    private AlarmCfgMailMapper cfgMailMapper;

    @Resource
    private MailAccountService mailAccountService;
    @Resource
    private MailTemplateService mailTemplateService;
    @Resource
    private MailSendService mailSendService;
    @Resource
    private MailLogService mailLogService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AlarmCfgPromptService alarmCfgPromptService;

    @Autowired
    private AlarmLogRecordService alarmLogRecordService;

    @Autowired
    private AlarmRabbitMQProducer alarmRabbitMQProducer;

    @Autowired
    private AudioPlayer audioPlayer;


    public AlarmCfgMailServiceImpl(MailAccountService mailAccountService) {
        this.mailAccountService = mailAccountService;
    }

    @Override
    public Integer saveCfgMail(AlarmCfgMailSaveReqVO saveReqVO) {

        AlarmCfgMailDO cfgMail = BeanUtils.toBean(saveReqVO, AlarmCfgMailDO.class);
        if (Objects.nonNull(saveReqVO.getId())) {
            // 修改
            cfgMailMapper.updateById(cfgMail);
        } else {
            // 插入
            cfgMailMapper.insert(cfgMail);
        }
        // 返回
        return cfgMail.getId();
    }

    @Override
    public void batchSave(List<AlarmCfgMailSaveReqVO> saveReqVOS) {
        if (!CollectionUtils.isEmpty(saveReqVOS)) {
            List<AlarmCfgMailDO> list = BeanUtils.toBean(saveReqVOS, AlarmCfgMailDO.class);
            cfgMailMapper.insertBatch(list);
        }
    }


    @Override
    public void updateCfgMail(AlarmCfgMailSaveReqVO updateReqVO) {
        // 校验存在
        validateCfgMailExists(updateReqVO.getId());
        // 更新
        AlarmCfgMailDO updateObj = BeanUtils.toBean(updateReqVO, AlarmCfgMailDO.class);
        cfgMailMapper.updateById(updateObj);
    }

    @Override
    public void deleteCfgMail(Integer id) {
        // 校验存在
        validateCfgMailExists(id);
        // 删除
        cfgMailMapper.deleteById(id);
    }

    @Override
    public void deleteMailAll() {
        cfgMailMapper.delete(null);
    }


    private void validateCfgMailExists(Integer id) {
        if (cfgMailMapper.selectById(id) == null) {
            throw new RuntimeException("数据不存在");
        }
    }

    @Override
    public AlarmCfgMailDO getCfgMail(Integer id) {
        return cfgMailMapper.selectById(id);
    }

    @Override
    public PageResult<AlarmCfgMailDO> getCfgMailPage(AlarmCfgMailPageReqVO pageReqVO) {
        return cfgMailMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<AlarmCfgMailDO>()
                .likeIfPresent(AlarmCfgMailDO::getMailAddr, pageReqVO.getMailAddr())
                .eqIfPresent(AlarmCfgMailDO::getIsEnable , pageReqVO.getIsEnable()));
    }

    @Override
    public List<AlarmCfgMailDO> getMailConfig() {
        return cfgMailMapper.selectList(new LambdaUpdateWrapper<>());
    }

    @Override
    public void sendAlarmMail(List<AlarmLogRecordDO> records) {

        List<AlarmCfgMailDO> mailAlarmConfigs = cfgMailMapper.selectList(new LambdaQueryWrapperX<AlarmCfgMailDO>()
                .eq(AlarmCfgMailDO::getIsEnable, DisableEnums.ENABLE.getStatus()));

        //报警信息
        List<Map<String, Object>> templateParams = new ArrayList<>();
        for (AlarmLogRecordDO record : records) {
            Map<String, Object> templateParam = new HashMap<>();
            record = alarmLogRecordService.getLogRecord(record.getId());
            templateParam.put("position",record.getAlarmPosition());
            templateParam.put("alarmType", AlarmTypeEnums.getNameByStatus(record.getAlarmType()));
            templateParam.put("alarmLevel", AlarmLevelEnums.getNameByStatus(record.getAlarmLevel()));
            templateParam.put("alarmDesc",record.getAlarmDesc());
            templateParam.put("startTime", record.getStartTime().toString());
            templateParams.add(templateParam);
        }


        if (!CollectionUtils.isEmpty(mailAlarmConfigs)){
            for (AlarmCfgMailDO config : mailAlarmConfigs) {
                String mail = config.getMailAddr();
                // 校验邮箱是否存在
                mail = validateMail(mail);

                // 校验邮箱模版是否合法
                MailTemplateDO template = validateMailTemplate(MailTemplateConstants.ALARM_MAIL);

                MailAccountDO account = validateMailAccount(template.getAccountId());
                StringBuffer contentBuff = new StringBuffer();
                for (Map<String, Object> templateParam : templateParams) {
                    // 校验邮箱账号是否合法
                    validateTemplateParams(template, templateParam);

                    // 组装发送内容
                    String content = mailTemplateService.formatMailTemplateContent(template.getContent(), templateParam);
                    contentBuff.append(content).append("\n");
                }


                // 创建发送日志。如果模板被禁用，则不发送短信，只记录日志
                Boolean isSend = CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus());
                String title = mailTemplateService.formatMailTemplateContent(template.getTitle(), templateParams.get(0));


                //邮件用户
                Long sendLogId = mailLogService.createMailLog(template.getAccountId(), 9, mail,
                        account, template, contentBuff.toString(), templateParams.get(0), isSend);
                MailSendMessage message = new MailSendMessage()
                        .setLogId(sendLogId).setMail(mail).setAccountId(template.getAccountId())
                        .setNickname(template.getNickname()).setTitle(title).setContent(contentBuff.toString());
                //发送邮件
                mailSendService.doSendMail(message);
            };
        }
    }

    @Override
    public void pushAlarmMessage(List<Map<String, Object>> mapList) {
        if (!CollectionUtils.isEmpty(mapList)) {
            // 获取告警记录变化的数据
            List<AlarmLogRecordDO> list = BeanUtils.toBean(mapList, AlarmLogRecordDO.class);

            // 获取告警配置
            List<AlarmCfgPromptDO> cfgPromptList = alarmCfgPromptService.getCfgPromptList();
            int voiceAlarm = 0;
            int emailAlarm = 0;
            int smsAlarm = 0;
            for (AlarmCfgPromptDO alarmCfgPromptDO : cfgPromptList) {
                if (AlarmPromptType.VOICE_ALARM.getCode().equals(alarmCfgPromptDO.getPromptType())) {
                    voiceAlarm = alarmCfgPromptDO.getIsEnable();
                } else if (AlarmPromptType.EMAIL_ALARM.getCode().equals(alarmCfgPromptDO.getPromptType())) {
                    emailAlarm = alarmCfgPromptDO.getIsEnable();
                } else if (AlarmPromptType.SMS_ALARM.getCode().equals(alarmCfgPromptDO.getPromptType())) {
                    smsAlarm = alarmCfgPromptDO.getIsEnable();
                }
            }

            //告警推送
            // 铃声告警
            //if (voiceAlarm == 1) {
            //    audioPlayer.playAudio();
            //}
            // 邮件告警
            if (emailAlarm == 1) {
                sendAlarmMail(list);
            }
            // rabbitmq告警
//            String content = constructMqContent(list);
//            alarmRabbitMQProducer.sendMessage(AlarmMqMessage.KEY_ALARM,content);
        }
    }


    @VisibleForTesting
    MailTemplateDO validateMailTemplate(String templateCode) {
        // 获得邮件模板。考虑到效率，从缓存中获取
        MailTemplateDO template = mailTemplateService.getMailTemplateByCodeFromCache(templateCode);
        // 邮件模板不存在
        if (template == null) {
            throw exception(MAIL_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    @VisibleForTesting
    MailAccountDO validateMailAccount(Long accountId) {
        // 获得邮箱账号。考虑到效率，从缓存中获取
        MailAccountDO account = mailAccountService.getMailAccountFromCache(accountId);
        // 邮箱账号不存在
        if (account == null) {
            throw exception(MAIL_ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    @VisibleForTesting
    String validateMail(String mail) {
        if (StrUtil.isEmpty(mail)) {
            throw exception(MAIL_SEND_MAIL_NOT_EXISTS);
        }
        return mail;
    }

    /**
     * 校验邮件参数是否确实
     *
     * @param template 邮箱模板
     * @param templateParams 参数列表
     */
    @VisibleForTesting
    void validateTemplateParams(MailTemplateDO template, Map<String, Object> templateParams) {
        template.getParams().forEach(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw ServiceExceptionUtil.exception(MAIL_SEND_TEMPLATE_PARAM_MISS, key);
            }
        });
    }

    public String constructMqContent (List<AlarmLogRecordDO> list) {
        StringBuilder contentBuff = new StringBuilder();
        for (AlarmLogRecordDO alarmLogRecordDO : list) {
            String alarmPosition = alarmLogRecordDO.getAlarmPosition();
            LocalDateTime startTime = alarmLogRecordDO.getStartTime();
            String alarmType = AlarmTypeEnums.getNameByStatus(alarmLogRecordDO.getAlarmType());
            String alarmDesc = alarmLogRecordDO.getAlarmDesc();
            String content = String.format("%s在%s发生%s，告警原因：%s", alarmPosition, startTime, alarmType, alarmDesc);
            contentBuff.append(content).append("\n");
        }
        return contentBuff.toString();
    }

}