package cn.iocoder.yudao.framework.common.mapper;


import cn.iocoder.yudao.framework.common.entity.mysql.alarm.AlarmLogRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统告警记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmLogRecordDoMapper extends BaseMapper<AlarmLogRecord> {

    @Delete("DELETE FROM alarm_log_record")
    void initLogRecordData();
}