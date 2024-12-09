package cn.iocoder.yudao.module.system.service.alarm;

import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.config.ConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemAlarmConfig;

import javax.validation.Valid;

/**
 * @author luowei
 * @version 1.0
 * @description: 告警配置
 * @date 2024/4/23 9:19
 */
public interface AlarmConfigService {

    /**
     * 保存配置
     *
     * @param saveReqVO 配置信息
     * @return 编号
     */
    Integer saveConfig(@Valid ConfigSaveReqVO saveReqVO);



    /**
     * 获取配置
     * @return 配置信息
     */
    SystemAlarmConfig getConfig();
}
