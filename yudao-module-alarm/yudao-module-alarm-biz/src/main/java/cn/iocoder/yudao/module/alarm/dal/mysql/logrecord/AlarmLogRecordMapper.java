package cn.iocoder.yudao.module.alarm.dal.mysql.logrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.alarm.controller.admin.logrecord.vo.*;

/**
 * 系统告警记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmLogRecordMapper extends BaseMapperX<AlarmLogRecordDO> {

    default PageResult<AlarmLogRecordDO> selectPage(AlarmLogRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AlarmLogRecordDO>()
                .eqIfPresent(AlarmLogRecordDO::getAlarmKey, reqVO.getAlarmKey())
                .eqIfPresent(AlarmLogRecordDO::getAlarmStatus, reqVO.getAlarmStatus())
                .eqIfPresent(AlarmLogRecordDO::getAlarmType, reqVO.getAlarmType())
                .eqIfPresent(AlarmLogRecordDO::getAlarmPosition, reqVO.getAlarmPosition())
                .eqIfPresent(AlarmLogRecordDO::getAlarmLevel, reqVO.getAlarmLevel())
                .eqIfPresent(AlarmLogRecordDO::getAlarmDesc, reqVO.getAlarmDesc())
                .betweenIfPresent(AlarmLogRecordDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(AlarmLogRecordDO::getFinishTime, reqVO.getFinishTime())
                .eqIfPresent(AlarmLogRecordDO::getFinishReason, reqVO.getFinishReason())
                .eqIfPresent(AlarmLogRecordDO::getConfirmReason, reqVO.getConfirmReason())
                .betweenIfPresent(AlarmLogRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AlarmLogRecordDO::getId));
    }


    @Delete("DELETE FROM alarm_log_record")
    void initLogRecordData();
}