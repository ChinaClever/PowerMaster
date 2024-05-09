package cn.iocoder.yudao.module.cabinet.service.statisconfig;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.cabinet.controller.admin.statisconfig.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.statisconfig.StatisConfigDO;
import cn.iocoder.yudao.module.cabinet.dal.mysql.statisconfig.StatisConfigMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.cabinet.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link StatisConfigServiceImpl} 的单元测试类
 *
 * @author clever
 */
@Import(StatisConfigServiceImpl.class)
public class StatisConfigServiceImplTest extends BaseDbUnitTest {

    @Resource
    private StatisConfigServiceImpl statisConfigService;

    @Resource
    private StatisConfigMapper statisConfigMapper;

    @Test
    public void testCreateStatisConfig_success() {
        // 准备参数
        StatisConfigSaveReqVO createReqVO = randomPojo(StatisConfigSaveReqVO.class).setId(null);

        // 调用
        Integer statisConfigId = statisConfigService.createStatisConfig(createReqVO);
        // 断言
        assertNotNull(statisConfigId);
        // 校验记录的属性是否正确
        StatisConfigDO statisConfig = statisConfigMapper.selectById(statisConfigId);
        assertPojoEquals(createReqVO, statisConfig, "id");
    }

    @Test
    public void testUpdateStatisConfig_success() {
        // mock 数据
        StatisConfigDO dbStatisConfig = randomPojo(StatisConfigDO.class);
        statisConfigMapper.insert(dbStatisConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        StatisConfigSaveReqVO updateReqVO = randomPojo(StatisConfigSaveReqVO.class, o -> {
            o.setId(dbStatisConfig.getId()); // 设置更新的 ID
        });

        // 调用
        statisConfigService.updateStatisConfig(updateReqVO);
        // 校验是否更新正确
        StatisConfigDO statisConfig = statisConfigMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, statisConfig);
    }

    @Test
    public void testUpdateStatisConfig_notExists() {
        // 准备参数
        StatisConfigSaveReqVO updateReqVO = randomPojo(StatisConfigSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> statisConfigService.updateStatisConfig(updateReqVO), STATIS_CONFIG_NOT_EXISTS);
    }

    @Test
    public void testDeleteStatisConfig_success() {
        // mock 数据
        StatisConfigDO dbStatisConfig = randomPojo(StatisConfigDO.class);
        statisConfigMapper.insert(dbStatisConfig);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbStatisConfig.getId();

        // 调用
        statisConfigService.deleteStatisConfig(id);
       // 校验数据不存在了
       assertNull(statisConfigMapper.selectById(id));
    }

    @Test
    public void testDeleteStatisConfig_notExists() {
        // 准备参数
        Integer id = randomIntegerId();

        // 调用, 并断言异常
        assertServiceException(() -> statisConfigService.deleteStatisConfig(id), STATIS_CONFIG_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetStatisConfigPage() {
       // mock 数据
       StatisConfigDO dbStatisConfig = randomPojo(StatisConfigDO.class, o -> { // 等会查询到
           o.setBillMode(null);
           o.setDayCron(null);
           o.setHourCron(null);
           o.setEqDayCron(null);
           o.setEqWeekCron(null);
           o.setEqMonthCron(null);
           o.setLoadLimit(null);
           o.setStatusAlarm(null);
           o.setStoreCron(null);
           o.setAlarmCron(null);
           o.setAlarmPush(null);
           o.setAlarmPushCron(null);
           o.setPushMqs(null);
           o.setRedisExpire(null);
           o.setEleStoreCron(null);
           o.setTimingPushCron(null);
           o.setTimingPush(null);
           o.setCreateTime(null);
           o.setRedisCron(null);
       });
       statisConfigMapper.insert(dbStatisConfig);
       // 测试 billMode 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setBillMode(null)));
       // 测试 dayCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setDayCron(null)));
       // 测试 hourCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setHourCron(null)));
       // 测试 eqDayCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setEqDayCron(null)));
       // 测试 eqWeekCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setEqWeekCron(null)));
       // 测试 eqMonthCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setEqMonthCron(null)));
       // 测试 loadLimit 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setLoadLimit(null)));
       // 测试 statusAlarm 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setStatusAlarm(null)));
       // 测试 storeCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setStoreCron(null)));
       // 测试 alarmCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setAlarmCron(null)));
       // 测试 alarmPush 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setAlarmPush(null)));
       // 测试 alarmPushCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setAlarmPushCron(null)));
       // 测试 pushMqs 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setPushMqs(null)));
       // 测试 redisExpire 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setRedisExpire(null)));
       // 测试 eleStoreCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setEleStoreCron(null)));
       // 测试 timingPushCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setTimingPushCron(null)));
       // 测试 timingPush 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setTimingPush(null)));
       // 测试 createTime 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setCreateTime(null)));
       // 测试 redisCron 不匹配
       statisConfigMapper.insert(cloneIgnoreId(dbStatisConfig, o -> o.setRedisCron(null)));
       // 准备参数
       StatisConfigPageReqVO reqVO = new StatisConfigPageReqVO();
       reqVO.setBillMode(null);
       reqVO.setDayCron(null);
       reqVO.setHourCron(null);
       reqVO.setEqDayCron(null);
       reqVO.setEqWeekCron(null);
       reqVO.setEqMonthCron(null);
       reqVO.setLoadLimit(null);
       reqVO.setStatusAlarm(null);
       reqVO.setStoreCron(null);
       reqVO.setAlarmCron(null);
       reqVO.setAlarmPush(null);
       reqVO.setAlarmPushCron(null);
       reqVO.setPushMqs(null);
       reqVO.setRedisExpire(null);
       reqVO.setEleStoreCron(null);
       reqVO.setTimingPushCron(null);
       reqVO.setTimingPush(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRedisCron(null);

       // 调用
       PageResult<StatisConfigDO> pageResult = statisConfigService.getStatisConfigPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbStatisConfig, pageResult.getList().get(0));
    }

}