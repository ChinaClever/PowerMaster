package cn.iocoder.yudao.module.alarm.service.cfgsms;

import javax.validation.*;

import cn.iocoder.yudao.module.alarm.controller.admin.cfgsms.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgsms.AlarmCfgSmsDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.alarm.dal.dataobject.logrecord.AlarmLogRecordDO;

import java.util.List;

/**
 * 告警短信接收人配置 Service 接口
 *
 * @author 芋道源码
 */
public interface AlarmCfgSmsService {

    /**
     * 创建告警短信接收人配置
     *
     * @param saveReqVOS 创建信息
     * @return 编号
     */
    Integer saveCfgSms(@Valid AlarmCfgSmsSaveReqVO saveReqVOS);

    /**
     * 更新告警短信接收人配置
     *
     * @param updateReqVO 更新信息
     */
    void updateCfgSms(@Valid AlarmCfgSmsSaveReqVO updateReqVO);

    /**
     * 删除告警短信接收人配置
     *
     * @param id 编号
     */
    void deleteCfgSms(Integer id);

    /**
     * 批量删除告警短信接收人配置
     */
    void deleteSmsAll();

    /**
     * 获得告警短信接收人配置
     *
     * @param id 编号
     * @return 告警短信接收人配置
     */
    AlarmCfgSmsDO getCfgSms(Integer id);

    /**
     * 获得告警短信接收人配置分页
     *
     * @param pageReqVO 分页查询
     * @return 告警短信接收人配置分页
     */
    PageResult<AlarmCfgSmsDO> getCfgSmsPage(AlarmCfgSmsPageReqVO pageReqVO);

    /**
     * 批量保存告警短信接收人配置
     *
     * @param saveReqVOS 创建信息
     * @return 编号
     */
    void batchSave(List<AlarmCfgSmsPageReqVO> saveReqVOS);

    /**
     * 获得手机账号列表
     *
     */
    List<AlarmCfgSmsDO> getPhoneList();


    /**
     * 发送短信
     * @param record 发送短信的内容
     */
    void sendSms(AlarmLogRecordDO record);

}