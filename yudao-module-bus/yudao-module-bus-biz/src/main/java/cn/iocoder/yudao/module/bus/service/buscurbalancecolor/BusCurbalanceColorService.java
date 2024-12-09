package cn.iocoder.yudao.module.bus.service.buscurbalancecolor;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.bus.controller.admin.buscurbalancecolor.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.buscurbalancecolor.BusCurbalanceColorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 母线不平衡度颜色 Service 接口
 *
 * @author clever
 */
public interface BusCurbalanceColorService {

    /**
     * 创建母线不平衡度颜色
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCurbalanceColor(@Valid BusCurbalanceColorSaveReqVO createReqVO);

    /**
     * 更新母线不平衡度颜色
     *
     * @param updateReqVO 更新信息
     */
    void updateCurbalanceColor(@Valid BusCurbalanceColorSaveReqVO updateReqVO);

    /**
     * 删除母线不平衡度颜色
     *
     * @param id 编号
     */
    void deleteCurbalanceColor(Long id);

    /**
     * 获得母线不平衡度颜色
     *
     * @return 母线不平衡度颜色
     */
    BusCurbalanceColorDO getCurbalanceColor();

    /**
     * 获得母线不平衡度颜色分页
     *
     * @param pageReqVO 分页查询
     * @return 母线不平衡度颜色分页
     */
    PageResult<BusCurbalanceColorDO> getCurbalanceColorPage(BusCurbalanceColorPageReqVO pageReqVO);

}