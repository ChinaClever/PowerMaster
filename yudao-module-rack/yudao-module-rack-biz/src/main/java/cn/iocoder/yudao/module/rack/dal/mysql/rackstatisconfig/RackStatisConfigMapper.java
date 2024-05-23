package cn.iocoder.yudao.module.rack.dal.mysql.rackstatisconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.rack.dal.dataobject.rackstatisconfig.RackStatisConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.rack.controller.admin.rackstatisconfig.vo.*;

/**
 * 机架计算服务配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface RackStatisConfigMapper extends BaseMapperX<RackStatisConfigDO> {

    default PageResult<RackStatisConfigDO> selectPage(RackStatisConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RackStatisConfigDO>()
                .eqIfPresent(RackStatisConfigDO::getBillMode, reqVO.getBillMode())
                .eqIfPresent(RackStatisConfigDO::getDayCron, reqVO.getDayCron())
                .eqIfPresent(RackStatisConfigDO::getHourCron, reqVO.getHourCron())
                .eqIfPresent(RackStatisConfigDO::getEqDayCron, reqVO.getEqDayCron())
                .eqIfPresent(RackStatisConfigDO::getEqWeekCron, reqVO.getEqWeekCron())
                .eqIfPresent(RackStatisConfigDO::getEqMonthCron, reqVO.getEqMonthCron())
                .betweenIfPresent(RackStatisConfigDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(RackStatisConfigDO::getStoreCron, reqVO.getStoreCron())
                .eqIfPresent(RackStatisConfigDO::getRedisExpire, reqVO.getRedisExpire())
                .eqIfPresent(RackStatisConfigDO::getEleStoreCron, reqVO.getEleStoreCron())
                .eqIfPresent(RackStatisConfigDO::getRedisCron, reqVO.getRedisCron())
                .orderByDesc(RackStatisConfigDO::getId));
    }

}