package cn.iocoder.yudao.module.statis.dal.mysql.pdudevice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.statis.dal.dataobject.pdudevice.PDUDeviceDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.statis.controller.admin.pdudevice.vo.*;

/**
 * PDU设备 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface PDUDeviceMapper extends BaseMapperX<PDUDeviceDO> {

    default PageResult<PDUDeviceDO> selectPage(PDUDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PDUDeviceDO>()
                .eqIfPresent(PDUDeviceDO::getDevKey, reqVO.getDevKey())
                .eqIfPresent(PDUDeviceDO::getIpAddr, reqVO.getIpAddr())
                .betweenIfPresent(PDUDeviceDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(PDUDeviceDO::getCascadeNum, reqVO.getCascadeNum())
                .orderByDesc(PDUDeviceDO::getId));
    }

}