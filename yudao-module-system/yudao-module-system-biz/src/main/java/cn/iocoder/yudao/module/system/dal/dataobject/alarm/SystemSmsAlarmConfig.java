package cn.iocoder.yudao.module.system.dal.dataobject.alarm;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 告警邮件发送配置
 * @date 2024/6/13 11:43
 */
@Data
@TableName(value = "sys_sms_alarm_config")
public class SystemSmsAlarmConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 手机号
     */
    private String phone;


    /**
     * 是否启用
     */
    private int isEnable;


    /**
     * 描述
     */
    private String smsDesc;

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
