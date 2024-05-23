package cn.iocoder.yudao.module.pdu.dal.mysql.curbalancecolor;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.CurbalanceColorDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.pdu.controller.admin.curbalancecolor.vo.*;

/**
 * PDU不平衡度颜色 Mapper
 *
 * @author clever
 */
@Mapper
public interface CurbalanceColorMapper extends BaseMapperX<CurbalanceColorDO> {

    default PageResult<CurbalanceColorDO> selectPage(CurbalanceColorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CurbalanceColorDO>()
                .eqIfPresent(CurbalanceColorDO::getRangeOne, reqVO.getRangeOne())
                .eqIfPresent(CurbalanceColorDO::getRangeTwo, reqVO.getRangeTwo())
                .eqIfPresent(CurbalanceColorDO::getRangeThree, reqVO.getRangeThree())
                .eqIfPresent(CurbalanceColorDO::getRangeFour, reqVO.getRangeFour())
                .betweenIfPresent(CurbalanceColorDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CurbalanceColorDO::getId));
    }

}