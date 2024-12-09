package cn.iocoder.yudao.module.room.dal.dataobject.roomstatisconfig;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 机房计算服务配置 DO
 *
 * @author clever
 */
@TableName("room_statis_config")
@KeySequence("room_statis_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatisConfigDO {

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
     * 存储任务
     */
    private String storeCron;
    /**
     * redis key过期时间
     */
    private Integer redisExpire;
    /**
     * 电能存储任务
     */
    private String eleStoreCron;
    /**
     * redis保存定时
     */
    private String redisCron;
    /**
     * 日用电告警开关 0 关 1开
     */
    private Integer eleAlarmDay;
    /**
     * 月用电告警开关 0关 1开
     */
    private Integer eleAlarmMonth;
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