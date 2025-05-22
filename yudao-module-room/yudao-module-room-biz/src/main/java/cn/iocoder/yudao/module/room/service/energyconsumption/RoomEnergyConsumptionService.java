package cn.iocoder.yudao.module.room.service.energyconsumption;

import cn.iocoder.yudao.framework.common.entity.mysql.FileManage;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.room.controller.admin.energyconsumption.VO.RoomEnergyConsumptionPageReqVO;
import cn.iocoder.yudao.module.room.vo.RoomEleExportVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RoomEnergyConsumptionService {
    /**
     * 获得机房电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 机房电量数据分页
     */
    PageResult<Object> getEQDataPage(RoomEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得机房电费数据分页
     *
     * @param pageReqVO 分页查询
     * @return 机房电量数据分页
     */
    PageResult<Object> getBillDataPage(RoomEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得机房电量数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return 机房历史数据详情
     */
    PageResult<Object> getEQDataDetails(RoomEnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得机房实时电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 机房电量数据分页
     */
    PageResult<Object> getRealtimeEQDataPage(RoomEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo) throws IOException;

    Map<String, Object> getNewData() throws IOException;

    Map<String, Object> getOneDaySumData() throws IOException;

    /**
     * 获取分段电能电费
     *
     * @param reqVO 分页查询
     * @return 分段电能电费详情
     */
    PageResult<Object> getSubBillDetails(RoomEnergyConsumptionPageReqVO reqVO) throws IOException;

    List<RoomEleExportVO> exportRoomEle() throws IOException;

    void saveFileManage(FileManage fileManage);
}
