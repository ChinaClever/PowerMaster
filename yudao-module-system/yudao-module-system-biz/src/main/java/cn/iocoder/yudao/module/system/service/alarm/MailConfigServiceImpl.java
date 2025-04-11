package cn.iocoder.yudao.module.system.service.alarm;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.mail.MailConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.mail.MailConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmConfig;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmRecord;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemMailAlarmConfig;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemSmsAlarmConfig;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailAccountDO;
import cn.iocoder.yudao.module.system.dal.dataobject.mail.MailTemplateDO;
import cn.iocoder.yudao.module.system.dal.mysql.alarm.SysAlarmMailConfigMapper;
import cn.iocoder.yudao.module.system.dal.mysql.alarm.SysAlarmSmsConfigMapper;
import cn.iocoder.yudao.module.system.mq.message.mail.MailSendMessage;
import cn.iocoder.yudao.module.system.service.mail.MailAccountService;
import cn.iocoder.yudao.module.system.service.mail.MailLogService;
import cn.iocoder.yudao.module.system.service.mail.MailSendService;
import cn.iocoder.yudao.module.system.service.mail.MailTemplateService;
import cn.iocoder.yudao.module.system.service.sms.SmsSendService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.sound.sampled.*;
import java.io.InputStream;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;

/**
 * 邮箱配置 Service 实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
@Slf4j
public class MailConfigServiceImpl implements MailConfigService {

    @Resource
    private SysAlarmMailConfigMapper alarmMailConfigMapper;
    @Resource
    private MailAccountService mailAccountService;
    @Resource
    private MailTemplateService mailTemplateService;
    @Resource
    private MailSendService mailSendService;
    @Resource
    private MailLogService mailLogService;
    @Resource
    private SysAlarmSmsConfigMapper smsConfigMapper;
    @Resource
    SmsSendService smsSendService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AlarmRecordService alarmRecordService;

    @Autowired
    private AlarmConfigService alarmConfigService;



    public static final String TEMPLATE_CODE = "ALARM_MAIL";

    public static final String TEMPLATE_CODE_SMS = "ALARM_SMS";

    @Override
    public Integer saveMailConfig(MailConfigSaveReqVO saveReqVO) {
        SystemMailAlarmConfig config = BeanUtils.toBean(saveReqVO, SystemMailAlarmConfig.class);
        if (Objects.nonNull(saveReqVO.getId())){
            //修改
            alarmMailConfigMapper.updateById(config);
        }else {
            //新增
            alarmMailConfigMapper.insert(config);
        }
        return config.getId();

    }

    @Override
    public void batchSave(List<MailConfigSaveReqVO> saveReqVOS) {
        if (!CollectionUtils.isEmpty(saveReqVOS)) {
            List<SystemMailAlarmConfig> list = BeanUtils.toBean(saveReqVOS, SystemMailAlarmConfig.class);
            alarmMailConfigMapper.insertBatch(list);
        }
    }

    @Override
    public void deleteMailAll() {
        alarmMailConfigMapper.delete(null);
    }

    @Override
    public List<SystemMailAlarmConfig> getMailConfig() {
        return alarmMailConfigMapper.selectList(new LambdaUpdateWrapper<>());
    }

    @Override
    public void deleteMailAccount(Integer id) {
        alarmMailConfigMapper.deleteById(id);
    }

    @Override
    public PageResult<SystemMailAlarmConfig> getMailAccountPage(MailConfigPageReqVO pageReqVO) {
        return  alarmMailConfigMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<SystemMailAlarmConfig>()
                .likeIfPresent(SystemMailAlarmConfig::getMail, pageReqVO.getMail())
                .eqIfPresent(SystemMailAlarmConfig::getIsEnable , pageReqVO.getIsEnable()));
    }

    @Override
    public void sendMail(List<SystemAlarmRecord> records) {

        List<SystemMailAlarmConfig> mailAlarmConfigs = alarmMailConfigMapper.selectList(new LambdaQueryWrapperX<SystemMailAlarmConfig>()
                .eq(SystemMailAlarmConfig::getIsEnable, DisableEnums.ENABLE.getStatus()));

        //报警信息
        List<Map<String, Object>> templateParams = new ArrayList<>();
        for (SystemAlarmRecord record : records) {
            Map<String, Object> templateParam = new HashMap<>();
            templateParam.put("devKey",record.getDevKey());
            templateParam.put("devName",record.getDevName());
            templateParam.put("devType", DeviceTypeEnums.getNameByStatus(record.getDevType()));
            templateParam.put("alarmType", AlarmTypeEnums.getNameByStatus(record.getAlarmType()));
            templateParam.put("alarmLevel", AlarmLevelEnums.getNameByStatus(record.getAlarmLevel()));
            templateParam.put("alarmDesc",record.getAlarmDesc());
            templateParam.put("position",record.getDevPosition());
        }


        if (!CollectionUtils.isEmpty(mailAlarmConfigs)){
            mailAlarmConfigs.forEach(config -> {
                String mail = config.getMail();
                // 校验邮箱是否存在
                mail = validateMail(mail);

                // 校验邮箱模版是否合法
                MailTemplateDO template = validateMailTemplate(TEMPLATE_CODE);

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
                        account, template, contentBuff.toString(), templateParams, isSend);
                MailSendMessage message = new MailSendMessage()
                        .setLogId(sendLogId).setMail(mail).setAccountId(template.getAccountId())
                        .setNickname(template.getNickname()).setTitle(title).setContent(contentBuff.toString());
                //发送邮件
                mailSendService.doSendMail(message);
            });
        }


    }

    @Override
    public void sendSms(SystemAlarmRecord record) {

        List<SystemSmsAlarmConfig> smsAlarmConfigs = smsConfigMapper.selectList(new LambdaQueryWrapperX<SystemSmsAlarmConfig>()
                .eq(SystemSmsAlarmConfig::getIsEnable, DisableEnums.ENABLE.getStatus()));

        //报警信息
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("devKey",record.getDevKey());
        templateParams.put("devName",record.getDevName());
        templateParams.put("devType", DeviceTypeEnums.getNameByStatus(record.getDevType()));
        templateParams.put("alarmType", AlarmTypeEnums.getNameByStatus(record.getAlarmType()));
        templateParams.put("alarmLevel", AlarmLevelEnums.getNameByStatus(record.getAlarmLevel()));
        templateParams.put("position",record.getDevPosition());

        if (!CollectionUtils.isEmpty(smsAlarmConfigs)){
            smsAlarmConfigs.forEach(config -> {
                String phone = config.getPhone();
                smsSendService.sendSingleSms(phone,null,null,TEMPLATE_CODE_SMS,templateParams);
            });
        }

    }

    @Override
    public void playAudio() {

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ALARM.WAV");

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            AudioFormat format = audioInputStream.getFormat();
            if (!AudioSystem.isLineSupported(new DataLine.Info(Clip.class, format))) {
               log.info("Line not supported for this audio format.");
            } else {
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                // 获取音频格式
                AudioFormat audioFormat = audioInputStream.getFormat();
                // 获取帧长度（如果可用）
                long frameLength = audioInputStream.getFrameLength();

                // 如果音频流没有指定帧长度，则需要找到音频文件的总字节数
                if (frameLength == AudioSystem.NOT_SPECIFIED) {
                    frameLength = audioInputStream.getFrameLength() * audioFormat.getFrameSize();
                }

                // 计算总播放时间（秒）
                float frameRate = audioFormat.getFrameRate();
                // 秒
                float totalSeconds = frameLength / frameRate;

                clip.start();
                //开始后睡眠播放时间秒
                Thread.sleep((long) (totalSeconds * 1000L));
                //关闭
                clip.close();
            }
        }catch (Exception e){
            log.error("播放异常：",e);
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
                throw exception(MAIL_SEND_TEMPLATE_PARAM_MISS, key);
            }
        });
    }


    @Override
    public void pushAlarmMessage() {
        // 获取告警记录变化的数据
        String table = "sys_alarm_record";
        List<Map<String,Object>> insertList = redisTemplate.opsForList().range(table + ":insert", 0, -1);
        List<Map<String,Object>> updateList = redisTemplate.opsForList().range(table + ":updateNew", 0, -1);
        // 获取告警配置
        SystemAlarmConfig config = alarmConfigService.getConfig();
        int voiceAlarm = config.getVoiceAlarm();
        int emailAlarm = config.getMailAlarm();
        int smsAlarm = config.getSmsAlarm();
        // 告警推送处理
        if (!insertList.isEmpty() || !updateList.isEmpty()) {
            // 获取告警记录
            List<SystemAlarmRecord> list = new ArrayList<>();
            if (!insertList.isEmpty()) {
                for (Map<String, Object> map : insertList) {
                    SystemAlarmRecord record = alarmRecordService.getAlarmRecordById((Integer) map.get("id"));
                    list.add(record);
                }
            }
            if (!updateList.isEmpty()) {
                for (Map<String, Object> map : updateList) {
                    SystemAlarmRecord record = alarmRecordService.getAlarmRecordById((Integer) map.get("id"));
                    list.add(record);
                }
            }

            // 铃声告警
//            if (voiceAlarm == 1) {
//                playAudio();
//            }
            // 邮件告警
            if (emailAlarm == 1) {
                sendMail(list);
            }

            // 删除缓存
            List<String> deleteKeys = new ArrayList<>();
            deleteKeys.add(table + ":insert");
            deleteKeys.add(table + ":updateNew");
            deleteKeys.add(table + ":updateOld");
            redisTemplate.delete(deleteKeys);
        }
    }

}
