package cn.iocoder.yudao.module.bus.service.busindex;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.bus.controller.admin.busindex.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.busindex.BusIndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 始端箱索引 Service 接口
 *
 * @author clever
 */
public interface BusIndexService {

    /**
     * 创建始端箱索引
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createIndex(@Valid BusIndexSaveReqVO createReqVO);

    /**
     * 更新始端箱索引
     *
     * @param updateReqVO 更新信息
     */
    void updateIndex(@Valid BusIndexSaveReqVO updateReqVO);

    /**
     * 删除始端箱索引
     *
     * @param id 编号
     */
    void deleteIndex(Long id);

    /**
     * 获得始端箱索引
     *
     * @param id 编号
     * @return 始端箱索引
     */
    BusIndexDO getIndex(Long id);

    /**
     * 获得始端箱索引分页
     *
     * @param pageReqVO 分页查询
     * @return 始端箱索引分页
     */
    PageResult<BusIndexRes> getIndexPage(BusIndexPageReqVO pageReqVO);

}