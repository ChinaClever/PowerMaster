package cn.iocoder.yudao.module.bus.dal.mysql.busindex;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO.BusAisleBarQueryVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.module.bus.vo.BalanceStatisticsVO;
import cn.iocoder.yudao.module.bus.vo.LoadRateStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 始端箱索引 Mapper
 *
 * @author clever
 */
@Mapper
public interface BusIndexMapper extends BaseMapperX<BusIndexDO> {

    default PageResult<BusIndexDO> selectPage(BusIndexPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BusIndexDO>().eq(BusIndexDO::getIsDeleted,false)
                .eqIfPresent(BusIndexDO::getBusKey, reqVO.getDevKey())
                .inIfPresent(BusIndexDO::getBusKey,reqVO.getBusDevKeyList())
                .inIfPresent(BusIndexDO::getId,reqVO.getBusIds())
                .eqIfPresent(BusIndexDO::getIpAddr, reqVO.getIpAddr())
                .eqIfPresent(BusIndexDO::getBusId, reqVO.getBarId())
                .eqIfPresent(BusIndexDO::getNodeId, reqVO.getNodeIp())
                .eqIfPresent(BusIndexDO::getIsDeleted, reqVO.getIsDeleted())
                .betweenIfPresent(BusIndexDO::getCreateTime, reqVO.getCreateTime())
                .inIfPresent(BusIndexDO::getRunStatus, reqVO.getStatus())
                        .inIfPresent(BusIndexDO::getLoadRateStatus, reqVO.getLoadRateStatus())
                        .inIfPresent(BusIndexDO::getCurUnbalanceStatus,reqVO.getCurUnbalanceStatus())
//                .ne(ObjectUtil.isNotEmpty(reqVO.getStatus()),BusIndexDO::getRunStatus, 0)
                .last("ORDER BY CASE WHEN run_status =1 THEN 4 ELSE run_status END desc, create_time desc"));
    }

    default PageResult<BusIndexDO> selectPage2(BusIndexPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BusIndexDO>()
                .eqIfPresent(BusIndexDO::getBusKey, reqVO.getDevKey())
                .inIfPresent(BusIndexDO::getBusKey,reqVO.getBusDevKeyList())
                .inIfPresent(BusIndexDO::getId,reqVO.getBusIds())
                .eqIfPresent(BusIndexDO::getIpAddr, reqVO.getIpAddr())
//                .eqIfPresent(BusIndexDO::getCasAddr, reqVO.getDevAddr())
                .eqIfPresent(BusIndexDO::getBusId, reqVO.getBarId())
                .eqIfPresent(BusIndexDO::getNodeId, reqVO.getNodeIp())
                .eqIfPresent(BusIndexDO::getIsDeleted, reqVO.getIsDeleted())
                .betweenIfPresent(BusIndexDO::getCreateTime, reqVO.getCreateTime())
                .in(ObjectUtil.isNotEmpty(reqVO.getStatus()), BusIndexDO::getRunStatus, reqVO.getStatus())
                .last("ORDER BY CASE WHEN run_status =1 THEN 4 ELSE run_status END desc, create_time desc"));
    }

    IPage<BusAisleBarQueryVO> selectPageList(@Param("page") Page<Object> page,@Param("devkeys") String[] devkeys);
    List<BusAisleBarQueryVO> selectPageList(@Param("devkeys") String[] devkeys);

    IPage<BusAisleBarQueryVO> selectBoxPageList(@Param("page") Page<Object> page,@Param("devkeys") String[] devkeys);
    List<BusAisleBarQueryVO> selectBoxPageList(@Param("devkeys") String[] devkeys);

    BusIndexStatisticsResVO selectBusIndexStatistics();

    LoadRateStatus selectBusIndexLoadRateStatus();

    BalanceStatisticsVO getBusBalanceStatistics();
}