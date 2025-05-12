package cn.iocoder.yudao.module.pdu.service.pdudevice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * PDU设备 Service 接口
 *
 * @author 芋道源码
 */
public interface PDUDeviceService {


    /**
     * 获得PDU设备分页
     *
     * @param pageReqVO 分页查询
     * @return PDU设备分页
     */
    PageResult<PDUDeviceDO> getPDUDevicePage(PDUDevicePageReqVO pageReqVO);

    PageResult<PDUDeviceDO> getDeletedPDUDevicePage(PDUDevicePageReqVO pageReqVO);

    String getDisplayDataByDevKey(String devKey);

    Map getHistoryDataByDevKey(String devKey, String type);

    Map getChartNewDataByPduDevKey(String devKey, LocalDateTime oldTime, String type);

    Map getReportConsumeDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime,Integer dataType);

    Map getReportOutLetDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime,Integer dataType);

    PageResult<PDULineRes> getPDULineDevicePage(PDUDevicePageReqVO pageReqVO);


    List<String> getDevKeyList();

    List<String> getIpList();

    Map getPDUPFLine(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime,Integer dataType);

    int deletePDU(String devKey) throws Exception;

    int restorePDU(String devKey) throws Exception;

    Integer getPDUMaxLineId(PDUDevicePageReqVO pageReqVO);

    PageResult<PDULineRes> getPDUMaxCurData(PDUDevicePageReqVO pageReqVO);

    String getLocationByDevKey(String devKey);

    Map getPduHdaLineHisdataKey(String devKey, Integer type,LocalDateTime oldTime,LocalDateTime newTime,Integer dataType);

    Map getPduHdaLineHisdataKeyByCabinet(Long cabinetId, String type, LocalDateTime oldTime, LocalDateTime newTime);

    Map getPduMaxLine(PDURequireDetailReq pduRequireDetailReq);

    PduBalanceDeatilRes getPDUDeviceDetail(String key);

    List<PduTrendVO> getPudBalanceTrend(Integer pduId, Integer timeType);

    PduDeviceCountResVO getPDUDeviceCount();

    BalancedDistributionStatisticsVO getBalancedDistribution();

    Map<String, String> setLocation(List<String> collect1);

    Map getReportLoopDataDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType);

    Map getReportOutLetCurDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime, Integer dataType);
}