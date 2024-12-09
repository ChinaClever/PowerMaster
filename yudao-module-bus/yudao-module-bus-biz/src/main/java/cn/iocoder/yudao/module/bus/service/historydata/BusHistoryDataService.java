package cn.iocoder.yudao.module.bus.service.historydata;


import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.bus.controller.admin.historydata.vo.BusHistoryDataPageReqVO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * pdu历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface BusHistoryDataService {
    String[]  getBusIdsbyBusDevkeys(String[] devkeys);
    String[]  getBoxIdsbyBoxDevkeys(String[] devkeys);
    List<Object> getLocationsAndNameByBusIds(List<Map<String, Object>> mapList);

    List<Object> getLocationsAndNameByBoxIds(List<Map<String, Object>> mapList);

    Map getHistoryDataTypeMaxValue(String[] boxIds) throws IOException;

    /**
     * 获得母线始端箱历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return 母线始端箱历史数据
     */
    PageResult<Object> getBusHistoryDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException;

    /**
     * 获得母线插接箱历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return 母线插接箱历史数据
     */
    PageResult<Object> getBoxHistoryDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException;

    /**
     * 获得母线始端箱历史数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return 母线始端箱历史数据详情
     */
    PageResult<Object> getBusHistoryDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException;

    /**
     * 获得母线插接箱历史数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return 母线始端箱历史数据详情
     */
    PageResult<Object> getBoxHistoryDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException;

    Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo, String field) throws IOException;
    Map<String, Object> getBusNavNewData(String granularity) throws IOException;
    Map<String, Object> getBoxNavNewData(String granularity) throws IOException;

    PageResult<Object> getBusEnvDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException;

    PageResult<Object> getBusEnvDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException;

    Map<String, Object> getBusEnvNavNewData() throws IOException;

    PageResult<Object> getBoxEnvDataPage(BusHistoryDataPageReqVO pageReqVO) throws IOException;

    PageResult<Object> getBoxEnvDataDetails(BusHistoryDataDetailsReqVO reqVO) throws IOException;

    Map<String, Object> getBoxEnvNavNewData() throws IOException;

    List<Object> getNewHistoryList(List<Object> list);

    List<Object> getNewHistoryList1(List<Object> list);

    List<Object> getNewBoxHistoryList(List<Object> list);

    List<Object> getNewBoxHistoryList1(List<Object> list);

    List<Object> getNewDetailHistoryList(List<Object> list);

    List<Object> getNewBoxDetailHistoryList(List<Object> list);

    List<Object>  getNewEnvHistoryList(List<Object> list);

    List<Object> getNewTemHistoryList(List<Object> list);
}