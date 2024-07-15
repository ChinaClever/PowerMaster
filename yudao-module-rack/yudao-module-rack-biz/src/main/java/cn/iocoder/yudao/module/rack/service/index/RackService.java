package cn.iocoder.yudao.module.rack.service.index;

import javax.validation.*;
import cn.iocoder.yudao.module.rack.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.rack.dal.dataobject.index.RackDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 机架索引 Service 接口
 *
 * @author clever
 */
public interface RackService {

    /**
     * 创建机架索引
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createIndex(@Valid IndexSaveReqVO createReqVO);

    /**
     * 更新机架索引
     *
     * @param updateReqVO 更新信息
     */
    void updateIndex(@Valid IndexSaveReqVO updateReqVO);

    /**
     * 删除机架索引
     *
     * @param id 编号
     */
    void deleteIndex(Integer id);

    /**
     * 获得机架索引
     *
     * @param id 编号
     * @return 机架索引
     */
    RackDO getIndex(Integer id);

    /**
     * 获得机架索引分页
     *
     * @param pageReqVO 分页查询
     * @return 机架索引分页
     */
    PageResult<RackDO> getIndexPage(IndexPageReqVO pageReqVO);

    Map getReportConsumeDataById(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportPowDataById(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    String getRackRedisById(Integer id);

    Map getRoomPFLine(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);
}