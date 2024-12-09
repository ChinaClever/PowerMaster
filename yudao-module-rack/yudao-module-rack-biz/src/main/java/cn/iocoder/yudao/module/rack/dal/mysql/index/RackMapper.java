package cn.iocoder.yudao.module.rack.dal.mysql.index;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.rack.dal.dataobject.index.RackDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.rack.controller.admin.index.vo.*;

/**
 * 机架索引 Mapper
 *
 * @author clever
 */
@Mapper
public interface RackMapper extends BaseMapperX<RackDO> {

    default PageResult<RackDO> selectPage(IndexPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RackDO>()
                .eqIfPresent(RackDO::getCabinetId, reqVO.getCabinetId())
                .eqIfPresent(RackDO::getRoomId, reqVO.getRoomId())
                .likeIfPresent(RackDO::getRackName, reqVO.getRackName())
                .eqIfPresent(RackDO::getIsDelete, reqVO.getIsDelete())
                .eqIfPresent(RackDO::getOutletIdA, reqVO.getOutletIdA())
                .eqIfPresent(RackDO::getOutletIdB, reqVO.getOutletIdB())
                .eqIfPresent(RackDO::getCompany, reqVO.getCompany())
                .eqIfPresent(RackDO::getUAddress, reqVO.getUAddress())
                .eqIfPresent(RackDO::getUHeight, reqVO.getUHeight())
                .betweenIfPresent(RackDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(RackDO::getType, reqVO.getType())
                .orderByDesc(RackDO::getId));
    }

}