package cn.iocoder.yudao.framework.common.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统告警记录 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmRecordMapper {

    @Delete("DELETE FROM alarm_log_record")
    void initLogRecordData();
}