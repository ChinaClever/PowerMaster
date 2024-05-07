package cn.iocoder.yudao.module.pdu.dal.dataobject.dcconfig;

import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * pdu数据采集配置 DO
 *
 * @author clever
 */
@TableName("pdu_dc_config")
@KeySequence("pdu_dc_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DcConfigDO {

    /**
     * 主键ID
     */
    @TableId
    private Short id;
    /**
     * 数据接收端口
     */
    private Integer receivePort;
    /**
     * 定时存储定时任务配置
     */
    private String fixStoreCron;
    /**
     * 变化存储定时任务配置
     */
    private String changeStoreCron;
    /**
     * 电能存储定时任务配置
     */
    private String eleStoreCron;
    /**
     * 总视在功率变化比
     */
    private Integer powLimitRate;
    /**
     * rediskey过期时间（分钟）
     */
    private Integer redisExpire;
    /**
     * 离线告警时长（分钟）
     */
    private Integer offLineDuration;
    /**
     * 离线告警开关（1开启 0关闭）
     */
    private Integer offLineAlarm;
    /**
     * 状态告警开关 1开启 0关闭
     */
    private Integer statusAlarm;
    /**
     * 定时推送任务配置
     */
    private String timingPushCron;
    /**
     * 变化推送任务配置
     */
    private String changePushCron;
    /**
     * 告警推送任务配置
     */
    private String alarmPushCron;
    /**
     * 定时推送开关 1开启 0关闭
     */
    private Integer timingPush;
    /**
     * 变化推送开关 1开启 0关闭
     */
    private Integer changePush;
    /**
     * 告警推送开关 1开启 0关闭
     */
    private Integer alarmPush;
    /**
     * 配置推送的mq
     */
    private String pushMqs;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}