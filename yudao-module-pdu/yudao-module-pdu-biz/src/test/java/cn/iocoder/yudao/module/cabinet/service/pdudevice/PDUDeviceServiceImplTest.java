package cn.iocoder.yudao.module.cabinet.service.pdudevice;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDeviceSaveReqVO;
import cn.iocoder.yudao.module.pdu.service.pdudevice.PDUDeviceServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.pdudevice.PduIndexMapper;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.pdu.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link PDUDeviceServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PDUDeviceServiceImpl.class)
public class PDUDeviceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PDUDeviceServiceImpl pDUDeviceService;

    @Resource
    private PduIndexMapper pDUDeviceMapper;

    @Test
    public void testCreatePDUDevice_success() {
        // 准备参数
        PDUDeviceSaveReqVO createReqVO = randomPojo(PDUDeviceSaveReqVO.class).setId(null);

        // 调用
        Long pDUDeviceId = pDUDeviceService.createPDUDevice(createReqVO);
        // 断言
        assertNotNull(pDUDeviceId);
        // 校验记录的属性是否正确
        PDUDeviceDO pDUDevice = pDUDeviceMapper.selectById(pDUDeviceId);
        assertPojoEquals(createReqVO, pDUDevice, "id");
    }

    @Test
    public void testUpdatePDUDevice_success() {
        // mock 数据
        PDUDeviceDO dbPDUDevice = randomPojo(PDUDeviceDO.class);
        pDUDeviceMapper.insert(dbPDUDevice);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PDUDeviceSaveReqVO updateReqVO = randomPojo(PDUDeviceSaveReqVO.class, o -> {
            o.setId(dbPDUDevice.getId()); // 设置更新的 ID
        });

        // 调用
        pDUDeviceService.updatePDUDevice(updateReqVO);
        // 校验是否更新正确
        PDUDeviceDO pDUDevice = pDUDeviceMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, pDUDevice);
    }

    @Test
    public void testUpdatePDUDevice_notExists() {
        // 准备参数
        PDUDeviceSaveReqVO updateReqVO = randomPojo(PDUDeviceSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> pDUDeviceService.updatePDUDevice(updateReqVO), PDU_DEVICE_NOT_EXISTS);
    }

    @Test
    public void testDeletePDUDevice_success() {
        // mock 数据
        PDUDeviceDO dbPDUDevice = randomPojo(PDUDeviceDO.class);
        pDUDeviceMapper.insert(dbPDUDevice);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbPDUDevice.getId();

        // 调用
        pDUDeviceService.deletePDUDevice(id);
       // 校验数据不存在了
       assertNull(pDUDeviceMapper.selectById(id));
    }

    @Test
    public void testDeletePDUDevice_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> pDUDeviceService.deletePDUDevice(id), PDU_DEVICE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPDUDevicePage() {

    }

}