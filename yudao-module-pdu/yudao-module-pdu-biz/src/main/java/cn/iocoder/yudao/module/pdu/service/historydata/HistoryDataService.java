package cn.iocoder.yudao.module.pdu.service.historydata;


import javax.validation.*;
import cn.iocoder.yudao.module.pdu.controller.admin.historydata.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.historydata.HistoryDataDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * pdu历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface HistoryDataService {

    /**
     * 创建pdu历史数据
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createHistoryData(@Valid HistoryDataSaveReqVO createReqVO);

    /**
     * 更新pdu历史数据
     *
     * @param updateReqVO 更新信息
     */
    void updateHistoryData(@Valid HistoryDataSaveReqVO updateReqVO);

    /**
     * 删除pdu历史数据
     *
     * @param id 编号
     */
    void deleteHistoryData(Long id);

    /**
     * 获得pdu历史数据
     *
     * @param id 编号
     * @return pdu历史数据
     */
    HistoryDataDO getHistoryData(Long id);

    /**
     * 获得pdu历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu历史数据分页
     */
    PageResult<HistoryDataDO> getHistoryDataPage(HistoryDataPageReqVO pageReqVO);


    Void insertHistoryDataDO(HistoryDataDO historyDataDO);

    //保存和修改
//    void save(EsHistoryDataDO esHistoryDataDO);

    //查询id
//    EsHistoryDataDO findById(Long id);
//
//    //删除指定ID数据
//    void deleteById(Long id);
//
//    long count();
//
//    boolean existsById(Long id);

}