package cn.iocoder.yudao.module.cabinet.dal.dataobject.statisconfig;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 机柜计算服务配置 DO
 *
 * @author clever
 */
@TableName("cabinet_statis_config")
@KeySequence("cabinet_statis_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisConfigDO {

    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     * 计费方式 1固定计费 2分段计费
     */
    private Integer billMode;
    /**
     * 按天统计历史数据任务
     */
    private String dayCron;
    /**
     * 按小时统计历史数据任务
     */
    private String hourCron;
    /**
     * 电量按天统计任务
     */
    private String eqDayCron;
    /**
     * 电量按周执行任务
     */
    private String eqWeekCron;
    /**
     * 按月统计电量任务
     */
    private String eqMonthCron;
    /**
     * 负载限制
     */
    private Integer loadLimit;
    /**
     * 状态告警开关 0关 1开
     */
    private Integer statusAlarm;
    /**
     * 存储任务
     */
    private String storeCron;
    /**
     * 告警任务
     */
    private String alarmCron;
    /**
     * 告警推送开关 0 关 1开
     */
    private Integer alarmPush;
    /**
     * 告警推送任务
     */
    private String alarmPushCron;
    /**
     * 推送mq配置
     */
    private String pushMqs;
    /**
     * redis key过期时间
     */
    private Integer redisExpire;
    /**
     * 电能存储任务
     */
    private String eleStoreCron;
    /**
     * 定时推送任务
     */
    private String timingPushCron;
    /**
     * 定时推送开关 1开启 0关闭
     */
    private Integer timingPush;
    /**
     * redis缓存任务
     */
    private String redisCron;

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