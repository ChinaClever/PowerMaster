package cn.iocoder.yudao.module.pdu.dal.mysql.eqbillconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pdu.dal.dataobject.eqbillconfig.EqBillConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.pdu.controller.admin.eqbillconfig.vo.*;

/**
 * pdu电量计费方式配置 Mapper
 *
 * @author clever
 */
@Mapper
public interface EqBillConfigMapper extends BaseMapperX<EqBillConfigDO> {

    default PageResult<EqBillConfigDO> selectPage(EqBillConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EqBillConfigDO>()
                .eqIfPresent(EqBillConfigDO::getBill, reqVO.getBill())
                .eqIfPresent(EqBillConfigDO::getBillMode, reqVO.getBillMode())
                .likeIfPresent(EqBillConfigDO::getBillPeriod, reqVO.getBillPeriod())
                .betweenIfPresent(EqBillConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EqBillConfigDO::getId));
    }

}