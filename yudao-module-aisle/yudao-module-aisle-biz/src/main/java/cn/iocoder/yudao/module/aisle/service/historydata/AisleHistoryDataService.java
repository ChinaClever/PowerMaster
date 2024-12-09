package cn.iocoder.yudao.module.aisle.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.aisle.controller.admin.historydata.vo.AisleHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.aisle.controller.admin.historydata.vo.AisleHistoryDataPageReqVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 柜列历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface AisleHistoryDataService {
    List<Object> getLocationsByAisleIds(List<Map<String, Object>> mapList);

    /**
     * 获得柜列历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return 柜列历史数据分页
     */
    PageResult<Object> getHistoryDataPage(AisleHistoryDataPageReqVO pageReqVO) throws IOException;

    /**
     * 获得柜列历史数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return 柜列历史数据详情
     */
    PageResult<Object> getHistoryDataDetails(AisleHistoryDataDetailsReqVO reqVO) throws IOException;

    Map<String, Object> getNavNewData(String granularity) throws IOException;


    List<Object> getNewExcelList(List<Object> list);

    List<Object> getNewDetailsExcelList(List<Object> list);

    Map<Integer, RoomIndex> getRoomById(List<Integer> roomIds);
}