package cn.iocoder.yudao.module.alarm.controller.admin.cfgprompt.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 系统告警配置
 * @date 2024/6/12 10:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmPromptConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 告警发送邮件开关
     */
    private int mailAlarm;

    /**
     * 告警声音开关
     */
    private int voiceAlarm;

    /**
     * 短信告警开关
     */
    private int smsAlarm;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;



}
