package cn.iocoder.yudao.module.cabinet.service.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.controller.admin.energyconsumption.VO.CabinetEnergyConsumptionPageReqVO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CabinetEnergyConsumptionService {
    /**
     * 获得机柜电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 机柜电量数据分页
     */
    PageResult<Object> getEQDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得机柜电费数据分页
     *
     * @param pageReqVO 分页查询
     * @return 机柜电量数据分页
     */
    PageResult<Object> getBillDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得机柜电量数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return 机柜历史数据详情
     */
    PageResult<Object> getEQDataDetails(CabinetEnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得机柜实时电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 机柜电量数据分页
     */
    PageResult<Object> getRealtimeEQDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo) throws IOException;

    Map<String, Object> getNewData() throws IOException;

    Map<String, Object> getOneDaySumData() throws IOException;

    /**
     * 获取分段电能电费
     *
     * @param reqVO 分页查询
     * @return 分段电能电费详情
     */
    PageResult<Object> getSubBillDetails(CabinetEnergyConsumptionPageReqVO reqVO) throws IOException;

    List<Object> getNewEqList(List<Object> list);

    List<Object>  getNewDetailsList(List<Object> list);

    List<Object> getNewRealtimeList(List<Object> list);
}
