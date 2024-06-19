package cn.iocoder.yudao.module.system.dal.mysql.alarm;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemMailAlarmConfig;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemSmsAlarmConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 系统短信告警
 * @date 2024/6/12 10:57
 */
@Mapper
public interface SysAlarmSmsConfigMapper extends BaseMapperX<SystemSmsAlarmConfig> {
}
