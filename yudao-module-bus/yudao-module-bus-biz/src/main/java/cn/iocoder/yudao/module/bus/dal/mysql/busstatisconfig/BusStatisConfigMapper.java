package cn.iocoder.yudao.module.bus.dal.mysql.busstatisconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bus.dal.dataobject.busstatisconfig.BusStatisConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.bus.controller.admin.busstatisconfig.vo.*;

/**
 * 母线计算服务配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface BusStatisConfigMapper extends BaseMapperX<BusStatisConfigDO> {

    default PageResult<BusStatisConfigDO> selectPage(BusStatisConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BusStatisConfigDO>()
                .eqIfPresent(BusStatisConfigDO::getBillMode, reqVO.getBillMode())
                .eqIfPresent(BusStatisConfigDO::getDayCron, reqVO.getDayCron())
                .eqIfPresent(BusStatisConfigDO::getHourCron, reqVO.getHourCron())
                .eqIfPresent(BusStatisConfigDO::getEqDayCron, reqVO.getEqDayCron())
                .eqIfPresent(BusStatisConfigDO::getEqWeekCron, reqVO.getEqWeekCron())
                .eqIfPresent(BusStatisConfigDO::getEqMonthCron, reqVO.getEqMonthCron())
                .betweenIfPresent(BusStatisConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(BusStatisConfigDO::getId));
    }

}