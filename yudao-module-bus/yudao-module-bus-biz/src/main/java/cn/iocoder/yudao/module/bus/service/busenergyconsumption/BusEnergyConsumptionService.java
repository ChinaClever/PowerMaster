package cn.iocoder.yudao.module.bus.service.busenergyconsumption;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO.BusEleTotalRealtimeResVO;
import cn.iocoder.yudao.module.bus.controller.admin.energyconsumption.VO.EnergyConsumptionPageReqVO;
import cn.iocoder.yudao.module.bus.dto.BusEleTotalRealtimeReqDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface BusEnergyConsumptionService {
    /**
     * 获得始端箱电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 始端箱电量数据分页
     */
    PageResult<Object> getEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得始端箱电费数据分页
     *
     * @param pageReqVO 分页查询
     * @return 始端箱电量数据分页
     */
    PageResult<Object> getBillDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得始端箱电量数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return 始端箱历史数据详情
     */
    PageResult<Object> getEQDataDetails(EnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得始端箱实时电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 始端箱电量数据分页
     */
    PageResult<Object> getRealtimeEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 公共方法 获取某段时间某个索引里新增多少条数据
     *
     * @param indices es索引名
     * @param name    返回数据的key
     * @param timeAgo 时间范围
     * @return 新增数据条数
     */
    Map<String, Object> getSumData(String[] indices, String[] name, LocalDateTime[] timeAgo) throws IOException;

    /**
     * 获得始端箱最近天周月各新增的数据条数
     *
     * @return 始端箱最近天周月各新增的数据条数
     */
    Map<String, Object> getNewData() throws IOException;

    /**
     * 获得始端箱实时数据最近一天各新增的数据条数
     *
     * @return 始端箱实时数据最近一天各新增的数据条数
     */
    Map<String, Object> getOneDaySumData(String timeRangeType,LocalDateTime oldTime,LocalDateTime newTime) throws IOException;

    /**
     * 获取始端箱分段电能电费
     *
     * @param reqVO 分页查询
     * @return 始端箱分段电能电费详情
     */
    PageResult<Object> getBusSubBillDetails(EnergyConsumptionPageReqVO reqVO) throws IOException;


//------------------------------------------------------------------------

    /**
     * 获得插接箱电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 插接箱电量数据分页
     */
    PageResult<Object> getBoxEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得插接箱电费数据分页
     *
     * @param pageReqVO 分页查询
     * @return 插接箱电量数据分页
     */
    PageResult<Object> getBoxBillDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得插接箱电量数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return 插接箱历史数据详情
     */
    PageResult<Object> getBoxEQDataDetails(EnergyConsumptionPageReqVO reqVO) throws IOException;

    /**
     * 获得插接箱实时电量数据分页
     *
     * @param pageReqVO 分页查询
     * @return 插接箱电量数据分页
     */
    PageResult<Object> getBoxRealtimeEQDataPage(EnergyConsumptionPageReqVO pageReqVO) throws IOException;

    /**
     * 获得插接箱最近天周月各新增的数据条数
     *
     * @return 插接箱最近天周月各新增的数据条数
     */
    Map<String, Object> getBoxNewData() throws IOException;

    /**
     * 获得插接箱实时数据最近一天各新增的数据条数
     *
     * @return 插接箱实时数据最近一天各新增的数据条数
     */
    Map<String, Object> getBoxOneDaySumData(String timeRangeType,LocalDateTime oldTime,LocalDateTime newTime) throws IOException;

    /**
     * 获取插接箱分段电能电费
     *
     * @param reqVO 分页查询
     * @return 始端箱分段电能电费详情
     */
    PageResult<Object> getBoxSubBillDetails(EnergyConsumptionPageReqVO reqVO) throws IOException;

    List<Object> getNewlList(List<Object> list);


    List<Object> getNewDetailList(List<Object> list);

    List<Object> getNewBillList(List<Object> list);

    List<Object> getNewRealtimeList(List<Object> list);

    List<Object> getNewList(List<Object> list);

    PageResult<BusEleTotalRealtimeResVO> getBusEleTotalRealtime(BusEleTotalRealtimeReqDTO reqDTO, boolean flag) throws IOException;

    PageResult<BusEleTotalRealtimeResVO> getBoxEleTotalRealtime(BusEleTotalRealtimeReqDTO reqDTO, boolean flag) throws IOException;
}
