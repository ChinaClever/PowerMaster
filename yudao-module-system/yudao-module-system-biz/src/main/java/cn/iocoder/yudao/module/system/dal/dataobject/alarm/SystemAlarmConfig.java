package cn.iocoder.yudao.module.system.dal.dataobject.alarm;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 系统告警配置
 * @date 2024/6/12 10:21
 */
@Data
@TableName(value = "sys_alarm_config")
public class SystemAlarmConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

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
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;



}
