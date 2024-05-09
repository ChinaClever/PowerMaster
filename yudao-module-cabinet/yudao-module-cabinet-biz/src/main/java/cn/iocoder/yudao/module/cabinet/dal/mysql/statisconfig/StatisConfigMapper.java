package cn.iocoder.yudao.module.cabinet.dal.mysql.statisconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.statisconfig.StatisConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig.vo.*;

/**
 * 机柜计算服务配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface StatisConfigMapper extends BaseMapperX<StatisConfigDO> {

    default PageResult<StatisConfigDO> selectPage(StatisConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StatisConfigDO>()
                .eqIfPresent(StatisConfigDO::getBillMode, reqVO.getBillMode())
                .eqIfPresent(StatisConfigDO::getDayCron, reqVO.getDayCron())
                .eqIfPresent(StatisConfigDO::getHourCron, reqVO.getHourCron())
                .eqIfPresent(StatisConfigDO::getEqDayCron, reqVO.getEqDayCron())
                .eqIfPresent(StatisConfigDO::getEqWeekCron, reqVO.getEqWeekCron())
                .eqIfPresent(StatisConfigDO::getEqMonthCron, reqVO.getEqMonthCron())
                .eqIfPresent(StatisConfigDO::getLoadLimit, reqVO.getLoadLimit())
                .eqIfPresent(StatisConfigDO::getStatusAlarm, reqVO.getStatusAlarm())
                .eqIfPresent(StatisConfigDO::getStoreCron, reqVO.getStoreCron())
                .eqIfPresent(StatisConfigDO::getAlarmCron, reqVO.getAlarmCron())
                .eqIfPresent(StatisConfigDO::getAlarmPush, reqVO.getAlarmPush())
                .eqIfPresent(StatisConfigDO::getAlarmPushCron, reqVO.getAlarmPushCron())
                .eqIfPresent(StatisConfigDO::getPushMqs, reqVO.getPushMqs())
                .eqIfPresent(StatisConfigDO::getRedisExpire, reqVO.getRedisExpire())
                .eqIfPresent(StatisConfigDO::getEleStoreCron, reqVO.getEleStoreCron())
                .eqIfPresent(StatisConfigDO::getTimingPushCron, reqVO.getTimingPushCron())
                .eqIfPresent(StatisConfigDO::getTimingPush, reqVO.getTimingPush())
                .betweenIfPresent(StatisConfigDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(StatisConfigDO::getRedisCron, reqVO.getRedisCron())
                .orderByDesc(StatisConfigDO::getId));
    }

}