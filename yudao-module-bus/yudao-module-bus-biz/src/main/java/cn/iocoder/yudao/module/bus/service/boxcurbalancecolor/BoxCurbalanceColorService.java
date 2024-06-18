package cn.iocoder.yudao.module.bus.service.boxcurbalancecolor;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.bus.controller.admin.boxcurbalancecolor.vo.*;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxcurbalancecolor.BoxCurbalanceColorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 插接箱不平衡度颜色 Service 接口
 *
 * @author clever
 */
public interface BoxCurbalanceColorService {

    /**
     * 创建插接箱不平衡度颜色
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBoxCurbalanceColor(@Valid BoxCurbalanceColorSaveReqVO createReqVO);

    /**
     * 更新插接箱不平衡度颜色
     *
     * @param updateReqVO 更新信息
     */
    void updateBoxCurbalanceColor(@Valid BoxCurbalanceColorSaveReqVO updateReqVO);

    /**
     * 删除插接箱不平衡度颜色
     *
     * @param id 编号
     */
    void deleteBoxCurbalanceColor(Long id);

    /**
     * 获得插接箱不平衡度颜色
     *
     * @param id 编号
     * @return 插接箱不平衡度颜色
     */
    BoxCurbalanceColorDO getBoxCurbalanceColor(Long id);

    /**
     * 获得插接箱不平衡度颜色分页
     *
     * @param pageReqVO 分页查询
     * @return 插接箱不平衡度颜色分页
     */
    PageResult<BoxCurbalanceColorDO> getBoxCurbalanceColorPage(BoxCurbalanceColorPageReqVO pageReqVO);

}