package cn.iocoder.yudao.module.system.api.alarm;

import javax.validation.Valid;
import java.util.List;

/**
 * 获取告警记录 接口
 *
 * @author 芋道源码
 */
public interface AlarmRecordApi {


    /**
     * 获取告警数量
     * @param name
     * @param alarmTypes
     * @return
     */
    Integer getAlarmRecordNum(@Valid String name,@Valid List<Integer> alarmTypes);


}
