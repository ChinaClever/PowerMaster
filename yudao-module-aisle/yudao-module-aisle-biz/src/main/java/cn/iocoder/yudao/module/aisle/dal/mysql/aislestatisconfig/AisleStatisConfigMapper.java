package cn.iocoder.yudao.module.aisle.dal.mysql.aislestatisconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aislestatisconfig.AisleStatisConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.aisle.controller.admin.aislestatisconfig.vo.*;

/**
 * 柜列计算服务配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface AisleStatisConfigMapper extends BaseMapperX<AisleStatisConfigDO> {

    default PageResult<AisleStatisConfigDO> selectPage(AisleStatisConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AisleStatisConfigDO>()
                .eqIfPresent(AisleStatisConfigDO::getBillMode, reqVO.getBillMode())
                .eqIfPresent(AisleStatisConfigDO::getDayCron, reqVO.getDayCron())
                .eqIfPresent(AisleStatisConfigDO::getHourCron, reqVO.getHourCron())
                .eqIfPresent(AisleStatisConfigDO::getEqDayCron, reqVO.getEqDayCron())
                .eqIfPresent(AisleStatisConfigDO::getEqWeekCron, reqVO.getEqWeekCron())
                .eqIfPresent(AisleStatisConfigDO::getEqMonthCron, reqVO.getEqMonthCron())
                .betweenIfPresent(AisleStatisConfigDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(AisleStatisConfigDO::getStoreCron, reqVO.getStoreCron())
                .eqIfPresent(AisleStatisConfigDO::getRedisExpire, reqVO.getRedisExpire())
                .eqIfPresent(AisleStatisConfigDO::getEleStoreCron, reqVO.getEleStoreCron())
                .eqIfPresent(AisleStatisConfigDO::getRedisCron, reqVO.getRedisCron())
                .eqIfPresent(AisleStatisConfigDO::getEleAlarmDay, reqVO.getEleAlarmDay())
                .eqIfPresent(AisleStatisConfigDO::getEleLimitDay, reqVO.getEleLimitDay())
                .eqIfPresent(AisleStatisConfigDO::getEleAlarmMonth, reqVO.getEleAlarmMonth())
                .eqIfPresent(AisleStatisConfigDO::getEleLimitMonth, reqVO.getEleLimitMonth())
                .orderByDesc(AisleStatisConfigDO::getId));
    }

}