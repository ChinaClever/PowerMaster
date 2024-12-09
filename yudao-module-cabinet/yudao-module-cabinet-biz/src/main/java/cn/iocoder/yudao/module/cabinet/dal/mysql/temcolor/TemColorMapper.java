package cn.iocoder.yudao.module.cabinet.dal.mysql.temcolor;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.temcolor.TemColorDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.cabinet.controller.admin.temcolor.vo.*;

/**
 * 机柜温度颜色 Mapper
 *
 * @author clever
 */
@Mapper
public interface TemColorMapper extends BaseMapperX<TemColorDO> {

    default PageResult<TemColorDO> selectPage(TemColorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<TemColorDO>()
                .eqIfPresent(TemColorDO::getMin, reqVO.getMin())
                .eqIfPresent(TemColorDO::getMax, reqVO.getMax())
                .eqIfPresent(TemColorDO::getColor, reqVO.getColor())
                .betweenIfPresent(TemColorDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(TemColorDO::getId));
    }

}