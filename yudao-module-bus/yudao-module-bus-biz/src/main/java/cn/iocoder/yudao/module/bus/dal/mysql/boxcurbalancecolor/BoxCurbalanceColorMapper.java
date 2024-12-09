package cn.iocoder.yudao.module.bus.dal.mysql.boxcurbalancecolor;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxcurbalancecolor.BoxCurbalanceColorDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.bus.controller.admin.boxcurbalancecolor.vo.*;

/**
 * 插接箱不平衡度颜色 Mapper
 *
 * @author clever
 */
@Mapper
public interface BoxCurbalanceColorMapper extends BaseMapperX<BoxCurbalanceColorDO> {

    default PageResult<BoxCurbalanceColorDO> selectPage(BoxCurbalanceColorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BoxCurbalanceColorDO>()
                .eqIfPresent(BoxCurbalanceColorDO::getRangeOne, reqVO.getRangeOne())
                .eqIfPresent(BoxCurbalanceColorDO::getRangeTwo, reqVO.getRangeTwo())
                .eqIfPresent(BoxCurbalanceColorDO::getRangeThree, reqVO.getRangeThree())
                .eqIfPresent(BoxCurbalanceColorDO::getRangeFour, reqVO.getRangeFour())
                .betweenIfPresent(BoxCurbalanceColorDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(BoxCurbalanceColorDO::getId));
    }

}