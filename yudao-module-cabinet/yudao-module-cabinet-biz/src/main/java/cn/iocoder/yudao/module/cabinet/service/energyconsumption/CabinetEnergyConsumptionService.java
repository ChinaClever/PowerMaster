package cn.iocoder.yudao.module.cabinet.service.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.controller.admin.energyconsumption.VO.CabinetEnergyConsumptionPageReqVO;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public interface CabinetEnergyConsumptionService {
    /**
     * 获得pdu电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getEQDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu电费数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getBillDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu电量数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    PageResult<Object> getEQDataDetails(CabinetEnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得pdu实时电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getRealtimeEQDataPage(CabinetEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime timeAgo) throws IOException;

    Map<String, Object> getOneWeekSumData() throws IOException;

    Map<String, Object> getOneDaySumData() throws IOException;

}
