package cn.iocoder.yudao.module.pdu.dal.mysql.statisconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pdu.dal.dataobject.statisconfig.StatisConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.pdu.controller.admin.statisconfig.vo.*;

/**
 * pdu计算服务配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface StatisConfigMapper extends BaseMapperX<StatisConfigDO> {

    default PageResult<StatisConfigDO> selectPage(StatisConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StatisConfigDO>()
                .eqIfPresent(StatisConfigDO::getId, reqVO.getId())
                .eqIfPresent(StatisConfigDO::getBillMode, reqVO.getBillMode())
                .eqIfPresent(StatisConfigDO::getDayCron, reqVO.getDayCron())
                .eqIfPresent(StatisConfigDO::getHourCron, reqVO.getHourCron())
                .eqIfPresent(StatisConfigDO::getEqDayCron, reqVO.getEqDayCron())
                .eqIfPresent(StatisConfigDO::getEqWeekCron, reqVO.getEqWeekCron())
                .eqIfPresent(StatisConfigDO::getEqMonthCron, reqVO.getEqMonthCron())
                .orderByDesc(StatisConfigDO::getId));
    }

}