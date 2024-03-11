package cn.iocoder.yudao.module.pdu.service.historydata;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.HistoryDataDO;
import cn.iocoder.yudao.module.pdu.dal.mysql.historydata.HistoryDataMapper;
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
 * {@link HistoryDataServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HistoryDataServiceImpl.class)
public class HistoryDataServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HistoryDataServiceImpl historyDataService;

    @Resource
    private HistoryDataMapper historyDataMapper;

    @Test
    public void testCreateHistoryData_success() {
        // 准备参数
        HistoryDataSaveReqVO createReqVO = randomPojo(HistoryDataSaveReqVO.class).setId(null);

        // 调用
        Long historyDataId = historyDataService.createHistoryData(createReqVO);
        // 断言
        assertNotNull(historyDataId);
        // 校验记录的属性是否正确
        HistoryDataDO historyData = historyDataMapper.selectById(historyDataId);
        assertPojoEquals(createReqVO, historyData, "id");
    }

    @Test
    public void testUpdateHistoryData_success() {
        // mock 数据
        HistoryDataDO dbHistoryData = randomPojo(HistoryDataDO.class);
        historyDataMapper.insert(dbHistoryData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        HistoryDataSaveReqVO updateReqVO = randomPojo(HistoryDataSaveReqVO.class, o -> {
            o.setId(dbHistoryData.getId()); // 设置更新的 ID
        });

        // 调用
        historyDataService.updateHistoryData(updateReqVO);
        // 校验是否更新正确
        HistoryDataDO historyData = historyDataMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, historyData);
    }

    @Test
    public void testUpdateHistoryData_notExists() {
        // 准备参数
        HistoryDataSaveReqVO updateReqVO = randomPojo(HistoryDataSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> historyDataService.updateHistoryData(updateReqVO), HISTORY_DATA_NOT_EXISTS);
    }

    @Test
    public void testDeleteHistoryData_success() {
        // mock 数据
        HistoryDataDO dbHistoryData = randomPojo(HistoryDataDO.class);
        historyDataMapper.insert(dbHistoryData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbHistoryData.getId();

        // 调用
        historyDataService.deleteHistoryData(id);
       // 校验数据不存在了
       assertNull(historyDataMapper.selectById(id));
    }

    @Test
    public void testDeleteHistoryData_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> historyDataService.deleteHistoryData(id), HISTORY_DATA_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHistoryDataPage() {
       // mock 数据
       HistoryDataDO dbHistoryData = randomPojo(HistoryDataDO.class, o -> { // 等会查询到
           o.setId(null);
           o.setPduId(null);
           o.setType(null);
           o.setTopic(null);
           o.setIndexes(null);
           o.setValue(null);
           o.setCreateTime(null);
       });
       historyDataMapper.insert(dbHistoryData);
       // 测试 id 不匹配
       historyDataMapper.insert(cloneIgnoreId(dbHistoryData, o -> o.setId(null)));
       // 测试 pduId 不匹配
       historyDataMapper.insert(cloneIgnoreId(dbHistoryData, o -> o.setPduId(null)));
       // 测试 type 不匹配
       historyDataMapper.insert(cloneIgnoreId(dbHistoryData, o -> o.setType(null)));
       // 测试 topic 不匹配
       historyDataMapper.insert(cloneIgnoreId(dbHistoryData, o -> o.setTopic(null)));
       // 测试 indexes 不匹配
       historyDataMapper.insert(cloneIgnoreId(dbHistoryData, o -> o.setIndexes(null)));
       // 测试 value 不匹配
       historyDataMapper.insert(cloneIgnoreId(dbHistoryData, o -> o.setValue(null)));
       // 测试 createTime 不匹配
       historyDataMapper.insert(cloneIgnoreId(dbHistoryData, o -> o.setCreateTime(null)));
       // 准备参数
       HistoryDataPageReqVO reqVO = new HistoryDataPageReqVO();
       reqVO.setId(null);
       reqVO.setPduId(null);
       reqVO.setType(null);
       reqVO.setTopic(null);
       reqVO.setIndexes(null);
       reqVO.setValue(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<HistoryDataDO> pageResult = historyDataService.getHistoryDataPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbHistoryData, pageResult.getList().get(0));
    }

}