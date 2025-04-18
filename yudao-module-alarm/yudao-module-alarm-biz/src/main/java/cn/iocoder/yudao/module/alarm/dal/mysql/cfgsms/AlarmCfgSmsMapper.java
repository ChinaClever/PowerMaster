package cn.iocoder.yudao.module.alarm.dal.mysql.cfgsms;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.alarm.dal.dataobject.cfgsms.AlarmCfgSmsDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.alarm.controller.admin.cfgsms.vo.*;

/**
 * 告警短信接收人配置 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AlarmCfgSmsMapper extends BaseMapperX<AlarmCfgSmsDO> {


    default PageResult<AlarmCfgSmsDO> selectPage(AlarmCfgSmsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AlarmCfgSmsDO>()
                .eqIfPresent(AlarmCfgSmsDO::getIsEnable, reqVO.getIsEnable())
                .eqIfPresent(AlarmCfgSmsDO::getPhoneNumber, reqVO.getPhoneNumber())
                .eqIfPresent(AlarmCfgSmsDO::getSmsDesc, reqVO.getSmsDesc())
                .betweenIfPresent(AlarmCfgSmsDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AlarmCfgSmsDO::getId));
    }

    @Delete("DELETE FROM alarm_cfg_sms")
    void initCfgSmsData();
}