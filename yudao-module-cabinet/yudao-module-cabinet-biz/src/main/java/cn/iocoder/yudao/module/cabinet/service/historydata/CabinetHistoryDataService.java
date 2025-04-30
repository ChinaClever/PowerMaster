package cn.iocoder.yudao.module.cabinet.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.aisle.AisleIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetEnvResVO;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataPageReqVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * pdu历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface CabinetHistoryDataService {
    List<Object> getLocationsByCabinetIds(List<Map<String, Object>> mapList);

    /**
     * 获得pdu历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu历史数据分页
     */
    PageResult<Object> getHistoryDataPage(CabinetHistoryDataPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu历史数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    PageResult<Object> getHistoryDataDetails(CabinetHistoryDataDetailsReqVO reqVO) throws IOException;

    Map<String, Object> getNavNewData(String granularity) throws IOException;


    List<Object> getNewHistoryList(List<Object> list);

    List<Object>  getNewDetailHistoryList(List<Object> list);

    Map<Integer, RoomIndex> getRoomById(List<Integer> roomIds);

    Map<Integer, AisleIndex> getAisleByIds(List<Integer> aisleIds);

    PageResult<CabinetEnvResVO> getHistoryDataPageEnv(CabinetHistoryDataPageReqVO pageReqVO,boolean isPage);

    Map<String, Object> getEnvNewData();

    PageResult<CabinetEnvResVO> getHistoryEnvDataDetails(Integer cabinetId, String granularity, String[] timeRange);
}