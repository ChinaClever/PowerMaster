package cn.iocoder.yudao.module.alarm.service.logrecord;

import javax.validation.*;
import cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 系统告警记录 Service 接口
 *
 * @author 芋道源码
 */
public interface AlarmLogRecordService {

    /**
     * 创建系统告警记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer saveLogRecord(@Valid AlarmLogRecordSaveReqVO createReqVO);

    /**
     * 更新系统告警记录
     *
     * @param updateReqVO 更新信息
     */
    void updateLogRecord(AlarmLogRecordSaveReqVO updateReqVO);

    /**
     * 删除系统告警记录
     *
     * @param id 编号
     */
    void deleteLogRecord(Integer id);

    /**
     * 批量删除系统告警记录
     *
     * @param ids 编号列表
     */
    void deleteBatchIds(List<Integer> ids);

    /**
     * 获得系统告警记录
     *
     * @param id 编号
     * @return 系统告警记录
     */
    AlarmLogRecordDO getLogRecord(Integer id);

    /**
     * 获得系统告警记录分页
     *
     * @param pageReqVO 分页查询
     * @return 系统告警记录分页
     */
    PageResult<AlarmLogRecordRespVO> getLogRecordPage(AlarmLogRecordPageReqVO pageReqVO);

    /**
     * 告警等级统计
     * @return
     */
    AlarmLogRecordStatisticsVO levelCount(Integer roomId);


    /**
     * 通过id获取告警记录
     *
     * @param id 告警记录id
     * @return 告警记录信息
     */
    AlarmLogRecordDO getAlarmRecordById(Integer id);

    /**
     * 当pdu报警时，插入/修改告警记录
     *
     */
    void insertOrUpdateAlarmRecordWhenPduAlarm( List<Map<String, Object>> oldMaps , List<Map<String, Object>> newMaps);

    /**
     * 当bus报警时，插入告警记录
     *
     */
    void insertOrUpdateAlarmRecordWhenBusAlarm(List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps);


    /**
     * 当cabinet报警时，插入告警记录
     *
     */
    void insertOrUpdateAlarmRecordWhenCabinetAlarm(List<Map<String, Object>> oldMaps, List<Map<String, Object>> newMaps);
}