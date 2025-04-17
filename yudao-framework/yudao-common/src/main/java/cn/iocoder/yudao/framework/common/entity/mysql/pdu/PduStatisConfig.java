package cn.iocoder.yudao.framework.common.entity.mysql.pdu;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author luowei
 * @version 1.0
 * @description: pdu计算服务配置
 * @date 2024/4/17 14:05
 */
@Data
@TableName(value = "pdu_cron_config")
public class PduStatisConfig  implements Serializable {
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
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;
}
