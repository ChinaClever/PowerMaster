package cn.iocoder.yudao.module.bus.dal.mysql.buscurbalancecolor;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bus.dal.dataobject.buscurbalancecolor.BusCurbalanceColorDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.bus.controller.admin.buscurbalancecolor.vo.*;

/**
 * 母线不平衡度颜色 Mapper
 *
 * @author clever
 */
@Mapper
public interface BusCurbalanceColorMapper extends BaseMapperX<BusCurbalanceColorDO> {

    default PageResult<BusCurbalanceColorDO> selectPage(BusCurbalanceColorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BusCurbalanceColorDO>()
                .eqIfPresent(BusCurbalanceColorDO::getRangeOne, reqVO.getRangeOne())
                .eqIfPresent(BusCurbalanceColorDO::getRangeTwo, reqVO.getRangeTwo())
                .eqIfPresent(BusCurbalanceColorDO::getRangeThree, reqVO.getRangeThree())
                .eqIfPresent(BusCurbalanceColorDO::getRangeFour, reqVO.getRangeFour())
                .betweenIfPresent(BusCurbalanceColorDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(BusCurbalanceColorDO::getId));
    }

}