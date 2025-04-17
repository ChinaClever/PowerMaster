package cn.iocoder.yudao.module.system.dal.dataobject.alarm;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author luowei
 * @version 1.0
 * @description: 系统告警记录
 * @date 2024/6/12 10:21
 */
@Data
@TableName(value = "alarm_log_record")
public class SystemAlarmRecord implements Serializable {


    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 设备key
     */
    private String devKey;

    /**
     * 设备名称
     */
    private String devName;

    /**
     * 设备类型
     */
    private Integer devType;

    /**
     * 告警类型
     */
    private Integer alarmType;


    /**
     * 告警描述
     */
    private String alarmDesc;


    /**
     * 告警级别
     */
    private Integer alarmLevel;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;


    /**
     * 记录状态
     */
    private Integer status;

    /**
     * 结束原因
     */
    private String finishReason;

    /**
     * 确认原因
     */
    private String confirmReason;

    /**
     * 位置信息
     */
    private String devPosition;

    /**
     * 更新时间
     */
//    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /**
     * 创建时间
     */
//    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "count(*)",insertStrategy = FieldStrategy.NEVER,updateStrategy = FieldStrategy.NEVER,select = false)
    private  Integer count;


}
