package cn.iocoder.yudao.module.bus.service.boxindex;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.dto.BoxIndexDTO;
import cn.iocoder.yudao.module.bus.controller.admin.boxindex.vo.*;
import cn.iocoder.yudao.framework.common.entity.mysql.bus.BoxIndex;

import javax.validation.Valid;

/**
 * 始端箱索引 Service 接口
 *
 * @author clever
 */
public interface BoxIndexService {

    /**
     * 创建始端箱索引
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createIndex(@Valid BoxIndexSaveReqVO createReqVO);

    /**
     * 更新始端箱索引
     *
     * @param updateReqVO 更新信息
     */
    void updateIndex(@Valid BoxIndexSaveReqVO updateReqVO);

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
    BoxIndex getIndex(Long id);

    /**
     * 获得始端箱索引分页
     *
     * @param pageReqVO 分页查询
     * @return 始端箱索引分页
     */
    PageResult<BoxIndexRes> getIndexPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxRedisDataRes> getBoxRedisPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxIndexDTO> getEqPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxBalanceDataRes> getBoxBalancePage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxTemRes> getBoxTemPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxPFRes> getBoxPFPage(BoxIndexPageReqVO pageReqVO);

    PageResult<BoxHarmonicRes> getBoxHarmonicPage(BoxIndexPageReqVO pageReqVO);
}