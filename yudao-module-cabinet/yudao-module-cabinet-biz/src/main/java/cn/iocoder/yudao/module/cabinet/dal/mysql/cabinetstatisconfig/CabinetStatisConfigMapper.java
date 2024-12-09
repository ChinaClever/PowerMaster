package cn.iocoder.yudao.module.cabinet.dal.mysql.cabinetstatisconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.statisconfig.CabinetStatisConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig.vo.*;

/**
 * 机柜计算服务配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface CabinetStatisConfigMapper extends BaseMapperX<CabinetStatisConfigDO> {

    default PageResult<CabinetStatisConfigDO> selectPage(CabinetStatisConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CabinetStatisConfigDO>()
                .eqIfPresent(CabinetStatisConfigDO::getBillMode, reqVO.getBillMode())
                .eqIfPresent(CabinetStatisConfigDO::getDayCron, reqVO.getDayCron())
                .eqIfPresent(CabinetStatisConfigDO::getHourCron, reqVO.getHourCron())
                .eqIfPresent(CabinetStatisConfigDO::getEqDayCron, reqVO.getEqDayCron())
                .eqIfPresent(CabinetStatisConfigDO::getEqWeekCron, reqVO.getEqWeekCron())
                .eqIfPresent(CabinetStatisConfigDO::getEqMonthCron, reqVO.getEqMonthCron())
                .eqIfPresent(CabinetStatisConfigDO::getLoadLimit, reqVO.getLoadLimit())
                .eqIfPresent(CabinetStatisConfigDO::getStatusAlarm, reqVO.getStatusAlarm())
                .eqIfPresent(CabinetStatisConfigDO::getStoreCron, reqVO.getStoreCron())
                .eqIfPresent(CabinetStatisConfigDO::getAlarmCron, reqVO.getAlarmCron())
                .eqIfPresent(CabinetStatisConfigDO::getAlarmPush, reqVO.getAlarmPush())
                .eqIfPresent(CabinetStatisConfigDO::getAlarmPushCron, reqVO.getAlarmPushCron())
                .eqIfPresent(CabinetStatisConfigDO::getPushMqs, reqVO.getPushMqs())
                .eqIfPresent(CabinetStatisConfigDO::getRedisExpire, reqVO.getRedisExpire())
                .eqIfPresent(CabinetStatisConfigDO::getEleStoreCron, reqVO.getEleStoreCron())
                .eqIfPresent(CabinetStatisConfigDO::getTimingPushCron, reqVO.getTimingPushCron())
                .eqIfPresent(CabinetStatisConfigDO::getTimingPush, reqVO.getTimingPush())
                .betweenIfPresent(CabinetStatisConfigDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(CabinetStatisConfigDO::getRedisCron, reqVO.getRedisCron())
                .orderByAsc(CabinetStatisConfigDO::getId));
    }

}