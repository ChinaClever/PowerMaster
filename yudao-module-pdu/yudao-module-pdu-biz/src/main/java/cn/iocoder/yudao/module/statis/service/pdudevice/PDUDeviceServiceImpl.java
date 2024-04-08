package cn.iocoder.yudao.module.statis.service.pdudevice;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import cn.iocoder.yudao.module.statis.controller.admin.pdudevice.vo.*;
import cn.iocoder.yudao.module.statis.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import cn.iocoder.yudao.module.statis.dal.mysql.pdudevice.PDUDeviceMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.statis.enums.ErrorCodeConstants.*;

/**
 * PDU设备 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PDUDeviceServiceImpl implements PDUDeviceService {

    @Resource
    private PDUDeviceMapper pDUDeviceMapper;

    @Override
    public Long createPDUDevice(PDUDeviceSaveReqVO createReqVO) {
        // 插入
        PDUDeviceDO pDUDevice = BeanUtils.toBean(createReqVO, PDUDeviceDO.class);
        pDUDeviceMapper.insert(pDUDevice);
        // 返回
        return pDUDevice.getId();
    }

    @Override
    public void updatePDUDevice(PDUDeviceSaveReqVO updateReqVO) {
        // 校验存在
        validatePDUDeviceExists(updateReqVO.getId());
        // 更新
        PDUDeviceDO updateObj = BeanUtils.toBean(updateReqVO, PDUDeviceDO.class);
        pDUDeviceMapper.updateById(updateObj);
    }

    @Override
    public void deletePDUDevice(Long id) {
        // 校验存在
        validatePDUDeviceExists(id);
        // 删除
        pDUDeviceMapper.deleteById(id);
    }

    private void validatePDUDeviceExists(Long id) {
        if (pDUDeviceMapper.selectById(id) == null) {
            throw exception(PDU_DEVICE_NOT_EXISTS);
        }
    }

    @Override
    public PDUDeviceDO getPDUDevice(Long id) {
        return pDUDeviceMapper.selectById(id);
    }

    @Override
    public PageResult<PDUDeviceDO> getPDUDevicePage(PDUDevicePageReqVO pageReqVO) {
        return pDUDeviceMapper.selectPage(pageReqVO);
    }

}