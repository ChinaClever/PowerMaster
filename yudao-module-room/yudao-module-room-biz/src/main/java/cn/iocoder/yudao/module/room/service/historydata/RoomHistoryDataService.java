package cn.iocoder.yudao.module.room.service.historydata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.room.controller.admin.historydata.vo.RoomHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.room.controller.admin.historydata.vo.RoomHistoryDataPageReqVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 机房历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface RoomHistoryDataService {
    List<Object> getLocationsByRoomIds(List<Map<String, Object>> mapList);

    /**
     * 获得机房历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return 机房历史数据分页
     */
    PageResult<Object> getHistoryDataPage(RoomHistoryDataPageReqVO pageReqVO) throws IOException;

    /**
     * 获得机房历史数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return 机房历史数据详情
     */
    PageResult<Object> getHistoryDataDetails(RoomHistoryDataDetailsReqVO reqVO) throws IOException;

    Map<String, Object> getNavNewData(String granularity) throws IOException;


}