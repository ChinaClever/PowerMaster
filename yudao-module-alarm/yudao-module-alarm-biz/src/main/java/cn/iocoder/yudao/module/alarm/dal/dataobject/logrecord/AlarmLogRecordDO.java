package cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord;

import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 系统告警记录 DO
 *
 * @author 芋道源码
 */
@TableName("alarm_log_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmLogRecordDO implements Serializable {

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
}