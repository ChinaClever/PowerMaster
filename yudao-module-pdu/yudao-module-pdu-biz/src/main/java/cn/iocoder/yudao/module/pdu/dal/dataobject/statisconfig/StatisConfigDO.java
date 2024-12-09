package cn.iocoder.yudao.module.pdu.dal.dataobject.statisconfig;

import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * pdu计算服务配置 DO
 *
 * @author clever
 */
@TableName("pdu_statis_config")
@KeySequence("pdu_statis_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisConfigDO{

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