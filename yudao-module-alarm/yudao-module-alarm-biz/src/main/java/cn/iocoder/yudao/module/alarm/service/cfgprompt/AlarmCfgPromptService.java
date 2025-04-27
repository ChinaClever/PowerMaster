package cn.iocoder.yudao.module.alarm.service.cfgprompt;

import javax.validation.*;
import cn.iocoder.yudao.module.alarm.controller.admin.cfgprompt.vo.*;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgprompt.AlarmCfgPromptDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.List;

/**
 * 系统告警配置 Service 接口
 *
 * @author 芋道源码
 */
public interface AlarmCfgPromptService {

    /**
     * 创建系统告警配置
     *
     * @param saveReqVO 创建信息
     * @return 编号
     */
    Integer saveCfgPrompt(@Valid AlarmCfgPromptSaveReqVO saveReqVO);

    /**
     * 更新系统告警配置
     *
     * @param updateReqVO 更新信息
     */
    void updateCfgPrompt(AlarmCfgPromptSaveReqVO updateReqVO);

    /**
     * 删除系统告警配置
     *
     * @param id 编号
     */
    void deleteCfgPrompt(Integer id);

    /**
     * 获得系统告警配置
     *
     * @param id 编号
     * @return 系统告警配置
     */
    AlarmCfgPromptDO getCfgPrompt(Integer id);

    /**
     * 获得系统告警配置列表
     *
     */
    List<AlarmCfgPromptDO> getCfgPromptList();

    /**
     * 获得系统告警配置分页
     *
     * @param pageReqVO 分页查询
     * @return 系统告警配置分页
     */
    PageResult<AlarmCfgPromptDO> getCfgPromptPage(AlarmCfgPromptPageReqVO pageReqVO);

    /**
     * 初始化告警配置
     *
     */
    void initAlarmPrompt();

}