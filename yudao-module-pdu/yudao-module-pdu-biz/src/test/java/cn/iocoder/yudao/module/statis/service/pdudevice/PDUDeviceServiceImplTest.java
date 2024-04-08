package cn.iocoder.yudao.module.statis.service.pdudevice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.statis.controller.admin.pdudevice.vo.*;
import cn.iocoder.yudao.module.statis.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.statis.dal.mysql.pdudevice.PDUDeviceMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;

import static cn.iocoder.yudao.module.statis.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
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
    private PDUDeviceMapper pDUDeviceMapper;

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
       // mock 数据
       PDUDeviceDO dbPDUDevice = randomPojo(PDUDeviceDO.class, o -> { // 等会查询到
           o.setDevKey(null);
           o.setIpAddr(null);
           o.setCreateTime(null);
           o.setCascadeNum(null);
       });
       pDUDeviceMapper.insert(dbPDUDevice);
       // 测试 devKey 不匹配
       pDUDeviceMapper.insert(cloneIgnoreId(dbPDUDevice, o -> o.setDevKey(null)));
       // 测试 ipAddr 不匹配
       pDUDeviceMapper.insert(cloneIgnoreId(dbPDUDevice, o -> o.setIpAddr(null)));
       // 测试 createTime 不匹配
       pDUDeviceMapper.insert(cloneIgnoreId(dbPDUDevice, o -> o.setCreateTime(null)));
       // 测试 cascadeNum 不匹配
       pDUDeviceMapper.insert(cloneIgnoreId(dbPDUDevice, o -> o.setCascadeNum(null)));
       // 准备参数
       PDUDevicePageReqVO reqVO = new PDUDevicePageReqVO();
       reqVO.setDevKey(null);
       reqVO.setIpAddr(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCascadeNum(null);

       // 调用
       PageResult<PDUDeviceDO> pageResult = pDUDeviceService.getPDUDevicePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbPDUDevice, pageResult.getList().get(0));
    }

}