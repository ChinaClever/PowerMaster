package cn.iocoder.yudao.module.pdu.service.energyconsumption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.BalancedDistributionStatisticsVO;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EleTotalRealtimeReqDTO;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EleTotalRealtimeRespVO;
import cn.iocoder.yudao.module.pdu.controller.admin.energyconsumption.VO.EnergyConsumptionPageReqVO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EnergyConsumptionService {
    /**
     * 获得pdu电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu电费数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getBillDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu电量数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    PageResult<Object> getEQDataDetails(EnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得pdu各输出位电量数据
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    List<Object> getOutletsEQData(EnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得pdu实时电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu电量数据分页
     */
    PageResult<Object> getRealtimeEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo) throws IOException;

    Map<String, Object> getNewData() throws IOException;

    Map<String, Object> getOneDaySumData() throws IOException;

    /**
     * 获取分段电能电费
     *
     * @param reqVO 分页查询
     * @return 分段电能电费详情
     */
    PageResult<Object> getSubBillDetails(EnergyConsumptionPageReqVO reqVO) throws IOException;

    List<Object> getNewList(List<Object> list1);

    List<Object> getNewBillList(List<Object> list1);

    List<Object> getNewEQList(List<Object> list1);

    List<Object> getNewOutLetsList(List<Object> list1);

    PageResult<EleTotalRealtimeRespVO> getEleTotalRealtime(EleTotalRealtimeReqDTO reqDTO, boolean flag) throws IOException;
}
