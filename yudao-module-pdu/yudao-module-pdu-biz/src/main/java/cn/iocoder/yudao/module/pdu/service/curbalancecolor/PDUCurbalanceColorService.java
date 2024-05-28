package cn.iocoder.yudao.module.pdu.service.curbalancecolor;

import javax.validation.*;
import cn.iocoder.yudao.module.pdu.controller.admin.curbalancecolor.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.PDUCurbalanceColorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * PDU不平衡度颜色 Service 接口
 *
 * @author clever
 */
public interface PDUCurbalanceColorService {

    /**
     * 创建PDU不平衡度颜色
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCurbalanceColor(@Valid CurbalanceColorSaveReqVO createReqVO);

    /**
     * 更新PDU不平衡度颜色
     *
     * @param updateReqVO 更新信息
     */
    void updateCurbalanceColor(@Valid CurbalanceColorSaveReqVO updateReqVO);

    /**
     * 删除PDU不平衡度颜色
     *
     * @param id 编号
     */
    void deleteCurbalanceColor(Long id);

    /**
     * 获得PDU不平衡度颜色
     *
     *
     * @return PDU不平衡度颜色
     */
    PDUCurbalanceColorDO getCurbalanceColor();

    /**
     * 获得PDU不平衡度颜色分页
     *
     * @param pageReqVO 分页查询
     * @return PDU不平衡度颜色分页
     */
    PageResult<PDUCurbalanceColorDO> getCurbalanceColorPage(CurbalanceColorPageReqVO pageReqVO);

}