package cn.iocoder.yudao.module.rack.service.historydata;

import cn.iocoder.yudao.framework.common.entity.mysql.cabinet.CabinetIndex;
import cn.iocoder.yudao.framework.common.entity.mysql.room.RoomIndex;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.dal.dataobject.index.IndexDO;
import cn.iocoder.yudao.module.rack.controller.admin.historydata.vo.RackHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.rack.controller.admin.historydata.vo.RackHistoryDataPageReqVO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * pdu历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface RackHistoryDataService {
    List<Object> getRackNameAndLocationsByCabinetIds(List<Map<String, Object>> mapList);

    /**
     * 获得pdu历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu历史数据分页
     */
    PageResult<Object> getHistoryDataPage(RackHistoryDataPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu历史数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    PageResult<Object> getHistoryDataDetails(RackHistoryDataDetailsReqVO reqVO) throws IOException;

    Map<String, Object> getNavNewData(String granularity) throws IOException;


    List<Object> getNewList(List<Object> list);

    Map<Integer, String> getRoomById(List<Integer> roomIds);

    Map<Integer , IndexDO> getCabinetByIds(List<Integer> cabineIds);
}