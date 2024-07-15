package cn.iocoder.yudao.module.room.dal.mysql.roomstatisconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.room.dal.dataobject.roomstatisconfig.RoomStatisConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.room.controller.admin.roomstatisconfig.vo.*;

/**
 * 机房计算服务配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface RoomStatisConfigMapper extends BaseMapperX<RoomStatisConfigDO> {

    default PageResult<RoomStatisConfigDO> selectPage(RoomStatisConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RoomStatisConfigDO>()
                .eqIfPresent(RoomStatisConfigDO::getBillMode, reqVO.getBillMode())
                .eqIfPresent(RoomStatisConfigDO::getDayCron, reqVO.getDayCron())
                .eqIfPresent(RoomStatisConfigDO::getHourCron, reqVO.getHourCron())
                .eqIfPresent(RoomStatisConfigDO::getEqDayCron, reqVO.getEqDayCron())
                .eqIfPresent(RoomStatisConfigDO::getEqWeekCron, reqVO.getEqWeekCron())
                .eqIfPresent(RoomStatisConfigDO::getEqMonthCron, reqVO.getEqMonthCron())
                .betweenIfPresent(RoomStatisConfigDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(RoomStatisConfigDO::getStoreCron, reqVO.getStoreCron())
                .eqIfPresent(RoomStatisConfigDO::getRedisExpire, reqVO.getRedisExpire())
                .eqIfPresent(RoomStatisConfigDO::getEleStoreCron, reqVO.getEleStoreCron())
                .eqIfPresent(RoomStatisConfigDO::getRedisCron, reqVO.getRedisCron())
                .eqIfPresent(RoomStatisConfigDO::getEleAlarmDay, reqVO.getEleAlarmDay())
                .eqIfPresent(RoomStatisConfigDO::getEleAlarmMonth, reqVO.getEleAlarmMonth())
                .orderByDesc(RoomStatisConfigDO::getId));
    }

}