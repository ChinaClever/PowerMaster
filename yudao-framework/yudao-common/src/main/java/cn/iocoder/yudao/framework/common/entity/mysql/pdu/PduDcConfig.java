package cn.iocoder.yudao.framework.common.entity.mysql.pdu;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author luowei
 * @version 1.0
 * @description: pdu配置表
 * @date 2024/4/16 9:38
 */
@Data
@TableName(value = "pdu_dc_config")
public class PduDcConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    /**
     * 数据接收端口
     */
    private Integer receivePort;

    /**
     * 定时存储任务
     */
    private String fixStoreCron;
    /**
     * 变化存储任务
     */
    private String changeStoreCron;

    /**
     * 电能存储任务
     */
    private String eleStoreCron;

    /**
     * 总视在功率变化比（百分比）
     */
    private int powLimitRate;

    /**
     * redis key过期时间
     */
    private int redisExpire;

    /**
     * 离线时长
     */
    private int offLineDuration;

    /**
     * 离线告警开关
     */
    private int offLineAlarm;

    /**
     * 状态告警开关
     */
    private int statusAlarm;

    /**
     * 定时推送
     */
    private String timingPushCron;

    /**
     * 变化推送
     */
    private String changePushCron;

    /**
     * 告警推送
     */
    private String alarmPushCron;
    /**
     * 定时推送
     */
    private int timingPush;

    /**
     * 变化推送
     */
    private int changePush;

    /**
     * 告警推送
     */
    private int alarmPush;

    /**
     * 推送mq配置
     */
    private String pushMqs;

    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 创建时间
     */
    private String createTime;
}
