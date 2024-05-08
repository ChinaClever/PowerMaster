package cn.iocoder.yudao.framework.common.entity.mysql.cabinet;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 机柜计算服务配置
 * @date 2024/4/17 14:05
 */
@Data
@TableName(value = "cabinet_statis_config")
public class CabinetStatisConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    /**
     * 电量计费方式
     */
    private int billMode;


    /**
     * 按天执行任务配置
     */
    private String dayCron;

    /**
     * 按小时执行任务配置
     */
    private String hourCron;

    /**
     * 电量按天执行任务配置
     */
    private String eqDayCron;

    /**
     * 电费计算按周
     */
    private String eqWeekCron;


    /**
     * 电费计算按月执行任务配置
     */
    private String eqMonthCron;

    /**
     * 负载限制
     */
    private int loadLimit;


    /**
     * 状态告警开关
     */
    private int statusAlarm;

    /**
     * 存储任务
     */
    private String storeCron;

    /**
     * 告警任务
     */
    private String alarmCron;


    /**
     * 告警推送开关
     */
    private int alarmPush;

    /**
     * 告警推送任务
     */
    private String alarmPushCron;

    /**
     * 定时推送开关
     */
    private int timingPush;

    /**
     * 定时推送任务
     */
    private String timingPushCron;

    /**
     * redis推送任务
     */
    private String redisCron;


    /**
     * 电能存储任务
     */
    private String eleStoreCron;


    /**
     * 推送mq配置
     */
    private String pushMqs;


    /**
     * redis key过期时间
     */
    private int redisExpire;


    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
