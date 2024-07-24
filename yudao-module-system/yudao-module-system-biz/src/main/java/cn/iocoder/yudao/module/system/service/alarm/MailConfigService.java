package cn.iocoder.yudao.module.system.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.mail.MailConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.mail.MailConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmRecord;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemMailAlarmConfig;

import javax.validation.Valid;
import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 告警邮件配置
 * @date 2024/4/23 9:19
 */
public interface MailConfigService {

    /**
     * 保存配置
     *
     * @param saveReqVO 邮箱账号信息
     * @return 编号
     */
    Integer saveMailConfig(@Valid MailConfigSaveReqVO saveReqVO);



    /**
     * 获取配置
     * @return 邮箱账号信息
     */
    List<SystemMailAlarmConfig> getMailConfig();


    /**
     * 删除邮箱账号
     *
     * @param id 编号
     */
    void deleteMailAccount(Integer id);

    /**
     * 获取邮箱账号分页信息
     *
     * @param pageReqVO 邮箱账号分页参数
     * @return 邮箱账号分页信息
     */
    PageResult<SystemMailAlarmConfig> getMailAccountPage(MailConfigPageReqVO pageReqVO);

    /**
     * 发送邮件
     * @param record 发送邮件的内容
     */
    void sendMail(SystemAlarmRecord record);

    /**
     * 发送短信
     * @param record 发送短信的内容
     */
    void sendSms(SystemAlarmRecord record);


    /**
     * 播放声音
     */
    void playAudio();
}
