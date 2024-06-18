package cn.iocoder.yudao.module.rack.service.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.rack.controller.admin.energyconsumption.VO.RackEnergyConsumptionPageReqVO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public interface RackEnergyConsumptionService {
    /**
     * 获得pdu电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getEQDataPage(RackEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu电费数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getBillDataPage(RackEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu电量数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    PageResult<Object> getEQDataDetails(RackEnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得pdu实时电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getRealtimeEQDataPage(RackEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo) throws IOException;

    Map<String, Object> getNewData() throws IOException;

    Map<String, Object> getOneDaySumData() throws IOException;

}
