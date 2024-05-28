package cn.iocoder.yudao.module.pdu.dal.mysql.curbalancecolor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.PDUCurbalanceColorDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.pdu.controller.admin.curbalancecolor.vo.*;

/**
 * PDU不平衡度颜色 Mapper
 *
 * @author clever
 */
@Mapper
public interface PDUCurbalanceColorMapper extends BaseMapperX<PDUCurbalanceColorDO> {

    default PageResult<PDUCurbalanceColorDO> selectPage(CurbalanceColorPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PDUCurbalanceColorDO>()
                .eqIfPresent(PDUCurbalanceColorDO::getRangeOne, reqVO.getRangeOne())
                .eqIfPresent(PDUCurbalanceColorDO::getRangeTwo, reqVO.getRangeTwo())
                .eqIfPresent(PDUCurbalanceColorDO::getRangeThree, reqVO.getRangeThree())
                .eqIfPresent(PDUCurbalanceColorDO::getRangeFour, reqVO.getRangeFour())
                .betweenIfPresent(PDUCurbalanceColorDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PDUCurbalanceColorDO::getId));
    }

}