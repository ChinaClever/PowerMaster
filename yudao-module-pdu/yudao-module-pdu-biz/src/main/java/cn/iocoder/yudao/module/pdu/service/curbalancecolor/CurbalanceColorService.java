package cn.iocoder.yudao.module.pdu.service.curbalancecolor;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.pdu.controller.admin.curbalancecolor.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.curbalancecolor.CurbalanceColorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * PDU不平衡度颜色 Service 接口
 *
 * @author clever
 */
public interface CurbalanceColorService {

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
    CurbalanceColorDO getCurbalanceColor();

    /**
     * 获得PDU不平衡度颜色分页
     *
     * @param pageReqVO 分页查询
     * @return PDU不平衡度颜色分页
     */
    PageResult<CurbalanceColorDO> getCurbalanceColorPage(CurbalanceColorPageReqVO pageReqVO);

}