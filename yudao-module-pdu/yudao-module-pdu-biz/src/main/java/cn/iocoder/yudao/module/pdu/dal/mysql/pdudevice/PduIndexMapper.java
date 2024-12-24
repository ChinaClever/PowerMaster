package cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.BalancedDistributionStatisticsVO;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PduDeviceCountResVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * PDU设备 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface PduIndexMapper extends BaseMapperX<PduIndex> {


    PduDeviceCountResVO getPDUDeviceCount();

    BalancedDistributionStatisticsVO getBalancedDistribution();

    Page<PduIndex> selectQuery(@Param("page") Page<PduIndex> page, @Param("pduVo") PDUDevicePageReqVO pduVo);
}