package cn.iocoder.yudao.module.aisle.dal.mysql.aisleindex;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo.*;

/**
 * 通道列 Mapper
 *
 * @author clever
 */
@Mapper
public interface AisleIndexCopyMapper extends BaseMapperX<AisleIndexDO> {

    default PageResult<AisleIndexDO> selectPage(AisleIndexPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AisleIndexDO>()
                .eqIfPresent(AisleIndexDO::getRoomId, reqVO.getRoomId())
                .inIfPresent(AisleIndexDO::getId,reqVO.getAisleIds())
                .eqIfPresent(AisleIndexDO::getId,reqVO.getId())
                .likeIfPresent(AisleIndexDO::getName, reqVO.getName())
                .eqIfPresent(AisleIndexDO::getPduBar, reqVO.getPduBar())
                .eqIfPresent(AisleIndexDO::getIsDelete, reqVO.getIsDelete())
                .betweenIfPresent(AisleIndexDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(AisleIndexDO::getLength, reqVO.getLength())
                .eqIfPresent(AisleIndexDO::getType, reqVO.getType())
                .orderByAsc(AisleIndexDO::getId));
    }

}