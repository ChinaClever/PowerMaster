package cn.iocoder.yudao.framework.common.mapper;


import cn.iocoder.yudao.framework.common.entity.mysql.alarm.AlarmLogRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 系统告警记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmLogRecordDoMapper extends BaseMapper<AlarmLogRecord> {

    @Delete("DELETE FROM alarm_log_record")
    void initLogRecordData();

    @Select("SELECT room_id as roomId , count(case when alarm_status = 0 then 1 else 0 end) as alarmStatus FROM alarm_log_record where room_id is not null GROUP BY room_id")
    List<Map<String, Object>> queryCount(List<Integer> ids);
}