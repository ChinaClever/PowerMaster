package cn.iocoder.yudao.module.alarm.dal.mysql.cfgprompt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgprompt.AlarmCfgPromptDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.alarm.controller.admin.cfgprompt.vo.*;

/**
 * 系统告警配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmCfgPromptMapper extends BaseMapperX<AlarmCfgPromptDO> {

    default PageResult<AlarmCfgPromptDO> selectPage(AlarmCfgPromptPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AlarmCfgPromptDO>()
                .eqIfPresent(AlarmCfgPromptDO::getIsEnable, reqVO.getIsEnable())
                .eqIfPresent(AlarmCfgPromptDO::getPromptType, reqVO.getPromptType())
                .betweenIfPresent(AlarmCfgPromptDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AlarmCfgPromptDO::getId));
    }

    default Integer getCfgPromptByType(Integer code) {
        return selectOne(new LambdaQueryWrapperX<AlarmCfgPromptDO>()
                .eq(AlarmCfgPromptDO::getPromptType, code)
                .last("limit 1")).getIsEnable();
    }
}