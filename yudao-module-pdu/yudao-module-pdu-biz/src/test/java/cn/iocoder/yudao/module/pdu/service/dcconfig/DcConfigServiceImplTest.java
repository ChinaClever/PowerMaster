package cn.iocoder.yudao.module.pdu.service.dcconfig;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.pdu.controller.admin.dcconfig.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.dcconfig.DcConfigDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.dcconfig.DcConfigMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.pdu.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link DcConfigServiceImpl} 的单元测试类
 *
 * @author clever
 */
@Import(DcConfigServiceImpl.class)
public class DcConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DcConfigServiceImpl dcConfigService;

    @Resource
    private DcConfigMapper dcConfigMapper;

    @Test
    public void testCreateDcConfig_success() {
        // 准备参数
        DcConfigSaveReqVO createReqVO = randomPojo(DcConfigSaveReqVO.class).setId(null);

        // 调用
        Short dcConfigId = dcConfigService.createDcConfig(createReqVO);
        // 断言
        assertNotNull(dcConfigId);
        // 校验记录的属性是否正确
        DcConfigDO dcConfig = dcConfigMapper.selectById(dcConfigId);
        assertPojoEquals(createReqVO, dcConfig, "id");
    }

    @Test
    public void testUpdateDcConfig_success() {
        // mock 数据
        DcConfigDO dbDcConfig = randomPojo(DcConfigDO.class);
        dcConfigMapper.insert(dbDcConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DcConfigSaveReqVO updateReqVO = randomPojo(DcConfigSaveReqVO.class, o -> {
            o.setId(dbDcConfig.getId()); // 设置更新的 ID
        });

        // 调用
        dcConfigService.updateDcConfig(updateReqVO);
        // 校验是否更新正确
        DcConfigDO dcConfig = dcConfigMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, dcConfig);
    }

    @Test
    public void testUpdateDcConfig_notExists() {
        // 准备参数
        DcConfigSaveReqVO updateReqVO = randomPojo(DcConfigSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> dcConfigService.updateDcConfig(updateReqVO), DC_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteDcConfig_success() {
        // mock 数据
        DcConfigDO dbDcConfig = randomPojo(DcConfigDO.class);
        dcConfigMapper.insert(dbDcConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Short id = dbDcConfig.getId();

        // 调用
        dcConfigService.deleteDcConfig(id);
       // 校验数据不存在了
       assertNull(dcConfigMapper.selectById(id));
    }

    @Test
    public void testDeleteDcConfig_notExists() {
        // 准备参数
        Short id = randomShortId();

        // 调用, 并断言异常
        assertServiceException(() -> dcConfigService.deleteDcConfig(id), DC_CONFIG_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetDcConfigPage() {
       // mock 数据
       DcConfigDO dbDcConfig = randomPojo(DcConfigDO.class, o -> { // 等会查询到
           o.setReceivePort(null);
           o.setFixStoreCron(null);
           o.setChangeStoreCron(null);
           o.setEleStoreCron(null);
           o.setPowLimitRate(null);
           o.setRedisExpire(null);
           o.setOffLineDuration(null);
           o.setOffLineAlarm(null);
           o.setStatusAlarm(null);
           o.setTimingPushCron(null);
           o.setChangePushCron(null);
           o.setAlarmPushCron(null);
           o.setTimingPush(null);
           o.setChangePush(null);
           o.setAlarmPush(null);
           o.setCreateTime(null);
           o.setPushMqs(null);
           o.setFixStore(null);
           o.setChangeStore(null);
           o.setEleStore(null);
           o.setRedisSwitch(null);
       });
       dcConfigMapper.insert(dbDcConfig);
       // 测试 receivePort 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setReceivePort(null)));
       // 测试 fixStoreCron 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setFixStoreCron(null)));
       // 测试 changeStoreCron 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setChangeStoreCron(null)));
       // 测试 eleStoreCron 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setEleStoreCron(null)));
       // 测试 powLimitRate 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setPowLimitRate(null)));
       // 测试 redisExpire 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setRedisExpire(null)));
       // 测试 offLineDuration 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setOffLineDuration(null)));
       // 测试 offLineAlarm 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setOffLineAlarm(null)));
       // 测试 statusAlarm 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setStatusAlarm(null)));
       // 测试 timingPushCron 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setTimingPushCron(null)));
       // 测试 changePushCron 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setChangePushCron(null)));
       // 测试 alarmPushCron 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setAlarmPushCron(null)));
       // 测试 timingPush 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setTimingPush(null)));
       // 测试 changePush 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setChangePush(null)));
       // 测试 alarmPush 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setAlarmPush(null)));
       // 测试 createTime 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setCreateTime(null)));
       // 测试 pushMqs 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setPushMqs(null)));
       // 测试 fixStore 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setFixStore(null)));
       // 测试 changeStore 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setChangeStore(null)));
       // 测试 eleStore 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setEleStore(null)));
       // 测试 redisSwitch 不匹配
       dcConfigMapper.insert(cloneIgnoreId(dbDcConfig, o -> o.setRedisSwitch(null)));
       // 准备参数
       DcConfigPageReqVO reqVO = new DcConfigPageReqVO();
       reqVO.setReceivePort(null);
       reqVO.setFixStoreCron(null);
       reqVO.setChangeStoreCron(null);
       reqVO.setEleStoreCron(null);
       reqVO.setPowLimitRate(null);
       reqVO.setRedisExpire(null);
       reqVO.setOffLineDuration(null);
       reqVO.setOffLineAlarm(null);
       reqVO.setStatusAlarm(null);
       reqVO.setTimingPushCron(null);
       reqVO.setChangePushCron(null);
       reqVO.setAlarmPushCron(null);
       reqVO.setTimingPush(null);
       reqVO.setChangePush(null);
       reqVO.setAlarmPush(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPushMqs(null);
       reqVO.setFixStore(null);
       reqVO.setChangeStore(null);
       reqVO.setEleStore(null);
       reqVO.setRedisSwitch(null);

       // 调用
       PageResult<DcConfigDO> pageResult = dcConfigService.getDcConfigPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbDcConfig, pageResult.getList().get(0));
    }

}