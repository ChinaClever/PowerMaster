package cn.iocoder.yudao.module.pdu.dal.mysql.historydata;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.HistoryDataDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.*;

/**
 * pdu历史数据 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface HistoryDataMapper extends BaseMapperX<HistoryDataDO> {

    default PageResult<HistoryDataDO> selectPage(HistoryDataPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HistoryDataDO>()
                .eqIfPresent(HistoryDataDO::getId, reqVO.getId())
                .eqIfPresent(HistoryDataDO::getPduId, reqVO.getPduId())
                .eqIfPresent(HistoryDataDO::getType, reqVO.getType())
                .eqIfPresent(HistoryDataDO::getTopic, reqVO.getTopic())
                .eqIfPresent(HistoryDataDO::getIndexes, reqVO.getIndexes())
                .eqIfPresent(HistoryDataDO::getValue, reqVO.getValue())
                .betweenIfPresent(HistoryDataDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HistoryDataDO::getId));
    }

}