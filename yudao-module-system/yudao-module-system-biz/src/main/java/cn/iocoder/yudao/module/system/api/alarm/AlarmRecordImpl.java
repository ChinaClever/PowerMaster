package cn.iocoder.yudao.module.system.api.alarm;

import cn.iocoder.yudao.framework.common.enums.AlarmStatusEnums;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmRecord;
import cn.iocoder.yudao.module.system.dal.mysql.alarm.SysAlarmRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * @Author: chenwany
 * @Date: 2024/7/22 16:01
 * @Description:
 */
@Service
@Validated
@Slf4j
public class AlarmRecordImpl implements AlarmRecordApi{
    @Autowired
    SysAlarmRecordMapper alarmRecordMapper;

    @Override
    public Integer getAlarmRecordNum(String name, List<Integer> alarmTypes) {

        List<SystemAlarmRecord> records = alarmRecordMapper.selectList( new LambdaQueryWrapperX<SystemAlarmRecord>()
                .likeIfPresent(SystemAlarmRecord::getDevPosition,name)
                .inIfPresent(SystemAlarmRecord::getAlarmType,alarmTypes)
                .ne(SystemAlarmRecord::getStatus, AlarmStatusEnums.FINISH.getStatus())
                .orderByDesc(SystemAlarmRecord::getCreateTime));
        if (CollectionUtils.isEmpty(records)){
            return 0;
        }else {
            return records.size();
        }
    }
}
