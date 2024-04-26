package cn.iocoder.yudao.module.pdu.service.pdudevice;

import javax.validation.*;

import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDevicePageReqVO;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.PDUDeviceSaveReqVO;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

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

    String getDisplayDataByDevKey(String devKey);

    Map getHistoryDataByPduId(Long id,String type);

    Map getChartNewDataByPduDevKey(String devKey, LocalDateTime oldTime,String type);

    Map getReportConsumeDataByDevKey(String devKey, Integer timeType,LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportPowDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportOutLetDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);

    Map getReportTemDataByDevKey(String devKey, Integer timeType, LocalDateTime oldTime, LocalDateTime newTime);
}