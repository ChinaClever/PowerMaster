package cn.iocoder.yudao.module.cabinet.service.historydata;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataDetailsReqVO;
import cn.iocoder.yudao.module.cabinet.controller.admin.historydata.vo.CabinetHistoryDataPageReqVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * pdu历史数据 Service 接口
 *
 * @author 芋道源码
 */
public interface CabinetHistoryDataService {
    List<Object> getLocationsByCabinetIds(List<Map<String, Object>> mapList);

    /**
     * 获得pdu历史数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu历史数据分页
     */
    PageResult<Object> getHistoryDataPage(CabinetHistoryDataPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu历史数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
    PageResult<Object> getHistoryDataDetails(CabinetHistoryDataDetailsReqVO reqVO) throws IOException;

    Map<String, Object> getOneHourSumData() throws IOException;

    /**
     * 获得pdu环境数据分页
     *
     * @param pageReqVO 分页查询
     * @return pdu环境数据分页
     */
//    PageResult<Object> getEnvDataPage(HistoryDataPageReqVO pageReqVO) throws IOException;

    /**
     * 获得pdu环境数据详情（曲线）
     *
     * @param reqVO 分页查询
     * @return pdu历史数据详情
     */
//    PageResult<Object> getEnvDataDetails(EnvDataDetailsReqVO reqVO) throws IOException;

}