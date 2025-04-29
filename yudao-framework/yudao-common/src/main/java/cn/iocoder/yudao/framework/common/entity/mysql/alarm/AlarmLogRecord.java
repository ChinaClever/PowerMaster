package cn.iocoder.yudao.framework.common.entity.mysql.alarm;

/**
 * @author luowei
 * @version 1.0
 * @description: TODO
 * @date 2025/4/18 13:53
 */

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统告警记录 DO
 *
 * @author 芋道源码
 */
@TableName("alarm_log_record")
@Data
public class AlarmLogRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId
    private Integer id;
    /**
     * 设备唯一标识
     */
    private String alarmKey;
    /**
     * 状态 0 未处理  1 挂起 2 确认 3 结束
     */
    private Integer alarmStatus;
    /**
     * 类型类型：1PDU离线 2PDU告警 3PDU预警 4母线告警报 5母线离线 6机柜容量
     */
    private Integer alarmType;
    /**
     * 所在位置
     */
    private String alarmPosition;
    /**
     * 告警级别 1 一级  2 二级
     */
    private Integer alarmLevel;
    /**
     * 告警信息
     */
    private String alarmDesc;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime finishTime;
    /**
     * 结束原因 1自动恢复 2手动结束
     */
    private String finishReason;
    /**
     * 确认原因
     */
    private String confirmReason;

    @TableField(value = "count(*)",insertStrategy = FieldStrategy.NEVER,updateStrategy = FieldStrategy.NEVER,select = false)
    private  Integer count;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 机房id
     */
    private Integer roomId;
    /**
     * 柜列id
     */
    private Integer aisleId;
    /**
     * 机柜id
     */
    private Integer cabinetId;

}
