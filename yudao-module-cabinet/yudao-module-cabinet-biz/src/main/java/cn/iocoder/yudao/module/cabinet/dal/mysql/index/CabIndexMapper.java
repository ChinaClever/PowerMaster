package cn.iocoder.yudao.module.cabinet.dal.mysql.index;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.*;

/**
 * 机柜索引 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CabIndexMapper extends BaseMapperX<IndexDO> {

    default PageResult<IndexDO> selectPage(IndexPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IndexDO>()
                .eqIfPresent(IndexDO::getRoomId, reqVO.getRoomId())
                .likeIfPresent(IndexDO::getName, reqVO.getName())
                .eqIfPresent(IndexDO::getAisleId, reqVO.getAisleId())
                .eqIfPresent(IndexDO::getPowCapacity, reqVO.getPowCapacity())
                .eqIfPresent(IndexDO::getPduBox, reqVO.getPduBox())
                .eqIfPresent(IndexDO::getIsDisabled, reqVO.getIsDisabled())
                .eqIfPresent(IndexDO::getIsDeleted, reqVO.getIsDeleted())
                .betweenIfPresent(IndexDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(IndexDO::getRunStatus, reqVO.getRunStatus())
                .orderByAsc(IndexDO::getId));
    }

}