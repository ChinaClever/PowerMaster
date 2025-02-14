package cn.iocoder.yudao.module.cabinet.service.index;

import java.time.LocalDateTime;
import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.cabinet.controller.admin.index.vo.*;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 机柜索引 Service 接口
 *
 * @author 芋道源码
 */
public interface IndexService {

    /**
     * 创建机柜索引
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createIndex(@Valid IndexSaveReqVO createReqVO);

    /**
     * 更新机柜索引
     *
     * @param updateReqVO 更新信息
     */
    void updateIndex(@Valid IndexSaveReqVO updateReqVO);

    /**
     * 删除机柜索引
     *
     * @param id 编号
     */
    void deleteIndex(Integer id);

    /**
     * 获得机柜索引
     *
     * @param id 编号
     * @return 机柜索引
     */
    IndexDO getIndex(Integer id);

    /**
     * 获得机柜索引分页
     *
     * @param pageReqVO 分页查询
     * @return 机柜索引分页
     */
    PageResult<IndexDO> getIndexPage(IndexPageReqVO pageReqVO);

    Map getReportConsumeDataById(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportPowDataById(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getCabinetEnvIceTemAndHumData(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getCabinetEnvHotTemAndHumData(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getCabinetPFLine(String id, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    List<Integer> idList();

    List<CabinetRackRspVO> getRackByCabinet(Integer id);
}