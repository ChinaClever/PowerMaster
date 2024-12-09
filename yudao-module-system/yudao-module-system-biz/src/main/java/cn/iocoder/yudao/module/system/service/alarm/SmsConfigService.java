package cn.iocoder.yudao.module.system.service.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.sms.SmsConfigPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.alarm.vo.sms.SmsConfigSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.alarm.SystemSmsAlarmConfig;

import javax.validation.Valid;
import java.util.List;

/**
 * @author luowei
 * @version 1.0
 * @description: 告警短信配置
 * @date 2024/4/23 9:19
 */
public interface SmsConfigService {

    /**
     * 保存配置
     *
     * @param saveReqVO 邮箱账号信息
     * @return 编号
     */
    Integer saveSmsConfig(@Valid SmsConfigSaveReqVO saveReqVO);



    /**
     * 获取配置
     * @return 邮箱账号信息
     */
    List<SystemSmsAlarmConfig> getSmsConfig();


    /**
     * 删除邮箱账号
     *
     * @param id 编号
     */
    void deleteSmsAccount(Integer id);

    /**
     * 获取邮箱账号分页信息
     *
     * @param pageReqVO 邮箱账号分页参数
     * @return 邮箱账号分页信息
     */
    PageResult<SystemSmsAlarmConfig> getSmsAccountPage(SmsConfigPageReqVO pageReqVO);

}
