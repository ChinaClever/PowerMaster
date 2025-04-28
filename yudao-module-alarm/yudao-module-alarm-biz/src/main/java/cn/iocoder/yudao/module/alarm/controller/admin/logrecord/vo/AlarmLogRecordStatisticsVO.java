package cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo;

import lombok.Data;

@Data
public class AlarmLogRecordStatisticsVO {
    /**
     *一级告警
     */
    private Integer alarmLevelOne;

    /**
     *二级告警
     */
    private Integer alarmLevelTwe;

    /**
     *三级告警
     */
    private Integer alarmLevelThree;

    /**
     * 确认
     */
    private Integer confirm;

    /**
     * 挂起
     */
    private Integer hung;

    /**
     * 总报警数
     */
    private Integer total;

    /**
     * 未处理
     */
    private Integer untreated;

}
