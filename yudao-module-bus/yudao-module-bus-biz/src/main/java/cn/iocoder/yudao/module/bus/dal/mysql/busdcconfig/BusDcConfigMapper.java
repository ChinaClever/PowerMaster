package cn.iocoder.yudao.module.bus.dal.mysql.busdcconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bus.dal.dataobject.busdcconfig.BusDcConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.bus.controller.admin.busdcconfig.vo.*;

/**
 * 母线数据采集配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface BusDcConfigMapper extends BaseMapperX<BusDcConfigDO> {

    default PageResult<BusDcConfigDO> selectPage(BusDcConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BusDcConfigDO>()
                .eqIfPresent(BusDcConfigDO::getReceivePort, reqVO.getReceivePort())
                .eqIfPresent(BusDcConfigDO::getFixStoreCron, reqVO.getFixStoreCron())
                .eqIfPresent(BusDcConfigDO::getChangeStoreCron, reqVO.getChangeStoreCron())
                .eqIfPresent(BusDcConfigDO::getEleStoreCron, reqVO.getEleStoreCron())
                .eqIfPresent(BusDcConfigDO::getPowLimitRate, reqVO.getPowLimitRate())
                .eqIfPresent(BusDcConfigDO::getRedisExpire, reqVO.getRedisExpire())
                .eqIfPresent(BusDcConfigDO::getOffLineDuration, reqVO.getOffLineDuration())
                .eqIfPresent(BusDcConfigDO::getOffLineAlarm, reqVO.getOffLineAlarm())
                .eqIfPresent(BusDcConfigDO::getStatusAlarm, reqVO.getStatusAlarm())
                .eqIfPresent(BusDcConfigDO::getTimingPushCron, reqVO.getTimingPushCron())
                .eqIfPresent(BusDcConfigDO::getChangePushCron, reqVO.getChangePushCron())
                .eqIfPresent(BusDcConfigDO::getAlarmPushCron, reqVO.getAlarmPushCron())
                .eqIfPresent(BusDcConfigDO::getTimingPush, reqVO.getTimingPush())
                .eqIfPresent(BusDcConfigDO::getChangePush, reqVO.getChangePush())
                .eqIfPresent(BusDcConfigDO::getAlarmPush, reqVO.getAlarmPush())
                .betweenIfPresent(BusDcConfigDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(BusDcConfigDO::getPushMqs, reqVO.getPushMqs())
                .eqIfPresent(BusDcConfigDO::getFixStore, reqVO.getFixStore())
                .eqIfPresent(BusDcConfigDO::getChangeStore, reqVO.getChangeStore())
                .eqIfPresent(BusDcConfigDO::getEleStore, reqVO.getEleStore())
                .eqIfPresent(BusDcConfigDO::getRedisSwitch, reqVO.getRedisSwitch())
                .orderByDesc(BusDcConfigDO::getId));
    }

}