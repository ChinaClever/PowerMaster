package cn.iocoder.yudao.module.system.dal.mysql.alarm;

import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 系统告警记录
 * @date 2024/6/12 10:57
 */
@Mapper
public interface SysAlarmConfigMapper extends BaseMapper<SystemAlarmConfig> {
}
