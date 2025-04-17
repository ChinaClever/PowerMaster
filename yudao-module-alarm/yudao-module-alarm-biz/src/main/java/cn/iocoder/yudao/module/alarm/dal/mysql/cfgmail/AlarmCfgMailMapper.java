package cn.iocoder.yudao.module.alarm.dal.mysql.cfgmail;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgmail.AlarmCfgMailDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.alarm.controller.admin.cfgmail.vo.*;

/**
 * 告警邮件接收人配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmCfgMailMapper extends BaseMapperX<AlarmCfgMailDO> {

    default PageResult<AlarmCfgMailDO> selectPage(AlarmCfgMailPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AlarmCfgMailDO>()
                .eqIfPresent(AlarmCfgMailDO::getIsEnable, reqVO.getIsEnable())
                .eqIfPresent(AlarmCfgMailDO::getMailAddr, reqVO.getMailAddr())
                .eqIfPresent(AlarmCfgMailDO::getMailDesc, reqVO.getMailDesc())
                .betweenIfPresent(AlarmCfgMailDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AlarmCfgMailDO::getId));
    }

}