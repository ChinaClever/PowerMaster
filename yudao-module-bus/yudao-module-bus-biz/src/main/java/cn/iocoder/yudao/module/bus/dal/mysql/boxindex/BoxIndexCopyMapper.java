package cn.iocoder.yudao.module.bus.dal.mysql.boxindex;

import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bus.vo.BalanceStatisticsVO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.BoxIndexPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.BusIndexStatisticsResVO;
import cn.iocoder.yudao.module.bus.vo.LoadRateStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author luowei
 * @version 1.0
 * @description: 插接箱索引
 * @date 2024/5/30 14:55
 */
@Mapper
public interface BoxIndexCopyMapper extends BaseMapperX<BoxIndex> {

    default PageResult<BoxIndex> selectPage(BoxIndexPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BoxIndex>()
                .eqIfPresent(BoxIndex::getBoxKey, reqVO.getDevKey())
                .inIfPresent(BoxIndex::getBoxKey,reqVO.getBoxDevKeyList())
                .inIfPresent(BoxIndex::getId,reqVO.getBoxIds())
                .eqIfPresent(BoxIndex::getIpAddr, reqVO.getIpAddr())
                .eqIfPresent(BoxIndex::getBoxId, reqVO.getBarId())
                .eqIfPresent(BoxIndex::getRunStatus, reqVO.getRunStatus())
                .inIfPresent(BoxIndex::getLoadRateStatus, reqVO.getColor())
                .eqIfPresent(BoxIndex::getNodeId, reqVO.getNodeIp())
                .eqIfPresent(BoxIndex::getIsDeleted, reqVO.getIsDeleted())
                .inIfPresent(BoxIndex::getCurUnbalanceStatus,reqVO.getCurUnbalanceStatus())
                .eqIfPresent(BoxIndex::getBoxType,0)
                .betweenIfPresent(BoxIndex::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(BoxIndex::getId));
    }

    BusIndexStatisticsResVO getBoxIndexStatistics();

    BalanceStatisticsVO getBoxBalanceStatistics();

    LoadRateStatus getBoxIndexLoadRateStatus();

}
