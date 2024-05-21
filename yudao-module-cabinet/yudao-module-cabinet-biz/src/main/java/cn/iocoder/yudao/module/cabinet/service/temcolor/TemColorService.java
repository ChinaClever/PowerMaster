package cn.iocoder.yudao.module.cabinet.service.temcolor;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.cabinet.controller.admin.temcolor.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.temcolor.TemColorDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 机柜温度颜色 Service 接口
 *
 * @author clever
 */
public interface TemColorService {

    /**
     * 创建机柜温度颜色
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTemColor(@Valid List<TemColorSaveReqVO> createReqVO);

    /**
     * 更新机柜温度颜色
     *
     * @param updateReqVO 更新信息
     */
    void updateTemColor(@Valid TemColorSaveReqVO updateReqVO);

    /**
     * 删除机柜温度颜色
     *
     * @param id 编号
     */
    void deleteTemColor(Long id);

    /**
     * 获得机柜温度颜色
     *
     * @param id 编号
     * @return 机柜温度颜色
     */
    TemColorDO getTemColor(Long id);

    /**
     * 获得机柜温度颜色分页
     *
     * @param pageReqVO 分页查询
     * @return 机柜温度颜色分页
     */
    PageResult<TemColorDO> getTemColorPage(TemColorPageReqVO pageReqVO);

    List<TemColorDO> getTemColorAll();
}