package cn.iocoder.yudao.module.pdu.controller.admin.pdudevice;

import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pdu.controller.admin.pdudevice.vo.*;
import cn.iocoder.yudao.module.pdu.dal.dataobject.pdudevice.PDUDeviceDO;
import cn.iocoder.yudao.module.pdu.service.pdudevice.PDUDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - PDU设备")
@RestController
@RequestMapping("/pdu/PDU-device")
@Validated
public class PDUDeviceController {

    @Autowired
    private PDUDeviceService pDUDeviceService;

    @PostMapping("/page")
    @Operation(summary = "获得PDU配电分页")
    public CommonResult<PageResult<PDUDeviceDO>> getPDUDevicePage(@RequestBody PDUDevicePageReqVO pageReqVO) {
        PageResult<PDUDeviceDO> pageResult = pDUDeviceService.getPDUDevicePage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/balancedDistribution")
    @Operation(summary = "获得pdu均衡配电统计")
    public CommonResult<BalancedDistributionStatisticsVO> getBalancedDistribution() throws IOException {
        BalancedDistributionStatisticsVO pageResult = pDUDeviceService.getBalancedDistribution();
        return success(pageResult);
    }

    @GetMapping("/balance/detail")
    @Operation(summary = "获得PDU配电分页详情")
    public CommonResult<PduBalanceDeatilRes> getPDUDeviceDetail(String devKey) {
        PduBalanceDeatilRes pageResult = pDUDeviceService.getPDUDeviceDetail(devKey);
        return success(pageResult);
    }

    @GetMapping("/detailCount")
    @Operation(summary = "获得PDU配电分汇总统计")
    public CommonResult<PduDeviceCountResVO> getPDUDeviceCount() {
        PduDeviceCountResVO resVO = pDUDeviceService.getPDUDeviceCount();
        return success(resVO);
    }

    @GetMapping("/balance/trend")
    @Operation(summary = "获得PDU不平衡度详情图表")
    public CommonResult<List<PduTrendVO>> getPudBalanceTrend(@RequestParam(value = "pduId")@Parameter(description = "机柜id") Integer pduId,
                                                             @RequestParam(value = "timeType")@Parameter(description = "0 - 实时；1-历史") Integer timeType) {
        List<PduTrendVO> result = pDUDeviceService.getPudBalanceTrend(pduId,timeType);
        return success(result);
    }

    @PostMapping("/getDeletedPage")
    @Operation(summary = "获得已删除PDU分页")
    public CommonResult<PageResult<PDUDeviceDO>> getDeletedPDUDevicePage(@RequestBody PDUDevicePageReqVO pageReqVO) {
        PageResult<PDUDeviceDO> pageResult = pDUDeviceService.getDeletedPDUDevicePage(pageReqVO);
        return success(pageResult);
    }

    @PostMapping("/line/page")
    @Operation(summary = "获得PDU需量分页")
    public CommonResult<PageResult<PDULineRes>> getPDULineDevicePage(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPDULineDevicePage(pageReqVO));
    }

    @PostMapping("/line/getMaxLineId")
    @Operation(summary = "获得PDU相id的最大值")
    public CommonResult<Integer> getPDUMaxLineId(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPDUMaxLineId(pageReqVO));
    }

    @PostMapping("/line/getMaxCur")
    @Operation(summary = "获得PDU电流最大值的相数据")
    public CommonResult<MaxCurAndOtherData> getPDUMaxCurData(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPDUMaxCurData(pageReqVO));
    }

    @GetMapping("/displayscreen")
    @Operation(summary = "获得PDU设备详细信息")
    public CommonResult<String> getDisplay(String devKey) {
        return success(pDUDeviceService.getDisplayDataByDevKey(devKey));
    }

    @PostMapping("/pduBasicInformation")
    @Operation(summary = "获得PDU设备详细信息")
    public CommonResult<List<PduBasicInformationVo>> getPduDisplayDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPduDisplayDataByDevKey(pageReqVO.getPduKeyList(),pageReqVO.getTimeType()
                ,pageReqVO.getOldTime(),pageReqVO.getNewTime()));
    }


    @GetMapping("/displayscreen/location")
    @Operation(summary = "获得位置")
    public CommonResult<String> getLocationByDevKey(String devKey) {
        return success(pDUDeviceService.getLocationByDevKey(devKey));
    }

    @GetMapping("/hisdata")
    @Operation(summary = "获得PDU历史数据")
    public CommonResult<Map> getHistoryDataByDevKey(String devKey, String type) {
        return success(pDUDeviceService.getHistoryDataByDevKey(devKey, type));
    }

    //pdu_hda_line_realtime
    @GetMapping("/pduHdaLineHisdata")
    @Operation(summary = "获得机柜PDU相历史数据")
    public CommonResult<Map> getPduHdaLineHisdataKey(String devKey, String type) {
        return success(pDUDeviceService.getPduHdaLineHisdataKey(devKey, type));
    }

    @GetMapping("/pduHdaLineHisdataReport")
    @Operation(summary = "获得机柜PDU相历史数据")
    public CommonResult<Map> pduHdaLineHisdataReportKey(String devKey, Integer type,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime,@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime,Integer dataType) {
        System.out.println("oldTime"+oldTime);
        System.out.println("newTime"+newTime);
        return success(pDUDeviceService.pduHdaLineHisdataReportKey(devKey, type,oldTime,newTime,dataType));
    }


    @GetMapping("/pduHdaLineHisdataByCabinet")
    @Operation(summary = "获得PDU相历史数据")
    public CommonResult<Map> getPduHdaLineHisdataKey(Long CabinetId, String type, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime) {
        return success(pDUDeviceService.getPduHdaLineHisdataKeyByCabinet(CabinetId, type, oldTime, newTime));
    }

    @GetMapping("/pduHdaLineHisDataByCabinetByType")
    @Operation(summary = "获得PDU相历史数据")
    public CommonResult<Map> getPduHdaLineHisDataKey(Long CabinetId, String type, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime newTime,Integer dataType) {
        return success(pDUDeviceService.getPduHdaLineHisdataKeyByCabinetByType(CabinetId, type, oldTime, newTime,dataType));
    }

    @GetMapping("/chartNewData")
    @Operation(summary = "获得PDU历史最新数据")
    public CommonResult<Map> getChartNewDataByPduDevKey(String devKey, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime oldTime, String type) {
        return success(pDUDeviceService.getChartNewDataByPduDevKey(devKey, oldTime, type));
    }

    @PostMapping("/report/ele")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportConsumeDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportConsumeDataByDevKey(pageReqVO.getDevKey(), pageReqVO.getTimeType(), pageReqVO.getOldTime(), pageReqVO.getNewTime()));
    }

    @PostMapping("/report/totalEle")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportConsumeDataByDevKeys(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportConsumeDataByDevKeys(pageReqVO.getPduKeyList(), pageReqVO.getTimeType(), pageReqVO.getOldTime(), pageReqVO.getNewTime()));
    }


    @PostMapping("/report/loop")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportLoopDataDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO){
        return success(pDUDeviceService.getReportLoopDataDataByDevKey(pageReqVO.getDevKey(), pageReqVO.getTimeType(), pageReqVO.getOldTime(), pageReqVO.getNewTime(),pageReqVO.getDataType()));
    }

    @PostMapping("/report/pfline")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getPDUPFLine(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getPDUPFLine(pageReqVO.getDevKey(), pageReqVO.getTimeType(), pageReqVO.getOldTime(), pageReqVO.getNewTime(),pageReqVO.getDataType()));
    }

    @PostMapping("/report/pow")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportPowDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportPowDataByDevKey(pageReqVO.getDevKey(), pageReqVO.getTimeType(), pageReqVO.getOldTime(), pageReqVO.getNewTime(),pageReqVO.getDataType()));
    }

    @PostMapping("/report/outletNew")
    @Operation(summary = "获得PDU报表输出位电流数据")
    public CommonResult<Map> getReportOutLetCurDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportOutLetCurDataByDevKey(pageReqVO.getDevKey(), pageReqVO.getTimeType(), pageReqVO.getOldTime(), pageReqVO.getNewTime(),pageReqVO.getDataType()));
    }

    @PostMapping("/report/outlet")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportOutLetDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportOutLetDataByDevKey(pageReqVO.getDevKey(), pageReqVO.getTimeType(), pageReqVO.getOldTime(), pageReqVO.getNewTime()));
    }

    @PostMapping("/report/tem")
    @Operation(summary = "获得PDU报表数据")
    public CommonResult<Map> getReportTemDataByDevKey(@RequestBody PDUDevicePageReqVO pageReqVO) {
        return success(pDUDeviceService.getReportTemDataByDevKey(pageReqVO.getDevKey(), pageReqVO.getTimeType(), pageReqVO.getOldTime(), pageReqVO.getNewTime(),pageReqVO.getDataType()));
    }

    @GetMapping("/devKeyList")
    @Operation(summary = "获得PDU设备devKey列表")
    public List<String> getDevKeyList() {
        return pDUDeviceService.getDevKeyList();
    }

    @GetMapping("/ipList")
    @Operation(summary = "获得PDU设备Ip列表")
    public List<String> getIpList() {
        return pDUDeviceService.getIpList();
    }

    @GetMapping("/delete")
    @Operation(summary = "删除PDU设备")
    public CommonResult<Integer> deletePDU(@Param("devKey") String devKey) throws Exception {
        int pduId = pDUDeviceService.deletePDU(devKey);
        if (pduId == -1) {
            return error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "删除失败");
        }
        return success(pduId);
    }

    @GetMapping("/restore")
    @Operation(summary = "恢复设备")
    public CommonResult<Integer> restorePDU(@Param("devKey") String devKey) throws Exception {
        int pduId = pDUDeviceService.restorePDU(devKey);
        if (pduId == -1) {
            return error(GlobalErrorCodeConstants.UNKNOWN.getCode(), "恢复失败");
        }
        return success(pduId);
    }

    @PostMapping("line/getMaxLine")
    @Operation(summary = "PDU需量详情数据")
    public CommonResult<Map> getPduMaxLine(@RequestBody PDURequireDetailReq requireDetailReq) {
        return success(pDUDeviceService.getPduMaxLine(requireDetailReq));
    }
}