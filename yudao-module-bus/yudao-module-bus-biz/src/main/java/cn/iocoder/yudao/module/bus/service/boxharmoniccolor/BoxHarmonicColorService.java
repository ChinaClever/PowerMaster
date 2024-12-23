package cn.iocoder.yudao.module.bus.service.boxharmoniccolor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor.vo.BoxHarmonicColorPageReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.boxharmoniccolor.vo.BoxHarmonicColorSaveReqVO;
import cn.iocoder.yudao.module.bus.dal.dataobject.boxharmoniccolor.BoxHarmonicColorDO;

import javax.validation.Valid;

/**
 * 插接箱谐波颜色 Service 接口
 *
 * @author clever
 */
public interface BoxHarmonicColorService {

    /**
     * 创建插接箱谐波颜色
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBoxHarmonicColor(@Valid BoxHarmonicColorSaveReqVO createReqVO);

    /**
     * 更新插接箱谐波颜色
     *
     * @param updateReqVO 更新信息
     */
    void updateBoxHarmonicColor(@Valid BoxHarmonicColorSaveReqVO updateReqVO);

    /**
     * 删除插接箱谐波颜色
     *
     * @param id 编号
     */
    void deleteBoxHarmonicColor(Long id);

    /**
     * 获得插接箱谐波颜色
     *
     * @return 插接箱谐波颜色
     */
    BoxHarmonicColorDO getBoxHarmonicColor();

    /**
     * 获得插接箱谐波颜色分页
     *
     * @param pageReqVO 分页查询
     * @return 插接箱谐波颜色分页
     */
    PageResult<BoxHarmonicColorDO> getBoxHarmonicColorPage(BoxHarmonicColorPageReqVO pageReqVO);

}