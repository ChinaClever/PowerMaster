package cn.iocoder.yudao.module.pdu.service.pdudevice;

import javax.validation.*;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDeviceSaveReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * PDU设备 Service 接口
 *
 * @author 芋道源码
 */
public interface PDUDeviceService {

    /**
     * 创建PDU设备
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPDUDevice(@Valid PDUDeviceSaveReqVO createReqVO);

    /**
     * 更新PDU设备
     *
     * @param updateReqVO 更新信息
     */
    void updatePDUDevice(@Valid PDUDeviceSaveReqVO updateReqVO);

    /**
     * 删除PDU设备
     *
     * @param id 编号
     */
    void deletePDUDevice(Long id);

    /**
     * 获得PDU设备
     *
     * @param id 编号
     * @return PDU设备
     */
    PDUDeviceDO getPDUDevice(Long id);

    /**
     * 获得PDU设备分页
     *
     * @param pageReqVO 分页查询
     * @return PDU设备分页
     */
    PageResult<PDUDeviceDO> getPDUDevicePage(PDUDevicePageReqVO pageReqVO);

}