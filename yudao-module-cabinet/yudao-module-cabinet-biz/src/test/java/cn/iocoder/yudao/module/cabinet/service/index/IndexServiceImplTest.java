package cn.iocoder.yudao.module.cabinet.service.index;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.module.cabinet.dal.mysql.index.IndexMapper;
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
 * {@link IndexServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(IndexServiceImpl.class)
public class IndexServiceImplTest extends BaseDbUnitTest {

    @Resource
    private IndexServiceImpl indexService;

    @Resource
    private IndexMapper indexMapper;

    @Test
    public void testCreateIndex_success() {
        // 准备参数
        IndexSaveReqVO createReqVO = randomPojo(IndexSaveReqVO.class).setId(null);

        // 调用
        Integer indexId = indexService.createIndex(createReqVO);
        // 断言
        assertNotNull(indexId);
        // 校验记录的属性是否正确
        IndexDO index = indexMapper.selectById(indexId);
        assertPojoEquals(createReqVO, index, "id");
    }

    @Test
    public void testUpdateIndex_success() {
        // mock 数据
        IndexDO dbIndex = randomPojo(IndexDO.class);
        indexMapper.insert(dbIndex);// @Sql: 先插入出一条存在的数据
        // 准备参数
        IndexSaveReqVO updateReqVO = randomPojo(IndexSaveReqVO.class, o -> {
            o.setId(dbIndex.getId()); // 设置更新的 ID
        });

        // 调用
        indexService.updateIndex(updateReqVO);
        // 校验是否更新正确
        IndexDO index = indexMapper.selectById(updateReqVO.getId()); // 获取最新的
        assertPojoEquals(updateReqVO, index);
    }

    @Test
    public void testUpdateIndex_notExists() {
        // 准备参数
        IndexSaveReqVO updateReqVO = randomPojo(IndexSaveReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> indexService.updateIndex(updateReqVO), INDEX_NOT_EXISTS);
    }

    @Test
    public void testDeleteIndex_success() {
        // mock 数据
        IndexDO dbIndex = randomPojo(IndexDO.class);
        indexMapper.insert(dbIndex);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbIndex.getId();

        // 调用
        indexService.deleteIndex(id);
       // 校验数据不存在了
       assertNull(indexMapper.selectById(id));
    }

    @Test
    public void testDeleteIndex_notExists() {
        // 准备参数
        Integer id = randomIntegerId();

        // 调用, 并断言异常
        assertServiceException(() -> indexService.deleteIndex(id), INDEX_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetIndexPage() {
       // mock 数据
       IndexDO dbIndex = randomPojo(IndexDO.class, o -> { // 等会查询到
           o.setRoomId(null);
           o.setName(null);
           o.setAisleId(null);
           o.setPowCapacity(null);
           o.setPduBox(null);
           o.setIsDisabled(null);
           o.setIsDeleted(null);
           o.setCreateTime(null);
           o.setRunStatus(null);
       });
       indexMapper.insert(dbIndex);
       // 测试 roomId 不匹配
       indexMapper.insert(cloneIgnoreId(dbIndex, o -> o.setRoomId(null)));
       // 测试 name 不匹配
       indexMapper.insert(cloneIgnoreId(dbIndex, o -> o.setName(null)));
       // 测试 aisleId 不匹配
       indexMapper.insert(cloneIgnoreId(dbIndex, o -> o.setAisleId(null)));
       // 测试 powCapacity 不匹配
       indexMapper.insert(cloneIgnoreId(dbIndex, o -> o.setPowCapacity(null)));
       // 测试 pduBox 不匹配
       indexMapper.insert(cloneIgnoreId(dbIndex, o -> o.setPduBox(null)));
       // 测试 isDisabled 不匹配
       indexMapper.insert(cloneIgnoreId(dbIndex, o -> o.setIsDisabled(null)));
       // 测试 isDeleted 不匹配
       indexMapper.insert(cloneIgnoreId(dbIndex, o -> o.setIsDeleted(null)));
       // 测试 createTime 不匹配
       indexMapper.insert(cloneIgnoreId(dbIndex, o -> o.setCreateTime(null)));
       // 测试 runStatus 不匹配
       indexMapper.insert(cloneIgnoreId(dbIndex, o -> o.setRunStatus(null)));
       // 准备参数
       IndexPageReqVO reqVO = new IndexPageReqVO();
       reqVO.setRoomId(null);
       reqVO.setName(null);
       reqVO.setAisleId(null);
       reqVO.setPowCapacity(null);
       reqVO.setPduBox(null);
       reqVO.setIsDisabled(null);
       reqVO.setIsDeleted(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRunStatus(null);

       // 调用
       PageResult<IndexDO> pageResult = indexService.getIndexPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbIndex, pageResult.getList().get(0));
    }

}