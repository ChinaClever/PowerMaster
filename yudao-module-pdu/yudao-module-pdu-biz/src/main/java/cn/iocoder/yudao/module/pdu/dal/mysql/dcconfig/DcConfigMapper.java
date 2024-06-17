package cn.iocoder.yudao.module.pdu.dal.mysql.dcconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pdu.dal.dataobject.dcconfig.DcConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo.*;

/**
 * pdu数据采集配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface DcConfigMapper extends BaseMapperX<DcConfigDO> {

    default PageResult<DcConfigDO> selectPage(DcConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DcConfigDO>()
                .eqIfPresent(DcConfigDO::getReceivePort, reqVO.getReceivePort())
                .eqIfPresent(DcConfigDO::getFixStoreCron, reqVO.getFixStoreCron())
                .eqIfPresent(DcConfigDO::getChangeStoreCron, reqVO.getChangeStoreCron())
                .eqIfPresent(DcConfigDO::getEleStoreCron, reqVO.getEleStoreCron())
                .eqIfPresent(DcConfigDO::getPowLimitRate, reqVO.getPowLimitRate())
                .eqIfPresent(DcConfigDO::getRedisExpire, reqVO.getRedisExpire())
                .eqIfPresent(DcConfigDO::getOffLineDuration, reqVO.getOffLineDuration())
                .eqIfPresent(DcConfigDO::getOffLineAlarm, reqVO.getOffLineAlarm())
                .eqIfPresent(DcConfigDO::getStatusAlarm, reqVO.getStatusAlarm())
                .eqIfPresent(DcConfigDO::getTimingPushCron, reqVO.getTimingPushCron())
                .eqIfPresent(DcConfigDO::getChangePushCron, reqVO.getChangePushCron())
                .eqIfPresent(DcConfigDO::getAlarmPushCron, reqVO.getAlarmPushCron())
                .eqIfPresent(DcConfigDO::getTimingPush, reqVO.getTimingPush())
                .eqIfPresent(DcConfigDO::getChangePush, reqVO.getChangePush())
                .eqIfPresent(DcConfigDO::getAlarmPush, reqVO.getAlarmPush())
                .betweenIfPresent(DcConfigDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(DcConfigDO::getPushMqs, reqVO.getPushMqs())
                .eqIfPresent(DcConfigDO::getFixStore, reqVO.getFixStore())
                .eqIfPresent(DcConfigDO::getChangeStore, reqVO.getChangeStore())
                .eqIfPresent(DcConfigDO::getEleStore, reqVO.getEleStore())
                .eqIfPresent(DcConfigDO::getRedisSwitch, reqVO.getRedisSwitch())
                .orderByAsc(DcConfigDO::getId));
    }

}