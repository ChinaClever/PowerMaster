package cn.iocoder.yudao.module.alarm.service.cfgmail;

import javax.validation.*;

import cn.iocoder.yudao.module.alarm.controller.admin.cfgmail.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgmail.AlarmCfgMailDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;
import java.util.List;
import java.util.Map;

/**
 * 告警邮件接收人配置 Service 接口
 *
 * @author 芋道源码
 */
public interface AlarmCfgMailService {

    /**
     * 创建告警邮件接收人配置
     *
     * @param saveReqVO 创建信息
     * @return 编号
     */
    Integer saveCfgMail(@Valid AlarmCfgMailSaveReqVO saveReqVO);

    /**
     * 更新告警邮件接收人配置
     *
     * @param updateReqVO 更新信息
     */
    void updateCfgMail(@Valid AlarmCfgMailSaveReqVO updateReqVO);

    /**
     * 删除告警邮件接收人配置
     *
     * @param id 编号
     */
    void deleteCfgMail(Integer id);

    /**
     * 获得告警邮件接收人配置
     *
     * @param id 编号
     * @return 告警邮件接收人配置
     */
    AlarmCfgMailDO getCfgMail(Integer id);

    /**
     * 获得告警邮件接收人配置分页
     *
     * @param pageReqVO 分页查询
     * @return 告警邮件接收人配置分页
     */
    PageResult<AlarmCfgMailDO> getCfgMailPage(AlarmCfgMailPageReqVO pageReqVO);

    /**
     * 获得告警邮件配置列表
     *
     */
    List<AlarmCfgMailDO> getMailConfig();

    /**
     * 清空表
     *
     */
    void deleteMailAll();

    /**
     * 批量插入邮箱信息
     *
     * @param saveReqVOS 告邮箱信息列表
     */
    void batchSave(List<AlarmCfgMailSaveReqVO> saveReqVOS);

    /**
     * 发送邮件
     * @param records 发送邮件的内容
     */
    void sendAlarmMail(List<AlarmLogRecordDO> records);

    void pushAlarmMessage(List<Map<String, Object>> mapList);
}