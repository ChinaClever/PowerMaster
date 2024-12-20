package cn.iocoder.yudao.module.bus.dal.mysql.boxharmoniccolor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor.vo.BoxHarmonicColorPageReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxharmoniccolor.BoxHarmonicColorDO;
import org.apache.ibatis.annotations.Mapper;

public interface BoxHarmonicColorMapper extends BaseMapperX<BoxHarmonicColorDO> {

    default PageResult<BoxHarmonicColorDO> selectPage(BoxHarmonicColorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BoxHarmonicColorDO>()
                .eqIfPresent(BoxHarmonicColorDO::getRangeOne, reqVO.getRangeOne())
                .eqIfPresent(BoxHarmonicColorDO::getRangeTwo, reqVO.getRangeTwo())
                .eqIfPresent(BoxHarmonicColorDO::getRangeThree, reqVO.getRangeThree())
                .eqIfPresent(BoxHarmonicColorDO::getRangeFour, reqVO.getRangeFour())
                .betweenIfPresent(BoxHarmonicColorDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(BoxHarmonicColorDO::getId));
    }
}
