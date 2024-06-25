package cn.iocoder.yudao.module.bus.dal.mysql.busindex;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;

/**
 * 始端箱索引 Mapper
 *
 * @author clever
 */
@Mapper
public interface BusIndexMapper extends BaseMapperX<BusIndexDO> {

    default PageResult<BusIndexDO> selectPage(BusIndexPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BusIndexDO>()
                .eqIfPresent(BusIndexDO::getDevKey, reqVO.getDevKey())
                .inIfPresent(BusIndexDO::getDevKey,reqVO.getBusDevKeyList())
                .eqIfPresent(BusIndexDO::getIpAddr, reqVO.getIpAddr())
                .eqIfPresent(BusIndexDO::getDevAddr, reqVO.getDevAddr())
                .eqIfPresent(BusIndexDO::getBarId, reqVO.getBarId())
                .eqIfPresent(BusIndexDO::getRunStatus, reqVO.getRunStatus())
                .eqIfPresent(BusIndexDO::getNodeIp, reqVO.getNodeIp())
                .eqIfPresent(BusIndexDO::getIsDeleted, reqVO.getIsDeleted())
                .betweenIfPresent(BusIndexDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(BusIndexDO::getId));
    }

}