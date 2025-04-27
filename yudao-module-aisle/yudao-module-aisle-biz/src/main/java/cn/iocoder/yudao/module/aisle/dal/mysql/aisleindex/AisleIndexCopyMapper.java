package cn.iocoder.yudao.module.aisle.dal.mysql.aisleindex;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.aisle.dal.dataobject.aisleindex.AisleIndexDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.aisle.controller.admin.aisleindex.vo.*;
import org.apache.ibatis.annotations.Param;

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
                .likeIfPresent(AisleIndexDO::getAisleName, reqVO.getName())
                .eqIfPresent(AisleIndexDO::getPduBar, reqVO.getPduBar())
//                .eqIfPresent(AisleIndexDO::getIsDelete, reqVO.getIsDelete())
                .betweenIfPresent(AisleIndexDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(AisleIndexDO::getAisleLength, reqVO.getLength())
                .eq(AisleIndexDO::getIsDelete, 0)
                .orderByAsc(AisleIndexDO::getId));
    }

    Page<AisleIndexDelResVO> selectDelPageQuery(@Param("page") Page page, @Param("pageReqVO") AisleIndexPageReqVO pageReqVO);

    Integer selectxOrY(AisleIndexDO aisleIndexDO);
}