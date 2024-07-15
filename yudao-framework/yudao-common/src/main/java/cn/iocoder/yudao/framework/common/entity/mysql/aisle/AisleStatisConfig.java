package cn.iocoder.yudao.framework.common.entity.mysql.aisle;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luowei
 * @version 1.0
 * @description: 柜列计算服务配置
 * @date 2024/4/17 14:05
 */
@Data
@TableName(value = "aisle_statis_config")
public class AisleStatisConfig implements Serializable {
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
     * 存储任务
     */
    private String storeCron;

    /**
     * redis推送任务
     */
    private String redisCron;

    /**
     * 电能存储任务
     */
    private String eleStoreCron;


    /**
     * redis key过期时间
     */
    private int redisExpire;

    /**
     * 日用能告警开关
     */
    private  int eleAlarmDay;

    /**
     * 月用能告警开关
     */
    private  int eleAlarmMonth;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
