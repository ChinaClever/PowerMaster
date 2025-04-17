package cn.iocoder.yudao.module.alarm.api.alarm;

import cn.iocoder.yudao.framework.common.enums.AlarmStatusEnums;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import cn.iocoder.yudao.module.alarm.dal.mysql.logrecord.AlarmLogRecordMapper;
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
    AlarmLogRecordMapper alarmLogRecordMapper;

    @Override
    public Integer getAlarmRecordNum(String name, List<Integer> alarmTypes) {

        List<AlarmLogRecordDO> records = alarmLogRecordMapper.selectList( new LambdaQueryWrapperX<AlarmLogRecordDO>()
                .likeIfPresent(AlarmLogRecordDO::getAlarmPosition,name)
                .inIfPresent(AlarmLogRecordDO::getAlarmType,alarmTypes)
                .ne(AlarmLogRecordDO::getAlarmStatus, AlarmStatusEnums.FINISH.getStatus())
                .orderByDesc(AlarmLogRecordDO::getCreateTime));
        if (CollectionUtils.isEmpty(records)){
            return 0;
        }else {
            return records.size();
        }
    }
}
