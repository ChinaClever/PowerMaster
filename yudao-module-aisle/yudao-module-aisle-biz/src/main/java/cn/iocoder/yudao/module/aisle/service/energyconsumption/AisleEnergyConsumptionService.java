package cn.iocoder.yudao.module.aisle.service.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.aisle.controller.admin.energyconsumption.VO.AisleEnergyConsumptionPageReqVO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public interface AisleEnergyConsumptionService {
    /**
     * 获得柜列电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 柜列电量数据分页
     */
    PageResult<Object> getEQDataPage(AisleEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得柜列电费数据分页
     *
     * @param pageReqVO 分页查询
     * @return 柜列电量数据分页
     */
    PageResult<Object> getBillDataPage(AisleEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得柜列电量数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return 柜列历史数据详情
     */
    PageResult<Object> getEQDataDetails(AisleEnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得柜列实时电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 柜列电量数据分页
     */
    PageResult<Object> getRealtimeEQDataPage(AisleEnergyConsumptionPageReqVO pageReqVO) throws IOException;

    Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo) throws IOException;

    Map<String, Object> getNewData() throws IOException;

    Map<String, Object> getOneDaySumData() throws IOException;

    /**
     * 获取分段电能电费
     *
     * @param reqVO 分页查询
     * @return 分段电能电费详情
     */
    PageResult<Object> getSubBillDetails(AisleEnergyConsumptionPageReqVO reqVO) throws IOException;

}
